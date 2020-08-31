package bl4ckscor3.mod.snowundertrees;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@Mod(SnowUnderTrees.MODID)
@EventBusSubscriber(modid=SnowUnderTrees.MODID, bus=Bus.MOD)
public class SnowUnderTrees
{
	public static final String MODID = "snowundertrees";
	@ObjectHolder(MODID + ":snow_under_trees")
	public static final Feature<NoFeatureConfig> SNOW_UNDER_TREES_FEATURE = (Feature<NoFeatureConfig>)new SnowUnderTreesFeature(NoFeatureConfig.field_236558_a_).setRegistryName("snow_under_trees");
	public static final ConfiguredFeature<?, ?> SNOW_UNDER_TREES = SNOW_UNDER_TREES_FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG));
	private static List<Biome> biomesToAddTo = new ArrayList<>();

	public SnowUnderTrees()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
	}

	@SubscribeEvent
	public static void onRegisterFeature(RegistryEvent.Register<Feature<?>> event)
	{
		event.getRegistry().register(SNOW_UNDER_TREES_FEATURE);
		Registry.register(WorldGenRegistries.field_243653_e, "snowundertrees:snow_under_trees", SNOW_UNDER_TREES);
	}

	@SubscribeEvent
	public static void onFMLLoadComplete(FMLLoadCompleteEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		if(Configuration.CONFIG.enableBiomeFeature.get())
		{
			Field field = BiomeGenerationSettings.class.getDeclaredField("field_242484_f");

			field.setAccessible(true);

			for(Biome biome : ForgeRegistries.BIOMES)
			{
				if((biome.getPrecipitation() == RainType.SNOW || biomesToAddTo.contains(biome)) && !Configuration.CONFIG.filteredBiomes.get().contains(biome.getRegistryName().toString()))
				{
					List<List<Supplier<ConfiguredFeature<?,?>>>> list = (List<List<Supplier<ConfiguredFeature<?,?>>>>)field.get(biome.func_242440_e());

					list = list.stream().map(e -> new ArrayList<>(e)).collect(Collectors.toList());
					list.get(GenerationStage.Decoration.TOP_LAYER_MODIFICATION.ordinal()).add(() -> SNOW_UNDER_TREES);
					field.set(biome.func_242440_e(), list);
				}
			}
		}

		biomesToAddTo = null;
	}

	public static void addSnowUnderTrees(Biome biome)
	{
		if(!biomesToAddTo.contains(biome))
			biomesToAddTo.add(biome);
	}
}
