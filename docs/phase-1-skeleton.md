# Esqueleto técnico — Fase 1

Esta fase prepara la infraestructura para una reconstrucción limpia de Thaumcraft 6. No incluye mecánicas, contenido ni código de gameplay de Thaumcraft.

## Estructura y capas

- `api`: identidad pública estable del mod.
- `core`: componentes de datos, attachments, payloads y la fachada `AccessoryAccess`.
- `systems`: capa reservada para lógica de dominio; todavía no contiene sistemas.
- `content`: únicamente el ítem y bloque de prueba, registrados en `DebugContent`.
- `compat.trinkets`: único adaptador que importa tipos de Trinkets Updated.
- `client`: networking, datagen y renderer; no se importa desde código común.
- `gametest`: pruebas separadas, no incluidas en el JAR principal.

`api` no depende de otras capas propias y expone `MOD_ID` e `id(String)`; `core` depende de `api` para construir identificadores, sin depender del punto de entrada. `content` usa `core`; `compat` implementa las fachadas y no filtra tipos externos a ellas. El código común no depende de `client`.

## Versiones

| Componente | Versión |
|---|---|
| Minecraft | 26.3 |
| Java | 25 |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.161.0+26.3 |
| Fabric Loom | 1.17.21 |
| Gradle Wrapper | 9.6.0 |
| Trinkets Updated | 4.2.1+26.3 |
| Yumi Foundation (transitiva) | 1.1.3+26.2 |

## Compilar, ejecutar, generar y probar

Con Java 25 seleccionado:

```sh
./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew runDatagen
./gradlew runGametest
./gradlew runClientGametest
```

Datagen escribe en `src/main/generated`; Loom registra esa salida como recursos principales. Los recursos específicos de Trinkets usados por las pruebas están en `src/gametest/resources`, no en el JAR de producción.

## Pruebas técnicas

- `ProbeStamp` demuestra un componente persistente y sincronizado, con codec de datos y codec de red; la prueba también lo serializa dentro de un `ItemStack`.
- `PROBE_INTERACTIONS` demuestra persistencia, copia al morir y sincronización sólo al jugador propietario.
- `probe_ping` y `probe_pong` demuestran el registro direccional de payloads y una respuesta calculada en el servidor.
- `AccessoryAccess` prueba la fachada desacoplada; el adaptador de Trinkets Updated cubre inventarios equipados y ausentes.
- `test_probe`, `test_render_block` y su block entity son contenido de depuración sin gameplay. El BER presenta el `test_probe` flotante usando la ruta de estado de ítem y envío de nodos de render moderno.
- Datagen genera modelos, traducciones `en_us`/`es_es` y loot para el bloque. Las texturas e icono son placeholders geométricos originales.
- Las pruebas de servidor verifican ids, codecs, serialización de attachments y la fachada de accesorios. La prueba cliente cubre el ping/pong y captura el bloque renderizado.

## Desviaciones / pendientes

- `All-Rights-Reserved` es un placeholder de licencia; la decisión legal sigue pendiente.
- Loom 1.17.21 y Gradle 9.6.0 se mantuvieron; no fue necesario el fallback a Loom 1.18.2 / Gradle 9.7.1.
- En Fabric 26.3 los payloads se registran con `serverboundPlay()` / `clientboundPlay()`; `Item.use` devuelve `InteractionResult` y el mensaje al jugador usa `sendOverlayMessage`. El block entity usa el constructor directo de `BlockEntityType` y el evento `CreativeModeTabEvents`.
- El renderer usa `ItemStackRenderState`, `CameraRenderState` y `SubmitNodeCollector`; la rotación se aplica con una matriz JOML aceptada por `PoseStack`. Para datagen se usa `DataProvider` con JSON generado directamente, ya que no están disponibles los antiguos métodos públicos de plantillas de modelos.
- En gametests se crea el jugador simulado con `GameType`; la persistencia de entidades usa `ValueInput` / `ValueOutput`, el tipo de entidad se consulta en el registro y `EntityAnchorArgument` está en `net.minecraft.commands.arguments`. La espera de chunks del test cliente se hace mediante `TestServerConnection`.
- `TrinketAttachment.getInventory()` sigue siendo la API de acceso a ranuras usada por la prueba, aunque Trinkets 4.2.1 la marca obsoleta para eliminación.
- Loom ya incorpora `src/main/generated` a los recursos principales al configurar datagen; añadirlo también manualmente duplicaba las entradas durante `processResources`, por lo que se eliminó el registro redundante.
