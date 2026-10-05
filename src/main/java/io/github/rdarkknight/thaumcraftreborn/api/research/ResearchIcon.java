package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public sealed interface ResearchIcon permits ResearchIcon.Texture, ResearchIcon.Item {
	Codec<ResearchIcon> CODEC = Codec.either(
			Identifier.CODEC.fieldOf("texture").codec(),
			ItemStack.CODEC.fieldOf("item").codec()
	).xmap(
			value -> value.map(Texture::new, Item::new),
			icon -> icon instanceof Texture texture
					? Either.left(texture.texture())
					: Either.right(((Item) icon).item())
	);
	StreamCodec<RegistryFriendlyByteBuf, ResearchIcon> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

	record Texture(Identifier texture) implements ResearchIcon {
	}

	record Item(ItemStack item) implements ResearchIcon {
		public Item {
			item = item.copy();
		}
	}
}
