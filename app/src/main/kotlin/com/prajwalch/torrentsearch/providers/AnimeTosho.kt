package com.prajwalch.torrentsearch.providers

import com.prajwalch.torrentsearch.domain.model.Category
import com.prajwalch.torrentsearch.domain.model.Torrent
import com.prajwalch.torrentsearch.util.DateUtils
import com.prajwalch.torrentsearch.util.TorrentUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class AnimeTosho : SearchProvider {
    override val info = SearchProviderInfo(
        id = "animetosho",
        name = "AnimeTosho",
        url = "https://animetosho.org",
        specializedCategory = Category.Anime,
        safetyStatus = SearchProviderSafetyStatus.Safe,
        enabledByDefault = true,
    )

    override suspend fun search(query: String, context: SearchContext): List<Torrent> {
        val requestUrl = "${info.url}/search?q=$query"
        val responseHtml = context.httpClient.get(url = requestUrl)

        return withContext(Dispatchers.Default) {
            parseHtml(html = responseHtml)
        }
    }

    /** Parses the entire result HTML and returns all the extracted torrents. */
    private fun parseHtml(html: String): List<Torrent> {
        return Jsoup
            .parse(html)
            .select("div.home_list_entry")
            .mapNotNull { parseEntryDiv(it) }
    }

    /** Parses an individual result row into a [Torrent] object. */
    private fun parseEntryDiv(entryDiv: Element): Torrent? {
        val anchor = entryDiv.selectFirst("div.link > a") ?: return null
        val name = anchor.text()
        val descriptionPageUrl = anchor.attr("href")

        val size = entryDiv.selectFirst("div.size")?.ownText() ?: return null
        val (seeders, peers) = parseSeedsAndPeers(entryDiv)

        val uploadDate = parseUploadDate(entryDiv) ?: return null

        val links = entryDiv.selectFirst("div.links") ?: return null
        val fileDownloadLink = links.selectFirst("a.dllink")?.attr("href")
        val magnetUri = links.selectFirst("""a[href^="magnet:"]""")?.attr("href") ?: return null
        val infoHash = TorrentUtils.getInfoHashFromMagnetUri(magnetUri)

        return Torrent(
            infoHash = infoHash,
            name = name,
            size = size,
            seeders = seeders,
            peers = peers,
            providerName = info.name,
            uploadDate = uploadDate,
            category = info.specializedCategory,
            descriptionPageUrl = descriptionPageUrl,
            magnetUri = magnetUri,
            fileDownloadLink = fileDownloadLink,
        )
    }

    /** Parses the upload date and converts "Today"/"Yesterday" into real dates. */
    private fun parseUploadDate(entryDiv: Element): String? {
        val raw = entryDiv
            .selectFirst("div.date")
            ?.attr("title")
            ?.removePrefix(DATE_PREFIX)
            ?.trim()
            ?: return null

        return when {
            raw.startsWith("Today") -> DateUtils.formatTodayDate()
            raw.startsWith("Yesterday") -> DateUtils.formatYesterdayDate()
            else -> {
                raw
                    .split(' ', limit = 2)
                    .firstOrNull()
                    ?.let { DateUtils.formatDayMonthYear(it) }
                    ?: raw
            }
        }
    }

    /** Extracts seeds and peers from the stats block. */
    private fun parseSeedsAndPeers(entryDiv: Element): Pair<UInt, UInt> {
        val span = entryDiv
            .selectFirst("div.links")
            ?.select("span")
            ?.firstOrNull { span -> span.hasAttr("title") }
            ?: return Pair(0u, 0u)
        val spanText = span.ownText()

        val match = STATS_REGEX.find(spanText)
        val seeds = match?.groupValues?.getOrNull(1)?.toUIntOrNull() ?: 0u
        val peers = match?.groupValues?.getOrNull(2)?.toUIntOrNull() ?: 0u

        return seeds to peers
    }

    private companion object {
        private const val DATE_PREFIX = "Date/time submitted: "
        private val STATS_REGEX = """\[(\d+)↑/(\d+)↓]""".toRegex()
    }
}