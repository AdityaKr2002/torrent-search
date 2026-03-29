package com.prajwalch.torrentsearch.di

import com.prajwalch.torrentsearch.providers.BuiltinSearchProviders
import com.prajwalch.torrentsearch.providers.DefaultEnabledProviderIds
import com.prajwalch.torrentsearch.providers.SearchProvider
import com.prajwalch.torrentsearch.providers.SearchProviderId

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BuiltinSearchProvidersModule {
    @Provides
    @Singleton
    fun provideBuiltinSearchProviders(): List<SearchProvider> = BuiltinSearchProviders

    @Provides
    @Singleton
    fun provideDefaultEnabledProviderIds(): Set<SearchProviderId> = DefaultEnabledProviderIds
}