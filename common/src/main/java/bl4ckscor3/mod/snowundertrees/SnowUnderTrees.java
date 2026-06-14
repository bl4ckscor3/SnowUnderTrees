package bl4ckscor3.mod.snowundertrees;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;

import bl4ckscor3.mod.snowundertrees.manager.SnowManager;
import bl4ckscor3.mod.snowundertrees.manager.VanillaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowUnderTrees {
	public static final String MODID = "snowundertrees";
	public static final Supplier<SnowUnderTreesFeature> SNOW_UNDER_TREES_FEATURE = Suppliers.memoize(() -> new SnowUnderTreesFeature(NoneFeatureConfiguration.CODEC));
	private static Platform platform;
	public static final RandomSource RANDOM = RandomSource.create();
	private static List<Identifier> biomesToAddTo = new ArrayList<>();
	private static SnowManager snowManager;
	private static ChunkRunner chunkRunner;
	private static boolean isSereneSeasonsLoaded, isDynamicTreesLoaded;
	private static BiFunction<WorldGenLevel, BlockPos, Boolean> temperatureCheck;

	public synchronized static void initialize(Platform platform) {
		if (SnowUnderTrees.platform != null) {
			throw new IllegalArgumentException(MODID + " platform has already been initialized");
		}

		SnowUnderTrees.platform = platform;
		platform.register(Registries.FEATURE, SNOW_UNDER_TREES_FEATURE, "snow_under_trees");

		isSereneSeasonsLoaded = platform.isModLoaded("sereneseasons");
		isDynamicTreesLoaded = platform.isModLoaded("dynamictrees");

		if (platform.isModLoaded("snowrealmagic"))
			snowManager = platform.getSnowRealMagicManager();
		else
			snowManager = new VanillaManager();

		if (isSereneSeasonsLoaded)
			temperatureCheck = (level, pos) -> SereneSeasonsHandler.coldEnoughToSnow(level, level.getBiome(pos), pos, level.getSeaLevel());
		else
			temperatureCheck = (level, pos) -> !level.getBiome(pos).value().warmEnoughToRain(pos, level.getSeaLevel());

		if (platform.isModLoaded("moonrise"))
			chunkRunner = MoonriseCompat::chunkRunner;
		else
			chunkRunner = (level, action) -> Iterables.unmodifiableIterable(level.getChunkSource().chunkMap.visibleChunkMap.values()).forEach(chunkHolder -> chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).ifSuccess(action::accept));

	}

	public static void addSnowUnderTrees(Identifier biomeName) {
		if (!biomesToAddTo.contains(biomeName))
			biomesToAddTo.add(biomeName);
	}

	public static boolean placeSnow(WorldGenLevel level, BlockPos pos) {
		return snowManager.placeSnow(level, pos);
	}

	public static boolean canSnow(WorldGenLevel level, BlockPos pos) {
		Holder<Biome> biome = level.getBiome(pos);

		if (biome.value().getPrecipitationAt(pos, level.getSeaLevel()) == Biome.Precipitation.SNOW || isSereneSeasonsLoaded && SereneSeasonsHandler.coldEnoughToSnow(level, biome, pos, level.getSeaLevel())) {
			BlockState stateAtPos = level.getBlockState(pos);

			if (!snowManager.canBeReplaced(stateAtPos))
				return false;

			if (temperatureCheck.apply(level, pos) && isInBuildRangeAndDarkEnough(level, pos)) {
				BlockPos posBelow = pos.below();
				BlockState stateBelow = level.getBlockState(posBelow);

				return stateBelow.isFaceSturdy(level, posBelow, Direction.UP);
			}
		}

		return false;
	}

	public static boolean isSnow(WorldGenLevel level, BlockPos pos) {
		return snowManager.isSnow(level, pos);
	}

	public static BlockState getStateAfterMelting(BlockState stateNow, WorldGenLevel level, BlockPos pos) {
		return snowManager.getStateAfterMelting(stateNow, level, pos);
	}

	private static boolean isInBuildRangeAndDarkEnough(WorldGenLevel level, BlockPos pos) {
		return pos.getY() >= level.getMinY() && pos.getY() <= level.getMaxY() && level.getBrightness(LightLayer.BLOCK, pos) < 10;
	}

	public static boolean isSereneSeasonsLoaded() {
		return isSereneSeasonsLoaded;
	}

	public static boolean isDynamicTreesLoaded() {
		return isDynamicTreesLoaded;
	}

	public static void runForChunks(ServerLevel level, Consumer<LevelChunk> action) {
		chunkRunner.run(level, chunk -> {
			if (level.shouldTickBlocksAt(chunk.getPos().pack()))
				action.accept(chunk);
		});
	}

	public static List<Identifier> biomesToAddTo() {
		return biomesToAddTo;
	}

	public static boolean isBiomeDisabled(Holder<Biome> biomeHolder) {
		return biomeHolder.unwrapKey().map(key -> Configuration.CONFIG.filteredBiomes.get().contains(key.identifier().toString())).orElse(false);
	}

	public static boolean shouldAddToBiome(Holder<Biome> biome, boolean hasPrecipitation, float temperature) {
		if (!Configuration.CONFIG.enableBiomeFeature.get() || isFiltered(biome))
			return false;

		return isPrecipitationSnow(hasPrecipitation, temperature) || isManuallyAdded(biome);
	}

	public static boolean isPrecipitationSnow(boolean hasPrecipitation, float temperature) {
		return hasPrecipitation && temperature < 0.15F;
	}

	public static boolean isManuallyAdded(Holder<Biome> biome) {
		return SnowUnderTrees.biomesToAddTo().stream().anyMatch(biome::is);
	}

	public static boolean isFiltered(Holder<Biome> biome) {
		return Configuration.CONFIG.filteredBiomes.get()
			.stream()
			.map(Identifier::parse)
			.anyMatch(biome::is);
	}

	@FunctionalInterface
	private interface ChunkRunner {
		void run(ServerLevel level, Consumer<LevelChunk> action);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MODID, path);
	}

	public static Platform platform() {
		return platform;
	}
}
