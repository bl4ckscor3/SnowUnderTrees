package bl4ckscor3.mod.snowundertrees;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ObjectHolder;

@Mod(SnowUnderTrees.MODID)
@EventBusSubscriber(modid=SnowUnderTrees.MODID, bus=Bus.MOD)
public class SnowUnderTrees
{
	public static final String MODID = "snowundertrees";
	@ObjectHolder(MODID + ":snow_under_trees")
	public static final Feature<NoneFeatureConfiguration> SNOW_UNDER_TREES_FEATURE = (Feature<NoneFeatureConfiguration>)new SnowUnderTreesFeature(NoneFeatureConfiguration.CODEC).setRegistryName("snow_under_trees");
	public static final ConfiguredFeature<?, ?> SNOW_UNDER_TREES = SNOW_UNDER_TREES_FEATURE.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
	private static List<ResourceLocation> biomesToAddTo = new ArrayList<>();

	public SnowUnderTrees()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
		MinecraftForge.EVENT_BUS.addListener(SnowUnderTrees::onBiomeLoading);
	}

	@SubscribeEvent
	public static void onRegisterFeature(RegistryEvent.Register<Feature<?>> event)
	{
		event.getRegistry().register(SNOW_UNDER_TREES_FEATURE);
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, "snowundertrees:snow_under_trees", SNOW_UNDER_TREES);
	}

	public static void onBiomeLoading(BiomeLoadingEvent event)
	{
		if(Configuration.CONFIG.enableBiomeFeature.get())
		{
			if((event.getClimate().precipitation == Precipitation.SNOW || biomesToAddTo.contains(event.getName())) && !Configuration.CONFIG.filteredBiomes.get().contains(event.getName().toString()))
				event.getGeneration().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal(), () -> SNOW_UNDER_TREES);
		}
	}

	public static void addSnowUnderTrees(Biome biome)
	{
		if(!biomesToAddTo.contains(biome.getRegistryName()))
			biomesToAddTo.add(biome.getRegistryName());
	}
}
