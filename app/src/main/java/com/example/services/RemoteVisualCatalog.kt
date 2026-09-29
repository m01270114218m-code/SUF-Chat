package com.example.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

data class RemoteVisualAsset(
    val key: String,
    val area: String,
    val url: String?,
    val type: String,
    val animated: Boolean,
    val animationType: String?,
    val durationMs: Int?,
    val loop: Boolean,
    val metadataJson: String,
    val positionX: Float,
    val positionY: Float,
    val width: Float?,
    val height: Float?,
    val zIndex: Int
)

object RemoteVisualCatalogStore {
    private val _assets = MutableStateFlow<List<RemoteVisualAsset>>(emptyList())
    val assets: StateFlow<List<RemoteVisualAsset>> = _assets.asStateFlow()

    fun replaceFromJson(raw: String) {
        val arr = JSONArray(raw)
        val mapped = buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    RemoteVisualAsset(
                        key = o.optString("asset_key"),
                        area = o.optString("area"),
                        url = o.optString("image_url").takeIf { it.isNotBlank() },
                        type = o.optString("asset_type", "image"),
                        animated = o.optBoolean("is_animated", false),
                        animationType = o.optString("animation_type").takeIf { it.isNotBlank() },
                        durationMs = if (o.isNull("duration_ms")) null else o.optInt("duration_ms"),
                        loop = o.optBoolean("loop", true),
                        metadataJson = o.optJSONObject("metadata")?.toString() ?: "{}",
                        positionX = o.optDouble("position_x", 0.0).toFloat(),
                        positionY = o.optDouble("position_y", 0.0).toFloat(),
                        width = if (o.isNull("width")) null else o.optDouble("width").toFloat(),
                        height = if (o.isNull("height")) null else o.optDouble("height").toFloat(),
                        zIndex = o.optInt("z_index", 0)
                    )
                )
            }
        }
        _assets.value = mapped
    }

    fun clear() { _assets.value = emptyList() }

    fun find(key: String, area: String? = null): RemoteVisualAsset? =
        _assets.value.firstOrNull { it.key == key && (area == null || it.area == area) }
            ?: _assets.value.firstOrNull { it.key == key }

    fun frameUrl(frameId: String): String? =
        find(frameId, "frame")?.url ?: find("frame_$frameId", "frame")?.url

    fun entryUrl(entryId: String): String? =
        find(entryId, "entry")?.url ?: find("entry_$entryId", "entry")?.url
}
