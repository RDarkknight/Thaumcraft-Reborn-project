package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspects;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.content.crucible.CrucibleBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;

public final class StageDCrucibleGameTests {
	@GameTest
	public void heatRampsAndCoolsAndStrictlyRequiresWater(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		BlockPos pos = crucible.getBlockPos();
		context.getLevel().setBlock(pos.below(), Blocks.LAVA.defaultBlockState(), 3);
		crucible.setWaterMb(1000);
		CrucibleBlockEntity.serverTick(context.getLevel(), pos, context.getLevel().getBlockState(pos), crucible);
		context.assertTrue(crucible.heat() == 1, "wet crucible gains one heat over lava");
		context.getLevel().setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);
		CrucibleBlockEntity.serverTick(context.getLevel(), pos, context.getLevel().getBlockState(pos), crucible);
		context.assertTrue(crucible.heat() == 0, "crucible cools by one without a heater");
		crucible.setHeat(150);
		context.assertTrue(!crucible.processItem(new ItemStack(Items.STONE), null).isEmpty(), "heat 150 does not process");
		crucible.setHeat(151);
		context.assertTrue(crucible.processItem(new ItemStack(Items.STONE), null).isEmpty(), "heat 151 processes");
		context.assertTrue(crucible.aspects().amount(aspect(Aspects.EARTH)) > 0, "stone dissolves into its aspects");
		crucible.setWaterMb(0);
		context.assertTrue(!crucible.processItem(new ItemStack(Items.STONE), null).isEmpty(), "an empty tank prevents processing");
		context.succeed();
	}

	@GameTest
	public void waterBucketFillsTheCrucibleAndReturnsAnEmptyBucket(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.setPos(crucible.getBlockPos().getX() + 0.5, crucible.getBlockPos().getY() + 1.0,
				crucible.getBlockPos().getZ() + 0.5);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
		BlockPos pos = crucible.getBlockPos();
		BlockPos localPos = new BlockPos(1, 1, 1);
		context.useBlock(localPos, player,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
		context.assertTrue(crucible.waterMb() == 1000
						&& player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).is(Items.BUCKET),
				"water bucket fills 1000 mB and becomes an empty bucket; water=" + crucible.waterMb()
						+ ", held=" + player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND));
		context.succeed();
	}

	@GameTest
	public void thaumiumRecipeConsumesOnlyRequiredAspectsAndRecordsCraft(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		unlock(player, "metallurgy", 2);
		crucible.setHeat(151);
		crucible.setWaterMb(1000);
		crucible.setAspects(AspectList.builder()
				.add(aspect(Aspects.MAGIC), 5)
				.add(aspect(Aspects.EARTH), 7)
				.build());
		context.assertTrue(context.getLevel().getServer().getRecipeManager().getRecipes().stream()
						.map(holder -> holder.value())
						.filter(io.github.rdarkknight.thaumcraftreborn.systems.recipe.ThaumcraftRecipe.class::isInstance)
						.map(io.github.rdarkknight.thaumcraftreborn.systems.recipe.ThaumcraftRecipe.class::cast)
						.anyMatch(recipe -> recipe.getType()
								== io.github.rdarkknight.thaumcraftreborn.systems.recipe.ThaumcraftRecipe.crucibleType()
								&& recipe.result().create().is(ThaumcraftContent.INGOT_THAUMIUM)
								&& recipe.matchesCrucible(new ItemStack(Items.IRON_INGOT), crucible.aspects())),
				"loaded Thaumium recipe matches the iron catalyst and required aspect list");
		context.assertTrue(crucible.processItem(new ItemStack(Items.IRON_INGOT), player).isEmpty(),
				"known Thaumium recipe processes iron");
		List<ItemEntity> output = context.getLevel().getEntitiesOfClass(ItemEntity.class,
				new AABB(crucible.getBlockPos()).inflate(3.0),
				entity -> entity.getItem().is(ThaumcraftContent.INGOT_THAUMIUM));
		String state = "output=" + output.size() + ", water=" + crucible.waterMb()
				+ ", praecantatio=" + crucible.aspects().amount(aspect(Aspects.MAGIC))
				+ ", terra=" + crucible.aspects().amount(aspect(Aspects.EARTH))
				+ ", crafts=" + CraftEventRecorder.count(player, ThaumcraftRebornApi.id("ingot_thaumium"));
		context.assertTrue(output.size() == 1 && crucible.waterMb() == 950
						&& crucible.aspects().amount(aspect(Aspects.MAGIC)) == 0
						&& crucible.aspects().amount(aspect(Aspects.EARTH)) == 2
						&& CraftEventRecorder.count(player, ThaumcraftRebornApi.id("ingot_thaumium")) == 1,
				"recipe consumes 50 mB and required aspects only, preserves extras, and records output: " + state);
		context.succeed();
	}

	@GameTest
	public void thrownFiveIronIngotsAreProcessedInOneCrucibleCollision(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		unlock(player, "metallurgy", 2);
		crucible.setHeat(151);
		crucible.setWaterMb(1000);
		crucible.setAspects(AspectList.builder()
				.add(aspect(Aspects.MAGIC), 25)
				.add(aspect(Aspects.EARTH), 25)
				.build());
		BlockPos pos = crucible.getBlockPos();
		ItemEntity thrown = new ItemEntity(context.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5,
				pos.getZ() + 0.5, new ItemStack(Items.IRON_INGOT, 5));
		thrown.setThrower(player);
		context.getLevel().addFreshEntity(thrown);

		crucible.entityInside(thrown);

		List<ItemEntity> output = context.getLevel().getEntitiesOfClass(ItemEntity.class,
				new AABB(pos).inflate(3.0), entity -> entity.getItem().is(ThaumcraftContent.INGOT_THAUMIUM));
		context.assertTrue(!thrown.isAlive() && output.size() == 5 && crucible.waterMb() == 750
						&& CraftEventRecorder.count(player, ThaumcraftRebornApi.id("ingot_thaumium")) == 5,
				"one collision processes five recipes, ejects five ingots, and spends 250 mB");
		ItemEntity first = output.getFirst();
		context.assertTrue(first.getDeltaMovement().x == 0.0 && first.getDeltaMovement().y == 0.075
						&& first.getDeltaMovement().z == 0.0,
				"the first ejected item uses TC6's centered upward motion");
		context.succeed();
	}

	@GameTest
	public void thrownIronStackDissolvesUnitsWithoutRecipeAspects(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		unlock(player, "metallurgy", 2);
		crucible.setHeat(151);
		crucible.setWaterMb(1000);
		crucible.setAspects(AspectList.builder()
				.add(aspect(Aspects.MAGIC), 10)
				.add(aspect(Aspects.EARTH), 10)
				.build());
		AspectList dissolvedIron = AspectLookup.get().getAspects(context.getLevel(), new ItemStack(Items.IRON_INGOT));
		BlockPos pos = crucible.getBlockPos();
		ItemEntity thrown = new ItemEntity(context.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5,
				pos.getZ() + 0.5, new ItemStack(Items.IRON_INGOT, 5));
		thrown.setThrower(player);
		context.getLevel().addFreshEntity(thrown);

		crucible.entityInside(thrown);

		List<ItemEntity> output = context.getLevel().getEntitiesOfClass(ItemEntity.class,
				new AABB(pos).inflate(3.0), entity -> entity.getItem().is(ThaumcraftContent.INGOT_THAUMIUM));
		context.assertTrue(!thrown.isAlive() && output.size() == 2 && crucible.waterMb() == 900,
				"two units craft and the remaining three dissolve without additional water");
		context.assertTrue(crucible.aspects().visSize() == dissolvedIron.visSize() * 3
						&& CraftEventRecorder.count(player, ThaumcraftRebornApi.id("ingot_thaumium")) == 2,
				"the dissolved remainder contributes three iron aspect lists and records two crafts");
		context.succeed();
	}

	@GameTest
	public void lockedThaumiumInputDissolvesInsteadOfCrafting(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		crucible.setHeat(151);
		crucible.setWaterMb(1000);
		context.assertTrue(crucible.processItem(new ItemStack(Items.IRON_INGOT), player).isEmpty(),
				"locked iron dissolves rather than crafting Thaumium");
		context.assertTrue(crucible.waterMb() == 1000 && crucible.aspects().visSize() > 0
						&& context.getLevel().getEntitiesOfClass(ItemEntity.class,
								new AABB(crucible.getBlockPos()).inflate(3.0),
								entity -> entity.getItem().is(ThaumcraftContent.INGOT_THAUMIUM)).isEmpty(),
				"locked input preserves water and yields no Thaumium");
		context.succeed();
	}

	@GameTest
	public void matchingRecipesPreferTheLargestAspectCost(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		unlock(player, "unlockalchemy", 3);
		unlock(player, "hedgealchemy", 2);
		crucible.setHeat(151);
		crucible.setWaterMb(1000);
		crucible.setAspects(AspectList.builder()
				.add(aspect(Aspects.ENERGY), 10)
				.add(aspect(Aspects.FIRE), 10)
				.add(aspect(Aspects.LIGHT), 10)
				.add(aspect(Aspects.SENSES), 5)
				.build());
		context.assertTrue(crucible.processItem(new ItemStack(Items.GLOWSTONE_DUST), player).isEmpty(),
				"matching glowstone recipes process");
		context.assertTrue(context.getLevel().getEntitiesOfClass(ItemEntity.class,
						new AABB(crucible.getBlockPos()).inflate(3.0),
						entity -> entity.getItem().is(ThaumcraftContent.NITOR_YELLOW.asItem())).size() == 1,
				"the 30-vis nitor recipe wins over the 15-vis hedge recipe");
		context.succeed();
	}

	@GameTest
	public void aspectlessItemsRemainAndRandomSpillsPolluteByAspect(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		crucible.setHeat(151);
		crucible.setWaterMb(1000);
		ItemStack aspectless = List.of(
						new ItemStack(Items.DEBUG_STICK),
						new ItemStack(Items.COMMAND_BLOCK),
						new ItemStack(ThaumcraftContent.CRUCIBLE.asItem()))
				.stream()
				.filter(stack -> AspectLookup.get().getAspects(context.getLevel(), stack).isEmpty())
				.findFirst()
				.orElse(ItemStack.EMPTY);
		context.assertTrue(!aspectless.isEmpty(), "test fixture contains an item without aspects");
		context.assertTrue(!crucible.processItem(aspectless, null).isEmpty(), "an item with no aspects is not consumed");
		Aspect earth = aspect(Aspects.EARTH);
		crucible.setAspects(AspectList.of(earth, 501));
		CrucibleBlockEntity.serverTick(context.getLevel(), crucible.getBlockPos(),
				context.getLevel().getBlockState(crucible.getBlockPos()), crucible);
		context.assertTrue(crucible.aspects().amount(earth) == 500
						&& AuraAccess.get().getFlux(context.getLevel(), crucible.getBlockPos()) == 0.25F,
				"overflow spills one ordinary aspect for 0.25 flux");
		Aspect flux = aspect(Aspects.FLUX);
		crucible.setAspects(AspectList.of(flux, 501));
		CrucibleBlockEntity.serverTick(context.getLevel(), crucible.getBlockPos(),
				context.getLevel().getBlockState(crucible.getBlockPos()), crucible);
		context.assertTrue(crucible.aspects().amount(flux) == 500
						&& AuraAccess.get().getFlux(context.getLevel(), crucible.getBlockPos()) == 1.25F,
				"overflow spills one flux aspect for 1.0 additional flux");
		context.succeed();
	}

	@GameTest
	public void sneakAndBlockBreakSpillAllContentsWithExactFlux(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		BlockPos pos = crucible.getBlockPos();
		crucible.setWaterMb(1000);
		crucible.setAspects(AspectList.builder()
				.add(aspect(Aspects.FLUX), 4)
				.add(aspect(Aspects.EARTH), 2)
				.build());
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
		player.setShiftKeyDown(true);
		var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		context.getLevel().getBlockState(pos).useWithoutItem(context.getLevel(), player, hit);
		context.assertTrue(crucible.waterMb() == 0 && crucible.aspects().isEmpty()
						&& AuraAccess.get().getFlux(context.getLevel(), pos) == 4.5F,
				"sneak-empty-hand spill clears contents and adds 4.5 flux; water=" + crucible.waterMb()
						+ ", aspects=" + crucible.aspects() + ", flux="
						+ AuraAccess.get().getFlux(context.getLevel(), pos));
		crucible.setWaterMb(1000);
		crucible.setAspects(AspectList.of(aspect(Aspects.EARTH), 4));
		context.destroyBlock(new BlockPos(1, 1, 1));
		context.assertTrue(AuraAccess.get().getFlux(context.getLevel(), pos) == 5.5F,
				"breaking the crucible spills four more vis at 0.25 flux each");
		context.succeed();
	}

	@GameTest
	public void hotWetCrucibleDamagesLivingEntitiesEveryTenCallbacks(GameTestHelper context) {
		CrucibleBlockEntity crucible = placeCrucible(context);
		crucible.setHeat(151);
		crucible.setWaterMb(1000);
		EntityType<?> cowType = BuiltInRegistries.ENTITY_TYPE.getValue(
				Identifier.withDefaultNamespace("cow"));
		LivingEntity cow = (LivingEntity) cowType.create(context.getLevel(), EntitySpawnReason.TRIGGERED);
		context.assertTrue(cow != null, "cow test entity exists");
		cow.setPos(crucible.getBlockPos().getX() + 0.5, crucible.getBlockPos().getY() + 1.0,
				crucible.getBlockPos().getZ() + 0.5);
		context.getLevel().addFreshEntity(cow);
		float health = cow.getHealth();
		for (int i = 0; i < 9; i++) {
			crucible.entityInside(cow);
		}
		context.assertTrue(cow.getHealth() == health, "first nine callbacks do not damage");
		crucible.entityInside(cow);
		context.assertTrue(cow.getHealth() == health - 1.0F,
				"tenth callback deals one in-fire damage; before=" + health + ", after=" + cow.getHealth());
		context.succeed();
	}

	@GameTest
	public void crucibleStatePersistsAndClusterSmeltingRecipesLoad(GameTestHelper context) {
		CrucibleBlockEntity original = placeCrucible(context);
		original.setHeat(177);
		original.setWaterMb(650);
		original.setAspects(AspectList.builder().add(aspect(Aspects.EARTH), 7).build());
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,
				context.getLevel().registryAccess());
		original.saveWithoutMetadata(output);
		CrucibleBlockEntity restored = new CrucibleBlockEntity(original.getBlockPos(),
				ThaumcraftContent.CRUCIBLE.defaultBlockState());
		restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
				context.getLevel().registryAccess(), output.buildResult()));
		context.assertTrue(restored.heat() == 177 && restored.waterMb() == 650
						&& restored.aspects().amount(aspect(Aspects.EARTH)) == 7,
				"heat, water, and aspects survive save/load");
		var recipes = context.getLevel().getServer().getRecipeManager();
		for (String id : List.of("smelt_cluster_iron", "smelt_cluster_gold", "smelt_cluster_copper", "smelt_cluster_cinnabar")) {
			context.assertTrue(recipes.byKey(recipeId("thaumcraft_reborn", id)).isPresent(),
					id + " recipe is registered");
		}
		context.succeed();
	}

	private static CrucibleBlockEntity placeCrucible(GameTestHelper context) {
		BlockPos pos = context.absolutePos(new BlockPos(1, 1, 1));
		context.getLevel().setBlock(pos, ThaumcraftContent.CRUCIBLE.defaultBlockState(), 3);
		context.getLevel().getChunkAt(pos).setAttached(ModAttachments.AURA,
				new AuraChunkData((short) 100, 0.0F, 0.0F));
		return (CrucibleBlockEntity) context.getLevel().getBlockEntity(pos);
	}

	private static Aspect aspect(net.minecraft.resources.ResourceKey<Aspect> key) {
		return ThaumcraftRegistries.ASPECT.getValue(key.identifier());
	}

	private static ResourceKey<Recipe<?>> recipeId(String namespace, String path) {
		return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(namespace, path));
	}

	private static void unlock(ServerPlayer player, String key, int stage) {
		var id = ThaumcraftRebornApi.id(key);
		KnowledgeAccess.get().addResearch(player, id);
		KnowledgeAccess.get().setResearchStage(player, id, stage);
	}
}
