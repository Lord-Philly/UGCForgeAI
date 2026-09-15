package com.ugcforge.mesh

data class MeshData(
    val vertices: FloatArray,
    val normals: FloatArray,
    val triangles: IntArray,
    val name: String = "mesh",
    val bounds: Bounds = Bounds.of(vertices),
) {
    val vertexCount: Int get() = vertices.size / 3
    val triangleCount: Int get() = triangles.size / 3
}

data class Bounds(
    val minX: Float, val minY: Float, val minZ: Float,
    val maxX: Float, val maxY: Float, val maxZ: Float,
) {
    val center: FloatArray get() = floatArrayOf((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f)
    val diameter: Float get() = maxOf(maxX - minX, maxY - minY, maxZ - minZ)

    companion object {
        fun of(vertices: FloatArray): Bounds {
            var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
            for (i in 0 until vertices.size step 3) {
                val x = vertices[i]; val y = vertices[i + 1]; val z = vertices[i + 2]
                if (x < minX) minX = x; if (x > maxX) maxX = x
                if (y < minY) minY = y; if (y > maxY) maxY = y
                if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
            }
            return Bounds(minX, minY, minZ, maxX, maxY, maxZ)
        }
    }
}

object MeshMath {
    /** Center + unit-scale so geometry is centered near origin, up = +Y. */
    fun normalize(mesh: MeshData): MeshData {
        val c = mesh.bounds.center
        val s = (mesh.bounds.diameter / 2f).takeIf { it > 0.0001f } ?: 1f
        val v = mesh.vertices.copyOf()
        for (i in 0 until v.size step 3) {
            v[i] = (v[i] - c[0]) / s
            v[i + 1] = (v[i + 1] - c[1]) / s
            v[i + 2] = (v[i + 2] - c[2]) / s
        }
        return MeshData(v, mesh.normals, mesh.triangles, mesh.name)
    }
}

/**
 * Minimal OBJ parser: v, vn, f (polygons fan-triangulated). Output is
 * non-indexed ("triangle soup") so triangles are simply 0..count*3 in order.
 */
class ObjParser {
    fun parse(text: String, name: String = "parsed"): MeshData? {
        val pos = ArrayList<Float>(8192)
        val vn = ArrayList<Float>(8192)
        val tris = ArrayList<IntArray>() // each: intArrayOf(v0,n0, v1,n1, v2,n2)

        for (line in text.lineSequence()) {
            val s = line.trim()
            when {
                s.startsWith("v ") -> {
                    val p = s.split(Regex("\\s+")).drop(1).mapNotNull { it.toFloatOrNull() }
                    if (p.size >= 3) { pos.add(p[0]); pos.add(p[1]); pos.add(p[2]) }
                }
                s.startsWith("vn ") -> {
                    val p = s.split(Regex("\\s+")).drop(1).mapNotNull { it.toFloatOrNull() }
                    if (p.size >= 3) { vn.add(p[0]); vn.add(p[1]); vn.add(p[2]) }
                }
                s.startsWith("f ") -> {
                    val refs = s.split(Regex("\\s+")).drop(1).filter { it.isNotBlank() }
                    if (refs.size < 3) continue
                    val vIdx = IntArray(refs.size)
                    val nIdx = IntArray(refs.size)
                    for ((k, spec) in refs.withIndex()) {
                        val parts = spec.split("/")
                        vIdx[k] = parts[0].toIntOrNull() ?: 0
                        nIdx[k] = if (parts.size >= 3) (parts[2].toIntOrNull() ?: 0) else 0
                    }
                    for (k in 1 until vIdx.size - 1) {
                        tris.add(intArrayOf(
                            vIdx[0], nIdx[0],
                            vIdx[k], nIdx[k],
                            vIdx[k + 1], nIdx[k + 1],
                        ))
                    }
                }
            }
        }
        if (pos.isEmpty() || tris.isEmpty()) return null

        fun resolve(raw: Int, size: Int): Int {
            val i = raw - 1
            return if (i >= 0) i % size else size + i
        }

        val outV = FloatArray(tris.size * 9)
        val outN = FloatArray(tris.size * 9)
        val outT = IntArray(tris.size * 3)
        var vi = 0; var ni = 0; var ti = 0
        for (tr in tris) {
            for (k in 0 until 6 step 2) {
                val p = resolve(tr[k], pos.size / 3)
                outV[vi] = pos[p * 3]; outV[vi + 1] = pos[p * 3 + 1]; outV[vi + 2] = pos[p * 3 + 2]; vi += 3
                if (vn.isNotEmpty() && tr[k + 1] != 0) {
                    val nn = resolve(tr[k + 1], vn.size / 3)
                    outN[ni] = vn[nn * 3]; outN[ni + 1] = vn[nn * 3 + 1]; outN[ni + 2] = vn[nn * 3 + 2]
                }
                ni += 3
                outT[ti] = ti; ti++
            }
        }
        if (vn.isEmpty()) computeFlatNormals(outV, outT, outN)
        return MeshData(outV, outN, outT, name)
    }

    private fun computeFlatNormals(v: FloatArray, t: IntArray, out: FloatArray) {
        for (i in 0 until out.size) out[i] = 0f
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
    }
}