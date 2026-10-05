package io.github.rdarkknight.thaumcraftreborn.systems.aspect;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;

public record AspectMappingFile(List<AspectMappingFile.Entry> entries) {
	public static final Codec<AspectMappingFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Entry.CODEC.listOf().fieldOf("entries").forGetter(AspectMappingFile::entries)
	).apply(instance, AspectMappingFile::new));

	public AspectMappingFile {
		entries = List.copyOf(entries);
	}

	public record Entry(
			Optional<Identifier> item,
			Optional<Identifier> tag,
			Optional<Identifier> entity,
			Optional<NbtPredicate> nbt,
			AspectList aspects,
			boolean replace
	) {
		private static final Codec<NbtPredicate> SNBT_CODEC = Codec.STRING.comapFlatMap(
				Entry::parseNbtPredicate,
				predicate -> predicate.tag().toString()
		);

		public static final Codec<Entry> CODEC = RecordCodecBuilder.<Entry>create(instance -> instance.group(
				Identifier.CODEC.optionalFieldOf("item").forGetter(Entry::item),
				Identifier.CODEC.optionalFieldOf("tag").forGetter(Entry::tag),
				Identifier.CODEC.optionalFieldOf("entity").forGetter(Entry::entity),
				SNBT_CODEC.optionalFieldOf("nbt").forGetter(Entry::nbt),
				AspectList.CODEC.fieldOf("aspects").forGetter(Entry::aspects),
				Codec.BOOL.optionalFieldOf("replace", false).forGetter(Entry::replace)
		).apply(instance, Entry::new)).validate(entry -> {
			int targetCount = (entry.item().isPresent() ? 1 : 0)
					+ (entry.tag().isPresent() ? 1 : 0)
					+ (entry.entity().isPresent() ? 1 : 0);
			if (targetCount != 1) {
				return DataResult.error(() -> "An aspect mapping entry must define exactly one of item, tag, or entity");
			}
			if (entry.nbt().isPresent() && entry.entity().isEmpty()) {
				return DataResult.error(() -> "NBT predicates are only valid for entity mappings");
			}
			return DataResult.success(entry);
		});

		private static DataResult<NbtPredicate> parseNbtPredicate(String snbt) {
			try {
				return DataResult.success(new NbtPredicate(TagParser.parseCompoundFully(snbt)));
			} catch (CommandSyntaxException exception) {
				return DataResult.error(exception::getMessage);
			}
		}
	}
}
