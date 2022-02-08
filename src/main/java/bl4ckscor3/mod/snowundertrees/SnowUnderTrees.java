package bl4ckscor3.mod.snowundertrees;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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
	public static final Feature<NoneFeatureConfiguration> SNOW_UNDER_TREES_FEATURE = (Feature<NoneFeatureConfiguration>)new SnowUnderTreesFeature(NoneFeatureConfiguration.CODEC).setRegistryName("snow_under_trees");
	public static final ConfiguredFeature<?, ?> CONFIGURED_SNOW_UNDER_TREES = SNOW_UNDER_TREES_FEATURE.configured(FeatureConfiguration.NONE);
	public static final PlacedFeature SNOW_UNDER_TREES = CONFIGURED_SNOW_UNDER_TREES.placed(BiomeFilter.biome());
	private static List<ResourceLocation> biomesToAddTo = new ArrayList<>();
	private static boolean isSereneSeasonsLoaded;
	private static BiFunction<WorldGenLevel,BlockPos,Boolean> snowPlaceFunction;
	private static BiFunction<WorldGenLevel,BlockPos,Boolean> temperatureCheck;

	public SnowUnderTrees()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
		MinecraftForge.EVENT_BUS.addListener(SnowUnderTrees::onBiomeLoading);
		isSereneSeasonsLoaded = ModList.get().isLoaded("sereneseasons");

		if(ModList.get().isLoaded("snowrealmagic")) {
			snowPlaceFunction = (level, pos) -> SnowRealMagicHandler.placeSnow(level, pos);}
		else
		{
			snowPlaceFunction = (level, pos) -> {
				if(canSnow(level, pos))
				{
					BlockState state = level.getBlockState(pos);

					if(state.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, pos))
					{
						level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
						return true;
					}
				}

				return false;
			};
		}

		if(isSereneSeasonsLoaded) {
			temperatureCheck = (level, pos) -> !SereneSeasonsHandler.warmEnoughToRain(level, level.getBiome(pos), pos);}
		else
			temperatureCheck = (level, pos) -> !level.getBiome(pos).warmEnoughToRain(pos);
	}

	@SubscribeEvent
	public static void onRegisterFeature(RegistryEvent.Register<Feature<?>> event)
	{
		event.getRegistry().register(SNOW_UNDER_TREES_FEATURE);
		FeatureUtils.register("snowundertrees:snow_under_trees", CONFIGURED_SNOW_UNDER_TREES);
		PlacementUtils.register("snowundertrees:snow_under_trees", SNOW_UNDER_TREES);
	}

	public static void onBiomeLoading(BiomeLoadingEvent event)
	{
		if(Configuration.CONFIG.enableBiomeFeature.get())
		{
			if((event.getClimate().precipitation == Precipitation.SNOW || event.getClimate().temperature < 0.15F || biomesToAddTo.contains(event.getName())) && !Configuration.CONFIG.filteredBiomes.get().contains(event.getName().toString()))
				event.getGeneration().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal(), () -> SNOW_UNDER_TREES);
		}
	}

	public static void addSnowUnderTrees(Biome biome)
	{
		if(!biomesToAddTo.contains(biome.getRegistryName()))
			biomesToAddTo.add(biome.getRegistryName());
	}

	public static boolean placeSnow(WorldGenLevel level, BlockPos pos)
	{
		return snowPlaceFunction.apply(level, pos);
	}

	public static boolean canSnow(WorldGenLevel level, BlockPos pos)
	{
		if(temperatureCheck.apply(level, pos) && isInBuildRangeAndDarkEnough(level, pos))
		{
			BlockPos posBelow = pos.below();
			BlockState stateBelow = level.getBlockState(posBelow);

			return stateBelow.isFaceSturdy(level, posBelow, Direction.UP);
		}

		return false;
	}

	private static final boolean isInBuildRangeAndDarkEnough(WorldGenLevel level, BlockPos pos)
	{
		return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight() && level.getBrightness(LightLayer.BLOCK, pos) < 10;
	}

	public static boolean isSereneSeasonsLoaded()
	{
		return isSereneSeasonsLoaded;
	}
}
