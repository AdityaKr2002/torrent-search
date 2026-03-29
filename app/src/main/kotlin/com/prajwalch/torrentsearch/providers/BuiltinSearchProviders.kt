package com.prajwalch.torrentsearch.providers

val BuiltinSearchProviders = listOf(
    AniRena(),
    AnimeTosho(),
    BitSearch(),
    Dmhy(),
    Eztv(),
    FileMood(),
    InternetArchive(),
    Knaben(),
    LimeTorrents(),
    MyPornClub(),
    Nyaa(),
    SubsPlease(),
    Sukebei(),
    ThePirateBay(),
    TheRarBg(),
    TokyoToshokan(),
    TorrentDatabase(),
    TorrentDownload(),
    TorrentDownloads(),
    TorrentsCSV(),
    UIndex(),
    XXXClub(),
    XXXTracker(),
    Yts(),
)

val DefaultEnabledProviderIds = BuiltinSearchProviders
    .filter { it.info.enabledByDefault }
    .map { it.info.id }
    .toSet()