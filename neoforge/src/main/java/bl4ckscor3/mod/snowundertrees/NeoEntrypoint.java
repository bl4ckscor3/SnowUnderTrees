package bl4ckscor3.mod.snowundertrees;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;

import bl4ckscor3.mod.snowundertrees.manager.SnowManager;
import bl4ckscor3.mod.snowundertrees.manager.VanillaManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(SnowUnderTrees.MODID)
@EventBusSubscriber
public class NeoEntrypoint implements Platform {
	public static final Supplier<MapCodec<SnowUnderTreesBiomeModifier>> SNOW_UNDER_TREES_BIOME_MODIFIER_CODEC = Suppliers.memoize(() -> SnowUnderTreesBiomeModifier.MAP_CODEC);
	private final Map<ResourceKey<? extends Registry<?>>, DeferredRegister<?>> registers = new HashMap<>();
	private final IEventBus modBus;

	public NeoEntrypoint(ModContainer modContainer, IEventBus modBus) {
		this.modBus = modBus;
		SnowUnderTrees.initialize(this);
		modContainer.registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
		register(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, SNOW_UNDER_TREES_BIOME_MODIFIER_CODEC, "snow_under_trees");
	}

	@Override
	public <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, Supplier<T> entry, String path) {
		@SuppressWarnings("unchecked")
		DeferredRegister<R> register = (DeferredRegister<R>) registers.computeIfAbsent(
			registry,
			_ -> {
				DeferredRegister<R> r = DeferredRegister.create(registry, SnowUnderTrees.MODID);

				r.register(modBus);
				return r;
			}
		);
		register.register(path, entry);
	}

	@Override
	public boolean isModLoaded(String modid) {
		return ModList.get().isLoaded(modid);
	}

	@SubscribeEvent
	public static void onLevelTickPre(LevelTickEvent.Pre event) {
		if (event.getLevel() instanceof ServerLevel level)
			LevelTickHandler.onLevelTickPre(level);
	}

	@SubscribeEvent
	public static void onLevelTickPost(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel level)
			LevelTickHandler.onLevelTickPost(level);
	}

	@Override
	public SnowManager getSnowRealMagicManager() {
		// FIXME: When Snow! Real Magic! is updated to 26.1.2 for NeoForge
		return new VanillaManager();
	}
}
