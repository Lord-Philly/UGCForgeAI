package com.ugcforge.exporter

import com.ugcforge.core.UgcProject
import com.ugcforge.mesh.MeshData
import java.io.File
import java.util.Locale

data class ExportResult(val file: File, val format: String)

/**
 * Roblox-friendly OBJ exporter. Writes a single mesh (rigid-accessory convention),
 * plus a metadata JSON describing the asset for later upload.
 */
object ObjExporter {

    fun export(project: UgcProject, mesh: MeshData, dir: File): ExportResult {
        dir.mkdirs()
        val obj = File(dir, "${sanitize(project.name)}.obj")
        val json = File(dir, "metadata.json")
        val sb = StringBuilder(1024 * 32)
        sb.append("# UGC Forge AI export\n")
        sb.append("# name: ${project.name}\n")
        sb.append("# type: ${project.assetType.label}\n")
        sb.append("# triangles: ${mesh.triangleCount}\n")
        for (i in 0 until mesh.vertexCount) {
            sb.append(String.format(Locale.US, "v %f %f %f\n", mesh.vertices[i * 3], mesh.vertices[i * 3 + 1], mesh.vertices[i * 3 + 2]))
        }
        for (i in 0 until mesh.vertexCount) {
            sb.append(String.format(Locale.US, "vn %f %f %f\n", mesh.normals[i * 3], mesh.normals[i * 3 + 1], mesh.normals[i * 3 + 2]))
        }
        for (i in 0 until mesh.triangleCount) {
            val a = mesh.triangles[i * 3] + 1
            val b = mesh.triangles[i * 3 + 1] + 1
            val c = mesh.triangles[i * 3 + 2] + 1
            sb.append("f $a//$a $b//$b $c//$c\n")
        }
        obj.writeText(sb.toString())

        val meta = """{
  "name": ${jsonSafe(project.name)},
  "assetType": ${jsonSafe(project.assetType.label)},
  "robloxProfile": ${jsonSafe(project.assetType.robloxProfile)},
  "description": ${jsonSafe(project.description)},
  "style": ${jsonSafe(project.style)},
  "primaryColor": ${jsonSafe(project.primaryColor)},
  "secondaryColor": ${jsonSafe(project.secondaryColor)},
  "geometryStyle": ${jsonSafe(project.geometryStyle)},
  "qualityPreset": ${jsonSafe(project.qualityPreset.label)},
  "engine": "exported",
  "triangles": ${mesh.triangleCount},
  "format": "obj",
  "validatorNote": "Review geometry in Roblox Studio before publishing. UGC Forge AI does not guarantee Marketplace acceptance.",
  "exportedAt": ${System.currentTimeMillis()}
}"""
        json.writeText(meta)
        return ExportResult(obj, "obj")
    }

    fun sanitize(name: String): String =
        name.replace(Regex("[^A-Za-z0-9 _-]"), "").trim().replace(Regex("\\s+"), "_").ifEmpty { "asset" }

    private fun jsonSafe(s: String): String =
        "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\""
}