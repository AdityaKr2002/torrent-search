package com.prajwalch.torrentsearch.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SearchResults(
    val successes: ImmutableList<Torrent> = persistentListOf(),
    val failures: ImmutableList<SearchException> = persistentListOf(),
)