# Etapa B — Modelo de dominio

Este documento registra los contratos de datos y las simplificaciones de la Etapa B. Los valores de gameplay se basan en TC6, pero los tipos, codecs, cargadores y attachments son nativos de Fabric/26.3. Ningún recurso de TC6 se copia al proyecto.

## Aspectos y asignaciones

El registro sincronizado `thaumcraft_reborn:aspect` contiene los seis primales y los 31 compuestos en el orden de declaración de TC6. `AspectList` es una lista inmutable de inserción ordenada; su codec JSON usa claves de aspecto namespaced y su `StreamCodec` conserva el orden.

Desviación respecto a TC6: `AspectList` descarta cantidades menores o iguales a cero, incluso al reducir a cero. TC6 puede conservar entradas con cantidad cero; `getAmount` no cambia, pero aquí la invariante es que sólo hay cantidades positivas.

Los nombres generados en `en_us` y `es_es` son las etiquetas capitalizadas de los aspectos (por ejemplo, `Aer` y `Praecantatio`). No se añaden descripciones: el texto de descripciones de TC6 queda pendiente y no se copia en esta etapa.

`aspect_mappings` usa archivos en `data/<namespace>/thaumcraft_reborn/aspect_mappings/*.json`. Los targets son excluyentes (`item`, `tag` o `entity`); `nbt` sólo se admite para entidades y `replace` es opcional:

```json
{
  "entries": [
    {
      "tag": "c:gems/diamond",
      "aspects": {
        "thaumcraft_reborn:terra": 5,
        "thaumcraft_reborn:metallum": 2
      }
    }
  ]
}
```

Los archivos se ordenan lexicográficamente por id. Primero se suman todos los targets `replace: false`; después se aplican los `replace: true` en orden, sobrescribiendo lo acumulado aunque el archivo de reemplazo ordene antes que uno aditivo. Cada reemplazo produce una advertencia; si se reemplaza el mismo target varias veces, otra advertencia indica que gana el último. Al construir el snapshot del servidor se advierte una vez por id desconocido de ítem o entidad en cada reload. Para ítems, una asignación exacta prevalece sobre tags; entre varios tags coincidentes se usa el id lexicográficamente menor. Las entidades prefieren el primer predicado NBT coincidente y usan después la regla sin predicado como fallback. No se derivan aspectos desde recetas. Tampoco se aplican bonificaciones por encantamientos, pociones o contenedores; estas últimas quedan para C.

### Transcripción vanilla de `ConfigAspects`

Los mappings convertidos se guardan como JSON propio del proyecto en:

- `data/thaumcraft_reborn/thaumcraft_reborn/aspect_mappings/tc6_vanilla_items.json`
- `data/thaumcraft_reborn/thaumcraft_reborn/aspect_mappings/tc6_vanilla_entities.json`

La conversión leyó sólo las llamadas vanilla `registerObjectTag` y `registerEntityTag` de `ConfigAspects.java`, resolvió campos SRG con MCP stable_39 y ejecutó `ItemStackTheFlatteningFix` más los datafixes posteriores de MC 26.3 (data version 5023). Las llamadas se simularon en orden de fuente: cada escritura oredict expandió la membresía vanilla actual del tag elegido; cada asignación posterior al mismo ID de ítem moderno reemplazó la anterior. `new AspectList(new ItemStack(X))` copió el valor acumulado de X en ese punto. El metadata `32767` se expandió a todas sus variantes flattening. Se escribió una entrada explícita por ítem vanilla con su valor final y una entrada por tag oredict mapeable; si la equivalencia exacta no existe, sólo se escribieron los ítems vanilla disponibles y no se inventó un tag. El lookup exacto de ítem conserva precedencia sobre los tags, permitiendo que otros ítems de esos tags reciban el valor oredict.

| Resultado de la conversión | Cantidad |
|---|---:|
| `registerObjectTag` inspeccionados | 333 |
| Targets item explícitos finales (`tc6_vanilla_items.json`) | 430 |
| Entradas de tag emitidas | 43 |
| Targets estáticos generados desde runtime (`tc6_vanilla_generated.json`) | 208 |
| `registerEntityTag` inspeccionados | 81 |
| Entradas de entidad emitidas | 53 (una con NBT) |
| Variantes de ítem producidas por metadata wildcard | 88 |
| IDs desconocidos en los datos emitidos | 0 |
| `registerComplexObjectTag` vanilla con valor raw emitido | 94 IDs item |
| Llamadas de ítems/bloques propios de TC6 diferidas a C | 83 |
| Llamadas de entidades propias de TC6 diferidas a C | 27 |

Las reglas que no caben en el formato o no tienen un target vanilla moderno se registraron sin derivar datos:

| Motivo | Cantidad |
|---|---:|
| Oredict sin tag exacto ni miembro vanilla | 36 |
| Targets con NBT/poción o construidos dinámicamente | 7 |
| Target externo/modded (`universalBucket`) | 1 |
| Target sin ítem registrable en MC 26.3 | 4 |
| Colisión de metadata que aplana al mismo ID (`minecraft:dead_bush`); gana la escritura TC6 posterior | 1 |

Los cuatro targets sin ítem moderno son `lit_redstone_ore`, `fire`, `portal` y `end_portal`. Los targets con datos de poción/NBT y los nombres oredict sin miembros vanilla se detallan en el reporte externo. Las 83 llamadas de contenido de ítem/bloque y las 27 de entidad se difieren a C porque esos registros propios todavía no existen.

#### Correspondencia de oredict a tags 26.3

La membresía de los tags se tomó de los datos vanilla y de los tags convencionales Fabric presentes en el entorno 0.161. `— (sólo ítems)` indica que no existe una equivalencia exacta publicable y se emitieron únicamente los miembros vanilla explícitos; `— (sin miembros vanilla)` indica que no había un tag equivalente ni miembros vanilla.

| Oredict | Tag vanilla/Fabric o resultado |
|---|---|
| `oreLapis` | `c:ores/lapis` |
| `oreDiamond` | `c:ores/diamond` |
| `gemDiamond` | `c:gems/diamond` |
| `oreRedstone` | `c:ores/redstone` |
| `oreEmerald` | `c:ores/emerald` |
| `gemEmerald` | `c:gems/emerald` |
| `oreQuartz` | `c:ores/quartz` |
| `gemQuartz` | `c:gems/quartz` |
| `oreIron` | `c:ores/iron` |
| `dustIron` | — (sin miembros vanilla) |
| `ingotIron` | `c:ingots/iron` |
| `oreGold` | `c:ores/gold` |
| `dustGold` | — (sin miembros vanilla) |
| `ingotGold` | `c:ingots/gold` |
| `dustRedstone` | `c:dusts/redstone` |
| `dustGlowstone` | `c:dusts/glowstone` |
| `glowstone` | — (sólo ítems) |
| `ingotCopper` | `c:ingots/copper` |
| `dustCopper` | — (sin miembros vanilla) |
| `oreCopper` | `c:ores/copper` |
| `clusterCopper` | — (sin miembros vanilla) |
| `ingotTin` | — (sin miembros vanilla) |
| `dustTin` | — (sin miembros vanilla) |
| `oreTin` | — (sin miembros vanilla) |
| `clusterTin` | — (sin miembros vanilla) |
| `ingotSilver` | — (sin miembros vanilla) |
| `dustSilver` | — (sin miembros vanilla) |
| `oreSilver` | — (sin miembros vanilla) |
| `clusterSilver` | — (sin miembros vanilla) |
| `ingotLead` | — (sin miembros vanilla) |
| `dustLead` | — (sin miembros vanilla) |
| `oreLead` | — (sin miembros vanilla) |
| `clusterLead` | — (sin miembros vanilla) |
| `ingotBrass` | — (sin miembros vanilla) |
| `dustBrass` | — (sin miembros vanilla) |
| `ingotBronze` | — (sin miembros vanilla) |
| `dustBronze` | — (sin miembros vanilla) |
| `oreUranium` | — (sin miembros vanilla) |
| `itemDropUranium` | — (sin miembros vanilla) |
| `ingotUranium` | — (sin miembros vanilla) |
| `gemRuby` | — (sin miembros vanilla) |
| `gemGreenSapphire` | — (sin miembros vanilla) |
| `gemSapphire` | — (sin miembros vanilla) |
| `ingotSteel` | — (sin miembros vanilla) |
| `itemRubber` | — (sin miembros vanilla) |
| `stone` | `c:stones` |
| `stoneGranite` | — (sólo ítems) |
| `stoneDiorite` | — (sólo ítems) |
| `stoneAndesite` | — (sólo ítems) |
| `cobblestone` | `c:cobblestones` |
| `dirt` | `minecraft:dirt` |
| `sand` | `c:sands` |
| `grass` | — (sólo ítems) |
| `endstone` | `c:end_stones` |
| `gravel` | `c:gravels` |
| `netherrack` | `c:netherracks` |
| `ingotBrickNether` | — (sólo ítems) |
| `blockGlass` | `c:glass_blocks` |
| `obsidian` | `c:obsidians` |
| `logWood` | `minecraft:logs` |
| `treeSapling` | `minecraft:saplings` |
| `treeLeaves` | `minecraft:leaves` |
| `vine` | — (sólo ítems) |
| `cropNetherWart` | `c:crops/nether_wart` |
| `blockCactus` | `c:crops/cactus` |
| `sugarcane` | `c:crops/sugar_cane` |
| `cropWheat` | `c:crops/wheat` |
| `cropCarrot` | `c:crops/carrot` |
| `cropPotato` | `c:crops/potato` |
| `string` | `c:strings` |
| `slimeball` | `c:slime_balls` |
| `leather` | `c:leathers` |
| `feather` | `c:feathers` |
| `bone` | `c:bones` |
| `egg` | `c:eggs` |
| `gunpowder` | `c:gunpowders` |
| `enderpearl` | `c:ender_pearls` |
| `netherStar` | `c:nether_stars` |
| `clusterIron` | — (sin miembros vanilla) |
| `clusterGold` | — (sin miembros vanilla) |
| `clusterCinnabar` | — (sin miembros vanilla) |
| `clusterQuartz` (2 registros) | — (sin miembros vanilla) |
| `oreCinnabar` | — (sin miembros vanilla) |
| `oreAmber` | — (sin miembros vanilla) |
| `quicksilver` | — (sin miembros vanilla) |
| `gemAmber` | — (sin miembros vanilla) |

#### Entidades vanilla, NBT y renombres

Se conservó el predicado de creeper `{powered:1b}` porque el campo mantiene su semántica. El target TC6 `Guardian` con `Elder:true` se convierte a `minecraft:elder_guardian` sin predicado. Los renombres del resto de entidades son:

| Nombre TC6 | ID moderno |
|---|---|
| `MushroomCow` | `minecraft:mooshroom` |
| `SnowMan` | `minecraft:snow_golem` |
| `Ozelot` | `minecraft:ocelot` |
| `XPOrb` | `minecraft:experience_orb` |
| `PigZombie` | `minecraft:zombified_piglin` |
| `LavaSlime` | `minecraft:magma_cube` |
| `WitherBoss` | `minecraft:wither` |
| `VillagerGolem` | `minecraft:iron_golem` |
| `EnderCrystal` | `minecraft:end_crystal` |
| `EvocationIllager` | `minecraft:evoker` |
| `VindicationIllager` | `minecraft:vindicator` |
| `IllusionIllager` | `minecraft:illusioner` |

Los mappings de `Thaumcraft.*` se difieren a C; el detalle por callsite está en `/home/ubuntu/stage-b/convert/report.md`.

## Research TC6

Se convirtieron los ocho archivos fuente (alchemy 22, artifice 20, auromancy 23, basics 20, eldritch 5, golemancy 29, infusion 17 y scans 12): **148 entradas**, más las siete categorías ya definidas. IDs y referencias se pasan a minúsculas y namespace `thaumcraft_reborn`; se conservan `@N` y `~`. Names/title y stages son claves `research.thaumcraft_reborn.<key>.title` y `research.thaumcraft_reborn.<key>.stage.N`; no se copian textos, texturas ni assets TC6. Los IDs de recetas y texturas son referencias nuevas; recetas aún no registradas no bloquean el decode.

Los campos de ítem usan `ItemReference` (`"item": "namespace:id"` o `{"item":"namespace:id","count":N,"components":{...}}`). El tipo retiene IDs aún no registrados y sólo resuelve `ItemStack` en el punto de uso. Es necesario porque Minecraft 26.3 enlaza los componentes de ítems después de terminar los reload listeners; `ItemStack.CODEC` no puede decodificarlos durante ese reload. Requisitos sin resolver no coinciden, recompensas sin resolver se omiten e iconos permanecen sin resolver; el índice cuenta ocurrencias e informa una vez por ID desconocido por reload.

Los `!` no son negación. Se clasificaron 61 claves: 5 flags/eventos, 28 claves de scans y 28 claves de aspecto. De ellas, 4 no aparecen como entradas/referencias en los ocho JSON fuente y sólo existen en el código Java: `!ORBLOCK3`, `!ORBOSS`, `!ORMOB`, `!gotcrystals`. Las claves Java encontradas sin clasificación son 0.

| TC6 key | New key | Class | TC6 granting code |
|---|---|---|---|
| `!BATHSALTS` | `thaumcraft_reborn:flag/bath_salts` | event flag | `WarpEvents.java:223-225` |
| `!BrainyZombie` | `thaumcraft_reborn:scan/brainy_zombie` | scan key | `ConfigResearch.java:256 (ScanEntity)` |
| `!CrimsonCultist` | `thaumcraft_reborn:scan/crimson_cultist` | scan key | `ConfigResearch.java:259 (ScanEntity)` |
| `!DRAGONBREATH` | `thaumcraft_reborn:scan/dragon_breath` | scan key | `ConfigResearch.java:332 (ScanGeneric)` |
| `!EldritchCrab` | `thaumcraft_reborn:scan/eldritch_crab` | scan key | `ConfigResearch.java:257 (ScanEntity)` |
| `!EldritchGuardian` | `thaumcraft_reborn:scan/eldritch_guardian` | scan key | `ConfigResearch.java:260 (ScanEntity)` |
| `!Firebat` | `thaumcraft_reborn:scan/firebat` | scan key | `ConfigResearch.java:254 (ScanEntity)` |
| `!FluxRift` | `thaumcraft_reborn:scan/flux_rift` | scan key | `ConfigResearch.java:266 (ScanEntity)` |
| `!INSTABILITY` | `thaumcraft_reborn:flag/instability` | event flag | `TileInfusionMatrix.java:680-681` |
| `!ORBLOCK1` | `thaumcraft_reborn:scan/or_block_1` | scan key | `ConfigResearch.java:281 (ScanBlock)` |
| `!ORBLOCK2` | `thaumcraft_reborn:scan/or_block_2` | scan key | `ConfigResearch.java:282 (ScanBlock)` |
| `!ORBLOCK3` | `thaumcraft_reborn:scan/or_block_3` | scan key | `ConfigResearch.java:283 (ScanBlock)` |
| `!ORBOSS` | `thaumcraft_reborn:scan/or_boss` | scan key | `ConfigResearch.java:280 (ScanEntity)` |
| `!OREAMBER` | `thaumcraft_reborn:scan/ore_amber` | scan key | `ConfigResearch.java:298 (ScanBlock)` |
| `!ORECINNABAR` | `thaumcraft_reborn:scan/ore_cinnabar` | scan key | `ConfigResearch.java:299 (ScanBlock)` |
| `!ORECRYSTAL` | `thaumcraft_reborn:scan/ore_crystal` | scan key | `ConfigResearch.java:302 (ScanGeneric)` |
| `!ORMOB` | `thaumcraft_reborn:scan/or_mob` | scan key | `ConfigResearch.java:279 (ScanEntity)` |
| `!PLANTCINDERPEARL` | `thaumcraft_reborn:scan/plant_cinder_pearl` | scan key | `ConfigResearch.java:328 (ScanBlock)` |
| `!PLANTSHIMMERLEAF` | `thaumcraft_reborn:scan/plant_shimmerleaf` | scan key | `ConfigResearch.java:329 (ScanBlock)` |
| `!PLANTVISHROOM` | `thaumcraft_reborn:scan/plant_vishroom` | scan key | `ConfigResearch.java:330 (ScanBlock)` |
| `!PLANTWOOD` | `thaumcraft_reborn:scan/plant_wood` | scan key | `ConfigResearch.java:324 (ScanBlock)` |
| `!Pech` | `thaumcraft_reborn:scan/pech` | scan key | `ConfigResearch.java:255 (ScanEntity)` |
| `!Pechwand` | `thaumcraft_reborn:scan/pech_wand` | scan key | `ConfigResearch.java:352 (ScanGeneric)` |
| `!TOTEMUNDYING` | `thaumcraft_reborn:scan/totem_undying` | scan key | `ConfigResearch.java:333 (ScanGeneric)` |
| `!TaintCrawler` | `thaumcraft_reborn:scan/taint_crawler` | scan key | `ConfigResearch.java:261 (ScanEntity)` |
| `!TaintSeed` | `thaumcraft_reborn:scan/taint_seed` | scan key | `ConfigResearch.java:263 (ScanEntity)` |
| `!TaintSwarm` | `thaumcraft_reborn:scan/taint_swarm` | scan key | `ConfigResearch.java:264 (ScanEntity)` |
| `!Taintacle` | `thaumcraft_reborn:scan/taintacle` | scan key | `ConfigResearch.java:262 (ScanEntity)` |
| `!ThaumSlime` | `thaumcraft_reborn:scan/thaum_slime` | scan key | `ConfigResearch.java:253 (ScanEntity)` |
| `!Wisp` | `thaumcraft_reborn:scan/wisp` | scan key | `ConfigResearch.java:252 (ScanEntity)` |
| `!aer` | `thaumcraft_reborn:scan/aspect/aer` | aspect scan key | `GuiResearchPage.java:492` |
| `!alkimia` | `thaumcraft_reborn:scan/aspect/alkimia` | aspect scan key | `GuiResearchPage.java:492` |
| `!auram` | `thaumcraft_reborn:scan/aspect/auram` | aspect scan key | `GuiResearchPage.java:492` |
| `!aversio` | `thaumcraft_reborn:scan/aspect/aversio` | aspect scan key | `GuiResearchPage.java:492` |
| `!bestia` | `thaumcraft_reborn:scan/aspect/bestia` | aspect scan key | `GuiResearchPage.java:492` |
| `!cognitio` | `thaumcraft_reborn:scan/aspect/cognitio` | aspect scan key | `GuiResearchPage.java:492` |
| `!desiderium` | `thaumcraft_reborn:scan/aspect/desiderium` | aspect scan key | `GuiResearchPage.java:492` |
| `!fabrico` | `thaumcraft_reborn:scan/aspect/fabrico` | aspect scan key | `GuiResearchPage.java:492` |
| `!gelum` | `thaumcraft_reborn:scan/aspect/gelum` | aspect scan key | `GuiResearchPage.java:492` |
| `!gotcrystals` | `thaumcraft_reborn:flag/got_crystals` | event flag | `PlayerEvents.java:137-139` |
| `!gotdream` | `thaumcraft_reborn:flag/got_dream` | event flag | `PlayerEvents.java:141-166` |
| `!gotthaumonomicon` | `thaumcraft_reborn:flag/got_thaumonomicon` | event flag | `PlayerEvents.java:146-148` |
| `!herba` | `thaumcraft_reborn:scan/aspect/herba` | aspect scan key | `GuiResearchPage.java:492` |
| `!ignis` | `thaumcraft_reborn:scan/aspect/ignis` | aspect scan key | `GuiResearchPage.java:492` |
| `!instrumentum` | `thaumcraft_reborn:scan/aspect/instrumentum` | aspect scan key | `GuiResearchPage.java:492` |
| `!lux` | `thaumcraft_reborn:scan/aspect/lux` | aspect scan key | `GuiResearchPage.java:492` |
| `!machina` | `thaumcraft_reborn:scan/aspect/machina` | aspect scan key | `GuiResearchPage.java:492` |
| `!mortuus` | `thaumcraft_reborn:scan/aspect/mortuus` | aspect scan key | `GuiResearchPage.java:492` |
| `!motus` | `thaumcraft_reborn:scan/aspect/motus` | aspect scan key | `GuiResearchPage.java:492` |
| `!perditio` | `thaumcraft_reborn:scan/aspect/perditio` | aspect scan key | `GuiResearchPage.java:492` |
| `!permutatio` | `thaumcraft_reborn:scan/aspect/permutatio` | aspect scan key | `GuiResearchPage.java:492` |
| `!potentia` | `thaumcraft_reborn:scan/aspect/potentia` | aspect scan key | `GuiResearchPage.java:492` |
| `!praecantatio` | `thaumcraft_reborn:scan/aspect/praecantatio` | aspect scan key | `GuiResearchPage.java:492` |
| `!praemunio` | `thaumcraft_reborn:scan/aspect/praemunio` | aspect scan key | `GuiResearchPage.java:492` |
| `!sensus` | `thaumcraft_reborn:scan/aspect/sensus` | aspect scan key | `GuiResearchPage.java:492` |
| `!terra` | `thaumcraft_reborn:scan/aspect/terra` | aspect scan key | `GuiResearchPage.java:492` |
| `!vacuos` | `thaumcraft_reborn:scan/aspect/vacuos` | aspect scan key | `GuiResearchPage.java:492` |
| `!victus` | `thaumcraft_reborn:scan/aspect/victus` | aspect scan key | `GuiResearchPage.java:492` |
| `!vinculum` | `thaumcraft_reborn:scan/aspect/vinculum` | aspect scan key | `GuiResearchPage.java:492` |
| `!vitium` | `thaumcraft_reborn:scan/aspect/vitium` | aspect scan key | `GuiResearchPage.java:492` |
| `!volatus` | `thaumcraft_reborn:scan/aspect/volatus` | aspect scan key | `GuiResearchPage.java:492` |

Conversión no equivalente pendiente de decisión/Stage C (no se inventaron componentes): los NBT legacy de `crystal_essence`, `phial` y `enchanted_placeholder`; IDs sin fila en `docs/id-mapping.md` (`thaumcraft:leather`, `thaumcraft:metal`, `thaumcraft:nitor`, `thaumcraft:arcane_stone`); el mapeo del oredict `chest` se usa como `c:chests`; y los iconos legacy `focus:thaumcraft.*` se conservan como referencias `ItemReference` no resueltas tras normalizar el path a minúsculas, sin afirmar que sean ítems equivalentes. Los detalles por ocurrencia están en `/home/ubuntu/stage-b/convert/research-conversion.tsv`.

## `registerComplexObjectTag`: valores finales del runtime TC6

**Decisión:** las 94 llamadas se representan con valores finales explícitos; no hay derivación de recetas en runtime ni API de derivación. Se capturaron en un servidor real Java 8 con TC6 6.1.BETA26, Forge 1.12.2-14.23.5.2860 y Baubles 1.12-1.5.2. El valor estático es una copia de `CommonInternals.objectTags` obtenida con la cadena de claves de `getObjectTags` (ID exacto, metadata 32767, metadata 0–15 para wildcard, ID stripped y stripped+32767), antes de consultar APIs públicas. No se ejecuta el fallback `generateTags`, `getBonusTags` ni el cap. Se emitieron 94 IDs vanilla; 10 llamadas a contenido TC6 se difieren a Stage C. Tres targets vanilla no produjeron resultado raw (un item con raw nulo, `tripwire` sin item resoluble y `gear*` sin miembro resoluble) y se dejaron sin emitir. No se adivinaron valores. El dump completo y la tabla de cada llamada están en `/home/ubuntu/stage-b/convert/report.md`; los JSONL permanecen fuera del repositorio.

Se corrigieron **58 valores** del baseline de 337 mappings item con su valor raw único. La discrepancia 59 es la colisión de flattening `minecraft:dead_bush`: `minecraft:deadbush@0` da `herba:5, perditio:1` y `minecraft:tallgrass@0` da `herba:5, aer:1`; se conserva la última asignación de TC6, `herba:5, perditio:1`. Las 43 entradas tag se mantienen sin cambios. El informe externo enumera cada valor previo, el raw observado y su diagnóstico.

### Capa generada TC6, congelada desde runtime

Después del inicio del servidor, el dumper llamó `ThaumcraftCraftingManager.generateTags(stack)` para cada stack cuyo lookup raw registrado era nulo. Restauró el snapshot de `CommonInternals.objectTags` antes de cada llamada y guardó el resultado devuelto por `generateTags`, sin consultar la API pública ni implementar derivación en runtime. Los resultados vanilla no vacíos se aplanaron y congelaron en `aspect_mappings/tc6_vanilla_generated.json`: **208 entries**, disjuntas de los **430 targets item** de `tc6_vanilla_items.json` (el conversor comprueba la disjunción). `minecraft:stone_pickaxe` es un ejemplo generado: `terra:11, perditio:2, herba:1`.

De 530 consultas generadas por raw nulo en el dump completo, 296 eran stacks vanilla; 208 tuvieron resultado no vacío y se representaron como item moderno único, y 88 quedaron vacías. No hubo colisiones ni targets vanilla generados no clasificables. Los 74 IDs que sólo existen en 26.3 carecen de stack legacy y reciben deliberadamente **ningún valor generado**. Para mantenerlos fuera de la herencia de tags se conserva un override exacto vacío `{}`. De los dos targets legacy restantes sin raw, `minecraft:dirt@1` se aplanó a `minecraft:coarse_dirt` y se trasladó a la capa generada con `terra:3`; `minecraft:monster_egg@1` se aplanó a `minecraft:infested_cobblestone` y no produjo ni raw ni resultado generado. Ambos stacks estaban presentes en el dump; no hubo stacks requeridos omitidos ni casos sin clasificar.

Las 43 tags abarcan 153 apariciones de miembros vanilla en 26.3. Todas están cubiertas por un item exacto en una de las dos capas, por lo que ningún mapping tag tiene un miembro vanilla efectivo: las tags conservadas sirven sólo a miembros de otros mods no presentes en este runtime.

### Capa de bonus (preparada, inerte en C; AD-13, Etapa E)

La API preparada en C separa `getBaseAspects` de la resolución final mediante `AspectBonusProvider`, `AspectBonusProviders` y `AspectLimiter` (AD-13). En C no se registra ningún proveedor ni se sustituye el limiter `IDENTITY`; el comportamiento visible sigue siendo el del dataset base.

`getBonusTags` (TC6 `ThaumcraftCraftingManager.java:194+`) es una capa por stack separada del registro: los `IEssentiaContainerItem` pueden sustituir la lista base por los aspectos contenidos; armadura agrega `praemunio` según protección; espada agrega `aversio` según daño; arco agrega `aversio` y `volatus`; herramientas, shears y hoes agregan `instrumentum` según material o durabilidad; los miembros dye-oredict agregan `sensus`; y los encantamientos agregan aspectos según la tabla de TC6. Después `AspectHelper.cullTags` limita a siete aspectos y `getObjectTags` limita cada cantidad a 500. No hay una rama específica de pociones: las 54 filas de poción que difieren entre raw y la consulta pública reflejan el culling general. De las 278 filas raw/public distintas, clasificación primaria: armor 12, sword 2, tool 0, bow 1, essentia container 75, enchantment 87, potion 0 y other 101 (incluye 54 filas de poción, 16 de dye-oredict y 31 restantes). La implementación de los proveedores y el limiter TC6 queda para la Etapa E.

| Línea | Target TC6 (expresión de fuente) | Parte explícita |
|---:|---|---|
| 262 | `new ItemStack(Blocks.field_192443_dR, 1, 32767)` | `new AspectList(new ItemStack(Blocks.field_192444_dS)).add(Aspect.WATER, 1).add(Aspect.ORDER, 1)` |
| 363 | `new ItemStack(Blocks.field_150440_ba, 1, 32767)` | `new AspectList().add(Aspect.PLANT, 10).remove(Aspect.LIFE, 5)` |
| 369 | `new ItemStack(Items.field_151102_aT)` | `new AspectList().add(Aspect.DESIRE, 1).add(Aspect.ENERGY, 1)` |
| 370 | `new ItemStack(Items.field_151105_aU)` | `new AspectList().add(Aspect.DESIRE, 1).add(Aspect.LIFE, 2)` |
| 371 | `new ItemStack(Items.field_151158_bO)` | `new AspectList().add(Aspect.DESIRE, 1).add(Aspect.LIFE, 2)` |
| 397 | `new ItemStack(Items.field_151106_aX, 1, 32767)` | `new AspectList().add(Aspect.DESIRE, 1)` |
| 399 | `new ItemStack(Blocks.field_150478_aa, 1, 32767)` | `new AspectList().add(Aspect.LIGHT, 5)` |
| 485 | `new ItemStack(Items.field_151066_bu)` | `new AspectList().add(Aspect.ALCHEMY, 15)` |
| 486 | `new ItemStack(Items.field_151071_bq)` | `new AspectList().add(Aspect.ALCHEMY, 5)` |
| 487 | `new ItemStack(Items.field_151065_br)` | `new AspectList().add(Aspect.ALCHEMY, 5)` |
| 488 | `new ItemStack(Items.field_151060_bw)` | `new AspectList().add(Aspect.ALCHEMY, 5)` |
| 489 | `new ItemStack(Items.field_151064_bs)` | `new AspectList().add(Aspect.ALCHEMY, 5)` |
| 550 | `new ItemStack(Items.field_151121_aF)` | `new AspectList().add(Aspect.MIND, 2)` |
| 551 | `new ItemStack(Items.field_151134_bR)` | `new AspectList(new ItemStack(Items.field_151122_aG))` |
| 552 | `new ItemStack(Blocks.field_150342_X)` | `new AspectList().add(Aspect.MIND, 8)` |
| 575 | `new ItemStack(Blocks.field_150486_ae, 1, 32767)` | `new AspectList().add(Aspect.VOID, 15)` |
| 576 | `new ItemStack(Blocks.field_150447_bR, 1, 32767)` | `new AspectList().add(Aspect.TRAP, 10)` |
| 577 | `new ItemStack(Items.field_151061_bv)` | `new AspectList().add(Aspect.SENSES, 10).add(Aspect.MAGIC, 5)` |
| 578 | `new ItemStack(Items.field_151032_g)` | `new AspectList().add(Aspect.AVERSION, 5)` |
| 579 | `new ItemStack(Items.field_151069_bo)` | `new AspectList().add(Aspect.VOID, 5)` |
| 580 | `new ItemStack(Items.field_151153_ao, 1, 0)` | `new AspectList().add(Aspect.MAGIC, 5).add(Aspect.LIFE, 10)` |
| 581 | `new ItemStack(Items.field_151153_ao, 1, 1)` | `new AspectList().add(Aspect.MAGIC, 5).add(Aspect.LIFE, 15).add(Aspect.PROTECT, 15)` |
| 584 | `new ItemStack(Items.field_151054_z)` | `new AspectList().add(Aspect.VOID, 5)` |
| 585 | `new ItemStack(Items.field_151009_A)` | `new AspectList().add(Aspect.LIFE, 5)` |
| 586 | `new ItemStack(Items.field_151143_au)` | `new AspectList().add(Aspect.MOTION, 15)` |
| 587 | `new ItemStack(Items.field_151139_aw)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 588 | `new ItemStack(Items.field_179572_au)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 589 | `new ItemStack(Items.field_179571_av)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 590 | `new ItemStack(Items.field_179567_at)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 591 | `new ItemStack(Items.field_179570_aq)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 592 | `new ItemStack(Items.field_179569_ar)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 593 | `new ItemStack(Items.field_179568_as)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 594 | `new ItemStack(Items.field_151124_az)` | `new AspectList().add(Aspect.WATER, 10).add(Aspect.MOTION, 15)` |
| 595 | `new ItemStack(Items.field_185153_aK)` | `new AspectList().add(Aspect.WATER, 10).add(Aspect.MOTION, 15)` |
| 596 | `new ItemStack(Items.field_185151_aI)` | `new AspectList().add(Aspect.WATER, 10).add(Aspect.MOTION, 15)` |
| 597 | `new ItemStack(Items.field_185154_aL)` | `new AspectList().add(Aspect.WATER, 10).add(Aspect.MOTION, 15)` |
| 598 | `new ItemStack(Items.field_185152_aJ)` | `new AspectList().add(Aspect.WATER, 10).add(Aspect.MOTION, 15)` |
| 599 | `new ItemStack(Items.field_185150_aH)` | `new AspectList().add(Aspect.WATER, 10).add(Aspect.MOTION, 15)` |
| 600 | `new ItemStack(Items.field_151033_d, 1, 32767)` | `new AspectList().add(Aspect.FIRE, 10).add(Aspect.TOOL, 5)` |
| 601 | `new ItemStack(Items.field_151112_aM, 1, 32767)` | `new AspectList().add(Aspect.WATER, 10).add(Aspect.TOOL, 5)` |
| 602 | `new ItemStack(Items.field_185159_cQ, 1, 32767)` | `new AspectList().add(Aspect.PROTECT, 20)` |
| 609 | `sis` | `new AspectList().merge(Aspect.PROTECT, 20)` |
| 612 | `new ItemStack(Items.field_185166_h, 1, 32767)` | `new AspectList().add(Aspect.SENSES, 10).add(Aspect.MAGIC, 5)` |
| 613 | `new ItemStack(Items.field_151133_ar)` | `new AspectList().add(Aspect.VOID, 5)` |
| 623 | `new ItemStack(Items.field_151067_bt)` | `new AspectList().add(Aspect.CRAFT, 15).add(Aspect.ALCHEMY, 25)` |
| 624 | `new ItemStack(Blocks.field_150430_aB)` | `new AspectList().add(Aspect.MECHANISM, 5)` |
| 625 | `new ItemStack(Blocks.field_150448_aq, 1, 32767)` | `new AspectList().add(Aspect.MOTION, 10)` |
| 626 | `new ItemStack(Blocks.field_150319_E, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.SENSES, 1)` |
| 627 | `new ItemStack(Blocks.field_150318_D, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.ENERGY, 1)` |
| 628 | `new ItemStack(Blocks.field_150408_cc, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5)` |
| 629 | `new ItemStack(Blocks.field_180387_bt, 1, 32767)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 630 | `new ItemStack(Blocks.field_180385_bs, 1, 32767)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 631 | `new ItemStack(Blocks.field_180386_br, 1, 32767)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 632 | `new ItemStack(Blocks.field_180385_bs, 1, 32767)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 633 | `new ItemStack(Blocks.field_180391_bp, 1, 32767)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 634 | `new ItemStack(Blocks.field_180392_bq, 1, 32767)` | `new AspectList().add(Aspect.TRAP, 5).add(Aspect.MECHANISM, 5)` |
| 635 | `new ItemStack(Blocks.field_150452_aw, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.SENSES, 5)` |
| 636 | `new ItemStack(Blocks.field_150456_au, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.SENSES, 5)` |
| 637 | `new ItemStack(Blocks.field_150445_bS, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.SENSES, 5)` |
| 638 | `new ItemStack(Blocks.field_150443_bT, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.SENSES, 5)` |
| 639 | `new ItemStack(Blocks.field_150442_at, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5)` |
| 640 | `new ItemStack(Blocks.field_150331_J, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.MOTION, 10)` |
| 641 | `new ItemStack(Blocks.field_150320_F, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.MOTION, 10)` |
| 642 | `new ItemStack(Blocks.field_150421_aI)` | `new AspectList().add(Aspect.SENSES, 20).add(Aspect.MECHANISM, 10).add(Aspect.AIR, 15)` |
| 645 | `new ItemStack(Blocks.field_150323_B)` | `new AspectList().add(Aspect.SENSES, 20).add(Aspect.MECHANISM, 10).add(Aspect.AIR, 15)` |
| 648 | `new ItemStack(Blocks.field_150415_aT, 1, 32767)` | `new AspectList().add(Aspect.MOTION, 5)` |
| 649 | `new ItemStack(Blocks.field_150460_al, 1, 32767)` | `new AspectList().add(Aspect.FIRE, 10)` |
| 650 | `new ItemStack(Blocks.field_150381_bn)` | `new AspectList().add(Aspect.MAGIC, 25).add(Aspect.CRAFT, 15)` |
| 651 | `new ItemStack(Blocks.field_150462_ai)` | `new AspectList().add(Aspect.CRAFT, 20)` |
| 652 | `new ItemStack(Items.field_151113_aN)` | `new AspectList().add(Aspect.MECHANISM, 10)` |
| 653 | `new ItemStack(Blocks.field_150461_bJ)` | `new AspectList().add(Aspect.AURA, 10).add(Aspect.MAGIC, 10).add(Aspect.EXCHANGE, 10)` |
| 656 | `new ItemStack(Blocks.field_150471_bO, 1, 32767)` | `new AspectList().add(Aspect.MECHANISM, 5)` |
| 657 | `new ItemStack(Items.field_151146_bM, 1, 32767)` | `new AspectList().add(Aspect.MOTION, 5).add(Aspect.DESIRE, 10)` |
| 658 | `new ItemStack(Items.field_151162_bE)` | `new AspectList().add(Aspect.VOID, 5).add(Aspect.PLANT, 5)` |
| 659 | `new ItemStack(Items.field_151150_bK)` | `new AspectList().add(Aspect.SENSES, 10).add(Aspect.ALCHEMY, 5)` |
| 660 | `new ItemStack(Blocks.field_150477_bB, 1, 32767)` | `new AspectList().merge(Aspect.EXCHANGE, 10).merge(Aspect.MOTION, 10).merge(Aspect.VOID, 20)` |
| 663 | `new ItemStack(Items.field_151132_bS, 1, 32767)` | `new AspectList().merge(Aspect.MECHANISM, 15).merge(Aspect.ORDER, 5).merge(Aspect.SENSES, 5)` |
| 666 | `new ItemStack(Items.field_151107_aW, 1, 32767)` | `new AspectList().merge(Aspect.MECHANISM, 15).merge(Aspect.ENERGY, 10)` |
| 669 | `new ItemStack(Blocks.field_150438_bZ, 1, 32767)` | `new AspectList().merge(Aspect.MECHANISM, 5).merge(Aspect.EXCHANGE, 10).merge(Aspect.VOID, 5)` |
| 672 | `new ItemStack(Blocks.field_150409_cd, 1, 32767)` | `new AspectList().merge(Aspect.MECHANISM, 5).merge(Aspect.EXCHANGE, 10).merge(Aspect.VOID, 5)` |
| 675 | `new ItemStack(Blocks.field_150367_z, 1, 32767)` | `new AspectList().merge(Aspect.MECHANISM, 5).merge(Aspect.EXCHANGE, 10).merge(Aspect.VOID, 5)` |
| 681 | `new ItemStack(Blocks.field_150473_bD, 1, 32767)` | `new AspectList().merge(Aspect.SENSES, 5).merge(Aspect.MECHANISM, 5).merge(Aspect.TRAP, 5)` |
| 684 | `new ItemStack(Blocks.field_150453_bW, 1, 32767)` | `new AspectList().merge(Aspect.SENSES, 10).merge(Aspect.LIGHT, 10).merge(Aspect.MECHANISM, 5)` |
| 687 | `"gear*"` | `new AspectList().add(Aspect.MECHANISM, 5)` |
| 712 | `new ItemStack(BlocksTC.tableWood)` | `new AspectList().add(Aspect.TOOL, 1)` |
| 713 | `new ItemStack(BlocksTC.tableStone)` | `new AspectList().add(Aspect.TOOL, 1)` |
| 793 | `new ItemStack(ca)` | `new AspectList().add(Aspect.LIGHT, 5)` |
| 799 | `new ItemStack(BlocksTC.pedestalArcane, 1, 0)` | `new AspectList().add(Aspect.MAGIC, 3).add(Aspect.AIR, 3)` |
| 800 | `new ItemStack(BlocksTC.pedestalAncient, 1, 1)` | `new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ELDRITCH, 3)` |
| 801 | `new ItemStack(BlocksTC.pedestalEldritch, 1, 2)` | `new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ELDRITCH, 3)` |
| 802 | `new ItemStack(ItemsTC.thaumometer, 1, 32767)` | `new AspectList().add(Aspect.SENSES, 10).add(Aspect.AURA, 10)` |
| 803 | `new ItemStack(ItemsTC.goggles, 1, 32767)` | `new AspectList().merge(Aspect.SENSES, 10).merge(Aspect.AURA, 10)` |
| 804 | `new ItemStack(BlocksTC.arcaneEar)` | `new AspectList().add(Aspect.SENSES, 20)` |
| 842 | `new ItemStack(ca)` | `new AspectList().add(Aspect.ELDRITCH, 5)` |

La tabla anterior conserva la llamada y su parte explícita TC6; los resultados raw finales por llamada y sus motivos de no emisión se documentan en `/home/ubuntu/stage-b/convert/report.md`.
