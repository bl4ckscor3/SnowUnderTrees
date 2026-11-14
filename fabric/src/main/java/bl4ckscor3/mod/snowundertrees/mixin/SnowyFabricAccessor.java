package bl4ckscor3.mod.snowundertrees.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import dev.mlow.mods.snowy.SnowyFabric;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;

@Mixin(SnowyFabric.class)
public interface SnowyFabricAccessor {
	@Invoker
	static boolean callCanModify(BiomeSelectionContext biome) {
		throw new UnsupportedOperationException("Call from Mixin");
	}
}
