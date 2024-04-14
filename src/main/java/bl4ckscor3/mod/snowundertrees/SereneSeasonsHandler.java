package bl4ckscor3.mod.snowundertrees;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.season.SeasonHooks;

public class SereneSeasonsHandler {
	public static void tryMeltSnowUnderTrees(LevelTickEvent event) {
		SubSeason subSeason = SeasonHelper.getSeasonState(event.level).getSubSeason();
		Season season = subSeason.getSeason();

		if (season != Season.WINTER) {
			ServerLevel level = (ServerLevel) event.level;

			level.getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
				Optional<LevelChunk> optional = chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();

				if (optional.isPresent()) {
					int meltRandomness = switch (subSeason) {
						case EARLY_SPRING -> 16;
						case MID_SPRING -> 12;
						case LATE_SPRING -> 8;
						default -> 4;
					};

					if (SnowUnderTrees.RANDOM.nextInt(meltRandomness) == 0) {
						LevelChunk chunk = optional.get();
						ChunkPos chunkPos = chunk.getPos();
						int chunkX = chunkPos.getMinBlockX();
						int chunkY = chunkPos.getMinBlockZ();
						BlockPos randomPos = level.getBlockRandomPos(chunkX, 0, chunkY, 15);
						Holder<Biome> biomeHolder = level.getBiome(randomPos);

						boolean biomeDisabled = Configuration.CONFIG.filteredBiomes.get().contains(biomeHolder.unwrapKey().get().location().toString());

						if (!biomeDisabled && level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos).below()).is(BlockTags.LEAVES)) {
							BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);

							if (SnowUnderTrees.isSnow(level, pos) && SeasonHooks.warmEnoughToRainSeasonal(level, biomeHolder, pos)) {
								BlockState stateNow = level.getBlockState(pos);
								BlockState stateAfter = SnowUnderTrees.getStateAfterMelting(stateNow, level, pos);

								if (stateNow != stateAfter) {
									BlockPos downPos = pos.below();
									BlockState below = level.getBlockState(downPos);

									level.setBlockAndUpdate(pos, stateAfter);

									if (below.hasProperty(SnowyDirtBlock.SNOWY))
										level.setBlock(downPos, below.setValue(SnowyDirtBlock.SNOWY, false), 2);
								}
							}
						}
					}
				}
			});
		}
	}

	public static boolean coldEnoughToSnow(WorldGenLevel level, Holder<Biome> biome, BlockPos pos) {
		return SeasonHooks.coldEnoughToSnowSeasonal(level, biome, pos);
	}

	public static boolean generateSnowAndIce() {
		return SeasonsConfig.generateSnowAndIce.get();
	}
}
