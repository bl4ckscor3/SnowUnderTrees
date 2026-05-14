package bl4ckscor3.mod.snowundertrees.manager;

import bl4ckscor3.mod.snowundertrees.SnowUnderTrees;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;

public interface SnowRealMagicManager extends SnowManager {
	@Override
	default boolean placeSnow(WorldGenLevel level, BlockPos pos) {
		if (SnowUnderTrees.canSnow(level, pos))
			return Hooks.convert(level, pos, level.getBlockState(pos), 1, 2, SnowCommonConfig.placeSnowOnBlockNaturally);
		else
			return false;
	}

	@Override
	default boolean isSnow(WorldGenLevel level, BlockPos pos) {
		Block block = level.getBlockState(pos).getBlock();

		return block == Blocks.SNOW || isSnowVariant(block);
	}

	@Override
	default BlockState getStateAfterMelting(BlockState stateNow, WorldGenLevel level, BlockPos pos) {
		if (SnowCommonConfig.snowNeverMelt)
			return stateNow;

		Block block = stateNow.getBlock();

		if (isSnowVariant(block))
			return getRaw(block, stateNow, level, pos);

		return Blocks.AIR.defaultBlockState();
	}

	boolean isSnowVariant(Block block);

	BlockState getRaw(Block block, BlockState stateNow, WorldGenLevel level, BlockPos pos);
}
