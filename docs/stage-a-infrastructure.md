# Etapa A — Infraestructura

Etapa A completa la infraestructura genérica que faltaba en la Fase 1. El contenido añadido bajo `content/debug` sólo demuestra las APIs: no implementa aspectos, aura, investigación, biomas ni mecánicas de Thaumcraft.

## Piezas y uso

- **Registro propio:** `ThaumcraftRegistryKeys.FX_TYPE` identifica el registro de tipos FX. `ThaumcraftRegistries.createSynced(key)` crea registros Fabric con atributo `SYNCED`; `DebugFx.DEBUG_BURST` es su primer consumidor.
- **Datos recargables:** `SyncedDataLoader<T>` lee `data/<namespace>/thaumcraft_reborn/<id.path>/**/*.json`. Los snapshots de servidor y cliente son inmutables y separados para que un mundo integrado no comparta estado accidentalmente. Los recursos solapados se registran con sus packs y prioridad; una entrada inválida se informa y se omite sin descartar las demás. Los payloads `data_sync/<path>` reemplazan el snapshot cliente completo al entrar y tras `/reload`.
- **Configuración:** los codecs de `CommonConfig` y `ClientConfig` suministran defaults para todos los campos. `ConfigFiles` escribe JSON legible cuando falta un archivo; ante JSON inválido registra el error y usa defaults sin modificar ese archivo. El servidor sincroniza `CommonConfig`; el cliente elimina la copia al desconectarse.
- **BlockEntities:** `ThaumcraftBlockEntity` unifica el paquete/tag de actualización y `markDirtyAndSync()`. `ThaumcraftContainerBlockEntity` persiste su inventario con `ValueInput`/`ValueOutput` y suelta el contenido desde `BlockEntity.preRemoveSideEffects` al retirar el bloque. `DebugContainerBlockEntity` prueba 9 slots, un contador persistente y sincronización cada 20 ticks.
- **Menús:** `ThaumcraftMenu` aporta slots del jugador, quick-move entre inventario de máquina y jugador y validación por distancia. `DebugContainerMenu` usa datos extendidos de `BlockPos`; `DebugContainerScreen` presenta los ticks del BE.
- **FX:** el servidor emite `FxPayload` a quienes siguen el chunk de origen. Los handlers se ejecutan en el hilo del cliente; los tipos sin handler se ignoran y se registran una única vez. `debug_burst` limita la cantidad del lado cliente a 1–32.
- **Attachment y comandos:** `DEBUG_CHUNK_MARKER` guarda un `Integer` persistente en `LevelChunk` sin sincronizarlo. Los comandos de diagnóstico requieren permiso 2 y están bajo `/thaumcraft_reborn debug`.
- **Datagen y placeholders:** el debug container obtiene blockstate, modelo, loot y traducciones desde datagen. Las texturas `debug_container.png` y `debug_spark.png` son placeholders propios de 16×16; no se incorporan assets de TC6.

Ejemplos mínimos:

```java
DebugData.ENTRIES.serverEntries().get(ThaumcraftRebornApi.id("sample"));
ThaumcraftConfig.common(false).debug().verboseDataLogging();
FxDispatcher.send(serverLevel, origin, DebugFx.DEBUG_BURST, 8);
```

## Verificación

La pasada de verificación de la etapa se ejecuta con Java 25:

```sh
./gradlew build
./gradlew runDatagen
git status --porcelain src/main/generated
xvfb-run -a ./gradlew runClientGametest
git diff --check
```

`StageAGameTests` cubre registro y atributo `SYNCED`, roundtrip FX, recarga de datos, codec/config, persistencia del BE, drops al retirar el contenedor, quick-move y attachment. `StageAClientGameTest` comprueba los snapshots y config sincronizados, la pantalla del contenedor y el burst de partículas; captura una pantalla de prueba.

## Desviaciones de API

- Para drops, 26.3 usa `BlockEntity.preRemoveSideEffects(BlockPos, BlockState)` y `Containers.dropContents(Level, BlockPos, Container)`; no se usa el antiguo hook `Block#onRemove`.
- La serialización del tag de actualización sigue el patrón de `BlockEntity` de 26.3 basado en `saveWithoutMetadata(provider)`; el paquete se genera con `ClientboundBlockEntityDataPacket.create(this)`.
- Fabric 26.3 expone `ExtendedMenuType` y `ExtendedMenuProvider` en `net.fabricmc.fabric.api.menu.v1`; `MenuScreens.register` queda accesible mediante el class tweaker de Fabric. Los receivers de datos/configuración se reencolan con `client.execute`.
- `GuiGraphicsExtractor.blit` recibe coordenadas UV normalizadas; el screen recorta el área de 176×166 de la textura vanilla de 256×256.
- El ticker de `debug_container` sólo avanza en el servidor; el cliente ve el contador mediante el paquete de actualización del bloque y los datos del menú.
- El gametest del attachment comprueba `setAttached`/`getAttachedOrCreate` y `AttachmentType.isPersistent()`. No fuerza un save/reload de chunk: la API de `AttachmentTarget` no expone un serializador de chunk y la ruta de guardado de `ChunkMap` es privada en 26.3.

No se añaden dependencias Gradle. Se mantienen las reglas de capas y la política de renderizado de §7.3: sin GL directo, shaders propios ni pipelines de render personalizados para estos efectos.
