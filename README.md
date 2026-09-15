# UGC Forge AI — criador de assets UGC (Roblox)

App Android (Compose, Material 3, offline-first) que transforma **imagem ou
descrição → mesh 3D (OBJ) → validação → exportação**, com motor de IA plugável
(local ou backend remoto).

Estrutura (`android/app/src/main/java/com/ugcforge`):

- `ai/` — provedores de geração (local/remoto), phases & geração honesta (não
  finge mesh: sem backend conectado → `NEEDS_BACKEND`).
- `core/` — `UgcProject`, `AssetType`, `QualityPreset`, `ProjectStatus`.
- `data/` — `ProjectRepository` (JSON em disco), `SettingsRepository`
  (backend URL, privacy, engine local).
- `mesh/` — `ObjParser` + `MeshData` (geração procedural, fallback verificado).
- `exporter/` — `ObjExporter` (OBJ + metadados prontos para o Studio).
- `preview/` — `MeshPreviewView` (OpenGL, rotate/pinch/taps de render).
- `ui/` — `UgcForgeApp` (navegação), `UgcForgeViewModel`, telas
  (Home, Create, Projects, ProjectDetail, Settings) e componentes.

## Build

```bash
cd android
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Requisitos: Android SDK (aapt2), JDK 17+, Gradle (empacotado).

## Foco

- **Sem fake generation**: se o backend de IA não está configurado, o app
  reporta `NEEDS_BACKEND` com honestidade.
- Offline-first (referências processadas no aparelho; opcional remoto).
- Exporta `.obj` + `metadata.json` prontos para revisão no Roblox Studio.
