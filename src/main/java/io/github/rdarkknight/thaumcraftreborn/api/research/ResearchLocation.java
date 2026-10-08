package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ResearchLocation(int column, int row) {
	public static final Codec<ResearchLocation> CODEC = Codec.INT.listOf().comapFlatMap(
			values -> values.size() == 2
					? DataResult.success(new ResearchLocation(values.get(0), values.get(1)))
					: DataResult.error(() -> "Research location must contain exactly two integers"),
			location -> List.of(location.column(), location.row())
	);
	public static final StreamCodec<ByteBuf, ResearchLocation> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			ResearchLocation::column,
			ByteBufCodecs.VAR_INT,
			ResearchLocation::row,
			ResearchLocation::new
	);
}
