package com.example.fitnessapp.di

import android.content.Context
import com.example.fitnessapp.ai.data.CactusAiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    
    @Provides
    @Singleton
    fun provideAiRepository(@ApplicationContext context: Context): CactusAiRepository {
        return CactusAiRepository(context)
    }
}
