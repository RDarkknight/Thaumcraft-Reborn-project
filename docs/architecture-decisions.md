# Decisiones arquitectónicas

> Registro breve de decisiones cerradas. Prevalecen sobre las marcas `requiere decisión` / `[Recomendación]` de `implementation-roadmap.md` y `fabric-26.3-architecture.md`. Una decisión sólo se reabre si el proyecto lo pide de forma explícita.
> Referencias: `Dn`/`Rn`/§ = `fabric-26.3-architecture.md`; «Etapa X» = `implementation-roadmap.md`.

## AD-00 — Principio de fidelidad

- Thaumcraft 6 (1.12.2, 6.1.BETA26) es la referencia de **comportamiento y gameplay**. La arquitectura de Fabric/Minecraft 26.3 se adapta a esas mecánicas; no las simplifica ni las reemplaza.
- Si 26.3 obliga a cambiar algo, se cambia la **implementación**, no la mecánica.
- Si una mecánica todavía no se puede implementar correctamente, se aísla detrás de una API/fachada; no se sustituye por una aproximación permanente.
- Toda simplificación de gameplay se registra en la tabla de [Simplificaciones](#simplificaciones-registradas) con su justificación y la etapa en que se elimina. Las que no figuren ahí no están permitidas.

## AD-01 — Decisiones ya confirmadas (sin cambios)

Siguen vigentes las de `fabric-26.3-architecture.md` § *Confirmed architectural decisions*: MC 26.3 + Fabric + Java 25; Iris/Sodium como objetivo de compatibilidad, no como dependencia; reconstrucción, no migración; Trinkets Updated detrás de `AccessoryAccess`; Magical Forest como bioma real; namespace `thaumcraft_reborn`.

## AD-02 — Assets

- Los assets originales de TC6 son la referencia y la base visual y sonora. No se rediseñan; sólo se modifican cuando el formato de 26.3 lo exige (por ejemplo, la conversión de modelos, la división de texturas con metadata o los `.mcmeta`).
- **Procedencia separada de la lógica:** los recursos derivados de TC6 viven en una raíz propia, `src/tc6/resources/` (que se añade a los recursos de `main` cuando se importe el primero), junto con un `PROVENANCE.md` que registra *ruta original → ruta nueva → modificación técnica*. Ese directorio no contiene código. Los placeholders y los recursos propios siguen en `src/main/resources/`.
- La Etapa A no importa assets: todavía no hay contenido de TC6 que los use.
- Sigue abierto **(requiere decisión humana)**: la licencia de redistribución de esos assets (R1) antes de cualquier publicación.

## AD-03 — Registries y datos

- **Tipos definidos en código** (aspectos, tipos de FX y, más adelante, tipos de foci, partes de golem…): registries estáticos propios creados con `FabricRegistryBuilder` y el atributo `SYNCED`. Cierra D3.
- **Contenido definido por datos** (mapeos de aspectos, research, multibloques…): un cargador recargable propio (`SyncedDataLoader<T>`) sobre `ResourceLoader` v1 con `Codec<T>`. Se sincroniza con el cliente mediante payload al conectarse y después de cada `/reload`. Cierra D4. Los *reloadable registries* de vanilla quedan descartados por ahora, porque su sincronización con el cliente no está verificada.
- Convención de rutas: `data/<namespace>/thaumcraft_reborn/<tipo>/<ruta>.json`.
- **Sin sobrescrituras silenciosas:** cuando más de un pack define la misma entrada, el cargador registra en el log qué pack prevalece.
- No se reproducen los eventos ni la arquitectura de registro de Forge 1.12.2.

## AD-04 — Aspectos

- Se mantienen los 37 aspectos de TC6: 6 primales y 31 compuestos, con sus componentes y colores originales.
- `AspectList` es un tipo fundamental de `api`, inmutable en su interfaz pública, con `Codec` y `StreamCodec`. Lo reutilizan los ítems, los bloques, las entidades, las recetas, la essentia y la red.
- Las asignaciones objeto → aspectos son **explícitas y data-driven** (AD-03): sin derivación automática desde recetas. Esto sustituye la recomendación de §6.1 y la opción de R6.
- Compatibilidad con otros mods: cada mod aporta archivos en su propio namespace, que se **suman** a los datos originales. Reemplazar una asignación original exige una marca explícita (`replace`) y queda registrado en el log. El formato exacto se define en la Etapa B.

## AD-05 — Aura / Vis / Flux

- El aura es un sistema propio por chunk (`base`, `vis`, `flux`), no un mana genérico.
- La fachada `AuraAccess` vive en `api`. Los consumidores (crafting arcano, casters, accesorios, HUD, worldgen) sólo la usan a través de esa fachada y nunca dependen de la simulación.
- La Etapa B puede incluir un modelo mínimo/mock detrás de `AuraAccess`; está registrado como simplificación temporal.
- La simulación completa (Etapa D) reproduce el comportamiento de `AuraHandler`/`AuraThread` de TC6: regeneración, consumo, difusión entre chunks, generación de flux y sus eventos. Corre en el tick del servidor en vez de un hilo propio, que es un cambio de implementación y no de mecánica.

## AD-06 — Magical Forest

- La implementación completa del bioma queda en la Etapa F.
- La fachada `BiomePlacementAccess` se define antes (Etapa B), sin implementación.
- Ningún sistema anterior a F depende de que el bioma exista físicamente: el aura base y similares se clasifican mediante tags de bioma propios (`thaumcraft_reborn:...`), en los que Magical Forest entrará cuando exista.
- La elección entre TerraBlender y Biolith (D6) se cierra antes de la Etapa F.

## AD-07 — Knowledge / Research / Theory

- El modelo de conocimiento del jugador se diseña desde la Etapa B para el sistema definitivo de TC6: stages de research, puntos de conocimiento por categoría y tipo (OBSERVATION / THEORY), flags y warp.
- Los campos de ítems de research usan `ItemReference` (ID, count y patch de componentes) y se resuelven sólo al usarlos. En Minecraft 26.3 el registro enlaza componentes después de terminar los reload listeners, así que `ItemStack.CODEC` no puede decodificar esos campos durante el reload; los IDs aún no registrados deben preservar la entrada de research.
- **No existen fuentes temporales de THEORY.** THEORY sólo se obtiene por theorycrafting, como en TC6.
- Para el primer milestone (Etapa D) se puede implementar una Research Table / theorycrafting **simplificada**: un subconjunto de cartas y aids sobre las mismas abstracciones, datos y codecs que usará el sistema completo (Etapa H), sin un modelo paralelo. Cierra §6.2 del roadmap (opción 1).

## AD-08 — Configuración

- JSON + `Codec`. Cierra D13. Archivos: `config/thaumcraft_reborn/common.json` y `config/thaumcraft_reborn/client.json`.
- Las claves conservan los nombres, los grupos y los valores por defecto de `ModConfig` de TC6, en snake_case (por ejemplo, `CONFIG_MISC.wussMode` → `misc.wuss_mode`). Cada clave la añade la etapa que la consume.
- El grupo `debug` es propio del port (no existe en TC6) y sólo contiene claves de diagnóstico de infraestructura.
- Los valores comunes que afectan al gameplay se sincronizan del servidor al cliente; los del cliente nunca viajan.
- Un archivo ausente se crea con los valores por defecto. Un archivo inválido se registra en el log y se usan los valores por defecto, sin sobrescribir el archivo del usuario.

## AD-09 — Persistencia de BlockEntities

- Se usan `ValueInput`/`ValueOutput` de 26.3 con codecs. Cierra D5.
- Una base común (`ThaumcraftBlockEntity`) centraliza la sincronización con el cliente (update tag + packet), y otra (`ThaumcraftContainerBlockEntity`) centraliza los inventarios.

## AD-10 — Tabla de ids 1.12.2 → 26.3

- `docs/id-mapping.md` es la fuente de verdad.
- Reglas: el namespace pasa a `thaumcraft_reborn`; la ruta se conserva tal cual (los nombres de TC6 ya están en snake_case); las variantes por metadata se aplanan como `<base>_<variante>`, igual que nombra TC6 sus texturas y modelos (`ingot` meta `thaumium` → `ingot_thaumium`).
- No se renombra nada por estilo. Cualquier excepción se justifica en la propia tabla.

## AD-11 — Mixins como último recurso de integración

- Se usan Mixins sólo cuando Fabric API no ofrece un hook público para el punto necesario. Los actuales son `PlayerPickupFlagMixin` en el hook de recogida servidor de `ItemEntity.playerTouch` (Fabric API no tiene evento de recogida de items) y `ContainerScreenAccessor` para leer `hoveredSlot` en cliente.
- La lógica de gameplay permanece en `systems`; cada mixin se limita a adaptar el hook o acceso y delegar en la lógica correspondiente.
- Los configs de mixin usan `JAVA_25`, soportado por la versión de Mixin incluida en Fabric.

## AD-12 — Goggles y Trinkets Updated

- `AccessoryAccess` es la fachada para el equipo accesorio. La lógica de gameplay no importa tipos de Trinkets; el código Trinkets de producción vive sólo en `compat.trinkets`. Los datos de slots y tags bajo `data/trinkets/...` forman parte de esa compatibilidad.
- Las Goggles usan el slot `head/face`, equivalente a `BaubleType.HEAD` de TC6, y siguen funcionando en el slot de casco. Trinkets Updated 4.2.1 ya incluye la definición estándar `head/face`; este mod añade la asignación a jugadores y el tag de Goggles. Llevarlas en el slot accesorio no aplica atributos de armadura vanilla.
- Quedan diferidos el renderizado de Goggles equipadas como Trinkets (TC6 `IRenderBauble`) y el descuento del 5 % de vis, que necesita los costes de vis de la Etapa D.

## AD-13 — Capa de bonus de aspectos

- La resolución de objetos sigue `getBaseAspects` → proveedores ordenados `AspectBonusProvider` → `AspectLimiter`. Cada proveedor transforma la lista anterior y puede reemplazarla, como hacen los contenedores de essentia; no se presupone que siempre sume aspectos.
- `AspectBonusProviders` registra proveedores por `Identifier` durante el bootstrap, rechaza ids duplicados y expone una instantánea inmutable en orden de registro. `AspectLimiter` es instalable y empieza como `IDENTITY`.
- En la Etapa C la capa es inerte: no se registra ningún proveedor y el limiter no se sustituye. La base se cachea por ítem; la resolución final ocurre bajo demanda, sin caché por stack. Una caché futura tendría que considerar los componentes del stack.
- En la Etapa E se implementará la capa completa: armadura (`praemunio`), espada (`aversio`), arco, herramientas (`instrumentum`), tinte (`sensus`), encantamientos, contenedores de essentia y el limiter de TC6 (culling a 7 aspectos y cap de 500 por cantidad).

## AD-14 — Aspectos de objetos nuevos de 26.3

- El dataset base sigue siendo estrictamente TC6 y vive en archivos `tc6_*`; no se inventan valores para objetos exclusivos de 26.3.
- La Etapa H añadirá un dataset de compatibilidad moderna separado, en archivos claramente nombrados (por ejemplo, `modern_*`), sin mezclarlo con `tc6_*`. Así se distingue siempre un aspecto original TC6 de uno de compatibilidad moderna. Es trabajo diferido, no descartado.
- Los `{}` de override existentes para 74 ids exclusivos de 26.3 en la capa generada significan «sin valor TC6». La capa moderna podrá reemplazarlos explícitamente con `replace`, según AD-04.

## Simplificaciones registradas

| Simplificación | Motivo | Aislada detrás de | Se elimina en |
|---|---|---|---|
| Aura mínima: regeneración de vis por fase lunar cada 20 ticks; sin difusión, decaimiento, comportamiento de vis bajo, propagación de flux ni rifts | Desbloquea consumidores de la Etapa B/C | `AuraAccess` | Etapa D |
| El attachment de aura no se sincroniza al cliente | HUD/thaumometer aún no existen | `AuraAccess` | Etapa C |
| `addFlux(..., showEffect)` no emite efectos | La política de FX pertenece a la simulación completa | `AuraAccess` | Etapa D |
| Sin packets de ganancia de conocimiento ni popups de interfaz | No hay UI de investigación | `KnowledgeAccess` | Etapa C |
| Sin aspectos de bonificación por armadura, herramientas, encantamientos, pociones o contenedores | Se conserva la resolución base durante C | `AspectBonusProvider` / `AspectLimiter` (AD-13) | Etapa E |
| Las Goggles no se renderizan al llevarlas en un slot de Trinkets | El renderizado equivalente a TC6 `IRenderBauble` queda separado del slot funcional | `AccessoryAccess` (AD-12) | Etapa H |
| Research Table simplificada (subconjunto de cartas) | Primer milestone | Abstracciones de theorycrafting (AD-07) | Etapa H |

## Conflictos detectados y corregidos

- `implementation-roadmap.md`: se eliminan la opción de una fuente temporal de THEORY (§6.2) y la derivación de aspectos desde recetas; §7 pasa a referenciar estas decisiones. Las desviaciones de §3 quedan aprobadas.
- `fabric-26.3-architecture.md` (PR #2, sin mergear): §6.1 (derivación de aspectos), D1, D3, D4, D5 y D13 quedan sustituidas por AD-02…AD-09. No se edita ese documento para no mezclar PRs.
- Código de la Fase 1: sin conflictos. El contenido de debug usa placeholders propios, lo que es compatible con AD-02.
