package com.example.fitnessapp.ai.data

// Временно отключаем импорты Cactus AI
// import com.cactus.CactusLM
// import com.cactus.CactusInitParams
// import com.cactus.ChatMessage as CactusChatMessage
import com.example.fitnessapp.ai.domain.models.AiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CactusAiRepository {
    // Временно отключаем Cactus AI
    // private var lm: CactusLM? = null
    
//    suspend fun initializeModel() = withContext(Dispatchers.IO) {
//        try {
//            // Временно отключаем инициализацию
//            /*
//            if (lm == null) {
//                lm = CactusLM()
//                lm?.downloadModel("qwen3-0.6") // 270MB модель
//                lm?.initializeModel(CactusInitParams(model = "qwen3-0.6"))
//            }
//            */
//            throw Exception("Cactus AI временно отключен")
//        } catch (e: Exception) {
//            throw Exception("Ошибка инициализации AI: ${e.message}")
//        }
//    }
    
    suspend fun generateResponse(request: AiRequest): String = withContext(Dispatchers.IO) {
        try {
            // Временно отключаем генерацию ответа
            /*
            initializeModel()
            
            val result = lm?.generateCompletion(
                messages = listOf(
                    CactusChatMessage(
                        content = "${request.context}\n\nВопрос: ${request.message}",
                        role = "user"
                    )
                )
            )
            
            result?.response ?: "Извините, не удалось получить ответ"
            */
            "Cactus AI временно отключен. Ответ: ${request.message}"
        } catch (e: Exception) {
            "Ошибка AI: ${e.message}"
        }
    }
    
    fun cleanup() {
        // Временно отключаем очистку
        // lm?.unload()
        // lm = null
    }
}
