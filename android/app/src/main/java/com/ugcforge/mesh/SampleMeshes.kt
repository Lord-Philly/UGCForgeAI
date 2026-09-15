package com.ugcforge.mesh

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural sources so the app always has a demonstrable mesh for the
 * vertical slice (preview + export), until a real generation backend is connected.
 */
object SampleMeshes {

    fun robloxCap(): MeshData {
        val ringSegments = 28
        val domeRings = 7
        val topR = 0.18f
        val baseR = 0.42f
        val domeHeight = 0.32f
        val brimRadius = 0.75f
        val brimThickness = 0.02f

        val v = ArrayList<Float>()
        val n = ArrayList<Float>()
        val tris = ArrayList<Int>()

        fun emit(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float) {
            v.add(x); v.add(y); v.add(z)
            n.add(nx); n.add(ny); n.add(nz)
        }

        val domeVerts = HashMap<Pair<Int, Int>, Int>()
        var index = 0
        fun domeIdx(ring: Int, seg: Int): Int =
            domeVerts.getOrPut(ring to seg) {
                val t = ring.toFloat() / (domeRings - 1)
                val r = topR + (baseR - topR) * t
                val y = domeHeight * (1f - t) * (1f - 0.15f * t)
                val a = seg.toFloat() / ringSegments * 2f * PI.toFloat()
                emit(cos(a) * r, y, sin(a) * r, cos(a), 0.45f + 0.3f * t, sin(a))
                index++
            }

        for (ring in 0 until domeRings - 1) {
            for (seg in 0 until ringSegments) {
                val a = domeIdx(ring, seg)
                val b = domeIdx(ring, (seg + 1) % ringSegments)
                val c = domeIdx(ring + 1, (seg + 1) % ringSegments)
                val d = domeIdx(ring + 1, seg)
                tris.add(a); tris.add(c); tris.add(b)
                tris.add(a); tris.add(d); tris.add(c)
            }
        }

        val crownBase = domeIdx(0, 0) // ensure bottom ring is materialized
        val topRing = domeRings - 1
        emit(0f, domeHeight + 0.01f, 0f, 0f, 1f, 0f)
        val crown = v.size / 3 - 1
        for (seg in 0 until ringSegments) {
            val s = domeIdx(topRing, seg)
            val e = domeIdx(topRing, (seg + 1) % ringSegments)
            tris.add(crown); tris.add(e); tris.add(s)
        }

        // brim annulus
        var ringBase = v.size / 3
        for (seg in 0 until ringSegments) {
            val a = seg.toFloat() / ringSegments * 2f * PI.toFloat()
            emit(cos(a) * baseR, -brimThickness, sin(a) * baseR, 0f, 1f, 0f)
        }
        val innerBase = ringBase
        ringBase = v.size / 3
        for (seg in 0 until ringSegments) {
            val a = seg.toFloat() / ringSegments * 2f * PI.toFloat()
            emit(cos(a) * brimRadius, -brimThickness, sin(a) * brimRadius, 0f, 1f, 0f)
        }
        val outerBase = ringBase

        fun inner(seg: Int) = innerBase + seg % ringSegments
        fun outer(seg: Int) = outerBase + seg % ringSegments

        for (seg in 0 until ringSegments) {
            val a = inner(seg); val b = inner(seg + 1)
            val c = outer(seg + 1); val d = outer(seg)
            tris.add(a); tris.add(b); tris.add(c)
            tris.add(a); tris.add(c); tris.add(d)
            // bottom faces (reversed winding)
            tris.add(a); tris.add(c); tris.add(b)
            tris.add(a); tris.add(d); tris.add(c)
        }

        val vertices = v.toFloatArray()
        val normals = n.toFloatArray()
        val triArr = tris.toIntArray()
        return rebuildWithFlatNormals(MeshData(vertices, normals, triArr, "robloxCap"))
    }

    /** Recompute face normals from geometry (robust for procedural data). */
    private fun rebuildWithFlatNormals(m: MeshData): MeshData {
        val out = FloatArray(m.normals.size)
        val v = m.vertices
        val t = m.triangles
        for (i in 0 until t.size step 3) {
            val a = t[i]; val b = t[i + 1]; val c = t[i + 2]
            val ux = v[b * 3] - v[a * 3]; val uy = v[b * 3 + 1] - v[a * 3 + 1]; val uz = v[b * 3 + 2] - v[a * 3 + 2]
            val wx = v[c * 3] - v[a * 3]; val wy = v[c * 3 + 1] - v[a * 3 + 1]; val wz = v[c * 3 + 2] - v[a * 3 + 2]
            var nx = uy * wz - uz * wy
            var ny = uz * wx - ux * wz
            var nz = ux * wy - uy * wx
            val len = Math.sqrt((nx * nx + ny * ny + nz * nz).toDouble()).toFloat()
            if (len > 0f) { nx /= len; ny /= len; nz /= len }
            for (idx in intArrayOf(a, b, c)) {
                out[idx * 3] += nx; out[idx * 3 + 1] += ny; out[idx * 3 + 2] += nz
            }
        }
        for (i in 0 until out.size step 3) {
            val x = out[i]; val y = out[i + 1]; val z = out[i + 2]
            val len = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            if (len > 0f) { out[i] = x / len; out[i + 1] = y / len; out[i + 2] = z / len }
        }
        return MeshData(m.vertices, out, m.triangles, m.name)
    }
}