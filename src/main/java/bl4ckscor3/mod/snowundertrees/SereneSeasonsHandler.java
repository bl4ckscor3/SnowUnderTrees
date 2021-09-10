package bl4ckscor3.mod.snowundertrees;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.season.SeasonHooks;

public class SereneSeasonsHandler
{
	public static void tryMeltSnowUnderTrees(WorldTickEvent event)
	{
		SubSeason subSeason = SeasonHelper.getSeasonState(event.world).getSubSeason();
		Season season = subSeason.getSeason();

		if(season != Season.WINTER)
		{
			ServerLevel world = (ServerLevel)event.world;

			world.getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
				Optional<LevelChunk> optional = chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();

				if(optional.isPresent())
				{
					int meltRandomness = switch(subSeason) {
						case EARLY_SPRING -> 16;
						case MID_SPRING -> 12;
						case LATE_SPRING -> 8;
						default -> 4;
					};

					if(world.random.nextInt(meltRandomness) == 0)
					{
						LevelChunk chunk = optional.get();
						ChunkPos chunkPos = chunk.getPos();
						int chunkX = chunkPos.getMinBlockX();
						int chunkY = chunkPos.getMinBlockZ();
						BlockPos randomPos = world.getBlockRandomPos(chunkX, 0, chunkY, 15);
						Biome biome = world.getBiome(randomPos);
						boolean biomeDisabled = Configuration.CONFIG.filteredBiomes.get().contains(biome.getRegistryName().toString());

						if(!biomeDisabled && world.getBlockState(world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos).below()).getBlock() instanceof LeavesBlock)
						{
							BlockPos pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);
							BlockState state = world.getBlockState(pos);

							if(state.getBlock() == Blocks.SNOW && SeasonHooks.getBiomeTemperature(world, biome, pos) >= 0.15F)
							{
								BlockPos downPos = pos.below();
								BlockState below = world.getBlockState(downPos);

								world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

								if(below.hasProperty(SnowyDirtBlock.SNOWY))
									world.setBlock(downPos, below.setValue(SnowyDirtBlock.SNOWY, false), 2);
							}
						}
					}
				}
			});
		}
	}
}
