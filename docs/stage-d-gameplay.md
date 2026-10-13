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
