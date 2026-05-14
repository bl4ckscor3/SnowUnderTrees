package bl4ckscor3.mod.snowundertrees;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;

public class LevelTickHandler {
	public static void onLevelTickPre(ServerLevel level) {
		if (level.isRaining() && Configuration.CONFIG.enableWhenSnowing.get()) {
			if (SnowUnderTrees.isSereneSeasonsLoaded() && !SereneSeasonsHandler.generateSnowAndIce())
				return;

			int randomTickSpeed = level.getGameRules().get(GameRules.RANDOM_TICK_SPEED);

			SnowUnderTrees.runForChunks(level, chunk -> addSnowUnderTrees(level, chunk, randomTickSpeed));
		}
	}

	private static void addSnowUnderTrees(ServerLevel level, LevelChunk chunk, int randomTickSpeed) {
		ChunkPos chunkPos = chunk.getPos();
		int chunkX = chunkPos.getMinBlockX();
		int chunkZ = chunkPos.getMinBlockZ();

		for (int i = 0; i < randomTickSpeed; i++) {
			if (SnowUnderTrees.RANDOM.nextInt(48) == 0) {
				BlockPos randomPos = level.getBlockRandomPos(chunkX, 0, chunkZ, 15);

				if (level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos).below()).is(BlockTags.LEAVES)) {
					BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);

					if (!SnowUnderTrees.isBiomeDisabled(level.getBiome(pos)) && SnowUnderTrees.placeSnow(level, pos)) {
						BlockPos posBelow = pos.below();
						BlockState stateBelow = level.getBlockState(posBelow);

						if (stateBelow.hasProperty(BlockStateProperties.SNOWY))
							level.setBlock(posBelow, stateBelow.setValue(BlockStateProperties.SNOWY, true), 2);
					}
				}
			}
		}
	}

	public static void onLevelTickPost(ServerLevel level) {
		if (SnowUnderTrees.isSereneSeasonsLoaded())
			SereneSeasonsHandler.tryMeltSnowUnderTrees(level);
	}
}
