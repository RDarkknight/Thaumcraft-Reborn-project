# Sistemas de Thaumcraft 6.1.BETA26 (1.12.2)

> Inventario técnico de los sistemas del mod, obtenido del JAR descompilado. Los nombres de clase se citan tal como aparecen en el bytecode (paquete `thaumcraft.*`).

## 1. Registro de contenido

| Tipo | Mecanismo 1.12.2 | Clase que registra | Cantidad |
|---|---|---|---:|
| Blocks | `RegistryEvent.Register<Block>` | `ConfigBlocks.initBlocks` | 152 asignaciones en `BlocksTC` |
| Items | `RegistryEvent.Register<Item>` | `ConfigItems.initItems` | 105 asignaciones en `ItemsTC` (+ ItemBlocks) |
| TileEntities | `GameRegistry.registerTileEntity(Class, "thaumcraft:TileX")` | `ConfigBlocks.initTileEntities` | 48 |
| Entities | `EntityRegistry.registerModEntity(..., id++, ...)` dentro de `Register<EntityEntry>` | `ConfigEntities.initEntities` | 43 |
| Potions | `Register<Potion>` | `Registrar.registerPotions` | 9 |
| Biomes | `Register<Biome>` + `BiomeManager.addBiome` + `BiomeDictionary.addTypes` | `Registrar.registerBiomes`, `BiomeHandler` | 3 |
| SoundEvents | `Register<SoundEvent>` | `SoundsTC` | 65 eventos (111 `.ogg`) |
| Recetas vanilla/arcanas | `Register<IRecipe>` | `ConfigRecipes` | 73 arcanas + shaped/shapeless vanilla |
| Recetas propias | Catálogo interno (`ThaumcraftApi`) | `ConfigRecipes` | 56 infusión, 42 crucible, 6 multiblock, 11 "fake", 35 smelting bonus |
| Aspectos de objetos | `ThaumcraftApi.registerObjectTag/EntityTag/ComplexObjectTag` | `ConfigAspects` | ~508 llamadas |
| Research | JSON en `assets/thaumcraft/research/*.json` vía `ThaumcraftApi.registerResearchLocation` | `ConfigResearch`, `ResearchManager.parseAllResearch` | 7 categorías, 136 entradas + 12 scans |
| Seals (golems) | `SealHandler.registerSeal` | `ConfigItems.preInitSeals` | 16 |
| Dust triggers | `IDustTrigger.registerDustTrigger` | `ConfigRecipes` | Simple/Ore + 7 multibloques |
| Ore Dictionary | `OreDictionary.registerOre` | varios | 58 registros |
| GUIs | `IGuiHandler` con IDs enteros 1–22 | `CommonProxy` / `ProxyGUI` | 22 IDs |
| Paquetes de red | `SimpleNetworkWrapper.registerMessage` | `PacketHandler` | 41 |
| Comandos | `CommandBase` | `CommandThaumcraft` | 1 (`/thaumcraft`, alias `thaum`, `tc`) |

Particularidades:
- Bloques con **metadata/variantes**: 47 clases implementan `getMetaFromState`; cristales (`BlockCrystal`) codifican tamaño en meta; ítems base (`ItemTCBase` con variantes: ingots, nuggets, clusters, plates, etc.) usan `setHasSubtypes`.
- IDs de TileEntity en formato antiguo (`thaumcraft:TileJar`, CamelCase).
- IDs de entidad CamelCase (`thaumcraft:CultistPortalGreater`) e IDs numéricos secuenciales.
- Champion mobs: whitelist vía IMC (`championWhiteList`, p.ej. `"Zombie:0"`).

## 2. Blocks

Agrupados por paquete (`thaumcraft.common.blocks.*`):

- **world/ore**: `ore_amber`, `ore_cinnabar`, `ore_quartz`; cristales `crystal_aer/ignis/aqua/terra/ordo/perditio/vitium` (`BlockCrystal`, modelo custom `CrystalModel`).
- **world/plants**: `sapling/log/leaves/plank` de **greatwood** y **silverwood**; `shimmerleaf`, `cinderpearl`, `vishroom`; `grassAmbient`.
- **world/taint**: `taint_crust`, `taint_soil`, `taint_rock`, `taint_geyser` (`BlockTaint`), `BlockTaintFibre`, `BlockTaintFeature`, `BlockTaintLog`, `BlockFluxGoo` (fluido).
- **basic**: piedra arcana/ancestral/eldritch (+ ladrillos, tiles, glyphed, doorway, rock), slabs/stairs, `amber_block/brick`, `flesh_block`, bloques metálicos (brass, thaumium, void, alchemical), pilares, pedestales, paving stones (travel/barrier), `banner_*`, `candle_*` (16 colores), `nitor_*` (16 colores), `table_wood/stone`, `matrix_speed/cost`.
- **crafting**: `arcane_workbench` (+ charger), `research_table`, `crucible`, `infusion_matrix`, `focal_manipulator` (`wandWorkbench`), `thaumatorium` (+ top), `golem_builder`, `pattern_crafter`, `void_siphon`.
- **devices**: `arcane_ear` (+ toggle), lámparas (arcane/growth/fertility), `levitator`, `bellows`, `infernal_furnace`, `everfull_urn` (`BlockWaterJug`), `mirror` / `mirror_essentia`, `redstone_relay`, `potion_sprayer`, `stabilizer`, `vis_generator`, `condenser` (+ lattice/dirty), `recharge_pedestal`, `spa`, `brain_box`, `dioptra`, `hungry_chest`, `vis_battery`, `inlay`, `activator_rail`.
- **essentia**: `tube` (+ valve, filter, restrict, oneway, buffer), `jar_normal/void/brain`, `smelter_basic/thaumium/void` (+ aux, vent), `alembic`, `centrifuge`, `essentia_input/output`.
- **misc**: `loot_crate_*`, `loot_urn_*` (common/uncommon/rare), `hole`, `barrier`, `effect_shock/sap/glimmer`, placeholders de multibloque (`placeholder_anvil/bars/brick/cauldron/obsidian/table`), fluidos `liquid_death` y `purifying_fluid`, `empty`.

## 3. Items

`thaumcraft.common.items.*` (~90 clases, 105 entradas en `ItemsTC`):

- **Recursos**: amber, quicksilver, ingots/nuggets/clusters (variantes por metadata), fabric, vis/morphic resonator, tallow, mechanism simple/complex, plates, filter, mirrored glass, void seed, mind (clockwork/biothaumic), modules, `crystalEssence` (ítem con aspecto en NBT), `salisMundus` (`ItemMagicDust`), `primordialPearl`.
- **Curios / libros**: `thaumonomicon`, `curio`, `celestialNotes`, `lootBag`, `pechWand`, `enchantedPlaceholder`.
- **Consumibles**: `alumentum`, `bottleTaint`, `bathSalts`, `sanitySoap`, `causalityCollapser`, `phial`, `label`, `tripleMeatTreat`, `chunks`, `brain`.
- **Herramientas**: thaumium / void / elemental (axe, sword, shovel, pick, hoe), `primalCrusher`, `crimsonBlade`, `grappleGun`, `thaumometer`, `resonator`, `sanityChecker`, `handMirror`, `scribingTools`.
- **Armaduras**: thaumium, cloth (robes, teñibles), traveller boots, fortress, void, void robe, crimson cult (plate, robe, praetor, boots), `goggles` (también bauble).
- **Baubles** (requieren Baubles): `baubles` (variantes), `amuletVis`, `charmVerdant`, `bandCuriosity`, `charmVoidseer`, `ringCloud`, `charmUndying`, `creativeFluxSponge`.
- **Casters**: `casterBasic` (`ItemCaster`), `ItemFocus` (focos en NBT), `focusPouch`.
- **Golems**: `golemPlacer`, `golemBell`, `seals` (`ItemSealPlacer`, 16 variantes), `turretPlacer`.
- **Encantamientos de infusión** (`EnumInfusionEnchantment`, guardados en NBT, no son `Enchantment` vanilla): COLLECTOR, DESTRUCTIVE, BURROWING, SOUNDING, REFINING, ARCING, ESSENCE, VISBATTERY, VISCHARGE, SWIFT, AGILE, INFESTED, LAMPLIGHT.

## 4. Entities

43 entidades registradas (`ConfigEntities`), 72 clases:

| Grupo | Entidades |
|---|---|
| Monstruos | BrainyZombie, GiantBrainyZombie, InhabitedZombie, Wisp, Firebat, Spellbat, Pech (comercio, GUI propia), MindSpider, EldritchGuardian, EldritchCrab, ThaumSlime |
| Culto Crimson | CultistKnight, CultistCleric, CultistLeader (boss), CultistPortalLesser, CultistPortalGreater (boss) |
| Eldritch / bosses | EldritchWarden, EldritchGolem, TaintacleGiant (base `EntityThaumcraftBoss`) |
| Taint | TaintCrawler, Taintacle, TaintacleTiny, TaintSwarm, TaintSeed, TaintSeedPrime, FallingTaint |
| Constructos | Golem (`EntityThaumcraftGolem`), TurretBasic, TurretAdvanced, ArcaneBore |
| Proyectiles / efectos | FocusProjectile, FocusCloud, Focusmine, GolemDart, GolemOrb, EldritchOrb, BottleTaint, Alumentum, Grapple, CausalityCollapser |
| Misceláneas | FluxRift, SpecialItem, FollowItem |

- **Champion modifiers** (`entities/monster/mods`, 14 + `Dummy`): Armored, Bold, Fire, Grim, Infested, Mighty, Poison, Sickly, Spined, Tainted, Undying, Vampire, Warded, Warp — aplicados a mobs vanilla/whitelist en `EntityEvents`.
- Spawns: `EntityRegistry.addSpawn` para BrainyZombie y Pech (biomas mágicos/Eerie), Firebat y Wisp (Nether y biomas mágicos).
- IA: tareas `EntityAI*` propias (`ai/combat`, `ai/pech`, `ai/misc`) y, para golems, navegación propia (`PathNavigateGolemGround/Air`, `GolemNodeProcessor`, `FlightNodeProcessor`).

## 5. TileEntities

48 registradas (54 clases incl. bases `TileThaumcraft`, `TileThaumcraftInventory`):

- **crafting**: ArcaneWorkbench, Crucible, FocalManipulator, GolemBuilder, InfusionMatrix, PatternCrafter, Pedestal, ResearchTable, Thaumatorium (+Top), VoidSiphon.
- **devices**: ArcaneEar, Bellows, Condenser, Dioptra, HungryChest, InfernalFurnace, JarBrain, Lamp (Arcane/Fertility/Growth), Levitator, Mirror, MirrorEssentia, PotionSprayer, RechargePedestal, RedstoneRelay, Spa, Stabilizer, VisGenerator, WaterJug.
- **essentia**: Alembic, Centrifuge, EssentiaInput/Output, Jar (Fillable, FillableVoid), Smelter, Tube (+Buffer, Filter, Oneway, Restrict, Valve).
- **misc**: Banner, BarrierStone, Hole, Memory, Nitor.

Características: 27 implementan `ITickable`; 19 usan `IInventory/ISidedInventory`; 12 `IItemHandler`; 3 `IFluidHandler` (crucible, jars, urn). Sincronización vía `PacketTileToClient/ToServer` y `getUpdatePacket`.

## 6. GUIs y rendering

### GUIs (`IGuiHandler`, IDs)
1 Pech · 3 Thaumatorium · 4 HandMirror · 5 FocusPouch · 6 Spa · 7 FocalManipulator · 9 Smelter · 10 ResearchTable · 12 ResearchBrowser (Thaumonomicon, solo cliente) · 13 ArcaneWorkbench · 14 ArcaneBore · 16/17 Turrets · 18 Seal config · 19 GolemBuilder · 20 Logistics · 21 PotionSprayer · 22 VoidSiphon.

GUIs más complejas: `GuiResearchBrowser` (mapa navegable de research con zoom/pan, 1383 líneas), `GuiResearchPage` (páginas del libro con recetas animadas, 2388), `GuiResearchTable` (minijuego de theorycraft, 834), `GuiFocalManipulator` (editor de árbol de nodos de foco, 1262), GUIs de seals (`common.golems.client.gui`). Widgets propios en `client/gui/plugins`. `ResearchToast` usa el sistema de toasts.

### Render
- **TESR** (22): Alembic, Banner, Bellows, Centrifuge, Crucible, Dioptra, FocalManipulator, GolemBuilder, Hole, HungryChest, InfusionMatrix, Jar, Mirror, PatternCrafter, Pedestal, RechargePedestal, ResearchTable, Thaumatorium, TubeBuffer/Oneway/Valve, VoidSiphon.
- **Entity renderers** (30) con `ModelBase` (20 modelos Java) + modelos **OBJ** cargados por un loader propio (`client/lib/obj`: `AdvancedModelLoader`, `WavefrontObject`, `ObjModelLoader`) para golems, turrets, crystal, orb, hemis.
- **Armaduras** con modelos custom (`ModelFortressArmor`, `ModelRobe`, `ModelLeaderArmor`, etc.).
- **Partículas**: motor propio `ParticleEngine` + `FXDispatcher` (1483 líneas) y 24 subclases de `Particle` (`FXGeneric`, `FXVisSparkle`, `FXWispEG`, `FXSwarm`...). Rayos/streams (`FXBolt`, `FXEssentiaStream`, `FXVoidStream`) extruidos con GLE.
- **Shaders**: GLSL legacy vía `ARBShaderObjects` (`ShaderHelper`: `ender`, `sketch`) y post-procesado vanilla `ShaderGroup` (`assets/minecraft/shaders/post`: blurtc, desaturatetc, hunger, sunscorned) activados por efectos de poción (`ShaderHandler`).
- **HUD/overlays**: `HudHandler` (vis del caster, goggles: aura/aspectos), `WandRenderingHandler` (preview de bloques del foco), `RenderEventHandler` (overlays de mundo, texturas extra en atlas).
- Color handlers (`ColorHandler`) para bloques/ítems teñidos por aspecto.
- 143 clases usan `GlStateManager`/`GL11` directamente; 55 usan `Tessellator`/`BufferBuilder` en modo inmediato.

## 7. World generation

`ThaumcraftWorldGenerator implements IWorldGenerator` (registrado con Forge) + `ChunkEvents` para retrogeneración:

- **Overworld** (`overworldDim` configurable): ores de cinnabar/quartz/amber escalados por `oreDensity`; vetas de cristales de aspectos (hasta 64·densidad por chunk, el aspecto depende del bioma); árboles greatwood/silverwood (`WorldGenGreatwoodTrees`, `WorldGenSilverwoodTrees`, `WorldGenBigMagicTree`); flores (`WorldGenCustomFlowers`: shimmerleaf, cinderpearl, vishroom); estructura **Mound** (`WorldGenMound`: 2448 `setBlockState` hardcodeados) con loot urns/crates.
- **Nether**: cristales (fuego/entropía) y flora específica.
- **Aura**: `AuraHandler.generateAura(chunk)` inicializa vis base por chunk según `BiomeHandler.registerBiomeInfo(Type, auraLevel, Aspect, greatwood, chance)` (tabla por tipo de `BiomeDictionary`).
- **Biomas**: `Magical Forest` (WARM/COOL, peso configurable), `Eerie`, `Outer Lands` (`BiomeGenEldritch`). La dimensión Outer Lands **no está implementada** en 6.1 (solo existe `dimensionOuterId` en config y el bioma; no hay `WorldProvider` propio).
- Blacklist de dimensiones/biomas (`BiomeHandler.getDimBlacklist`), flags `regen*` para retrogenerar en chunks existentes.

## 8. Networking

Canal único `SimpleNetworkWrapper` `"thaumcraft"` con 41 mensajes `IMessage`+`IMessageHandler`, serializados a mano con `ByteBuf`:

| Grupo | Mensajes |
|---|---|
| FX (S→C, 16) | BlockArc, BlockBamf, BlockMist, BoreDig, EssentiaSource, FocusEffect, FocusPartImpact(+Burst), InfusionSource, Pollute, ScanSource, Shield, Slash, Sonic, WispZap, Zap |
| misc (14) | AuraToClient, BiomeChange, FocusChangeToServer, ItemKeyToServer, ItemToClientContainer, KnowledgeGain, LogisticsRequestToServer, MiscEvent, MiscStringToServer, Note, SealFilterToClient, SealToClient, SelectThaumotoriumRecipeToServer, StartTheoryToServer |
| playerdata (8) | FocusNameToServer, FocusNodesToServer, PlayerFlagToServer, SyncKnowledge, SyncProgressToServer, SyncResearchFlagsToServer, SyncWarp, WarpMessage |
| tiles (2) | TileToClient, TileToServer (NBT genérico) |

Además: `EventHandlerNetwork` y `FakeNetHandlerPlayServer` (para FakePlayers de golems/bore). Varios paquetes `*ToServer` confían en datos del cliente (p.ej. nodos de foco, progreso de research) y requieren validación en un port.

## 9. Research / Thaumonomicon

- **Datos**: JSON en `assets/thaumcraft/research/` — `basics` (20), `alchemy` (22), `auromancy` (23), `artifice` (20), `infusion` (17), `golemancy` (29), `eldritch` (5), `scans` (12 "!Entity" ocultas). Formato documentado en `_example.json.txt`: `key`, `name`, `icons`, `category`, `parents` (con `@stage` y `~` sin línea), `siblings`, `meta` (ROUND, SPIKY, HIDDEN, REVERSE, AUTOUNLOCK, HEX), `location`, `reward_item`, `reward_knowledge`, `stages[]` (`text`, `recipes`, `required_item`, `required_craft`, `required_knowledge`, `required_research`, `warp`), `addenda[]`.
- **Categorías** (`ResearchCategories.registerCategory`): BASICS, ALCHEMY, AUROMANCY, ARTIFICE, INFUSION, GOLEMANCY, ELDRITCH; cada una con una fórmula de aspectos (`AspectList`) e iconos/fondos.
- **Conocimiento del jugador** (`IPlayerKnowledge`, capability): progreso de research por etapas, flags, y puntos de conocimiento `THEORY` y `OBSERVATION` por categoría.
- **Escaneo** (`ScanningManager`, `IScanThing`): Thaumometer escanea ítems, bloques, blockstates, entidades, materiales, oredict, encantamientos, pociones, cielo (`ScanSky`) → otorga OBSERVATION y aspectos descubiertos.
- **Theorycraft** (`api/research/theorycraft`, `common/lib/research/theorycraft`, 52 clases): minijuego de la Research Table con cartas (`CardStudy`, `CardAnalyze`, `CardCelestial`, `CardDarkWhispers`...) y "aids" según bloques cercanos (`AidBookshelf`, `AidEnchantmentTable`, `AidBeacon`, `AidPortal`, `AidDragonEgg`, `AidBrainInAJar`...). Produce THEORY.
- **Triggers**: Salis Mundus sobre bloques (`IDustTrigger`: bookshelf→Thaumonomicon, workbench→Arcane Workbench, caldero→Crucible, multibloques de infusión/infernal furnace/thaumatorium/golem press).
- **Warp** (`IPlayerWarp`: PERMANENT, NORMAL, TEMPORARY) y eventos de warp (`WarpEvents`) disparados por research/ítems; `ConfigResearch.checkPeriodicStuff`.
- **GUI**: `GuiResearchBrowser` (árbol/hex map por categoría), `GuiResearchPage` (renderiza recetas de todos los tipos), `ResearchToast`.
- **Comando**: `/thaumcraft research|warp|reload|reset|...`.

## 10. Aspectos, Essentia, Vis, Aura, Flux y Taint

### Aspectos
37 aspectos (`Aspect`): 6 primales — `aer`, `terra`, `ignis`, `aqua`, `ordo`, `perditio` — y 31 compuestos (`vacuos`, `lux`, `motus`, `gelum`, `vitreus`, `metallum`, `victus`, `mortuus`, `potentia`, `permutatio`, `praecantatio`, `auram`, `alkimia`, `vitium`, `tenebrae`, `alienis`, `volatus`, `herba`, `instrumentum`, `fabrico`, `machina`, `vinculum`, `spiritus`, `cognitio`, `sensus`, `aversio`, `praemunio`, `desiderium`, `exanimis`, `bestia`, `humanus`). Cada compuesto se define por dos componentes; `AspectList` es el contenedor. Los aspectos de ítems se asignan en `ConfigAspects` y se **derivan recursivamente de recetas** (`ThaumcraftCraftingManager`) cuando no hay tag explícito. Evento `AspectRegistryEvent` para addons.

### Essentia
Aspectos en forma líquida "física": `IAspectContainer`, `IEssentiaTransport` (modelo de **succión** por cara: `getSuctionType/Amount`, `getEssentiaAmount`). Producción: Smelter (+ vents, alembics, bellows); almacenamiento: Jars (normal/void, labels); transporte: tubos (valve, filter, restrict, oneway, buffer), espejos de essentia (`EssentiaHandler` busca fuentes a distancia y dibuja streams); consumo: Infusion Matrix, Thaumatorium, Centrifuge, lámparas, Infernal Furnace, golem press.

### Vis / Aura
- Aura **por chunk**: `AuraChunk {base:short, vis:float, flux:float}` (clamp 0–32766), `AuraWorld` por dimensión, `AuraHandler` (mapas concurrentes estáticos, `AURA_CEILING = 500`).
- Persistencia: NBT del chunk vía `ChunkDataEvent.Load/Save`; sync a clientes con `PacketAuraToClient` en `ChunkWatchEvent`.
- **Simulación en un hilo propio por dimensión** (`AuraThread`, cada ~1000 ms): difunde vis a vecinos con menos vis, difunde flux, regenera vis hasta `base·phaseMax` según **fase lunar**, convierte vis excedente en flux, y si flux > 75 % del base programa un **Flux Rift** (`AuraHandler.riftTrigger`, consumido en `ServerEvents.worldTick`).
- Consumo: casters (`AuraHelper.drainVis`), Arcane Workbench (vis del chunk + cristales), Vis Generator, recarga de ítems (`RechargePedestal`, `amuletVis`, `vis_battery`).

### Flux
`AuraHelper.polluteAura` agrega flux al chunk (crafting arcano, infusión inestable, essentia vertida, foco Flux). Efectos: Flux Rifts (`EntityFluxRift`: tamaño, estabilidad, semilla; pueden colapsar en taint), flux goo (`BlockFluxGoo`), cristales vitium, potions `fluxTaint`, `visExhaust`. Mitigación: Condenser, Void Siphon, purifying fluid, `creativeFluxSponge`.

### Taint
- `TaintHelper`: semillas de taint (`EntityTaintSeed`, `EntityTaintSeedPrime`) definen el área (`taintSpreadArea`); `spreadFibres` convierte bloques en `taint_*`, `BlockTaintFibre`, `BlockTaintLog`, `BlockTaintFeature`.
- Mobs tainted (`ITaintedMob`) y capa visual `LayerTainted`; champion mod `Tainted`; animales infectados por `PotionFluxTaint`.
- Biomas cambiados en runtime (`PacketBiomeChange`), con impacto directo sobre la arquitectura moderna de biomas.

## 11. Otros sistemas

- **Infusión**: `TileInfusionMatrix` (altar multibloque con pedestales; ítems + essentia; **inestabilidad** reworkeada en BETA26 con stabilizers y redstone inlay; eventos negativos: explosiones, flux, warp, mobs). `InfusionEnchantmentRecipe`, `InfusionRunicAugmentRecipe`.
- **Alquimia**: `TileCrucible` (calor, agua, disuelve ítems a aspectos; `CrucibleRecipe`), Thaumatorium (crucible automatizado).
- **Arcane crafting**: `ShapedArcaneRecipe` / `ShapelessArcaneRecipe` con coste de vis + cristales.
- **Casters y foci**: `FocusEngine` + árbol de `FocusNode` (medium → effect → mod) editado en el Focal Manipulator. Mediums: Bolt, Cloud, Mine, Plan, Projectile, SpellBat, Touch; Effects: Air, Break, Curse, Earth, Exchange, Fire, Flux, Frost, Heal, Rift; Mods: Scatter, SplitTarget, SplitTrajectory. Todo serializado en NBT.
- **Golemancia**: golem modular (material: WOOD, IRON, CLAY, BRASS, THAUMIUM, VOID; cabeza: BASIC, SMART, SCOUT, SMART_SCOUT; brazos: BASIC, FINE, CLAWS, BREAKERS, DARTS; piernas: WALKER, ROLLER, CLIMBER, FLYER; addons: ARMORED, FIGHTER, HAULER) con traits (`EnumGolemTrait`). Seals: Pickup(+Adv), Fill(+Adv), Empty(+Adv), Provide, Stock, Guard(+Adv), Harvest, Lumber, Breaker(+Adv), Butcher, Use. Sistema de tareas (`TaskHandler`) y logística (`GuiLogistics`, `ProvisionRequest`).
- **Pociones** (9): fluxTaint, visExhaust, infectiousVisExhaust, unnaturalHunger, warpWard, deathGaze, blurredVision, sunScorned, thaumarhia.
- **Fluidos**: flux goo, liquid death, purifying fluid (+ bucket universal de Forge).
- **Loot**: `loot_tables/cultist.json`, `pech.json`; loot bags con pesos (`addLootBagItem`, 44 entradas).
