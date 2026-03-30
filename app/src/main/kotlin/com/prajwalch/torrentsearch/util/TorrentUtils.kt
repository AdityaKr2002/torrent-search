package com.prajwalch.torrentsearch.util

object TorrentUtils {
    private const val MAGNET_URI_PREFIX = "magnet:?xt=urn:btih:"

    fun getInfoHashFromMagnetUri(magnetUri: String): String {
        require(magnetUri.startsWith(MAGNET_URI_PREFIX)) {
            "Can't extract info hash from '$magnetUri'"
        }

        return magnetUri.removePrefix(MAGNET_URI_PREFIX).takeWhile { it != '&' }
    }

    fun generateFileDownloadLinks(infoHash: String): Set<String> {
        val infoHash = infoHash.uppercase()

        return setOf(
            "https://itorrents.net/torrent/$infoHash.torrent",
            "https://torrage.info/torrent.php?h=$infoHash",
        )
    }
}