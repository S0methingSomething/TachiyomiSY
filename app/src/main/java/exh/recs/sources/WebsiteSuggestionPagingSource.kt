package exh.recs.sources

import dev.icerock.moko.resources.StringResource
import eu.kanade.domain.manga.model.toSManga
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.data.source.NoResultsException
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.sy.SYMR

/**
 * SY-specific file
 *
 * Recommendation source backed by the manga's source website suggestions.
 */
class WebsiteSuggestionPagingSource(
    manga: Manga,
    private val websiteSource: Source,
    networkToLocalManga: NetworkToLocalManga,
) : RecommendationPagingSource(manga, { websiteSource }, networkToLocalManga) {

    override val name: String
        get() = websiteSource.name

    override val category: StringResource
        get() = SYMR.strings.related_mangas_website_suggestions

    override val associatedSourceId: Long
        get() = websiteSource.id

    override suspend fun requestNextPage(source: Source?, currentPage: Int): MangasPage {
        val sManga = manga.toSManga()
        val results = withContext(Dispatchers.IO) {
            val list = mutableListOf<SManga>()
            if (websiteSource.supportsRelatedMangas) {
                try {
                    list.addAll(websiteSource.fetchRelatedMangaList(sManga))
                } catch (e: Exception) {
                    logcat(LogPriority.ERROR, e) { "Failed to fetch related manga from source extension" }
                }
            }

            if (list.isEmpty() && !websiteSource.disableRelatedMangas && !websiteSource.disableRelatedMangasBySearch) {
                try {
                    websiteSource.getRelatedMangaList(
                        manga = sManga,
                        exceptionHandler = { e ->
                            logcat(LogPriority.ERROR, e) { "Failed to search related manga from source" }
                        },
                        pushResults = { pair, _ ->
                            synchronized(list) {
                                list.addAll(pair.second)
                            }
                        },
                    )
                } catch (e: Exception) {
                    logcat(LogPriority.ERROR, e) { "Failed to search related manga from source" }
                }
            }

            list.distinctBy { it.url }
        }

        if (results.isEmpty()) {
            throw NoResultsException()
        }
        return MangasPage(results, false)
    }
}
