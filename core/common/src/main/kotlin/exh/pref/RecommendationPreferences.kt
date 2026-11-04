package exh.pref

import dev.zacsweers.metro.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

/**
 * SY-specific file
 */
@Serializable
data class RecommendationProviderConfig(
    val id: String,
    val enabled: Boolean = true,
    val autoExpand: Boolean = false,
)

@Inject
class RecommendationPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object {
        const val PROVIDER_WEBSITE = "website"
        const val PROVIDER_ANILIST = "anilist"
        const val PROVIDER_MYANIMELIST = "myanimelist"
        const val PROVIDER_MANGAUPDATES_SIMILAR = "mangaupdates_similar"
        const val PROVIDER_MANGAUPDATES_COMMUNITY = "mangaupdates_community"

        val defaultProviders = listOf(
            RecommendationProviderConfig(PROVIDER_WEBSITE, enabled = true, autoExpand = false),
            RecommendationProviderConfig(PROVIDER_ANILIST, enabled = true, autoExpand = false),
            RecommendationProviderConfig(PROVIDER_MYANIMELIST, enabled = true, autoExpand = false),
            RecommendationProviderConfig(PROVIDER_MANGAUPDATES_SIMILAR, enabled = true, autoExpand = false),
            RecommendationProviderConfig(PROVIDER_MANGAUPDATES_COMMUNITY, enabled = true, autoExpand = false),
        )
    }

    val providers: Preference<List<RecommendationProviderConfig>> = preferenceStore.getObjectFromString(
        key = "recommendation_providers_config",
        defaultValue = defaultProviders,
        serializer = { Json.encodeToString(it) },
        deserializer = {
            try {
                val list = Json.decodeFromString<List<RecommendationProviderConfig>>(it)
                val existingIds = list.map { c -> c.id }.toSet()
                val missing = defaultProviders.filter { d -> d.id !in existingIds }
                list + missing
            } catch (e: Exception) {
                defaultProviders
            }
        },
    )
}
