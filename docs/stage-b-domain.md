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

Los archivos se fusionan en orden lexicográfico de id. Las asignaciones repetidas se suman; `replace: true` sobrescribe lo acumulado. Para ítems, una asignación exacta prevalece sobre tags; entre varios tags coincidentes se usa el id lexicográficamente menor. Las entidades prefieren el primer predicado NBT coincidente y usan después la regla sin predicado como fallback. No se incluyen mappings vanilla en esta entrega ni se derivan aspectos desde recetas. Tampoco se aplican bonificaciones por encantamientos, pociones o contenedores; estas últimas quedan para C.

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

Los tipos de conocimiento se ordenan `THEORY` y `OBSERVATION`; una unidad de progresión equivale respectivamente a 32 y 16 unidades raw. El API conserva los valores raw y devuelve el entero inferior al consultar puntos completos. Research desconocido devuelve `UNKNOWN`; una referencia `clave@N` exige alcanzar ese stage. Los límites y casos de research no cargado siguen la semántica de TC6 implementada por `PlayerKnowledge`.

## Datos de investigación

Los archivos se cargan desde `data/<namespace>/thaumcraft_reborn/research_categories/<ruta>.json` y `data/<namespace>/thaumcraft_reborn/research/<ruta>.json`, con `SyncedDataLoader` y codecs JSON/StreamCodec. El loader de research decodifica con el `RegistryAccess` activo del servidor (también tras cada reload) para que el codec `ItemStack` resuelva ítems y componentes en 26.3; los demás loaders usan los registros vanilla.

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

Una entrada se identifica por el id del archivo y requiere `name`, `category` y al menos un `stage`. Los demás campos son `icons`, `parents`, `siblings`, `location: [columna, fila]`, `meta`, `reward_item`, `reward_knowledge` y `addenda`. Un icono es `{"texture": "namespace:id"}` o `{"item": {"id": "minecraft:stone"}}`. Las etapas admiten `text`, `recipes`, `required_item`, `required_craft`, `required_knowledge`, `required_research` y `warp`; un requisito de research contiene `reference` y `icon` opcional. Un addendum contiene `text`, `recipes` y `required_research`. Las referencias aceptan `namespace:clave`, `namespace:clave@N` y el prefijo `~` para línea oculta.

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

`ResearchAccess` da acceso a categorías/entradas y a la progresión. `ResearchProgression` porta los stages y requisitos externos, completion, rewards, flags, warp y siblings recursivos. Warp mayor que uno se divide entre `PERMANENT` (cantidad menos la mitad entera) y `NORMAL` (mitad entera), como en TC6; `misc.wuss_mode` es `false` por defecto y desactiva esa concesión. Los rewards de conocimiento multiplican la cantidad declarada por la progresión de `KnowledgeType`; el warp de una categoría no sustituye a las comprobaciones de stages. Las entradas `AUTOUNLOCK` se otorgan al conectar el jugador y después de un reload exitoso.

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
