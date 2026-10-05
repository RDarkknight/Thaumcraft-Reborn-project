package io.github.rdarkknight.thaumcraftreborn.systems.aspect;

import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectBlend;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspects;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class AspectDefinitions {
	public static final Aspect AIR = primal(Aspects.AIR, "aer", 16777086, ChatFormatting.YELLOW, AspectBlend.ADDITIVE);
	public static final Aspect EARTH = primal(Aspects.EARTH, "terra", 5685248, ChatFormatting.DARK_GREEN, AspectBlend.ADDITIVE);
	public static final Aspect FIRE = primal(Aspects.FIRE, "ignis", 16734721, ChatFormatting.RED, AspectBlend.ADDITIVE);
	public static final Aspect WATER = primal(Aspects.WATER, "aqua", 3986684, ChatFormatting.DARK_AQUA, AspectBlend.ADDITIVE);
	public static final Aspect ORDER = primal(Aspects.ORDER, "ordo", 14013676, ChatFormatting.GRAY, AspectBlend.ADDITIVE);
	public static final Aspect ENTROPY = primal(Aspects.ENTROPY, "perditio", 4210752, ChatFormatting.DARK_GRAY, AspectBlend.TRANSLUCENT);
	public static final Aspect VOID = compound(Aspects.VOID, "vacuos", 8947848, AIR, ENTROPY, AspectBlend.TRANSLUCENT);
	public static final Aspect LIGHT = compound(Aspects.LIGHT, "lux", 16777152, AIR, FIRE, AspectBlend.ADDITIVE);
	public static final Aspect MOTION = compound(Aspects.MOTION, "motus", 13487348, AIR, ORDER, AspectBlend.ADDITIVE);
	public static final Aspect COLD = compound(Aspects.COLD, "gelum", 14811135, FIRE, ENTROPY, AspectBlend.ADDITIVE);
	public static final Aspect CRYSTAL = compound(Aspects.CRYSTAL, "vitreus", 8454143, EARTH, AIR, AspectBlend.ADDITIVE);
	public static final Aspect METAL = compound(Aspects.METAL, "metallum", 11908557, EARTH, ORDER, AspectBlend.ADDITIVE);
	public static final Aspect LIFE = compound(Aspects.LIFE, "victus", 14548997, EARTH, WATER, AspectBlend.ADDITIVE);
	public static final Aspect DEATH = compound(Aspects.DEATH, "mortuus", 6946821, WATER, ENTROPY, AspectBlend.ADDITIVE);
	public static final Aspect ENERGY = compound(Aspects.ENERGY, "potentia", 12648447, ORDER, FIRE, AspectBlend.ADDITIVE);
	public static final Aspect EXCHANGE = compound(Aspects.EXCHANGE, "permutatio", 5735255, ENTROPY, ORDER, AspectBlend.ADDITIVE);
	public static final Aspect MAGIC = compound(Aspects.MAGIC, "praecantatio", 13566207, ENERGY, AIR, AspectBlend.ADDITIVE);
	public static final Aspect AURA = compound(Aspects.AURA, "auram", 16761087, MAGIC, AIR, AspectBlend.ADDITIVE);
	public static final Aspect ALCHEMY = compound(Aspects.ALCHEMY, "alkimia", 2337949, MAGIC, WATER, AspectBlend.ADDITIVE);
	public static final Aspect FLUX = compound(Aspects.FLUX, "vitium", 8388736, ENTROPY, MAGIC, AspectBlend.ADDITIVE);
	public static final Aspect DARKNESS = compound(Aspects.DARKNESS, "tenebrae", 2236962, VOID, LIGHT, AspectBlend.ADDITIVE);
	public static final Aspect ELDRITCH = compound(Aspects.ELDRITCH, "alienis", 8409216, VOID, DARKNESS, AspectBlend.ADDITIVE);
	public static final Aspect FLIGHT = compound(Aspects.FLIGHT, "volatus", 15198167, AIR, MOTION, AspectBlend.ADDITIVE);
	public static final Aspect PLANT = compound(Aspects.PLANT, "herba", 109568, LIFE, EARTH, AspectBlend.ADDITIVE);
	public static final Aspect TOOL = compound(Aspects.TOOL, "instrumentum", 4210926, METAL, ENERGY, AspectBlend.ADDITIVE);
	public static final Aspect CRAFT = compound(Aspects.CRAFT, "fabrico", 8428928, EXCHANGE, TOOL, AspectBlend.ADDITIVE);
	public static final Aspect MECHANISM = compound(Aspects.MECHANISM, "machina", 8421536, MOTION, TOOL, AspectBlend.ADDITIVE);
	public static final Aspect TRAP = compound(Aspects.TRAP, "vinculum", 10125440, MOTION, ENTROPY, AspectBlend.ADDITIVE);
	public static final Aspect SOUL = compound(Aspects.SOUL, "spiritus", 15461371, LIFE, DEATH, AspectBlend.ADDITIVE);
	public static final Aspect MIND = compound(Aspects.MIND, "cognitio", 16356991, FIRE, SOUL, AspectBlend.ADDITIVE);
	public static final Aspect SENSES = compound(Aspects.SENSES, "sensus", 12648384, AIR, SOUL, AspectBlend.ADDITIVE);
	public static final Aspect AVERSION = compound(Aspects.AVERSION, "aversio", 12603472, SOUL, ENTROPY, AspectBlend.ADDITIVE);
	public static final Aspect PROTECT = compound(Aspects.PROTECT, "praemunio", 49344, SOUL, EARTH, AspectBlend.ADDITIVE);
	public static final Aspect DESIRE = compound(Aspects.DESIRE, "desiderium", 15121988, SOUL, VOID, AspectBlend.ADDITIVE);
	public static final Aspect UNDEAD = compound(Aspects.UNDEAD, "exanimis", 3817472, MOTION, DEATH, AspectBlend.ADDITIVE);
	public static final Aspect BEAST = compound(Aspects.BEAST, "bestia", 10445833, MOTION, LIFE, AspectBlend.ADDITIVE);
	public static final Aspect MAN = compound(Aspects.MAN, "humanus", 16766912, SOUL, LIFE, AspectBlend.ADDITIVE);

	public static final List<Aspect> ALL = List.of(
			AIR, EARTH, FIRE, WATER, ORDER, ENTROPY, VOID, LIGHT, MOTION, COLD, CRYSTAL, METAL,
			LIFE, DEATH, ENERGY, EXCHANGE, MAGIC, AURA, ALCHEMY, FLUX, DARKNESS, ELDRITCH,
			FLIGHT, PLANT, TOOL, CRAFT, MECHANISM, TRAP, SOUL, MIND, SENSES, AVERSION, PROTECT,
			DESIRE, UNDEAD, BEAST, MAN
	);

	private AspectDefinitions() {
	}

	public static void init() {
	}

	private static Aspect primal(
			ResourceKey<Aspect> key,
			String tag,
			int color,
			ChatFormatting chatFormatting,
			AspectBlend blend
	) {
		return Registry.register(
				ThaumcraftRegistries.ASPECT,
				key,
				new Aspect(tag, color, List.of(), blend, Optional.of(chatFormatting))
		);
	}

	private static Aspect compound(
			ResourceKey<Aspect> key,
			String tag,
			int color,
			Aspect first,
			Aspect second,
			AspectBlend blend
	) {
		return Registry.register(
				ThaumcraftRegistries.ASPECT,
				key,
				new Aspect(tag, color, List.of(first, second), blend, Optional.empty())
		);
	}
}
