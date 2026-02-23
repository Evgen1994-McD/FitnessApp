package com.example.fitnessapp.ai.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.ai.data.CactusAiRepository
import com.example.fitnessapp.ai.domain.models.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.UInt
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: CactusAiRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Для стриминга - храним текущее сообщение AI
    private var currentAiMessage = ""

    init {
        warmUpModel()
    }

    private fun warmUpModel() {
        viewModelScope.launch {
            try {
                // Проверяем, не инициализирована ли уже модель
                if (!aiRepository.isModelReady()) {
                    Log.d("AiViewModel", "🔄 Модель не готова, инициализируем...")
                    aiRepository.ensureModelInitialized()
                } else {
                    Log.d("AiViewModel", "✅ Модель уже готова, пропускаем инициализацию")
                }
                
                _messages.value = listOf(
                    ChatMessage(
                        content = "Привет! Я твой AI-помощник по тренировкам. Задай мне любой вопрос!",
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                _messages.value = listOf(
                    ChatMessage(
                        content = "Ошибка загрузки модели: ${e.message}",
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            val userMessage = ChatMessage(
                content = message,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )
            _messages.update { it + userMessage }

            _inputText.value = ""
            _isLoading.value = true

            try {
                // Проверяем готовность модели
                if (!aiRepository.isModelReady()) {
                    Log.d("AiViewModel", "🔄 Модель не готова, инициализируем перед отправкой...")
                    aiRepository.ensureModelInitialized()
                }

                // Создаем пустое сообщение AI для стриминга
                val aiMessage = ChatMessage(
                    content = "",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.update { it + aiMessage }
                currentAiMessage = ""

                // Генерируем ответ со стримингом
                val finalResponse = aiRepository.generateResponse(
                    userMessage = message,
                    onToken = { token, tokenId ->
                        // Обновляем сообщение по мере поступления токенов
                        currentAiMessage += token
                        _messages.update { messages ->
                            messages.map { msg ->
                                if (msg.timestamp == aiMessage.timestamp && !msg.isUser) {
                                    msg.copy(content = currentAiMessage)
                                } else {
                                    msg
                                }
                            }
                        }
                    }
                )

                // Финальное обновление (на случай если что-то пропустили)
                _messages.update { messages ->
                    messages.map { msg ->
                        if (msg.timestamp == aiMessage.timestamp && !msg.isUser) {
                            msg.copy(content = finalResponse)
                        } else {
                            msg
                        }
                    }
                }

            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "⚠️ Ошибка: ${e.message}",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.update { it + errorMessage }
            } finally {
                _isLoading.value = false
                currentAiMessage = ""
            }
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun onSendAction() {
        sendMessage(_inputText.value)
    }

    fun clearChat() {
        _messages.value = emptyList()
        warmUpModel()
    }

    override fun onCleared() {
        aiRepository.unloadModel()
        super.onCleared()
    }
}
