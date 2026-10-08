# C2 — Thaumometer, revealing, and Thaumonomicon

#### End worldgen

TC6 calls `generateOres` for dimensions other than the Nether (`/home/ubuntu/tc/src/thaumcraft/common/world/ThaumcraftWorldGenerator.java:224–236`); amber, cinnabar, quartz, and primal-crystal attempts are in `:128–185`. Ore replacement targets stone and therefore do not match End stone. Primal crystals require a supporting block whose material is `ROCK` (`/home/ubuntu/tc/src/thaumcraft/common/blocks/world/ore/BlockCrystal.java:51–60,124–143`). In 26.3, End stone is the selected equivalent: the crystal feature is added through `BiomeSelectors.foundInTheEnd()` and accepts `Blocks.END_STONE` as a full-face support. Ore features remain Overworld-only because their replacement predicate cannot place in the End. No other modded-dimension selector is added.

#### Scanning

`ItemThaumometer` is stack-size one and Uncommon; use dispatches scanning immediately without a duration or cooldown (`/home/ubuntu/tc/src/thaumcraft/common/items/tools/ItemThaumometer.java:32–50,147–161`). Aura reads are sent every 20 ticks while selected or in inventory slot zero, and the low-vis/high-flux warning condition is `flux > vis || flux > base / 3` (`:53–71,97–109`). The modern implementation uses a nine-block block/entity ray, reports scan status through translated action-bar messages, and does not add a cooldown. TC6 scan sound and particle/rune effects are not copied because their assets are excluded.

The generic scan awards category OBSERVATION using the aspect/category formula (`/home/ubuntu/tc/src/thaumcraft/common/lib/research/ScanGeneric.java:17–76`, `/home/ubuntu/tc/src/thaumcraft/api/research/ResearchCategory.java:41–60`). A newly discovered aspect key also awards one OBSERVATION in Auromancy, Basics, and Alchemy (`/home/ubuntu/tc/src/thaumcraft/api/research/ScanAspect.java:24–67`). The implementation stores `scan/<target>` and `scan/aspect/<tag>` in ordinary player research knowledge; already-known keys award nothing. It supports static item/entity aspect mappings, C1 amber/cinnabar/crystal block scan keys, potion effects, enchantments, and scans up to 100 stacks in a scanned container. Inventory hover sends only menu/slot IDs in a `CustomPacketPayload`; the server verifies the active menu, slot bounds, current non-empty stack, and active Thaumometer, with a five-tick per-player rate limit. Client-supplied aspect data is never accepted.

TC6-specific scanners for Thaumcraft entities, blocks, metadata variants, Forge materials, and OreDictionary targets are not active for C1 content and remain deferred with those later registry targets. Modern item components replace legacy item metadata for potion/enchantment scanning; no broad Forge OreDictionary substitute is introduced.

#### Goggles and HUD

TC6 Goggles use the special armor material and head slot, 350 durability, Rare rarity, and are repaired by a Thaumium ingot (`/home/ubuntu/tc/src/thaumcraft/common/items/armor/ItemGoggles.java:32–95`; material durability multiplier 25, helmet defense 1, enchantability 25, toughness 1: `/home/ubuntu/tc/src/thaumcraft/api/ThaumcraftMaterials.java:20–22`). The 26.3 armor material preserves those values. The single revealing helper checks the head equipment slot and the existing `AccessoryAccess`; no Trinkets slot is defined. TC6's five-point vis discount is not applied because C1/C2 has no vis-cost system to consume it.

The HUD uses Fabric's extraction-based HUD API, without GL. It renders the aura meter from a client-only cache while the Thaumometer is held, and target aspects for blocks, entities, and hovered inventory slots while revealing is active or the Thaumometer is held. Aura is read through `AuraAccess`, sent only to players meeting TC6's held/slot-zero condition at 20-tick cadence, and is not simulated or mutated (`ItemThaumometer.java:53–71,97–109`; `HudHandler.java:71–90,216–295`; `RenderEventHandler.java:532–542`). TC6's hover-target selection is updated every five client ticks (`ItemThaumometer.java:59–69`), while inventory aspect display is in `HudHandler.java:505–524`. Text-only aspect labels are a 26.3 presentation adaptation; TC6's rune/texture effects are not reused.

#### Thaumonomicon and pickup flags

Thaumonomicon is a stack-size-one item and opens a client screen. The screen gates categories by their research key, lists only known entries (`ResearchStatus`), and translates the selected entry title and stages reached. It omits the hex map, recipe pages, theorycrafting, and Salis Mundus bookshelf/dust progression (`/home/ubuntu/tc/src/thaumcraft/common/items/curios/ItemThaumonomicon.java:31–65`; pickup flags in `/home/ubuntu/tc/src/thaumcraft/common/lib/events/PlayerEvents.java:131–175`).

Pickup of the Thaumonomicon grants `flag/got_thaumonomicon`; pickup of crystal essence grants `flag/got_crystals`. The TC6 wake-up condition (`got_crystals` and not already `got_dream`) grants `flag/got_dream` through Fabric's sleep-stop event (`PlayerEvents.java:153–167`). The separate immediate crystal-pickup branch depends on the TC6 `noSleep` config and is not modeled; the dream-journal item delivery is also deferred. The item-pickup hook uses `ItemEntity.playerTouch` and grants no flag merely for possessing an item. The Salis Mundus bookshelf/dust route remains Stage D and is not implemented.

# Stage C1 — contenido

Este documento registra el linaje de los valores implementados para los elementos 1, 2, 4 y 5 de Etapa C. Los assets y textos de TC6 no se copiaron. Las recetas vanilla se generan mediante datagen; los tipos de receta propios sólo describen datos y no simulan máquinas.

## Contenido y propiedades

| Contenido | Valores implementados | Fuente TC6 |
|---|---|---|
| Amber ore | dureza 1.5; resistencia 5; pico, harvest level 1; drop base 1–2 amber, escalado por Fortune como en TC6; XP 1–4 | `common/config/ConfigBlocks.java:154,172`; `common/blocks/world/ore/BlockOreTC.java:20–63,66–75` |
| Cinnabar ore | dureza 2; resistencia 5; pico, harvest level 2; drop propio, 1 item; sin XP | `common/config/ConfigBlocks.java:155,173`; `common/blocks/world/ore/BlockOreTC.java:20–24,26–35,52–63` |
| Quartz ore | dureza 3; resistencia 5; pico; drop de quartz; Fortune y XP 1–4 | `common/config/ConfigBlocks.java:174`; `common/blocks/world/ore/BlockOreTC.java:20–35,52–75` |
| Primal crystals | dureza 0.25; `size` 0–3, `gen` 1–4; drop `size + 1` esencias con el aspecto en Data Component; al perder soporte se rompen y sueltan el drop; selección full-cube con más de un soporte, media caja hacia un soporte, full-cube sin soporte | `common/blocks/world/ore/BlockCrystal.java:40–60,75–91,170–184,186–249` |
| Crecimiento de cristales | intento cada `1 / (3 + gen)` ticks aleatorios; umbral 10 de vis; tamaño máximo 3; dispersión a bloque adyacente con probabilidad 1/16, y decremento de generación con probabilidad 1/6 | `common/blocks/world/ore/BlockCrystal.java:94–148,151–175` |
| Arcane stone / bricks | dureza 2, resistencia 10; las recetas son crafting normal de TC6 | `common/blocks/basic/BlockStoneTC.java:11–20`; `common/config/ConfigRecipes.java:2811–2822` |
| Amber block / brick | dureza 0.5; crafting normal | `common/blocks/basic/BlockTranslucent.java:17–22`; `common/config/ConfigRecipes.java:2666–2669` |
| Thaumium block | dureza 4, resistencia 10 | `common/blocks/basic/BlockMetalTC.java:9–15` |
| Greatwood / silverwood logs | dureza 2, resistencia 5, hacha; silverwood emite luz nivel 5 | `common/blocks/world/plants/BlockLogsTC.java:21–39` |
| Greatwood / silverwood planks | dureza 2, hacha; flammability 20, fire spread 5 | `common/blocks/basic/BlockPlanksTC.java:10–24` |
| Leaves | la probabilidad base de sapling usa `1/75`; silverwood además suelta un nugget de quicksilver con probabilidad `1/56` (la expresión TC6 trunca `(int)(75 * 0.75)`) | `common/blocks/world/plants/BlockLeavesTC.java:114–121`; `common/config/ConfigItems.java:179–182` |
| Saplings | growth y generación de árboles se difieren a Stage F | `common/blocks/world/plants/BlockSaplingTC.java:25–63` |
| Thaumium tools | tier 3, durabilidad 500, velocidad 7.0, bonus de daño 2.5, encantabilidad 22; reparación con lingote | `api/ThaumcraftMaterials.java:12–13`; `common/config/ConfigRecipes.java:2677–2686` |
| Quartz y quicksilver nuggets | quartz es el catalizador de las seis recetas de esencias; quicksilver reproduce el drop de hojas silverwood. El mapeo del nugget es `metallum:1`, congelado del registro `thaumcraft:nugget` meta 5 en `/home/ubuntu/tc6-dump/server/dump/items.jsonl` | `common/config/ConfigItems.java:179–182`; `common/config/ConfigRecipes.java:185–194`; `common/blocks/world/plants/BlockLeavesTC.java:118–121` |

El material de herramientas usa la tag `minecraft:incorrect_for_diamond_tool` como equivalente al tier TC6 3. Las tags de bloques traducen harvest level 1 de amber a `needs_stone_tool` y level 2 de cinnabar a `needs_iron_tool`; quartz no tenía harvest level explícito en TC6. Los logs, planks y leaves usan tags vanilla de herramienta y bloques.

## Recetas

- Conversiones de thaumium, amber y ladrillos: `common/config/ConfigRecipes.java:2643–2669`.
- Herramientas thaumium: `common/config/ConfigRecipes.java:2677–2686`.
- Greatwood/silverwood planks, arcane stone y arcane bricks: `common/config/ConfigRecipes.java:2770–2780,2811–2822`. La receta `StoneArcane` es crafting normal, no arcane.
- Smelting de cinnabar → quicksilver, amber ore → amber y quartz ore → quartz, con XP 1.0: `common/config/ConfigRecipes.java:2911–2914`.
- Smelting de ambos logs → charcoal, con XP 0.5: `common/config/ConfigRecipes.java:2915–2916`.
- Crucible para thaumium ingot: hierro como catalizador, `praecantatio 5 + terra 5`, `METALLURGY@2`: `common/config/ConfigRecipes.java:229–232`.
- Crucible para esencias primales: nugget de quartz y 2 unidades del aspecto correspondiente, `BASEALCHEMY`: `common/config/ConfigRecipes.java:185–194`. Se registra `nugget_quartz` como item C1 y miembro de `c:nuggets/quartz`, porque el catalizador TC6 no tiene equivalente vanilla.

Los recursos de crafting/smelting se generan en `data/thaumcraft_reborn/recipe/`. Las recetas de thaumium y las seis esencias usan el serializer crucible aunque la máquina todavía no existe. TC6 no define, entre estos outputs C1, recetas arcane, crucible adicionales ni infusion; arcane stone y las herramientas son crafting normal. Los fixtures de GameTest ejercitan arcane shaped/shapeless, crucible e infusion. Client recipe sync queda fuera de alcance.

## Worldgen

Fuentes: `common/world/ThaumcraftWorldGenerator.java:128–219` y `common/config/ModConfig.java:409,432–435`.

| Feature | Conteo TC6 | Rango / regla implementada |
|---|---:|---|
| Cinnabar | 18 por chunk a densidad 100 | Y `[0, worldHeight/5)`; target piedra reemplazable |
| Quartz | 18 por chunk a densidad 100 | Y `[0, worldHeight/4)`; target piedra reemplazable |
| Amber | 20 por chunk a densidad 100 | cerca del world surface, restando `nextInt(25)` |
| Cristales | 8 intentos por chunk a densidad 100, máximo `64 * density` unidades | Y aleatoria bajo `surface - 5`; hasta 27 posiciones por intento; size aleatorio 1–3 |

La densidad predeterminada de TC6 es `oreDensity=100`; la implementación usa esos conteos predeterminados, pero no porta los toggles/configuración dinámica ni los flags de regeneración TC6. Los rangos absolutos 0–50 para cinnabar y 0–63 para quartz se conservan en 26.3; como 26.3 permite alturas negativas, no se extienden las vetas por debajo de Y=0. Amber conserva su selección respecto a la superficie.

`BiomeModifications` añade las placed features de ores sólo a biomas Overworld y los cristales a biomas Overworld y End (`systems/ThaumcraftWorldgen.java:126–143`). TC6 registra `ThaumcraftWorldGenerator` como `IWorldGenerator` (`proxies/CommonProxy.java:56–58`); `generate()` delega a `worldGeneration` (`ThaumcraftWorldGenerator.java:42–45`), que ejecuta `generateAll` en toda dimensión excepto la Outer configurada (`ThaumcraftWorldGenerator.java:47–55`, `ModConfig.java:399,402`; Outer es `-42` por defecto). `generateAll` llama a `generateOres` cuando la dimensión no está marcada con nivel de blacklist 0 o 2 (`ThaumcraftWorldGenerator.java:224–236`); una dimensión no registrada devuelve `-1` (`BiomeHandler.java:86–88`). `generateOres` comprueba también el blacklist del bioma y genera ores —incluidos amber y cristales— en toda dimensión distinta de Nether (`ThaumcraftWorldGenerator.java:128–132,159–180`). Por tanto, en la configuración vanilla TC6 también genera en End (dimensión 1, no marcada por defecto) y otras dimensiones elegibles; Nether no genera esos ores/cristales. `generateSurface` sólo se invoca para `overworldDim` (predeterminado `0`), mientras `generateNether` sólo se invoca para `-1` (`ThaumcraftWorldGenerator.java:47–56`, `ModConfig.java:396–402`); `generateNether` está vacío (`ThaumcraftWorldGenerator.java:239–241`).

La feature de cristales también usa `BiomeSelectors.foundInTheEnd()` y admite `minecraft:end_stone` como soporte junto con `minecraft:base_stone_overworld`; exige una cara de colisión completa (`systems/ThaumcraftWorldgen.java:126–144,152–165`). Es la equivalencia elegida para el test TC6 de material ROCK y cara sólida. Los ores mantienen como target `minecraft:stone_ore_replaceables`, así que no se colocan en End Stone. La ampliación sólo alcanza los biomas seleccionados por el selector End de Fabric; no se añaden otros selectores de dimensiones moddeadas.

La selección de aspecto toma la lógica TC6 de `BiomeHandler.java:48–55` y `ThaumcraftWorldGenerator.java:185–193`: 1/3 de intento usa un tipo biome→aspecto, con fallback aleatorio primal. Se usan tags `c:` equivalentes en `systems/ThaumcraftWorldgen.java:91–120`. En TC6 `Type.DRY` se registra primero como FIRE y luego como ENTROPY; el valor final sobrescrito es ENTROPY. Los tags convencionales no garantizan el mismo conjunto ni orden que `BiomeDictionary`, por lo que la selección aleatoria entre tipos coincidentes no es idéntica. Vitium no se incluye: aunque el bloque se registra (`ConfigBlocks.java:181`), la generación selecciona `md` entre los seis primales (`ThaumcraftWorldGenerator.java:185–195`).

## Diferencias y comportamiento diferido

- El drop raro de curio de amber queda diferido a contenido posterior (`BlockOreTC.java:36–46`). La probabilidad base de amber (1–2) y la fórmula Fortune están portadas a la loot table (`BlockOreTC.java:52–63`). Silverwood drops a quicksilver nugget con la probabilidad base equivalente a la expresión TC6 (`BlockLeavesTC.java:118–121`); el ajuste dinámico de `chance` que pudiera aplicar la ruta de hojas con Fortune no está representado en la loot table.
- Los cristales usan el Data Component `crystal_aspect` en un solo item stackable `crystal_essence`, no items por aspecto. El bloque conserva size, generación, drops y una lógica de crecimiento aislada en `systems/CrystalGrowth.java`; ahora rompe y suelta sus drops al perder apoyo, y reproduce la forma de selección TC6 como `VoxelShape`. El soporte moderno usa `minecraft:base_stone_overworld` y caras de colisión completas en lugar del test TC6 de material ROCK y `isSideSolid`; el collision shape permanece vacío, como el `null` de TC6 (`BlockCrystal.java:247–249`). Los efectos de aura existentes se consultan; no se modificó su simulación.
- Los saplings son placeholders sin crecimiento; crecimiento de árboles se difiere a F. No se genera ningún árbol en C1.
- Texturas son placeholders procedurales originales y los nombres son propios `en_us`/`es_es`; no se importaron assets ni texto TC6.
- El framework de recipes registra codecs/serializers y `matches` arcane puro. Crucible e infusion no ejecutan máquinas; el sync de recetas al cliente queda diferido.
