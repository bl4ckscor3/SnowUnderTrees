package bl4ckscor3.mod.snowundertrees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.init.ModConfig;
import sereneseasons.init.ModTags;
import sereneseasons.season.SeasonHooks;

public class SereneSeasonsHandler {
	public static void tryMeltSnowUnderTrees(LevelTickEvent event) {
		ServerLevel level = (ServerLevel) event.level;
		SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		SeasonsConfig.SeasonProperties seasonProperties = ModConfig.seasons.getSeasonProperties(subSeason);
		float meltRandomness = seasonProperties.meltChance() / 100.0F;
		int rolls = seasonProperties.meltRolls();

		if (rolls > 0 && meltRandomness > 0.0F && generateSnowAndIce() && ModConfig.seasons.isDimensionWhitelisted(level.dimension())) {
			SnowUnderTrees.runForChunks(level, chunk -> {
				for (int i = 0; i < rolls; i++) {
					if (level.random.nextFloat() < meltRandomness) {
						ChunkPos chunkPos = chunk.getPos();
						int chunkX = chunkPos.getMinBlockX();
						int chunkZ = chunkPos.getMinBlockZ();
						BlockPos randomPos = level.getBlockRandomPos(chunkX, 0, chunkZ, 15);

						if (level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos).below()).is(BlockTags.LEAVES)) {
							BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);

							if (SnowUnderTrees.isDynamicTreesLoaded()) {
								pos = DynamicTreesHandler.findGround(level, pos.mutable());

								if (pos == null)
									return;
								
								if (level.getBlockState(pos).isAir()) //need the snow block, not the air above it
									pos = pos.below();
							}

							Holder<Biome> biomeHolder = level.getBiome(pos);
							boolean biomeDisabled = Configuration.CONFIG.filteredBiomes.get().contains(biomeHolder.unwrapKey().get().location().toString()) || biomeHolder.is(ModTags.Biomes.BLACKLISTED_BIOMES);

							if (!biomeDisabled && SnowUnderTrees.isSnow(level, pos) && SeasonHooks.warmEnoughToRainSeasonal(level, biomeHolder, pos)) {
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
		return ModConfig.seasons.generateSnowAndIce;
	}
}
