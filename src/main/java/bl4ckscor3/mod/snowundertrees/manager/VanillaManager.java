package bl4ckscor3.mod.snowundertrees.manager;

import bl4ckscor3.mod.snowundertrees.SnowUnderTrees;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VanillaManager implements SnowManager {
	@Override
	public boolean placeSnow(WorldGenLevel level, BlockPos pos) {
		int accumulationHeight = level instanceof Level l ? l.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT) : 1;

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
				level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
				return true;
			}
		}

		return false;
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
