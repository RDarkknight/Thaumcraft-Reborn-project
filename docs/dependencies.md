# Dependencias — Thaumcraft 6.1.BETA26 (1.12.2)

## 1. Dependencias externas de runtime

| Dependencia | Tipo | Versión | Uso en Thaumcraft |
|---|---|---|---|
| Minecraft | Plataforma | 1.12.2 | Base |
| Minecraft Forge | Mod loader | ≥ 14.23.5.2768 | Registro, eventos, capabilities, red, config, modelos, fluidos, ore dictionary |
| **Baubles** (Azanor) | Mod requerido | ≥ 1.5.2 (`dependencies.info`: `Baubles-1.12.2-1.5.2.jar`) | Slots de accesorios: amuletos, anillos, cinturones, cabeza (goggles), charms. Usado en 16 archivos (34 imports de `baubles.api.*`) |
| Botania API (solo una interfaz) | Soft-compat embebida | — | `vazkii.botania.api.item.IPetalApothecary` implementada por `TileWaterJug` (el Everfull Urn puede llenar el Petal Apothecary) |
| CodeChickenLib (re-empaquetado) | Librería embebida | desconocida | `thaumcraft.codechicken.lib.*` (42 clases): `Vector3`, `Matrix4`, `Cuboid6`, `CCModel`, `CCRenderState`, `RayTracer`, iluminación. Copia con paquete renombrado; licencia original de CCL: LGPL 2.1 (verificar versión exacta antes de reutilizar) |
| GLE (sasmaster `glelwjgl`) | Librería embebida | "GLE 095" (CVS 1998) | `com.sasmaster.glelwjgl.java.CoreGLE`: extrusión de tubos con OpenGL/GLU. Usado por `FXBolt`, `FXVoidStream`, `FXEssentiaStream`, `FXBoreStream`, `RenderFluxRift`, `RenderRiftBlast`, `RenderGrapple`, `GuiVoidSiphon` |
| `net.tofweb.starlite` | Directorio vacío | — | Restos de una librería de pathfinding (D* Lite) sin clases. Sin uso |

No hay dependencias a JEI, CraftTweaker, The One Probe, etc. en el código (0 referencias a `mezz.jei`). No se usa `Loader.isModLoaded` ni `@Optional`.

## 2. Librerías transitivas (provistas por MC/Forge 1.12.2)

| Librería | Imports | Uso |
|---|---:|---|
| LWJGL 2 (`org.lwjgl`) | 139 | `GL11`/`GL20`/`ARBShaderObjects`, `Keyboard`, `Mouse` |
| Netty (`io.netty`) | 55 | `ByteBuf` en los `IMessage` |
| Guava / Gson (`com.google`) | 49 | Colecciones; parseo JSON de research |
| Log4j (`org.apache`) | 10 | Logger |
| AWT (`java.awt`) | 35 | `Color` para cálculo de colores de aspectos/FX |

## 3. APIs de Minecraft/Forge 1.12.2 utilizadas (por área)

| Área | APIs 1.12.2 | Intensidad |
|---|---|---|
| Ciclo de vida | `@Mod`, `@SidedProxy`, `FMLPreInit/Init/PostInit`, `FMLServerStartingEvent`, `IMCEvent` | Central |
| Registro | `RegistryEvent.Register<T>`, `IForgeRegistry`, `GameRegistry.registerTileEntity`, `EntityRegistry.registerModEntity` (IDs numéricos), `EntityRegistry.addSpawn`, `BiomeManager`, `BiomeDictionary` | Central |
| Bloques | `Block`, `IBlockState`, `BlockStateContainer`, `Property*` (15 clases), `getMetaFromState` (47 clases), `AxisAlignedBB`, `IBlockAccess` | Muy alta |
| Ítems | `Item` con `setHasSubtypes`/metadata (12 clases), `NBTTagCompound` en stacks (12+ clases), `ItemArmor`, `ItemTool` | Muy alta |
| TileEntities | `TileEntity`, `ITickable` (27), `IInventory`/`ISidedInventory` (19), `IItemHandler` (12), `IFluidHandler` (3), `TileEntitySpecialRenderer` (22) | Muy alta |
| Entidades | `EntityMob`, `EntityAI*`, `DataParameter`/`EntityDataManager`, `PathNavigate`, `NodeProcessor` propios (golems) | Alta |
| GUIs | `IGuiHandler` + IDs enteros, `GuiContainer`, `GuiScreen`, `Container`, `Slot` | Alta |
| Red | `SimpleNetworkWrapper`, `IMessage`/`IMessageHandler`, `ByteBuf` | Alta (41 mensajes) |
| Capabilities | `@CapabilityInject`, `AttachCapabilitiesEvent<Entity>`, `INBTSerializable` | Media (2 capabilities de jugador) |
| Worldgen | `IWorldGenerator`, `WorldGenerator`/`WorldGenAbstractTree`, `Biome`/`BiomeProperties`, `DimensionManager`, IDs de dimensión enteros | Alta |
| Persistencia chunk | `ChunkDataEvent.Load/Save`, `ChunkWatchEvent` (datos de aura en NBT del chunk) | Media |
| Render | `Tessellator`/`BufferBuilder` (55 clases), `GlStateManager`/`GL11` (143 clases), `ModelBase` (20), `Render<T>` (30), `Particle` (24), `IBakedModel`, `ModelLoader`, `ShaderGroup` | Muy alta |
| Recetas | `IRecipe` + `IForgeRegistryEntry.Impl`, `ShapedOreRecipe`, `OreDictionary` (58 registros) | Alta |
| Pociones | `Potion` (9 propias), `PotionHelper` vía AT/reflexión | Media |
| Config | `@Config` anotado + `ConfigManager.sync` | Baja |
| Comandos | `CommandBase` | Baja |
| Fluidos | `FluidRegistry`, `BlockFluidClassic` (3 fluidos: flux goo, liquid death, purifying fluid) | Media |
| Input | `KeyBinding` + `ClientRegistry` (F: cambiar foco, G: toggle misc) | Baja |

## 4. Access Transformer (`tc_at.cfg`) y reflexión

El AT hace públicos 24 miembros vanilla (nombres SRG), entre ellos:
`SaveHandler.playersDirectory`, `EntityLivingBase.jumpTicks/recentlyHit`, `TileEntityFurnace.cookTime`, `SoundEvent.registerSound`, `NetHandlerPlayServer.floatingTickCount`, `Minecraft.modelManager`, `GuiContainer.xSize/ySize`, `Gui.zLevel`, `ItemRenderer.itemStackMainHand/OffHand`, `ParticleManager.PARTICLE_TEXTURES`, `RenderItem.registerItems`, `PotionHelper.*` (conversiones de pociones), `ItemPredicate.item`, `InventoryCrafting.eventHandler` (quita `final`).

Reflexión adicional: `ObfuscationReflectionHelper` en `ThaumcraftCraftingManager` (campos de `PotionHelper.MixPredicate`), y uso de reflexión/`DimensionManager` en `AuraThread`, `Utils`, `BlockUtils`, `ItemHandMirror`, `TileMirror*`, `EntityCultistPortalGreater`, `FocusPackage`.

Cada entrada del AT y cada acceso reflexivo es un punto de acoplamiento a internals de 1.12.2 que debe re-evaluarse en el port (ver `porting-risks.md`).

## 5. Dependencias del API público de Thaumcraft hacia terceros

El paquete `thaumcraft.api` fue diseñado para que otros mods (addons) lo compilen. Expone tipos de MC 1.12.2 en sus firmas (`ItemStack`, `EntityPlayer`, `World`, `BlockPos`, `IBlockState`, `EnumFacing`, `NBTTagCompound`), por lo que **no es reutilizable tal cual**: cualquier API nueva deberá redefinirse sobre los tipos modernos.

## 6. Proyectos comunitarios

Existen ports/reimplementaciones comunitarias de Thaumcraft para versiones modernas. Según la consigna del proyecto, **no se copiará su código**; sólo podrán consultarse como referencia conceptual. Los assets y el código de Thaumcraft original pertenecen a Azanor (ver `assets.md` §Licencias).
