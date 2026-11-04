package exh.recs.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.browse.components.InLibraryBadge
import eu.kanade.presentation.library.components.CommonMangaItemDefaults
import eu.kanade.presentation.library.components.MangaComfortableGridItem
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowForward
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.asMangaCover
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.i18n.stringResource

sealed interface RecommendationPreviewState {
    data object Loading : RecommendationPreviewState
    data class Success(val title: String, val mangas: List<Manga>) : RecommendationPreviewState
    data object Empty : RecommendationPreviewState
}

@Composable
fun RecommendationPreviewRow(
    state: RecommendationPreviewState,
    onHeaderClicked: () -> Unit,
    onMangaClicked: (Manga) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().animateContentSize()) {
        when (state) {
            RecommendationPreviewState.Empty, RecommendationPreviewState.Loading -> Unit
            is RecommendationPreviewState.Success -> {
                if (state.mangas.isEmpty()) return@Box
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onHeaderClicked)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(SYMR.strings.pref_source_related_mangas),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (state.title.isNotBlank() && state.title != stringResource(SYMR.strings.pref_source_related_mangas)) {
                                Text(
                                    text = state.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Icon(
                            imageVector = MaterialSymbols.AutoMirroredRounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.mangas, key = { it.id }) { manga ->
                            Box(modifier = Modifier.width(96.dp)) {
                                MangaComfortableGridItem(
                                    title = manga.title,
                                    titleMaxLines = 3,
                                    coverData = manga.asMangaCover(),
                                    coverBadgeStart = {
                                        InLibraryBadge(enabled = manga.favorite)
                                    },
                                    coverAlpha = if (manga.favorite) CommonMangaItemDefaults.BrowseFavoriteCoverAlpha else 1f,
                                    onClick = { onMangaClicked(manga) },
                                    onLongClick = { onMangaClicked(manga) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
