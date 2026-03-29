package com.prajwalch.torrentsearch.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService

import com.prajwalch.torrentsearch.network.ConnectivityChecker
import com.prajwalch.torrentsearch.network.HttpClient

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService<ConnectivityManager>()!!

    @Provides
    fun provideConnectivityChecker(
        connectivityManager: ConnectivityManager,
    ): ConnectivityChecker = ConnectivityChecker(
        connectivityManager = connectivityManager
    )

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient
}