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

`BiomeModifications` añade las placed features sólo a biomas Overworld (`systems/ThaumcraftWorldgen.java:126–143`), según el alcance C1 vanilla. TC6 registra `ThaumcraftWorldGenerator` como `IWorldGenerator` (`proxies/CommonProxy.java:56–58`); `generate()` delega a `worldGeneration` (`ThaumcraftWorldGenerator.java:42–45`), que ejecuta `generateAll` en toda dimensión excepto la Outer configurada (`ThaumcraftWorldGenerator.java:47–55`, `ModConfig.java:399,402`; Outer es `-42` por defecto). `generateAll` llama `generateOres` cuando la dimensión no está marcada con nivel de blacklist 0 o 2 (`ThaumcraftWorldGenerator.java:224–236`); una dimensión no registrada devuelve `-1` (`BiomeHandler.java:86–88`). `generateOres` comprueba también el blacklist del bioma y genera ores —incluidos amber y cristales— en toda dimensión distinta de Nether (`ThaumcraftWorldGenerator.java:128–132,159–180`). Por tanto, en la configuración vanilla TC6 también genera en End (dimensión 1, no marcada por defecto) y otras dimensiones elegibles; Nether no genera esos ores/cristales. `generateSurface` sólo se invoca para `overworldDim` (predeterminado `0`), mientras `generateNether` sólo se invoca para `-1` (`ThaumcraftWorldGenerator.java:47–56`, `ModConfig.java:396–402`); `generateNether` está vacío (`ThaumcraftWorldGenerator.java:239–241`). **C1 conserva las features Overworld-only; la ampliación de dimensión queda pendiente de decisión.** Los targets vanilla son `minecraft:stone_ore_replaceables`.

La selección de aspecto toma la lógica TC6 de `BiomeHandler.java:48–55` y `ThaumcraftWorldGenerator.java:185–193`: 1/3 de intento usa un tipo biome→aspecto, con fallback aleatorio primal. Se usan tags `c:` equivalentes en `systems/ThaumcraftWorldgen.java:91–120`. En TC6 `Type.DRY` se registra primero como FIRE y luego como ENTROPY; el valor final sobrescrito es ENTROPY. Los tags convencionales no garantizan el mismo conjunto ni orden que `BiomeDictionary`, por lo que la selección aleatoria entre tipos coincidentes no es idéntica. Vitium no se incluye: aunque el bloque se registra (`ConfigBlocks.java:181`), la generación selecciona `md` entre los seis primales (`ThaumcraftWorldGenerator.java:185–195`).

## Diferencias y comportamiento diferido

- El drop raro de curio de amber queda diferido a contenido posterior (`BlockOreTC.java:36–46`). La probabilidad base de amber (1–2) y la fórmula Fortune están portadas a la loot table (`BlockOreTC.java:52–63`). Silverwood drops a quicksilver nugget con la probabilidad base equivalente a la expresión TC6 (`BlockLeavesTC.java:118–121`); el ajuste dinámico de `chance` que pudiera aplicar la ruta de hojas con Fortune no está representado en la loot table.
- Los cristales usan el Data Component `crystal_aspect` en un solo item stackable `crystal_essence`, no items por aspecto. El bloque conserva size, generación, drops y una lógica de crecimiento aislada en `systems/CrystalGrowth.java`; ahora rompe y suelta sus drops al perder apoyo, y reproduce la forma de selección TC6 como `VoxelShape`. El soporte moderno usa `minecraft:base_stone_overworld` y caras de colisión completas en lugar del test TC6 de material ROCK y `isSideSolid`; el collision shape permanece vacío, como el `null` de TC6 (`BlockCrystal.java:247–249`). Los efectos de aura existentes se consultan; no se modificó su simulación.
- Los saplings son placeholders sin crecimiento; crecimiento de árboles se difiere a F. No se genera ningún árbol en C1.
- Texturas son placeholders procedurales originales y los nombres son propios `en_us`/`es_es`; no se importaron assets ni texto TC6.
- El framework de recipes registra codecs/serializers y `matches` arcane puro. Crucible e infusion no ejecutan máquinas; el sync de recetas al cliente queda diferido.
