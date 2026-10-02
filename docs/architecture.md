# Arquitectura de Thaumcraft 6 (1.12.2) — Auditoría del JAR de referencia

> Fuente: `Reference/Thaumcraft-1.12.2-6.1.BETA26 (1).jar` (Thaumcraft `6.1.BETA26`, autor Azanor).
> Descompilado con Vineflower 1.10.1 **fuera del repositorio** (el código descompilado no se versiona).
> Este documento describe la estructura original; no propone implementación.

## 1. Identidad del mod

| Campo | Valor |
|---|---|
| modid | `thaumcraft` |
| Versión | `6.1.BETA26` (changelog incluye "hotfix1") |
| Minecraft | `1.12.2` (`acceptedMinecraftVersions = "[1.12.2]"`) |
| Forge requerido | `required-after:forge@[14.23.5.2768,)` |
| Dependencia dura | `required-after:baubles@[1.5.2,)` |
| Bytecode | Java 8 (class major version 52 en las 1120 clases) |
| Access Transformer | `META-INF/tc_at.cfg` (declarado en `MANIFEST.MF` como `FMLAT`) |
| Coremod / ASM | **No** (no hay `IFMLLoadingPlugin` ni `IClassTransformer`) |
| API pública | `@API(owner="Thaumcraft", apiVersion="6.0.2", provides="Thaumcraft|API")` |

## 2. Contenido del JAR

| Elemento | Cantidad |
|---|---|
| Archivos totales | 2660 |
| Archivos `.class` | 1120 (incluye clases internas/anónimas) |
| Clases top-level descompiladas (`.java`) | 903 |
| Líneas de código descompilado | ~131 600 (thaumcraft: ~128 300) |
| Assets (`assets/`) | ~1 530 archivos, ~15 MB |

Raíz del JAR: `thaumcraft/`, `com/sasmaster/`, `vazkii/botania/api/`, `net/tofweb/starlite/` (directorio vacío), `assets/`, `META-INF/`, `mcmod.info`, `dependencies.info`, `pack.mcmeta`, `changelog.txt`, `TCLogo.png`.

## 3. Organización de paquetes

Conteo de clases top-level (`.java`) por paquete:

| Paquete | Clases | Responsabilidad |
|---|---:|---|
| `thaumcraft` | 2 | `Thaumcraft` (@Mod) y `Registrar` (eventos `RegistryEvent.Register<T>`) |
| `thaumcraft.api.*` | 125 | API pública: aspects, aura, capabilities, casters, crafting, golems, research, items, blocks, potions, internal |
| `thaumcraft.common.blocks.*` | 88 | Bloques (basic, crafting, devices, essentia, misc, world/ore, world/plants, world/taint) |
| `thaumcraft.common.items.*` | 90 | Ítems (armor, baubles, casters/foci, consumables, curios, tools, resources) |
| `thaumcraft.common.tiles.*` | 54 | TileEntities (crafting, devices, essentia, misc) |
| `thaumcraft.common.entities.*` | 72 | Entidades (monster, boss, cult, tainted, construct, projectile, ai, champion mods) |
| `thaumcraft.common.golems.*` | 58 | Golemancia: entidad golem, partes, sellos (seals), tareas, IA, GUIs y render propios |
| `thaumcraft.common.container.*` | 36 | `Container` y `Slot` del lado servidor |
| `thaumcraft.common.lib.*` | 134 | Núcleo: network (41 paquetes), research/theorycraft, crafting, events, capabilities, potions, utils |
| `thaumcraft.common.world.*` | 14 | Worldgen, biomas, aura (hilo propio) |
| `thaumcraft.common.config` | 7 | Registro de contenido (`ConfigBlocks/Items/Entities/Recipes/Research/Aspects`) y `ModConfig` |
| `thaumcraft.client.*` | 166 | GUIs (26), FX/partículas (31), renderers (87), lib (OBJ loader, shaders, HUD) |
| `thaumcraft.codechicken.lib.*` | 42 | Copia embebida (re-empaquetada) de CodeChickenLib (vectores, CCModel, raytracer, lighting) |
| `thaumcraft.proxies` | 8 | `ClientProxy`/`ServerProxy`/`CommonProxy` (`IGuiHandler`), `ProxyBlock`, `ProxyEntities`, `ProxyTESR`, `ProxyGUI` |
| `com.sasmaster.glelwjgl.java` | 6 (7 .class) | Port Java de GLE (extrusión de tubos en OpenGL) usado para rayos/streams |
| `vazkii.botania.api.item` | 1 | Interfaz `IPetalApothecary` (soft-compat Botania) |

Archivos más grandes (indicador de complejidad): `ConfigRecipes` (3094 líneas), `WorldGenMound` (2566, generación bloque a bloque), `GuiResearchPage` (2388), `FXDispatcher` (1483), `GuiResearchBrowser` (1383), `TileInfusionMatrix` (1292), `GuiFocalManipulator` (1262), `EntityThaumcraftGolem` (969), `RenderEventHandler` (933), `UtilsFX` (926), `ConfigAspects` (918).

## 4. Ciclo de vida y punto de entrada

```
Thaumcraft (@Mod, @SidedProxy -> ClientProxy / ServerProxy)
 ├─ static { FluidRegistry.enableUniversalBucket(); }
 ├─ preInit  -> proxy.preInit   (config, capabilities, red, eventos, aspects base)
 ├─ init     -> proxy.init      (GUI handler, research init, worldgen, IMC)
 ├─ postInit -> proxy.postInit  (ResearchManager.parseAllResearch, aspects de ítems)
 ├─ serverLoad -> CommandThaumcraft (/thaumcraft, /thaum, /tc)
 └─ IMCEvent   -> proxy.checkInterModComs (p.ej. "championWhiteList")

Registrar (@EventBusSubscriber)
 ├─ Register<Block>       -> ConfigBlocks.initBlocks / initTileEntities / initMisc
 ├─ Register<Item>        -> ConfigItems.preInitSeals / initItems / initMisc (+ modelos en cliente)
 ├─ Register<EntityEntry> -> ConfigEntities.initEntities
 ├─ Register<IRecipe>     -> ConfigRecipes.* (vanilla, arcane, infusion, alchemy, compound)
 ├─ Register<Potion>      -> 9 efectos
 ├─ Register<Biome>       -> 3 biomas + BiomeManager / BiomeDictionary
 └─ Register<SoundEvent>  -> SoundsTC
```

Patrón dominante: **campos estáticos mutables** (`BlocksTC.*`, `ItemsTC.*`, `PotionX.instance`, `BiomeHandler.*`) asignados durante los eventos de registro, y clases `Config*` que concentran todo el registro de contenido.

## 5. Capas y dependencias internas

```
             ┌───────────────────────────── client (solo CLIENT) ─────────────────────────────┐
             │ gui/*  fx/*  renderers/*  lib/obj  lib/events(HUD, shaders, wand render)       │
             │ codechicken.lib  com.sasmaster.glelwjgl                                        │
             └──────────────▲─────────────────────────────────────────────▲──────────────────┘
                            │ paquetes S->C (PacketHandler)               │ lee estado de tiles/entities
┌───────────────────────────┴─────────────── common ─────────────────────┴───────────────────┐
│ config (registro) ─► blocks / items / tiles / entities / golems / container                 │
│ lib/research (ResearchManager, theorycraft) ◄─► lib/capabilities (PlayerKnowledge, Warp)   │
│ lib/crafting (ThaumcraftCraftingManager, dust triggers, infusion enchant)                  │
│ world/aura (AuraHandler + AuraThread por dimensión) ◄─► lib/events (ServerEvents, Chunk...) │
│ lib/network (SimpleNetworkWrapper "thaumcraft", 41 mensajes)                                │
└───────────────────────────▲────────────────────────────────────────────────────────────────┘
                            │ implementa IInternalMethodHandler
┌───────────────────────────┴──────────── api (thaumcraft.api) ──────────────────────────────┐
│ aspects, aura(AuraHelper), capabilities, casters(FocusEngine), crafting, golems, research   │
│ ThaumcraftApi.internalMethods (inyección de la implementación real desde common)            │
└────────────────────────────────────────────────────────────────────────────────────────────┘
```

- La API (`thaumcraft.api`) es una fachada: funciones como `AuraHelper.drainVis` delegan en `ThaumcraftApi.internalMethods` (`InternalMethodHandler` en `common.lib`).
- `common` depende de `api`; `client` depende de ambos. Algunos paquetes de `common` contienen código cliente marcado con `@SideOnly(Side.CLIENT)` (p.ej. `common.golems.client`, `KeyHandler`).
- Event handlers: 74 métodos `@SubscribeEvent` en 20 clases `@Mod.EventBusSubscriber`.

## 6. Configuración

`ModConfig` usa el sistema anotado de Forge (`@Config`, `@Config.Comment`, `@Config.RangeInt`, `@Config.RequiresMcRestart`, `@Config.LangKey`) con tres categorías: `CONFIG_GRAPHICS`, `CONFIG_MISC`, `CONFIG_WORLD` (generación de ores/cristales/árboles/estructuras, densidades, `overworldDim`, `dimensionOuterId`, pesos de biomas, área de expansión del taint, flags de regeneración retroactiva).

## 7. Calidad de la descompilación

- 893 de 903 clases descompilan sin errores.
- 10 métodos no pudieron descompilarse (detalle en `porting-risks.md` §1).
- Ver `porting-risks.md` para el análisis de ofuscación (nombres SRG) y partes difíciles de recuperar.

## Documentos relacionados

- [`systems.md`](systems.md) — sistemas de juego en detalle (blocks, items, entities, tiles, GUIs, worldgen, red, research, aspects/vis/flux/taint).
- [`dependencies.md`](dependencies.md) — dependencias externas y APIs de Minecraft/Forge usadas.
- [`assets.md`](assets.md) — inventario de recursos.
- [`porting-risks.md`](porting-risks.md) — riesgos del port a Minecraft 26.3.
