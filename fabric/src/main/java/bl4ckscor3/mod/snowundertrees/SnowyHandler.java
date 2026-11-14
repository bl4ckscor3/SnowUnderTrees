package bl4ckscor3.mod.snowundertrees;

import bl4ckscor3.mod.snowundertrees.mixin.SnowyFabricAccessor;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;

public class SnowyHandler {
	public static boolean isActive(BiomeSelectionContext ctx) {
		return SnowyFabricAccessor.callCanModify(ctx);
	}
}
