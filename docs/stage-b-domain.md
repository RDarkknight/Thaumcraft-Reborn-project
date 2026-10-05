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
| Entradas explícitas de ítem emitidas | 337 |
| Entradas de tag emitidas | 43 |
| `registerEntityTag` inspeccionados | 81 |
| Entradas de entidad emitidas | 53 (una con NBT) |
| Variantes de ítem producidas por metadata wildcard | 88 |
| IDs desconocidos en los datos emitidos | 0 |
| `registerComplexObjectTag` diferidos, no emitidos | 94 |
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

#### requiere decisión: valores finales explícitos

`registerComplexObjectTag` puede combinar aspectos derivados de recetas con una parte explícita. No se emitió ninguno de estos valores ni se evaluaron recetas; la tabla conserva cada target y sólo la operación/aspectos explícitos de TC6 para una decisión posterior.

| Línea | Target TC6/vanilla | Parte explícita (no emitida) |
|---:|---|---|
| 262 | `Blocks.field_192443_dR` (`minecraft:concrete`, metadata wildcard; 16 colores flattening) | copiar `Blocks.field_192444_dS`; sumar `aqua 1`, `ordo 1` |
| 363 | `Blocks.field_150440_ba` (`minecraft:melon_block`, wildcard) | sumar `herba 10`; quitar `victus 5` |
| 369 | `Items.field_151102_aT` (`minecraft:sugar`) | sumar `desiderium 1`, `potentia 1` |
| 370 | `Items.field_151105_aU` (`minecraft:cake`) | sumar `desiderium 1`, `victus 2` |
| 371 | `Items.field_151158_bO` (`minecraft:pumpkin_pie`) | sumar `desiderium 1`, `victus 2` |
| 397 | `Items.field_151106_aX` (`minecraft:cookie`, wildcard) | sumar `desiderium 1` |
| 399 | `Blocks.field_150478_aa` (`minecraft:torch`, wildcard) | sumar `lux 5` |
| 485 | `Items.field_151066_bu` (`minecraft:cauldron`) | sumar `alkimia 15` |
| 486 | `Items.field_151071_bq` (`minecraft:fermented_spider_eye`) | sumar `alkimia 5` |
| 487 | `Items.field_151065_br` (`minecraft:blaze_powder`) | sumar `alkimia 5` |
| 488 | `Items.field_151060_bw` (`minecraft:speckled_melon` → `minecraft:glistering_melon_slice`) | sumar `alkimia 5` |
| 489 | `Items.field_151064_bs` (`minecraft:magma_cream`) | sumar `alkimia 5` |
| 550 | `Items.field_151121_aF` (`minecraft:paper`) | sumar `cognitio 2` |
| 551 | `Items.field_151134_bR` (`minecraft:enchanted_book`) | copiar `Items.field_151122_aG` |
| 552 | `Blocks.field_150342_X` (`minecraft:bookshelf`) | sumar `cognitio 8` |
| 575 | `Blocks.field_150486_ae` (`minecraft:chest`, wildcard) | sumar `vacuos 15` |
| 576 | `Blocks.field_150447_bR` (`minecraft:trapped_chest`, wildcard) | sumar `vinculum 10` |
| 577 | `Items.field_151061_bv` (`minecraft:ender_eye`) | sumar `sensus 10`, `praecantatio 5` |
| 578 | `Items.field_151032_g` (`minecraft:arrow`) | sumar `aversio 5` |
| 579 | `Items.field_151069_bo` (`minecraft:glass_bottle`) | sumar `vacuos 5` |
| 580 | `Items.field_151153_ao` (`minecraft:golden_apple`, metadata 0) | sumar `praecantatio 5`, `victus 10` |
| 581 | `Items.field_151153_ao` (`minecraft:golden_apple`, metadata 1 → `minecraft:enchanted_golden_apple`) | sumar `praecantatio 5`, `victus 15`, `praemunio 15` |
| 584 | `Items.field_151054_z` (`minecraft:bowl`) | sumar `vacuos 5` |
| 585 | `Items.field_151009_A` (`minecraft:mushroom_stew`) | sumar `victus 5` |
| 586 | `Items.field_151143_au` (`minecraft:minecart`) | sumar `motus 15` |
| 587 | `Items.field_151139_aw` (`minecraft:iron_door`) | sumar `vinculum 5`, `machina 5` |
| 588 | `Items.field_179572_au` (`minecraft:acacia_door`) | sumar `vinculum 5`, `machina 5` |
| 589 | `Items.field_179571_av` (`minecraft:dark_oak_door`) | sumar `vinculum 5`, `machina 5` |
| 590 | `Items.field_179567_at` (`minecraft:jungle_door`) | sumar `vinculum 5`, `machina 5` |
| 591 | `Items.field_179570_aq` (`minecraft:oak_door`) | sumar `vinculum 5`, `machina 5` |
| 592 | `Items.field_179569_ar` (`minecraft:spruce_door`) | sumar `vinculum 5`, `machina 5` |
| 593 | `Items.field_179568_as` (`minecraft:birch_door`) | sumar `vinculum 5`, `machina 5` |
| 594 | `Items.field_151124_az` (`minecraft:boat` → `minecraft:oak_boat`) | sumar `aqua 10`, `motus 15` |
| 595 | `Items.field_185153_aK` (`minecraft:acacia_boat`) | sumar `aqua 10`, `motus 15` |
| 596 | `Items.field_185151_aI` (`minecraft:birch_boat`) | sumar `aqua 10`, `motus 15` |
| 597 | `Items.field_185154_aL` (`minecraft:dark_oak_boat`) | sumar `aqua 10`, `motus 15` |
| 598 | `Items.field_185152_aJ` (`minecraft:jungle_boat`) | sumar `aqua 10`, `motus 15` |
| 599 | `Items.field_185150_aH` (`minecraft:spruce_boat`) | sumar `aqua 10`, `motus 15` |
| 600 | `Items.field_151033_d` (`minecraft:flint_and_steel`, wildcard) | sumar `ignis 10`, `instrumentum 5` |
| 601 | `Items.field_151112_aM` (`minecraft:fishing_rod`, wildcard) | sumar `aqua 10`, `instrumentum 5` |
| 602 | `Items.field_185159_cQ` (`minecraft:shield`, wildcard) | sumar `praemunio 20` |
| 609 | `sis` (target construido dinámicamente) | merge `praemunio 20` |
| 612 | `Items.field_185166_h` (`minecraft:spectral_arrow`, wildcard) | sumar `sensus 10`, `praecantatio 5` |
| 613 | `Items.field_151133_ar` (`minecraft:bucket`) | sumar `vacuos 5` |
| 623 | `Items.field_151067_bt` (`minecraft:brewing_stand`) | sumar `fabrico 15`, `alkimia 25` |
| 624 | `Blocks.field_150430_aB` (`minecraft:stone_button`) | sumar `machina 5` |
| 625 | `Blocks.field_150448_aq` (`minecraft:rail`, wildcard) | sumar `motus 10` |
| 626 | `Blocks.field_150319_E` (`minecraft:detector_rail`, wildcard) | sumar `machina 5`, `sensus 1` |
| 627 | `Blocks.field_150318_D` (`minecraft:golden_rail` → `minecraft:powered_rail`, wildcard) | sumar `machina 5`, `potentia 1` |
| 628 | `Blocks.field_150408_cc` (`minecraft:activator_rail`, wildcard) | sumar `machina 5` |
| 629 | `Blocks.field_180387_bt` (`minecraft:acacia_fence_gate`, wildcard) | sumar `vinculum 5`, `machina 5` |
| 630 | `Blocks.field_180385_bs` (`minecraft:dark_oak_fence_gate`, wildcard) | sumar `vinculum 5`, `machina 5` |
| 631 | `Blocks.field_180386_br` (`minecraft:jungle_fence_gate`, wildcard) | sumar `vinculum 5`, `machina 5` |
| 632 | `Blocks.field_180385_bs` (`minecraft:dark_oak_fence_gate`, wildcard; duplicate call) | sumar `vinculum 5`, `machina 5` |
| 633 | `Blocks.field_180391_bp` (`minecraft:spruce_fence_gate`, wildcard) | sumar `vinculum 5`, `machina 5` |
| 634 | `Blocks.field_180392_bq` (`minecraft:birch_fence_gate`, wildcard) | sumar `vinculum 5`, `machina 5` |
| 635 | `Blocks.field_150452_aw` (`minecraft:wooden_pressure_plate` → `minecraft:oak_pressure_plate`, wildcard) | sumar `machina 5`, `sensus 5` |
| 636 | `Blocks.field_150456_au` (`minecraft:stone_pressure_plate`, wildcard) | sumar `machina 5`, `sensus 5` |
| 637 | `Blocks.field_150445_bS` (`minecraft:light_weighted_pressure_plate`, wildcard) | sumar `machina 5`, `sensus 5` |
| 638 | `Blocks.field_150443_bT` (`minecraft:heavy_weighted_pressure_plate`, wildcard) | sumar `machina 5`, `sensus 5` |
| 639 | `Blocks.field_150442_at` (`minecraft:lever`, wildcard) | sumar `machina 5` |
| 640 | `Blocks.field_150331_J` (`minecraft:piston`, wildcard) | sumar `machina 10`, `motus 10` |
| 641 | `Blocks.field_150320_F` (`minecraft:sticky_piston`, wildcard) | sumar `machina 10`, `motus 10` |
| 642 | `Blocks.field_150421_aI` (`minecraft:jukebox`) | sumar `sensus 20`, `machina 10`, `aer 15` |
| 645 | `Blocks.field_150323_B` (`minecraft:noteblock` → `minecraft:note_block`) | sumar `sensus 20`, `machina 10`, `aer 15` |
| 648 | `Blocks.field_150415_aT` (`minecraft:trapdoor` → `minecraft:oak_trapdoor`, wildcard) | sumar `motus 5` |
| 649 | `Blocks.field_150460_al` (`minecraft:furnace`, wildcard) | sumar `ignis 10` |
| 650 | `Blocks.field_150381_bn` (`minecraft:enchanting_table`) | sumar `praecantatio 25`, `fabrico 15` |
| 651 | `Blocks.field_150462_ai` (`minecraft:crafting_table`) | sumar `fabrico 20` |
| 652 | `Items.field_151113_aN` (`minecraft:clock`) | sumar `machina 10` |
| 653 | `Blocks.field_150461_bJ` (`minecraft:beacon`) | sumar `auram 10`, `praecantatio 10`, `permutatio 10` |
| 656 | `Blocks.field_150471_bO` (`minecraft:wooden_button` → `minecraft:oak_button`, wildcard) | sumar `machina 5` |
| 657 | `Items.field_151146_bM` (`minecraft:carrot_on_a_stick`, wildcard) | sumar `motus 5`, `desiderium 10` |
| 658 | `Items.field_151162_bE` (`minecraft:flower_pot`) | sumar `vacuos 5`, `herba 5` |
| 659 | `Items.field_151150_bK` (`minecraft:golden_carrot`) | sumar `sensus 10`, `alkimia 5` |
| 660 | `Blocks.field_150477_bB` (`minecraft:ender_chest`, wildcard) | merge `permutatio 10`, `motus 10`, `vacuos 20` |
| 663 | `Items.field_151132_bS` (`minecraft:comparator`, wildcard) | merge `machina 15`, `ordo 5`, `sensus 5` |
| 666 | `Items.field_151107_aW` (`minecraft:repeater`, wildcard) | merge `machina 15`, `potentia 10` |
| 669 | `Blocks.field_150438_bZ` (`minecraft:hopper`, wildcard) | merge `machina 5`, `permutatio 10`, `vacuos 5` |
| 672 | `Blocks.field_150409_cd` (`minecraft:dropper`, wildcard) | merge `machina 5`, `permutatio 10`, `vacuos 5` |
| 675 | `Blocks.field_150367_z` (`minecraft:dispenser`, wildcard) | merge `machina 5`, `permutatio 10`, `vacuos 5` |
| 681 | `Blocks.field_150473_bD` (`minecraft:tripwire`, wildcard) | merge `sensus 5`, `machina 5`, `vinculum 5` |
| 684 | `Blocks.field_150453_bW` (`minecraft:daylight_detector`, wildcard) | merge `sensus 10`, `lux 10`, `machina 5` |
| 687 | `gear*` (target oredict) | sumar `machina 5` |
| 712 | `BlocksTC.tableWood` (diferido a C) | sumar `instrumentum 1` |
| 713 | `BlocksTC.tableStone` (diferido a C) | sumar `instrumentum 1` |
| 793 | `ca` (target dinámico fuera del alcance vanilla) | sumar `lux 5` |
| 799 | `BlocksTC.pedestalArcane` (diferido a C) | sumar `praecantatio 3`, `aer 3` |
| 800 | `BlocksTC.pedestalAncient` (diferido a C) | sumar `praecantatio 3`, `alienis 3` |
| 801 | `BlocksTC.pedestalEldritch` (diferido a C) | sumar `praecantatio 3`, `alienis 3` |
| 802 | `ItemsTC.thaumometer` (diferido a C) | sumar `sensus 10`, `auram 10` |
| 803 | `ItemsTC.goggles` (diferido a C) | merge `sensus 10`, `auram 10` |
| 804 | `BlocksTC.arcaneEar` (diferido a C) | sumar `sensus 20` |
| 842 | `ca` (target dinámico fuera del alcance vanilla) | sumar `alienis 5` |

## Aura, vis y flux

`AuraAccess` opera sólo en servidor y sólo sobre chunks ya cargados (`getChunkNow`); un mundo cliente o chunk ausente devuelve cero/no modifica nada. Los valores de vis y flux se limitan a `[0, 32766]`; los add negativos se ignoran y los drains devuelven la cantidad realmente drenada, también en modo de simulación. `shouldPreserveAura` requiere jugador nulo o investigación `thaumcraft_reborn:aurapreserve` completa y `vis / base < 0.1`.

El attachment persistente `thaumcraft_reborn:aura` contiene `{base, vis, flux}`. No tiene initializer: su ausencia significa que el chunk aún no se generó. Al cargar un chunk ausente, se promedia el modificador de los biomas del centro y los cuatro centros vecinos, todos a Y=50. La consulta de bioma usa el resolver de ruido sin cargar chunks vecinos. El valor inicial es `base = clamp((int)(modifier * 500 * (1 + gaussian * 0.1)), 0, 500)`, `vis = base` y `flux = 0`. El client gametest guarda y reabre el mundo para comprobar que el attachment de aura se conserva.

`biome_aura` carga un archivo por tipo. `biomes` es un tag de bioma con `#` inicial; `modifier` y `aspect` son opcionales:

```json
{
  "biomes": "#thaumcraft_reborn:aura_type/forest",
  "modifier": 0.5,
  "aspect": "thaumcraft_reborn:terra"
}
```

El modificador es la media de los tipos con modificador que contienen el bioma. Si no coincide ningún tipo, o coincide un tipo sin modificador, se usa `0.5`, como fallback de TC6.

| Tipo | Modificador | Aspecto | Tag convencional opcional |
|---|---:|---|---|
| water | 0.33 | aqua | `c:is_aquatic` |
| ocean | 0.33 | aqua | `c:is_ocean` |
| river | 0.4 | aqua | `c:is_river` |
| wet | 0.4 | aqua | `c:is_wet` |
| lush | 0.5 | aqua | `c:is_lush` |
| hot | 0.33 | ignis | `c:is_hot` |
| dry | 0.125 | perditio | `c:is_dry` |
| nether | 0.125 | ignis | `c:is_nether` |
| mesa | 0.33 | ignis | `c:is_badlands` |
| spooky | 0.5 | ignis | `c:is_spooky` |
| dense | 0.4 | ordo | `c:is_dense_vegetation` |
| snowy | 0.25 | ordo | `c:is_snowy` |
| cold | 0.25 | ordo | `c:is_cold` |
| mushroom | 0.75 | ordo | `c:is_mushroom` |
| magical | 0.75 | ordo | `c:is_magical` |
| coniferous | 0.33 | terra | `c:is_tree/coniferous` |
| forest | 0.5 | terra | `c:is_forest` |
| sandy | 0.25 | terra | `c:is_sandy` |
| beach | 0.3 | terra | `c:is_beach` |
| jungle | 0.6 | terra | `c:is_jungle` |
| savanna | 0.25 | aer | `c:is_savanna` |
| mountain | 0.3 | aer | `c:is_mountain` |
| hills | 0.33 | aer | `c:is_hill` |
| plains | 0.3 | aer | `c:is_plains` |
| end | 0.125 | aer | `c:is_end` |
| sparse | 0.2 | perditio | `c:is_sparse_vegetation` |
| swamp | 0.5 | perditio | `c:is_swamp` |
| wasteland | 0.125 | perditio | `c:is_wasteland` |
| dead | 0.1 | perditio | `c:is_dead` |
| rare | — | — | `c:is_rare` |
| void | — | — | `c:is_void` |

Los 31 tags propios siguen `thaumcraft_reborn:aura_type/<tipo>` e incorporan como referencia opcional el tag convencional disponible. Se verificó en Fabric API 0.161 que todos existen; no hay ausencias que marcar como «requiere verificación». La constante Fabric `IS_CONIFEROUS_TREE` corresponde a `c:is_tree/coniferous` (no a `c:is_coniferous_tree`). RARE y VOID no tienen modificador ni aspecto, como los tipos no registrados de TC6. DRY queda en `0.125/perditio`: TC6 lo registra primero como `0.25/ignis` y después lo reemplaza en el `HashMap`.

La simulación actual sólo regenera vis cada 20 ticks de servidor, si avanzó el game time. Usa las tablas lunares TC6:

```text
phaseVis = [0.25, 0.15, 0.1, 0.05, 0, 0.05, 0.1, 0.15]
phaseMax = [0.15, 0.05, 0, -0.05, -0.15, -0.05, 0, 0.05]
target = base * (1 + phaseMax[phase])
```

Si `vis + flux < target`, se suma a `vis` el mínimo entre el déficit y `phaseVis[phase]`. `addFlux(..., showEffect)` conserva el parámetro pero aún no emite efectos; el attachment tampoco se sincroniza al cliente, por lo que HUD/thaumometer quedan para C.

| Simplificación temporal | Se completa en |
|---|---|
| Sólo regeneración lunar: sin difusión, decaimiento por exceso, comportamiento de vis bajo, propagación de flux ni rifts | D |
| Sin sincronización cliente del aura | C |
| `showEffect` no produce FX | D |
| No hay aspectos extra derivados de encantamientos, pociones o contenedores | C |

## Conocimiento y warp

`KnowledgeAccess` expone las vistas `PlayerKnowledge` y `PlayerWarp` y las mutaciones de servidor. `thaumcraft_reborn:knowledge` guarda research conocido, stage, flags y cantidades raw de conocimiento; `thaumcraft_reborn:warp` guarda warp permanente, normal, temporal y su contador. Ambos attachments son persistentes, se copian al morir y se sincronizan sólo al jugador propietario (`AttachmentSyncPredicate.targetOnly()`). Cada mutación exitosa reemplaza el attachment mediante `setAttached`.

Los tipos de conocimiento se ordenan `THEORY` y `OBSERVATION`; una unidad de progresión equivale respectivamente a 32 y 16 unidades raw. El API conserva los valores raw y devuelve el entero inferior al consultar puntos completos. Research desconocido devuelve `UNKNOWN`; una referencia `clave@N` exige alcanzar ese stage. Para los padres, una referencia con `@N` se satisface con `isResearchKnown` en ese stage; una referencia sin stage exige que el research esté completo. Las expresiones compuestas de padres de TC6 con `&&` o `||` no están soportadas por este formato y quedan pendientes de decisión.

## Datos de investigación

Los archivos se cargan desde `data/<namespace>/thaumcraft_reborn/research_categories/<ruta>.json` y `data/<namespace>/thaumcraft_reborn/research/<ruta>.json`, con `SyncedDataLoader` y codecs JSON/StreamCodec. Todos los loaders leen `ResourceLoader.REGISTRY_LOOKUP_KEY` del `SharedState` de Fabric durante el reload y decodifican con `RegistryOps.create(JsonOps.INSTANCE, lookup)`. Los campos de investigación que describen stacks usan `ItemStackTemplate`, igual que los resultados de recetas vanilla en 26.3: el reload enlaza los componentes de ítems después de ejecutar sus listeners, así que la plantilla conserva id, cantidad y parche de componentes y sólo se materializa como `ItemStack` al usarla, por ejemplo al entregar una recompensa.

Una categoría usa `sort_order`, `research_key` opcional, `formula`, `icon`, `background` y `background2` opcional. El índice ordena categorías por `sort_order` y luego por id; las entradas se ordenan por id. Las categorías presentes y sus fórmulas originales de TC6 son:

| Categoría | Orden | Fórmula |
|---|---:|---|
| `basics` | 0 | `herba 5`, `ordo 5`, `perditio 5`, `aer 5`, `ignis 5`, `terra 3`, `aqua 5` |
| `auromancy` | 1 | `auram 20`, `praecantatio 20`, `vitium 15`, `vitreus 5`, `gelum 5`, `aer 5` |
| `alchemy` | 2 | `alkimia 30`, `vitium 10`, `praecantatio 10`, `victus 5`, `aversio 5`, `desiderium 5`, `aqua 5` |
| `artifice` | 3 | `machina 10`, `fabrico 10`, `metallum 10`, `instrumentum 10`, `potentia 10`, `lux 5`, `volatus 5`, `vinculum 5`, `ignis 5` |
| `infusion` | 4 | `praecantatio 30`, `praemunio 10`, `instrumentum 10`, `vitium 5`, `fabrico 5`, `spiritus 5`, `terra 3` |
| `golemancy` | 5 | `humanus 20`, `motus 10`, `cognitio 10`, `machina 10`, `permutatio 5`, `sensus 5`, `bestia 5`, `ordo 5` |
| `eldritch` | 6 | `alienis 20`, `tenebrae 10`, `praecantatio 5`, `cognitio 5`, `vacuos 5`, `mortuus 5`, `exanimis 5`, `perditio 5` |

Una entrada se identifica por el id del archivo y requiere `name`, `category` y al menos un `stage`. Los demás campos son `icons`, `parents`, `siblings`, `location: [columna, fila]`, `meta`, `reward_item`, `reward_knowledge` y `addenda`. Un icono es `{"texture": "namespace:id"}` o `{"item": {"id": "minecraft:stone", "count": 1, "components": {}}}`. Los ítems de `reward_item` y los requisitos `required_item` usan la misma forma de plantilla; `count` y `components` son opcionales según el codec vanilla. Las etapas admiten `text`, `recipes`, `required_item`, `required_craft`, `required_knowledge`, `required_research` y `warp`; un requisito de research contiene `reference` y `icon` opcional. Un addendum contiene `text`, `recipes` y `required_research`. Las referencias aceptan `namespace:clave`, `namespace:clave@N` y el prefijo `~` para línea oculta.

Ejemplo reducido de entrada:

```json
{
  "name": "research.example",
  "category": "thaumcraft_reborn:basics",
  "icons": [{"texture": "thaumcraft_reborn:textures/research/example"}],
  "location": [0, 0],
  "meta": ["hex"],
  "reward_knowledge": [{"type": "theory", "category": "thaumcraft_reborn:basics", "amount": 1}],
  "stages": [
    {
      "text": "research.example.stage1",
      "required_item": [{"tag": "c:gems/quartz", "count": 1}],
      "required_research": [{"reference": "thaumcraft_reborn:firststeps@1"}],
      "warp": 2
    }
  ]
}
```

El índice omite y registra con error las entradas cuya categoría no existe; un padre desconocido sólo produce advertencia, pues puede ser una pseudo-investigación. Los límites de categoría se calculan desde las ubicaciones cargadas. No se incluyen entradas de investigación de producción ni JSON de TC6: la conversión de las aproximadamente 136 entradas queda pendiente de decisión.

`ResearchAccess` da acceso a categorías/entradas y a la progresión. `ResearchProgression` porta los stages y requisitos externos, completion, rewards, flags, warp y siblings recursivos. Warp mayor que uno se divide entre `PERMANENT` (cantidad menos la mitad entera) y `NORMAL` (mitad entera), como en TC6; `misc.wuss_mode` es `false` por defecto y desactiva esa concesión. Los rewards de conocimiento multiplican la cantidad declarada por la progresión de `KnowledgeType`. Las entradas `AUTOUNLOCK` se otorgan al conectar el jugador y después de un reload exitoso.

Los comandos con permiso de gamemaster son `/thaumcraft_reborn research list`, `research <targets> list|all|reset|grant <key>|revoke <key>`, `warp <targets> get|add|set <amount> [permanent|temporary]` y `knowledge <targets> get`. El warp omite el tipo para usar `NORMAL`. Debug añade `aspects list|hand|entity` y `aura get|drain_vis|add_vis|add_flux|drain_flux`. No existe comando para conceder puntos de conocimiento ni THEORY. No se envían packets de ganancia de conocimiento ni se muestran popups de interfaz; sí se guardan y sincronizan los flags de research.

| Simplificación temporal | Se completa en |
|---|---|
| Sin packets de ganancia de conocimiento ni popups de interfaz | C |

## Inserción futura de biomas

`BiomePlacementAccess.NONE` declara `providerId() == "none"` y `isAvailable() == false`. Registra cada solicitud en una vista no modificable y advierte `no biome placement provider; <biome> not placed`; no añade biomas ni introduce dependencias.

| Alternativa para la Etapa F | Estado |
|---|---|
| TerraBlender | Plan A; requiere verificación antes de F |
| Biolith | Plan B; requiere verificación antes de F |
| Mixin | Plan C; requiere verificación antes de F |

## Decisiones pendientes

- Decidir si y cómo convertir los JSON de research de TC6; no copiar entradas ni assets originales en esta etapa.
- Decidir el convenio para pseudo-keys TC6 como `!gotdream`; se propone `thaumcraft_reborn:discovery/<lowercase>`, aún sin aprobar.
- Incorporar descripciones de aspectos sólo cuando se apruebe el texto y su procedencia; esta etapa incluye únicamente los nombres capitalizados.
- Verificar y cerrar la elección del proveedor de biomas antes de la Etapa F.
