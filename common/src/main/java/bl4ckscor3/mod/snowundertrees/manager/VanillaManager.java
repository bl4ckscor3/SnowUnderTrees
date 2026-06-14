package bl4ckscor3.mod.snowundertrees.manager;

import bl4ckscor3.mod.snowundertrees.SnowUnderTrees;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gamerules.GameRules;

public class VanillaManager implements SnowManager {
	@Override
	public boolean placeSnow(WorldGenLevel level, BlockPos pos) {
		int accumulationHeight = level instanceof ServerLevel l ? l.getGameRules().get(GameRules.MAX_SNOW_ACCUMULATION_HEIGHT) : 1;

		if (accumulationHeight > 0 && SnowUnderTrees.canSnow(level, pos)) {
			BlockState state = level.getBlockState(pos);

			if (state.is(Blocks.SNOW)) {
				int currentLayers = state.getValue(SnowLayerBlock.LAYERS);

				if (currentLayers < Math.min(accumulationHeight, 8)) {
					BlockState snowLayers = state.setValue(SnowLayerBlock.LAYERS, currentLayers + 1);

					Block.pushEntitiesUp(state, snowLayers, level, pos);
					level.setBlock(pos, snowLayers, 2);
					return true;
				}
			}
			else {
				if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))
					level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);

				level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canBeReplaced(BlockState state) {
		return state.canBeReplaced();
	}

	@Override
	public boolean isSnow(WorldGenLevel level, BlockPos pos) {
		return level.getBlockState(pos).getBlock() == Blocks.SNOW;
	}

	@Override
	public BlockState getStateAfterMelting(BlockState stateNow, WorldGenLevel level, BlockPos pos) {
		return Blocks.AIR.defaultBlockState();
	}
}
