package exh.recs

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import tachiyomi.domain.manga.model.Manga
import java.util.Collections
import java.util.LinkedHashMap

@Inject
@SingleIn(AppScope::class)
class RecommendationCache {

    private val websiteSuggestions = Collections.synchronizedMap(
        object : LinkedHashMap<Long, List<Manga>>(MAX_CACHE_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, List<Manga>>?): Boolean {
                return size > MAX_CACHE_ENTRIES
            }
        },
    )

    private val fullRecommendations = Collections.synchronizedMap(
        object : LinkedHashMap<Long, Map<String, List<Manga>>>(MAX_CACHE_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Map<String, List<Manga>>>?): Boolean {
                return size > MAX_CACHE_ENTRIES
            }
        },
    )

    fun getWebsiteSuggestions(mangaId: Long): List<Manga>? {
        return websiteSuggestions[mangaId]
    }

    fun putWebsiteSuggestions(mangaId: Long, mangas: List<Manga>) {
        websiteSuggestions[mangaId] = mangas
    }

    fun getRecommendations(mangaId: Long): Map<String, List<Manga>>? {
        return fullRecommendations[mangaId]
    }

    fun putRecommendations(mangaId: Long, sourceKey: String, mangas: List<Manga>) {
        val current = fullRecommendations[mangaId]?.toMutableMap() ?: mutableMapOf()
        current[sourceKey] = mangas
        fullRecommendations[mangaId] = current
    }

    fun clear() {
        websiteSuggestions.clear()
        fullRecommendations.clear()
    }

    companion object {
        private const val MAX_CACHE_ENTRIES = 25
    }
}
