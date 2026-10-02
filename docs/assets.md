# Assets — Thaumcraft 6.1.BETA26 (1.12.2)

> Inventario de `assets/` dentro del JAR de referencia (~1 530 archivos, ~15 MB). Ningún asset se ha copiado al repositorio; este documento sólo los cataloga.

## 1. Resumen por tipo

| Extensión | Cantidad | Uso |
|---|---:|---|
| `.png` | 767 | Texturas |
| `.json` | 511 | Blockstates, modelos, research, loot tables, sounds, shaders post |
| `.ogg` | 111 | Sonidos |
| `.mcmeta` | 81 | Animaciones de texturas (y `pack.mcmeta`) |
| `.obj` / `.mtl` | 27 / 6 | Modelos Wavefront (loader propio) |
| `.lang` | 9 | Traducciones |
| `.jpg` | 7 | Fondos del Thaumonomicon |
| `.fsh` `.vsh` `.frag` `.vert` | 3 / 1 / 3 / 2 | Shaders |
| otros | 3 | `lang/LICENSE`, `lang/README.md`, `research/_example.json.txt` |

## 2. `assets/thaumcraft/`

| Carpeta | Archivos | Tamaño | Notas |
|---|---:|---:|---|
| `blockstates/` | 219 | 908 KB | 170 usan el formato **Forge blockstate v1** (`"forge_marker": 1`), resto formato vanilla 1.12 con variantes por propiedad/metadata |
| `models/block/` | 127 | | Modelos JSON 1.12 (parents vanilla `block/cube_all`, etc.) |
| `models/item/` | 157 | | Modelos de ítem (incluye variantes por metadata) |
| `models/obj/` | 23 (+mtl) | | `golem_*` (base, armor, arms ×5, heads ×5, legs ×4, hauler), `turret`, `crossbow_advanced`, `crystal`, `orb`, `hemis` |
| `textures/blocks/` | 220 | | Carpeta en **plural** (1.12); muchos con `.mcmeta` animado |
| `textures/items/` | 198 (+14 celestial, +33 seals) | | Notas celestiales, sellos de golem |
| `textures/models/` | 100 | | Texturas para TESR/ModelBase/OBJ (bore, centrifuge, hemis1–15 animadas, armaduras...) |
| `textures/entity/` | 23 (+20 armor, +13 golems) | | Mobs, armaduras custom, golems |
| `textures/gui/` | 41 | | GUIs (arcane workbench, research table, thaumatorium, smelter, logistics...), 7 fondos `.jpg` del research browser |
| `textures/research/` | 46 | | Iconos de categorías (`cat_*`), knowledge, iconos de entradas (`r_*`) |
| `textures/aspects/` | 39 | | Íconos de los 37 aspectos (+ auxiliares) |
| `textures/foci/` | 39 | | Íconos de nodos de foco |
| `textures/misc/` | 32 (+37 golem) | | Partículas (`particles.png` atlas), efectos, iconos de tareas de golem |
| `research/` | 9 | 124 KB | 8 JSON de research + ejemplo comentado |
| `lang/` | 9 `.lang` + LICENSE + README | 2.0 MB | `en_us` (1819 líneas), `de_de`, `fr_fr`, `ja_jp`, `ko_kr`, `nl_NL`, `ru_ru`, `zh_cn`, `zh_tw`. Formato `.lang` clave=valor |
| `sounds/` + `sounds.json` | 111 `.ogg`, 65 eventos | 1.5 MB | |
| `shader/` | 5 | 24 KB | `ender.frag/.vert`, `sketch.frag/.vert`, `test.frag` (GLSL legacy, cargados por `ShaderHelper`) |
| `loot_tables/` | 2 | 12 KB | `cultist.json`, `pech.json` (esquema 1.12) |

## 3. `assets/minecraft/shaders/`

Post-procesado que sobrescribe/añade al namespace `minecraft` (esquema de shaders 1.12):

- `post/`: `blurtc.json`, `desaturatetc.json`, `hunger.json`, `sunscorned.json`.
- `program/`: `bloom2`, `bloomcolor`, `color_convolve2` (`.json` + `.fsh`), `bloomvertexbase.vsh`.

## 4. Otros archivos en la raíz del JAR

`TCLogo.png`, `mcmod.info`, `pack.mcmeta` (`pack_format: 1`), `dependencies.info` (Baubles), `changelog.txt`, `META-INF/tc_at.cfg`, `META-INF/fml_cache_*.json`.

## 5. Implicaciones para el port (resumen)

| Asset | Estado esperado en Minecraft moderno |
|---|---|
| `textures/blocks`, `textures/items` | Renombrar a `textures/block`, `textures/item` (cambio desde 1.13) |
| Blockstates Forge v1 y por metadata | Reescribir: formato Forge v1 ya no existe; sin metadata (flattening 1.13) → un bloque por variante o propiedades explícitas |
| Modelos de ítem | Desde 1.21.4 los ítems requieren definiciones en `assets/<ns>/items/` además de `models/item` (a verificar contra 26.3) |
| `.lang` | Convertir a `lang/*.json` (desde 1.13) y renombrar claves (`tile.*`/`item.*` → `block.thaumcraft.*`/`item.thaumcraft.*`) |
| `sounds.json` | Formato compatible en líneas generales; registro de `SoundEvent` cambia |
| `loot_tables` | Mover a `data/thaumcraft/loot_table/` (singular desde 1.21) y actualizar esquema |
| Research JSON | Formato propio; puede mantenerse como datos pero idealmente migrar a `data/` (datapack/reload listener) y actualizar referencias de ítems (`"minecraft:dye;1;15"` usa metadata y NBT) |
| Shaders GLSL legacy / post 1.12 | Reescribir: el pipeline moderno usa core shaders y JSON de post-efectos con otro esquema |
| OBJ | Requiere loader OBJ del loader elegido (NeoForge tiene uno) o conversión a modelos JSON/baked |
| `.mcmeta` de animación | Formato prácticamente compatible |
| `.jpg` | Minecraft moderno espera PNG para texturas; convertir |

## 6. Licencias

- El código y los assets de Thaumcraft son propiedad de **Azanor**; Thaumcraft no se distribuye con licencia abierta. El uso/redistribución de texturas, modelos y sonidos en un port debe tratarse como **riesgo legal** a resolver antes de publicar (permiso del autor o arte propio).
- `lang/LICENSE` es MIT (2017) y aplica a los archivos de traducción del repositorio beta de Thaumcraft (`lang/README.md` remite a `thaumcraft-beta`); verificar alcance exacto antes de reutilizarlos.
- Las librerías embebidas (CodeChickenLib, GLE) tienen licencias propias (ver `dependencies.md`).
