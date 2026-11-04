package exh.recs.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.Screen
import exh.pref.RecommendationPreferences
import exh.pref.RecommendationProviderConfig
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.DragHandle
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import tachiyomi.presentation.core.util.plus

/**
 * SY-specific file
 */
class RecommendationSettingsScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val recommendationPreferences = remember { context.appGraph.recommendationPreferences }
        val uiPreferences = remember { context.appGraph.uiPreferences }
        val showHomeOnRecommendationsPref = uiPreferences.showHomeOnRecommendations
        val showHomeOnRecommendations by showHomeOnRecommendationsPref.collectAsState()
        val providerConfigsPref = recommendationPreferences.providers
        val providerConfigs by providerConfigsPref.collectAsState()

        val itemsState = remember { mutableStateListOf<RecommendationProviderConfig>() }

        LaunchedEffect(providerConfigs) {
            if (itemsState != providerConfigs) {
                itemsState.clear()
                itemsState.addAll(providerConfigs)
            }
        }

        val lazyListState = rememberLazyListState()
        val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
            val fromIndex = from.index
            val toIndex = to.index
            if (fromIndex in itemsState.indices && toIndex in itemsState.indices) {
                val item = itemsState.removeAt(fromIndex)
                itemsState.add(toIndex, item)
                providerConfigsPref.set(itemsState.toList())
            }
        }

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(SYMR.strings.pref_recommendation_settings),
                    scrollBehavior = scrollBehavior,
                    navigateUp = navigator::pop,
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = paddingValues +
                    topSmallPaddingValues +
                    PaddingValues(horizontal = MaterialTheme.padding.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            ) {
                items(
                    items = itemsState,
                    key = { it.id },
                ) { provider ->
                    ReorderableItem(reorderableState, provider.id) {
                        RecommendationProviderItem(
                            provider = provider,
                            onToggleEnabled = { enabled ->
                                val index = itemsState.indexOfFirst { it.id == provider.id }
                                if (index != -1) {
                                    itemsState[index] = itemsState[index].copy(enabled = enabled)
                                    providerConfigsPref.set(itemsState.toList())
                                }
                            },
                            onToggleAutoExpand = { autoExpand ->
                                val index = itemsState.indexOfFirst { it.id == provider.id }
                                if (index != -1) {
                                    itemsState[index] = itemsState[index].copy(autoExpand = autoExpand)
                                    providerConfigsPref.set(itemsState.toList())
                                }
                            },
                        )
                    }
                }

                item(key = "home_button_preference") {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MaterialTheme.padding.small),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHomeOnRecommendationsPref.set(!showHomeOnRecommendations) }
                                .padding(
                                    horizontal = MaterialTheme.padding.medium,
                                    vertical = MaterialTheme.padding.small,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = stringResource(SYMR.strings.pref_show_home_on_related_mangas),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = stringResource(SYMR.strings.pref_show_home_on_related_mangas_summary),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = showHomeOnRecommendations,
                                onCheckedChange = { showHomeOnRecommendationsPref.set(it) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.RecommendationProviderItem(
    provider: RecommendationProviderConfig,
    onToggleEnabled: (Boolean) -> Unit,
    onToggleAutoExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (titleRes, descRes) = remember(provider.id) {
        when (provider.id) {
            RecommendationPreferences.PROVIDER_WEBSITE ->
                SYMR.strings.rec_provider_website to SYMR.strings.rec_provider_website_desc
            RecommendationPreferences.PROVIDER_ANILIST ->
                SYMR.strings.rec_provider_anilist to SYMR.strings.rec_provider_anilist_desc
            RecommendationPreferences.PROVIDER_MYANIMELIST ->
                SYMR.strings.rec_provider_myanimelist to SYMR.strings.rec_provider_myanimelist_desc
            RecommendationPreferences.PROVIDER_MANGAUPDATES_SIMILAR ->
                SYMR.strings.rec_provider_mangaupdates_similar to SYMR.strings.rec_provider_mangaupdates_similar_desc
            RecommendationPreferences.PROVIDER_MANGAUPDATES_COMMUNITY ->
                SYMR.strings.rec_provider_mangaupdates_community to SYMR.strings.rec_provider_mangaupdates_community_desc
            else ->
                SYMR.strings.pref_source_related_mangas to SYMR.strings.pref_source_related_mangas_summary
        }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.padding.small),
        ) {
            // Main Row: Drag Handle + Title/Description + Main ON/OFF Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleEnabled(!provider.enabled) }
                    .padding(
                        start = MaterialTheme.padding.small,
                        end = MaterialTheme.padding.medium,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.DragHandle,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(MaterialTheme.padding.small)
                        .draggableHandle(),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = MaterialTheme.padding.small),
                ) {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(descRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Switch(
                    checked = provider.enabled,
                    onCheckedChange = onToggleEnabled,
                )
            }

            // Sub-row: Auto-expand into grid (visible only when provider is enabled)
            AnimatedVisibility(
                visible = provider.enabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleAutoExpand(!provider.autoExpand) }
                        .padding(start = 48.dp, end = MaterialTheme.padding.medium, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(SYMR.strings.rec_auto_expand_grid),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Switch(
                        checked = provider.autoExpand,
                        onCheckedChange = onToggleAutoExpand,
                    )
                }
            }
        }
    }
}
