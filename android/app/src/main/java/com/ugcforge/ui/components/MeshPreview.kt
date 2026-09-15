package com.ugcforge.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ugcforge.mesh.MeshData
import com.ugcforge.preview.MeshPreviewView

@Composable
fun MeshPreview(mesh: MeshData, modifier: Modifier = Modifier, wireframe: Boolean = false) {
    val renderMode = remember(wireframe) {
        if (wireframe) MeshPreviewView.RENDER_WIRE else MeshPreviewView.RENDER_SOLID
    }
    // Reuse a single sane key so the GL surface stays alive across recompositions.
    val key = if (wireframe) "w" else "s"
    AndroidView(
        modifier = modifier,
        factory = { ctx -> MeshPreviewView(ctx, mesh, renderMode) },
        update = { view -> view.setRenderMode(renderMode) },
    )
}
