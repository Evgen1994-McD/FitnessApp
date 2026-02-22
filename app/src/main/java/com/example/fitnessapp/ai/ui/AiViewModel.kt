package com.example.fitnessapp.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.ai.data.CactusAiRepository
import com.example.fitnessapp.ai.domain.models.AiRequest
import com.example.fitnessapp.ai.domain.models.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: CactusAiRepository
) : ViewModel() {

    // Состояние сообщений
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Текст ввода
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Состояние инициализации модели
    private val _isModelInitializing = MutableStateFlow(false)
    val isModelInitializing: StateFlow<Boolean> = _isModelInitializing.asStateFlow()

    // Прогресс загрузки модели
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    // Флаг, что модель готова
    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady.asStateFlow()

    // Сообщение об ошибке
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Прогреваем модель при создании ViewModel
        warmUpModel()
    }

    /**
     * Прогрев модели (загрузка в фоне)
     */
    private fun warmUpModel() {
        viewModelScope.launch {
            _isModelInitializing.value = true
            _errorMessage.value = null

            try {
                // Подписываемся на прогресс загрузки
                launch {
                    aiRepository.downloadProgress.collect { progress ->
                        _downloadProgress.value = progress
                    }
                }

                // Запускаем инициализацию
                aiRepository.ensureModelInitialized()
                _isModelReady.value = true

                // Можно добавить приветственное сообщение
                _messages.value = listOf(
                    ChatMessage(
                        content = "Привет! Я твой AI-помощник по тренировкам. Задай мне любой вопрос!",
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                )

            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки модели: ${e.message}"
            } finally {
                _isModelInitializing.value = false
            }
        }
    }

    /**
     * Отправка сообщения
     */
    fun sendMessage(message: String) {
        if (message.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            // Добавляем сообщение пользователя
            val userMessage = ChatMessage(
                content = message,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )
            _messages.update { it + userMessage }

            // Очищаем поле ввода
            _inputText.value = ""

            // Показываем загрузку
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Проверяем, готова ли модель
                if (!_isModelReady.value) {
                    throw IllegalStateException("Модель ещё не загружена")
                }

                // Получаем ответ AI
                val aiResponse = aiRepository.generateResponse(
                    request = AiRequest(
                        message = message,
                        context = buildContext()
                    )
                )

                // Добавляем ответ AI
                val aiMessage = ChatMessage(
                    content = aiResponse,
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.update { it + aiMessage }

            } catch (e: Exception) {
                _errorMessage.value = e.message

                // Добавляем сообщение об ошибке
                val errorMessage = ChatMessage(
                    content = "⚠️ Ошибка: ${e.message}",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.update { it + errorMessage }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Формирование контекста для AI
     */
    private fun buildContext(): String {
        return """
            Ты - профессиональный фитнес-тренер и эксперт по здоровому образу жизни.
            Отвечай на русском языке, будь дружелюбным и мотивирующим.
            Давай конкретные, практические советы по тренировкам.
            Если спрашивают не о фитнесе, вежливо направляй в тему тренировок.
        """.trimIndent()
    }

    /**
     * Обновление текста ввода
     */
    fun updateInputText(text: String) {
        _inputText.value = text
    }

    /**
     * Отправка сообщения по нажатию Enter
     */
    fun onSendAction() {
        sendMessage(_inputText.value)
    }

    /**
     * Очистка истории сообщений
     */
    fun clearChat() {
        _messages.value = emptyList()
        warmUpModel() // Показываем приветствие снова
    }

    /**
     * Повторная отправка последнего сообщения (при ошибке)
     */
    fun retryLastMessage() {
        val lastUserMessage = _messages.value.lastOrNull { it.isUser }
        lastUserMessage?.content?.let { sendMessage(it) }
    }

    /**
     * Сброс модели (для отладки)
     */
    fun resetModel() {
        viewModelScope.launch {
            _isModelReady.value = false
            aiRepository.resetModel()
            warmUpModel()
        }
    }

    /**
     * Очистка ошибки
     */
    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        // Освобождаем ресурсы при уничтожении ViewModel
        // (только если приложение закрывается)
        if (!isAppInBackground()) {
            aiRepository.unloadModel()
        }
        super.onCleared()
    }

    /**
     * Эвристика: проверяем, не в фоне ли приложение
     * (упрощённая версия)
     */
    private fun isAppInBackground(): Boolean {
        // В реальном приложении можно использовать ProcessLifecycleOwner
        return false
    }
}

// Добавьте это в domain/models/ChatMessage.kt
/*
package com.example.fitnessapp.ai.domain.models

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
*/