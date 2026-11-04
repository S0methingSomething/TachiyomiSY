package exh.recs.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.browse.components.GlobalSearchCardRow
import eu.kanade.presentation.browse.components.GlobalSearchErrorResultItem
import eu.kanade.presentation.browse.components.GlobalSearchLoadingResultItem
import eu.kanade.presentation.browse.components.InLibraryBadge
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.library.components.MangaComfortableGridItem
import eu.kanade.presentation.util.formattedMessage
import exh.recs.RecommendationItemResult
import exh.recs.RecommendsViewModel
import exh.recs.sources.RecommendationPagingSource
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowForward
import mihon.icons.materialsymbols.rounded.KeyboardArrowDown
import mihon.icons.materialsymbols.rounded.KeyboardArrowUp
import nl.adaptivity.xmlutil.core.impl.multiplatform.name
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.asMangaCover
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

/**
 * SY-specific file
 */
@Composable
fun RecommendsScreen(
    title: String,
    state: RecommendsViewModel.State,
    navigateUp: () -> Unit,
    getManga: @Composable (Manga) -> State<Manga>,
    onClickSource: (RecommendationPagingSource) -> Unit,
    onClickItem: (Manga) -> Unit,
    onLongClickItem: (Manga) -> Unit,
    goHome: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigateUp = navigateUp,
                goHome = goHome,
            )
        },
    ) { paddingValues ->
        RecommendsContent(
            items = state.filteredItems,
            contentPadding = paddingValues,
            getManga = getManga,
            onClickSource = onClickSource,
            onClickItem = onClickItem,
            onLongClickItem = onLongClickItem,
        )
    }
}

@Composable
internal fun RecommendsContent(
    items: Map<RecommendationPagingSource, RecommendationItemResult>,
    contentPadding: PaddingValues,
    getManga: @Composable (Manga) -> State<Manga>,
    onClickSource: (RecommendationPagingSource) -> Unit,
    onClickItem: (Manga) -> Unit,
    onLongClickItem: (Manga) -> Unit,
) {
    // Keep expanded states preserved across scrolling and configuration changes
    val expandedStates = rememberSaveable(
        saver = listSaver(
            save = { it.entries.map { e -> "${e.key}=${e.value}" } },
            restore = { list ->
                mutableStateMapOf<String, Boolean>().apply {
                    list.forEach { item ->
                        val parts = item.split("=", limit = 2)
                        if (parts.size == 2) {
                            this[parts[0]] = parts[1].toBoolean()
                        }
                    }
                }
            },
        ),
    ) {
        mutableStateMapOf<String, Boolean>()
    }

    LazyColumn(
        contentPadding = contentPadding,
    ) {
        items.forEach { (source, recResult) ->
            val sourceKey = "${source::class.name}-${source.name}-${source.category.resourceId}"
            item(key = sourceKey) {
                val isExpanded = expandedStates[sourceKey] ?: source.autoExpand
                RecommendationSection(
                    source = source,
                    recResult = recResult,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedStates[sourceKey] = !isExpanded
                    },
                    getManga = getManga,
                    onClickSource = { onClickSource(source) },
                    onClickItem = onClickItem,
                    onLongClickItem = onLongClickItem,
                )
            }
        }
    }
}

@Composable
private fun RecommendationSection(
    source: RecommendationPagingSource,
    recResult: RecommendationItemResult,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    getManga: @Composable (Manga) -> State<Manga>,
    onClickSource: () -> Unit,
    onClickItem: (Manga) -> Unit,
    onLongClickItem: (Manga) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.padding.medium,
                    end = MaterialTheme.padding.extraSmall,
                )
                .clickable(onClick = onClickSource),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(source.category),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Toggle between horizontal row and expanded vertical grid
                if (recResult is RecommendationItemResult.Success && recResult.result.isNotEmpty()) {
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) {
                                MaterialSymbols.Rounded.KeyboardArrowUp
                            } else {
                                MaterialSymbols.Rounded.KeyboardArrowDown
                            },
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                IconButton(onClick = onClickSource) {
                    Icon(
                        imageVector = MaterialSymbols.AutoMirroredRounded.ArrowForward,
                        contentDescription = null,
                    )
                }
            }
        }

        // Section Content
        when (recResult) {
            RecommendationItemResult.Loading -> {
                GlobalSearchLoadingResultItem()
            }
            is RecommendationItemResult.Success -> {
                if (recResult.result.isEmpty()) {
                    // Empty, no items
                } else if (isExpanded) {
                    // 3-Column Expanded Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.padding.small,
                                vertical = MaterialTheme.padding.extraSmall,
                            ),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                    ) {
                        recResult.result.chunked(3).forEach { rowTitles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                            ) {
                                for (manga in rowTitles) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        val title by getManga(manga)
                                        MangaComfortableGridItem(
                                            title = title.title,
                                            titleMaxLines = 3,
                                            coverData = title.asMangaCover(),
                                            coverBadgeStart = {
                                                InLibraryBadge(enabled = title.favorite)
                                            },
                                            onClick = { onClickItem(title) },
                                            onLongClick = { onLongClickItem(title) },
                                        )
                                    }
                                }
                                for (i in rowTitles.size until 3) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    // Horizontal Scrolling Row
                    GlobalSearchCardRow(
                        titles = recResult.result,
                        getManga = getManga,
                        onClick = onClickItem,
                        onLongClick = onLongClickItem,
                    )
                }
            }
            is RecommendationItemResult.Error -> {
                GlobalSearchErrorResultItem(
                    message = with(LocalContext.current) {
                        recResult.throwable.formattedMessage
                    },
                )
            }
        }
    }
}
