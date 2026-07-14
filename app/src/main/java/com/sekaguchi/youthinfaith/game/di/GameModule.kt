package com.sekaguchi.youthinfaith.game.di

import com.google.firebase.database.FirebaseDatabase
import com.sekaguchi.youthinfaith.game.data.GameRepository
import com.sekaguchi.youthinfaith.game.data.GameRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideGameRepository(db: FirebaseDatabase): GameRepository = GameRepositoryImpl(db)
}
