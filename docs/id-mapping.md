# Mapeo de IDs 1.12.2 → 26.3

Este inventario registra nombres y ubicaciones de registro; no copia código, modelos ni texturas. La auditoría usó `/home/ubuntu/tc/src/thaumcraft` y contrastó nombres de bloque/ítem con `/home/ubuntu/tc/src/assets/thaumcraft` (`blockstates/`, `models/block/`, `models/item/`, `textures/items/`). Los assets sólo se consultaron localmente y no se incorporaron al proyecto.

Las filas agrupan variantes generadas en un mismo punto de registro; cada fila conserva la línea de registro. En las llamadas al helper `registerBlock`, el helper también registra un `ItemBlock` con el mismo ID (`ConfigBlocks.java:465–474`). Los block items se anotan en la sección Blocks. Para items con metadata, los nombres del constructor se expanden a `<base>_<variante>` en el orden declarado.

## Blocks

**Recuento:** 155 sitios/familias documentados, 200 IDs distintos de bloque listados (219 blockstates y 119 modelos de bloque presentes en el árbol de assets de referencia); 0 llamadas de registro quedaron sin ID. Los IDs pueden superar los sitios por los bucles de colores.

| ID 1.12.2 | ID nuevo | Tipo | Origen | Notas |
|---|---|---|---|---|
| `thaumcraft:ore_amber` | `thaumcraft_reborn:ore_amber` | Block + block item | `ConfigBlocks.java:172` | `blockstates/ore_amber.json` |
| `thaumcraft:ore_cinnabar` | `thaumcraft_reborn:ore_cinnabar` | Block + block item | `ConfigBlocks.java:173` | `blockstates/ore_cinnabar.json` |
| `thaumcraft:ore_quartz` | `thaumcraft_reborn:ore_quartz` | Block + block item | `ConfigBlocks.java:174` | `blockstates/ore_quartz.json` |
| `thaumcraft:crystal_aer` | `thaumcraft_reborn:crystal_aer` | Block + block item | `ConfigBlocks.java:175` | `blockstates/crystal_aer.json` |
| `thaumcraft:crystal_ignis` | `thaumcraft_reborn:crystal_ignis` | Block + block item | `ConfigBlocks.java:176` | `blockstates/crystal_ignis.json` |
| `thaumcraft:crystal_aqua` | `thaumcraft_reborn:crystal_aqua` | Block + block item | `ConfigBlocks.java:177` | `blockstates/crystal_aqua.json` |
| `thaumcraft:crystal_terra` | `thaumcraft_reborn:crystal_terra` | Block + block item | `ConfigBlocks.java:178` | `blockstates/crystal_terra.json` |
| `thaumcraft:crystal_ordo` | `thaumcraft_reborn:crystal_ordo` | Block + block item | `ConfigBlocks.java:179` | `blockstates/crystal_ordo.json` |
| `thaumcraft:crystal_perditio` | `thaumcraft_reborn:crystal_perditio` | Block + block item | `ConfigBlocks.java:180` | `blockstates/crystal_perditio.json` |
| `thaumcraft:crystal_vitium` | `thaumcraft_reborn:crystal_vitium` | Block + block item | `ConfigBlocks.java:181` | `blockstates/crystal_vitium.json` |
| `thaumcraft:stone_arcane` | `thaumcraft_reborn:stone_arcane` | Block + block item | `ConfigBlocks.java:189` | `blockstates/stone_arcane.json` |
| `thaumcraft:stone_arcane_brick` | `thaumcraft_reborn:stone_arcane_brick` | Block + block item | `ConfigBlocks.java:190` | `blockstates/stone_arcane_brick.json` |
| `thaumcraft:stone_ancient` | `thaumcraft_reborn:stone_ancient` | Block + block item | `ConfigBlocks.java:191` | `blockstates/stone_ancient.json` |
| `thaumcraft:stone_ancient_tile` | `thaumcraft_reborn:stone_ancient_tile` | Block + block item | `ConfigBlocks.java:192` | `blockstates/stone_ancient_tile.json` |
| `thaumcraft:stone_ancient_rock` | `thaumcraft_reborn:stone_ancient_rock` | Block + block item | `ConfigBlocks.java:193` | `blockstates/stone_ancient_rock.json` |
| `thaumcraft:stone_ancient_glyphed` | `thaumcraft_reborn:stone_ancient_glyphed` | Block + block item | `ConfigBlocks.java:194` | `blockstates/stone_ancient_glyphed.json` |
| `thaumcraft:stone_ancient_doorway` | `thaumcraft_reborn:stone_ancient_doorway` | Block + block item | `ConfigBlocks.java:195` | `blockstates/stone_ancient_doorway.json` |
| `thaumcraft:stone_eldritch_tile` | `thaumcraft_reborn:stone_eldritch_tile` | Block + block item | `ConfigBlocks.java:196` | `blockstates/stone_eldritch_tile.json` |
| `thaumcraft:stone_porous` | `thaumcraft_reborn:stone_porous` | Block + block item | `ConfigBlocks.java:197` | `blockstates/stone_porous.json` |
| `thaumcraft:stairs_arcane` | `thaumcraft_reborn:stairs_arcane` | Block + block item | `ConfigBlocks.java:198` | `blockstates/stairs_arcane.json` |
| `thaumcraft:stairs_arcane_brick` | `thaumcraft_reborn:stairs_arcane_brick` | Block + block item | `ConfigBlocks.java:199` | `blockstates/stairs_arcane_brick.json` |
| `thaumcraft:stairs_ancient` | `thaumcraft_reborn:stairs_ancient` | Block + block item | `ConfigBlocks.java:200` | `blockstates/stairs_ancient.json` |
| `thaumcraft:sapling_greatwood` | `thaumcraft_reborn:sapling_greatwood` | Block + block item | `ConfigBlocks.java:241` | `blockstates/sapling_greatwood.json` |
| `thaumcraft:sapling_silverwood` | `thaumcraft_reborn:sapling_silverwood` | Block + block item | `ConfigBlocks.java:242` | `blockstates/sapling_silverwood.json` |
| `thaumcraft:log_greatwood` | `thaumcraft_reborn:log_greatwood` | Block + block item | `ConfigBlocks.java:243` | `blockstates/log_greatwood.json` |
| `thaumcraft:log_silverwood` | `thaumcraft_reborn:log_silverwood` | Block + block item | `ConfigBlocks.java:244` | `blockstates/log_silverwood.json` |
| `thaumcraft:leaves_greatwood` | `thaumcraft_reborn:leaves_greatwood` | Block + block item | `ConfigBlocks.java:245` | `blockstates/leaves_greatwood.json` |
| `thaumcraft:leaves_silverwood` | `thaumcraft_reborn:leaves_silverwood` | Block + block item | `ConfigBlocks.java:246` | `blockstates/leaves_silverwood.json` |
| `thaumcraft:shimmerleaf` | `thaumcraft_reborn:shimmerleaf` | Block + block item | `ConfigBlocks.java:247` | `blockstates/shimmerleaf.json` |
| `thaumcraft:cinderpearl` | `thaumcraft_reborn:cinderpearl` | Block + block item | `ConfigBlocks.java:248` | `blockstates/cinderpearl.json` |
| `thaumcraft:vishroom` | `thaumcraft_reborn:vishroom` | Block + block item | `ConfigBlocks.java:249` | `blockstates/vishroom.json` |
| `thaumcraft:plank_greatwood` | `thaumcraft_reborn:plank_greatwood` | Block + block item | `ConfigBlocks.java:250` | `blockstates/plank_greatwood.json` |
| `thaumcraft:plank_silverwood` | `thaumcraft_reborn:plank_silverwood` | Block + block item | `ConfigBlocks.java:251` | `blockstates/plank_silverwood.json` |
| `thaumcraft:stairs_greatwood` | `thaumcraft_reborn:stairs_greatwood` | Block + block item | `ConfigBlocks.java:252` | `blockstates/stairs_greatwood.json` |
| `thaumcraft:stairs_silverwood` | `thaumcraft_reborn:stairs_silverwood` | Block + block item | `ConfigBlocks.java:253` | `blockstates/stairs_silverwood.json` |
| `thaumcraft:amber_block` | `thaumcraft_reborn:amber_block` | Block + block item | `ConfigBlocks.java:276` | `blockstates/amber_block.json` |
| `thaumcraft:amber_brick` | `thaumcraft_reborn:amber_brick` | Block + block item | `ConfigBlocks.java:277` | `blockstates/amber_brick.json` |
| `thaumcraft:flesh_block` | `thaumcraft_reborn:flesh_block` | Block + block item | `ConfigBlocks.java:278` | `blockstates/flesh_block.json` |
| `thaumcraft:loot_crate_common` | `thaumcraft_reborn:loot_crate_common` | Block + block item | `ConfigBlocks.java:279` | `blockstates/loot_crate_common.json` |
| `thaumcraft:loot_crate_uncommon` | `thaumcraft_reborn:loot_crate_uncommon` | Block + block item | `ConfigBlocks.java:280` | `blockstates/loot_crate_uncommon.json` |
| `thaumcraft:loot_crate_rare` | `thaumcraft_reborn:loot_crate_rare` | Block + block item | `ConfigBlocks.java:281` | `blockstates/loot_crate_rare.json` |
| `thaumcraft:loot_urn_common` | `thaumcraft_reborn:loot_urn_common` | Block + block item | `ConfigBlocks.java:282` | `blockstates/loot_urn_common.json` |
| `thaumcraft:loot_urn_uncommon` | `thaumcraft_reborn:loot_urn_uncommon` | Block + block item | `ConfigBlocks.java:283` | `blockstates/loot_urn_uncommon.json` |
| `thaumcraft:loot_urn_rare` | `thaumcraft_reborn:loot_urn_rare` | Block + block item | `ConfigBlocks.java:284` | `blockstates/loot_urn_rare.json` |
| `thaumcraft:taint_fibre` | `thaumcraft_reborn:taint_fibre` | Block + block item | `ConfigBlocks.java:285` | `blockstates/taint_fibre.json` |
| `thaumcraft:taint_crust` | `thaumcraft_reborn:taint_crust` | Block + block item | `ConfigBlocks.java:286` | `blockstates/taint_crust.json` |
| `thaumcraft:taint_soil` | `thaumcraft_reborn:taint_soil` | Block + block item | `ConfigBlocks.java:287` | `blockstates/taint_soil.json` |
| `thaumcraft:taint_rock` | `thaumcraft_reborn:taint_rock` | Block + block item | `ConfigBlocks.java:288` | `blockstates/taint_rock.json` |
| `thaumcraft:taint_geyser` | `thaumcraft_reborn:taint_geyser` | Block + block item | `ConfigBlocks.java:289` | `blockstates/taint_geyser.json` |
| `thaumcraft:taint_feature` | `thaumcraft_reborn:taint_feature` | Block + block item | `ConfigBlocks.java:290` | `blockstates/taint_feature.json` |
| `thaumcraft:taint_log` | `thaumcraft_reborn:taint_log` | Block + block item | `ConfigBlocks.java:291` | `blockstates/taint_log.json` |
| `thaumcraft:grass_ambient` | `thaumcraft_reborn:grass_ambient` | Block + block item | `ConfigBlocks.java:292` | `blockstates/grass_ambient.json` |
| `thaumcraft:table_wood` | `thaumcraft_reborn:table_wood` | Block + block item | `ConfigBlocks.java:293` | `blockstates/table_wood.json` |
| `thaumcraft:table_stone` | `thaumcraft_reborn:table_stone` | Block + block item | `ConfigBlocks.java:294` | `blockstates/table_stone.json` |
| `thaumcraft:pedestal_arcane` | `thaumcraft_reborn:pedestal_arcane` | Block + block item | `ConfigBlocks.java:295` | `blockstates/pedestal_arcane.json` |
| `thaumcraft:pedestal_ancient` | `thaumcraft_reborn:pedestal_ancient` | Block + block item | `ConfigBlocks.java:296` | `blockstates/pedestal_ancient.json` |
| `thaumcraft:pedestal_eldritch` | `thaumcraft_reborn:pedestal_eldritch` | Block + block item | `ConfigBlocks.java:297` | `blockstates/pedestal_eldritch.json` |
| `thaumcraft:metal_brass` | `thaumcraft_reborn:metal_brass` | Block + block item | `ConfigBlocks.java:298` | `blockstates/metal_brass.json` |
| `thaumcraft:metal_thaumium` | `thaumcraft_reborn:metal_thaumium` | Block + block item | `ConfigBlocks.java:299` | `blockstates/metal_thaumium.json` |
| `thaumcraft:metal_void` | `thaumcraft_reborn:metal_void` | Block + block item | `ConfigBlocks.java:300` | `blockstates/metal_void.json` |
| `thaumcraft:metal_alchemical` | `thaumcraft_reborn:metal_alchemical` | Block + block item | `ConfigBlocks.java:301` | `blockstates/metal_alchemical.json` |
| `thaumcraft:metal_alchemical_advanced` | `thaumcraft_reborn:metal_alchemical_advanced` | Block + block item | `ConfigBlocks.java:302` | `blockstates/metal_alchemical_advanced.json` |
| `thaumcraft:paving_stone_travel` | `thaumcraft_reborn:paving_stone_travel` | Block + block item | `ConfigBlocks.java:303` | `blockstates/paving_stone_travel.json` |
| `thaumcraft:paving_stone_barrier` | `thaumcraft_reborn:paving_stone_barrier` | Block + block item | `ConfigBlocks.java:304` | `blockstates/paving_stone_barrier.json` |
| `thaumcraft:pillar_arcane` | `thaumcraft_reborn:pillar_arcane` | Block + block item | `ConfigBlocks.java:305` | `blockstates/pillar_arcane.json` |
| `thaumcraft:pillar_ancient` | `thaumcraft_reborn:pillar_ancient` | Block + block item | `ConfigBlocks.java:306` | `blockstates/pillar_ancient.json` |
| `thaumcraft:pillar_eldritch` | `thaumcraft_reborn:pillar_eldritch` | Block + block item | `ConfigBlocks.java:307` | `blockstates/pillar_eldritch.json` |
| `thaumcraft:matrix_speed` | `thaumcraft_reborn:matrix_speed` | Block + block item | `ConfigBlocks.java:308` | `blockstates/matrix_speed.json` |
| `thaumcraft:matrix_cost` | `thaumcraft_reborn:matrix_cost` | Block + block item | `ConfigBlocks.java:309` | `blockstates/matrix_cost.json` |
| `thaumcraft:candle_black`<br>`thaumcraft:candle_blue`<br>`thaumcraft:candle_brown`<br>`thaumcraft:candle_cyan`<br>`thaumcraft:candle_gray`<br>`thaumcraft:candle_green`<br>`thaumcraft:candle_lightblue`<br>`thaumcraft:candle_lime`<br>`thaumcraft:candle_magenta`<br>`thaumcraft:candle_orange`<br>`thaumcraft:candle_pink`<br>`thaumcraft:candle_purple`<br>`thaumcraft:candle_red`<br>`thaumcraft:candle_silver`<br>`thaumcraft:candle_white`<br>`thaumcraft:candle_yellow` | `thaumcraft_reborn:candle_black`<br>`thaumcraft_reborn:candle_blue`<br>`thaumcraft_reborn:candle_brown`<br>`thaumcraft_reborn:candle_cyan`<br>`thaumcraft_reborn:candle_gray`<br>`thaumcraft_reborn:candle_green`<br>`thaumcraft_reborn:candle_lightblue`<br>`thaumcraft_reborn:candle_lime`<br>`thaumcraft_reborn:candle_magenta`<br>`thaumcraft_reborn:candle_orange`<br>`thaumcraft_reborn:candle_pink`<br>`thaumcraft_reborn:candle_purple`<br>`thaumcraft_reborn:candle_red`<br>`thaumcraft_reborn:candle_silver`<br>`thaumcraft_reborn:candle_white`<br>`thaumcraft_reborn:candle_yellow` | Block + block item | `ConfigBlocks.java:312` | Familia dinámica; 16 variantes cotejadas con `blockstates/`. |
| `thaumcraft:nitor_black`<br>`thaumcraft:nitor_blue`<br>`thaumcraft:nitor_brown`<br>`thaumcraft:nitor_cyan`<br>`thaumcraft:nitor_gray`<br>`thaumcraft:nitor_green`<br>`thaumcraft:nitor_lightblue`<br>`thaumcraft:nitor_lime`<br>`thaumcraft:nitor_magenta`<br>`thaumcraft:nitor_orange`<br>`thaumcraft:nitor_pink`<br>`thaumcraft:nitor_purple`<br>`thaumcraft:nitor_red`<br>`thaumcraft:nitor_silver`<br>`thaumcraft:nitor_white`<br>`thaumcraft:nitor_yellow` | `thaumcraft_reborn:nitor_black`<br>`thaumcraft_reborn:nitor_blue`<br>`thaumcraft_reborn:nitor_brown`<br>`thaumcraft_reborn:nitor_cyan`<br>`thaumcraft_reborn:nitor_gray`<br>`thaumcraft_reborn:nitor_green`<br>`thaumcraft_reborn:nitor_lightblue`<br>`thaumcraft_reborn:nitor_lime`<br>`thaumcraft_reborn:nitor_magenta`<br>`thaumcraft_reborn:nitor_orange`<br>`thaumcraft_reborn:nitor_pink`<br>`thaumcraft_reborn:nitor_purple`<br>`thaumcraft_reborn:nitor_red`<br>`thaumcraft_reborn:nitor_silver`<br>`thaumcraft_reborn:nitor_white`<br>`thaumcraft_reborn:nitor_yellow` | Block + block item | `ConfigBlocks.java:328` | Familia dinámica; 16 variantes cotejadas con `blockstates/`. |
| `thaumcraft:vis_battery` | `thaumcraft_reborn:vis_battery` | Block + block item | `ConfigBlocks.java:331` | `blockstates/vis_battery.json` |
| `thaumcraft:inlay` | `thaumcraft_reborn:inlay` | Block + block item | `ConfigBlocks.java:332` | `blockstates/inlay.json` |
| `thaumcraft:arcane_workbench` | `thaumcraft_reborn:arcane_workbench` | Block + block item | `ConfigBlocks.java:333` | `blockstates/arcane_workbench.json` |
| `thaumcraft:arcane_workbench_charger` | `thaumcraft_reborn:arcane_workbench_charger` | Block + block item | `ConfigBlocks.java:334` | `blockstates/arcane_workbench_charger.json` |
| `thaumcraft:dioptra` | `thaumcraft_reborn:dioptra` | Block + block item | `ConfigBlocks.java:335` | `blockstates/dioptra.json` |
| `thaumcraft:research_table` | `thaumcraft_reborn:research_table` | Block + block item | `ConfigBlocks.java:336` | `blockstates/research_table.json` |
| `thaumcraft:crucible` | `thaumcraft_reborn:crucible` | Block + block item | `ConfigBlocks.java:337` | `blockstates/crucible.json` |
| `thaumcraft:arcane_ear` | `thaumcraft_reborn:arcane_ear` | Block + block item | `ConfigBlocks.java:338` | `blockstates/arcane_ear.json` |
| `thaumcraft:arcane_ear_toggle` | `thaumcraft_reborn:arcane_ear_toggle` | Block + block item | `ConfigBlocks.java:339` | `blockstates/arcane_ear_toggle.json` |
| `thaumcraft:lamp_arcane` | `thaumcraft_reborn:lamp_arcane` | Block + block item | `ConfigBlocks.java:340` | `blockstates/lamp_arcane.json` |
| `thaumcraft:lamp_fertility` | `thaumcraft_reborn:lamp_fertility` | Block + block item | `ConfigBlocks.java:341` | `blockstates/lamp_fertility.json` |
| `thaumcraft:lamp_growth` | `thaumcraft_reborn:lamp_growth` | Block + block item | `ConfigBlocks.java:342` | `blockstates/lamp_growth.json` |
| `thaumcraft:levitator` | `thaumcraft_reborn:levitator` | Block + block item | `ConfigBlocks.java:343` | `blockstates/levitator.json` |
| `thaumcraft:centrifuge` | `thaumcraft_reborn:centrifuge` | Block + block item | `ConfigBlocks.java:344` | `blockstates/centrifuge.json` |
| `thaumcraft:bellows` | `thaumcraft_reborn:bellows` | Block + block item | `ConfigBlocks.java:345` | `blockstates/bellows.json` |
| `thaumcraft:smelter_basic` | `thaumcraft_reborn:smelter_basic` | Block + block item | `ConfigBlocks.java:346` | `blockstates/smelter_basic.json` |
| `thaumcraft:smelter_thaumium` | `thaumcraft_reborn:smelter_thaumium` | Block + block item | `ConfigBlocks.java:347` | `blockstates/smelter_thaumium.json` |
| `thaumcraft:smelter_void` | `thaumcraft_reborn:smelter_void` | Block + block item | `ConfigBlocks.java:348` | `blockstates/smelter_void.json` |
| `thaumcraft:smelter_aux` | `thaumcraft_reborn:smelter_aux` | Block + block item | `ConfigBlocks.java:349` | `blockstates/smelter_aux.json` |
| `thaumcraft:smelter_vent` | `thaumcraft_reborn:smelter_vent` | Block + block item | `ConfigBlocks.java:350` | `blockstates/smelter_vent.json` |
| `thaumcraft:alembic` | `thaumcraft_reborn:alembic` | Block + block item | `ConfigBlocks.java:351` | `blockstates/alembic.json` |
| `thaumcraft:recharge_pedestal` | `thaumcraft_reborn:recharge_pedestal` | Block + block item | `ConfigBlocks.java:352` | `blockstates/recharge_pedestal.json` |
| `thaumcraft:wand_workbench` | `thaumcraft_reborn:wand_workbench` | Block + block item | `ConfigBlocks.java:353` | `blockstates/wand_workbench.json` |
| `thaumcraft:hungry_chest` | `thaumcraft_reborn:hungry_chest` | Block + block item | `ConfigBlocks.java:354` | `blockstates/hungry_chest.json` |
| `thaumcraft:tube` | `thaumcraft_reborn:tube` | Block + block item | `ConfigBlocks.java:355` | `blockstates/tube.json` |
| `thaumcraft:tube_valve` | `thaumcraft_reborn:tube_valve` | Block + block item | `ConfigBlocks.java:356` | `blockstates/tube_valve.json` |
| `thaumcraft:tube_restrict` | `thaumcraft_reborn:tube_restrict` | Block + block item | `ConfigBlocks.java:357` | `blockstates/tube_restrict.json` |
| `thaumcraft:tube_oneway` | `thaumcraft_reborn:tube_oneway` | Block + block item | `ConfigBlocks.java:358` | `blockstates/tube_oneway.json` |
| `thaumcraft:tube_filter` | `thaumcraft_reborn:tube_filter` | Block + block item | `ConfigBlocks.java:359` | `blockstates/tube_filter.json` |
| `thaumcraft:tube_buffer` | `thaumcraft_reborn:tube_buffer` | Block + block item | `ConfigBlocks.java:360` | `blockstates/tube_buffer.json` |
| `thaumcraft:jar_normal` | `thaumcraft_reborn:jar_normal` | Block + block item | `ConfigBlocks.java:361` | `blockstates/jar_normal.json` |
| `thaumcraft:jar_void` | `thaumcraft_reborn:jar_void` | Block + block item | `ConfigBlocks.java:362` | `blockstates/jar_void.json` |
| `thaumcraft:jar_brain` | `thaumcraft_reborn:jar_brain` | Block + block item | `ConfigBlocks.java:363` | `blockstates/jar_brain.json` |
| `thaumcraft:infusion_matrix` | `thaumcraft_reborn:infusion_matrix` | Block + block item | `ConfigBlocks.java:364` | `blockstates/infusion_matrix.json` |
| `thaumcraft:infernal_furnace` | `thaumcraft_reborn:infernal_furnace` | Block + block item | `ConfigBlocks.java:365` | `blockstates/infernal_furnace.json` |
| `thaumcraft:everfull_urn` | `thaumcraft_reborn:everfull_urn` | Block + block item | `ConfigBlocks.java:366` | `blockstates/everfull_urn.json` |
| `thaumcraft:thaumatorium`<br>`thaumcraft:thaumatorium_top` | `thaumcraft_reborn:thaumatorium`<br>`thaumcraft_reborn:thaumatorium_top` | Block + block item | `ConfigBlocks.java:367` | Familia dinámica; 2 variantes cotejadas con `blockstates/`. |
| `thaumcraft:thaumatorium`<br>`thaumcraft:thaumatorium_top` | `thaumcraft_reborn:thaumatorium`<br>`thaumcraft_reborn:thaumatorium_top` | Block + block item | `ConfigBlocks.java:368` | Familia dinámica; 2 variantes cotejadas con `blockstates/`. |
| `thaumcraft:brain_box` | `thaumcraft_reborn:brain_box` | Block + block item | `ConfigBlocks.java:369` | `blockstates/brain_box.json` |
| `thaumcraft:spa` | `thaumcraft_reborn:spa` | Block + block item | `ConfigBlocks.java:370` | `blockstates/spa.json` |
| `thaumcraft:golem_builder` | `thaumcraft_reborn:golem_builder` | Block + block item | `ConfigBlocks.java:371` | `blockstates/golem_builder.json` |
| `thaumcraft:mirror` | `thaumcraft_reborn:mirror` | Block + block item | `ConfigBlocks.java:372` | `blockstates/mirror.json` |
| `thaumcraft:mirror_essentia` | `thaumcraft_reborn:mirror_essentia` | Block + block item | `ConfigBlocks.java:373` | `blockstates/mirror_essentia.json` |
| `thaumcraft:essentia_input` | `thaumcraft_reborn:essentia_input` | Block + block item | `ConfigBlocks.java:374` | `blockstates/essentia_input.json` |
| `thaumcraft:essentia_output` | `thaumcraft_reborn:essentia_output` | Block + block item | `ConfigBlocks.java:375` | `blockstates/essentia_output.json` |
| `thaumcraft:redstone_relay` | `thaumcraft_reborn:redstone_relay` | Block + block item | `ConfigBlocks.java:376` | `blockstates/redstone_relay.json` |
| `thaumcraft:pattern_crafter` | `thaumcraft_reborn:pattern_crafter` | Block + block item | `ConfigBlocks.java:377` | `blockstates/pattern_crafter.json` |
| `thaumcraft:potion_sprayer` | `thaumcraft_reborn:potion_sprayer` | Block + block item | `ConfigBlocks.java:378` | `blockstates/potion_sprayer.json` |
| `thaumcraft:activator_rail` | `thaumcraft_reborn:activator_rail` | Block + block item | `ConfigBlocks.java:379` | `blockstates/activator_rail.json` |
| `thaumcraft:stabilizer` | `thaumcraft_reborn:stabilizer` | Block + block item | `ConfigBlocks.java:383` | `blockstates/stabilizer.json` |
| `thaumcraft:vis_generator` | `thaumcraft_reborn:vis_generator` | Block + block item | `ConfigBlocks.java:384` | `blockstates/vis_generator.json` |
| `thaumcraft:condenser` | `thaumcraft_reborn:condenser` | Block + block item | `ConfigBlocks.java:385` | `blockstates/condenser.json` |
| `thaumcraft:condenser_lattice`<br>`thaumcraft:condenser_lattice_dirty` | `thaumcraft_reborn:condenser_lattice`<br>`thaumcraft_reborn:condenser_lattice_dirty` | Block + block item | `ConfigBlocks.java:386` | Familia dinámica; 2 variantes cotejadas con `blockstates/`. |
| `thaumcraft:condenser_lattice`<br>`thaumcraft:condenser_lattice_dirty` | `thaumcraft_reborn:condenser_lattice`<br>`thaumcraft_reborn:condenser_lattice_dirty` | Block + block item | `ConfigBlocks.java:387` | Familia dinámica; 2 variantes cotejadas con `blockstates/`. |
| `thaumcraft:void_siphon` | `thaumcraft_reborn:void_siphon` | Block + block item | `ConfigBlocks.java:388` | `blockstates/void_siphon.json` |
| `thaumcraft:hole` | `thaumcraft_reborn:hole` | Block + block item | `ConfigBlocks.java:400` | `blockstates/hole.json` |
| `thaumcraft:effect_shock` | `thaumcraft_reborn:effect_shock` | Block + block item | `ConfigBlocks.java:401` | `blockstates/effect_shock.json` |
| `thaumcraft:effect_sap` | `thaumcraft_reborn:effect_sap` | Block + block item | `ConfigBlocks.java:402` | `blockstates/effect_sap.json` |
| `thaumcraft:effect_glimmer` | `thaumcraft_reborn:effect_glimmer` | Block + block item | `ConfigBlocks.java:403` | `blockstates/effect_glimmer.json` |
| `thaumcraft:placeholder_brick` | `thaumcraft_reborn:placeholder_brick` | Block + block item | `ConfigBlocks.java:404` | `blockstates/placeholder_brick.json` |
| `thaumcraft:placeholder_obsidian` | `thaumcraft_reborn:placeholder_obsidian` | Block + block item | `ConfigBlocks.java:405` | `blockstates/placeholder_obsidian.json` |
| `thaumcraft:placeholder_bars` | `thaumcraft_reborn:placeholder_bars` | Block + block item | `ConfigBlocks.java:406` | `blockstates/placeholder_bars.json` |
| `thaumcraft:placeholder_anvil` | `thaumcraft_reborn:placeholder_anvil` | Block + block item | `ConfigBlocks.java:407` | `blockstates/placeholder_anvil.json` |
| `thaumcraft:placeholder_cauldron` | `thaumcraft_reborn:placeholder_cauldron` | Block + block item | `ConfigBlocks.java:408` | `blockstates/placeholder_cauldron.json` |
| `thaumcraft:placeholder_table` | `thaumcraft_reborn:placeholder_table` | Block + block item | `ConfigBlocks.java:409` | `blockstates/placeholder_table.json` |
| `thaumcraft:empty` | `thaumcraft_reborn:empty` | Block + block item | `ConfigBlocks.java:410` | `blockstates/empty.json` |
| `thaumcraft:barrier` | `thaumcraft_reborn:barrier` | Block + block item | `ConfigBlocks.java:411` | `blockstates/barrier.json` |
| `thaumcraft:slab_arcane_stone` | `thaumcraft_reborn:slab_arcane_stone` | Block + ItemSlab | `ConfigBlocks.java:217` | `blockstates/slab_arcane_stone.json`; el `ItemSlab` reutiliza el ID del bloque y se registra en `ConfigBlocks.java:225`. |
| `thaumcraft:slab_double_arcane_stone` | `thaumcraft_reborn:slab_double_arcane_stone` | Block | `ConfigBlocks.java:218` | `blockstates/slab_double_arcane_stone.json`; el `ItemSlab` de la losa mitad se registra en `ConfigBlocks.java:225`, no hay un item separado para la doble. |
| `thaumcraft:slab_arcane_brick` | `thaumcraft_reborn:slab_arcane_brick` | Block + ItemSlab | `ConfigBlocks.java:219` | `blockstates/slab_arcane_brick.json`; el `ItemSlab` reutiliza el ID del bloque y se registra en `ConfigBlocks.java:230`. |
| `thaumcraft:slab_double_arcane_brick` | `thaumcraft_reborn:slab_double_arcane_brick` | Block | `ConfigBlocks.java:220` | `blockstates/slab_double_arcane_brick.json`; el `ItemSlab` de la losa mitad se registra en `ConfigBlocks.java:230`, no hay un item separado para la doble. |
| `thaumcraft:slab_ancient` | `thaumcraft_reborn:slab_ancient` | Block + ItemSlab | `ConfigBlocks.java:221` | `blockstates/slab_ancient.json`; el `ItemSlab` reutiliza el ID del bloque y se registra en `ConfigBlocks.java:235`. |
| `thaumcraft:slab_double_ancient` | `thaumcraft_reborn:slab_double_ancient` | Block | `ConfigBlocks.java:222` | `blockstates/slab_double_ancient.json`; el `ItemSlab` de la losa mitad se registra en `ConfigBlocks.java:235`, no hay un item separado para la doble. |
| `thaumcraft:slab_eldritch` | `thaumcraft_reborn:slab_eldritch` | Block + ItemSlab | `ConfigBlocks.java:223` | `blockstates/slab_eldritch.json`; el `ItemSlab` reutiliza el ID del bloque y se registra en `ConfigBlocks.java:237`. |
| `thaumcraft:slab_double_eldritch` | `thaumcraft_reborn:slab_double_eldritch` | Block | `ConfigBlocks.java:224` | `blockstates/slab_double_eldritch.json`; el `ItemSlab` de la losa mitad se registra en `ConfigBlocks.java:237`, no hay un item separado para la doble. |
| `thaumcraft:slab_greatwood` | `thaumcraft_reborn:slab_greatwood` | Block + ItemSlab | `ConfigBlocks.java:262` | `blockstates/slab_greatwood.json`; el `ItemSlab` reutiliza el ID del bloque y se registra en `ConfigBlocks.java:266`. |
| `thaumcraft:slab_double_greatwood` | `thaumcraft_reborn:slab_double_greatwood` | Block | `ConfigBlocks.java:263` | `blockstates/slab_double_greatwood.json`; el `ItemSlab` de la losa mitad se registra en `ConfigBlocks.java:266`, no hay un item separado para la doble. |
| `thaumcraft:slab_silverwood` | `thaumcraft_reborn:slab_silverwood` | Block + ItemSlab | `ConfigBlocks.java:264` | `blockstates/slab_silverwood.json`; el `ItemSlab` reutiliza el ID del bloque y se registra en `ConfigBlocks.java:271`. |
| `thaumcraft:slab_double_silverwood` | `thaumcraft_reborn:slab_double_silverwood` | Block | `ConfigBlocks.java:265` | `blockstates/slab_double_silverwood.json`; el `ItemSlab` de la losa mitad se registra en `ConfigBlocks.java:271`, no hay un item separado para la doble. |
| `thaumcraft:banner_black`<br>`thaumcraft:banner_blue`<br>`thaumcraft:banner_brown`<br>`thaumcraft:banner_cyan`<br>`thaumcraft:banner_gray`<br>`thaumcraft:banner_green`<br>`thaumcraft:banner_lightblue`<br>`thaumcraft:banner_lime`<br>`thaumcraft:banner_magenta`<br>`thaumcraft:banner_orange`<br>`thaumcraft:banner_pink`<br>`thaumcraft:banner_purple`<br>`thaumcraft:banner_red`<br>`thaumcraft:banner_silver`<br>`thaumcraft:banner_white`<br>`thaumcraft:banner_yellow` | `thaumcraft_reborn:banner_black`<br>`thaumcraft_reborn:banner_blue`<br>`thaumcraft_reborn:banner_brown`<br>`thaumcraft_reborn:banner_cyan`<br>`thaumcraft_reborn:banner_gray`<br>`thaumcraft_reborn:banner_green`<br>`thaumcraft_reborn:banner_lightblue`<br>`thaumcraft_reborn:banner_lime`<br>`thaumcraft_reborn:banner_magenta`<br>`thaumcraft_reborn:banner_orange`<br>`thaumcraft_reborn:banner_pink`<br>`thaumcraft_reborn:banner_purple`<br>`thaumcraft_reborn:banner_red`<br>`thaumcraft_reborn:banner_silver`<br>`thaumcraft_reborn:banner_white`<br>`thaumcraft_reborn:banner_yellow` | Block + block item | `ConfigBlocks.java:317` | Bucle `EnumDyeColor`; 16 IDs cotejados con `blockstates/`; `BlockBannerTCItem` usa el mismo ID en `ConfigBlocks.java:318`. |
| `thaumcraft:banner_crimson_cult` | `thaumcraft_reborn:banner_crimson_cult` | Block + block item | `ConfigBlocks.java:323` | `blockstates/banner_crimson_cult.json`; item con el mismo ID en `ConfigBlocks.java:325`. |
| `thaumcraft:flux_goo` | `thaumcraft_reborn:flux_goo` | Fluid block | `ConfigBlocks.java:391` | `blockstates/flux_goo.json`; no se encontró registro de un block item. |
| `thaumcraft:liquid_death` | `thaumcraft_reborn:liquid_death` | Fluid block | `ConfigBlocks.java:395` | `blockstates/liquid_death.json`; no se encontró registro de un block item. |
| `thaumcraft:purifying_fluid` | `thaumcraft_reborn:purifying_fluid` | Fluid block | `ConfigBlocks.java:399` | `blockstates/purifying_fluid.json`; no se encontró registro de un block item. |

## Items

**Recuento:** 108 registros directos (`iForgeRegistry.register`), con 164 IDs nuevos enumerados a partir de bases/variantes; se cotejaron 155 archivos `models/item/**/*.json` y 213 `textures/items/**/*.png` por nombre (182 texturas están en la raíz y 31 en subdirectorios). Los block items no se vuelven a contar aquí.

| ID 1.12.2 | ID nuevo | Tipo | Origen | Notas |
|---|---|---|---|---|
| `thaumcraft:thaumonomicon` (meta 0: `normal`)<br>`thaumcraft:thaumonomicon` (meta 1: `cheat`) | `thaumcraft_reborn:thaumonomicon_normal`<br>`thaumcraft_reborn:thaumonomicon_cheat` | Item con metadata | `ConfigItems.java:170` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: cheat→`thaumonomicon_cheat.png`. Assets base/overlay sin asignación inferida: `thaumonomicon.png`. Sin match de nombre de modelo/textura: `normal`. |
| `thaumcraft:curio` (meta 0: `arcane`)<br>`thaumcraft:curio` (meta 1: `preserved`)<br>`thaumcraft:curio` (meta 2: `ancient`)<br>`thaumcraft:curio` (meta 3: `eldritch`)<br>`thaumcraft:curio` (meta 4: `knowledge`)<br>`thaumcraft:curio` (meta 5: `twisted`)<br>`thaumcraft:curio` (meta 6: `rites`) | `thaumcraft_reborn:curio_arcane`<br>`thaumcraft_reborn:curio_preserved`<br>`thaumcraft_reborn:curio_ancient`<br>`thaumcraft_reborn:curio_eldritch`<br>`thaumcraft_reborn:curio_knowledge`<br>`thaumcraft_reborn:curio_twisted`<br>`thaumcraft_reborn:curio_rites` | Item con metadata | `ConfigItems.java:171` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: arcane→`curio_arcane.png`, preserved→`curio_preserved.png`, ancient→`curio_ancient.png`, eldritch→`curio_eldritch.png`, knowledge→`curio_knowledge.png`, twisted→`curio_twisted.png`. Sin match de nombre de modelo/textura: `rites`. |
| `thaumcraft:loot_bag` (meta 0: `common`)<br>`thaumcraft:loot_bag` (meta 1: `uncommon`)<br>`thaumcraft:loot_bag` (meta 2: `rare`) | `thaumcraft_reborn:loot_bag_common`<br>`thaumcraft_reborn:loot_bag_uncommon`<br>`thaumcraft_reborn:loot_bag_rare` | Item con metadata | `ConfigItems.java:172` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: common→`loot_bag_common.png`, uncommon→`loot_bag_uncommon.png`, rare→`loot_bag_rare.png`. |
| `thaumcraft:primordial_pearl` | `thaumcraft_reborn:primordial_pearl` | Item | `ConfigItems.java:173` | `models/item/primordial_pearl.json`. |
| `thaumcraft:pech_wand` | `thaumcraft_reborn:pech_wand` | Item | `ConfigItems.java:174` | `models/item/pech_wand.json`. |
| `thaumcraft:celestial_notes` (meta 0: `sun`)<br>`thaumcraft:celestial_notes` (meta 1: `stars_1`)<br>`thaumcraft:celestial_notes` (meta 2: `stars_2`)<br>`thaumcraft:celestial_notes` (meta 3: `stars_3`)<br>`thaumcraft:celestial_notes` (meta 4: `stars_4`)<br>`thaumcraft:celestial_notes` (meta 5: `moon_1`)<br>`thaumcraft:celestial_notes` (meta 6: `moon_2`)<br>`thaumcraft:celestial_notes` (meta 7: `moon_3`)<br>`thaumcraft:celestial_notes` (meta 8: `moon_4`)<br>`thaumcraft:celestial_notes` (meta 9: `moon_5`)<br>`thaumcraft:celestial_notes` (meta 10: `moon_6`)<br>`thaumcraft:celestial_notes` (meta 11: `moon_7`)<br>`thaumcraft:celestial_notes` (meta 12: `moon_8`) | `thaumcraft_reborn:celestial_notes_sun`<br>`thaumcraft_reborn:celestial_notes_stars_1`<br>`thaumcraft_reborn:celestial_notes_stars_2`<br>`thaumcraft_reborn:celestial_notes_stars_3`<br>`thaumcraft_reborn:celestial_notes_stars_4`<br>`thaumcraft_reborn:celestial_notes_moon_1`<br>`thaumcraft_reborn:celestial_notes_moon_2`<br>`thaumcraft_reborn:celestial_notes_moon_3`<br>`thaumcraft_reborn:celestial_notes_moon_4`<br>`thaumcraft_reborn:celestial_notes_moon_5`<br>`thaumcraft_reborn:celestial_notes_moon_6`<br>`thaumcraft_reborn:celestial_notes_moon_7`<br>`thaumcraft_reborn:celestial_notes_moon_8` | Item con metadata | `ConfigItems.java:175` | Sin match exacto en `models/item` por ID aplanado. Nombres de variante coincidentes sin ID completo: sun→`celestial/sun.png` (nombre de variante, no ID literal). Sin match de nombre de modelo/textura: `stars_1`, `stars_2`, `stars_3`, `stars_4`, `moon_1`, `moon_2`, `moon_3`, `moon_4`, `moon_5`, `moon_6`, `moon_7`, `moon_8`. |
| `thaumcraft:amber` | `thaumcraft_reborn:amber` | Item | `ConfigItems.java:176` | `models/item/amber.json`. |
| `thaumcraft:quicksilver` | `thaumcraft_reborn:quicksilver` | Item | `ConfigItems.java:177` | `models/item/quicksilver.json`. |
| `thaumcraft:ingot` (meta 0: `thaumium`)<br>`thaumcraft:ingot` (meta 1: `void`)<br>`thaumcraft:ingot` (meta 2: `brass`) | `thaumcraft_reborn:ingot_thaumium`<br>`thaumcraft_reborn:ingot_void`<br>`thaumcraft_reborn:ingot_brass` | Item con metadata | `ConfigItems.java:178` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: thaumium→`ingot_thaumium.png`, void→`ingot_void.png`, brass→`ingot_brass.png`. |
| `thaumcraft:nugget` (meta 0: `iron`)<br>`thaumcraft:nugget` (meta 1: `copper`)<br>`thaumcraft:nugget` (meta 2: `tin`)<br>`thaumcraft:nugget` (meta 3: `silver`)<br>`thaumcraft:nugget` (meta 4: `lead`)<br>`thaumcraft:nugget` (meta 5: `quicksilver`)<br>`thaumcraft:nugget` (meta 6: `thaumium`)<br>`thaumcraft:nugget` (meta 7: `void`)<br>`thaumcraft:nugget` (meta 8: `brass`)<br>`thaumcraft:nugget` (meta 9: `quartz`)<br>`thaumcraft:nugget` (meta 10: `rareearth`) | `thaumcraft_reborn:nugget_iron`<br>`thaumcraft_reborn:nugget_copper`<br>`thaumcraft_reborn:nugget_tin`<br>`thaumcraft_reborn:nugget_silver`<br>`thaumcraft_reborn:nugget_lead`<br>`thaumcraft_reborn:nugget_quicksilver`<br>`thaumcraft_reborn:nugget_thaumium`<br>`thaumcraft_reborn:nugget_void`<br>`thaumcraft_reborn:nugget_brass`<br>`thaumcraft_reborn:nugget_quartz`<br>`thaumcraft_reborn:nugget_rareearth` | Item con metadata | `ConfigItems.java:179` | models/item: quicksilver→token `quicksilver.json` (no ID exact match). textures/items exactas: iron→`nugget_iron.png`, copper→`nugget_copper.png`, tin→`nugget_tin.png`, silver→`nugget_silver.png`, lead→`nugget_lead.png`, quicksilver→`nugget_quicksilver.png`, thaumium→`nugget_thaumium.png`, void→`nugget_void.png`, brass→`nugget_brass.png`, quartz→`nugget_quartz.png`. Sin match de nombre de modelo/textura: `rareearth`. |
| `thaumcraft:cluster` (meta 0: `iron`)<br>`thaumcraft:cluster` (meta 1: `gold`)<br>`thaumcraft:cluster` (meta 2: `copper`)<br>`thaumcraft:cluster` (meta 3: `tin`)<br>`thaumcraft:cluster` (meta 4: `silver`)<br>`thaumcraft:cluster` (meta 5: `lead`)<br>`thaumcraft:cluster` (meta 6: `cinnabar`)<br>`thaumcraft:cluster` (meta 7: `quartz`) | `thaumcraft_reborn:cluster_iron`<br>`thaumcraft_reborn:cluster_gold`<br>`thaumcraft_reborn:cluster_copper`<br>`thaumcraft_reborn:cluster_tin`<br>`thaumcraft_reborn:cluster_silver`<br>`thaumcraft_reborn:cluster_lead`<br>`thaumcraft_reborn:cluster_cinnabar`<br>`thaumcraft_reborn:cluster_quartz` | Item con metadata | `ConfigItems.java:184` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: iron→`cluster_iron.png`, gold→`cluster_gold.png`, copper→`cluster_copper.png`, tin→`cluster_tin.png`, silver→`cluster_silver.png`, lead→`cluster_lead.png`, cinnabar→`cluster_cinnabar.png`, quartz→`cluster_quartz.png`. |
| `thaumcraft:fabric` | `thaumcraft_reborn:fabric` | Item | `ConfigItems.java:185` | `models/item/fabric.json`. |
| `thaumcraft:vis_resonator` | `thaumcraft_reborn:vis_resonator` | Item | `ConfigItems.java:186` | `models/item/vis_resonator.json`. |
| `thaumcraft:tallow` | `thaumcraft_reborn:tallow` | Item | `ConfigItems.java:187` | `models/item/tallow.json`. |
| `thaumcraft:mechanism_simple` | `thaumcraft_reborn:mechanism_simple` | Item | `ConfigItems.java:188` | `models/item/mechanism_simple.json`. |
| `thaumcraft:mechanism_complex` | `thaumcraft_reborn:mechanism_complex` | Item | `ConfigItems.java:189` | `models/item/mechanism_complex.json`. |
| `thaumcraft:plate` (meta 0: `brass`)<br>`thaumcraft:plate` (meta 1: `iron`)<br>`thaumcraft:plate` (meta 2: `thaumium`)<br>`thaumcraft:plate` (meta 3: `void`) | `thaumcraft_reborn:plate_brass`<br>`thaumcraft_reborn:plate_iron`<br>`thaumcraft_reborn:plate_thaumium`<br>`thaumcraft_reborn:plate_void` | Item con metadata | `ConfigItems.java:190` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: brass→`plate_brass.png`, iron→`plate_iron.png`, thaumium→`plate_thaumium.png`, void→`plate_void.png`. |
| `thaumcraft:filter` | `thaumcraft_reborn:filter` | Item | `ConfigItems.java:191` | `models/item/filter.json`. |
| `thaumcraft:morphic_resonator` | `thaumcraft_reborn:morphic_resonator` | Item | `ConfigItems.java:192` | `models/item/morphic_resonator.json`. |
| `thaumcraft:salis_mundus` | `thaumcraft_reborn:salis_mundus` | Item | `ConfigItems.java:193` | `models/item/salis_mundus.json`. |
| `thaumcraft:mirrored_glass` | `thaumcraft_reborn:mirrored_glass` | Item | `ConfigItems.java:194` | `models/item/mirrored_glass.json`. |
| `thaumcraft:void_seed` | `thaumcraft_reborn:void_seed` | Item | `ConfigItems.java:195` | `models/item/void_seed.json`. |
| `thaumcraft:mind` (meta 0: `clockwork`)<br>`thaumcraft:mind` (meta 1: `biothaumic`) | `thaumcraft_reborn:mind_clockwork`<br>`thaumcraft_reborn:mind_biothaumic` | Item con metadata | `ConfigItems.java:196` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: clockwork→`mind_clockwork.png`, biothaumic→`mind_biothaumic.png`. |
| `thaumcraft:module` (meta 0: `vision`)<br>`thaumcraft:module` (meta 1: `aggression`) | `thaumcraft_reborn:module_vision`<br>`thaumcraft_reborn:module_aggression` | Item con metadata | `ConfigItems.java:197` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: vision→`module_vision.png`, aggression→`module_aggression.png`. |
| `thaumcraft:crystal_essence` | `thaumcraft_reborn:crystal_essence` | Item | `ConfigItems.java:198` | Sin modelo item homónimo; la ruta requiere cotejo con renderer/modelo especial. |
| `thaumcraft:chunk` | `thaumcraft_reborn:chunk` | Item | `ConfigItems.java:199` | Sin modelo item homónimo; la ruta requiere cotejo con renderer/modelo especial. |
| `thaumcraft:triple_meat_treat` | `thaumcraft_reborn:triple_meat_treat` | Item | `ConfigItems.java:200` | `models/item/triple_meat_treat.json`. |
| `thaumcraft:brain` | `thaumcraft_reborn:brain` | Item | `ConfigItems.java:201` | `models/item/brain.json`. |
| `thaumcraft:label` (meta 0: `blank`)<br>`thaumcraft:label` (meta 1: `filled`) | `thaumcraft_reborn:label_blank`<br>`thaumcraft_reborn:label_filled` | Item con metadata | `ConfigItems.java:202` | Sin match exacto en `models/item` por ID aplanado. Assets base/overlay sin asignación inferida: `label.png`, `label_overlay.png`. Sin match de nombre de modelo/textura: `blank`, `filled`. |
| `thaumcraft:phial` (meta 0: `empty`)<br>`thaumcraft:phial` (meta 1: `filled`) | `thaumcraft_reborn:phial_empty`<br>`thaumcraft_reborn:phial_filled` | Item con metadata | `ConfigItems.java:203` | Sin match exacto en `models/item` por ID aplanado. Assets base/overlay sin asignación inferida: `phial.png`, `phial_overlay.png`. Sin match de nombre de modelo/textura: `empty`, `filled`. |
| `thaumcraft:alumentum` | `thaumcraft_reborn:alumentum` | Item | `ConfigItems.java:204` | `models/item/alumentum.json`. |
| `thaumcraft:jar_brace` | `thaumcraft_reborn:jar_brace` | Item | `ConfigItems.java:205` | `models/item/jar_brace.json`. |
| `thaumcraft:bottle_taint` | `thaumcraft_reborn:bottle_taint` | Item | `ConfigItems.java:206` | `models/item/bottle_taint.json`. |
| `thaumcraft:sanity_soap` | `thaumcraft_reborn:sanity_soap` | Item | `ConfigItems.java:207` | `models/item/sanity_soap.json`. |
| `thaumcraft:bath_salts` | `thaumcraft_reborn:bath_salts` | Item | `ConfigItems.java:208` | `models/item/bath_salts.json`. |
| `thaumcraft:turret` (meta 0: `basic`)<br>`thaumcraft:turret` (meta 1: `advanced`)<br>`thaumcraft:turret` (meta 2: `bore`) | `thaumcraft_reborn:turret_basic`<br>`thaumcraft_reborn:turret_advanced`<br>`thaumcraft_reborn:turret_bore` | Item con metadata | `ConfigItems.java:209` | Sin match exacto en `models/item` por ID aplanado. textures/items exactas: basic→`turret_basic.png`, advanced→`turret_advanced.png`, bore→`turret_bore.png`. |
| `thaumcraft:causality_collapser` | `thaumcraft_reborn:causality_collapser` | Item | `ConfigItems.java:210` | `models/item/causality_collapser.json`. |
| `thaumcraft:scribing_tools` | `thaumcraft_reborn:scribing_tools` | Item | `ConfigItems.java:211` | `models/item/scribing_tools.json`. |
| `thaumcraft:thaumometer` | `thaumcraft_reborn:thaumometer` | Item | `ConfigItems.java:212` | Sin modelo item homónimo; la ruta requiere cotejo con renderer/modelo especial. |
| `thaumcraft:resonator` | `thaumcraft_reborn:resonator` | Item | `ConfigItems.java:213` | `models/item/resonator.json`. |
| `thaumcraft:sanity_checker` | `thaumcraft_reborn:sanity_checker` | Item | `ConfigItems.java:214` | `models/item/sanity_checker.json`. |
| `thaumcraft:hand_mirror` | `thaumcraft_reborn:hand_mirror` | Item | `ConfigItems.java:215` | `models/item/hand_mirror.json`. |
| `thaumcraft:thaumium_axe` | `thaumcraft_reborn:thaumium_axe` | Item | `ConfigItems.java:216` | `models/item/thaumium_axe.json`. |
| `thaumcraft:thaumium_sword` | `thaumcraft_reborn:thaumium_sword` | Item | `ConfigItems.java:217` | `models/item/thaumium_sword.json`. |
| `thaumcraft:thaumium_shovel` | `thaumcraft_reborn:thaumium_shovel` | Item | `ConfigItems.java:218` | `models/item/thaumium_shovel.json`. |
| `thaumcraft:thaumium_pick` | `thaumcraft_reborn:thaumium_pick` | Item | `ConfigItems.java:219` | `models/item/thaumium_pick.json`. |
| `thaumcraft:thaumium_hoe` | `thaumcraft_reborn:thaumium_hoe` | Item | `ConfigItems.java:220` | `models/item/thaumium_hoe.json`. |
| `thaumcraft:void_axe` | `thaumcraft_reborn:void_axe` | Item | `ConfigItems.java:221` | `models/item/void_axe.json`. |
| `thaumcraft:void_sword` | `thaumcraft_reborn:void_sword` | Item | `ConfigItems.java:222` | `models/item/void_sword.json`. |
| `thaumcraft:void_shovel` | `thaumcraft_reborn:void_shovel` | Item | `ConfigItems.java:223` | `models/item/void_shovel.json`. |
| `thaumcraft:void_pick` | `thaumcraft_reborn:void_pick` | Item | `ConfigItems.java:224` | `models/item/void_pick.json`. |
| `thaumcraft:void_hoe` | `thaumcraft_reborn:void_hoe` | Item | `ConfigItems.java:225` | `models/item/void_hoe.json`. |
| `thaumcraft:elemental_axe` | `thaumcraft_reborn:elemental_axe` | Item | `ConfigItems.java:226` | `models/item/elemental_axe.json`. |
| `thaumcraft:elemental_sword` | `thaumcraft_reborn:elemental_sword` | Item | `ConfigItems.java:227` | `models/item/elemental_sword.json`. |
| `thaumcraft:elemental_shovel` | `thaumcraft_reborn:elemental_shovel` | Item | `ConfigItems.java:228` | `models/item/elemental_shovel.json`. |
| `thaumcraft:elemental_pick` | `thaumcraft_reborn:elemental_pick` | Item | `ConfigItems.java:229` | `models/item/elemental_pick.json`. |
| `thaumcraft:elemental_hoe` | `thaumcraft_reborn:elemental_hoe` | Item | `ConfigItems.java:230` | `models/item/elemental_hoe.json`. |
| `thaumcraft:primal_crusher` | `thaumcraft_reborn:primal_crusher` | Item | `ConfigItems.java:231` | `models/item/primal_crusher.json`. |
| `thaumcraft:crimson_blade` | `thaumcraft_reborn:crimson_blade` | Item | `ConfigItems.java:232` | `models/item/crimson_blade.json`. |
| `thaumcraft:grapple_gun` | `thaumcraft_reborn:grapple_gun` | Item | `ConfigItems.java:233` | `models/item/grapple_gun.json`. |
| `thaumcraft:grapple_gun_tip` | `thaumcraft_reborn:grapple_gun_tip` | Item | `ConfigItems.java:234` | `models/item/grapple_gun_tip.json`. |
| `thaumcraft:grapple_gun_spool` | `thaumcraft_reborn:grapple_gun_spool` | Item | `ConfigItems.java:235` | `models/item/grapple_gun_spool.json`. |
| `thaumcraft:goggles` | `thaumcraft_reborn:goggles` | Item | `ConfigItems.java:236` | `models/item/goggles.json`. |
| `thaumcraft:thaumium_helm` | `thaumcraft_reborn:thaumium_helm` | Item | `ConfigItems.java:237` | `models/item/thaumium_helm.json`. |
| `thaumcraft:thaumium_chest` | `thaumcraft_reborn:thaumium_chest` | Item | `ConfigItems.java:238` | `models/item/thaumium_chest.json`. |
| `thaumcraft:thaumium_legs` | `thaumcraft_reborn:thaumium_legs` | Item | `ConfigItems.java:241` | `models/item/thaumium_legs.json`. |
| `thaumcraft:thaumium_boots` | `thaumcraft_reborn:thaumium_boots` | Item | `ConfigItems.java:242` | `models/item/thaumium_boots.json`. |
| `thaumcraft:cloth_chest` | `thaumcraft_reborn:cloth_chest` | Item | `ConfigItems.java:245` | `models/item/cloth_chest.json`. |
| `thaumcraft:cloth_legs` | `thaumcraft_reborn:cloth_legs` | Item | `ConfigItems.java:246` | `models/item/cloth_legs.json`. |
| `thaumcraft:cloth_boots` | `thaumcraft_reborn:cloth_boots` | Item | `ConfigItems.java:247` | `models/item/cloth_boots.json`. |
| `thaumcraft:traveller_boots` | `thaumcraft_reborn:traveller_boots` | Item | `ConfigItems.java:248` | `models/item/traveller_boots.json`. |
| `thaumcraft:fortress_helm` | `thaumcraft_reborn:fortress_helm` | Item | `ConfigItems.java:249` | `models/item/fortress_helm.json`. |
| `thaumcraft:fortress_chest` | `thaumcraft_reborn:fortress_chest` | Item | `ConfigItems.java:250` | `models/item/fortress_chest.json`. |
| `thaumcraft:fortress_legs` | `thaumcraft_reborn:fortress_legs` | Item | `ConfigItems.java:253` | `models/item/fortress_legs.json`. |
| `thaumcraft:void_helm` | `thaumcraft_reborn:void_helm` | Item | `ConfigItems.java:254` | `models/item/void_helm.json`. |
| `thaumcraft:void_chest` | `thaumcraft_reborn:void_chest` | Item | `ConfigItems.java:255` | `models/item/void_chest.json`. |
| `thaumcraft:void_legs` | `thaumcraft_reborn:void_legs` | Item | `ConfigItems.java:256` | `models/item/void_legs.json`. |
| `thaumcraft:void_boots` | `thaumcraft_reborn:void_boots` | Item | `ConfigItems.java:257` | `models/item/void_boots.json`. |
| `thaumcraft:void_robe_helm` | `thaumcraft_reborn:void_robe_helm` | Item | `ConfigItems.java:258` | `models/item/void_robe_helm.json`. |
| `thaumcraft:void_robe_chest` | `thaumcraft_reborn:void_robe_chest` | Item | `ConfigItems.java:261` | `models/item/void_robe_chest.json`. |
| `thaumcraft:void_robe_legs` | `thaumcraft_reborn:void_robe_legs` | Item | `ConfigItems.java:264` | `models/item/void_robe_legs.json`. |
| `thaumcraft:crimson_plate_helm` | `thaumcraft_reborn:crimson_plate_helm` | Item | `ConfigItems.java:267` | `models/item/crimson_plate_helm.json`. |
| `thaumcraft:crimson_plate_chest` | `thaumcraft_reborn:crimson_plate_chest` | Item | `ConfigItems.java:270` | `models/item/crimson_plate_chest.json`. |
| `thaumcraft:crimson_plate_legs` | `thaumcraft_reborn:crimson_plate_legs` | Item | `ConfigItems.java:273` | `models/item/crimson_plate_legs.json`. |
| `thaumcraft:crimson_boots` | `thaumcraft_reborn:crimson_boots` | Item | `ConfigItems.java:276` | `models/item/crimson_boots.json`. |
| `thaumcraft:crimson_robe_helm` | `thaumcraft_reborn:crimson_robe_helm` | Item | `ConfigItems.java:277` | `models/item/crimson_robe_helm.json`. |
| `thaumcraft:crimson_robe_chest` | `thaumcraft_reborn:crimson_robe_chest` | Item | `ConfigItems.java:280` | `models/item/crimson_robe_chest.json`. |
| `thaumcraft:crimson_robe_legs` | `thaumcraft_reborn:crimson_robe_legs` | Item | `ConfigItems.java:283` | `models/item/crimson_robe_legs.json`. |
| `thaumcraft:crimson_praetor_helm` | `thaumcraft_reborn:crimson_praetor_helm` | Item | `ConfigItems.java:286` | `models/item/crimson_praetor_helm.json`. |
| `thaumcraft:crimson_praetor_chest` | `thaumcraft_reborn:crimson_praetor_chest` | Item | `ConfigItems.java:287` | `models/item/crimson_praetor_chest.json`. |
| `thaumcraft:crimson_praetor_legs` | `thaumcraft_reborn:crimson_praetor_legs` | Item | `ConfigItems.java:288` | `models/item/crimson_praetor_legs.json`. |
| `thaumcraft:baubles` (meta 0: `amulet_mundane`)<br>`thaumcraft:baubles` (meta 1: `ring_mundane`)<br>`thaumcraft:baubles` (meta 2: `girdle_mundane`)<br>`thaumcraft:baubles` (meta 3: `ring_apprentice`)<br>`thaumcraft:baubles` (meta 4: `amulet_fancy`)<br>`thaumcraft:baubles` (meta 5: `ring_fancy`)<br>`thaumcraft:baubles` (meta 6: `girdle_fancy`) | `thaumcraft_reborn:baubles_amulet_mundane`<br>`thaumcraft_reborn:baubles_ring_mundane`<br>`thaumcraft_reborn:baubles_girdle_mundane`<br>`thaumcraft_reborn:baubles_ring_apprentice`<br>`thaumcraft_reborn:baubles_amulet_fancy`<br>`thaumcraft_reborn:baubles_ring_fancy`<br>`thaumcraft_reborn:baubles_girdle_fancy` | Item con metadata | `ConfigItems.java:289` | Sin match exacto en `models/item` por ID aplanado. Nombres de variante coincidentes sin ID completo: amulet_mundane→`amulet_mundane.png` (nombre de variante, no ID literal), ring_mundane→`ring_mundane.png` (nombre de variante, no ID literal), girdle_mundane→`girdle_mundane.png` (nombre de variante, no ID literal), ring_apprentice→`ring_apprentice.png` (nombre de variante, no ID literal), amulet_fancy→`amulet_fancy.png` (nombre de variante, no ID literal), ring_fancy→`ring_fancy.png` (nombre de variante, no ID literal), girdle_fancy→`girdle_fancy.png` (nombre de variante, no ID literal). |
| `thaumcraft:amulet_vis` (meta 0: `found`)<br>`thaumcraft:amulet_vis` (meta 1: `crafted`) | `thaumcraft_reborn:amulet_vis_found`<br>`thaumcraft_reborn:amulet_vis_crafted` | Item con metadata | `ConfigItems.java:290` | Sin match exacto en `models/item` por ID aplanado. Assets base/overlay sin asignación inferida: `amulet_vis.png`, `amulet_vis_stone.png`. Sin match de nombre de modelo/textura: `found`, `crafted`. |
| `thaumcraft:verdant_charm` | `thaumcraft_reborn:verdant_charm` | Item | `ConfigItems.java:291` | `models/item/verdant_charm.json`. |
| `thaumcraft:curiosity_band` | `thaumcraft_reborn:curiosity_band` | Item | `ConfigItems.java:292` | `models/item/curiosity_band.json`. |
| `thaumcraft:voidseer_charm` | `thaumcraft_reborn:voidseer_charm` | Item | `ConfigItems.java:293` | `models/item/voidseer_charm.json`. |
| `thaumcraft:cloud_ring` | `thaumcraft_reborn:cloud_ring` | Item | `ConfigItems.java:294` | `models/item/cloud_ring.json`. |
| `thaumcraft:charm_undying` | `thaumcraft_reborn:charm_undying` | Item | `ConfigItems.java:295` | `models/item/charm_undying.json`. |
| `thaumcraft:creative_flux_sponge` | `thaumcraft_reborn:creative_flux_sponge` | Item | `ConfigItems.java:296` | `models/item/creative_flux_sponge.json`. |
| `thaumcraft:enchanted_placeholder` | `thaumcraft_reborn:enchanted_placeholder` | Item | `ConfigItems.java:297` | `models/item/enchanted_placeholder.json`. |
| `thaumcraft:caster_basic` | `thaumcraft_reborn:caster_basic` | Item | `ConfigItems.java:298` | `models/item/caster_basic.json`. |
| `thaumcraft:focus_1` | `thaumcraft_reborn:focus_1` | Item | `ConfigItems.java:299` | `models/item/focus_1.json`. |
| `thaumcraft:focus_2` | `thaumcraft_reborn:focus_2` | Item | `ConfigItems.java:300` | `models/item/focus_2.json`. |
| `thaumcraft:focus_3` | `thaumcraft_reborn:focus_3` | Item | `ConfigItems.java:301` | `models/item/focus_3.json`. |
| `thaumcraft:focus_pouch` | `thaumcraft_reborn:focus_pouch` | Item | `ConfigItems.java:302` | `models/item/focus_pouch.json`. |
| `thaumcraft:golem_bell` | `thaumcraft_reborn:golem_bell` | Item | `ConfigItems.java:303` | `models/item/golem_bell.json`. |
| `thaumcraft:golem` | `thaumcraft_reborn:golem` | Item | `ConfigItems.java:304` | `models/item/golem.json`. |
| `thaumcraft:seal` (meta 0: `blank`) | `thaumcraft_reborn:seal_blank` | Item con metadata | `ConfigItems.java:305` | models/item: blank→`seal_blank.json`. textures/items exactas: blank→`seals/seal_blank.png`. |

## BlockEntities

**Recuento:** 48 registros `registerTileEntity`; 48 mapeados, 0 pendientes.

| ID 1.12.2 | ID nuevo | Tipo | Origen | Notas |
|---|---|---|---|---|
| `thaumcraft:TileArcaneWorkbench` | `thaumcraft_reborn:arcane_workbench` | TileArcaneWorkbench | `ConfigBlocks.java:415` | ID del bloque `arcane_workbench`. |
| `thaumcraft:TileDioptra` | `thaumcraft_reborn:dioptra` | TileDioptra | `ConfigBlocks.java:416` | ID del bloque `dioptra`. |
| `thaumcraft:TileArcaneEar` | `thaumcraft_reborn:arcane_ear` | TileArcaneEar | `ConfigBlocks.java:417` | ID del bloque `arcane_ear`. |
| `thaumcraft:TileLevitator` | `thaumcraft_reborn:levitator` | TileLevitator | `ConfigBlocks.java:418` | ID del bloque `levitator`. |
| `thaumcraft:TileCrucible` | `thaumcraft_reborn:crucible` | TileCrucible | `ConfigBlocks.java:419` | ID del bloque `crucible`. |
| `thaumcraft:TileNitor` | `thaumcraft_reborn:nitor` | TileNitor | `ConfigBlocks.java:420` | Excepción AD-10: tipo compartido por varias variantes; `snake_case` de `Nitor`. |
| `thaumcraft:TileFocalManipulator` | `thaumcraft_reborn:wand_workbench` | TileFocalManipulator | `ConfigBlocks.java:421` | ID del bloque `wand_workbench`. |
| `thaumcraft:TilePedestal` | `thaumcraft_reborn:pedestal` | TilePedestal | `ConfigBlocks.java:422` | Excepción AD-10: tipo compartido por varias variantes; `snake_case` de `Pedestal`. |
| `thaumcraft:TileRechargePedestal` | `thaumcraft_reborn:recharge_pedestal` | TileRechargePedestal | `ConfigBlocks.java:423` | ID del bloque `recharge_pedestal`. |
| `thaumcraft:TileResearchTable` | `thaumcraft_reborn:research_table` | TileResearchTable | `ConfigBlocks.java:424` | ID del bloque `research_table`. |
| `thaumcraft:TileTube` | `thaumcraft_reborn:tube` | TileTube | `ConfigBlocks.java:425` | ID del bloque `tube`. |
| `thaumcraft:TileTubeValve` | `thaumcraft_reborn:tube_valve` | TileTubeValve | `ConfigBlocks.java:426` | ID del bloque `tube_valve`. |
| `thaumcraft:TileTubeFilter` | `thaumcraft_reborn:tube_filter` | TileTubeFilter | `ConfigBlocks.java:427` | ID del bloque `tube_filter`. |
| `thaumcraft:TileTubeRestrict` | `thaumcraft_reborn:tube_restrict` | TileTubeRestrict | `ConfigBlocks.java:428` | ID del bloque `tube_restrict`. |
| `thaumcraft:TileTubeOneway` | `thaumcraft_reborn:tube_oneway` | TileTubeOneway | `ConfigBlocks.java:429` | ID del bloque `tube_oneway`. |
| `thaumcraft:TileTubeBuffer` | `thaumcraft_reborn:tube_buffer` | TileTubeBuffer | `ConfigBlocks.java:430` | ID del bloque `tube_buffer`. |
| `thaumcraft:TileChestHungry` | `thaumcraft_reborn:hungry_chest` | TileHungryChest | `ConfigBlocks.java:431` | ID del bloque `hungry_chest`. |
| `thaumcraft:TileCentrifuge` | `thaumcraft_reborn:centrifuge` | TileCentrifuge | `ConfigBlocks.java:432` | ID del bloque `centrifuge`. |
| `thaumcraft:TileJar` | `thaumcraft_reborn:jar_normal` | TileJarFillable | `ConfigBlocks.java:433` | ID del bloque `jar_normal`. |
| `thaumcraft:TileJarVoid` | `thaumcraft_reborn:jar_void` | TileJarFillableVoid | `ConfigBlocks.java:434` | ID del bloque `jar_void`. |
| `thaumcraft:TileJarBrain` | `thaumcraft_reborn:jar_brain` | TileJarBrain | `ConfigBlocks.java:435` | ID del bloque `jar_brain`. |
| `thaumcraft:TileBellows` | `thaumcraft_reborn:bellows` | TileBellows | `ConfigBlocks.java:436` | ID del bloque `bellows`. |
| `thaumcraft:TileSmelter` | `thaumcraft_reborn:smelter` | TileSmelter | `ConfigBlocks.java:437` | Excepción AD-10: tipo compartido por varias variantes; `snake_case` de `Smelter`. |
| `thaumcraft:TileAlembic` | `thaumcraft_reborn:alembic` | TileAlembic | `ConfigBlocks.java:438` | ID del bloque `alembic`. |
| `thaumcraft:TileInfusionMatrix` | `thaumcraft_reborn:infusion_matrix` | TileInfusionMatrix | `ConfigBlocks.java:439` | ID del bloque `infusion_matrix`. |
| `thaumcraft:TileWaterJug` | `thaumcraft_reborn:everfull_urn` | TileWaterJug | `ConfigBlocks.java:440` | ID del bloque `everfull_urn`. |
| `thaumcraft:TileInfernalFurnace` | `thaumcraft_reborn:infernal_furnace` | TileInfernalFurnace | `ConfigBlocks.java:441` | ID del bloque `infernal_furnace`. |
| `thaumcraft:TileThaumatorium` | `thaumcraft_reborn:thaumatorium` | TileThaumatorium | `ConfigBlocks.java:442` | ID del bloque `thaumatorium`. |
| `thaumcraft:TileThaumatoriumTop` | `thaumcraft_reborn:thaumatorium_top` | TileThaumatoriumTop | `ConfigBlocks.java:443` | ID del bloque `thaumatorium_top`. |
| `thaumcraft:TileSpa` | `thaumcraft_reborn:spa` | TileSpa | `ConfigBlocks.java:444` | ID del bloque `spa`. |
| `thaumcraft:TileLampGrowth` | `thaumcraft_reborn:lamp_growth` | TileLampGrowth | `ConfigBlocks.java:445` | ID del bloque `lamp_growth`. |
| `thaumcraft:TileLampArcane` | `thaumcraft_reborn:lamp_arcane` | TileLampArcane | `ConfigBlocks.java:446` | ID del bloque `lamp_arcane`. |
| `thaumcraft:TileLampFertility` | `thaumcraft_reborn:lamp_fertility` | TileLampFertility | `ConfigBlocks.java:447` | ID del bloque `lamp_fertility`. |
| `thaumcraft:TileMirror` | `thaumcraft_reborn:mirror` | TileMirror | `ConfigBlocks.java:448` | ID del bloque `mirror`. |
| `thaumcraft:TileMirrorEssentia` | `thaumcraft_reborn:mirror_essentia` | TileMirrorEssentia | `ConfigBlocks.java:449` | ID del bloque `mirror_essentia`. |
| `thaumcraft:TileRedstoneRelay` | `thaumcraft_reborn:redstone_relay` | TileRedstoneRelay | `ConfigBlocks.java:450` | ID del bloque `redstone_relay`. |
| `thaumcraft:TileGolemBuilder` | `thaumcraft_reborn:golem_builder` | TileGolemBuilder | `ConfigBlocks.java:451` | ID del bloque `golem_builder`. |
| `thaumcraft:TileEssentiaInput` | `thaumcraft_reborn:essentia_input` | TileEssentiaInput | `ConfigBlocks.java:452` | ID del bloque `essentia_input`. |
| `thaumcraft:TileEssentiaOutput` | `thaumcraft_reborn:essentia_output` | TileEssentiaOutput | `ConfigBlocks.java:453` | ID del bloque `essentia_output`. |
| `thaumcraft:TilePatternCrafter` | `thaumcraft_reborn:pattern_crafter` | TilePatternCrafter | `ConfigBlocks.java:454` | ID del bloque `pattern_crafter`. |
| `thaumcraft:TilePotionSprayer` | `thaumcraft_reborn:potion_sprayer` | TilePotionSprayer | `ConfigBlocks.java:455` | ID del bloque `potion_sprayer`. |
| `thaumcraft:TileVisGenerator` | `thaumcraft_reborn:vis_generator` | TileVisGenerator | `ConfigBlocks.java:456` | ID del bloque `vis_generator`. |
| `thaumcraft:TileStabilizer` | `thaumcraft_reborn:stabilizer` | TileStabilizer | `ConfigBlocks.java:457` | ID del bloque `stabilizer`. |
| `thaumcraft:TileCondenser` | `thaumcraft_reborn:condenser` | TileCondenser | `ConfigBlocks.java:458` | ID del bloque `condenser`. |
| `thaumcraft:TileVoidSiphon` | `thaumcraft_reborn:void_siphon` | TileVoidSiphon | `ConfigBlocks.java:459` | ID del bloque `void_siphon`. |
| `thaumcraft:TileBanner` | `thaumcraft_reborn:banner` | TileBanner | `ConfigBlocks.java:460` | Excepción AD-10: tipo compartido por varias variantes; `snake_case` de `Banner`. |
| `thaumcraft:TileHole` | `thaumcraft_reborn:hole` | TileHole | `ConfigBlocks.java:461` | ID del bloque `hole`. |
| `thaumcraft:TileBarrierStone` | `thaumcraft_reborn:barrier` | TileBarrierStone | `ConfigBlocks.java:462` | ID del bloque `barrier`. |

## Entities

**Recuento:** 43 llamadas `registerModEntity`, todas con clave rastreable. Los nombres históricos con mayúsculas se pasan a minúsculas en la columna nueva porque `Identifier` de 26.3 no admite mayúsculas; no se inventan nombres snake_case.

| ID 1.12.2 | ID nuevo | Tipo | Origen | Notas |
|---|---|---|---|---|
| `thaumcraft:CultistPortalGreater` | `thaumcraft_reborn:cultistportalgreater` | EntityCultistPortalGreater | `ConfigEntities.java:82` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:CultistPortalLesser` | `thaumcraft_reborn:cultistportallesser` | EntityCultistPortalLesser | `ConfigEntities.java:94` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:FluxRift` | `thaumcraft_reborn:fluxrift` | EntityFluxRift | `ConfigEntities.java:106` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:SpecialItem` | `thaumcraft_reborn:specialitem` | EntitySpecialItem | `ConfigEntities.java:109` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:FollowItem` | `thaumcraft_reborn:followitem` | EntityFollowingItem | `ConfigEntities.java:112` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:FallingTaint` | `thaumcraft_reborn:fallingtaint` | EntityFallingTaint | `ConfigEntities.java:115` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Alumentum` | `thaumcraft_reborn:alumentum` | EntityAlumentum | `ConfigEntities.java:118` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:GolemDart` | `thaumcraft_reborn:golemdart` | EntityGolemDart | `ConfigEntities.java:121` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:EldritchOrb` | `thaumcraft_reborn:eldritchorb` | EntityEldritchOrb | `ConfigEntities.java:124` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:BottleTaint` | `thaumcraft_reborn:bottletaint` | EntityBottleTaint | `ConfigEntities.java:127` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:GolemOrb` | `thaumcraft_reborn:golemorb` | EntityGolemOrb | `ConfigEntities.java:130` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Grapple` | `thaumcraft_reborn:grapple` | EntityGrapple | `ConfigEntities.java:131` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:CausalityCollapser` | `thaumcraft_reborn:causalitycollapser` | EntityCausalityCollapser | `ConfigEntities.java:132` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:FocusProjectile` | `thaumcraft_reborn:focusprojectile` | EntityFocusProjectile | `ConfigEntities.java:142` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:FocusCloud` | `thaumcraft_reborn:focuscloud` | EntityFocusCloud | `ConfigEntities.java:145` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Focusmine` | `thaumcraft_reborn:focusmine` | EntityFocusMine | `ConfigEntities.java:148` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:TurretBasic` | `thaumcraft_reborn:turretbasic` | EntityTurretCrossbow | `ConfigEntities.java:151` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:TurretAdvanced` | `thaumcraft_reborn:turretadvanced` | EntityTurretCrossbowAdvanced | `ConfigEntities.java:154` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:ArcaneBore` | `thaumcraft_reborn:arcanebore` | EntityArcaneBore | `ConfigEntities.java:157` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Golem` | `thaumcraft_reborn:golem` | EntityThaumcraftGolem | `ConfigEntities.java:160` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:EldritchWarden` | `thaumcraft_reborn:eldritchwarden` | EntityEldritchWarden | `ConfigEntities.java:163` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:EldritchGolem` | `thaumcraft_reborn:eldritchgolem` | EntityEldritchGolem | `ConfigEntities.java:175` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:CultistLeader` | `thaumcraft_reborn:cultistleader` | EntityCultistLeader | `ConfigEntities.java:187` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:TaintacleGiant` | `thaumcraft_reborn:taintaclegiant` | EntityTaintacleGiant | `ConfigEntities.java:199` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:BrainyZombie` | `thaumcraft_reborn:brainyzombie` | EntityBrainyZombie | `ConfigEntities.java:211` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:GiantBrainyZombie` | `thaumcraft_reborn:giantbrainyzombie` | EntityGiantBrainyZombie | `ConfigEntities.java:223` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Wisp` | `thaumcraft_reborn:wisp` | EntityWisp | `ConfigEntities.java:235` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Firebat` | `thaumcraft_reborn:firebat` | EntityFireBat | `ConfigEntities.java:238` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Spellbat` | `thaumcraft_reborn:spellbat` | EntitySpellBat | `ConfigEntities.java:241` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Pech` | `thaumcraft_reborn:pech` | EntityPech | `ConfigEntities.java:244` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:MindSpider` | `thaumcraft_reborn:mindspider` | EntityMindSpider | `ConfigEntities.java:247` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:EldritchGuardian` | `thaumcraft_reborn:eldritchguardian` | EntityEldritchGuardian | `ConfigEntities.java:250` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:CultistKnight` | `thaumcraft_reborn:cultistknight` | EntityCultistKnight | `ConfigEntities.java:262` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:CultistCleric` | `thaumcraft_reborn:cultistcleric` | EntityCultistCleric | `ConfigEntities.java:265` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:EldritchCrab` | `thaumcraft_reborn:eldritchcrab` | EntityEldritchCrab | `ConfigEntities.java:277` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:InhabitedZombie` | `thaumcraft_reborn:inhabitedzombie` | EntityInhabitedZombie | `ConfigEntities.java:280` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:ThaumSlime` | `thaumcraft_reborn:thaumslime` | EntityThaumicSlime | `ConfigEntities.java:292` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:TaintCrawler` | `thaumcraft_reborn:taintcrawler` | EntityTaintCrawler | `ConfigEntities.java:295` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:Taintacle` | `thaumcraft_reborn:taintacle` | EntityTaintacle | `ConfigEntities.java:307` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:TaintacleTiny` | `thaumcraft_reborn:taintacletiny` | EntityTaintacleSmall | `ConfigEntities.java:310` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:TaintSwarm` | `thaumcraft_reborn:taintswarm` | EntityTaintSwarm | `ConfigEntities.java:313` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:TaintSeed` | `thaumcraft_reborn:taintseed` | EntityTaintSeed | `ConfigEntities.java:316` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |
| `thaumcraft:TaintSeedPrime` | `thaumcraft_reborn:taintseedprime` | EntityTaintSeedPrime | `ConfigEntities.java:319` | Se conserva el nombre de registro y se normaliza a minúsculas, requisito de `Identifier` 26.3; no se convierte a snake_case. |

## Pendientes

- `ItemSealPlacer` registra el nombre base `seal` y el subtipo `blank` en código, pero los subtipos restantes dependen del registro dinámico de seals. Aunque hay modelos `seal_*` en el árbol de assets, no se asignan IDs de metadata sin reconciliar cada registro dinámico con sus datos de modelo.
- Para `thaumonomicon`, `label`, `phial` y `amulet_vis`, algunos nombres de textura son base u overlay y no coinciden literalmente con las variantes del código; se muestran en las filas, sin inferir equivalencias. En particular, `amulet_vis_found`/`crafted` no se asignan a `amulet_vis.png`/`amulet_vis_stone.png` por nombre solamente.
- En `celestial_notes`, `sun` coincide sólo con el token de variante; las texturas están bajo `textures/items/celestial/`. Los nombres `stars1`/`moon1`…`stars4`/`moon8` no conservan los guiones bajos de `stars_1`/`moon_1` en el código, por lo que no se asignan por inferencia.
- Los items sin modelo homónimo se marcan en su fila; sus IDs proceden de la construcción/registro Java, no de inferirlos a partir de texturas. Deben verificarse con la política de modelos de la etapa de contenido.
- Diecinueve JSON de `blockstates/` no son evidencia de un bloque de `ConfigBlocks`: `amulet_vis`, `baubles`, `celestial_notes`, `chunk`, `cluster`, `creative_placer`, `crystal_essence`, `curio`, `ingot`, `label`, `loot_bag`, `mind`, `module`, `nugget`, `phial`, `plate`, `thaumometer`, `thaumonomicon` y `turret`. La mayoría corresponden a modelos/variantes de items y se registran en Items cuando hay una llamada rastreable; `creative_placer` queda pendiente de identificar en un registro Java.
- Los nombres de entidades conservan la secuencia original en minúsculas. La futura migración de referencias externas que dependan de la capitalización 1.12.2 debe tratarse en datos de compatibilidad, no mediante IDs inválidos.

## Comando de reconciliación

Conteos raw comprobados con `rg -c` (incluyen definiciones/helpers donde aplica):

```sh
rg -c 'registerBlock\(' /home/ubuntu/tc/src/thaumcraft/common/config/ConfigBlocks.java
rg -c 'ForgeRegistries\.BLOCKS\.register\(' /home/ubuntu/tc/src/thaumcraft/common/config/ConfigBlocks.java
rg -c 'iForgeRegistry\.register\(' /home/ubuntu/tc/src/thaumcraft/common/config/ConfigBlocks.java
rg -c 'iForgeRegistry\.register\(' /home/ubuntu/tc/src/thaumcraft/common/config/ConfigItems.java
rg -c 'registerTileEntity\(' /home/ubuntu/tc/src/thaumcraft/common/config/ConfigBlocks.java
rg -c 'registerModEntity\(' /home/ubuntu/tc/src/thaumcraft/common/config/ConfigEntities.java
find /home/ubuntu/tc/src/assets/thaumcraft/models/item -type f -name '*.json' | wc -l
find /home/ubuntu/tc/src/assets/thaumcraft/textures/items -type f -name '*.png' | wc -l
```

Valores obtenidos: `registerBlock(` 142; `ForgeRegistries.BLOCKS.register(` 16; `ConfigBlocks` `iForgeRegistry.register(` 3; `ConfigItems` `iForgeRegistry.register(` 108; `registerTileEntity(` 48; `registerModEntity(` 43; 155 modelos de item y 213 texturas de item. En `ConfigBlocks`, `registerBlock(` arroja 142 coincidencias raw; 4 son la declaración/cadena de helpers y 138 llamadas de inicialización. `ForgeRegistries.BLOCKS.register(` arroja 16, incluidas 2 dentro de helpers (`ConfigBlocks.java:470,478`); `iForgeRegistry.register(` añade 3 registros. Los 155 sitios/familias documentados agrupan 138 llamadas al helper, 12 registros slab, 2 registros banner (uno genera 16 colores) y 3 registros de bloques fluidos. La tabla expande las familias a 200 IDs distintos; los JSON de blockstates/modelos son contraste de assets, no fuente de registros.
