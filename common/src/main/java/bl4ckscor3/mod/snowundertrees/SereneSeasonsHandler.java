package bl4ckscor3.mod.snowundertrees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.init.ModConfig;
import sereneseasons.init.ModTags;
import sereneseasons.season.SeasonHooks;

public class SereneSeasonsHandler {
	public static void tryMeltSnowUnderTrees(ServerLevel level) {
		Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		SeasonsConfig.SeasonProperties seasonProperties = ModConfig.seasons.getSeasonProperties(subSeason);
		float meltRandomness = seasonProperties.meltChance() / 100.0F;
		int rolls = seasonProperties.meltRolls();

		if (rolls > 0 && meltRandomness > 0.0F && generateSnowAndIce() && ModConfig.seasons.isDimensionWhitelisted(level.dimension())) {
			SnowUnderTrees.runForChunks(level, chunk -> {
				for (int i = 0; i < rolls; i++) {
					if (level.getRandom().nextFloat() < meltRandomness) {
						ChunkPos chunkPos = chunk.getPos();
						int chunkX = chunkPos.getMinBlockX();
						int chunkZ = chunkPos.getMinBlockZ();
						BlockPos randomPos = level.getBlockRandomPos(chunkX, 0, chunkZ, 15);

						if (level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos).below()).is(BlockTags.LEAVES)) {
							BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);
							Holder<Biome> biomeHolder = level.getBiome(pos);
							boolean biomeDisabled = SnowUnderTrees.isBiomeDisabled(biomeHolder) || biomeHolder.is(ModTags.Biomes.BLACKLISTED_BIOMES);

							if (!biomeDisabled && SnowUnderTrees.isSnow(level, pos) && SeasonHooks.warmEnoughToRainSeasonal(level, biomeHolder, pos, level.getSeaLevel())) {
								BlockState stateNow = level.getBlockState(pos);
								BlockState stateAfter = SnowUnderTrees.getStateAfterMelting(stateNow, level, pos);

								if (stateNow != stateAfter) {
									BlockPos downPos = pos.below();
									BlockState below = level.getBlockState(downPos);

									level.setBlockAndUpdate(pos, stateAfter);

									if (below.hasProperty(BlockStateProperties.SNOWY))
										level.setBlock(downPos, below.setValue(BlockStateProperties.SNOWY, false), 2);
								}
							}
						}
					}
				}
			});
		}
	}

	public static boolean coldEnoughToSnow(WorldGenLevel level, Holder<Biome> biome, BlockPos pos, int seaLevel) {
		return SeasonHooks.coldEnoughToSnowSeasonal(level, biome, pos, seaLevel);
	}

	public static boolean generateSnowAndIce() {
		return ModConfig.seasons.generateSnowAndIce;
	}
}
