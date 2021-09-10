package bl4ckscor3.mod.snowundertrees;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
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
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid=SnowUnderTrees.MODID)
public class WorldTickHandler
{
	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event)
	{
		if(event.side == LogicalSide.SERVER)
		{
			if(event.phase == Phase.START && event.world.isRaining() && Configuration.CONFIG.enableWhenSnowing.get())
			{
				ServerLevel world = (ServerLevel)event.world;

				world.getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
					Optional<LevelChunk> optional = chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();

					if(optional.isPresent() && world.random.nextInt(16) == 0)
					{
						LevelChunk chunk = optional.get();
						ChunkPos chunkPos = chunk.getPos();
						int chunkX = chunkPos.getMinBlockX();
						int chunkY = chunkPos.getMinBlockZ();
						BlockPos randomPos = world.getBlockRandomPos(chunkX, 0, chunkY, 15);
						Biome biome = world.getBiome(randomPos);
						boolean biomeDisabled = Configuration.CONFIG.filteredBiomes.get().contains(world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome).toString());

						if(!biomeDisabled && world.getBlockState(world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos).below()).getBlock() instanceof LeavesBlock)
						{
							BlockPos pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);
							BlockState state = world.getBlockState(pos);

							if(biome.shouldSnow(world, pos) && state.isAir())
							{
								BlockPos downPos = pos.below();
								BlockState stateBelow = world.getBlockState(downPos);

								if(stateBelow.isFaceSturdy(world, downPos, Direction.UP))
								{
									world.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());

									if(stateBelow.hasProperty(SnowyDirtBlock.SNOWY))
										world.setBlock(downPos, stateBelow.setValue(SnowyDirtBlock.SNOWY, true), 2);
								}
							}
						}
					}
				});
			}
			else if(event.phase == Phase.END && ModList.get().isLoaded("sereneseasons"))
				SereneSeasonsHandler.tryMeltSnowUnderTrees(event);
		}
	}
}
