# UGC Forge AI — Master Plan (recomeço planejado)

> Regra 2 + regra 36. Este documento existe porque os ciclos anteriores
> pularam o planejamento e quebraram: 4 builds corrompidos, canal de escrita
> frágil para arquivos grandes, e um stub honesto `NEEDS_BACKEND` que NÃO
> gera mesh. **A partir daqui: peças pequenas, build verde entre cada uma.**

---

## 1. DIAGNÓSTICO HONESTO (estado REAL do repositório)

### O que JÁ FUNCIONA (build verde, APK instalável no teu celular):

- APK `UGCForgeAI-0.2.0.apk` (9,7 MB) — build SUCCESSFUL, MainActivity
  montando `UgcForgeApp` real (não template cinza), 5 telas pt-BR:
  Home, Criar, Projetos, Detalhe, Configurações. Tema escuro, ícone próprio,
  perm-storage local, pt-BR.
- GitHub `Lord-Philly/UGCForgeAI` (master) — docs, spec Sculpt+, ícone,
  pipeline documentado, tudo commitado e publicado.
- `mesh/`: `ObjParser.kt` (MeshData + Bounds), `SampleMeshes.kt`,
  `ObjExporter.kt` — pipeline de mesh real compilando.
- `ai/`: `AIProvider.kt` honesto — `LocalProvider.generate` reporta
  `NEEDS_BACKEND` com mensagem honesta, NUNCA finge geração (regra 36).

### O que NÃO está feito (a causa do "app não gera nada"):

- O **LocalMeshGenerator procedural** (dome/brim/torso — estética Sculpt+)
  existe como conceito/documento/SampleMeshes, mas **NÃO está acoplado** ao
  fluxo GENERATE. Ou seja: o botão GENERATE hoje responde honestamente
  "requer backend" em vez de produzir a mesh procedural local.

---

## 2. CAUSA RAIZ DOS BUILDS CORROMPIDOS (lição registrada)

- Escrever arquivos grandes de uma vez +080% corrompia o canal de escrita
  4× no mesmo ciclo, produzindo erros estranhos (`Unresolved reference '都将'`,
  `Call has no callee`) que NÃO existiam no código lógico.
- **Decisão arquitetural**: TODA peça nova entra em chunks ≤ 60 linhas, com
  build único entre cada chunk. Nada de arquivo de 2000 linhas de uma vez.

---

## 3. PLANO DE IMPLEMENTAÇÃO (fases pequenas, cada uma = build verde)

### FASE A — Acoplar o gerador procedural ao GENERATE (PRIMEIRA: gera algo REAL)

**A1.** Criar `mesh/LocalMeshGenerator.kt` — objeto que gera `MeshData` real
procedural LOCAL (dome→hair/helmet, torso→shirt, pauldron→shoulder) usando
só `kotlin.math` + `MeshData`/`Mesh` existentes. Sem qualquer dependência nova.
→ build. **A única peça nova de verdade.**

**A2.** Editar `ai/AIProvider.kt` (`LocalProvider.generate`) para chamar
`LocalMeshGenerator.generate(...)` + `ObjExporter.export(...)` e retornar
`Availability.AVAILABLE` com `meshPath`/formato OBJ. Manter mensagem honesta:
"mesh procedural local honesto — Sculpt+ como referência de estilo".
→ build.

**A3.** Wire do `LocalProvider` ao `ProviderRegistry.resolve` (se ainda não
estiver), garantindo que GENERATE usa o provider local por padrão.
→ build + testar GENERATE no device.

**Resultado**: IMAGEM → análise honesta → mesh procedural real no preview 3D
→ export OBJ local. **Pendência zero de "gera nada".**

### FASE B — Preview 3D evolui (depois que A gerar mesh de verdade)

- Controles: rotate/zoom/pan/reset/lighting/wireframe/solid/stats.
- Mostrar: Triangles, Vertices, Materials, Textures, Bounding Box.

### FASE C — Editor + variações

- EDIT: scale/rotation/position/color/material/optimize/undo/redo.
- GENERATE VARIATION: reciclar malha, mudar cor/detalhe — sem regenerar tudo.

### FASE D — Roblox pipeline

- `RobloxValidator` (PASS/WARNING/FAIL + explicação), `RobloxOptimizer`
  (perfis MOBILE/OPTIMIZED/STANDARD/HIGH com parâmetros reais),
  `RobloxExporter`. **Nunca** "Marketplace guaranteed".

### FASE E — Backend honesto (somente depois do pipeline local funcionar)

- `LocalNetworkAIProvider` (PC via Wi-Fi) e `RemoteAIProvider` — opcionais,
  NUNCA API keys no APK, nunca fingir geração remota.

---

## 4. REGRAS DE TRABALHO (a partir de agora)

1. **Um write por ciclo ≤ 60 linhas** + build verde antes do próximo.
2. Construir/testar/verificar após CADA etapa (regra 44).
3. Nenhuma feature falsa (regra 36): se não está pronto → esconde ou diz
   honestamente que não está.
4. Licenças verificadas antes de integrar (docs/THIRD_PARTY_LICENSES.md).
5. Offline-first: nada de imagem saindo do device sem permissão explícita.

---

## 5. DEFINIÇÃO DE PRONTO (v1 funcional)

- Abrir APK → Criar projeto → selecionar imagem → GENERATE → gerar mesh
  procedural local REAL → preview 3D → validar Roblox → exportar OBJ/GLB
  → encontrar o arquivo no Download.
