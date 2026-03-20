package com.prajwalch.torrentsearch.util

import com.prajwalch.torrentsearch.domain.model.SortCriteria
import com.prajwalch.torrentsearch.domain.model.SortOrder
import com.prajwalch.torrentsearch.domain.model.Torrent

fun createSortComparator(criteria: SortCriteria, order: SortOrder): Comparator<Torrent> {
    val comparator: Comparator<Torrent> = when (criteria) {
        SortCriteria.Name -> compareBy { it.name }
        SortCriteria.Seeders -> compareBy { it.seeders }
        SortCriteria.Peers -> compareBy { it.peers }
        SortCriteria.FileSize -> compareBy { FileSizeUtils.getBytes(it.size) }
        SortCriteria.Date -> compareBy { it.uploadDate }
    }

    return when (order) {
        SortOrder.Ascending -> comparator
        SortOrder.Descending -> comparator.reversed()
    }
}