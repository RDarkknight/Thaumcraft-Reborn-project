package io.github.rdarkknight.thaumcraftreborn.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectBlend;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectCombinations;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.PlayerKnowledge;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.PlayerWarp;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchCategory;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchEntry;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchFlag;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchIcon;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchMeta;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchReference;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStatus;
import io.github.rdarkknight.thaumcraftreborn.api.research.WarpType;
import io.github.rdarkknight.thaumcraftreborn.api.world.BiomePlacementAccess;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.config.CommonConfig;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerKnowledgeData;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerKnowledgeData.KnowledgeKey;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerKnowledgeData.ResearchState;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerWarpData;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.AuraSystems;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.BiomeAuraType;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.BiomeAuraTypes;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.MinimalAuraSimulation;
import io.github.rdarkknight.thaumcraftreborn.systems.aspect.AspectMappingFile;
import io.github.rdarkknight.thaumcraftreborn.systems.aspect.AspectMappings;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchIndex;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchProgression;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.LevelChunk;

public final class StageBGameTests implements CustomTestMethodInvoker {
	@GameTest
	public void aspectRegistryContainsTheOrderedTc6Definitions(GameTestHelper context) {
		List<ExpectedAspect> expected = expectedAspects();
		List<Aspect> registered = ThaumcraftRegistries.ASPECT.stream().toList();
		context.assertTrue(registered.size() == 37, "the aspect registry contains 37 entries");
		context.assertTrue(
				registered.stream().filter(Aspect::isPrimal).count() == 6,
				"the aspect registry contains six primals"
		);
		context.assertTrue(
				registered.stream().filter(aspect -> !aspect.isPrimal()).count() == 31,
				"the aspect registry contains 31 compounds"
		);
		for (int index = 0; index < expected.size(); index++) {
			ExpectedAspect definition = expected.get(index);
			Aspect actual = registered.get(index);
			context.assertTrue(actual.tag().equals(definition.tag()), "aspect registry order at " + index);
			context.assertTrue(actual.color() == definition.color(), "aspect color for " + definition.tag());
			context.assertTrue(actual.blend() == definition.blend(), "aspect blend for " + definition.tag());
			context.assertTrue(
					actual.components().stream().map(Aspect::tag).toList().equals(definition.components()),
					"aspect components for " + definition.tag()
			);
			context.assertTrue(actual.chatFormatting().equals(definition.chatFormatting()), "aspect chat color for " + definition.tag());
			context.assertTrue(
					actual.translationKey().equals("aspect.thaumcraft_reborn." + definition.tag()),
					"aspect translation key for " + definition.tag()
			);
		}
		context.succeed();
	}

	@GameTest
	public void aspectCombinationIsOrderInsensitive(GameTestHelper context) {
		Optional<Aspect> forward = AspectCombinations.combine(ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("aer")),
				ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("terra")));
		Optional<Aspect> reverse = AspectCombinations.combine(ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("terra")),
				ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("aer")));
		context.assertTrue(forward.isPresent() && forward.get().tag().equals("vitreus"), "air and earth combine to crystal");
		context.assertTrue(reverse.equals(forward), "combination ignores primal order");
		context.assertTrue(
				AspectCombinations.combine(
						ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("terra")),
						ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("ignis"))
				).isEmpty(),
				"unlisted combinations return empty"
		);
		context.succeed();
	}

	@GameTest
	public void aspectListOperationsPreserveOrderAndInvariants(GameTestHelper context) {
		Aspect aer = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("aer"));
		Aspect terra = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("terra"));
		Aspect fire = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("ignis"));
		Aspect order = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("ordo"));
		AspectList base = AspectList.builder().add(order, 2).add(aer, 2).add(fire, 1).build();
		AspectList added = base.add(aer, 3).add(fire, -1);
		AspectList merged = base.merge(aer, 1).merge(fire, 4);
		AspectList removed = base.remove(order, 2).remove(fire, 1);
		Optional<AspectList> reduced = base.reduce(order, 2);
		AspectList equalContentInOtherOrder = AspectList.builder().add(fire, 1).add(aer, 2).add(order, 2).build();

		context.assertTrue(added.amount(aer) == 5 && added.amount(fire) == 0, "add sums and drops non-positive entries");
		context.assertTrue(merged.amount(aer) == 2 && merged.amount(fire) == 4, "merge keeps the maximum amount");
		context.assertTrue(removed.aspects().equals(List.of(aer)), "remove drops entries at zero");
		context.assertTrue(reduced.isPresent() && reduced.get().amount(order) == 0, "reduce removes an exhausted aspect");
		context.assertTrue(base.reduce(order, 3).isEmpty(), "reduce fails when the amount is insufficient");
		context.assertTrue(base.visSize() == 5 && base.size() == 3, "visSize and size count amount and entries");
		context.assertTrue(base.sortedByName().equals(List.of(aer, fire, order)), "name sorting uses aspect tags");
		context.assertTrue(base.sortedByAmount().equals(List.of(order, aer, fire)), "amount sorting is descending and stable");
		context.assertTrue(base.equals(equalContentInOtherOrder) && base.hashCode() == equalContentInOtherOrder.hashCode(),
				"equality and hash code ignore insertion order");
		context.assertTrue(AspectList.of(aer, 0).isEmpty(), "zero-valued construction is empty");
		context.assertTrue(base.add(aer, -2).amount(aer) == 0, "addition maintains the positive-entry invariant");
		context.assertTrue(AspectList.of(terra, 5).add(aer, 2).toString()
						.equals("{thaumcraft_reborn:terra=5, thaumcraft_reborn:aer=2}"),
				"AspectList toString uses registry ids and preserves insertion order");
		context.succeed();
	}

	@GameTest
	public void aspectListCodecsRoundTripAndRejectInvalidValues(GameTestHelper context) {
		Aspect air = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("aer"));
		Aspect earth = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("terra"));
		AspectList original = AspectList.builder().add(earth, 5).add(air, 3).build();
		JsonElement encodedJson = AspectList.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		AspectList decodedJson = AspectList.CODEC.parse(JsonOps.INSTANCE, encodedJson).getOrThrow();
		List<String> jsonOrder = new ArrayList<>(encodedJson.getAsJsonObject().keySet());
		context.assertTrue(decodedJson.equals(original), "AspectList JSON codec round trip");
		context.assertTrue(jsonOrder.equals(List.of("thaumcraft_reborn:terra", "thaumcraft_reborn:aer")), "JSON preserves insertion order");
		context.assertTrue(decodedJson.aspects().equals(original.aspects()), "JSON decode preserves entry order");
		context.assertTrue(
				AspectList.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"thaumcraft_reborn:missing\":1}")).result().isEmpty(),
				"unknown aspect identifiers are rejected"
		);
		context.assertTrue(
				AspectList.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"thaumcraft_reborn:aer\":0}")).result().isEmpty(),
				"zero aspect amounts are rejected"
		);
		context.assertTrue(
				AspectList.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"thaumcraft_reborn:aer\":-1}")).result().isEmpty(),
				"negative aspect amounts are rejected"
		);

		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), context.getLevel().registryAccess());
		AspectList.STREAM_CODEC.encode(buffer, original);
		AspectList decodedStream = AspectList.STREAM_CODEC.decode(buffer);
		context.assertTrue(decodedStream.equals(original), "AspectList stream codec round trip");
		context.assertTrue(decodedStream.aspects().equals(original.aspects()), "stream codec preserves insertion order");
		buffer.release();
		context.assertTrue(rejectsStream(context, 0, 0), "stream codec rejects zero amounts");
		context.assertTrue(rejectsStream(context, 0, -1), "stream codec rejects negative amounts");
		context.assertTrue(
				rejectsStream(context, ThaumcraftRegistries.ASPECT.size(), 1),
				"stream codec rejects unknown aspect ids"
		);
		context.succeed();
	}

	@GameTest
	public void aspectsItemComponentCodecRoundTrips(GameTestHelper context) {
		Aspect air = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("aer"));
		AspectList aspects = AspectList.of(air, 7);
		ItemStack original = new ItemStack(Items.DIAMOND);
		original.set(ModDataComponents.ASPECTS, aspects);
		var ops = RegistryOps.create(JsonOps.INSTANCE, context.getLevel().registryAccess());
		JsonElement json = ItemStack.CODEC.encodeStart(ops, original).getOrThrow();
		ItemStack restored = ItemStack.CODEC.parse(ops, json).getOrThrow();
		context.assertTrue(restored.get(ModDataComponents.ASPECTS).equals(aspects), "item aspect component JSON round trip");
		context.succeed();
	}

	@GameTest
	public void aspectMappingsResolveItemsTagsEntitiesAndMerges(GameTestHelper context) {
		Aspect air = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("aer"));
		Aspect earth = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("terra"));
		Aspect fire = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("ignis"));
		AspectList exactItem = AspectLookup.get().getAspects(context.getLevel(), vanillaItem("barrier"));
		AspectList taggedItem = AspectLookup.get().getAspects(context.getLevel(), vanillaItem("structure_void"));
		AspectList summedItem = AspectLookup.get().getAspects(context.getLevel(), vanillaItem("command_block"));
		AspectList replacedItem = AspectLookup.get().getAspects(context.getLevel(), new ItemStack(Items.IRON_INGOT));
		AspectList replaceBeforeAdditive = AspectLookup.get().getAspects(context.getLevel(), new ItemStack(Items.DIAMOND_SWORD));
		AspectList unmappedItem = AspectLookup.get().getAspects(context.getLevel(), vanillaItem("structure_block"));
		var armorStandType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", "armor_stand"));
		Entity untagged = armorStandType.create(context.getLevel(), EntitySpawnReason.COMMAND);
		Entity tagged = armorStandType.create(context.getLevel(), EntitySpawnReason.COMMAND);
		context.assertTrue(untagged != null && tagged != null, "armor stands are created from the entity registry");
		context.assertTrue(tagged.addTag("stage_b_powered"), "NBT fixture tag is applied to the matching entity");

		context.assertTrue(exactItem.amount(earth) == 2 && exactItem.size() == 1, "exact item mapping wins over matching tags");
		context.assertTrue(taggedItem.amount(air) == 3 && taggedItem.size() == 1, "lexicographically first matching tag is used");
		context.assertTrue(summedItem.amount(earth) == 5 && summedItem.amount(air) == 1, "same targets sum across files");
		context.assertTrue(replacedItem.amount(fire) == 5 && replacedItem.size() == 1, "replace mapping overwrites earlier targets");
		context.assertTrue(replaceBeforeAdditive.amount(earth) == 7 && replaceBeforeAdditive.size() == 1,
				"replacement wins over additive entries even when its file sorts first");
		context.assertTrue(
				AspectMappings.LOADER.serverEntries().keySet().stream().anyMatch(id -> id.getPath().endsWith("00_base"))
						&& AspectMappings.LOADER.serverEntries().keySet().stream().noneMatch(id -> id.getPath().endsWith("30_invalid")),
				"invalid mapping fixture is skipped while valid mapping fixtures load"
		);
		context.assertTrue(unmappedItem.isEmpty(), "unmapped items return EMPTY");
		context.assertTrue(
				AspectLookup.get().getAspects(untagged).amount(earth) == 1,
				"entity mapping without NBT is the fallback"
		);
		context.assertTrue(
				AspectLookup.get().getAspects(tagged).amount(air) == 4,
				"matching entity NBT mapping wins over the fallback"
		);
		context.succeed();
	}

	@GameTest
	public void tc6VanillaAspectMappingsPreserveFlattenedValues(GameTestHelper context) {
		AspectMappingFile vanillaItems = AspectMappings.LOADER.serverEntries()
				.get(ThaumcraftRebornApi.id("tc6_vanilla_items"));
		Identifier lapisOre = Identifier.fromNamespaceAndPath("minecraft", "lapis_ore");
		Identifier lapisOreTag = Identifier.fromNamespaceAndPath("c", "ores/lapis");
		AspectList lapisAspects = aspectFormula("terra", 5, "sensus", 15);

		context.assertTrue(AspectMappings.unknownServerRegistryTargetCount() == 0,
				"shipped aspect mappings contain no unknown item or entity ids");
		context.assertTrue(vanillaItems != null, "the vanilla item mapping file is loaded");
		context.assertTrue(vanillaItems.entries().stream()
						.anyMatch(entry -> entry.tag().filter(lapisOreTag::equals).isPresent())
						&& vanillaItems.entries().stream().anyMatch(entry ->
								entry.item().filter(lapisOre::equals).isPresent() && entry.aspects().equals(lapisAspects)),
				"the ore tag and its vanilla item value are both shipped");
		context.assertTrue(
				AspectLookup.get().getAspects(context.getLevel(), vanillaItem("granite"))
						.equals(aspectFormula("terra", 5)),
				"legacy stone metadata maps granite to its flattened item id"
		);
		context.assertTrue(
				AspectLookup.get().getAspects(context.getLevel(), vanillaItem("charcoal"))
						.equals(aspectFormula("potentia", 10, "ignis", 10)),
				"wildcard coal metadata expands to charcoal"
		);
		context.assertTrue(
				AspectLookup.get().getAspects(context.getLevel(), vanillaItem("lapis_ore")).equals(lapisAspects),
				"the emitted ore-dictionary tag value is preserved on its vanilla item"
		);
		context.assertTrue(
				AspectLookup.get().getAspects(context.getLevel(), vanillaItem("brick"))
						.equals(aspectFormula("aqua", 5, "terra", 5, "ignis", 1)),
				"brick copies clay-ball aspects before adding fire"
		);
		context.assertTrue(
				AspectLookup.get().getAspects(context.getLevel(), vanillaItem("dead_bush"))
						.equals(aspectFormula("herba", 5, "perditio", 1)),
				"the later dead-bush registration overwrites its earlier flattened wildcard value"
		);

		Creeper powered = (Creeper) BuiltInRegistries.ENTITY_TYPE
				.getValue(Identifier.fromNamespaceAndPath("minecraft", "creeper"))
				.create(context.getLevel(), EntitySpawnReason.COMMAND);
		LightningBolt lightning = (LightningBolt) BuiltInRegistries.ENTITY_TYPE
				.getValue(Identifier.fromNamespaceAndPath("minecraft", "lightning_bolt"))
				.create(context.getLevel(), EntitySpawnReason.COMMAND);
		context.assertTrue(powered != null && lightning != null, "the vanilla entity types can be created");
		powered.thunderHit(context.getLevel(), lightning);
		context.assertTrue(
				powered.isPowered()
						&& AspectLookup.get().getAspects(powered)
								.equals(aspectFormula("herba", 15, "ignis", 15, "potentia", 15)),
				"the powered-creeper NBT rule matches its exact TC6 aspects"
		);
		context.succeed();
	}

	@GameTest
	public void auraGenerationAccessAndMinimalMoonRegeneration(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		LevelChunk chunk = level.getChunkAt(BlockPos.ZERO);
		context.assertTrue(chunk.getAttached(ModAttachments.AURA) != null, "loaded chunks receive aura data");
		AuraChunkData generated = AuraSystems.generateData(level, chunk);
		context.assertTrue(generated.base() >= 0 && generated.base() <= 500, "generated base is clamped");
		context.assertTrue(generated.vis() == generated.base() && generated.flux() == 0.0F, "generated aura starts at base vis");
		context.assertTrue(ModAttachments.AURA.isPersistent(), "aura attachment is persistent without an initializer");
		context.assertTrue(ModAttachments.AURA.initializer() == null, "aura attachment does not create data before generation");
		context.assertTrue(chunk.hasAttached(ModAttachments.AURA), "generated chunk has the aura attachment");

		HolderLookup.RegistryLookup<net.minecraft.world.level.biome.Biome> biomes =
				level.registryAccess().lookupOrThrow(Registries.BIOME);
		Holder<net.minecraft.world.level.biome.Biome> plains = biomes.getOrThrow(Biomes.PLAINS);
		List<BiomeAuraType> matchingTypes = BiomeAuraTypes.LOADER.serverEntries().values().stream()
				.filter(type -> plains.is(type.biomes()))
				.toList();
		context.assertTrue(!matchingTypes.isEmpty(), "main biome aura tags classify plains");
		float expectedModifier;
		if (matchingTypes.stream().anyMatch(type -> type.modifier().isEmpty())) {
			expectedModifier = 0.5F;
		} else {
			expectedModifier = (float) matchingTypes.stream()
					.mapToDouble(type -> type.modifier().orElseThrow())
					.average()
					.orElse(0.5D);
		}
		context.assertTrue(
				Math.abs(BiomeAuraTypes.modifier(plains, false) - expectedModifier) < 0.0001F,
				"biome aura modifier averages its matching types"
		);
		context.assertTrue(
				BiomeAuraTypes.modifier(Holder.direct(plains.value()), Map.of()) == 0.5F,
				"unclassified biomes use the default modifier"
		);
		context.assertTrue(
				BiomeAuraTypes.LOADER.serverEntries().containsKey(testId("test_primary"))
						&& BiomeAuraTypes.LOADER.serverEntries().containsKey(testId("test_secondary")),
				"gametest biome aura fixtures load"
		);

		BlockPos pos = BlockPos.ZERO;
		AuraAccess aura = AuraAccess.get();
		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 2.0F, 3.0F));
		context.assertTrue(aura.getBase(level, pos) == 100, "aura access reads the base");
		context.assertTrue(aura.getVis(level, pos) == 2.0F && aura.getFlux(level, pos) == 3.0F, "aura access reads vis and flux");
		context.assertTrue(aura.getTotalAura(level, pos) == 5.0F, "total aura sums vis and flux");
		context.assertTrue(Math.abs(aura.getFluxSaturation(level, pos) - 0.03F) < 0.0001F, "flux saturation divides by base");
		context.assertTrue(aura.drainVis(level, pos, 5.0F, true) == 2.0F, "simulated vis drain returns the available amount");
		context.assertTrue(aura.getVis(level, pos) == 2.0F, "simulated vis drain does not mutate aura");
		context.assertTrue(aura.drainFlux(level, pos, 1.0F, false) == 1.0F, "flux drain returns the drained amount");
		context.assertTrue(aura.getFlux(level, pos) == 2.0F, "actual flux drain mutates aura");
		aura.addVis(level, pos, -10.0F);
		aura.addFlux(level, pos, -10.0F, false);
		context.assertTrue(aura.getVis(level, pos) == 2.0F && aura.getFlux(level, pos) == 2.0F, "negative additions are ignored");
		aura.addVis(level, pos, 40000.0F);
		aura.addFlux(level, pos, 40000.0F, true);
		context.assertTrue(aura.getVis(level, pos) == 32766.0F && aura.getFlux(level, pos) == 32766.0F, "aura values clamp to 32766");

		Holder<WorldClock> overworldClock = level.registryAccess()
				.lookupOrThrow(Registries.WORLD_CLOCK)
				.getOrThrow(WorldClocks.OVERWORLD);
		level.clockManager().setTotalTicks(overworldClock, 0L);
		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 99.0F, 0.0F));
		new MinimalAuraSimulation().tick(level, chunk);
		context.assertTrue(Math.abs(aura.getVis(level, pos) - 99.25F) < 0.0001F, "full-moon regeneration adds at most phase vis");
		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 99.0F, 16.0F));
		new MinimalAuraSimulation().tick(level, chunk);
		context.assertTrue(aura.getVis(level, pos) == 99.0F, "flux prevents regeneration above base times phase max");
		context.assertTrue(MoonPhase.FULL_MOON.index() == 0, "the 26.3 moon phase index starts at full moon");

		BlockPos unloaded = new BlockPos(1_000_000, 64, 1_000_000);
		context.assertTrue(aura.getBase(level, unloaded) == 0 && aura.getVis(level, unloaded) == 0.0F, "unloaded chunks return zero aura");
		context.assertTrue(aura.drainVis(level, unloaded, 1.0F, false) == 0.0F, "unloaded chunks cannot be drained");
		context.succeed();
	}

	@GameTest
	public void playerKnowledgeAndWarpMatchTc6StateRules(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ServerPlayer observer = context.makeMockServerPlayerInLevel();
		KnowledgeAccess access = KnowledgeAccess.get();
		PlayerKnowledge knowledge = access.knowledge(player);
		PlayerWarp warp = access.warp(player);
		Identifier fixtureResearch = testId("fixture_main");
		Identifier unknownResearch = testId("not_loaded");

		AttachmentSyncPredicate targetOnly = AttachmentSyncPredicate.targetOnly();
		context.assertTrue(ModAttachments.KNOWLEDGE.isPersistent() && ModAttachments.KNOWLEDGE.isSynced()
						&& ModAttachments.KNOWLEDGE.copyOnDeath()
						&& ModAttachments.WARP.isPersistent() && ModAttachments.WARP.isSynced() && ModAttachments.WARP.copyOnDeath(),
				"knowledge and warp attachments persist, copy on death, and synchronize");
		context.assertTrue(targetOnly.test(player, player) && !targetOnly.test(player, observer),
				"targetOnly attachment sync includes the owner and excludes another player");
		context.assertTrue(!CommonConfig.DEFAULT.misc().wussMode(), "misc.wuss_mode defaults to false");
		context.assertTrue(knowledge.getResearchStatus(fixtureResearch) == ResearchStatus.UNKNOWN, "unknown research status");
		context.assertTrue(knowledge.getResearchStage(fixtureResearch) == -1, "unknown research stage is -1");
		context.assertTrue(!access.setResearchStage(player, fixtureResearch, 1), "stage cannot be set before research is known");
		context.assertTrue(access.addResearch(player, fixtureResearch), "adding research succeeds once");
		context.assertTrue(!access.addResearch(player, fixtureResearch), "duplicate research is rejected");
		context.assertTrue(knowledge.getResearchStatus(fixtureResearch) == ResearchStatus.IN_PROGRESS, "known staged research is in progress");
		context.assertTrue(knowledge.getResearchStage(fixtureResearch) == 0, "new research starts at stage zero");
		context.assertTrue(
				!knowledge.isResearchKnown(new ResearchReference(fixtureResearch, OptionalInt.of(1), false)),
				"research reference requires its requested stage"
		);
		context.assertTrue(access.setResearchStage(player, fixtureResearch, 1), "known research accepts a positive stage");
		context.assertTrue(
				knowledge.isResearchKnown(new ResearchReference(fixtureResearch, OptionalInt.of(1), false)),
				"research reference is known at the matching stage"
		);
		context.assertTrue(
				!knowledge.isResearchKnown(new ResearchReference(fixtureResearch, OptionalInt.of(2), false)),
				"research reference rejects a later stage"
		);
		context.assertTrue(access.setResearchFlag(player, fixtureResearch, ResearchFlag.PAGE), "research flag is added");
		context.assertTrue(knowledge.hasResearchFlag(fixtureResearch, ResearchFlag.PAGE), "research flag is readable");
		context.assertTrue(access.clearResearchFlag(player, fixtureResearch, ResearchFlag.PAGE), "research flag is cleared");
		context.assertTrue(!knowledge.hasResearchFlag(fixtureResearch, ResearchFlag.PAGE), "cleared research flag is absent");

		context.assertTrue(access.addResearch(player, unknownResearch), "unloaded research may still be known");
		context.assertTrue(knowledge.getResearchStatus(unknownResearch) == ResearchStatus.COMPLETE, "unknown entries are complete once known");
		Identifier category = testId("fixture");
		context.assertTrue(access.addKnowledge(player, KnowledgeType.THEORY, Optional.of(category), 31), "raw theory can be added");
		context.assertTrue(
				knowledge.getKnowledge(KnowledgeType.THEORY, Optional.of(category)) == 0
						&& knowledge.getKnowledgeRaw(KnowledgeType.THEORY, Optional.of(category)) == 31,
				"knowledge uses floored progression points"
		);
		context.assertTrue(access.addKnowledge(player, KnowledgeType.THEORY, Optional.of(category), 1), "knowledge reaches a full point");
		context.assertTrue(knowledge.getKnowledge(KnowledgeType.THEORY, Optional.of(category)) == 1, "theory progression is 32 raw units");
		context.assertTrue(!access.addKnowledge(player, KnowledgeType.THEORY, Optional.of(category), -33), "negative knowledge underflow is rejected");
		context.assertTrue(knowledge.getKnowledgeRaw(KnowledgeType.THEORY, Optional.of(category)) == 32, "failed knowledge mutation preserves raw value");
		context.assertTrue(access.addKnowledge(player, KnowledgeType.OBSERVATION, Optional.empty(), 17), "raw observation can be added");
		context.assertTrue(knowledge.getKnowledge(KnowledgeType.OBSERVATION, Optional.empty()) == 1, "observation progression is 16 raw units");

		access.setWarp(player, WarpType.PERMANENT, -20);
		context.assertTrue(warp.get(WarpType.PERMANENT) == 0, "warp clamps below zero");
		context.assertTrue(access.addWarp(player, WarpType.PERMANENT, 600) == 500, "warp addition clamps at 500");
		context.assertTrue(access.reduceWarp(player, WarpType.PERMANENT, 450) == 50, "warp reduction returns the new value");
		access.setWarp(player, WarpType.TEMPORARY, 5);
		context.assertTrue(access.reduceWarp(player, WarpType.TEMPORARY, 10) == 0, "warp reduction clamps at zero");
		access.setWarpCounter(player, -9);
		context.assertTrue(warp.getCounter() == -9, "warp counter follows TC6 without a clamp");
		context.assertTrue(ModAttachments.KNOWLEDGE.isPersistent() && ModAttachments.WARP.isPersistent(), "player attachments persist");
		access.clearKnowledge(player);
		context.assertTrue(knowledge.getResearchList().isEmpty() && knowledge.getKnowledgeRaw(KnowledgeType.THEORY, Optional.of(category)) == 0,
				"clearKnowledge resets research and knowledge");
		context.succeed();
	}

	@GameTest
	public void playerKnowledgeAndWarpDataCodecsRoundTrip(GameTestHelper context) {
		Identifier research = testId("fixture_main");
		Identifier category = testId("fixture");
		PlayerKnowledgeData knowledge = PlayerKnowledgeData.EMPTY
				.withResearch(research, new ResearchState(true, 2, Set.of(ResearchFlag.PAGE, ResearchFlag.POPUP)))
				.withKnowledge(new KnowledgeKey(KnowledgeType.THEORY, Optional.of(category)), 73);
		PlayerWarpData warp = new PlayerWarpData(125, 26, 4, -3);
		var knowledgeJson = PlayerKnowledgeData.CODEC.encodeStart(JsonOps.INSTANCE, knowledge).getOrThrow();
		var decodedKnowledge = PlayerKnowledgeData.CODEC.parse(JsonOps.INSTANCE, knowledgeJson).getOrThrow();
		var warpJson = PlayerWarpData.CODEC.encodeStart(JsonOps.INSTANCE, warp).getOrThrow();
		var decodedWarp = PlayerWarpData.CODEC.parse(JsonOps.INSTANCE, warpJson).getOrThrow();
		context.assertTrue(decodedKnowledge.equals(knowledge), "player knowledge JSON codec round trip");
		context.assertTrue(decodedWarp.equals(warp), "player warp JSON codec round trip");

		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), context.getLevel().registryAccess());
		PlayerKnowledgeData.STREAM_CODEC.encode(buffer, knowledge);
		PlayerKnowledgeData streamedKnowledge = PlayerKnowledgeData.STREAM_CODEC.decode(buffer);
		context.assertTrue(streamedKnowledge.equals(knowledge), "player knowledge stream codec round trip");
		buffer.clear();
		PlayerWarpData.STREAM_CODEC.encode(buffer, warp);
		PlayerWarpData streamedWarp = PlayerWarpData.STREAM_CODEC.decode(buffer);
		context.assertTrue(streamedWarp.equals(warp), "player warp stream codec round trip");
		buffer.release();
		context.succeed();
	}

	@GameTest
	public void researchLoadersBuildOrderedCategoriesAndValidatedIndex(GameTestHelper context) {
		ResearchAccess research = ResearchAccess.get();
		List<Identifier> expectedCategoryOrder = List.of(
				ThaumcraftRebornApi.id("basics"),
				ThaumcraftRebornApi.id("auromancy"),
				ThaumcraftRebornApi.id("alchemy"),
				ThaumcraftRebornApi.id("artifice"),
				ThaumcraftRebornApi.id("infusion"),
				ThaumcraftRebornApi.id("golemancy"),
				ThaumcraftRebornApi.id("eldritch"),
				testId("fixture")
		);
		context.assertTrue(research.categoryOrder(false).equals(expectedCategoryOrder), "categories sort by declared order then id");
		context.assertTrue(research.categories(false).size() == 8, "seven production categories and the test category load");

		Map<String, AspectList> expectedFormulas = Map.of(
				"basics", aspectFormula("herba", 5, "ordo", 5, "perditio", 5, "aer", 5, "ignis", 5, "terra", 3, "aqua", 5),
				"auromancy", aspectFormula("auram", 20, "praecantatio", 20, "vitium", 15, "vitreus", 5, "gelum", 5, "aer", 5),
				"alchemy", aspectFormula("alkimia", 30, "vitium", 10, "praecantatio", 10, "victus", 5, "aversio", 5, "desiderium", 5, "aqua", 5),
				"artifice", aspectFormula("machina", 10, "fabrico", 10, "metallum", 10, "instrumentum", 10, "potentia", 10,
						"lux", 5, "volatus", 5, "vinculum", 5, "ignis", 5),
				"infusion", aspectFormula("praecantatio", 30, "praemunio", 10, "instrumentum", 10, "vitium", 5, "fabrico", 5,
						"spiritus", 5, "terra", 3),
				"golemancy", aspectFormula("humanus", 20, "motus", 10, "cognitio", 10, "machina", 10, "permutatio", 5, "sensus", 5,
						"bestia", 5, "ordo", 5),
				"eldritch", aspectFormula("alienis", 20, "tenebrae", 10, "praecantatio", 5, "cognitio", 5, "vacuos", 5,
						"mortuus", 5, "exanimis", 5, "perditio", 5)
		);
		for (Map.Entry<String, AspectList> expected : expectedFormulas.entrySet()) {
			ResearchCategory category = research.category(ThaumcraftRebornApi.id(expected.getKey()), false).orElseThrow();
			context.assertTrue(category.formula().equals(expected.getValue()), "exact formula for category " + expected.getKey());
			context.assertTrue(category.icon().getPath().startsWith("textures/research/"), "category icon uses a future research texture id");
			if (!expected.getKey().equals("basics")) {
				context.assertTrue(category.researchKey().isPresent(), "non-basics category has its unlock research key");
			}
		}
		context.assertTrue(research.category(testId("fixture"), false).orElseThrow().formula()
						.equals(aspectFormula("terra", 3, "aer", 1)),
				"test category preserves aspect formula data");
		context.assertTrue(research.category(ThaumcraftRebornApi.id("basics"), false).orElseThrow().researchKey().isEmpty(),
				"BASICS has no unlock research key");

		Identifier fixtureCategory = testId("fixture");
		Identifier fixtureMainKey = testId("fixture_main");
		ResearchEntry fixtureMain = research.entry(fixtureMainKey, false).orElseThrow();
		context.assertTrue(fixtureMain.category().equals(fixtureCategory), "test research category is indexed");
		context.assertTrue(fixtureMain.meta().contains(ResearchMeta.HEX), "research metadata decodes");
		context.assertTrue(fixtureMain.icons().size() == 2 && fixtureMain.parents().size() == 2, "icons and parent references decode");
		context.assertTrue(fixtureMain.icons().stream().filter(ResearchIcon.Item.class::isInstance)
						.map(ResearchIcon.Item.class::cast)
						.anyMatch(icon -> icon.item().item().value() == Items.DIAMOND && icon.item().count() == 1),
				"item icons decode to deferred stack templates");
		context.assertTrue(fixtureMain.siblings().size() == 1 && fixtureMain.stages().size() == 2, "siblings and stages decode");
		context.assertTrue(fixtureMain.stages().getFirst().warp() == 3, "stage warp decodes");
		context.assertTrue(fixtureMain.stages().getFirst().requiredItem().getFirst().item()
						.map(template -> template.item().value() == Items.DIAMOND && template.count() == 1).orElse(false),
				"item requirement decodes to a deferred stack template");
		context.assertTrue(fixtureMain.stages().getFirst().requiredCraft().getFirst().tag().isPresent(), "tag requirement decodes");
		context.assertTrue(fixtureMain.rewardItem().getFirst().count() == 2, "item rewards decode to deferred stack templates");
		context.assertTrue(fixtureMain.rewardKnowledge().getFirst().amount() == 2, "knowledge rewards decode");
		context.assertTrue(fixtureMain.addenda().size() == 1, "addenda decode");
		context.assertTrue(research.entriesInCategory(fixtureCategory, false).size() == 7, "entries are grouped by category");
		context.assertTrue(research.bounds(fixtureCategory, false).orElseThrow()
						.equals(new io.github.rdarkknight.thaumcraftreborn.api.research.ResearchBounds(0, 7, -2, 4)),
				"category display bounds include loaded entry locations");
		context.assertTrue(research.entry(testId("fixture_invalid"), false).isEmpty(), "invalid files are skipped");
		context.assertTrue(research.entry(testId("fixture_unknown_category"), false).isEmpty(),
				"entries with unknown categories are skipped");
		context.assertTrue(!ResearchIndex.RESEARCH.serverEntries().containsKey(testId("fixture_invalid")),
				"loader isolates malformed entry files");

		ResearchReference parsed = ResearchReference.CODEC.parse(JsonOps.INSTANCE,
				com.google.gson.JsonParser.parseString("\"~thaumcraft_reborn_test:fixture_main@2\"")).getOrThrow();
		context.assertTrue(parsed.hiddenLine() && parsed.stage().getAsInt() == 2, "research reference accepts hidden lines and stages");
		context.assertTrue(
				ResearchReference.CODEC.parse(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString("\"bad key@x\"")).result().isEmpty(),
				"invalid research reference is rejected"
		);
		context.succeed();
	}

	@GameTest
	public void researchProgressionGrantsRewardsWarpFlagsAndRecursiveChanges(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ResearchAccess research = ResearchAccess.get();
		KnowledgeAccess knowledge = KnowledgeAccess.get();
		Identifier fixtureAuto = testId("fixture_auto");
		Identifier fixtureParent = testId("fixture_parent");
		Identifier missingParent = testId("missing_parent");
		Identifier fixtureMain = testId("fixture_main");
		Identifier fixtureSibling = testId("fixture_sibling");
		Identifier fixtureStrict = testId("fixture_strict");
		ResearchProgression.unlockAutomaticResearch(player);
		context.assertTrue(knowledge.knowledge(player).isResearchKnown(fixtureAuto), "AUTOUNLOCK research is granted on join");

		ServerPlayer strictPlayer = context.makeMockServerPlayerInLevel();
		KnowledgeAccess.get().addResearch(strictPlayer, fixtureParent);
		context.assertTrue(!research.doesPlayerHaveRequisites(strictPlayer, fixtureStrict),
				"an in-progress parent does not satisfy an unstaged prerequisite");
		context.assertTrue(research.completeResearch(strictPlayer, missingParent, false),
				"the unstaged pseudo-parent can be completed");
		context.assertTrue(!research.doesPlayerHaveRequisites(strictPlayer, fixtureMain),
				"a staged prerequisite is not satisfied before its requested stage");
		context.assertTrue(KnowledgeAccess.get().setResearchStage(strictPlayer, fixtureParent, 1),
				"an in-progress parent can reach stage one");
		context.assertTrue(research.doesPlayerHaveRequisites(strictPlayer, fixtureMain),
				"a staged prerequisite is satisfied once its requested stage is reached");
		context.assertTrue(!research.doesPlayerHaveRequisites(strictPlayer, fixtureStrict),
				"reaching a stage does not complete an unstaged prerequisite");
		context.assertTrue(research.completeResearch(strictPlayer, fixtureParent, false),
				"the parent can then be completed");
		context.assertTrue(research.doesPlayerHaveRequisites(strictPlayer, fixtureStrict),
				"an unstaged prerequisite is satisfied after completion");

		context.assertTrue(research.completeResearch(player, fixtureParent, false), "parent completes without sync");
		context.assertTrue(research.completeResearch(player, missingParent, false), "unknown pseudo-parent can be completed");
		context.assertTrue(research.doesPlayerHaveRequisites(player, fixtureMain), "all parent stages satisfy requisites");
		context.assertTrue(research.progressResearch(player, fixtureMain, true), "first progression advances to the first stage");
		context.assertTrue(knowledge.knowledge(player).getResearchStage(fixtureMain) == 1, "first stage index is stored");
		context.assertTrue(!knowledge.knowledge(player).hasResearchFlag(fixtureMain, ResearchFlag.POPUP),
				"an intermediate stage does not set completion flags");
		context.assertTrue(research.progressResearch(player, fixtureMain, true), "second progression completes the entry");
		context.assertTrue(knowledge.knowledge(player).isResearchComplete(fixtureMain), "last stage completes the research");
		context.assertTrue(knowledge.knowledge(player).hasResearchFlag(fixtureMain, ResearchFlag.POPUP)
						&& knowledge.knowledge(player).hasResearchFlag(fixtureMain, ResearchFlag.RESEARCH),
				"completion sets the sync flags");
		context.assertTrue(knowledge.knowledge(player).hasResearchFlag(fixtureMain, ResearchFlag.PAGE),
				"unlocked addenda set the PAGE flag");
		context.assertTrue(knowledge.knowledge(player).isResearchComplete(fixtureSibling), "siblings complete recursively");
		context.assertTrue(knowledge.knowledge(player).getKnowledgeRaw(KnowledgeType.THEORY,
				Optional.of(testId("fixture"))) == 64, "knowledge reward uses raw progression units");
		context.assertTrue(player.getInventory().countItem(Items.DIAMOND) == 2, "item reward enters the inventory");
		context.assertTrue(knowledge.warp(player).get(WarpType.PERMANENT) == 2
						&& knowledge.warp(player).get(WarpType.NORMAL) == 2,
				"stage warp is split between permanent and normal warp");

		ServerPlayer recursivePlayer = context.makeMockServerPlayerInLevel();
		context.assertTrue(research.giveRecursiveResearch(recursivePlayer, fixtureMain), "recursive grant reports a change");
		context.assertTrue(knowledge.knowledge(recursivePlayer).isResearchComplete(fixtureMain)
						&& knowledge.knowledge(recursivePlayer).isResearchComplete(fixtureParent)
						&& knowledge.knowledge(recursivePlayer).isResearchComplete(missingParent),
				"recursive grant includes parent and pseudo-parent requisites");
		context.assertTrue(knowledge.knowledge(recursivePlayer).hasResearchFlag(fixtureSibling, ResearchFlag.PAGE),
				"recursive grant adds PAGE to entries whose stages require the granted key");

		ServerPlayer directRequirementPlayer = context.makeMockServerPlayerInLevel();
		Identifier directTarget = testId("fixture_required_target");
		Identifier directRequirement = testId("fixture_required");
		context.assertTrue(research.giveRecursiveResearch(directRequirementPlayer, directTarget),
				"recursive grant completes the requested entry");
		context.assertTrue(knowledge.knowledge(directRequirementPlayer).isResearchComplete(directTarget)
						&& !knowledge.knowledge(directRequirementPlayer).isResearchKnown(directRequirement)
						&& !knowledge.knowledge(directRequirementPlayer).isResearchKnown(fixtureParent),
				"required_research is completed directly rather than recursively granted");
		context.assertTrue(research.revokeRecursiveResearch(recursivePlayer, fixtureParent), "recursive revoke removes the target");
		context.assertTrue(!knowledge.knowledge(recursivePlayer).isResearchKnown(fixtureParent)
						&& !knowledge.knowledge(recursivePlayer).isResearchKnown(fixtureMain)
						&& !knowledge.knowledge(recursivePlayer).isResearchKnown(fixtureSibling),
				"recursive revoke removes dependent research");
		context.succeed();
	}

	@GameTest
	public void unavailableBiomePlacementProviderRecordsRequests(GameTestHelper context) {
		BiomePlacementAccess access = BiomePlacementAccess.get();
		Identifier biomeId = ThaumcraftRebornApi.id("placement_test");
		var biome = ResourceKey.create(Registries.BIOME, biomeId);
		Climate.ParameterPoint parameters = Climate.parameters(0.1F, -0.2F, 0.3F, -0.4F, 0.5F, -0.6F, 0.7F);
		int requestCount = BiomePlacementAccess.NONE.requests().size();
		BiomePlacementAccess.NONE.addOverworldBiome(biome, parameters);
		context.assertTrue(!access.isAvailable() && access.providerId().equals("none"), "no biome placement provider is installed");
		context.assertTrue(BiomePlacementAccess.NONE.requests().size() == requestCount + 1, "NONE records the placement request");
		context.assertTrue(BiomePlacementAccess.NONE.requests().getLast().equals(new BiomePlacementAccess.Request(biome, parameters)),
				"placement request preserves its biome and climate parameters");
		boolean immutable = false;
		try {
			BiomePlacementAccess.NONE.requests().clear();
		} catch (UnsupportedOperationException exception) {
			immutable = true;
		}
		context.assertTrue(immutable, "request list is exposed as an unmodifiable view");
		context.succeed();
	}

	private static AspectList aspectFormula(Object... values) {
		AspectList.Builder builder = AspectList.builder();
		for (int index = 0; index < values.length; index += 2) {
			Aspect aspect = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id((String) values[index]));
			builder.add(aspect, (Integer) values[index + 1]);
		}
		return builder.build();
	}

	private static Identifier testId(String path) {
		return Identifier.fromNamespaceAndPath("thaumcraft_reborn_test", path);
	}

	private static ItemStack vanillaItem(String path) {
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", path)));
	}

	@Override
	public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
		method.invoke(this, context);
	}

	private static boolean rejectsStream(GameTestHelper context, int aspectId, int amount) {
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), context.getLevel().registryAccess());
		try {
			ByteBufCodecs.VAR_INT.encode(buffer, 1);
			ByteBufCodecs.VAR_INT.encode(buffer, aspectId);
			ByteBufCodecs.VAR_INT.encode(buffer, amount);
			AspectList.STREAM_CODEC.decode(buffer);
			return false;
		} catch (RuntimeException exception) {
			return true;
		} finally {
			buffer.release();
		}
	}

	private static List<ExpectedAspect> expectedAspects() {
		return List.of(
				new ExpectedAspect("aer", 16777086, List.of(), AspectBlend.ADDITIVE, Optional.of(ChatFormatting.YELLOW)),
				new ExpectedAspect("terra", 5685248, List.of(), AspectBlend.ADDITIVE, Optional.of(ChatFormatting.DARK_GREEN)),
				new ExpectedAspect("ignis", 16734721, List.of(), AspectBlend.ADDITIVE, Optional.of(ChatFormatting.RED)),
				new ExpectedAspect("aqua", 3986684, List.of(), AspectBlend.ADDITIVE, Optional.of(ChatFormatting.DARK_AQUA)),
				new ExpectedAspect("ordo", 14013676, List.of(), AspectBlend.ADDITIVE, Optional.of(ChatFormatting.GRAY)),
				new ExpectedAspect("perditio", 4210752, List.of(), AspectBlend.TRANSLUCENT, Optional.of(ChatFormatting.DARK_GRAY)),
				new ExpectedAspect("vacuos", 8947848, List.of("aer", "perditio"), AspectBlend.TRANSLUCENT, Optional.empty()),
				new ExpectedAspect("lux", 16777152, List.of("aer", "ignis"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("motus", 13487348, List.of("aer", "ordo"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("gelum", 14811135, List.of("ignis", "perditio"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("vitreus", 8454143, List.of("terra", "aer"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("metallum", 11908557, List.of("terra", "ordo"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("victus", 14548997, List.of("terra", "aqua"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("mortuus", 6946821, List.of("aqua", "perditio"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("potentia", 12648447, List.of("ordo", "ignis"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("permutatio", 5735255, List.of("perditio", "ordo"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("praecantatio", 13566207, List.of("potentia", "aer"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("auram", 16761087, List.of("praecantatio", "aer"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("alkimia", 2337949, List.of("praecantatio", "aqua"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("vitium", 8388736, List.of("perditio", "praecantatio"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("tenebrae", 2236962, List.of("vacuos", "lux"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("alienis", 8409216, List.of("vacuos", "tenebrae"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("volatus", 15198167, List.of("aer", "motus"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("herba", 109568, List.of("victus", "terra"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("instrumentum", 4210926, List.of("metallum", "potentia"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("fabrico", 8428928, List.of("permutatio", "instrumentum"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("machina", 8421536, List.of("motus", "instrumentum"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("vinculum", 10125440, List.of("motus", "perditio"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("spiritus", 15461371, List.of("victus", "mortuus"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("cognitio", 16356991, List.of("ignis", "spiritus"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("sensus", 12648384, List.of("aer", "spiritus"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("aversio", 12603472, List.of("spiritus", "perditio"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("praemunio", 49344, List.of("spiritus", "terra"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("desiderium", 15121988, List.of("spiritus", "vacuos"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("exanimis", 3817472, List.of("motus", "mortuus"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("bestia", 10445833, List.of("motus", "victus"), AspectBlend.ADDITIVE, Optional.empty()),
				new ExpectedAspect("humanus", 16766912, List.of("spiritus", "victus"), AspectBlend.ADDITIVE, Optional.empty())
		);
	}

	private record ExpectedAspect(
			String tag,
			int color,
			List<String> components,
			AspectBlend blend,
			Optional<ChatFormatting> chatFormatting
	) {
	}
}
