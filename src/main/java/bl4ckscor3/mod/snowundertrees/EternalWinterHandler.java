package bl4ckscor3.mod.snowundertrees;

import java.util.function.Predicate;

import ichttt.mods.eternalwinter.EWConfig;
import net.minecraft.resources.ResourceLocation;

public class EternalWinterHandler {
	public static boolean isActive(ResourceLocation registryName) {
		Predicate<String> checker = s -> s.equals(registryName.getPath()) || s.equals(registryName.toString());

		return switch (EWConfig.GENERAL.listMode.get()) {
			case BLACKLIST -> !EWConfig.GENERAL.biomeList.get().stream().anyMatch(checker);
			case WHITELIST -> !EWConfig.GENERAL.biomeList.get().stream().noneMatch(checker);
		};
	}
}
