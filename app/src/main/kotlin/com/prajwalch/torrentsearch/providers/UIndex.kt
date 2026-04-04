package com.prajwalch.torrentsearch.providers

import com.prajwalch.torrentsearch.domain.model.Category
import com.prajwalch.torrentsearch.domain.model.Torrent
import com.prajwalch.torrentsearch.util.TorrentUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class UIndex : SearchProvider {
    override val info = SearchProviderInfo(
        id = "uindex",
        name = "UIndex",
        url = "https://uindex.org",
        specializedCategory = Category.All,
        safetyStatus = SearchProviderSafetyStatus.Safe,
        enabledByDefault = true,
    )

    override suspend fun search(query: String, context: SearchContext): List<Torrent> {
        val requestUrl = buildString {
            append(info.url)
            append("/search.php")
            append("?search=$query")

            val categoryIndex = getCategoryIndex(category = context.category)
            append("&c=$categoryIndex")
        }

        val responseHtml = context.httpClient.get(url = requestUrl)
        val torrents = withContext(Dispatchers.Default) {
            parseHtml(html = responseHtml)
        }

        return torrents.orEmpty()
    }

    private fun getCategoryIndex(category: Category): Int = when (category) {
        Category.All, Category.Books -> 0
        Category.Anime -> 7
        Category.Apps -> 5
        Category.Games -> 3
        Category.Movies -> 1
        Category.Music -> 4
        Category.Porn -> 6
        Category.Series -> 2
        Category.Other -> 8
    }

    private fun parseHtml(html: String): List<Torrent>? {
        return Jsoup
            .parse(html)
            .selectFirst("table.sr-table > tbody")
            ?.children()
            ?.mapNotNull { parseTableRow(tr = it) }
    }

    private fun parseTableRow(tr: Element): Torrent? {
        val categoryString = tr
            .selectFirst("td.sr-col-cat")
            ?.selectFirst("a")
            ?.ownText()
            ?: return null
        val category = getCategoryFromString(string = categoryString)

        // It contains magnet link and torrent name.
        val secondTd = tr.selectFirst("td.sr-col-name") ?: return null
        val magnetUri = secondTd.selectFirst("a.sr-magnet")?.attr("href") ?: return null
        val infoHash = TorrentUtils.getInfoHashFromMagnetUri(magnetUri)
        // Anchor which contains a name and description page URL.
        val nameHref = secondTd.selectFirst("a.sr-torrent-link") ?: return null
        val torrentName = nameHref.text()
        val descriptionPageUrl = info.url + nameHref.attr("href")

        val size = tr.selectFirst("td.sr-col-size")?.ownText() ?: return null
        val uploadDate = tr.selectFirst("td.sr-col-uploaded")?.ownText() ?: return null
        val seeders = tr
            .selectFirst("td.sr-col-seeders")
            ?.selectFirst("span")
            ?.ownText()
            ?.filter { it != ',' }
            ?: return null
        val peers = tr
            .selectFirst("td.sr-col-leechers")
            ?.selectFirst("span")
            ?.ownText()
            ?.filter { it != ',' }
            ?: return null

        return Torrent(
            infoHash = infoHash,
            name = torrentName,
            size = size,
            seeders = seeders.toUIntOrNull() ?: 0u,
            peers = peers.toUIntOrNull() ?: 0u,
            providerName = info.name,
            uploadDate = uploadDate,
            category = category,
            descriptionPageUrl = descriptionPageUrl,
            magnetUri = magnetUri,
        )
    }

    private fun getCategoryFromString(string: String): Category = when (string) {
        "Anime" -> Category.Anime
        "Apps" -> Category.Apps
        "Games" -> Category.Games
        "Movies" -> Category.Movies
        "Music" -> Category.Music
        "XXX" -> Category.Porn
        "TV" -> Category.Series
        "Other" -> Category.Other
        else -> Category.Other
    }
}