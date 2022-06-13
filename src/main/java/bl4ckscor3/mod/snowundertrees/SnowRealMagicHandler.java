package bl4ckscor3.mod.snowundertrees;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SnowRealMagicHandler
{
	public static boolean placeSnow(WorldGenLevel level, BlockPos pos)
	{
		//		if(SnowUnderTrees.canSnow(level, pos))
		//			return ModSnowLayerBlock.convert(level, pos, level.getBlockState(pos), 1, 2);
		//		else
		return false;
	}

	public static boolean isSnow(WorldGenLevel level, BlockPos pos)
	{
		Block block = level.getBlockState(pos).getBlock();

		return block == Blocks.SNOW;// || block instanceof SnowVariant;
	}

	public static BlockState getStateAfterMelting(BlockState stateNow, WorldGenLevel level, BlockPos pos)
	{
		//		if(SnowCommonConfig.snowNeverMelt)
		//			return stateNow;
		//
		//		if(stateNow.getBlock() instanceof SnowVariant snowVariant)
		//			return snowVariant.getRaw(stateNow, level, pos);

		return Blocks.AIR.defaultBlockState();
	}
}
