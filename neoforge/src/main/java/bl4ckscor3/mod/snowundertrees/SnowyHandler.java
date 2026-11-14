package bl4ckscor3.mod.snowundertrees;

import bl4ckscor3.mod.snowundertrees.mixin.SnowyNeoForgeAccessor;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public class SnowyHandler {
	public static boolean isActive(Holder<Biome> biome) {
		return SnowyNeoForgeAccessor.callCanModify(biome);
	}
}
