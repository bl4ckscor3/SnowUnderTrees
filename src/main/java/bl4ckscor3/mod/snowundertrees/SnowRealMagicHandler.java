package bl4ckscor3.mod.snowundertrees;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import snownee.snow.block.ModSnowLayerBlock;

public class SnowRealMagicHandler
{
	public static boolean placeSnow(WorldGenLevel level, BlockPos pos)
	{
		if(SnowUnderTrees.canSnow(level, pos))
			return ModSnowLayerBlock.convert(level, pos, level.getBlockState(pos), 1, 2);
		else
			return false;
	}
}
