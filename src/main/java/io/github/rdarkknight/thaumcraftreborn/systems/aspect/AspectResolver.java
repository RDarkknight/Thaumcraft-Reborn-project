package io.github.rdarkknight.thaumcraftreborn.systems.aspect;

import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectBonusProvider;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLimiter;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AspectResolver {
	private AspectResolver() {
	}

	public static AspectList resolve(
			Level level,
			ItemStack stack,
			AspectList base,
			List<AspectBonusProvider> providers,
			AspectLimiter limiter
	) {
		Objects.requireNonNull(level);
		Objects.requireNonNull(stack);
		Objects.requireNonNull(base);
		Objects.requireNonNull(providers);
		Objects.requireNonNull(limiter);
		if (providers.isEmpty() && limiter == AspectLimiter.IDENTITY) {
			return base;
		}

		AspectList resolved = base;
		for (AspectBonusProvider provider : providers) {
			resolved = Objects.requireNonNull(provider.apply(level, stack, resolved));
		}
		return Objects.requireNonNull(limiter.limit(resolved));
	}
}
