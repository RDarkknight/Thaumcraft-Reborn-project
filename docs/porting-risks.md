# Riesgos del port: Thaumcraft 6 (MC 1.12.2 / Forge 14.23) → Minecraft 26.3

> Basado en la auditoría del JAR `6.1.BETA26`. Los hechos sobre el código original están verificados en el descompilado. Las afirmaciones sobre Minecraft 26.3 se basan en la evolución conocida de la plataforma (1.13 → 1.21.x → versionado anual 26.x) y **deben verificarse contra el mod loader y los mappings elegidos** cuando se fije el toolchain (NeoForge / Fabric / otro para 26.3).

## Conclusión general

El salto abarca ~9 años de cambios (flattening 1.13, nuevo render 1.15/1.17, data components 1.20.5, nuevo pipeline de render/ítems 1.21.x, Java 8 → Java 21+/25). **No es viable un port mecánico**: prácticamente cada clase del mod toca una API que cambió o desapareció. El JAR debe usarse como **especificación de comportamiento**, reconstruyendo cada sistema sobre las APIs modernas. Lo que sí es mayormente reutilizable son los **datos y la lógica de diseño** (aspectos, research, recetas, fórmulas de aura) y, sujeto a licencia, los assets.

## 1. Partes ofuscadas o difíciles de recuperar

| Problema | Evidencia | Impacto |
|---|---|---|
| Nombres SRG de Minecraft en el bytecode | ~42 250 ocurrencias `field_*`/`func_*` en 753 de 903 archivos | Hay que remapear (MCP 1.12 → nombres legibles) para leer el código; los nombres no corresponden a 26.x |
| Parámetros/locales sin nombre | ~4 850 `var*`/`par*` y ~800 `p_*` | Lectura más lenta; semántica a inferir |
| Métodos no descompilados por Vineflower (10) | `EntityCultistPortalGreater.func_70071_h_` (onUpdate); `func_185496_a` (getBoundingBox) en `BlockTaintFeature`, `BlockSmelterVent`, `BlockEssentiaTransport`, `BlockArcaneEar`, `BlockMirror`; `TileAlembicRenderer.renderTileEntityAt`, `TileThaumatoriumRenderer.renderTileEntityAt`, `TileGolemBuilderRenderer.renderTileEntityAt`, `TileBellowsRenderer.render` | Requiere leer bytecode (`javap -c`) o probar otro descompilador (CFR, Procyon); los renderers igualmente deberán reescribirse |
| Código con constantes "mágicas" | `WorldGenMound` (2448 `setBlockState` hardcodeados), blueprints de multibloques `Part[][][]` en `ConfigRecipes` | Conviene convertir a estructuras NBT/JSON (templates) en vez de traducir línea a línea |
| Access Transformer (24 entradas) + reflexión (`ObfuscationReflectionHelper`) | `META-INF/tc_at.cfg`, `ThaumcraftCraftingManager`, `AuraThread`, `Utils`, `BlockUtils` | Cada acceso a internals debe re-diseñarse o sustituirse por AT/mixin del loader moderno |
| Librerías embebidas sin fuente | CodeChickenLib re-empaquetado (42 clases), GLE (`com.sasmaster`) | No portar: sustituir por math de JOML (incluido en MC moderno) y geometría propia |
| Comportamiento dependiente de bugs/orden de 1.12 | Hilo de aura con `DimensionManager.getWorld`, mutación concurrente de mapas estáticos | Difícil de reproducir 1:1; requiere rediseño |

No se detectó ofuscación propia del autor (ni ProGuard ni nombres basura en clases `thaumcraft.*`); la dificultad proviene de los mappings de Minecraft y de la descompilación.

## 2. Sistemas que requieren reconstrucción importante

Escala: **Crítico** = reescritura completa del sistema; **Alto** = reescritura de la capa de integración con la lógica conservable; **Medio** = adaptación sustancial pero acotada.

### 2.1 Rendering (cliente) — **Crítico**
- 143 clases con `GL11`/`GlStateManager`, 55 con `Tessellator`/`BufferBuilder` en modo inmediato, 22 TESR, 30 renderers de entidad con `ModelBase`, 24 partículas, motor de partículas propio, shaders GLSL vía `ARBShaderObjects`, GLE para tubos, loader OBJ propio, LWJGL 2 (`Keyboard`/`Mouse`).
- Moderno: `BlockEntityRenderer`, `EntityRenderer` + render state, `PoseStack`, `VertexConsumer`/`MultiBufferSource`, `RenderType`, `ModelLayer`/`LayerDefinition`, core shaders, LWJGL 3 (y en 1.21.x el nuevo backend `RenderPipeline`/GpuDevice). El render inmediato y el GL directo **no existen**.
- Afecta: infusion matrix, crucible, jars (líquido), tubos/streams de essentia, flux rift, focus FX, golems (OBJ modular), armaduras custom, HUD de vis/goggles, shaders de pociones (blur/desaturate/sunscorned/hunger).

### 2.2 Bloques con metadata y blockstates — **Crítico**
- 47 clases usan `getMetaFromState`; ores/cristales/plantas/piedras como variantes por meta; 170 blockstates Forge v1; `textures/blocks`.
- Moderno: flattening (cada variante = bloque propio o `Property` explícita), `BlockBehaviour.Properties`, `VoxelShape` en lugar de `AxisAlignedBB`, blockstates vanilla, `MapCodec` para bloques (1.20.5+), loot tables de bloques como datos.
- Implica rediseñar IDs y escribir una tabla de equivalencias 1.12 → 26.3.

### 2.3 Ítems, NBT de stacks y encantamientos de infusión — **Crítico**
- Ítems con subtipos por metadata (`ItemTCBase` variantes), datos en NBT de stack: aspectos de `crystalEssence`/phials/jars, foci (`FocusPackage` serializado), golem placer (propiedades), seals, research notes, encantamientos de infusión (`EnumInfusionEnchantment` en NBT), vis cargada en baubles.
- Moderno: **Data Components** (1.20.5+) reemplazan el NBT de stacks; definiciones de ítem en `assets/*/items/` (1.21.4+); `Item.Properties` con componentes (tools/armor como componentes en 1.21.x).
- Hay que diseñar componentes propios (`aspects`, `focus`, `golem_properties`, `infusion_enchantments`, `vis_charge`...) con codecs.

### 2.4 Aura / Vis / Flux (simulación) — **Crítico**
- `AuraThread`: un hilo Java por dimensión que muta mapas estáticos (`ConcurrentHashMap<Integer, AuraWorld>`) cada ~1 s, usando IDs de dimensión enteros y `DimensionManager`.
- Moderno: dimensiones por `ResourceKey<Level>`, sin `DimensionManager`; datos de chunk mediante attachments/capabilities del loader o `SavedData`; acceso al mundo desde otro hilo es inseguro.
- Recomendación: reconstruir como simulación **en el tick del servidor** (presupuestada por tick) con almacenamiento por chunk; conservar las fórmulas (difusión <75 %, tabla de fase lunar `{0.25,0.15,0.1,0.05,0,0.05,0.1,0.15}`, rift si `flux > base·0.75`, clamp 32766, `AURA_CEILING 500`).

### 2.5 World generation — **Crítico**
- `IWorldGenerator` + `WorldGenerator` imperativo + retrogeneración por evento + biomas registrados por código con `BiomeManager`/`BiomeDictionary`; `WorldGenMound` hardcodeado.
- Moderno: worldgen **data-driven** (`ConfiguredFeature`/`PlacedFeature`, biome modifiers / API de biomas del loader, `Structure` + templates, biomas por JSON + tags, noise/multi-noise para inyectar biomas). `IWorldGenerator` y la retrogeneración no existen.
- El cambio de biomas en runtime por taint (`PacketBiomeChange`) choca con biomas por sección 4×4×4 y paletas: requiere rediseño.
- Inicialización del aura por chunk debe engancharse a otro punto (evento de carga/generación de chunk).

### 2.6 Networking — **Alto**
- 41 `IMessage` + `SimpleNetworkWrapper`, serialización manual con `ByteBuf`, `FakeNetHandlerPlayServer`.
- Moderno: payloads tipados (`CustomPacketPayload` + `StreamCodec`), registro por fase/dirección. Los paquetes de FX pueden reducirse usando partículas con opciones sincronizadas.
- Riesgo de seguridad: paquetes `*ToServer` (FocusNodes, SyncProgress, PlayerFlag, MiscString) aceptan datos del cliente; validar en servidor.

### 2.7 GUIs y menús — **Alto**
- `IGuiHandler` con 22 IDs enteros, `GuiContainer`/`Container`, render GL de GUIs (research browser con zoom/pan, focal manipulator, theorycraft, logistics).
- Moderno: `MenuType` + `AbstractContainerMenu` + `AbstractContainerScreen`, `GuiGraphics`, apertura con buffer extra; widgets nuevos.
- `GuiResearchBrowser`/`GuiResearchPage` (~3 800 líneas) son en sí un proyecto: reescritura completa.

### 2.8 TileEntities → BlockEntities — **Alto**
- 48 tiles, 27 `ITickable`, inventarios `IInventory`/`ISidedInventory`/`IItemHandler`, `IFluidHandler`, `getUpdatePacket` + `PacketTileToClient`.
- Moderno: `BlockEntityType` con builder, tickers vía `EntityBlock#getTicker`, `saveAdditional/loadAdditional` con `HolderLookup.Provider` (y `ValueInput/ValueOutput` en 1.21.6+), capabilities/transfer API del loader para ítems y fluidos.
- El **modelo de succión de essentia** (`IEssentiaTransport` por `EnumFacing`) debe re-implementarse como capability/lookup propio por `Direction`.

### 2.9 Recetas y crafting — **Alto**
- Recetas registradas por código (`IRecipe` + `ShapedOreRecipe`), Ore Dictionary (58 registros), catálogos propios (73 arcanas, 56 infusión, 42 crucible, 6 multibloque), derivación recursiva de aspectos a partir de recetas.
- Moderno: `RecipeType`/`RecipeSerializer` + JSON en `data/`, tags en lugar de ore dictionary, `Ingredient`/`ItemStackTemplate`, `RecipeManager` sólo servidor (desde 1.21.2 el cliente no tiene todas las recetas: afecta al Thaumonomicon y al cálculo de aspectos en cliente).
- Dust triggers / multibloques: rediseñar como datos.

### 2.10 Research / Thaumonomicon — **Medio-Alto**
- Lógica en gran parte independiente de MC (JSON propio, `ResearchManager`), reutilizable como **diseño**. Pero: referencias de ítems con metadata/NBT (`"minecraft:dye;1;15"`, `oredict:`), carga desde `assets` (cliente) vía `ResourceLocation` en vez de datapack de servidor, sync de capability, toasts y GUI (ver 2.7).
- Escaneo (`ScanningManager`) depende de `IBlockState`, ore dictionary, `Potion`, `Enchantment` (registro dinámico desde 1.21).

### 2.11 Entidades, IA y golems — **Alto**
- 43 entidades con IDs numéricos, `EntityAI*`, `DataParameter`, `EntityRegistry.addSpawn`, atributos en `applyEntityAttributes`.
- Moderno: `EntityType.Builder`, `Goal`/`GoalSelector` (o Brain), `SynchedEntityData`, atributos por `AttributeSupplier` + evento, spawns por biome modifiers/`SpawnPlacements`, loot tables por datos.
- **Golems**: pathfinding propio (`PathNavigateGolemGround/Air`, `NodeProcessor`s), FakePlayer para interactuar con bloques, modelo OBJ modular, seals como entidades/datos de mundo (`SealHandler` en `WorldSavedData`). Uno de los sistemas más costosos.
- Champion modifiers: dependen de eventos de entidad y atributos; adaptación media.

### 2.12 Capabilities de jugador (Knowledge, Warp) y persistencia — **Medio**
- `@CapabilityInject`, `AttachCapabilitiesEvent<Entity>`, `INBTSerializable`, `PlayerEvents.Clone`.
- Moderno: data attachments (NeoForge) / component API (Fabric/CCA), con codecs y sync propio.

### 2.13 Baubles — **Alto (dependencia muerta)**
- Baubles no existe para 26.x. Opciones: Curios (NeoForge) / Trinkets/Accessories (Fabric) si existen para 26.3, o slots propios. Afecta ~16 archivos y a los ítems de vis, warp, cloud ring, goggles.

### 2.14 Pociones, fluidos, sonidos, config, comandos — **Medio**
- `Potion` → `MobEffect` (holders); los shaders de pociones (blur, sunscorned) dependen de 2.1.
- `FluidRegistry`/`BlockFluidClassic`/universal bucket → `FluidType` (NeoForge) o fluidos vanilla `FlowingFluid`.
- `@Config` de Forge → `ModConfigSpec`/config del loader.
- `CommandBase` → Brigadier.
- `SoundEvent` registro con `SoundEvent.createVariableRangeEvent`; `sounds.json` casi compatible.

### 2.15 Assets y localización — **Medio**
Ver `assets.md` §5: carpetas singulares, lang a JSON con claves nuevas, modelos/definiciones de ítem, blockstates sin metadata, loot tables y shaders con nuevos esquemas. Riesgo legal de redistribuir assets de Azanor (ver `assets.md` §6).

### 2.16 API pública para addons — **Medio**
`thaumcraft.api` expone tipos 1.12 en todas las firmas. Debe re-definirse desde cero; no habrá compatibilidad con addons de 1.12.

## 3. Sistemas con menor riesgo (lógica conservable)

- Definición de los 37 aspectos y sus combinaciones; tablas de aspectos de objetos (`ConfigAspects`, ~508 registros) → convertibles a datos (tags/JSON).
- Contenido de research (136 entradas + 12 scans) y textos en `en_us.lang`.
- Fórmulas de aura/flux, inestabilidad de infusión, costes de recetas, parámetros de golems/seals, foci (medium/effect/mod) como diseño.
- Sonidos `.ogg` y la mayoría de texturas PNG (formato compatible, sujetos a licencia).

## 4. Matriz resumen

| Sistema | Riesgo | Motivo principal |
|---|---|---|
| Rendering / FX / shaders / OBJ | Crítico | GL inmediato, LWJGL2, TESR, ModelBase eliminados |
| Bloques + metadata | Crítico | Flattening, VoxelShape, blockstates |
| Ítems + NBT | Crítico | Data Components, item model definitions |
| Aura/Vis/Flux | Crítico | Hilo propio, IDs de dimensión, almacenamiento por chunk |
| Worldgen + biomas + taint de biomas | Crítico | Worldgen data-driven, sin `IWorldGenerator` |
| Networking | Alto | Payloads + StreamCodec |
| GUIs / Thaumonomicon UI | Alto | Menus/Screens, GuiGraphics |
| BlockEntities + essentia | Alto | Tickers, capabilities/transfer, serialización |
| Recetas / ore dictionary | Alto | Recetas data-driven, tags, recipes server-side |
| Entidades / golems / IA | Alto | EntityType, Goals, pathfinding, FakePlayer |
| Baubles | Alto | Dependencia inexistente en 26.x |
| Research (lógica) | Medio-Alto | Datos reutilizables, integración nueva |
| Capabilities jugador | Medio | Attachments/components |
| Pociones/fluidos/config/comandos/sonidos | Medio | APIs renombradas/reemplazadas |
| Assets/lang | Medio | Conversión de formatos + licencia |

## 5. Pendientes antes de diseñar el port

1. Fijar loader (NeoForge / Fabric) y versión exacta de 26.3; verificar las APIs mencionadas en este documento contra esa versión.
2. Resolver la situación legal de assets/código de Azanor.
3. Elegir reemplazo de Baubles.
4. Remapear el descompilado a nombres legibles (sólo como referencia local, sin versionarlo) y recuperar los 10 métodos fallidos con otro descompilador.
5. Definir la tabla de IDs 1.12 → 26.3 (bloques/ítems aplanados).
6. Priorizar: núcleo (aspectos, aura, registro) → research/Thaumonomicon → crafting (arcane, crucible, infusion) → essentia → golems → worldgen/taint → FX.
