package tachiyomi.domain.manga.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository

@Inject
class NetworkToLocalManga(
    private val mangaRepository: MangaRepository,
) {

    /**
     * @param updateInfo Default: `true`. If `true`, it will update the manga's information in the database if it already exists.
     * If `false`, it will only insert the manga if it doesn't exist in the database. This is used to avoid the case when fetching
     * related mangas from within `MangaViewModel` would overwrite current manga's `getDetails` info with info from browsing.
     */
    suspend operator fun invoke(
        manga: Manga,
        // SY -->
        // Ported from Komikku (domain .../manga/interactor/NetworkToLocalManga.kt, updateInfo param)
        updateInfo: Boolean = true,
        // SY <--
    ): Manga {
        return invoke(
            listOf(manga),
            // SY -->
            updateInfo,
            // SY <--
        ).single()
    }

    /**
     * @param updateInfo Default: `true`. If `true`, it will update the manga's information in the database if it already exists.
     * If `false`, it will only insert the manga if it doesn't exist in the database. This is used to avoid the case when fetching
     * related mangas from within `MangaViewModel` would overwrite current manga's `getDetails` info with info from browsing.
     */
    suspend operator fun invoke(
        manga: List<Manga>,
        // SY -->
        // Ported from Komikku (domain .../manga/interactor/NetworkToLocalManga.kt, updateInfo param)
        updateInfo: Boolean = true,
        // SY <--
    ): List<Manga> {
        return mangaRepository.insertNetworkManga(
            manga,
            // SY -->
            updateInfo,
            // SY <--
        )
    }
}
