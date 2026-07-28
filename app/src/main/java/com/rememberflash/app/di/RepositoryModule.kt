package com.rememberflash.app.di

import com.rememberflash.app.data.ocr.MlKitTextExtractor
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.data.remote.gemini.GeminiScheduleClient
import com.rememberflash.app.data.repository.AuthRepositoryImpl
import com.rememberflash.app.data.repository.ContestRepositoryImpl
import com.rememberflash.app.data.repository.DisciplineRepositoryImpl
import com.rememberflash.app.data.repository.EssayRepositoryImpl
import com.rememberflash.app.data.repository.FlashcardRepositoryImpl
import com.rememberflash.app.data.repository.QuestionRepositoryImpl
import com.rememberflash.app.data.repository.ScheduleRepositoryImpl
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.EssayRepository
import com.rememberflash.app.domain.repository.FlashcardRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import com.rememberflash.app.domain.repository.ScheduleRepository
import com.rememberflash.app.domain.usecase.essay.EvaluateEssayUseCase
import com.rememberflash.app.domain.usecase.essay.ExtractTextFromImageUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindContestRepository(impl: ContestRepositoryImpl): ContestRepository

    @Binds
    @Singleton
    abstract fun bindDisciplineRepository(impl: DisciplineRepositoryImpl): DisciplineRepository

    @Binds
    @Singleton
    abstract fun bindFlashcardRepository(impl: FlashcardRepositoryImpl): FlashcardRepository

    @Binds
    @Singleton
    abstract fun bindEssayRepository(impl: EssayRepositoryImpl): EssayRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(impl: QuestionRepositoryImpl): QuestionRepository

    @Binds
    @Singleton
    abstract fun bindTextExtractor(impl: MlKitTextExtractor): ExtractTextFromImageUseCase.TextExtractor

    @Binds
    @Singleton
    abstract fun bindEssayEvaluator(impl: GeminiClient): EvaluateEssayUseCase.EssayEvaluator

    @Binds
    @Singleton
    abstract fun bindGeminiScheduleClient(impl: GeminiClient): GeminiScheduleClient
}
