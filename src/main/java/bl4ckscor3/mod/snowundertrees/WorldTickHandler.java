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
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid=SnowUnderTrees.MODID)
public class WorldTickHandler
{
	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event)
	{
		if(event.phase == Phase.START && !event.world.isRemote && event.world.isRaining() && Configuration.CONFIG.enableWhenSnowing.get())
		{
			ServerWorld world = (ServerWorld)event.world;

			world.getChunkProvider().chunkManager.getLoadedChunksIterable().forEach(chunkHolder -> {
					Optional<Chunk> optional = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();

				if(optional.isPresent())
				{
					Chunk chunk = optional.get();
					ChunkPos chunkPos = chunk.getPos();
					int chunkX = chunkPos.getXStart();
					int chunkY = chunkPos.getZStart();

					if(world.rand.nextInt(16) == 0)
					{
						BlockPos randomPos = world.getBlockRandomPos(chunkX, 0, chunkY, 15);
							Biome biome = world.getBiome(randomPos);
						boolean biomeDisabled = Configuration.CONFIG.filteredBiomes.get().contains(biome.getRegistryName().toString());

						if(!biomeDisabled && world.getBlockState(world.getHeight(Heightmap.Type.MOTION_BLOCKING, randomPos).down()).getBlock() instanceof LeavesBlock)
						{
							BlockPos pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, randomPos);
							BlockState state = world.getBlockState(pos);

							if(biome.doesSnowGenerate(world, pos) && state.isAir(world, pos))
							{
								BlockPos downPos = pos.down();
								BlockState below = world.getBlockState(downPos);

								world.setBlockState(pos, Blocks.SNOW.getDefaultState());

								if(below.has(SnowyDirtBlock.SNOWY))
									world.setBlockState(downPos, below.with(SnowyDirtBlock.SNOWY, true), 2);
							}
						}
					}
				}
			});
		}
	}
}
