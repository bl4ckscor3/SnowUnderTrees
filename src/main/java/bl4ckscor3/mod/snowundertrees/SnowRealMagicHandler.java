package bl4ckscor3.mod.snowundertrees;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import snownee.snow.block.ModSnowBlock;

public class SnowRealMagicHandler
{
	public static boolean placeSnow(IWorld world, BlockPos pos)
	{
		if(world.getBiome(pos).doesSnowGenerate(world, pos))
			return ModSnowBlock.convert(world, pos, world.getBlockState(pos), 1, 2);
		else
			return false;
	}
}
