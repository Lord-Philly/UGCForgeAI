# UGC Forge AI

App Android (Jetpack Compose, Material 3, Kotlin) para criação de **assets UGC
para Roblox** offline-first: crie uma ficha de asset (mesh/decoração), gere um
modelo 3D (mesh) com preview GL e exporte para o Roblox Studio.

> Projeto educacional autoral do usuário **Lord-Philly**, documentado e
> publicado a partir da lição de "_build before publish_" do repositório
> agent-learning.

## Estrutura

```
android/app/src/main/java/com/ugcforge/
├── ai/          generadores (estágios: analyze → mesh → texture → combine)
├── core/        modelos: AssetType, UgcProject, QualityPreset, ProjectStatus
├── data/        ProjectRepository + SettingsRepository (JSON em disco)
├── mesh/        geração procedural de malha + OBJ parser/exporter
├── preview/     visualizador GL (MalhaPreviewView + renderer/estilos)
├── ui/          Compose: tema, VM, navegação e 5 telas
└── UGCForgeAI.kt  entry point (Application)
```

## Telas

| Rota | Arquivo | Função |
|------|---------|--------|
| `/home` | HomeScreen.kt | visão geral do usuário + atalhos |
| `/create` | CreateScreen.kt | nova ficha de asset (tipo, cor, estilo, qualidade, direitos) |
| `/projects` | ProjectsScreen.kt | lista de projetos salvos, filtros por status |
| `/settings` | SettingsScreen.kt | URL do backend, privacidade, engine local |
| `/project/{id}` | ProjectDetailScreen.kt | detalhes + gerar mesh + exportar/renomear/duplicar/remover |

## Compilar

Requer Android SDK (AndroidX, minSdk ~24) e Java 21. Gradle 8.13.

```sh
cd android
gradle :app:assembleDebug   # APK em app/build/outputs/apk/debug/app-debug.apk
```

## Estado

**Compila e gera APK de debug** (`BUILD SUCCESSFUL`, app-debug.apk ≈ 10 MB).
As 5 telas estão no ar; a geração de mesh usa um motor local procedural e pode
ser apontada para um backend remoto via Configurações.
