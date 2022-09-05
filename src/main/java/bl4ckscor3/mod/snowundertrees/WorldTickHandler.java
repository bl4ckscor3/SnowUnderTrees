package bl4ckscor3.mod.snowundertrees;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
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
				ServerLevel level = (ServerLevel)event.world;

				level.getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
					Optional<LevelChunk> optional = chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();

					if(optional.isPresent() && level.random.nextInt(16) == 0)
					{
						LevelChunk chunk = optional.get();
						ChunkPos chunkPos = chunk.getPos();
						int chunkX = chunkPos.getMinBlockX();
						int chunkY = chunkPos.getMinBlockZ();
						BlockPos randomPos = level.getBlockRandomPos(chunkX, 0, chunkY, 15);
						Biome biome = level.getBiome(randomPos).value();
						boolean biomeDisabled = Configuration.CONFIG.filteredBiomes.get().contains(level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome).toString());

						if(!biomeDisabled && level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos).below()).is(BlockTags.LEAVES))
						{
							BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);

							if(SnowUnderTrees.isDynamicTreesLoaded())
								pos = DynamicTreesHandler.findGround(level, pos.mutable());

							if(SnowUnderTrees.placeSnow(level, pos))
							{
								BlockPos posBelow = pos.below();
								BlockState stateBelow = level.getBlockState(posBelow);

								if(stateBelow.hasProperty(SnowyDirtBlock.SNOWY))
									level.setBlock(posBelow, stateBelow.setValue(SnowyDirtBlock.SNOWY, true), 2);
							}
						}
					}
				});
			}
			else if(event.phase == Phase.END && SnowUnderTrees.isSereneSeasonsLoaded())
				SereneSeasonsHandler.tryMeltSnowUnderTrees(event);
		}
	}
}
