package bl4ckscor3.mod.snowundertrees;

import com.ferreusveritas.dynamictrees.data.DTBlockTags;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class DynamicTreesHandler
{
	public static BlockPos findGround(IWorld world, BlockPos.Mutable pos)
	{
		BlockState state;

		pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
		state = world.getBlockState(pos);

		while(state.isAir(world, pos) || state.isIn(DTBlockTags.BRANCHES) || state.isIn(DTBlockTags.LEAVES))
		{
			pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
			state = world.getBlockState(pos);
		}

		//make sure snow real magic still works by not moving the block position up when a flower/grass/etc. is at the position
		if(!(state.isIn(DTBlockTags.FOLIAGE) || state.isIn(BlockTags.FLOWERS) || state.getBlock() == Blocks.BROWN_MUSHROOM || state.getBlock() == Blocks.RED_MUSHROOM || state.getBlock() == Blocks.SWEET_BERRY_BUSH))
			pos.setPos(pos.getX(), pos.getY() + 1, pos.getZ()); //the block above the ground is needed, not the ground block itself

		return pos;
	}
}
