package com.example.fitnessapp.ai.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.os.Environment
import com.cactus.CactusLM
import com.cactus.CactusInitParams
import com.cactus.ChatMessage as CactusChatMessage
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CactusAiRepository(
    private val context: Context,

) {
    private var lm: CactusLM? = null
    private val initMutex = Mutex()

    companion object {
        private const val TAG = "CactusAiRepo"
        private const val MODEL_SLUG = "qwen3-0.6"
    }

    // Переменная состояния должна быть вне companion object
    private var isModelInitialized = false

    init {
        Log.d(TAG, "🔧 Репозиторий создан")
        checkPermissions()
    }

    /**
     * Проверка прав на запись и доступности хранилища
     */
    private fun checkPermissions() {
        Log.d(TAG, "🔍 Проверяем права на запись...")
        
        // Проверяем состояние внешнего хранилища
        val externalStorageState = Environment.getExternalStorageState()
        Log.d(TAG, "   - Состояние внешнего хранилища: $externalStorageState")
        
        // Проверяем доступность внутреннего хранилища
        val filesDir = context.filesDir
        Log.d(TAG, "   - Внутренняя директория: ${filesDir.absolutePath}")
        Log.d(TAG, "   - Директория существует: ${filesDir.exists()}")
        Log.d(TAG, "   - Можно читать: ${filesDir.canRead()}")
        Log.d(TAG, "   - Можно писать: ${filesDir.canWrite()}")
        
        // Проверяем свободное место
        val freeSpace = filesDir.freeSpace
        val freeSpaceMb = freeSpace / (1024 * 1024)
        Log.d(TAG, "   - Свободное место: ${freeSpaceMb} MB")
        
        // Пробуем создать тестовый файл
        try {
            val testFile = java.io.File(filesDir, "test_write_permission.tmp")
            testFile.writeText("test")
            val canWrite = testFile.exists() && testFile.readText() == "test"
            testFile.delete()
            Log.d(TAG, "   - Тест записи: ${if (canWrite) "✅ Успешно" else "❌ Ошибка"}")
        } catch (e: Exception) {
            Log.e(TAG, "   - ❌ Тест записи не удался: ${e.message}")
        }
        
        Log.d(TAG, "Проверка прав завершена")
    }

    /**
     * Проверка, загружена ли модель
     */
    fun isModelLoaded(): Boolean = isModelInitialized && lm != null

    /**
     * Проверка, готова ли модель к использованию (без инициализации)
     */
    fun isModelReady(): Boolean = isModelInitialized && lm != null

    /**
     * Инициализация модели
     */
    suspend fun ensureModelInitialized() = withContext(Dispatchers.IO) {
        // Быстрый выход если уже инициализирована
        if (isModelInitialized && lm != null) {
            Log.d(TAG, "Модель уже инициализирована, пропускаем")
            return@withContext
        }
        
        initMutex.withLock {
            try {
                Log.d(TAG, "🚀 Начинаем инициализацию модели $MODEL_SLUG...")

                // Создаём CactusLM
                Log.d(TAG, "   📱 Создаём экземпляр CactusLM...")
                lm = CactusLM()
                Log.d(TAG, "   ✅ CactusLM создан успешно")

                // Скачиваем модель
                Log.d(TAG, "   ⬇️ Начинаем скачивание модели $MODEL_SLUG...")
                Log.d(TAG, "   - Это может занять время при первом запуске...")
                
                // Проверяем существует ли директория модели
                val modelsDir = java.io.File(context.filesDir, "models")
                val modelDir = java.io.File(modelsDir, MODEL_SLUG)
                
                if (modelDir.exists()) {
                    Log.d(TAG, "   - Директория модели уже существует: ${modelDir.absolutePath}")
                    Log.d(TAG, "   - Пропускаем скачивание, используем существующую модель")
                } else {
                    Log.d(TAG, "   - Директория модели не найдена, создаём новую")
                    modelsDir.mkdirs()
                    modelDir.mkdirs()
                    Log.d(TAG, "   - Создана директория: ${modelDir.absolutePath}")
                }
                
                val downloadStart = System.currentTimeMillis()
                
                // Проверяем, нужно ли скачивать модель
                if (modelDir.exists() && modelDir.listFiles()?.isNotEmpty() == true) {
                    Log.d(TAG, "   - Модель уже скачана, пропускаем downloadModel()")
                    Log.d(TAG, "   - Файлы в директории: ${modelDir.listFiles()?.size}")
                } else {
                    Log.d(TAG, "   - Модель не найдена, начинаем скачивание...")
                    lm?.downloadModel(MODEL_SLUG)
                }
                
                val downloadTime = System.currentTimeMillis() - downloadStart
                
                Log.d(TAG, "   ✅ Модель скачана за ${downloadTime}ms")
                Log.d(TAG, "   📁 Проверяем директорию после скачивания...")
                
                // Проверяем где сохранилась модель
                if (modelsDir.exists()) {
                    val modelFiles = modelsDir.listFiles()
                    Log.d(TAG, "   - Директория models существует: ${modelFiles?.size ?: 0} файлов/папок")
                    modelFiles?.forEach { file ->
                        val isDir = file.isDirectory
                        val size = if (isDir) {
                            file.walkTopDown().sumOf { it.length() } / (1024 * 1024)
                        } else {
                            file.length() / (1024 * 1024)
                        }
                        Log.d(TAG, "     - ${file.name} (${if (isDir) "папка" else "файл"}, ${size}MB)")
                    }
                } else {
                    Log.w(TAG, "   - ⚠️ Директория models не найдена после скачивания")
                }

                // Инициализируем модель
                Log.d(TAG, "   🔄 Инициализируем модель...")
                Log.d(TAG, "   - Параметры: model=$MODEL_SLUG, contextSize=2048")
                
                val initStart = System.currentTimeMillis()
                lm?.initializeModel(
                    CactusInitParams(
                        model = MODEL_SLUG,
                        contextSize = 2048
                    )
                )
                val initTime = System.currentTimeMillis() - initStart
                
                Log.d(TAG, "   ✅ Модель инициализирована за ${initTime}ms")
                Log.d(TAG, "🎉 Модель $MODEL_SLUG успешно инициализирована!")
                
                // Устанавливаем флаг успешной инициализации
                isModelInitialized = true

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка инициализации модели: ${e.message}")
                e.printStackTrace()
                
                // Если ошибка связана с файлами, очищаем директорию модели
                if (e.message?.contains("ENOTDIR") == true || 
                    e.message?.contains("FileNotFoundException") == true ||
                    e.message?.contains("corrupted") == true) {
                    Log.w(TAG, "🗑️ Обнаружена ошибка файлов, очищаем директорию модели...")
                    val modelsDir = java.io.File(context.filesDir, "models")
                    val modelDir = java.io.File(modelsDir, MODEL_SLUG)
                    if (modelDir.exists()) {
                        val deleted = modelDir.deleteRecursively()
                        Log.d(TAG, "   - Директория очищена: $deleted")
                    }
                }
                
                // Очищаем при ошибке
                lm?.unload()
                lm = null
                isModelInitialized = false
                throw Exception("Ошибка инициализации AI: ${e.message}")
            }
        }
    }

    /**
     * Генерация ответа
     */
    suspend fun generateResponse(userMessage: String): String = withContext(Dispatchers.IO) {
        ensureModelInitialized()

        if (lm == null) {
            throw Exception("Модель не инициализирована")
        }

        Log.d(TAG, "💬 Генерируем ответ на: \"$userMessage\"")

        try {
            val result = lm?.generateCompletion(
                messages = listOf(
                    CactusChatMessage(
                        content = userMessage,
                        role = "user"
                    )
                )
            )

            result?.let { response ->
                if (response.success) {
                    val answer = response.response
                    Log.d(TAG, "✅ Ответ сгенерирован")
                    Log.d(TAG, "📝 Ответ: \"$answer\"")
                    return@withContext answer ?: "Ничего не нашлось"
                } else {
                    throw Exception("Ошибка генерации: ${response}")
                }
            } ?: throw Exception("Пустой ответ от модели")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка генерации ответа: ${e.message}")
            throw e
        }
    }

    /**
     * Выгрузка модели
     */
    fun unloadModel() {
        Log.d(TAG, "🗑️ Выгружаем модель...")
        lm?.unload()
        lm = null
        isModelInitialized = false
        Log.d(TAG, "✅ Модель выгружена")
    }
}
