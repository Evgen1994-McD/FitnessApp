package com.example.fitnessapp.ai.domain.models

data class AiRequest(
    val message: String,
    val context: String = "Ты - фитнес-тренер. Отвечай кратко и по делу на русском языке."
)
