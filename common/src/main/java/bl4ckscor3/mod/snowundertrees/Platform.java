package bl4ckscor3.mod.snowundertrees;

import java.util.function.Supplier;

import bl4ckscor3.mod.snowundertrees.manager.SnowManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface Platform {
	<R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, Supplier<T> entry, String path);

	boolean isModLoaded(String modid);

	SnowManager getSnowRealMagicManager();
}
