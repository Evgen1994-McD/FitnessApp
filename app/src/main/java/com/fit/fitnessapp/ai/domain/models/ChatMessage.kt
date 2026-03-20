package com.fit.fitnessapp.ai.domain.models

data class ChatMessage(
    val content: String,
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
