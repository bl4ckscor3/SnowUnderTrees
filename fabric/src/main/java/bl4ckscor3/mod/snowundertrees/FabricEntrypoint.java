package bl4ckscor3.mod.snowundertrees;

import java.util.Optional;
import java.util.function.Supplier;

import bl4ckscor3.mod.snowundertrees.manager.FabricSnowRealMagicManager;
import bl4ckscor3.mod.snowundertrees.manager.SnowManager;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.fml.config.ModConfig;

public class FabricEntrypoint implements ModInitializer, Platform {
	@Override
	public void onInitialize() {
		SnowUnderTrees.initialize(this);
		ConfigRegistry.INSTANCE.register(SnowUnderTrees.MODID, ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
		ServerTickEvents.START_LEVEL_TICK.register(LevelTickHandler::onLevelTickPre);
		ServerTickEvents.END_LEVEL_TICK.register(LevelTickHandler::onLevelTickPost);
		BiomeModifications.create(SnowUnderTrees.id("snow_under_trees"))
			.add(ModificationPhase.ADDITIONS,
				ctx -> SnowUnderTrees.shouldAddToBiome(ctx.getBiomeHolder(), ctx.getBiome().hasPrecipitation(), ctx.getBiome().getBaseTemperature()),
				ctx -> ctx.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, ResourceKey.create(Registries.PLACED_FEATURE, SnowUnderTrees.id("snow_under_trees")))
			);
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public <R, T extends R> void register(ResourceKey<? extends Registry<R>> registryKey, Supplier<T> entry, String path) {
		Optional<Holder.Reference<R>> registry = BuiltInRegistries.REGISTRY.get((ResourceKey) registryKey);

		if (registry.isEmpty()) {
			throw new IllegalArgumentException("Couldn't find registry " + registryKey);
		}

		Registry.register((Registry<R>) registry.get().value(), SnowUnderTrees.id(path), entry.get());
	}

	@Override
	public boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	@Override
	public SnowManager getSnowRealMagicManager() {
		return new FabricSnowRealMagicManager();
	}
}
