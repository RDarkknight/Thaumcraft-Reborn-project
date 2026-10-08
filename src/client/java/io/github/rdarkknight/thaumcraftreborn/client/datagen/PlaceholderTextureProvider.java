package io.github.rdarkknight.thaumcraftreborn.client.datagen;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

final class PlaceholderTextureProvider implements DataProvider {
	private final PackOutput.PathProvider textures;

	PlaceholderTextureProvider(FabricPackOutput output) {
		this.textures = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		List<CompletableFuture<Void>> writes = new ArrayList<>();
		for (Block block : ThaumcraftContent.BLOCKS) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			writes.add(write(textures.file(Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()), "png"), id.toString()));
		}
		for (Item item : ThaumcraftContent.ITEMS) {
			Identifier id = BuiltInRegistries.ITEM.getKey(item);
			writes.add(write(textures.file(Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()), "png"), id.toString()));
		}
		return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
	}

	private CompletableFuture<Void> write(java.nio.file.Path path, String seed) {
		return CompletableFuture.runAsync(() -> {
			try {
				Files.createDirectories(path.getParent());
				Files.write(path, png(seed));
			} catch (IOException exception) {
				throw new IllegalStateException("Could not generate placeholder texture " + path, exception);
			}
		});
	}

	private static byte[] png(String seed) throws IOException {
		int hash = seed.hashCode();
		int red = 80 + (hash >>> 16 & 0x7f);
		int green = 80 + (hash >>> 8 & 0x7f);
		int blue = 80 + (hash & 0x7f);
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				boolean accent = ((x + y + (hash & 7)) % 5 == 0) || x == 0 || y == 0 || x == 15 || y == 15;
				int shade = accent ? 48 : 0;
				image.setRGB(x, y, 0xff000000 | Math.max(0, red - shade) << 16
						| Math.max(0, green - shade) << 8 | Math.max(0, blue - shade));
			}
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(image, "png", bytes);
		return bytes.toByteArray();
	}

	@Override
	public String getName() {
		return "Thaumcraft Reborn Placeholder Textures";
	}
}
