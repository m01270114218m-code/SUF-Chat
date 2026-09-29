package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.services.RemoteVisualCatalogStore

@Composable
fun ServerDrivenAssetLayer(
    area: String,
    modifier: Modifier = Modifier
) {
    val assets by RemoteVisualCatalogStore.assets.collectAsState()
    Box(modifier = modifier.fillMaxSize()) {
        assets.asSequence()
            .filter { it.area == area || it.area == "global" }
            .filter { !it.url.isNullOrBlank() }
            .sortedBy { it.zIndex }
            .forEach { asset ->
                AsyncImage(
                    model = Uri.parse(asset.url),
                    contentDescription = asset.key,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(asset.positionX.dp, asset.positionY.dp)
                        .then(if (asset.width != null && asset.height != null) Modifier.size(asset.width.dp, asset.height.dp) else Modifier.size(96.dp))
                )
            }
    }
}
