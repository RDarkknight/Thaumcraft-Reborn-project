package io.github.rdarkknight.thaumcraftreborn.api.aspect;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.registry.ThaumcraftRegistryKeys;
import net.minecraft.resources.ResourceKey;

public final class Aspects {
	public static final ResourceKey<Aspect> AIR = key("aer");
	public static final ResourceKey<Aspect> EARTH = key("terra");
	public static final ResourceKey<Aspect> FIRE = key("ignis");
	public static final ResourceKey<Aspect> WATER = key("aqua");
	public static final ResourceKey<Aspect> ORDER = key("ordo");
	public static final ResourceKey<Aspect> ENTROPY = key("perditio");
	public static final ResourceKey<Aspect> VOID = key("vacuos");
	public static final ResourceKey<Aspect> LIGHT = key("lux");
	public static final ResourceKey<Aspect> MOTION = key("motus");
	public static final ResourceKey<Aspect> COLD = key("gelum");
	public static final ResourceKey<Aspect> CRYSTAL = key("vitreus");
	public static final ResourceKey<Aspect> METAL = key("metallum");
	public static final ResourceKey<Aspect> LIFE = key("victus");
	public static final ResourceKey<Aspect> DEATH = key("mortuus");
	public static final ResourceKey<Aspect> ENERGY = key("potentia");
	public static final ResourceKey<Aspect> EXCHANGE = key("permutatio");
	public static final ResourceKey<Aspect> MAGIC = key("praecantatio");
	public static final ResourceKey<Aspect> AURA = key("auram");
	public static final ResourceKey<Aspect> ALCHEMY = key("alkimia");
	public static final ResourceKey<Aspect> FLUX = key("vitium");
	public static final ResourceKey<Aspect> DARKNESS = key("tenebrae");
	public static final ResourceKey<Aspect> ELDRITCH = key("alienis");
	public static final ResourceKey<Aspect> FLIGHT = key("volatus");
	public static final ResourceKey<Aspect> PLANT = key("herba");
	public static final ResourceKey<Aspect> TOOL = key("instrumentum");
	public static final ResourceKey<Aspect> CRAFT = key("fabrico");
	public static final ResourceKey<Aspect> MECHANISM = key("machina");
	public static final ResourceKey<Aspect> TRAP = key("vinculum");
	public static final ResourceKey<Aspect> SOUL = key("spiritus");
	public static final ResourceKey<Aspect> MIND = key("cognitio");
	public static final ResourceKey<Aspect> SENSES = key("sensus");
	public static final ResourceKey<Aspect> AVERSION = key("aversio");
	public static final ResourceKey<Aspect> PROTECT = key("praemunio");
	public static final ResourceKey<Aspect> DESIRE = key("desiderium");
	public static final ResourceKey<Aspect> UNDEAD = key("exanimis");
	public static final ResourceKey<Aspect> BEAST = key("bestia");
	public static final ResourceKey<Aspect> MAN = key("humanus");

	private Aspects() {
	}

	private static ResourceKey<Aspect> key(String tag) {
		return ResourceKey.create(ThaumcraftRegistryKeys.ASPECT, ThaumcraftRebornApi.id(tag));
	}
}
