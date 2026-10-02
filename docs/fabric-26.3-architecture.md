# Arquitectura objetivo: Thaumcraft sobre Minecraft 26.3 + Fabric

> Estado: arquitectura objetivo previa a la implementación. Las decisiones de la sección «Confirmed architectural decisions» son **requisitos obligatorios**; el resto del documento sigue siendo propuesta y se puede revisar. Este documento no contiene código funcional.
> Referencia de comportamiento: `Thaumcraft-1.12.2-6.1.BETA26` (auditoría en `docs/architecture.md`, `docs/systems.md`, `docs/dependencies.md`, `docs/assets.md`, `docs/porting-risks.md`, PR #1).
> Fecha de verificación de versiones: 2026-10-02. Revisión con decisiones confirmadas del proyecto: 2026-10-02.

## 0. Convenciones del documento

Cada afirmación se clasifica así:

| Etiqueta | Significado |
|---|---|
| **[JAR]** | Hecho observado en el JAR 1.12.2 descompilado. |
| **[Confirmado]** | API o versión moderna comprobada en una fuente oficial (blog de Fabric, Fabric Docs, Javadoc de Fabric API para `+26.3`, Modrinth/Maven). |
| **[Decisión confirmada]** | Decisión tomada por el proyecto. Es un requisito obligatorio y no se reconsidera salvo petición explícita (ver «Confirmed architectural decisions»). |
| **[Recomendación]** | Decisión de arquitectura propuesta por este documento. |
| **[Hipótesis]** | Suposición razonable que todavía no se ha comprobado. |
| **requiere verificación** | No hay documentación clara para 26.3; hay que comprobarlo contra el código de Minecraft 26.3 / Fabric API antes de diseñar sobre ello. |

Los nombres de clases de Minecraft son los nombres oficiales de Mojang. Desde 26.1 el juego ya no está ofuscado y Fabric dejó de dar soporte oficial a Yarn **[Confirmado: blog Fabric 26.1]**.

### 0.1 Versiones de referencia verificadas

| Componente | Versión verificada | Fuente | Notas |
|---|---|---|---|
| Minecraft | 26.3 "Wilderness Bound", publicada el 15-09-2026 | minecraft.net, blog Fabric 26.3 | Java 25 mínimo. |
| Fabric Loader | 0.19.5 (última estable en la fecha) | blog Fabric 26.3 | Cumple el requisito "0.19.x". |
| Fabric API | `0.161.0+26.3` (también existen `0.160.x+26.3`) | `maven.fabricmc.net` metadata | Javadoc consultado: `0.160.4+26.3`. |
| Loom / Gradle | Loom 1.17, Gradle 9.6.0 "at the time of writing" | blog Fabric 26.3 | Plugin `net.fabricmc.fabric-loom` sin remapeo; `implementation`/`compileOnly` en lugar de `modImplementation`; `jar` en lugar de `remapJar` (blog 26.1). |
| Java | 25 | blog Fabric 26.1 y 26.3 | IntelliJ IDEA 2025.3+ para Mixins (blog 26.1). |
| Sodium | `mc26.3-0.9.2-fabric` (estable), `0.9.3-alpha.1` | Modrinth API | Iris lo requiere. |
| Iris | `1.11.7+26.3-fabric` (30-09-2026) | Modrinth API / CurseForge | Sólo prueba de compatibilidad; no es una dependencia. |
| Trinkets Updated | `4.2.1+26.3` (26-09-2026), licencia MIT | Modrinth API | Jar universal Fabric/NeoForge; sólo depende de Fabric API. |
| Accessories (Wisp Forest) | **sin build para 26.3** (último soporte publicado: 1.21.10) | Modrinth API | Se descarta por ahora. |
| Cloth Config | `26.3.159+fabric` | Modrinth API | Opcional; ver §6.19. |
| Baubles (Azanor) | Sólo 1.12.2 y anteriores; licencia CC BY-NC-SA 3.0 | GitHub Azanor/Baubles | Ver §5. **No se porta** [Decisión confirmada]. |
| TerraBlender (Glitchfiend) | `26.3.0.0.9` (beta, 02-10-2026), licencia LGPL-3.0 | Modrinth API; inspección del jar (`fabric.mod.json`, `terrablender.api.*`) | Depende de Fabric API; `minecraft: 26.3`, `java: >=25`. Candidata para insertar Magical Forest (§6.16). |
| Biolith (TerraformersMC) | `3.8.0-beta.1` (23-09-2026), licencia LGPL-3.0 | Modrinth API; inspección del jar (`com.terraformersmc.biolith.api.*`) | Requiere Fabric Loader `>=0.19.5`, `minecraft >=26.3 <26.4` y MixinExtras. Alternativa (§6.16). |

## Confirmed architectural decisions

Estas decisiones son requisitos obligatorios. **No se reconsideran en las siguientes etapas salvo que el proyecto lo pida explícitamente.** Donde el resto del documento las menciona, aparecen marcadas como **[Decisión confirmada]**.

1. **Plataforma:** Minecraft 26.3, Java 25 y Fabric (Fabric Loader 0.19.x + Fabric API `+26.3`). No se usan NeoForge ni configuraciones multiloader.
2. **Reconstrucción, no migración:** Thaumcraft 6.1.BETA26 para 1.12.2 es la referencia de **comportamiento y contenido**. No se traslada su arquitectura ni sus clases. Cada sistema se implementa sobre las abstracciones modernas de Minecraft/Fabric 26.3.
3. **Iris y Sodium:** son **objetivos de compatibilidad**, no dependencias. El mod no compila contra ellos (ni siquiera como `compileOnly`), no los necesita en runtime y no aplica Mixins sobre sus clases (§7.3).
4. **Baubles no se porta:** ni como mod separado ni como reimplementación de su API (§5.3).
5. **Accesorios con Trinkets Updated:** es la solución de accesorios para esta etapa y una dependencia obligatoria. Todo acceso pasa por la fachada interna `AccessoryAccess`. Sólo el adaptador `compat/accessories-trinkets` importa tipos de Trinkets, de modo que el proveedor se puede reemplazar en el futuro sin tocar `api`, `systems` ni `content` (§5.4).
6. **Magical Forest es un bioma real e independiente del Overworld**, con clave, definición, clima y superficie propios. **No negociable.** Queda **descartado** representarlo como región, capa, *feature* o modificación visual de un bioma vanilla. El mecanismo de inserción se oculta tras la fachada `BiomePlacementAccess`. Lo que sigue abierto es **qué mecanismo** se usa (D6), no si el bioma existe (§6.16).

## 1. Principios de arquitectura

1. **Reconstrucción, no migración.** El JAR describe el comportamiento: fórmulas, contenido, valores, flujo de juego. No se trasladan clases. Cada sistema se reescribe sobre las abstracciones de 26.3 (Data Components, codecs, render states, registros dinámicos) **[Decisión confirmada]**.
2. **Datos antes que código.** Todo lo que vanilla 26.3 ya trata como datos (recetas, worldgen, loot, tags, transformadores de bloques, combustibles y compostables) se define como datos. Lo propio de Thaumcraft (aspectos de objetos, investigación, escaneos, recetas de infusión y crisol) también se modela como datos cargados por reload listeners o registros recargables **[Recomendación]**.
3. **Servidor autoritativo, cliente presentacional.** El estado de aura, conocimiento, warp, essentia y golems vive en el servidor. El cliente recibe sólo lo que necesita para la HUD, el Thaumonomicon y los efectos visuales **[Recomendación]**.
4. **Fabric API primero, Mixin como último recurso.** Cada Mixin necesita una justificación escrita (§8) y debe ser pequeño, en un único paquete `mixin` y opcional cuando sea posible **[Recomendación]**.
5. **Rendering sin OpenGL directo.** Desde 26.2 existe un backend Vulkan experimental y está previsto retirar OpenGL; no se admite GL crudo, sólo Blaze3D **[Confirmado: blog Fabric 26.2, Fabric Docs "Basic Rendering Concepts"]**. Todo efecto debe expresarse con `RenderType`/`RenderPipeline`, render states y submits.
6. **Compatibilidad con shaders por diseño.** Iris y Sodium son objetivos de compatibilidad, no dependencias **[Decisión confirmada]**. No se usan programas de shader propios en la ruta principal. Si un efecto los necesita, debe existir una ruta alternativa sin shader propio, que es lo que recomienda Iris (§7).
7. **Fachadas internas para integraciones.** Accesorios (`AccessoryAccess`, **[Decisión confirmada]**), colocación de biomas (`BiomePlacementAccess`, **[Decisión confirmada]**), visores de recetas, configuración y detección de shaders se acceden a través de interfaces internas. Así, cambiar de librería no obliga a tocar el contenido **[Recomendación]** para el resto.
8. **API pública propia y estable.** Se define una API (`api`) nueva, pequeña y basada en tipos de 26.3. No se replica la `thaumcraft.api` 1.12.2 (dependía de `EnumFacing`, `NBTTagCompound`, `World`, etc.) **[Recomendación]**.

## 2. Arquitectura objetivo general

### 2.1 Proyecto y source sets

**[Recomendación]** Empezar con **un único mod Fabric** y un único proyecto Gradle con source sets separados por entorno (`main` común/servidor y `client`), usando la opción de Loom para dividir los source sets por entorno (nombre exacto de la opción en Loom 1.17: requiere verificación). Fabric Docs ya organiza los renderers en `src/client/` **[Confirmado: Fabric Docs "Block Entity Renderers"]**.

La modularidad se consigue con paquetes y límites de dependencia, no con varios jars, por tres motivos:

- Los sistemas centrales (aspectos ↔ aura ↔ investigación ↔ recetas) están muy acoplados en el JAR **[JAR]**.
- Varios jars multiplican la gestión de versiones mientras la API todavía no es estable.
- Más adelante se pueden extraer módulos opcionales (`compat-*`) como subproyectos Gradle o como jars anidados (jar-in-jar), si hace falta.

### 2.2 Capas lógicas

```
┌────────────────────────────────────────────────────────────────────────┐
│ compat/ (adaptadores; Trinkets y biomas obligatorios, resto opcional)  │
│   accessories-trinkets · biome-placement (TerraBlender/Biolith)        │
│   recipe-viewer (EMI/REI/JEI) · iris-detect (sólo reflexión)           │
├────────────────────────────────────────────────────────────────────────┤
│ client/  (sólo source set client)                                      │
│   screens · thaumonomicon · hud · renderers (BER/entity) · particles   │
│   fx (beams, streams, rifts) · model loading (OBJ si aplica) · keybinds│
├────────────────────────────────────────────────────────────────────────┤
│ content/ (bloques, ítems, entidades, BEs, menús, recetas concretas)    │
│   alchemy · artifice · infusion · golemancy · eldritch · auromancy     │
├────────────────────────────────────────────────────────────────────────┤
│ systems/ (lógica de dominio sin contenido concreto)                    │
│   aura (vis/flux) · essentia (red de succión) · research/knowledge     │
│   warp · taint · casting (foci) · golems/seals · aspects-mapping       │
├────────────────────────────────────────────────────────────────────────┤
│ core/                                                                  │
│   registro (helpers) · ids · data components · attachments · payloads  │
│   codecs · config · tags · reload listeners · datagen                  │
├────────────────────────────────────────────────────────────────────────┤
│ api/  (tipos públicos estables: Aspect, AspectList, EssentiaTransport, │
│        AuraAccess, KnowledgeAccess, eventos)                           │
└────────────────────────────────────────────────────────────────────────┘
        Minecraft 26.3 (Mojang names) · Fabric Loader 0.19.x · Fabric API
```

Reglas de dependencia **[Recomendación]**:

- `api` no depende de nada del mod.
- `core` sólo depende de `api`.
- `systems` depende de `core` y `api`.
- `content` depende de `systems`.
- `client` puede depender de todo, pero nada común depende de `client`.
- `compat` sólo depende de `api` y de fachadas de `core`. `AccessoryAccess` y `BiomePlacementAccess` son fachadas de `core`; **sólo** `compat` importa tipos de Trinkets Updated o de la librería de biomas **[Decisión confirmada]**.

Estas reglas se pueden hacer cumplir más adelante con un test de arquitectura (por ejemplo ArchUnit; añadirlo es una decisión pendiente, no una dependencia de runtime).

### 2.3 Puntos de entrada

| 1.12.2 **[JAR]** | 26.3 **[Confirmado salvo indicación]** |
|---|---|
| `@Mod` + `@SidedProxy` (`ClientProxy`/`ServerProxy`) | `fabric.mod.json` con entrypoints `main` (`ModInitializer`), `client` (`ClientModInitializer`) y `fabric-datagen` (`DataGeneratorEntrypoint`). |
| `preInit`/`init`/`postInit` | No hay fases. El registro ocurre en el `onInitialize`. Lo que depende de datos va a reload listeners (`resource.v1.reloader`) y a eventos de ciclo de vida (`ServerLifecycleEvents`, `CommonLifecycleEvents.TagsLoaded`). |
| IMC | Entrypoints personalizados de Fabric (`FabricLoader#getEntrypointContainers`) o una API pública con registro explícito **[Recomendación]**. |
| 74 `@SubscribeEvent` en 20 clases | Eventos de Fabric API (callbacks). La tabla de §3 indica cuáles no tienen equivalente. |

### 2.4 Identificadores y espacio de nombres

- **[Recomendación]** Decidir **antes de registrar nada** el mod id y el namespace: `thaumcraft` o uno nuevo (por ejemplo `thaumcraft_reborn`). Afecta a todos los IDs, assets, tags, datos y payloads; cambiarlo después obliga a rehacerlo todo. Va ligado a la decisión legal sobre nombre y assets (§10, D1).
- **[Recomendación]** Mantener una **tabla de mapeo de IDs 1.12.2 → 26.3** (`docs/id-mapping.md`, futura) generada a partir de `ConfigBlocks`/`ConfigItems` y de los `metadata`. En 26.x no hay metadata, así que cada combinación `id:meta` pasa a ser un bloque o ítem propio, o una propiedad de blockstate.
- Desde 26.2, vanilla separa los ids en `BlockIds`, `BlockItemIds` e `ItemIds`, y datagen usa esas claves; se recomienda separar los ids de las instancias **[Confirmado: blog Fabric 26.2]**. Diseñar así la clase de ids desde el primer día.

## 3. Tabla de sistemas: Thaumcraft 1.12.2 → equivalente moderno

Leyenda de la columna "Fabric API":

- **D** = se implementa directamente con Fabric API o vanilla.
- **P** = requiere un sistema propio.
- **M** = puede requerir Mixin.

| Sistema | 1.12.2 **[JAR]** | Equivalente 26.3 | Fabric API | Cambio conceptual | Estrategia |
|---|---|---|---|---|---|
| Registro | `RegistryEvent.Register<T>` en `Registrar`; `GameRegistry` | `Registry.register(BuiltInRegistries.X, key, value)` en `onInitialize`; registros propios con `FabricRegistryBuilder` (paquete `event.registry`; API exacta: requiere verificación) | D | No hay eventos de registro por tipo; ids separados de instancias (26.2) | Helpers de registro propios y finos en `core`; ids declarados por separado. |
| Bloques | ~152 bloques con metadata y `IBlockState` | Un bloque por variante, o `BlockState` con propiedades; `BlockBehaviour.Properties` | D | No hay metadata; las propiedades de bloque cambiaron; `Block#getProvidedEnchantmentPower` (26.3) sirve para estanterías | Aplanar las variantes según la tabla de IDs; bloques "tipo" (`BlockTCDevice`) → jerarquías pequeñas. |
| Ítems | ~105 ítems; subtipos por meta; NBT en `ItemStack` | `Item` + `Item.Properties`; **Data Components** para todo el estado; `ItemStackTemplate` (26.1) para plantillas antes de cargar el mundo | D | No se puede crear un `ItemStack` antes de cargar el mundo; el NBT arbitrario se sustituye por componentes tipados | Definir los componentes en `core` (§6.4); no hay subtipos por meta. |
| BlockEntities | 48 `TileEntity`; `readFromNBT`/`writeToNBT`; `ITickable` | `BlockEntity` + `BlockEntityType`; ticker de `EntityBlock#getTicker`; serialización con `ValueInput`/`ValueOutput` (nombre y firma en 26.3: requiere verificación); `fabric-object-builder` (`object.builder.v1.block.entity`) | D | Ticking por ticker estático; sincronización por paquete de actualización del BE; componentes de BE (`DataComponentMap` en BE: requiere verificación) | Reescribir cada BE con su estado como record/codec. |
| Datos persistentes | Capabilities (`PlayerKnowledge`, `PlayerWarp`), `WorldSavedData`, NBT de chunk | **Fabric Data Attachments** sobre `Entity`, `BlockEntity`, `ServerLevel`, `ChunkAccess` y `GlobalAttachments`, con `persistent(Codec)`, `syncWith(StreamCodec, predicate)` y `copyOnDeath()` | D | Las capabilities de Forge no existen | Conocimiento y warp → attachment de jugador; aura → attachment de chunk; estado global → `GlobalAttachments`/`SavedData`. |
| NBT | `NBTTagCompound` directo en ítems, BEs y paquetes | Codecs (`Codec`, `MapCodec`, `StreamCodec`); NBT sólo como formato de guardado detrás de codecs | D | El NBT deja de ser la API de datos | Ningún sistema nuevo lee o escribe NBT a mano. |
| Networking | `SimpleNetworkWrapper`, 41 `IMessage` | `CustomPacketPayload` (record) + `StreamCodec`; `PayloadTypeRegistry.clientboundPlay()/serverboundPlay()`; `ServerPlayNetworking`/`ClientPlayNetworking` | D | Payloads tipados; registro separado por dirección | Recalcular el inventario: buena parte de los 41 mensajes se resuelve con sincronización de attachments y BEs (§6.6). |
| Menús/GUIs | `IGuiHandler` con 22 IDs; `Container`/`GuiContainer` | `MenuType` + `AbstractContainerMenu`; `ExtendedMenuType`/`ExtendedMenuProvider` de Fabric (`menu.v1`) para enviar datos iniciales; `AbstractContainerScreen`; renderizado por `GuiGraphicsExtractor` | D | No hay IDs numéricos de GUI; la GUI usa el modelo de extracción a render state; `setScreen` se mueve a `Minecraft.getInstance().gui` (26.2) | Un `MenuType` por GUI con contenedor; el Thaumonomicon es una `Screen` sólo de cliente. |
| Rendering BE | 22 TESR con `GlStateManager`/`Tessellator` | `BlockEntityRenderer<BE, RenderState>` con `createRenderState`, `extractRenderState` y `submit(state, PoseStack, SubmitNodeCollector, CameraRenderState)` | D (+P) | Separación extracción/envío; sin estado GL | Reescribir cada renderer (§7). |
| Rendering de entidades | ~30 `Render*`, ~20 `ModelBase` | `EntityRenderer` + `EntityRenderState`; `LayerDefinition`/`ModelLayerRegistry`; `EntityRendererRegistry`; capas con `LivingEntityRenderLayerRegistrationCallback` | D | `ModelBase` → `Model`/`ModelPart` declarativo | Volver a modelar las geometrías como `LayerDefinition`. |
| Partículas | 24 clases FX propias; `ParticleEngine` propio; `FXDispatcher` | `ParticleType` (`FabricParticleTypes`), `ParticleProviderRegistry`, `ParticleGroupRegistry`, `FabricSpriteSet`, `ParticleRenderEvents` | D (+P) | No hay motor de partículas propio con GL; partículas de GUI separadas | Reimplementar sobre partículas vanilla; emisión desde servidor por payload. |
| OBJ/modelos | Loader OBJ propio (27 OBJ, 6 MTL); blockstates Forge v1 (170) | No hay loader OBJ en vanilla ni en Fabric API. Fabric Model Loading API (`ModelLoadingPlugin`, `UnbakedModelDeserializer`, `CustomUnbakedBlockStateModel`) + Renderer API (mesh) están disponibles en 26.3 | P | `forge_marker` no existe; las definiciones de modelos de ítem (`assets/<ns>/items/`) son obligatorias desde 1.21.4 (formato en 26.3: requiere verificación) | Convertir los OBJ estáticos a JSON y los dinámicos a `LayerDefinition`; un loader OBJ propio sólo si quedan casos (§6.11). |
| Shaders | ARB shaders (`ShaderHelper`: ender/sketch), post shaders (`shaders/post/*`: blur, desaturate, hunger, sunscorned) | `RenderPipeline`/`FabricRenderPipeline`; post-efectos vanilla (existe `/posteffect` en 26.3; API de mod: requiere verificación) | P/M | Shaders propios ignorados con un shader pack de Iris activo | Ruta principal sin shaders propios; post-efectos opcionales con alternativa HUD (§7). |
| Eventos de render | `RenderWorldLastEvent`, `RenderGameOverlayEvent`, `EntityViewRenderEvent`, `RenderLivingEvent` | `LevelRenderEvents` (`AfterTranslucentFeatures`, `CollectSubmits`…), `LevelExtractionEvents`, `HudElementRegistry`; `HudRenderCallback` eliminado (26.1) | D | Las fases cambiaron | Mapear cada hook a su fase moderna (§7.2). |
| Entidades | 43 entidades; IA con `EntityAITasks` | `EntityType` vía `FabricEntityType.Builder`; `FabricDefaultAttributeRegistry`; `Goal`/`GoalSelector` o `Brain` | D | Atributos obligatorios; data trackers (`EntityDataAccessor`) en lugar de `DataManager`; `FabricEntityDataRegistry` para serializadores | Reescribir la IA con goals; bosses con `ServerBossEvent`. |
| Golems | `EntityThaumcraftGolem`, partes (material/cabeza/brazos/patas/addon), seals, tasks | Entidad propia + Data Components/attachment para la configuración de partes + sistema propio de seals/tasks | P | — | El sistema más propio después de la investigación (§6.13). |
| Worldgen | `IWorldGenerator` imperativo; 2448 `setBlockState` en `WorldGenMound` | Features data-driven (`worldgen/feature` en 26.3, sin objeto `config`), placed features, `BiomeModifications`; estructuras con `Structure` + plantillas NBT/jigsaw | D/P | La generación imperativa por chunk desaparece; el formato de feature cambió en 26.3 | Features propias registradas en `BuiltInRegistries.FEATURE_TYPE`; estructuras como plantillas (§6.15). |
| Biomas | 3 biomas (Magical Forest, Eerie, Eldritch); `setBiomeArray` para taint/magic | Biomas data-driven propios (`worldgen/biome`); inserción en el Overworld con una librería mantenida tras `BiomePlacementAccess` (TerraBlender recomendada, Biolith como alternativa); `NetherBiomes`/`TheEndBiomes` para Nether/End | D (vía librería)/M (plan C) | Fabric API **no** tiene API para añadir biomas al Overworld **[Confirmado: `biome.v1`]**; biomas en celdas de 4×4×4 | **Magical Forest es un bioma real [Decisión confirmada]**; §6.16. |
| Recetas | 73 arcanas, 56 infusión, 42 crisol, 6 multibloque, smelting bonus | `Recipe`/`RecipeType` + `RecipeSerializer(MapCodec, StreamCodec)` (26.1); recetas dentro de registros recargables (26.3); `FabricRecipeManager`/`recipe.v1.sync` | D/P | Serializers simplificados; recetas sincronizadas selectivamente al cliente | Tipos de receta propios como datos (§6.17). |
| Research | 7 categorías, ~136 entradas JSON, 12 scans; `IPlayerKnowledge` | Reload listener o registro recargable (`DynamicRegistries.registerReloadable`, nuevo en 26.3: requiere verificación de detalles); conocimiento en attachment de jugador | P | — | Formato JSON nuevo con codec, convertido desde los JSON 1.12.2 (§6.18). |
| Aspectos | 37 aspectos (6 primales); 508 registros por código; Ore Dictionary | Registro propio estático de aspectos + mapeo objeto→aspectos por datos (JSON + tags `c:`) | P | No hay Ore Dictionary (son tags) | §6.1. |
| Essentia | `IEssentiaTransport` con succión por cara | API propia expuesta con `BlockApiLookup` (Fabric `lookup.v1.block`) | P (+D) | No hay capabilities; `EnumFacing` → `Direction` | §6.2. |
| Vis/Aura/Flux | `AuraChunk` (base/vis/flux), `AuraHandler` + `AuraThread` (hilo propio) | Attachment de chunk persistente + simulación en `ServerTickEvents.END_LEVEL_TICK` | P (+D) | **No se toca el mundo desde otro hilo** | §6.3. |
| Taint | `TaintHelper`, bloques de taint, semillas, propagación | Random ticks / scheduled ticks + tags + attachment de chunk | P | Cambio de bioma en runtime distinto | §6.14. |
| "Nodos de aura" | **No existen en 6.1.BETA26** (son de TC4); el aura es por chunk **[JAR]** | — | — | — | No se implementan; ver §6.3. Cualquier mecánica de nodos sería contenido nuevo y queda fuera del alcance de referencia. |
| Wand/Staff | **No existen en TC6**. TC6 usa *casters* (guanteletes, `ItemCaster`) + *foci* con grafo de nodos (`FocusMedium`/`FocusEffect`/`FocusMod`, `FocusElementNode`) **[JAR]** | Ítems + Data Components (foco, grafo serializado) + entidades proyectil + payloads | P | — | §6.12. |
| Accesorios | Baubles 1.5.2 (16 archivos, 34 imports) | Trinkets Updated 4.2.1+26.3 tras la fachada `AccessoryAccess` **[Decisión confirmada]** | D (vía librería) | Baubles no se porta **[Decisión confirmada]** | §5. |
| Configuración | Forge `@Config` (`ModConfig`, 455 líneas) | Fabric API no tiene API de config: config propia basada en codecs | P | — | §6.19. |
| Datagen | No existe (JSON a mano) | Fabric Data Generation (`DataGeneratorEntrypoint`, `FabricDataGenerator`, providers para recetas, loot, tags, modelos y avances) | D | — | §6.20. |
| Assets | 767 PNG, 511 JSON, 111 OGG, 9 `.lang` | Paquetes de recursos 26.3 (formato 97.1 en RC); lang JSON | D | `.lang` → `.json`; blockstates Forge → vanilla | §6.21. |
| Sonidos | 65 eventos, 111 OGG | `SoundEvent` registrado en `BuiltInRegistries.SOUND_EVENT` + `sounds.json` | D | — | Directo. |
| Traducciones | 9 `.lang` | `assets/<ns>/lang/<locale>.json`; `en_us` generado con datagen | D | — | §6.21. |
| Pociones | 9 `Potion` | `MobEffect` registrado; las pociones de brewing son datos en 26.3 | D | `FabricPotionBrewingBuilder` eliminado (26.3) | Efectos propios + recetas de brewing en JSON si hacen falta. |
| Ore Dictionary | 58 usos | Tags convencionales `c:` (`tag.convention.v2`) | D | — | Mapear cada clave OreDict a un tag. |
| Access transformer | `tc_at.cfg` (24 entradas) | Class tweaker / access widener (nombre en Loom 1.17: requiere verificación) | M | — | Reevaluar cada entrada; la mayoría deberían desaparecer. |
| Fuel/compost/stripping | `GameRegistry`/Forge | Componentes `COOKING_FUEL`, `COMPOSTABLE`, `BREWING_FUEL` y `BLOCK_TRANSFORMER` (26.3); `DefaultItemComponentEvents`, `BlockTransformerHelper` | D | `FuelRegistry`, `CompostingChanceRegistry` y `Strippable/Tillable/FlattenableBlockRegistry` eliminados en 26.3 | Troncos de greatwood/silverwood con transformador de bloque en datos. |

## 4. Dependencias externas propuestas

| Dependencia | Tipo | Justificación | Riesgo |
|---|---|---|---|
| Fabric Loader 0.19.x | Obligatoria | Requisito del stack. | Bajo. |
| Fabric API `0.161.0+26.3` o posterior | Obligatoria | Registros, networking, attachments, menús, renderers, partículas, datagen, worldgen, lookup, eventos. | Bajo; seguir los parches `+26.3`. |
| Trinkets Updated `4.2.x+26.3` | **Obligatoria [Decisión confirmada]** | Solución de accesorios de esta etapa (§5). MIT. Sólo depende de Fabric API. Sólo la importa `compat/accessories-trinkets`. | Medio: proyecto pequeño (fork mantenido por Patbox) que tiene que seguir el ritmo de cada versión de Minecraft. Se mitiga con la fachada `AccessoryAccess`. |
| Iris / Sodium | **No** son dependencias de compilación ni de runtime; son objetivos de compatibilidad **[Decisión confirmada]** | El mod no compila contra ellos, tampoco como `compileOnly`. La detección de un shader pack activo usa `FabricLoader#isModLoaded` y reflexión aislada en `compat/iris-detect` (§7.3). Para probar en desarrollo se pueden cargar como mods de runtime local, sin exponerlos al classpath de compilación (configuración de Loom 1.17 para esto: requiere verificación). | Medio (§7). |
| Visor de recetas (EMI, REI o JEI) | Opcional, `compileOnly` + módulo `compat` | Mostrar recetas arcanas, de infusión y de crisol. Decisión D9. | Bajo; se pospone. |
| Mod Menu + Cloth Config | Opcional, sólo cliente | Pantalla de configuración. Cloth tiene build 26.3; Mod Menu también (según terceros: requiere verificación en Modrinth). | Bajo; no son necesarios para la v1. |
| Librería de inserción de biomas en el Overworld | **Obligatoria** (la exige la decisión confirmada sobre Magical Forest) | Recomendada: TerraBlender `26.3.0.0.x` (LGPL-3.0). Alternativa: Biolith `3.8.x` (LGPL-3.0). Sólo la importa `compat/biome-placement`, tras `BiomePlacementAccess` (§6.16). Elección final: D6. | Medio: las builds 26.3 de ambas están en **beta**. LGPL: se usa como dependencia externa sin modificarla; incrustarla con jar-in-jar requiere verificación de obligaciones de licencia. |
| Librería OBJ | **No** inicialmente | Se prefiere convertir los OBJ (§6.11). | — |

Dependencias **descartadas** explícitamente:

- Baubles: ni port ni reimplementación de su API (licencia NC-SA, sólo Forge 1.12.2) **[Decisión confirmada]**.
- CodeChickenLib (las utilidades de render embebidas en el JAR no se trasladan).
- GLE/LWJGL directo (OpenGL crudo no está permitido).
- Botania API embebida.
- Accessories (sin build 26.3).
- Cualquier librería de matemáticas: JOML ya viene con Minecraft (requiere verificación de la versión incluida).

## 5. Reemplazo de Baubles

### 5.1 Qué usa Thaumcraft de Baubles **[JAR]**

- Tipos de slot: `AMULET`, `RING`, `BELT`, `HEAD`, `BODY`, `CHARM` y `TRINKET`.
- Callbacks `onWornTick`, `onEquipped`, `onUnequipped`, `canEquip` y `canUnequip`.
- Iteración del inventario de baubles de un jugador para descuentos de vis, visión de aura y revelado.
- Render en el jugador (`IRenderBauble`): goggles, amuletos y cinturón.
- Sincronización del inventario de baubles al cliente.

Ítems afectados: Goggles of Revealing (también son casco), Amulet of Vis (Vis Amulet), Cloud Ring, Verdant Charms, Curiosity Band, Charm of Undying, Ring of Runic Shielding, Amulet/Girdle of Runic Shielding, Bauble Blanks y Thaumostatic Girdle (lista exacta: se valida contra `ConfigItems` al implementar).

### 5.2 Comparación

| Criterio | Trinkets Updated | Accessories (Wisp Forest) | Slots propios | Port de Baubles como mod separado |
|---|---|---|---|---|
| Disponible en 26.3 | **Sí**, `4.2.1+26.3` **[Confirmado]** | **No** (último 1.21.10) **[Confirmado]** | N/A | No existe; habría que hacerlo. |
| Licencia | MIT | MIT | Propia | **CC BY-NC-SA 3.0**: no comercial y share-alike; impone restricciones a la distribución y obliga a usar esa misma licencia en derivados **[Confirmado: README]**. |
| Slots data-driven | Sí (grupos/slots por JSON) | Sí | Habría que diseñarlo | El original tiene 7 slots fijos. |
| UI de inventario | Incluida | Incluida | **Propia + Mixins** en `InventoryScreen`/`InventoryMenu` | Propia + Mixins. |
| Persistencia y sync | Incluida | Incluida | Attachment de jugador (fácil) | Igual que "slots propios". |
| Render en jugador | API de renderer de trinket (firma 26.3: requiere verificación) | API de renderer | `LivingEntityRenderLayerRegistrationCallback` + capa propia | Igual que "slots propios". |
| Compatibilidad con otros mods | Alta en el ecosistema Fabric/NeoForge que use Trinkets | Alta, con capas de compatibilidad, pero no disponible | Nula: otros mods no ven los slots | Nula fuera de Thaumcraft. |
| Coste de mantenimiento | Bajo para nosotros; dependemos del ritmo de actualización | — | Alto (UI + Mixins por versión) | Alto: es un segundo proyecto. |
| Riesgo de versión | Medio (proyecto de un mantenedor) | Alto (no hay build) | Bajo de dependencia, alto de Mixins | Alto. |

### 5.3 Valoración de "portar Baubles como mod separado"

**[Decisión confirmada] No se porta Baubles**, ni como mod separado ni como reimplementación de su API. Los motivos quedan aquí como contexto; la decisión no se reabre:

1. **Licencia.** La CC BY-NC-SA 3.0 impide el uso comercial, obliga a que los derivados mantengan la misma licencia y no está pensada para código. Una reimplementación limpia (no un port) sería posible, pero entonces deja de ser "Baubles": es, de hecho, la opción "slots propios" con otro nombre.
2. **Valor nulo para compatibilidad.** Ningún mod moderno usa la API de Baubles. Portarla crearía una API que sólo usaría Thaumcraft.
3. **El trabajo técnico es el mismo** que en "slots propios" (UI del inventario, Mixins, sync, render), más el mantenimiento de un segundo mod.
4. Usar "los mismos mecanismos" que con Thaumcraft (auditar y reconstruir) es viable técnicamente, pero no aporta nada que Trinkets Updated no ofrezca ya en 26.3.

### 5.4 Decisión confirmada

- **[Decisión confirmada] Trinkets Updated es la solución de accesorios de esta etapa** (dependencia obligatoria), **siempre detrás de la fachada interna** `AccessoryAccess`, que vive en `core`. Ninguna clase de `api`, `systems` ni `content` importa tipos de Trinkets:
  - `getEquipped(player, predicate)`, `isEquipped(player, item)` y `forEachEquipped(player, consumer)`.
  - Registro de comportamiento por ítem: tick, equipar/desequipar, puede equipar.
  - Registro de render por ítem (lado cliente).
- Mapeo de slots propuesto: `AMULET` → `chest/necklace`, `RING` → `hand/ring` (×2), `BELT` → `legs/belt`, `HEAD` → `head/face`, `CHARM` → grupo y slot propios de Thaumcraft si Trinkets no tiene uno equivalente, `BODY` → `chest/back` o el que más se acerque. Los nombres concretos de grupo y slot en Trinkets Updated 4.2: requiere verificación.
- Las Goggles of Revealing pueden equiparse **también** en el slot de casco vanilla (`EquipmentSlot.HEAD`). La lógica "¿lleva goggles?" consulta ambas vías mediante la fachada.
- **Requisito de reemplazabilidad [Decisión confirmada]:** la fachada sólo expone tipos de Minecraft o de Thaumcraft (`Player`, `ItemStack` y un enum propio de tipo de accesorio), nunca tipos de Trinkets. Si Trinkets Updated deja de mantenerse, otro proveedor (por ejemplo **slots propios**: attachment de jugador + pestaña de UI) implementa la misma fachada sin tocar el contenido. Ese es el plan B, no el plan A. La persistencia de lo equipado la gestiona el proveedor, así que un cambio de proveedor exigiría migrar los accesorios guardados en mundos existentes (estrategia: requiere verificación).
- Por ahora no se hace una implementación dual Trinkets + Accessories; se reevaluará si Accessories publica build 26.x.

## 6. Diseño por sistema

### 6.1 Aspectos

- **[JAR]** 37 aspectos fijos con componentes (`Aspect(tag, color, components[])`); 508 registros por código (`registerObjectTag`, `registerEntityTag`, `registerComplexObjectTag`); generación automática de aspectos de objetos a partir de las recetas.
- **[Recomendación]**:
  - **Registro propio estático** `thaumcraft:aspect` (`FabricRegistryBuilder`, API exacta: requiere verificación). Los 37 aspectos se registran por código: son parte de la lógica, las recetas y los codecs. Lo de "aspectos añadidos por addons" se resuelve registrando en ese mismo registro.
  - `AspectList` inmutable con `Codec` y `StreamCodec`, que se usa en componentes, recetas, attachments y payloads.
  - **Mapeo objeto→aspectos por datos**: `data/<ns>/thaumcraft_aspects/items/*.json` y `.../entities/*.json` admiten ids, tags (`#c:ingots/iron`) y predicados de componentes. Se cargan con un reload listener del servidor.
  - **Generación derivada** (aspectos calculados desde recetas) en un paso posterior al reload, cuando ya hay recetas y tags (`ServerLifecycleEvents.EndDataPackReload` / `CommonLifecycleEvents.TagsLoaded`; orden exacto: requiere verificación). Se cachea el resultado y se sincroniza al cliente por payload al hacer login o reload. El cálculo puede ser costoso: hay que limitarlo y detectar ciclos.
- **Riesgo:** el algoritmo original de derivación depende del `CraftingManager` de 1.12.2; los resultados no serán idénticos con las recetas de 26.3. Se tratará como una aproximación documentada.

### 6.2 Essentia

- **[JAR]** Transporte por succión (`getSuctionType/Amount`, `getEssentiaAmount`, `takeEssentia/addEssentia` por cara), en jars, tubes, valves, filters, buffers, mirrors, alembics, centrifuges, smelters y crucible.
- **[Recomendación] API propia** `EssentiaTransport` (en `api`) expuesta mediante `BlockApiLookup<EssentiaTransport, Direction>` **[Confirmado: existe `lookup.v1.block.BlockApiLookup`]**. Así los addons pueden participar sin capabilities.
- **No usar Fabric Transfer API como modelo principal.** La essentia no es un fluido y su semántica es *pull por succión* con prioridad, no inserción/extracción transaccional. Opción a evaluar (D8): exponer **además** un `Storage<EssentiaVariant>` con transacciones, para que otros mods (tuberías genéricas) la muevan.
- La simulación de tubos se hace en tick del servidor y por red (grafo cacheado e invalidado al cambiar vecinos), no BE a BE en cada tick como en 1.12.2, para controlar el coste. Hay que preservar el comportamiento observable (velocidades, prioridades). Esto requiere extraer las constantes del JAR.
- Mirrors de essentia (enlace entre dimensiones/posiciones): `GlobalPos` en un componente + búsqueda del BE destino sólo si su chunk está cargado.

### 6.3 Aura, Vis y Flux

- **[JAR]** `AuraChunk{base, vis, flux}` por chunk y dimensión; `AuraThread` (hilo Java propio, ~1 s); difusión entre chunks vecinos, regeneración hacia `base` modulada por la fase lunar (`phaseTable`), conversión de exceso de vis en flux, `AURA_CEILING = 500` y Flux Rift cuando `flux > base * 0.75` (aprox.).
- **[Recomendación]**:
  - **Persistencia:** attachment de chunk persistente (`AttachmentRegistry` sobre `ChunkAccess`) con un codec `{base:int, vis:float, flux:float}`. El `base` se inicializa con un hook de generación de chunk (`ServerChunkEvents.Generate` existe en 26.3; momento exacto del evento: requiere verificación) según el bioma y el ruido, imitando el original.
  - **Simulación:** en el **hilo del servidor**, en `ServerTickEvents.END_LEVEL_TICK`, con presupuesto por tick (round-robin de chunks cargados para repartir la carga de ~1 s). **No** hay hilo independiente que lea o escriba el mundo. Si hace falta paralelismo, se calcula sobre una instantánea inmutable en un worker y se aplica en el hilo principal.
  - **Chunks no cargados:** el original mantenía datos de chunks fuera de memoria. Política: sólo se simulan los chunks cargados y, al cargar un chunk, se aplica una "recuperación" en función del tiempo transcurrido (fórmula de compensación: a definir para preservar la sensación original).
  - **Sincronización al cliente:** no se sincroniza el attachment completo. La HUD de vis/flux (goggles, thaumometer) pide valores del chunk actual con un payload periódico (por ejemplo cada 10–20 ticks) sólo a los jugadores que lo necesitan.
  - **API pública:** `AuraAccess.drainVis(level, pos, amount, simulate)`, `addFlux(level, pos, amount)`, `getVis`, `getFlux` y `getBase`, equivalentes conceptuales de `AuraHelper`.
- **Nodos de aura:** no forman parte de TC6 (§3). No se diseñan.

### 6.4 Data Components (ítems)

**[Recomendación]** Inventario inicial de componentes propios (`DataComponentType` registrado, con `Codec` persistente y `StreamCodec` de red):

| Componente | Uso | Sustituye a |
|---|---|---|
| `thaumcraft:aspects` (`AspectList`) | Phials, jars de essentia como ítem, crystals, items con aspectos fijos | NBT `Aspects` |
| `thaumcraft:vis_charge` | Casters y amuletos de vis | NBT de carga |
| `thaumcraft:focus` | Foco insertado (grafo serializado, §6.12) | NBT `focus` |
| `thaumcraft:focus_pouch` | Contenido de la Focus Pouch (o vanilla `CONTAINER`, a verificar) | Inventario NBT |
| `thaumcraft:golem_properties` | Golem placer (material, cabeza, brazos, patas, addon) | `GolemProperties` en long/NBT |
| `thaumcraft:seal` | Sellos | NBT |
| `thaumcraft:research_note` / `knowledge` | Notas, fragmentos | NBT |
| `thaumcraft:linked_pos` (`GlobalPos`) | Mirrors, hand mirror, golem bell | NBT con coordenadas |
| `thaumcraft:infusion_enchantments` | Encantamientos de infusión | NBT `infench` |
| `thaumcraft:warping` | Warp de equipo | NBT `TC.WARP` |

Los componentes vanilla se usan cuando ya existen (encantamientos, nombre, `CUSTOM_DATA` **sólo** como último recurso, comida y durabilidad). Los casos especiales de infusión (Runic Shielding, Warping) deberían aprovechar los componentes de equipamiento y atributos vanilla siempre que se pueda; la extensibilidad concreta en 26.3 requiere verificación.

### 6.5 BlockEntities

- **[Recomendación]** El estado de cada BE es un record o clase con un codec y una lista explícita de campos "sincronizados al cliente". Las actualizaciones se envían con el paquete de actualización del BE (`getUpdatePacket`/`getUpdateTag`; firmas en 26.3: requiere verificación) y no con payloads propios, salvo animaciones puntuales.
- Los BE multibloque (Infusion Matrix con pedestales, Thaumatorium, Golem Builder, Crucible + Alembics) **detectan** la estructura con un validador de patrones propio, no con Mixins.
- Para exponer inventarios a hoppers y otros mods se usa la Fabric Transfer API (`ItemStorage.SIDED`), mediante `BlockApiLookup`.
- Los BE que hacen tick necesitan control de coste: un ticker sólo para las variantes que lo requieren.

### 6.6 Networking

- **[Recomendación]** Reinventariar los 41 mensajes **[JAR]** en tres grupos:
  1. **Sustituidos por sincronización declarativa:** conocimiento y warp del jugador (attachment con `syncWith(..., targetOnly())`), estado de BEs (paquete de actualización) y datos de entidades (`EntityDataAccessor`).
  2. **Payloads de efecto** (servidor→cliente): partículas FX, zaps, beams, sonidos especiales, bolt de focus. Se agrupan en pocos payloads parametrizados (`FxPayload{type, pos, data}`) para no tener decenas de tipos.
  3. **Payloads de acción** (cliente→servidor): investigación (completar un paso o una nota), configuración de foci en la mesa, seals, teclas (cambio de foco), menús. **Toda acción se valida en el servidor**: distancia, permisos, conocimiento e ítems.
- Los payloads de configuración (aspectos derivados, investigación, config de servidor) se envían en la fase de *configuration* o al hacer login o reload. Si la fase de configuration de Fabric es la adecuada para datos de reload: requiere verificación.

### 6.7 Menús y GUIs

- 22 IDs **[JAR]**: las GUIs con inventario (Arcane Workbench, Thaumatorium, Smelter, Golem Builder, Focal Manipulator, Research Table, Pech, Turret, Hungry Chest, Spa, Void Siphon, Potion Sprayer, Focus Pouch, Logistics, Arcane Bore…) pasan a ser un `MenuType` cada una. Las que necesitan datos iniciales (posición, configuración) usan `ExtendedMenuType` **[Confirmado: `menu.v1`]**.
- Las GUIs sin contenedor (Thaumonomicon, configuración de seals si no requiere slots, el visor de *research notes*) son `Screen` de cliente, con acciones vía payload.
- El renderizado de las GUIs usa `GuiGraphicsExtractor`: se "extrae" al render state y no se dibuja en modo inmediato **[Confirmado: Fabric Docs]**. Los efectos dentro de la GUI (partículas del Thaumonomicon, aspectos animados, el "swirl" de la Research Table) se reimplementan como elementos de GUI. Para elementos 3D dentro de la GUI hay `PictureInPictureRendererRegistry` **[Confirmado: existe la clase en Fabric API 26.3]**; su uso exacto requiere verificación.

### 6.8 Rendering general

Ver §7 para el análisis completo y la compatibilidad con Iris.

### 6.9 Partículas y efectos

- **[JAR]** `ParticleEngine` y `FXDispatcher` propios (116 archivos los usan), 24 clases FX, partículas de GUI, beams (`FXBeamWand`, `FXBeamBore`…), streams de essentia y void (`FXVoidStream`) con GLE.
- **[Recomendación]**:
  - Partículas de mundo: `ParticleType`s propios (`FabricParticleTypes`) con `ParticleProvider` y `FabricSpriteSet`; las texturas se cargan en el atlas de partículas. Lo que requiere ordenación o blending especial se agrupa con `ParticleGroupRegistry` (semántica en 26.3: requiere verificación).
  - `FXDispatcher` se convierte en una fachada de cliente que recibe `FxPayload` y crea partículas. El servidor nunca llama al cliente directamente.
  - Beams, streams y arcos (rayos): **geometría generada en CPU** (tiras de quads orientadas a cámara) enviada en `LevelRenderEvents.CollectSubmits` o en el `submit` del BER o la entidad correspondiente, con `RenderType`s vanilla translúcidos o emisivos (aditivos sólo si existe un pipeline vanilla equivalente; requiere verificación).
  - Las partículas de GUI se implementan en la pantalla, sin `ParticleEngine` de mundo.

### 6.10 Entidades

- 43 entidades **[JAR]**: monstruos (Pech, Wisp, Eldritch Crab, Guardian, Inhabited Zombie, Taint creatures, Cultists), bosses (Eldritch Golem, Cultist Leader, Cultist Portal, Taintacle Giant), constructos (Arcane Bore, Turrets), golems, proyectiles (Golem Bobber, focus bolts, alumentum, grappling…), Flux Rift, Special Item, Falling Taint, etc.
- **[Recomendación]**:
  - `FabricEntityType.Builder` y atributos con `FabricDefaultAttributeRegistry`.
  - IA con `Goal`s, reimplementando el *comportamiento* de cada `EntityAI*` original. Se usa `Brain` sólo si un mob necesita memorias complejas (por ejemplo, los Pech con su comercio).
  - Spawns naturales con `BiomeModifications` (spawn settings).
  - El trading de Pech es una mecánica propia. El trading de aldeanos vanilla es data-driven desde 26.1, pero los Pech no son aldeanos.
  - **Métodos no descompilados [JAR]:** `EntityCultistPortalGreater.onUpdate` (`func_70071_h_`). Su comportamiento debe reconstruirse por observación en el juego 1.12.2 (D10).

### 6.11 Modelos OBJ y modelos de bloques

- **[JAR]** 27 OBJ (crucible, infusion pillar, alembic, tubes, golems, ítems 3D…) cargados con un loader propio y renderizados en TESR o ítems.
- **[Recomendación] Estrategia escalonada:**
  1. **OBJ estáticos de bloque** → convertir **fuera de línea** a modelos de bloque JSON vanilla (cubos y planos) siempre que la geometría lo permita. Es la mejor opción para Iris y Sodium: geometría de chunk normal.
  2. **Geometría animada** (partes móviles de golems, infusion matrix, bellows, alembic) → `LayerDefinition` con `ModelPart`s, animadas desde el render state.
  3. **Geometría compleja no representable en JSON** → loader OBJ propio que use Fabric Model Loading API (`UnbakedModelDeserializer`/`CustomUnbakedBlockStateModel`) y genere un mesh con la Renderer API **[Confirmado: módulos disponibles en 26.3]**. Requiere verificación: soporte de mesh de la Renderer API con Sodium 0.9 en 26.3 ([Hipótesis] Sodium implementa la Renderer API en versiones recientes; para 0.9 en 26.3, requiere verificación).
- Los blockstates `forge_marker` (170) se reescriben al formato vanilla (variants/multipart), preferiblemente generados con datagen.
- Las definiciones de modelo de ítem (`assets/<ns>/items/*.json`) se generan con datagen.

### 6.12 Wand/staff → Casters y foci

- **[JAR]** No existen wands ni staffs en 6.1.BETA26: hay *Caster's Gauntlets* (`ItemCaster`, `CasterManager`) y *foci* compuestos en el Focal Manipulator como un grafo de `FocusNode` (medium → effects → mods), con coste en vis, Focus Pouch y teclas para cambiar de foco.
- **[Recomendación]**:
  - El grafo de foco es un tipo de datos propio con un `Codec` (árbol de nodos con parámetros tipados) que se guarda en el componente `thaumcraft:focus`. Los tipos de nodo se registran en un registro propio `thaumcraft:focus_part`, para que los addons puedan añadir partes.
  - La ejecución se hace en el servidor: el *medium* crea proyectiles, nubes, toques o minas (entidades propias) y los *effects* se aplican al impactar.
  - El coste en vis sale del aura del chunk (`AuraAccess`) con descuentos por equipo (vis discount mediante atributos o un componente; decisión D11).
  - El cambio de foco usa una tecla (`KeyMappingHelper`/`keymapping.v1`), un payload al servidor y, en el cliente, una HUD radial como `Screen` u overlay.
  - "Wand/staff mechanics" en el sentido de TC4 queda **fuera de alcance** salvo decisión expresa.

### 6.13 Golems y seals

- **[JAR]** `EntityThaumcraftGolem` con propiedades combinadas en `GolemProperties` (material, head, arms, legs, addon) que derivan rasgos (`EnumGolemTrait`); seals colocados en bloques que generan *tasks* en un gestor global (`TaskHandler`) y que los golems reclaman; provisioning; Golem Builder (BE + GUI); golem bell.
- **[Recomendación]**:
  - Los registros propios `golem_material`, `golem_head`, `golem_arm`, `golem_leg` y `golem_addon` son **datos** (JSON con codec): estadísticas, rasgos, receta de coste y referencia de modelo y textura. Los rasgos son un registro estático en código.
  - La entidad guarda sus partes en un attachment o en `EntityDataAccessor` (sincronizado; los golems se renderizan según sus partes).
  - Seals: los datos de seal (posición, cara, configuración y filtro) se guardan en un attachment de nivel (`ServerLevel`) indexado por posición, en lugar del mapa global estático del original. El gestor de tareas también se guarda por nivel.
  - IA: goals que consultan el `TaskManager` de su nivel; pathfinding vanilla.
  - Render: `LivingEntityRenderer` compuesto por capas (`LayerDefinition` por tipo de parte), con texturas por material.

### 6.14 Taint

- **[JAR]** Bloques de taint (fibres, crust, soil, geyser, log, feature), semillas de taint (entidad), flux goo, propagación con `TaintHelper`, conversión de bioma a "Eerie"/taint mediante `Utils.setBiomeAt` (escribe el array de biomas del chunk) y efectos de flux.
- **[Recomendación]**:
  - La propagación usa random ticks de los bloques de taint y scheduled ticks para los frentes, con un límite por chunk y una opción de configuración para desactivarla (crítico para servidores).
  - Los "anclajes" de taint (semillas) son entidades. La influencia de taint por chunk se guarda en un attachment de chunk, si hace falta para gameplay (reducción de aura, spawns).
  - **El cambio de bioma en runtime no se usa como mecanismo principal**: vanilla almacena biomas en celdas 4×4×4 y la API pública para mutarlos en runtime requiere verificación (`/fillbiome` demuestra que es posible internamente). El efecto visual (color de hierba y follaje) se intenta con bloques propios y, opcionalmente, con mutación de bioma si se verifica una vía segura (posible Mixin/accessor, §8). Como Magical Forest es un bioma real (§6.16), la conversión tiene un destino válido (`magical_forest` o Eerie) si se verifica M2. Hasta entonces no se diseña gameplay que dependa de mutar biomas.

### 6.15 World generation

- **[JAR]** `ThaumcraftWorldGenerator implements IWorldGenerator`: menas (amber, cinnabar, quartz…), cristales de vis, árboles (greatwood, silverwood), plantas (shimmerleaf, cinderpearl, vishroom), mounds (2448 `setBlockState`), obeliscos o estructuras eldritch, tótems y biomas mágicos.
- **[Recomendación]**:
  - Menas, cristales, plantas y árboles: features data-driven en el **nuevo formato 26.3** (`worldgen/feature`, sin `config`, con placement modifiers renombrados) **[Confirmado: blog 26.3]**. Se insertan en biomas con `BiomeModifications.addFeature` (paquete `biome.v1` disponible).
  - Las features propias de Magical Forest (greatwood, silverwood, vishroom, flores…) se declaran **en el JSON del propio bioma**, no con `BiomeModifications` sobre bosques vanilla **[Decisión confirmada: §6.16]**. `BiomeModifications` se reserva para añadir contenido de Thaumcraft (menas, cristales, plantas sueltas) a biomas vanilla.
  - Los tipos de feature propios (cristales con orientación, silverwood, greatwood, nidos de taint) son `Feature`s registrados en `BuiltInRegistries.FEATURE_TYPE` **[Confirmado: blog 26.3]**, con trunk/foliage placers propios si los vanilla no bastan (registro de placers: requiere verificación).
  - **Mounds y estructuras grandes** se convierten a **plantillas de estructura** (`.nbt` vía `StructureTemplate`) mediante una herramienta de un solo uso: generarlas en 1.12.2 y exportarlas, o transcribir las llamadas `setBlockState` a un formato intermedio. Después se colocan con `Structure` + `StructureSet` + template pools en datos. Hay que confirmar la licencia y procedencia de las plantillas resultantes.
  - Se usa datagen para los JSON de worldgen siempre que sea posible (Fabric Docs anuncia ejemplos para 26.3).

### 6.16 Biomas

- **[JAR]** Magical Forest, Eerie (taint) y Eldritch (dimensión Outer Lands, no usada en BETA26 según la auditoría; a confirmar). En 1.12.2, Magical Forest es un bioma registrado propio (`BiomeGenMagicalForest extends Biome`), no una variante de un bioma vanilla. Se añade a la generación con `BiomeManager.addBiome` en `BiomeType.WARM` y `BiomeType.COOL`, con peso `ModConfig.biomeMagicalForestWeight = 5` (rango 0–100) y el interruptor `generateMagicForest = true` (`Registrar.java`). `BiomeProperties`: `0.2F`, `0.3F`, `0.8F`, `0.4F`, llamados con nombres SRG sin mapear; [Hipótesis] corresponden a altura base, variación de altura, temperatura y lluvia. En ese mismo bloque, Eerie y Outer Lands se registran pero **no** se añaden a `BiomeManager`.
- **[Decisión confirmada — no negociable] Magical Forest es un bioma real e independiente del Overworld:**
  - `ResourceKey<Biome>` propio (`<namespace>:magical_forest`) y definición data-driven propia (`worldgen/biome`: clima, efectos de entorno, spawns, features y carvers), generada con datagen.
  - Lo coloca en el mundo el *multi-noise biome source* del Overworld mediante su propio punto de parámetros climáticos. Su superficie se define con *material rules* propias, que en 26.3 se pueden registrar por datos **[Confirmado: blog 26.3]**.
  - Los atributos de entorno (cielo, niebla, color de hierba y follaje) se definen en el bioma y, cuando aplique, con `EnvironmentAttributes` (26.1).
  - **Descartado:** representar Magical Forest como *feature*, región de vegetación, capa o modificación visual de un bioma vanilla, y usar `BiomeModifications` sobre bosques vanilla como sustituto. Era la antigua opción (c) de este documento y queda **eliminada**.
- **Restricción de plataforma:** Fabric API ofrece `NetherBiomes` y `TheEndBiomes`, pero **no** una API para insertar biomas en el Overworld **[Confirmado: contenido de `biome.v1`]**. Hace falta un mecanismo adicional, que se oculta tras la fachada `BiomePlacementAccess` (en `core`). El contenido declara una sola vez el bioma, sus parámetros climáticos y su peso, y el adaptador de `compat` los traduce a la librería elegida.
- **Investigación de soluciones mantenidas para 26.3 [Confirmado: Modrinth API + inspección de los jars publicados, 2026-10-02]:**

  | | TerraBlender | Biolith |
  |---|---|---|
  | Build Fabric 26.3 | `26.3.0.0.9` (**beta**, 02-10-2026); varias builds 26.3 publicadas entre el 23-09 y el 02-10-2026 | `3.8.0-beta.1` (**beta**, 23-09-2026) |
  | Licencia | LGPL-3.0 | LGPL-3.0 |
  | Dependencias | Fabric API; `minecraft: 26.3`; `java >=25` | Fabric API; Loader `>=0.19.5`; MixinExtras `>=0.5.5`; `minecraft >=26.3 <26.4` |
  | Modelo | **Regiones** por mod con peso: cada mod define sus biomas en su propia región del espacio climático | Inserción directa en un punto de ruido del bioma vanilla, reemplazos y sub-biomas por criterios |
  | API observada en el jar 26.3 | `Region(Identifier, RegionType, int)`, `Region#addBiomes(Registry<Biome>, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>>)`, `addModifiedVanillaOverworldBiomes`, `Regions.register(...)`, `RegionType.OVERWORLD`, `MaterialRuleManager`, regiones por datos (`terrablender.api.data`) | `BiomePlacement.addOverworld(ResourceKey<Biome>, Climate.ParameterPoint)`, `replaceOverworld(..., double)`, `addSubOverworld(..., Criterion)`, `api.surface.*`; colocación también por datapack (README) |
  | Punto de entrada | Entrypoint `terrablender` (`TerraBlenderApi#onTerraBlenderInitialized`, según su wiki) | Llamadas estáticas durante la inicialización (momento exacto: requiere verificación) |
  | Madurez | La más adoptada (≈40 M descargas en Modrinth); el wiki documenta el modelo de regiones | El README advierte "Somewhat Experimental": las estrategias de selección pueden cambiar y **mover biomas en mundos existentes** |
  | Compatibilidad entre ellas | — | El README declara compatibilidad total con TerraBlender y con biomas de Fabric Biome API |

- **[Recomendación] Plan A: TerraBlender**, porque es la más adoptada, ya tiene varias builds 26.3 y sus regiones evitan competir por el espacio climático con otros mods de biomas. **Plan B: Biolith**, si TerraBlender no da el control de distribución necesario o deja de actualizarse; la fachada hace el cambio local. La elección final queda en D6. **Requiere verificación** antes de implementar:
  - Estabilidad de la API en las builds beta de 26.3 y si la API pública de ambas se mantiene entre parches `26.3.x`.
  - Parámetros climáticos y peso de región para Magical Forest. La referencia es el peso 5 en `WARM`/`COOL` de 1.12.2, pero el sistema de pesos de `BiomeManager` no tiene equivalente directo en multi-noise, así que la frecuencia se calibra empíricamente. El peso pasa a ser una opción de configuración, igual que en el original.
  - Cómo interactúan las *material rules* de la librería (`MaterialRuleManager`, `api.surface`) con las *material rules* por datos de 26.3.
  - Que la distribución sea determinista con una semilla fija entre versiones de la librería (test de semilla fija en CI).
- **Plan C, si ninguna librería mantenida sirve (último recurso):**
  - (C1) **Mixin propio** (M1) sobre la construcción de los parámetros del Overworld (`OverworldBiomeBuilder` o la lista de parámetros del `MultiNoiseBiomeSource`; nombres y puntos de inyección en 26.3: requiere verificación). Es frágil entre drops y puede chocar con TerraBlender o Biolith si el jugador los tiene instalados por otros mods.
  - (C2) **Sólo datos:** sobrescribir la definición de la dimensión del Overworld (o su lista de parámetros multi-noise) con un datapack que incluya Magical Forest. No necesita código, pero reemplaza la lista completa de biomas, es incompatible con otros mods de biomas y con *world presets* propios, y hay que rehacerla en cada drop. Viabilidad exacta en 26.3: requiere verificación. Sólo sirve como prototipo o *fallback* documentado.
- **Eerie** (taint) usa el mismo mecanismo si se decide que se genere de forma natural, y no sólo por conversión de taint (D6). **Eldritch** pertenece a una dimensión propia y queda fuera de esta decisión.
- Cualquier efecto de bioma en runtime (taint, conversión hacia Magical Forest) depende de M2 (§6.14) y sigue en "requiere verificación".

### 6.17 Recetas

- **[JAR]** Arcanas (con coste de vis y cristales, *shaped* y *shapeless*), crisol (ítem + aspectos → ítem), infusión (centro + componentes + aspectos + inestabilidad), multibloque (transformación de estructuras: Infernal Furnace, Infusion Altar, Thaumatorium, Golem Press…), smelting bonus, fake recipes del Thaumonomicon y loot bags.
- **[Recomendación]**:
  - `RecipeType`s propios: `arcane_crafting`, `crucible`, `infusion` y `infusion_enchantment`. Los serializers son `RecipeSerializer(MapCodec, StreamCodec)` **[Confirmado: blog 26.1]**. Las recetas van en `data/<ns>/recipe/` y desde 26.3 se cargan como registros recargables **[Confirmado: blog 26.3]**.
  - Cada receta tiene un campo `research` (clave). La comprobación se hace en el servidor contra el conocimiento del jugador.
  - **Multibloques:** tipo de dato propio (`multiblock_transform`): patrón 3D, bloque disparador, resultado y requisitos de conocimiento. Se carga por reload listener y lo usa la Salis Mundus. No es un `Recipe` vanilla.
  - Smelting bonus: datos propios (un mapa de ítem o tag a bonus) aplicados por evento de horno. No hay evento de horno en Fabric API: probable **Mixin** (§8) o alternativa (D12).
  - Sincronización al cliente para el Thaumonomicon y visores: `recipe.v1.sync` **[Confirmado: el paquete existe]**; semántica concreta: requiere verificación.
  - Las recetas vanilla (crafteo de bloques decorativos) se generan con datagen.

### 6.18 Research / Thaumonomicon

- **[JAR]** 7 categorías, ~136 entradas, 12 scans; stages con requisitos (ítems, crafteos, knowledge observación/teoría, research previo), addenda, warp, recompensas; Research Table con *theorycrafting* (cartas) en 6.1.
- **[Recomendación]**:
  - Esquema JSON nuevo con codec: `category`, `entry` (`stages[]`, `addenda[]`, `parents`, `siblings`, `location`, `icons`, `rewards`, `warp`) y `scan` (predicados de ítem/bloque/entidad). Se convierten una sola vez desde los JSON originales con un script (no se copian tal cual: cambian los ids y los formatos de ítem).
  - Se cargan con un **reload listener del servidor** o con un **registro recargable** (`DynamicRegistries.registerReloadable`, 26.3) y se sincronizan al cliente. Decidir cuál (D4): el registro recargable da sincronización e integración con tags, pero su API es nueva (requiere verificación).
  - El conocimiento del jugador (`PlayerKnowledge`: research completado y stage, puntos de knowledge por categoría y tipo, flags) se guarda en un attachment persistente, `copyOnDeath`, sincronizado sólo al propio jugador.
  - Warp (`PlayerWarp`: permanent/normal/temporary) se guarda en otro attachment, con eventos de warp en tick del servidor.
  - Thaumonomicon: `Screen` de cliente con mapa desplazable, nodos e iconos, páginas de texto con formato propio y páginas de receta. Es mucho trabajo de UI, pero sin riesgo de API: sólo dibujo 2D con `GuiGraphicsExtractor`.
  - Theorycrafting: las cartas son un registro propio (código + datos); la sesión vive en el BE de la Research Table, que es autoritativo en el servidor.
  - El texto largo de las entradas va en archivos de traducción (claves por stage), como en el original.

### 6.19 Configuración

- **[JAR]** `ModConfig` con Forge `@Config`: worldgen, aura, taint, gráficos, misc. Algunos valores afectan al servidor y otros sólo al cliente.
- **[Recomendación]**:
  - Config propia sin dependencia: dos archivos (`thaumcraft-common.json`/`toml` y `thaumcraft-client.json`) definidos con **Codecs** y valores por defecto, con comentarios si el formato los permite (decisión de formato: D13). Se recargan al iniciar el servidor; los valores de gameplay se sincronizan al cliente por payload.
  - Lo que afecta a worldgen se expresa preferiblemente como **datos** (datapack sobrescribible), no como config, porque los features ya son data-driven.
  - Pantalla de configuración opcional con Mod Menu + Cloth Config en un módulo `compat`. Fabric API no ofrece una API de configuración **[Confirmado: no aparece en los paquetes de Fabric API 26.3]**.

### 6.20 Datagen

- **[Confirmado]** Fabric Data Generation: `DataGeneratorEntrypoint`, `FabricDataGenerator` y providers para recetas, loot, tags, avances y modelos de cliente (`client.datagen.v1`).
- **[Recomendación]** Datagen es la **fuente de verdad** para:
  - Blockstates y modelos de bloque e ítem, y definiciones de ítem.
  - Loot tables de bloques.
  - Tags (incluidos los `c:` que sustituyen al Ore Dictionary).
  - Recetas vanilla y, con providers propios, las recetas de Thaumcraft.
  - Features y estructuras cuando haya soporte (worldgen 26.3: ejemplos "shortly after release").
  - `en_us.json` de nombres de contenido (con los textos largos aparte).
  - Mapeos de aspectos (provider propio).
- Los archivos generados se commitean en `src/main/generated` (convención de Loom: requiere verificación) y CI comprueba que datagen no produce diferencias.

### 6.21 Assets, sonidos y traducciones

- **[JAR]** 767 PNG, 511 JSON, 111 OGG, 81 `.mcmeta`, 27 OBJ, 9 `.lang`, shaders GLSL. Son propiedad de Azanor.
- **[Recomendación]**:
  - **Bloqueante legal:** antes de redistribuir cualquier asset original hace falta una decisión explícita (D1): obtener permiso, recrear los assets o exigir que el usuario los aporte desde su JAR (por ejemplo, un *resource pack* que extraiga localmente). Mientras tanto, el desarrollo puede usar los assets originales **sólo en local** sin commitearlos, o placeholders.
  - Texturas: los PNG y `.mcmeta` de animación son compatibles en concepto; la estructura de carpetas cambia (`textures/block`, `textures/item`, atlas de partículas, GUI sprites en `textures/gui/sprites` desde 1.20.2). Hay que revisar las animaciones `.mcmeta` (formato 26.3: requiere verificación).
  - Sonidos: los OGG se reutilizan si la licencia lo permite; `sounds.json` se reescribe con los nuevos ids. Hay que registrar 65 `SoundEvent`s.
  - Traducciones: conversión `.lang` → `.json` con un script; las claves cambian según la tabla de IDs. Se mantienen todas las locales disponibles (9).
  - Formato de resource pack 97.1 y de data pack 121.0 en RC/final **[Confirmado: fuentes de terceros sobre la release; requiere verificación con el `pack.mcmeta` que genere Loom]**.

## 7. Rendering e Iris

### 7.1 Inventario de rendering del JAR **[JAR]**

| Elemento | Cantidad / ejemplo | Técnica 1.12.2 | Riesgo |
|---|---|---|---|
| TESR | 22 (jar, crucible, infusion matrix, pedestal, alembic\*, thaumatorium\*, golem builder\*, bellows\*, mirror, hole, void siphon, banner…) | `GlStateManager`, `Tessellator`, `bindTexture`, matrices GL | Alto. \* = **no descompilado** (`renderTileEntityAt`/`render`). |
| Entity renderers | ~30, ~20 `ModelBase` | `ModelRenderer`, GL directo | Medio. |
| OpenGL directo | 143 clases con `GlStateManager`/GL11; 55 con `Tessellator`/`BufferBuilder` | Estado global GL, `glDepthMask` (34 clases), blending aditivo `(770, 1)` (~20 clases) | **Crítico**: no existe en 26.3. |
| Shaders ARB propios | `ShaderHelper`: `ender.vert/frag`, `sketch.vert/frag` | `ARBShaderObjects.glCreateProgramObjectARB`, uniform `time` | **Crítico** con Iris y Vulkan. |
| Post-procesado | `ShaderHandler`: `desaturatetc`, `blurtc`, `hunger` y `sunscorned` en `assets/minecraft/shaders/post` (efectos de Death Gaze, Blurred Vision, Unnatural Hunger y Sun Scorned), con un `ShaderGroup` cada uno | `ShaderGroup` vanilla 1.12.2 | Alto: con Iris activo, conflicto con el pipeline del shader pack. |
| Framebuffers propios | **No encontrados** (búsqueda de `Framebuffer`/`glGenFramebuffers`: 0 resultados) | — | Bajo. |
| Stencil | **No encontrado** | — | — |
| GLE/LWJGL | Tubos, streams y rayos | Llamadas GL de geometría | Crítico. |
| Overlays HUD | `RenderGameOverlayEvent` (vis del caster, aura, warp vignette) | GL 2D | Bajo–medio. |
| `RenderWorldLastEvent` | Highlight de blocks, goggles (revelado de aspectos), líneas de golems/seals | GL inmediato | Medio. |
| Partículas | `ParticleEngine` propio, 24 FX | Capas GL propias, blending aditivo | Alto. |

### 7.2 Pipeline moderno y mapeo **[Confirmado salvo indicación]**

- **Modelo extract/submit:** desde 1.21.6 el render se divide en extracción (datos → `RenderState`) y envío (submits). Los BER y los renderers de entidad implementan `extractRenderState` + `submit(…, SubmitNodeCollector, …)`. **No se puede leer el mundo durante el submit.**
- **Blaze3D:** es la única capa de render permitida. Desde 26.2 coexisten los backends OpenGL y Vulkan (experimental); el GL crudo no está soportado.
- **Capas de chunk automáticas** (26.1): `ChunkSectionLayer` se asigna por quad según la transparencia del sprite (sólido, cutout o translúcido). Las texturas translúcidas de bloques (jars, cristales, vidrios arcanos) **deben** tener píxeles translúcidos reales para caer en la capa correcta. Se puede forzar desde el modelo o desde `MutableQuadView`.
- **OIT** (26.3): la opción "Improved Transparency" pasa a ser *order-independent transparency*. Afecta a cómo se componen las superficies translúcidas. Hay que probar jars con essentia, cristales, beams translúcidos y partículas con OIT activado y desactivado. La interacción con Iris (que sustituye el pipeline) requiere verificación.
- **Hooks de mundo:** `LevelRenderEvents` (`StartMain`, `AfterOpaqueTerrain`, `AfterSolidFeatures`, `BeforeTranslucentTerrain`, `AfterTranslucentTerrain`, `AfterTranslucentFeatures`, `CollectSubmits`, `BeforeBlockOutline`, `BeforeGizmos`, `EndMain`) y `LevelExtractionEvents`. Sustituyen a `RenderWorldLastEvent`.
- **HUD:** `HudElementRegistry` (`VanillaHudElements` para el orden).
- **Pipelines propios:** existe `FabricRenderPipeline` (con `Builder` y `Snippet`). Crear un pipeline propio implica shaders propios (o reutilizar los vanilla con otro estado de blending o depth). Cómo trata Iris un `RenderPipeline` de mod requiere verificación.

| Necesidad TC | Mecanismo recomendado | Compatibilidad Iris |
|---|---|---|
| Bloques con modelo fijo (pedestales, tubos, crucible) | Modelo JSON de chunk | **Alta** (geometría de terreno normal). |
| Partes animadas (bellows, infusion matrix, golem builder) | BER con `ModelPart` y `RenderType` vanilla de entidad | Alta. |
| Contenido de jar (líquido de color) | Quads en el BER con `RenderType` translúcido vanilla y tinte | Media–alta; revisar con OIT. |
| Beams, streams y rayos (casters, bore, essentia, zaps) | Quads en CPU con `RenderType` vanilla translúcido/emisivo (tipo `beacon beam`/`lightning`, según disponibilidad: requiere verificación) | Media: los shader packs suelen tratar bien los tipos vanilla, pero no los aditivos propios. |
| Blending aditivo (wisps, partículas de vis, halos) | Preferir un tipo vanilla con blending aditivo si existe; si no, `FabricRenderPipeline` propio con **alternativa** translúcida normal | Baja con pipeline propio, alta con la alternativa. |
| Efecto "ender/void" (hole, void siphon, rift) | Reutilizar el `RenderType` del portal del End vanilla (requiere verificación del nombre) en lugar de `ender.frag` | Alta: los shader packs suelen soportar el portal del End. |
| `sketch` shader | Rediseño: textura animada o efecto de GUI | Alta. |
| Post-efectos de pociones (desaturate, blur, hunger, sunscorned) | Post-efecto vanilla si hay una API de mod (26.3 tiene `/posteffect`; API: requiere verificación). **Alternativa obligatoria**: overlay HUD (viñeta, tinte, desenfoque simulado) cuando se detecta un shader pack activo o si la API no existe | Los post chains propios probablemente no se apliquen o choquen con Iris (requiere verificación); el overlay HUD es seguro. |
| Revelado con goggles (aspectos sobre bloques) | `LevelRenderEvents.AfterTranslucentFeatures`/`CollectSubmits` + texto o sprites en el mundo | Alta. |
| Líneas de seals y áreas de golem | Submits de líneas (`RenderType` de líneas vanilla) o `BeforeGizmos` | Alta. |
| Flux Rift (malla deformada animada) | Geometría generada en CPU por la entidad, `RenderType` vanilla translúcido o portal del End | Media. |
| HUD de vis, aura y warp | `HudElementRegistry` | No afecta (la HUD no pasa por el shader pack). |

### 7.3 Política de compatibilidad con Iris **[Decisión confirmada: Iris/Sodium como objetivos de compatibilidad, no dependencias; resto Recomendación]**

1. **Ningún Mixin sobre clases de Iris o Sodium.**
2. La ruta principal usa **sólo** `RenderType`s y pipelines vanilla. Iris advierte de que los shaders añadidos por mods se ignoran con un shader pack activo y recomienda **rutas alternativas sin shaders propios** **[Confirmado: Iris, `docs/development/compatibility/core-shaders.md`]**.
3. Detectar un shader pack activo sólo para elegir alternativas (post-efectos → HUD; pipeline aditivo → translúcido). Se hace en `compat/iris-detect`, que **no compila contra Iris**: primero `FabricLoader#isModLoaded("iris")` y, si está, una consulta por reflexión aislada a la API pública de Iris (nombre y estabilidad de `IrisApi` en 1.11.x para 26.3: requiere verificación). Sin Iris instalado, la detección devuelve "no". Si Iris está instalado pero la reflexión falla, se asume un shader pack activo y se usan las rutas seguras [Recomendación].
4. Probar con Sodium solo, Sodium + Iris sin pack, Sodium + Iris con 2–3 packs populares, OIT on/off y, cuando esté disponible para jugadores, con el backend Vulkan.
5. Los efectos que no se puedan reproducir de forma segura se **degradan** (documentado por efecto). No se busca la paridad visual exacta con 1.12.2 a costa de la compatibilidad.

### 7.4 Renderers no descompilados

`TileAlembicRenderer`, `TileThaumatoriumRenderer`, `TileGolemBuilderRenderer` y `TileBellowsRenderer` **[JAR]** no se pudieron descompilar. Como su reimplementación va a ser nueva de todos modos, el riesgo se limita a **reproducir el aspecto visual**. Se usarán capturas o vídeo del juego 1.12.2 como referencia (D10). Lo mismo con los 5 `getBoundingBox`/`getCollisionBoundingBox` (`func_185496_a`) no descompilados: se reconstruyen con los modelos y la observación del juego.

## 8. Mixins potencialmente necesarios

Ninguno se implementa ahora. Cada uno se marca con su prioridad: **evitar** (hay alternativa), **probable** o **último recurso**.

| # | Objetivo (clase/punto, a verificar en 26.3) | Motivo | ¿Lo cubre Fabric API? | Riesgo | Prioridad |
|---|---|---|---|---|---|
| M1 | Construcción de los parámetros de biomas del Overworld (`OverworldBiomeBuilder` / lista de parámetros del `MultiNoiseBiomeSource`; nombres en 26.3: requiere verificación) | Insertar Magical Forest (bioma real, **[Decisión confirmada]**) y Eerie si se decide | No (sólo Nether/End) | Alto: frágil entre drops y choca con TerraBlender o Biolith | **Plan C** (§6.16): sólo si ninguna librería mantenida sirve. |
| M2 | Mutación de biomas en runtime (contenedor de biomas del chunk) + resincronización | Taint y conversiones de bioma (hacia Magical Forest o Eerie) | No (requiere verificación) | Medio | Evitar en la v1; reevaluar cuando Magical Forest exista como bioma. |
| M3 | Hook en `AbstractFurnaceBlockEntity` al completar la fundición | Smelting bonus (pepitas extra) | No (no hay evento de horno, requiere verificación) | Bajo–medio | Probable; o rediseñar el bonus como mecánica del Infernal Furnace propio (D12). |
| M4 | `InventoryScreen`/`InventoryMenu` | Slots de accesorios propios | No | Alto | **Evitar**: se usa Trinkets Updated. |
| M5 | `LivingEntity` daño/curación/muerte | Runic Shielding, Charm of Undying, warp | **Sí en su mayor parte**: `ServerLivingEntityEvents.ALLOW_DAMAGE`/`AFTER_DAMAGE`/`ALLOW_DEATH` | — | Evitar. La absorción tipo escudo rúnico podría necesitar un hook en el cálculo de daño (requiere verificación). |
| M6 | Interacción con ítems/bloques (Salis Mundus sobre bloques vanilla) | Transformaciones de multibloque | **Sí**: `UseBlockCallback` y `BlockEvents.USE_ITEM_ON` (26.1) | — | Evitar. |
| M7 | Lógica de "ver" o renderizado condicional (goggles revelan bloques u ores) | Revelado | Parcial: `LevelRenderEvents` | Bajo | Evitar. |
| M8 | Post-efecto de pantalla (si no hay API de mod para post chains) | Efectos de pociones TC | No confirmado | Medio con Iris | Último recurso; preferir el overlay HUD. |
| M9 | Acceso a campos privados (accessor/invoker) | Lo que quede del antiguo `tc_at.cfg` (24 entradas) | Class tweaker/access widener | Bajo | Probable, en accessors puntuales. |
| M10 | Enchanting/encantamientos de infusión que modifican la minería (`Refining`, `Destructive`, `Lamplight`…) | Infusion enchantments | Parcial: `EnchantmentEvents`; vanilla tiene enchantments data-driven con *effect components* | Medio | Preferir enchantments data-driven vanilla con efectos propios (D14). |
| M11 | Sincronización de BEs o entidades | — | Sí (paquete de actualización del BE, `EntityDataAccessor`, attachments) | — | Evitar. |
| M12 | `RecipeManager` interno | Recetas condicionadas por research en el crafteo vanilla | Parcial (`FabricRecipeManager`, recetas propias) | Medio | Evitar con tipos de receta propios. |
| M13 | Clases de Iris/Sodium | Compatibilidad | — | Muy alto | **Prohibido** salvo verificación y acuerdo explícito. |
| M14 | Visión nocturna/rayos de los goggles, FOV del caster | Efectos de cámara | Parcial (requiere verificación) | Bajo | Evaluar en su momento. |

## 9. Riesgos y problemas de compatibilidad

| # | Riesgo | Impacto | Mitigación |
|---|---|---|---|
| R1 | **Licencia** del código y los assets (Azanor) y del nombre "Thaumcraft" | Bloquea la distribución | D1 antes de cualquier release; reimplementación limpia del código; los assets se resuelven aparte. |
| R2 | Volatilidad de las APIs de Minecraft (cada drop trimestral cambia render, componentes, worldgen y registros, como 26.1→26.2→26.3) | Retrabajo continuo | Fachadas internas, datos en lugar de código, pocos Mixins, CI que compila contra la versión fijada. |
| R3 | Rendering: no hay GL directo, Vulkan está en camino, OIT, Iris | Efectos visuales degradados o rotos | Política de §7; rutas alternativas; pruebas en una matriz de configuraciones. |
| R4 | Rendimiento del aura y la essentia en el hilo del servidor (antes en un hilo propio) | Lag en servidores | Presupuesto por tick, round-robin, grafos cacheados, métricas de perfilado desde el principio. |
| R5 | Propagación de taint | Daño a mundos y servidores | Límites, desactivación por config, pruebas con gametests. |
| R6 | Algoritmo de aspectos derivados distinto | Valores de aspectos distintos al original | Documentarlo; permitir overrides por datos. |
| R7 | Reconstrucción de comportamiento no descompilado (10 métodos) | Divergencias de gameplay | Observación en 1.12.2 (D10). |
| R8 | Trinkets Updated deja de mantenerse | Accesorios rotos | Fachada `AccessoryAccess` + plan B de slots propios. |
| R9 | La inserción de Magical Forest en el Overworld depende de una librería con builds 26.3 en beta o, si no, de Mixins | Retraso del worldgen; cambios de distribución de biomas entre versiones | Fachada `BiomePlacementAccess`; TerraBlender (plan A), Biolith (plan B), Mixin M1 o datapack (plan C); versiones fijadas; test de semilla fija. |
| R10 | Formatos de datos que cambian entre drops (datapack 121.0 en 26.3; features renombradas) | JSON inválidos | Datagen como fuente de verdad; regenerar en cada drop. |
| R11 | Compatibilidad de saves 1.12.2 → 26.3 | Mundos antiguos inservibles | **Fuera de alcance**: no se soporta la migración de mundos 1.12.2 (Minecraft tampoco lo garantiza para mods). |
| R12 | Escala de la UI del Thaumonomicon y de la Research Table | Mucho esfuerzo de UI | Construirla tarde, sobre los datos de investigación ya estables. |
| R13 | Multiplayer: confianza en el cliente en las acciones de research y foci | Exploits | Validación en el servidor de todos los payloads serverbound. |
| R14 | Dependencias de visor de recetas cambiantes | Integración rota | Módulo `compat` opcional, `compileOnly`. |

## 10. Decisiones que requieren investigación

| ID | Decisión | Qué verificar | Bloquea |
|---|---|---|---|
| D1 | Mod id, nombre y estrategia de assets | Permiso/licencia de Azanor; `thaumcraft` frente a un namespace nuevo; assets originales, recreados o aportados por el usuario | Todo el registro y los assets. |
| D2 | Estructura Gradle (un proyecto con source sets divididos frente a multi-proyecto) | Opción exacta de Loom 1.17 para split source sets; jar-in-jar para `compat` | Bootstrap. |
| D3 | API exacta de registros propios (`FabricRegistryBuilder`) y sincronización de registros propios | Javadoc de `event.registry` en `0.161.0+26.3` | Aspectos, foci, golems. |
| D4 | Research como reload listener o como registro recargable (`DynamicRegistries.registerReloadable`) | Semántica, sync y tags del registro recargable en 26.3 | Investigación y recetas. |
| D5 | Serialización de BEs (`ValueInput`/`ValueOutput`) y componentes en BEs en 26.3 | Código de Minecraft 26.3 | Todos los BEs. |
| D6 | **Mecanismo** de inserción de Magical Forest (que sea un bioma real ya está decidido) y si Eerie se genera de forma natural | Confirmar TerraBlender (plan A) frente a Biolith (plan B) con un prototipo; parámetros climáticos y peso; estabilidad de las betas 26.3; calibrar la frecuencia frente al peso 5 de 1.12.2 | Worldgen, aura base por bioma y taint. |
| D7 | Nombres de grupos y slots de Trinkets Updated 4.2 y su API de render | Wiki o código de Trinkets Updated para 26.3 | Accesorios. |
| D8 | Exponer la essentia también como `Storage<EssentiaVariant>` (Transfer API) | Diseño y coste | Essentia. |
| D9 | Visor de recetas soportado (EMI, REI o JEI) | Builds para 26.3 | `compat`. |
| D10 | Fuente de referencia para comportamiento no descompilado | Instancia 1.12.2 jugable para capturas y mediciones | Renderers y entidades afectadas. |
| D11 | Descuento de vis: atributo propio o componente | API de atributos en 26.3 | Casters y accesorios. |
| D12 | Smelting bonus: Mixin de horno o mecánica propia | ¿Existe hook de Fabric? | Recetas. |
| D13 | Formato de config (JSON con codec o TOML) | Comentarios y librerías | Config. |
| D14 | Encantamientos de infusión: enchantments data-driven vanilla o sistema propio | *Effect components* de 26.3 | Infusión. |
| D15 | API de post-efectos para mods y comportamiento con Iris | Código 26.3, `/posteffect`, prueba con Iris | Efectos de pociones. |
| D16 | Tipos de render vanilla aditivos, de beam o de portal reutilizables | `RenderTypes` de 26.3 | FX. |
| D17 | Soporte de la Renderer API (mesh) en Sodium 0.9 y con Iris | Pruebas | OBJ complejos. |
| D18 | Momento y garantías de `ServerChunkEvents.Generate` para inicializar el aura | Javadoc y prueba | Aura. |

## 11. Orden recomendado de implementación

Los sistemas de las fases 0–2 son **fundaciones**: cambiarlos después obliga a rehacer contenido. Por eso van primero aunque no aporten gameplay visible.

| Fase | Contenido | Por qué en este orden | Criterio de salida |
|---|---|---|---|
| **0. Decisiones y bootstrap** | D1, D2; proyecto Fabric 26.3 (Loom 1.17, Java 25), `fabric.mod.json`, entrypoints, CI (build + datagen sin diffs + gametest vacío), licencia del repo | Sin namespace ni build fijado, todo lo demás se rehace | El mod vacío carga en cliente y servidor dedicado; CI en verde. |
| **1. Núcleo técnico** | Helpers de registro, clase de ids, tabla de mapeo de IDs (documento), codecs base, config (D13), infraestructura de payloads, attachments, reload listeners, datagen base (lang, modelos, loot, tags) | Todo el contenido depende de esto | Un bloque y un ítem de prueba generados por datagen. |
| **2. Modelo de datos de dominio** | Registro de aspectos + `AspectList` + mapeo por datos + derivación; componentes de ítem (§6.4); fachada `AccessoryAccess` + adaptador Trinkets; fachada `BiomePlacementAccess` + clave y definición mínima del bioma Magical Forest insertado con la librería elegida (D6); `AuraAccess` + attachment de chunk (sin simulación completa); `PlayerKnowledge`/`PlayerWarp` | Recetas, research, essentia, casters y golems leen estos tipos; cambiarlos después cascadea | Comando de debug que muestra los aspectos de un ítem y el aura del chunk; datos sincronizados. |
| **3. Aura completa** | Simulación (difusión, regeneración, fase lunar, flux), Flux Rift básico, HUD de goggles/thaumometer, crystals y worldgen de cristales | Es la base del vis para casting y crafting | Valores estables y perfilados en un servidor de prueba. |
| **4. Research (datos + lógica)** | Esquema, conversión de JSON, scans, progreso y payloads; el Thaumonomicon de momento sólo en modo debug (lista simple) | Las recetas se condicionan por research | Completar investigación por comandos y por escaneo. |
| **5. Crafting base** | Arcane Workbench (menú), recetas arcanas, Salis Mundus + multibloques, Crucible + recetas, bloques y materiales básicos (amber, cinnabar, quicksilver, thaumium) | Desbloquea la progresión inicial | Progresión "Basics → Alchemy" jugable sin libro gráfico. |
| **6. Essentia** | API, jars, tubes, valves, filters, buffers, alembic, smelter, centrifuge, mirrors | Necesaria para infusión y golems de essentia | La red mueve essentia según las reglas de succión. |
| **7. Infusión y artifice** | Infusion matrix, pedestales, estabilidad, recetas de infusión y de encantamiento, dispositivos de artifice | Depende de la essentia y del research | Las infusiones del árbol funcionan. |
| **8. Casting** | Casters, foci (grafo), Focal Manipulator, pouch, teclas, HUD | Depende del aura, del research y de la infusión (algunos foci) | Foci combinables con coste de vis. |
| **9. Golemancy** | Partes por datos, Golem Builder, entidad, seals, task manager, render compuesto | Sistema propio grande, depende de casi todo | Golems básicos (fill/empty/harvest/guard). |
| **10. Mundo** | Worldgen completo (árboles, plantas, mounds y estructuras como plantillas), mobs (Pech, Wisps, cultistas), Magical Forest completo (features, spawns, superficie y atributos de entorno), Eerie según D6, loot | Depende de los bloques y las entidades ya existentes | Generación estable en un mundo nuevo. |
| **11. Taint y Eldritch** | Taint (propagación con límites), criaturas de taint, warp events, bosses, contenido eldritch | Es el contenido de final de juego; necesita los sistemas anteriores | Jugable con límites de servidor. |
| **12. Thaumonomicon gráfico y pulido visual** | Libro completo, Research Table con theorycrafting, FX completos, alternativas para Iris, matriz de pruebas de shaders | Depende de los datos de research estables y de todo el contenido | Matriz Iris/Sodium/OIT verificada. |
| **13. Integraciones** | Visor de recetas, Mod Menu/Cloth, API pública documentada para addons | Requiere APIs estables | Módulos `compat` opcionales. |

Cosas que **deben** hacerse pronto para evitar retrabajo:

1. Namespace e IDs (D1) y la tabla de mapeo 1.12.2 → 26.3.
2. Codecs y componentes de ítem: todo el contenido los usa.
3. Registro de aspectos y `AspectList`: recetas, essentia, research, scans y JEI/EMI dependen de ellos.
4. Attachments (aura de chunk y conocimiento del jugador) y su política de sincronización.
5. La fachada de accesorios (aunque se use Trinkets).
6. Datagen como fuente de verdad (evita miles de JSON escritos a mano que luego cambian en cada drop).
7. Política de rendering (§7.3), fijada **antes** del primer BER, para no escribir renderers que luego haya que reescribir para Iris.
8. La clave del bioma Magical Forest y la fachada `BiomePlacementAccess`: el aura base por bioma y el worldgen dependen del bioma, e insertarlo tarde cambia la distribución de los mundos ya generados.

## 12. Fuera de alcance de esta etapa

- Cualquier código funcional, Mixin, build Gradle o asset.
- Migración de mundos 1.12.2.
- Mecánicas de TC4 (nodos de aura, wands/staffs) que no están en 6.1.BETA26.
- Soporte de NeoForge, aunque Trinkets Updated e Iris sí lo tienen: el stack fijado es Fabric.
- Implementación del bioma Magical Forest: en esta etapa sólo se fija la decisión y sus alternativas técnicas.

## 13. Fuentes consultadas

- Fabric: "Fabric for Minecraft 26.1" (2026-03-14), "26.2" (2026-06-15) y "26.3" (2026-09-15), en `fabricmc.net`.
- Fabric Docs: Data Attachments, Networking, Basic Rendering Concepts, Block Entity Renderers, Creating Custom Particles (`docs.fabricmc.net`).
- Javadoc de Fabric API `0.160.4+26.3` (`maven.fabricmc.net/docs`) y metadata de Maven (`0.161.0+26.3`).
- Modrinth API: versiones 26.3 de Trinkets Updated, Accessories, Sodium, Iris, Cloth Config, TerraBlender y Biolith (consultado el 2026-10-02).
- TerraBlender: wiki "Getting started" (`github.com/Glitchfiend/TerraBlender`) y firmas de `terrablender.api.*` del jar `26.3.0.0.9`, obtenidas con `javap`.
- Biolith: README y wiki (`github.com/TerraformersMC/Biolith`) y firmas de `BiomePlacement` del jar `3.8.0-beta.1`, obtenidas con `javap`.
- Iris: `docs/development/compatibility/core-shaders.md`.
- Minecraft 26.3 Snapshot 2 (OIT), en `minecraft.net`.
- Baubles: README de `github.com/Azanor/Baubles` (licencia).
- Proyectos comunitarios: sólo consultados como referencia de existencia y versión. No se ha copiado código.
