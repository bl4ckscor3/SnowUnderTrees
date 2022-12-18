package bl4ckscor3.mod.snowundertrees;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.PropertyDispatch.TriFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(SnowUnderTrees.MODID)
@EventBusSubscriber(modid = SnowUnderTrees.MODID, bus = Bus.MOD)
public class SnowUnderTrees {
	public static final String MODID = "snowundertrees";
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
	public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);
	public static final RegistryObject<SnowUnderTreesFeature> SNOW_UNDER_TREES_FEATURE = FEATURES.register("snow_under_trees", () -> new SnowUnderTreesFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Codec<SnowUnderTreesBiomeModifier>> SNOW_UNDER_TREES_BIOME_MODIFIER_CODEC = BIOME_MODIFIER_SERIALIZERS.register("snow_under_trees", () -> RecordCodecBuilder.create(builder -> builder.group(PlacedFeature.CODEC.fieldOf("feature").forGetter(SnowUnderTreesBiomeModifier::snowUnderTreesFeature)).apply(builder, SnowUnderTreesBiomeModifier::new)));
	public static final RandomSource RANDOM = RandomSource.create();
	public static List<ResourceLocation> biomesToAddTo = new ArrayList<>();
	private static boolean isSereneSeasonsLoaded;
	private static BiFunction<WorldGenLevel, BlockPos, Boolean> snowPlaceFunction;
	private static BiFunction<WorldGenLevel, BlockPos, Boolean> temperatureCheck;
	private static BiFunction<WorldGenLevel, BlockPos, Boolean> isSnowCheck;
	private static TriFunction<BlockState, WorldGenLevel, BlockPos, BlockState> stateAfterMeltingGetter;

	public SnowUnderTrees() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
		FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
		BIOME_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
		isSereneSeasonsLoaded = ModList.get().isLoaded("sereneseasons");

		if (ModList.get().isLoaded("snowrealmagic")) {
			snowPlaceFunction = (level, pos) -> SnowRealMagicHandler.placeSnow(level, pos);
			isSnowCheck = (level, pos) -> SnowRealMagicHandler.isSnow(level, pos);
			stateAfterMeltingGetter = (stateNow, level, pos) -> SnowRealMagicHandler.getStateAfterMelting(stateNow, level, pos);
		}
		else {
			snowPlaceFunction = (level, pos) -> {
				int accumulationHeight = level instanceof Level l ? l.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT) : 1;

				if (accumulationHeight > 0 && canSnow(level, pos)) {
					BlockState state = level.getBlockState(pos);

					if (state.is(Blocks.SNOW)) {
						int currentLayers = state.getValue(SnowLayerBlock.LAYERS);

						if (currentLayers < Math.min(accumulationHeight, 8)) {
							BlockState snowLayers = state.setValue(SnowLayerBlock.LAYERS, currentLayers + 1);

							Block.pushEntitiesUp(state, snowLayers, level, pos);
							level.setBlock(pos, snowLayers, 2);
							return true;
						}
					}
					else {
						level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
						return true;
					}
				}

				return false;
			};
			isSnowCheck = (level, pos) -> level.getBlockState(pos).getBlock() == Blocks.SNOW;
			stateAfterMeltingGetter = (stateNow, level, pos) -> Blocks.AIR.defaultBlockState();
		}

		if (isSereneSeasonsLoaded)
			temperatureCheck = (level, pos) -> !SereneSeasonsHandler.warmEnoughToRain(level, level.getBiome(pos), pos);
		else
			temperatureCheck = (level, pos) -> !level.getBiome(pos).value().warmEnoughToRain(pos);
	}

	public static void addSnowUnderTrees(Biome biome) {
		ResourceLocation biomeName = ForgeRegistries.BIOMES.getKey(biome);

		if (!biomesToAddTo.contains(biomeName))
			biomesToAddTo.add(biomeName);
	}

	public static boolean placeSnow(WorldGenLevel level, BlockPos pos) {
		return snowPlaceFunction.apply(level, pos);
	}

	public static boolean canSnow(WorldGenLevel level, BlockPos pos) {
		if (temperatureCheck.apply(level, pos) && isInBuildRangeAndDarkEnough(level, pos)) {
			BlockPos posBelow = pos.below();
			BlockState stateBelow = level.getBlockState(posBelow);

			return stateBelow.isFaceSturdy(level, posBelow, Direction.UP);
		}

		return false;
	}

	public static boolean isSnow(WorldGenLevel level, BlockPos pos) {
		return isSnowCheck.apply(level, pos);
	}

	public static BlockState getStateAfterMelting(BlockState stateNow, WorldGenLevel level, BlockPos pos) {
		return stateAfterMeltingGetter.apply(stateNow, level, pos);
	}

	private static final boolean isInBuildRangeAndDarkEnough(WorldGenLevel level, BlockPos pos) {
		return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight() && level.getBrightness(LightLayer.BLOCK, pos) < 10;
	}

	public static boolean isSereneSeasonsLoaded() {
		return isSereneSeasonsLoaded;
	}
}
