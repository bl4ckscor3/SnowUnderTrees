package bl4ckscor3.mod.snowundertrees;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import snownee.snow.block.ModSnowBlock;

public class SnowRealMagicHandler
{
	public static boolean placeSnow(IWorld world, BlockPos pos)
	{
		if(world.getBiome(pos).getTemperature() <= 0.15 && pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(LightType.BLOCK, pos) < 10)
			return ModSnowBlock.convert(world, pos, world.getBlockState(pos), 1, 2);
		else
			return false;
	}
}
