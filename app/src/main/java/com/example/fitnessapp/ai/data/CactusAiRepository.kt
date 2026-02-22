package com.example.fitnessapp.ai.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.cactus.CactusLM
import com.cactus.CactusCompletionParams
import com.cactus.CactusInitParams
import com.cactus.ChatMessage as CactusChatMessage
import com.cactus.CactusModelManager
import com.example.fitnessapp.ai.domain.models.AiRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class CactusAiRepository(private val context: Context) {
    private var lm: CactusLM? = null
    private var isModelInitialized = false

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("cactus_ai_prefs", Context.MODE_PRIVATE)
    }

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    // Mutex для потокобезопасности
    private val initMutex = Mutex()

    companion object {
        private const val KEY_MODEL_DOWNLOADED = "model_downloaded"
        private const val KEY_MODEL_PATH = "model_path"
        // ВАЖНО: Используем имя, под которым Cactus ожидает модель
        private const val MODEL_NAME = "gemma-3-270m-it-Q4_K_M.gguf"  // Полное имя файла
        private const val MODEL_SLUG = "gemma-3-270m-it"  // Slug для Cactus (без расширения)
        private const val MODEL_ASSETS_PATH = "llm/gemma-3-270m-it-Q4_K_M.gguf"  // Путь в assets
        private const val TAG = "CactusAiRepo"
        
        private const val GENERATION_TIMEOUT_MS = 45_000L
        private const val MAX_RESPONSE_CHARS = 1_500
        private const val MAX_TOKENS = 160
    }

    init {
        Log.d(TAG, "🔧 Репозиторий создан")
        // При создании репозитория проверяем статус модели
        checkModelStatus()
    }

    private fun checkModelStatus() {
        val isDownloaded = prefs.getBoolean(KEY_MODEL_DOWNLOADED, false)
        val modelPath = prefs.getString(KEY_MODEL_PATH, null)

        Log.d(TAG, "📊 Статус модели при создании репозитория:")
        Log.d(TAG, "   - isModelInitialized: $isModelInitialized")
        Log.d(TAG, "   - lm != null: ${lm != null}")
        Log.d(TAG, "   - prefs.isDownloaded: $isDownloaded")
        Log.d(TAG, "   - prefs.modelPath: $modelPath")
        Log.d(TAG, "   - MODEL_ASSETS_PATH: $MODEL_ASSETS_PATH")

        // Проверяем наличие модели в assets
        try {
            context.assets.open(MODEL_ASSETS_PATH).use { stream ->
                val size = stream.available()
                val sizeMb = size / (1024 * 1024)
                Log.d(TAG, "   ✅ Модель найдена в assets:")
                Log.d(TAG, "      - Путь: $MODEL_ASSETS_PATH")
                Log.d(TAG, "      - Размер: $size bytes ($sizeMb MB)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "   ❌ Модель не найдена в assets: $MODEL_ASSETS_PATH")
            Log.e(TAG, "   - Ошибка: ${e.message}")
        }
    }

    /**
     * Инициализирует модель из assets - копирует в files и использует оттуда
     */
    suspend fun ensureModelInitialized() = withContext(Dispatchers.IO) {
        val methodStart = System.currentTimeMillis()
        Log.d(TAG, "🟡 ensureModelInitialized() started")

        // Уже инициализировано? Быстрый выход
        if (isModelInitialized && lm != null) {
            val duration = System.currentTimeMillis() - methodStart
            Log.d(TAG, "✅ Модель уже инициализирована, выход (${duration}ms)")
            return@withContext
        }

        Log.d(TAG, "🔄 Модель не инициализирована, входим в Mutex...")

        // Используем Mutex, чтобы гарантировать только одну инициализацию
        initMutex.withLock {
            Log.d(TAG, "🔒 Mutex захвачен")

            // Проверяем ещё раз внутри блокировки (double-check)
            if (isModelInitialized && lm != null) {
                Log.d(TAG, "✅ Модель уже инициализирована (double-check), выходим")
                return@withLock
            }

            try {
                _isDownloading.value = true
                _downloadProgress.update { 0.1f }
                Log.d(TAG, "🚀 Начинаем инициализацию модели...")

                // Создаём экземпляр CactusLM
                Log.d(TAG, "   - Создаём CactusLM...")
                val createStart = System.currentTimeMillis()
                lm = CactusLM()
                val createTime = System.currentTimeMillis() - createStart
                Log.d(TAG, "   - CactusLM создан за ${createTime}ms")
                
                _downloadProgress.update { 0.3f }

                // Определяем пути
                val cactusModelsDir = File(context.filesDir, "models")
                val targetModelDir = File(cactusModelsDir, MODEL_SLUG)  // Cactus ожидает папку с именем slug
                val targetModelFile = File(targetModelDir, MODEL_NAME)  // GGUF файл внутри папки
                
                Log.d(TAG, "   - Целевая директория Cactus: ${targetModelDir.path}")
                
                // Создаём директорию если нужно
                targetModelDir.mkdirs()
                
                // Проверяем, есть ли уже файл
                if (!targetModelFile.exists() || targetModelFile.length() == 0L) {
                    Log.d(TAG, "   📋 Копируем модель из assets...")
                    
                    // Копируем из assets
                    try {
                        context.assets.open(MODEL_ASSETS_PATH).use { inputStream ->
                            targetModelFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        
                        val fileSize = targetModelFile.length()
                        val fileSizeMb = fileSize / (1024 * 1024)
                        Log.d(TAG, "   ✅ Модель скопирована: ${targetModelFile.path} (${fileSizeMb} MB)")
                        
                        if (fileSize < 100 * 1024 * 1024) { // Меньше 100 MB
                            throw Exception("Файл модели слишком маленький: $fileSize байт")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "   ❌ Ошибка копирования модели: ${e.message}")
                        throw e
                    }
                } else {
                    val fileSize = targetModelFile.length()
                    val fileSizeMb = fileSize / (1024 * 1024)
                    Log.d(TAG, "   ✅ Модель уже существует: ${targetModelFile.path} (${fileSizeMb} MB)")
                }
                
                _downloadProgress.update { 0.7f }

                // ВАЖНО: Теперь "скачиваем" модель через Cactus (он увидит существующий файл)
                Log.d(TAG, "   ⬇️ Вызываем downloadModel для регистрации в Cactus...")
                try {
                    lm?.downloadModel(MODEL_SLUG)
                    Log.d(TAG, "   ✅ Модель зарегистрирована в Cactus")
                } catch (e: Exception) {
                    Log.w(TAG, "   - downloadModel: ${e.message}")
                    // Игнорируем - модель уже может быть зарегистрирована
                }
                
                _downloadProgress.update { 0.9f }

                // Инициализируем модель
                Log.d(TAG, "   🔄 Инициализация модели...")
                val initStart = System.currentTimeMillis()
                
                lm?.initializeModel(
                    CactusInitParams(
                        model = MODEL_SLUG  // Используем slug, а не имя файла!
                    )
                )
                
                val initTime = System.currentTimeMillis() - initStart
                Log.d(TAG, "   ✅ Модель инициализирована за ${initTime}ms")

                _downloadProgress.update { 1.0f }
                isModelInitialized = true

                prefs.edit()
                    .putBoolean(KEY_MODEL_DOWNLOADED, true)
                    .putString(KEY_MODEL_PATH, targetModelFile.absolutePath)
                    .apply()

                val totalTime = System.currentTimeMillis() - methodStart
                Log.d(TAG, "🎉 Модель полностью готова! Общее время: ${totalTime}ms (${totalTime/1000} сек)")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка инициализации модели: ${e.message}")
                e.printStackTrace()

                lm?.unload()
                lm = null
                isModelInitialized = false
                throw Exception("Ошибка инициализации AI: ${e.message}")
            } finally {
                _isDownloading.value = false
                Log.d(TAG, "🔓 Mutex освобождён, _isDownloading = false")
            }
        }
    }

    /**
     * Генерирует ответ, используя уже загруженную модель
     */
    suspend fun generateResponse(request: AiRequest): String = withContext(Dispatchers.IO) {
        val requestId = System.currentTimeMillis().toString().takeLast(4)
        val methodStart = System.currentTimeMillis()

        Log.d(TAG, "📝 [$requestId] generateResponse() started")
        Log.d(TAG, "   - Message: \"${request.message.take(50)}${if (request.message.length > 50) "..." else ""}\"")
        Log.d(TAG, "   - Context length: ${request.context?.length ?: 0} chars")

        try {
            // Гарантируем, что модель инициализирована (но не качаем заново)
            Log.d(TAG, "   [$requestId] Вызываем ensureModelInitialized()...")
            val ensureStart = System.currentTimeMillis()
            ensureModelInitialized()
            val ensureTime = System.currentTimeMillis() - ensureStart
            Log.d(TAG, "   [$requestId] ensureModelInitialized() завершён за ${ensureTime}ms")

            // Проверяем, что модель действительно загружена
            if (lm == null) {
                Log.e(TAG, "   [$requestId] lm == null после ensureModelInitialized!")
                return@withContext "Модель AI не инициализирована"
            }

            Log.d(TAG, "   [$requestId] lm != null, isModelInitialized = $isModelInitialized")

            // Генерируем ответ
            Log.d(TAG, "   [$requestId] Вызываем model.generateCompletion()...")
            val genStart = System.currentTimeMillis()

            val result = try {
                withTimeout(GENERATION_TIMEOUT_MS) {
                    lm?.generateCompletion(
                        messages = listOf(
                            CactusChatMessage(
                                content = buildPrompt(request),
                                role = "user"
                            )
                        ),
                        params = CactusCompletionParams(
                            maxTokens = MAX_TOKENS
                        )
                    )
                }
            } catch (e: TimeoutCancellationException) {
                val elapsed = System.currentTimeMillis() - genStart
                Log.e(TAG, "[$requestId] Таймаут генерации через ${elapsed}ms")
                return@withContext "Ответ генерируется слишком долго. Попробуйте более короткий вопрос."
            }

            val genTime = System.currentTimeMillis() - genStart
            val totalTime = System.currentTimeMillis() - methodStart

            if (result != null) {
                Log.d(TAG, "[$requestId] Генерация завершена:")
                Log.d(TAG, "   - Время генерации: ${genTime}ms (${genTime/1000} сек)")
                Log.d(TAG, "   - Общее время: ${totalTime}ms (${totalTime/1000} сек)")
                Log.d(TAG, "   - Скорость: ${result.tokensPerSecond} токенов/сек")
                Log.d(TAG, "   - Время до первого токена: ${result.timeToFirstTokenMs}ms")
                Log.d(TAG, "   - Всего токенов: ${result.totalTokens}")
                Log.d(TAG, "   - Длина ответа: ${result.response?.length ?: 0} символов")
                Log.d(TAG, "   - Ответ: \"${result.response?.take(100)}${if ((result.response?.length ?: 0) > 100) "..." else ""}\"")
            } else {
                Log.e(TAG, "[$requestId] generateCompletion() вернул null")
            }

            val response = result?.response
            if (response.isNullOrBlank()) {
                return@withContext "Извините, не удалось получить ответ"
            }

            sanitizeResponse(response)

        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - methodStart
            Log.e(TAG, "[$requestId] Ошибка через ${totalTime}ms: ${e.message}")
            e.printStackTrace()
            "Ошибка AI: ${e.message}"
        }
    }

    private fun buildPrompt(request: AiRequest): String {
        return buildString {
            if (!request.context.isNullOrBlank()) {
                append(request.context)
                append("\n\n")
            }
            append("Правила ответа: отвечай кратко (1-5 предложений). Не показывай размышления, скрытые рассуждения или теги </think>.\n")
            append("Вопрос: ${request.message}")
        }
    }

    private fun sanitizeResponse(text: String): String {
        val withoutThink = text
            .replace(Regex("(?s)</think>.*?</think>"), "")
            .replace(Regex("(?s)</think>.*"), "")
            .trim()

        return if (withoutThink.length <= MAX_RESPONSE_CHARS) {
            withoutThink
        } else {
            withoutThink.take(MAX_RESPONSE_CHARS).trimEnd()
        }
    }

    /**
     * Полностью выгружает модель из памяти (освобождает ресурсы)
     * Вызывать, например, в onDestroy ViewModel или при выходе из приложения
     */
    fun unloadModel() {
        Log.d(TAG, "🧹 unloadModel() вызван")
        Log.d(TAG, "   - Было: isModelInitialized=$isModelInitialized, lm=${if (lm != null) "non-null" else "null"}")

        lm?.unload()
        lm = null
        isModelInitialized = false
        _downloadProgress.value = 0f
        _isDownloading.value = false

        Log.d(TAG, "   - Стало: isModelInitialized=$isModelInitialized, lm=${if (lm != null) "non-null" else "null"}")
    }

    /**
     * Сброс состояния модели (для отладки или принудительной перезагрузки)
     */
    fun resetModel() {
        Log.d(TAG, "🔄 resetModel() вызван - полный сброс")
        Log.d(TAG, "   - Очищаем SharedPreferences")

        prefs.edit()
            .remove(KEY_MODEL_DOWNLOADED)
            .remove(KEY_MODEL_PATH)
            .apply()

        unloadModel()

        Log.d(TAG, "✅ resetModel() завершён")
    }

    /**
     * Проверяет, загружена ли модель в данный момент
     */
    fun isModelLoaded(): Boolean {
        val loaded = isModelInitialized && lm != null
        Log.d(TAG, "🔍 isModelLoaded() = $loaded")
        return loaded
    }

    /**
     * Прогрев модели (опционально) - можно вызвать заранее, до первого запроса
     */
    suspend fun warmUp() {
        Log.d(TAG, "🔥 warmUp() вызван")
        val start = System.currentTimeMillis()
        ensureModelInitialized()
        val duration = System.currentTimeMillis() - start
        Log.d(TAG, "✅ warmUp() завершён за ${duration}ms")
    }

    /**
     * Проверяет и исправляет пустые файлы модели
     */
    suspend fun validateAndFixModelFiles(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔧 validateAndFixModelFiles() вызван")
        
        try {
            val cactusModelsDir = File(context.filesDir, "models")
            val targetModelDir = File(cactusModelsDir, MODEL_SLUG)
            val targetModelFile = File(targetModelDir, MODEL_NAME)
            val prefsDownloaded = prefs.getBoolean(KEY_MODEL_DOWNLOADED, false)
            val modelManagerDownloaded = CactusModelManager.isModelDownloaded(MODEL_SLUG)
            
            Log.d(TAG, "   - prefsDownloaded: $prefsDownloaded")
            Log.d(TAG, "   - modelManagerDownloaded: $modelManagerDownloaded")
            Log.d(TAG, "   - targetModelFile.exists(): ${targetModelFile.exists()}")
            Log.d(TAG, "   - targetModelFile.length(): ${targetModelFile.length()} bytes")
            
            when {
                !modelManagerDownloaded -> {
                    Log.d(TAG, "   ❌ ModelManager говорит что модели нет, сбрасываем статус")
                    prefs.edit()
                        .remove(KEY_MODEL_DOWNLOADED)
                        .remove(KEY_MODEL_PATH)
                        .apply()
                    // Также удаляем возможный пустой файл
                    if (targetModelFile.exists()) {
                        targetModelFile.delete()
                        Log.d(TAG, "   - Удалён пустой файл модели")
                    }
                    false
                }
                targetModelFile.length() < 1024 * 1024 -> { // Меньше 1MB
                    Log.d(TAG, "   ❌ Файл модели слишком маленький, удаляем и сбрасываем статус")
                    targetModelFile.delete()
                    CactusModelManager.deleteModel(MODEL_SLUG) // Удаляем через ModelManager
                    prefs.edit()
                        .remove(KEY_MODEL_DOWNLOADED)
                        .remove(KEY_MODEL_PATH)
                        .apply()
                    false
                }
                !prefsDownloaded -> {
                    Log.d(TAG, "   ✅ Файл существует и не пустой, восстанавливаем статус")
                    prefs.edit()
                        .putBoolean(KEY_MODEL_DOWNLOADED, true)
                        .putString(KEY_MODEL_PATH, targetModelFile.absolutePath)
                        .apply()
                    true
                }
                else -> {
                    Log.d(TAG, "   ✅ Всё в порядке")
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "   ❌ Ошибка при проверке: ${e.message}")
            false
        }
    }

    /**
     * Диагностика - показывает детальную информацию о состоянии
     */
    suspend fun diagnose(): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔬 Запуск диагностики...")

        val result = buildString {
            appendLine("=== ДИАГНОСТИКА CACTUS AI ===")
            appendLine("📱 isModelInitialized: $isModelInitialized")
            appendLine("📱 lm != null: ${lm != null}")

            val isDownloaded = prefs.getBoolean(KEY_MODEL_DOWNLOADED, false)
            val modelPath = prefs.getString(KEY_MODEL_PATH, null)
            appendLine("📱 prefs.model_downloaded: $isDownloaded")
            appendLine("📱 prefs.model_path: $modelPath")

            try {
                val filesDir = context.filesDir
                val modelDir = File(filesDir, "models")
                appendLine("📁 Директория приложения: ${filesDir.path}")
                appendLine("📁 Директория моделей: ${modelDir.path}")

                if (modelDir.exists()) {
                    val files = modelDir.listFiles()
                    appendLine("📊 Файлов в директории: ${files?.size ?: 0}")

                    files?.forEach { file ->
                        val sizeBytes = file.length()
                        val sizeMb = sizeBytes / (1024 * 1024)
                        val lastModified = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(java.util.Date(file.lastModified()))
                        val status = if (sizeBytes == 0L) "❌ ПУСТОЙ" else "✅ OK"
                        appendLine("   - ${file.name} (${sizeBytes} bytes = ${sizeMb} MB, изменён: $lastModified) $status")
                    }
                } else {
                    appendLine("❌ Директория моделей не найдена")
                }
            } catch (e: Exception) {
                appendLine("❌ Ошибка при проверке файлов: ${e.message}")
            }

            appendLine("================================")
        }

        Log.d(TAG, "\n" + result)
        return@withContext result
    }

    /**
     * Принудительное скачивание модели через OkHttp
     */
    suspend fun forceDownloadModel() = withContext(Dispatchers.IO) {
        Log.d(TAG, "🚀 Начинаем принудительное скачивание модели $MODEL_NAME...")
        
        val modelUrl = "https://huggingface.co/cactuscompute/gemma3-270m-int8/resolve/main/gemma3-270m-int8.bin"
        val modelFile = File(context.filesDir, "models/$MODEL_NAME")
        
        // Создаём директорию
        modelFile.parentFile?.mkdirs()
        
        // Удаляем старый повреждённый файл
        if (modelFile.exists()) {
            modelFile.delete()
            Log.d(TAG, "   - Удалён старый файл модели")
        }
        
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)  // ВАЖНО!
            .followSslRedirects(true) // ВАЖНО!
            .build()
        
        val request = Request.Builder()
            .url(modelUrl)
            .header("User-Agent", "Mozilla/5.0 (Android)") // Некоторые CDN требуют
            .build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to download: ${response.code}")
                }
                
                val contentLength = response.body?.contentLength() ?: -1
                Log.d(TAG, "   - Размер на сервере: ${contentLength / 1024 / 1024} MB")
                
                response.body?.byteStream()?.use { inputStream ->
                    modelFile.outputStream().use { outputStream ->
                        val totalBytes = inputStream.copyTo(outputStream)
                        Log.d(TAG, "   - Скачано байт: $totalBytes (${totalBytes / 1024 / 1024} MB)")
                    }
                }
            }
            
            // Проверяем результат
            if (modelFile.length() < 100 * 1024 * 1024) { // меньше 100 МБ
                throw Exception("Файл слишком маленький: ${modelFile.length()} байт")
            }
            
            // Обновляем SharedPreferences
            prefs.edit()
                .putBoolean(KEY_MODEL_DOWNLOADED, true)
                .putString(KEY_MODEL_PATH, MODEL_NAME)
                .apply()
            
            Log.d(TAG, "✅ Модель скачана вручную: ${modelFile.length() / 1024 / 1024} MB")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка при скачивании модели: ${e.message}")
            // Удаляем возможно частично скачанный файл
            if (modelFile.exists()) {
                modelFile.delete()
            }
            throw e
        }
    }
}
