package bl4ckscor3.mod.snowundertrees;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.models.blockstates.PropertyDispatch.TriFunction;
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
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ObjectHolder;

@Mod(SnowUnderTrees.MODID)
@EventBusSubscriber(modid=SnowUnderTrees.MODID, bus=Bus.MOD)
public class SnowUnderTrees
{
	public static final String MODID = "snowundertrees";
	@ObjectHolder(MODID + ":snow_under_trees")
	public static final Feature<NoneFeatureConfiguration> SNOW_UNDER_TREES_FEATURE = null;
	public static Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> snowUnderTreesConfiguredFeature;
	public static Holder<PlacedFeature> snowUnderTreesPlacedFeature;
	private static List<ResourceLocation> biomesToAddTo = new ArrayList<>();
	private static boolean isSereneSeasonsLoaded;
	private static boolean isDynamicTreesLoaded;
	private static BiFunction<WorldGenLevel,BlockPos,Boolean> snowPlaceFunction;
	private static BiFunction<WorldGenLevel,BlockPos,Boolean> temperatureCheck;
	private static BiFunction<WorldGenLevel,BlockPos,Boolean> isSnowCheck;
	private static TriFunction<BlockState,WorldGenLevel,BlockPos,BlockState> stateAfterMeltingGetter;

	public SnowUnderTrees()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
		MinecraftForge.EVENT_BUS.addListener(SnowUnderTrees::onBiomeLoading);
		isSereneSeasonsLoaded = ModList.get().isLoaded("sereneseasons");
		isDynamicTreesLoaded = ModList.get().isLoaded("dynamictrees");

		if(ModList.get().isLoaded("snowrealmagic"))
		{
			snowPlaceFunction = (level, pos) -> SnowRealMagicHandler.placeSnow(level, pos);
			isSnowCheck = (level, pos) -> SnowRealMagicHandler.isSnow(level, pos);
			stateAfterMeltingGetter = (stateNow, level, pos) -> SnowRealMagicHandler.getStateAfterMelting(stateNow, level, pos);
		}
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
			isSnowCheck = (level, pos) -> level.getBlockState(pos).getBlock() == Blocks.SNOW;
			stateAfterMeltingGetter = (stateNow, level, pos) -> Blocks.AIR.defaultBlockState();
		}

		if(isSereneSeasonsLoaded) {
			temperatureCheck = (level, pos) -> !SereneSeasonsHandler.warmEnoughToRain(level, level.getBiome(pos), pos);}
		else
			temperatureCheck = (level, pos) -> !level.getBiome(pos).value().warmEnoughToRain(pos);
	}

	@SubscribeEvent
	public static void onRegisterFeature(RegistryEvent.Register<Feature<?>> event)
	{
		event.getRegistry().register(new SnowUnderTreesFeature(NoneFeatureConfiguration.CODEC).setRegistryName("snow_under_trees"));
	}

	@SubscribeEvent
	public static void onFMLCommonSetup(FMLCommonSetupEvent event)
	{
		event.enqueueWork(() -> {
			snowUnderTreesConfiguredFeature = FeatureUtils.register("snowundertrees:snow_under_trees", SNOW_UNDER_TREES_FEATURE);
			snowUnderTreesPlacedFeature = PlacementUtils.register("snowundertrees:snow_under_trees", snowUnderTreesConfiguredFeature, BiomeFilter.biome());
		});
	}

	public static void onBiomeLoading(BiomeLoadingEvent event)
	{
		if(Configuration.CONFIG.enableBiomeFeature.get())
		{
			if((event.getClimate().precipitation == Precipitation.SNOW || event.getClimate().temperature < 0.15F || biomesToAddTo.contains(event.getName())) && !Configuration.CONFIG.filteredBiomes.get().contains(event.getName().toString()))
				event.getGeneration().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal(), snowUnderTreesPlacedFeature);
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

	public static boolean isSnow(WorldGenLevel level, BlockPos pos)
	{
		return isSnowCheck.apply(level, pos);
	}

	public static BlockState getStateAfterMelting(BlockState stateNow, WorldGenLevel level, BlockPos pos)
	{
		return stateAfterMeltingGetter.apply(stateNow, level, pos);
	}

	private static final boolean isInBuildRangeAndDarkEnough(WorldGenLevel level, BlockPos pos)
	{
		return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight() && level.getBrightness(LightLayer.BLOCK, pos) < 10;
	}

	public static boolean isSereneSeasonsLoaded()
	{
		return isSereneSeasonsLoaded;
	}

	public static boolean isDynamicTreesLoaded()
	{
		return isDynamicTreesLoaded;
	}
}
