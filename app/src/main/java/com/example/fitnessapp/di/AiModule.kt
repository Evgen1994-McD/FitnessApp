package com.example.fitnessapp.di

import com.example.fitnessapp.ai.data.CactusAiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton // Возвращаем javax.inject

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    
    @Provides
    @Singleton
    fun provideAiRepository(): CactusAiRepository {
        return CactusAiRepository()
    }
}
