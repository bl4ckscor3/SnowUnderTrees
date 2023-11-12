package bl4ckscor3.mod.snowundertrees;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import bl4ckscor3.mod.snowundertrees.manager.SnowManager;
import bl4ckscor3.mod.snowundertrees.manager.SnowRealMagicManager;
import bl4ckscor3.mod.snowundertrees.manager.VanillaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@Mod(SnowUnderTrees.MODID)
public class SnowUnderTrees {
	public static final String MODID = "snowundertrees";
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
	public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);
	public static final RegistryObject<SnowUnderTreesFeature> SNOW_UNDER_TREES_FEATURE = FEATURES.register("snow_under_trees", () -> new SnowUnderTreesFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Codec<SnowUnderTreesBiomeModifier>> SNOW_UNDER_TREES_BIOME_MODIFIER_CODEC = BIOME_MODIFIER_SERIALIZERS.register("snow_under_trees", () -> RecordCodecBuilder.create(builder -> builder.group(PlacedFeature.CODEC.fieldOf("feature").forGetter(SnowUnderTreesBiomeModifier::snowUnderTreesFeature)).apply(builder, SnowUnderTreesBiomeModifier::new)));
	public static final RandomSource RANDOM = RandomSource.create();
	public static List<ResourceLocation> biomesToAddTo = new ArrayList<>();
	public static SnowManager snowManager;
	private static boolean isSereneSeasonsLoaded, isEternalWinterLoaded;
	private static BiFunction<WorldGenLevel, BlockPos, Boolean> temperatureCheck;

	public SnowUnderTrees() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
		FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
		BIOME_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
		isSereneSeasonsLoaded = ModList.get().isLoaded("sereneseasons");
		isEternalWinterLoaded = ModList.get().isLoaded("eternalwinter");

		if (ModList.get().isLoaded("snowrealmagic"))
			snowManager = new SnowRealMagicManager();
		else
			snowManager = new VanillaManager();

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
		return snowManager.placeSnow(level, pos);
	}

	public static boolean canSnow(WorldGenLevel level, BlockPos pos) {
		if (level.getBiome(pos).get().getPrecipitationAt(pos) != Precipitation.SNOW)
			return false;

		BlockState stateAtPos = level.getBlockState(pos);

		if (!stateAtPos.canBeReplaced())
			return false;

		if (temperatureCheck.apply(level, pos) && isInBuildRangeAndDarkEnough(level, pos)) {
			BlockPos posBelow = pos.below();
			BlockState stateBelow = level.getBlockState(posBelow);

			return stateBelow.isFaceSturdy(level, posBelow, Direction.UP);
		}

		return false;
	}

	public static boolean isSnow(WorldGenLevel level, BlockPos pos) {
		return snowManager.isSnow(level, pos);
	}

	public static BlockState getStateAfterMelting(BlockState stateNow, WorldGenLevel level, BlockPos pos) {
		return snowManager.getStateAfterMelting(stateNow, level, pos);
	}

	private static final boolean isInBuildRangeAndDarkEnough(WorldGenLevel level, BlockPos pos) {
		return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight() && level.getBrightness(LightLayer.BLOCK, pos) < 10;
	}

	public static boolean isSereneSeasonsLoaded() {
		return isSereneSeasonsLoaded;
	}

	public static boolean isEternalWinterLoaded() {
		return isEternalWinterLoaded;
	}
}
