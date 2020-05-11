package bl4ckscor3.mod.snowundertrees;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
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
			ServerWorld world = (ServerWorld)event.world;

			world.getChunkProvider().chunkManager.getLoadedChunksIterable().forEach(chunkHolder -> {
				Optional<Chunk> optional = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();

				if(optional.isPresent())
				{
					int meltRandomness;

					switch(subSeason)
					{
						case EARLY_SPRING: meltRandomness = 16; break;
						case MID_SPRING: meltRandomness = 12; break;
						case LATE_SPRING: meltRandomness = 8; break;
						default: meltRandomness = 4; break;
					}

					if(world.rand.nextInt(meltRandomness) == 0)
					{
						Chunk chunk = optional.get();
						ChunkPos chunkPos = chunk.getPos();
						int chunkX = chunkPos.getXStart();
						int chunkY = chunkPos.getZStart();
						BlockPos randomPos = world.getBlockRandomPos(chunkX, 0, chunkY, 15);
						Biome biome = world.getBiome(randomPos);
						boolean biomeDisabled = Configuration.CONFIG.filteredBiomes.get().contains(biome.getRegistryName().toString());

						if(!biomeDisabled && world.getBlockState(world.getHeight(Heightmap.Type.MOTION_BLOCKING, randomPos).down()).getBlock() instanceof LeavesBlock)
						{
							BlockPos pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, randomPos);
							BlockState state = world.getBlockState(pos);

							if(state.getBlock() == Blocks.SNOW && SeasonHooks.getBiomeTemperature(world, biome, pos) >= 0.15F)
							{
								BlockPos downPos = pos.down();
								BlockState below = world.getBlockState(downPos);

								world.setBlockState(pos, Blocks.AIR.getDefaultState());

								if(below.has(SnowyDirtBlock.SNOWY))
									world.setBlockState(downPos, below.with(SnowyDirtBlock.SNOWY, false), 2);
							}
						}
					}
				}
			});
		}
	}
}
