package bl4ckscor3.mod.snowundertrees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = SnowUnderTrees.MODID)
public class LevelTickHandler {
	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Pre event) {
		if (!event.getLevel().isClientSide) {
			ServerLevel level = (ServerLevel) event.getLevel();

			if (level.isRaining() && Configuration.CONFIG.enableWhenSnowing.get()) {
				if (SnowUnderTrees.isSereneSeasonsLoaded() && !SereneSeasonsHandler.generateSnowAndIce())
					return;

				level.getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
					LevelChunk chunk = chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).orElse(null);

					if (chunk != null && SnowUnderTrees.RANDOM.nextInt(16) == 0) {
						ChunkPos chunkPos = chunk.getPos();
						int chunkX = chunkPos.getMinBlockX();
						int chunkY = chunkPos.getMinBlockZ();
						BlockPos randomPos = level.getBlockRandomPos(chunkX, 0, chunkY, 15);
						Biome biome = level.getBiome(randomPos).value();
						boolean biomeDisabled = Configuration.CONFIG.filteredBiomes.get().contains(level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome).toString());

						if (!biomeDisabled && level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos).below()).is(BlockTags.LEAVES)) {
							BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);

							if (SnowUnderTrees.placeSnow(level, pos)) {
								BlockPos posBelow = pos.below();
								BlockState stateBelow = level.getBlockState(posBelow);

								if (stateBelow.hasProperty(SnowyDirtBlock.SNOWY))
									level.setBlock(posBelow, stateBelow.setValue(SnowyDirtBlock.SNOWY, true), 2);
							}
						}
					}
				});
			}
		}
	}

	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Post event) {
		if (!event.getLevel().isClientSide && SnowUnderTrees.isSereneSeasonsLoaded())
			SereneSeasonsHandler.tryMeltSnowUnderTrees((ServerLevel) event.getLevel());
	}
}
