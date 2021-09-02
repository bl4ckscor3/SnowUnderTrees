package bl4ckscor3.mod.snowundertrees;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
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
	public static final Feature<NoFeatureConfig> SNOW_UNDER_TREES_FEATURE = (Feature<NoFeatureConfig>)new SnowUnderTreesFeature(NoFeatureConfig.CODEC).setRegistryName("snow_under_trees");
	public static final ConfiguredFeature<?, ?> SNOW_UNDER_TREES = SNOW_UNDER_TREES_FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG));
	private static List<ResourceLocation> biomesToAddTo = new ArrayList<>();
	private static boolean sereneSeasonsLoaded;
	private static BiFunction<IWorld,BlockPos,Boolean> snowPlaceFunction;

	public SnowUnderTrees()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
		MinecraftForge.EVENT_BUS.addListener(SnowUnderTrees::onBiomeLoading);
		sereneSeasonsLoaded = ModList.get().isLoaded("sereneseasons");

		if(ModList.get().isLoaded("snowrealmagic"))
		{
			snowPlaceFunction = (world, pos) -> SnowRealMagicHandler.placeSnow(world, pos);
		}
		else
		{
			snowPlaceFunction = (world, pos) -> {
				if(world.getBiome(pos).doesSnowGenerate(world, pos))
				{
					world.setBlockState(pos, Blocks.SNOW.getDefaultState(), 2);
					return true;
				}
				else
					return false;
			};
		}
	}

	@SubscribeEvent
	public static void onRegisterFeature(RegistryEvent.Register<Feature<?>> event)
	{
		event.getRegistry().register(SNOW_UNDER_TREES_FEATURE);
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, "snowundertrees:snow_under_trees", SNOW_UNDER_TREES);
	}

	public static void onBiomeLoading(BiomeLoadingEvent event)
	{
		if(Configuration.CONFIG.enableBiomeFeature.get())
		{
			if((event.getClimate().precipitation == RainType.SNOW || biomesToAddTo.contains(event.getName())) && !Configuration.CONFIG.filteredBiomes.get().contains(event.getName().toString()))
				event.getGeneration().withFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION.ordinal(), () -> SNOW_UNDER_TREES);
		}
	}

	public static void addSnowUnderTrees(Biome biome)
	{
		if(!biomesToAddTo.contains(biome.getRegistryName()))
			biomesToAddTo.add(biome.getRegistryName());
	}

	public static boolean isSereneSeasonsLoaded()
	{
		return sereneSeasonsLoaded;
	}

	public static boolean placeSnow(IWorld world, BlockPos pos)
	{
		return snowPlaceFunction.apply(world, pos);
	}
}
