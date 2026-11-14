package bl4ckscor3.mod.snowundertrees.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import dev.mlow.mods.snowy.SnowyNeoForge;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

@Mixin(SnowyNeoForge.class)
public interface SnowyNeoForgeAccessor {
	@Invoker
	static boolean callCanModify(Holder<Biome> biome) {
		throw new UnsupportedOperationException("Call from Mixin");
	}
}
