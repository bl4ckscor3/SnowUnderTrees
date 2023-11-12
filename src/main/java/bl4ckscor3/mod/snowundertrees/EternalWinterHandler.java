package bl4ckscor3.mod.snowundertrees;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public class EternalWinterHandler {
	public static boolean isActive(Holder<Biome> biome) {
		//		ResourceLocation registryName = biome.unwrapKey().get().location();
		//		Predicate<String> checker = s -> s.equals(registryName.getPath()) || s.equals(registryName.toString());
		//
		//		return switch (EWConfig.GENERAL.listMode.get()) {
		//			case BLACKLIST -> !EWConfig.GENERAL.biomeList.get().stream().anyMatch(checker);
		//			case WHITELIST -> !EWConfig.GENERAL.biomeList.get().stream().noneMatch(checker);
		//		};
		return false;
	}
}
