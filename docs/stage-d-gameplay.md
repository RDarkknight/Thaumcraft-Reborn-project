# Etapa D — Aprendiz de taumaturgo

## Auditoría de recetas e identidades (D0)

El informe de colisiones compara las recetas TC6 pertinentes y las recetas vanilla 26.3 empaquetadas en el JAR de Minecraft. No hay una receta de crafting TC6 con los mismos ingredientes que una receta vanilla y un resultado distinto; los parecidos de forma o las recetas de máquina con resultados vanilla no son colisiones de crafting. [RecipeMagicDust.java:16–98; ConfigRecipes.java:513–532,674–692,2723–2875; vanilla_recipe_collisions.md §Método y alcance, §Resultado]

| Caso | Identidad TC6 y comparación 26.3 | Resultado |
|---|---|---|
| `Items.DYE` metadata 0 | En `hedge_dye`, el ingrediente y resultado de metadata 0 son `minecraft:ink_sac`, no `minecraft:black_dye`. En 1.12 `dyeBlack` resuelve a `dye:0` (ink sac); por eso el tag local `#thaumcraft_reborn:dyes/black` une `#c:dyes/black` (incluye el miembro moderno `black_dye`) con `minecraft:ink_sac`, sin modificar `c:dyes/black`. [ConfigRecipes.java:2864–2875,2995–3003] |
| Activator rail arcano | La receta TC6 `activatorrail` consume un activator rail vanilla y produce el bloque propio `thaumcraft:activator_rail`, registrado como `BlockRailPowered`; no produce el rail vanilla. La receta vanilla de activator rail tiene entradas distintas (hierro, palos y antorcha de redstone). No hay colisión de entradas. [ConfigRecipes.java:513–515; ConfigBlocks.java:379–381; minecraft-common.jar!/data/minecraft/recipe/activator_rail.json] |
| Hedge alchemy | Los resultados son identidades vanilla: leather, gunpowder, slime ball, glowstone dust, ink sac, clay ball, string, cobweb y lava bucket. Se reutilizan los objetos vanilla; las recetas de hedge alchemy quedan para D4. [ConfigRecipes.java:246–254,2972–3039] |
| Charcoal y quartz | Las recetas de charcoal a partir de Greatwood/Silverwood y la receta de quartz de TC6 Quartz Ore son distintas de las recetas vanilla de esos resultados. La implementación de Stage C ya conserva esas entradas TC6 específicas. [ConfigRecipes.java:2911–2916; docs/stage-c-content.md §Recetas] |
| Triggers de bookshelf y crafting table | Fabric Convention Tags 2.0.0 incluye `c:bookshelves` (vanilla bookshelf) y `c:player_workstations/crafting_tables` (vanilla crafting table); se usan esos tags además de los triggers exactos TC6. [fabric-convention-tags-v2-4.10.3+6b5b47fd5d.jar!/data/c/tags/block/bookshelves.json; .../player_workstations/crafting_tables.json; ConfigRecipes.java:63–67] |

El activator rail TC6 es una identidad independiente, `thaumcraft:activator_rail`, implementada como `BlockRailPowered`; su receta arcana consume un activator rail vanilla y no colisiona con la receta vanilla de ingredientes distintos. No se registra ni se mapea al rail vanilla en D: queda diferido, pendiente de verificar su comportamiento. [ConfigRecipes.java:513–515; ConfigBlocks.java:379–381; `minecraft-common.jar!/data/minecraft/recipe/activator_rail.json`] La selección de recetas en Arcane Workbench se define independientemente: una receta arcana habilitada por research tiene prioridad; si el gate no está satisfecho, se muestra el resultado vanilla 3×3 cuando exista.

### Diferencias registradas

| Comportamiento TC6 | Comportamiento de Stage D | Motivo | Etapa objetivo |
|---|---|---|---|
| `activatorrail` produce `BlocksTC.activatorRail`, un `BlockRailPowered` propio, al consumir un rail vanilla. [ConfigRecipes.java:513–515; ConfigBlocks.java:379–381] | No se registra el bloque ni la receta. | La solicitud confirma que su identidad se conserva; su comportamiento necesita verificación antes de implementar el contenido. | Pendiente de verificación; fuera de D0–D3 |
| `dyeBlack` corresponde al ink sac de la Ore Dictionary 1.12. [ConfigRecipes.java:2864–2875] | Se usa el tag local `thaumcraft_reborn:dyes/black`, unión de `#c:dyes/black` y `minecraft:ink_sac`. | No se modifica `c:dyes/black`; el ink sac conserva la identidad TC6 y `black_dye` cubre el miembro moderno del mismo rol. | D4 usará `minecraft:ink_sac` para hedge_dye |

## Aura (D1)

La simulación corre en el tick de servidor, sin hilo dedicado. Cada nivel procesa el bucket `Math.floorMod(chunkX * 31 + chunkZ, 20)` correspondiente a `gameTime % 20`, y la guarda evita procesar dos veces el mismo nivel en un mismo `gameTime`. Sólo entran chunks cargados con attachment de aura. Los vecinos se consultan sin cargarlos; si no están cargados o no tienen datos de aura, no participan. Los adjuntos modificados se sustituyen con sus nuevos valores para que Fabric los persista.

Se conservan las tablas de TC6: `phaseVis = {0.25F, 0.15F, 0.1F, 0.05F, 0.0F, 0.05F, 0.1F, 0.15F}`, `phaseMax = 1 + {0.15F, 0.05F, 0.0F, -0.05F, -0.15F, -0.05F, 0.0F, 0.05F}` y `phaseFlux = 0.25F - phaseVis`. Cuando `vis + flux < base * phaseMax`, la regeneración es `min(base * phaseMax - (vis + flux), phaseVis)`. También se conservan las dos conversiones aleatorias del 10%, la selección barajada de los cuatro vecinos cardinales, las condiciones de difusión y los límites `[0, 32766]`. [AuraThread.java:21–26,83–145; AuraChunk.java:18–26,45–55]

La creación de chunks mantiene la fórmula ya portada en Etapa B: media del modificador del bioma central y cuatro cardinales a Y=50, `base = clamp((int)(modifier * 500 * (1 + gaussian * 0.1)), 0, 500)`, `vis = base`, `flux = 0`. [AuraHandler.java:173–190]

`drainVis` limita la extracción al vis disponible, retorna lo extraído y no modifica el chunk al simular; `addFlux` acepta sólo cantidades no negativas y respeta el clamp de `AuraChunkData`. `AuraHelper.polluteAura` de TC6 delega en `addFlux`; la fachada portada mantiene la operación detrás de `AuraAccess`. [AuraHandler.java:107–131,149–170; AuraHelper.java:25–27; AuraChunk.java:18–26,45–55]

El comando `/thaumcraft_reborn debug aura get` ahora informa fase, tiempo de mundo del último tick, coste en milisegundos y nanosegundos, y cantidad de chunks procesados. La sincronización no se convierte en un broadcast por tick: cada 20 ticks el servidor envía el estado del chunk actual sólo al jugador que tiene el Thaumometer seleccionado o en hotbar; no se envían actualizaciones si falta el chunk de aura. El paquete actualiza únicamente la caché de aura del cliente. TC6 también muestra la condición de Flux cuando `flux > vis` o `flux > base / 3`. [ItemThaumometer.java:53–57,97–109; PacketAuraToClient.java:11–45]

Los triggers de rift y Flux Goo no se implementan en este handoff; quedan fuera de la simulación D1. [AuraThread.java:148–150; BlockTaint.java:72–83; BlockTaintFeature.java:67–75]

| Comportamiento TC6 | Comportamiento de Stage D | Motivo | Etapa objetivo |
|---|---|---|---|
| Un `AuraThread` por dimensión duerme hasta completar intervalos de 1000 ms y procesa sus chunks en ese hilo. [AuraThread.java:16–18,32–70] | La simulación se ejecuta en el tick de servidor y distribuye chunks entre 20 buckets, de modo que cada chunk se procesa una vez por cada 20 ticks de mundo. | No se ejecuta lógica de mundo en un hilo adicional; la carga queda repartida durante el tick normal. | D1 |
| Los rifts pueden marcarse al superar el umbral de flux; los cambios de taint pueden producir Flux Goo. [AuraThread.java:148–150; BlockTaint.java:72–83; BlockTaintFeature.java:67–75] | No se implementan el trigger de rift ni la ecología de Flux Goo. | El plan reserva esas mecánicas y sus bloques para Stage E. | Stage E |

Los GameTests cubren regeneración en cada fase, ambas conversiones, los límites estrictos de difusión de vis/flux, clamp, vecinos descargados, la guarda por `gameTime` y el round-trip del attachment persistente.

## Salis Mundus y triggers (D2)

La receta shapeless de Salis Mundus exige bowl, flint, redstone y exactamente tres crystal essences de aspectos distintos; no fija posiciones, requiere área de al menos seis celdas y devuelve bowl y flint. [RecipeMagicDust.java:16–98] Los gates portados son `flag/got_dream` para bookshelf, `FIRSTSTEPS@1` para crafting table y `UNLOCKALCHEMY@1` para cauldron, siguiendo los tres triggers simples de `ConfigRecipes`. [ConfigRecipes.java:62–67]

Los triggers exactos y los basados en tags comunes `c:bookshelves` y `c:player_workstations/crafting_tables` se mantienen en un registro ordenado y pasan por la misma API de transformación. Al activarse, el servidor consume un polvo salvo en creativo, registra el resultado y programa la transformación a 50 ticks; agacharse devuelve `PASS` y se respeta el permiso de interacción. Al ejecutarse el swap, se comprueba que el bloque siga en el estado original, pero no se reevalúa research: TC6 comprueba el gate al elegir el trigger (`DustTriggerSimple.getValidFace`) y el swap diferido usa `DEFAULT_PREDICATE`, que siempre devuelve `true`; el swapper comprueba el estado original y el predicado, no research. [DustTriggerSimple.java:28–50; ServerEvents.java:62–65,208–255] La estantería se reemplaza por aire y genera un solo Thaumonomicon en `(x+0.5, y+0.1, z+0.5)` con velocidad cero; la crafting table cambia a Arcane Workbench y el cauldron a Crucible. [ServerEvents.java:295–315]

El efecto local despacha 50 sparks con el FX propio del mod y usa un evento de sonido propio respaldado por el sonido vanilla de mesa de encantamientos; no se añaden assets de TC6. TC6 emite 50 sparks y `SoundsTC.dust` al aceptar un trigger. [ItemMagicDust.java:59–110]

El trigger de bookshelf no concede research directamente: deja caer el Thaumonomicon, y su recogida concede `flag/got_thaumonomicon`; el marcador `flag/got_dream` es el gate del trigger. [ConfigRecipes.java:63–64; DustTriggerSimple.java:35–50; PlayerEvents.java:130–149]

| Comportamiento TC6 | Comportamiento de Stage D | Motivo | Etapa objetivo |
|---|---|---|---|
| Registra triggers exactos y Ore Dictionary; también define triggers multiblock para máquinas y altares. [ConfigRecipes.java:62–169] | Se portan sólo bookshelf, crafting table y cauldron, usando tags comunes además de los bloques vanilla exactos. La API conserva resultados de múltiples posiciones para extensiones futuras. | Las estructuras grandes no forman parte del alcance de D2. | Pendiente; fuera de D0–D3 |
| Usa `SoundsTC.dust` y su sistema de partículas TC6. [ItemMagicDust.java:59–110] | FX y SoundEvent propios; la ráfaga usa partículas placeholder del mod y sonido vanilla. | No se importan assets originales. | D2 |

Los GameTests cubren gate ausente/presente para los tres triggers, bloque incorrecto, agacharse, creativo, retraso de 50 ticks, registro del resultado, aspectos distintos, sobrantes bowl/flint y rechazo de una cuadrícula 2×2.

## Arcane Workbench (D3)

El block entity conserva 15 slots: 0–8 son la cuadrícula 3×3 y 9–14 los seis cristales primales; la salida se presenta en un slot aparte no insertable. Los slots de cristal sólo aceptan esencia del aspecto de su posición. [TileArcaneWorkbench.java:14–44; InventoryArcaneWorkbench.java:8–22; ContainerArcaneWorkbench.java:38–62; SlotCrystal.java:9–27]

La receta arcana se prefiere cuando su investigación está desbloqueada; si el gate falla, se busca crafting vanilla en la misma cuadrícula. Vista previa y retirada comparten validación del match idéntico, cristales y `drainVis(..., simulate=true) >= coste`; se revalida y se paga antes de consumir. Si el pago real falla, se devuelve al cursor el stack entregado y no se consumen recursos. Se drena `(int)(vis * (1 - discount))`; las Goggles descuentan 5 %. Se consumen cristales e ingredientes, se procesan sobrantes vanilla y se registra el craft. Esta ruta no genera flux. El shift-click sí está permitido en TC6 y se implementa con comprobación de espacio y rollback seguro. [ContainerArcaneWorkbench.java:191–243; SlotCraftingArcaneWorkbench.java:76–141; CasterManager.java:24–59; ItemGoggles.java:82–85]

Sin charger TC6 sólo consulta y drena el chunk local de la mesa; el área de nueve chunks requiere el charger, que está fuera de D3. [TileArcaneWorkbench.java:56–105] El contenido de esta entrega registra la receta del Thaumometer (`FIRSTSTEPS@2`, 20 vis y una esencia de cada aspecto primal) y la receta de Goggles (`UNLOCKARTIFICE`, 50 vis, patrón `LGL/L L/TGT`, `L` `c:leathers`, `G` `c:ingots/brass` y `T` Thaumometer); Goggles no requiere cristales. [ConfigRecipes.java:517–532,674–692] El parser normaliza la clave de research a minúsculas, así que `firststeps@2` es válido. [ResearchGate.java:25–47]

Fabric Convention Tags no define `c:ingots/brass` y Stage C no registra brass. Se declara el tag común vacío (`replace: false`) para que el codec de ingredientes de Minecraft 26.3 pueda cargar la receta; conserva el ingrediente `ingotBrass` de TC6 sin sustituirlo por otro material. Goggles seguirá sin entradas craftables hasta que D4 aporte brass al tag. [ConfigRecipes.java:674–692]

| Comportamiento TC6 | Comportamiento de Stage D | Motivo | Etapa objetivo |
|---|---|---|---|
| El coste arcano puede descontar equipo y pociones; el charger permite pagar desde nueve chunks. [ContainerArcaneWorkbench.java:112–123; TileArcaneWorkbench.java:56–105] | Se implementa el descuento de 5 % de Goggles y el aura local; no se implementan pociones ni charger. | La integración de pociones y charger queda fuera de este handoff. | Etapa posterior; no asignada |
| La pantalla presenta los recursos y texturas de TC6. [ContainerArcaneWorkbench.java:43–62] | Pantalla propia placeholder, sin assets de TC6. | La presentación final queda fuera de D3. | Etapa posterior |
| Las Goggles requieren `ingotBrass`. [ConfigRecipes.java:674–692] | La receta usa `c:ingots/brass`, declarado vacío hasta que se añada el ítem. | El codec de Minecraft 26.3 rechaza referencias a tags inexistentes; no se inventa ni sustituye brass. | D4 |

Los GameTests verifican coste de 20 vis y de 19 con Goggles, slot de cristal, fallback con Thaumometer y research desconocido, vis insuficiente entre preview y take, consumo normal y shift-craft, y persistencia del inventario del block entity sin duplicación al cerrar. El client GameTest abre la pantalla y guarda una captura.

## Crucible (D4)
### D4 pila arrojada y salida

La colisión de una `ItemEntity` procesa la pila completa: cada unidad intenta primero una receta y, si no hay receta aplicable, se disuelve sólo cuando existe una resolución de aspectos. La pila devuelta reemplaza el contenido de la entidad; se elimina cuando queda vacía. La interacción manual sigue pasando una sola unidad. La eyección mantiene la posición y velocidad de `TileCrucible.ejectItem`. [TileCrucible.java:211–263,300–317]

## Research Table (D5)

La mesa de madera se transforma en Research Table al usar herramientas de escritura; la receta es la de TC6 con tags modernos de tablones y losas. Las herramientas apilan a uno, tienen durabilidad 100, y se fabrican con botella, pluma y `#thaumcraft_reborn:dyes/black`; su recarga sigue la receta TC6. La variante con phial queda diferida. [ConfigRecipes.java:160–177; BlockTable.java:47–64; ItemScribingTools.java:6–12]

El estado completo de sesión vive en `ResearchTableBlockEntity` mediante `ResearchTableData`; registros extensibles `TheorycraftCard`/`TheorycraftAid` viven en `api`. La sesión parte de `round(5 + 0.5 × SPIKY + 0.1 × HIDDEN)`, con research estrictamente conocido, límite 15 y resta de un punto por ayuda detectada. BASICS siempre está disponible; ALCHEMY requiere `UNLOCKALCHEMY` estrictamente conocido. En D5 sólo se ofrecen esas dos categorías, aun cuando haya más categorías registradas. Cada draw consume un papel, ofrece dos cartas (tres con bonus), y filtra por coste y categoría. Un aid puede entrar con probabilidad `<= 0.25`. Los aids recorren x,z `-4..4`, y `-1..1`; bookshelf aporta Balance, dos Notation y tres Study; Crucible aporta Concentrate, Reactions y Synthesis. El BE persiste inspiración inicial/actual, totales por categoría, bonus draws, ofertas, ayudas detectadas/cartas de ayuda, penalización y las semillas guardadas de cartas elegidas. [TheorycraftSystem.java:74–126,146–175,267–300; ResearchTableData.java:14–48,87–118,220–285,342–389; AidBookshelf.java:5–14; AidBasicAlchemy.java:7–16]

El servidor recibe start/draw/pick por `clickMenuButton`, valida propietario, estado, materiales, papel y requisitos/coste de las cartas. A diferencia de TC6, start también valida en servidor las herramientas y el papel. Se conserva el quirk de TC6: fallar el consumo de tinta no bloquea la elección de una carta. Al terminar se concede THEORY por Knowledge API con `round(c / 100F * 32)` y penalización `(int) max(1, tot * 0.666666667)` desde `penaltyStart`. [ContainerResearchTable.java:48–111; TileResearchTable.java:78–100,141–160]

Cartas incluidas, con todos los costes y efectos del subconjunto BASICS/ALCHEMY:

| Carta | Coste / requisitos y consumos | Efecto |
|---|---|---|
| Study (sólo aid) | 1 | +15..25 a categoría disponible aleatoria. [CardStudy.java:27–80] |
| Analyze | 2; requiere y consume un nivel OBSERVATION de la categoría elegida | +5 BASICS y +25..50 a esa categoría. [CardAnalyze.java:35–82] |
| Balance | 1; sólo aid | iguala totales activos, +5 BASICS y `penaltyStart +1`. [CardBalance.java:27–69] |
| Notation (sólo aid) | 1 | transfiere un total positivo entre dos categorías: quita todo del origen y entrega entre la mitad y todo al destino. [CardNotation.java:27–77] |
| Ponder | 2 | distribuye 25 puntos entre categorías activas, +5 BASICS y un draw bonus. [CardPonder.java:27–64] |
| Rethink | 2 | quita hasta 10 puntos repartidos de totales, +1 draw bonus y +1..10 BASICS. [CardRethink.java:27–69] |
| Reject | 0 | bloquea una categoría para la sesión y +5 BASICS. [CardReject.java:27–55] |
| Experimentation | 2 | +15..30 a una categoría registrada y +1..10 BASICS. [CardExperimentation.java:27–57] |
| Inspired | 2 | añade la cantidad del offer a una categoría activa. [CardInspired.java:27–59] |
| Concentrate | 1; requiere un cristal de aspecto, no consumido | +15 ALCHEMY, draw bonus y probabilidad 0.33 de +1 inspiración. [CardConcentrate.java:38–73] |
| Reactions | 1; requiere dos cristales de aspectos compuestos distintos, no consumidos | +25 ALCHEMY y probabilidad 0.33 de +1 inspiración. [CardReactions.java:34–88] |
| Synthesis | 1; requiere y consume los dos cristales componentes | crea el cristal compuesto, +40 ALCHEMY y probabilidad 0.33 de +1 inspiración. [CardSynthesis.java:37–95] |

Se omiten las restantes cartas de `ConfigResearch.initTheorycraft`: Curio y las cartas de encantamiento, beacon, celestialidad, calibración, tinkering/foci/infusión/golems/portales/eldritch requieren entidades, bloques o sistemas posteriores que no están en D; sus requisitos/efectos no se sustituyen por no-ops. [ConfigResearch.java:357–405]

## Progresión de research (D6)

El botón «Advance stage» de la pantalla mínima envía un payload C2S; el servidor vuelve a resolver la entrada y prevalida research padre, `required_knowledge`, `required_item`, `required_craft` y `required_research`. Knowledge se descuenta y los ítems requeridos se consumen sólo tras validar todos los costes; después se ejecuta el avance existente. Es una ruta server-side equivalente para este esquema a `PacketSyncProgressToServer.checkRequisites` → `ResearchManager.progressResearch`; TC6 valida crafts como flags `[ # ]`, consume `obtain` y descuenta knowledge bruto según la progresión del tipo. [ResearchProgression.java:69–101,153–209; PacketSyncProgressToServer.java:76–155; ResearchManager.java:98–155]

Los flags de pickup se otorgan al adquirir un cristal y Thaumonomicon; `got_dream` se otorga al despertar después de `got_crystals`. El Thaumometer añade OBSERVATION al escanear por primera vez y registra claves de scan. `FIRSTSTEPS` exige en orden el flag Thaumonomicon y crafting de Workbench; después requiere el craft de Thaumometer más OBSERVATION BASICS. `UNLOCKALCHEMY` exige OBSERVATION ALCHEMY y BASICS, luego craft de Crucible y Nitor. `METALLURGY` requiere OBSERVATION ALCHEMY/craft de brass, y THEORY ALCHEMY/craft de thaumium. [PickupResearchFlags.java:23–47; ThaumometerSystem.java:186–242; firststeps.json:9–57; unlockalchemy.json:9–65; metallurgy.json:9–64]

El GameTest E2E «Aprendiz de taumaturgo» recorrió la secuencia completa hasta completar METALLURGY y producir Thaumium: pickup de cristal/despertar, crafting recipe de Salis, transformaciones con `useOn`, pickup del Thaumonomicon, avances validados de FIRSTSTEPS/UNLOCKALCHEMY/METALLURGY, Arcane Workbench, escaneos, Crucible, Research Table vía `clickMenuButton` y Thaumium. Usó materias primas vanilla y cristales; la perla y el polvo de blaze se combinaron con la receta vanilla de Eye of Ender, y la caña de azúcar se crafteó a papel. La teoría usa una fuente aleatoria sembrada por UUID sólo durante el GameTest, limpiada al terminar; no cambia selección ni reglas de producción. [ApprenticeEndToEndGameTest.java; firststeps.json:9–57; unlockalchemy.json:9–65; basealchemy.json:9–33; metallurgy.json:9–64]

El Client GameTest abre las pantallas de Research Table y Thaumonomicon con el botón de avance visible; cambia el aura en el servidor, ejecuta la ruta `AuraSyncPayload` y comprueba en la caché usada por el HUD los valores `base=100`, `vis=321`, `flux=12`. [ThaumometerSystem.java:46–52,91–109; ThaumometerClient.java:34–45]

El Crucible usa un bloque no opaco con block entity propio. El estado persistido conserva calor `0..200`, agua de capacidad `1000 mB` y `AspectList`; el cliente sincroniza agua, aspectos y cruces de `heat > 150`, no cada tick. [TileCrucible.java:40–54; BlockCrucible.java:100–140]

Cada tick calienta `+1` hasta `200` con agua y lava, fuego, soul fire, magma block o nitor amarillo debajo; en otro caso enfría `-1` hasta `0`. El contador empieza en `-100`, con derrame al superar `500` de vis o llegar a `100`; procesar disolución lo fija a `-150` y receta a `-250`. Campfire queda fuera. [TileCrucible.java:41–50,75–110,217–241]

Con `heat > 150` y agua, cada unidad de la pila arrojada intenta una receta que exige jugador online, research estricto, catalizador y aspectos; se escoge el mayor `visSize`. Cada resultado consume `50 mB` y sólo los aspectos requeridos, conserva sobrantes, registra el craft y se eyecta; sin receta, `AspectLookup` final disuelve la unidad sólo si hay aspectos, sin gastar agua. La `ItemEntity` se actualiza con el resto o se descarta vacía. La entrega manual sigue procesando una unidad. La eyección mantiene la posición y velocidad de TC6. El cap `500/aspecto` queda para Etapa E. [TileCrucible.java:211–276,300–317; ThaumcraftCraftingManager.java:50–69; CrucibleRecipe.java:49–78]

`spillRandom` elimina una unidad y añade `1.0F` de flux para vitium o `0.25F` para otro aspecto. `spillRemnants` vacía agua/aspectos y añade `visSize * 0.25F + vitium * 0.75F`; agacharse con mano vacía y romper usan ese camino. El cubo llena `1000 mB` y devuelve cubo salvo creativo; extracción con cubo vacío es **UNKNOWN**. [TileCrucible.java:293–318; BlockCrucible.java:100–107,152–158]

Los ítems usan el owner/thrower para resolver `ServerPlayer`; las entidades vivas reciben `1.0F` in-fire tras diez callbacks. Se añadieron nitor, brass, alumentum, tallow y clusters; la fundición usa 2 lingotes y `1.0 XP`. La receta de cobre es una desviación moderna documentada: el smelting TC6 comprobado no contiene metadata 2. [BlockCrucible.java:35,47–75; TileCrucible.java:266–276; ConfigRecipes.java:2723–2760]

Se excluyen `focus_1`, `LiquidDeath`, `BottleTaint`, `BathSalts`, `SaneSoap` y `EverfullUrn`; mostrar aspectos apuntados por Goggles/Thaumometer queda diferido.
