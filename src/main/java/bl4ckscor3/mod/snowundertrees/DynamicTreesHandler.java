package bl4ckscor3.mod.snowundertrees;

import com.ferreusveritas.dynamictrees.data.DTBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DynamicTreesHandler
{
	public static BlockPos findGround(WorldGenLevel level, BlockPos.MutableBlockPos pos)
	{
		BlockState state;

		pos.set(pos.getX(), pos.getY() - 1, pos.getZ());
		state = level.getBlockState(pos);

		while(state.isAir() || state.is(DTBlockTags.BRANCHES) || state.is(DTBlockTags.LEAVES))
		{
			pos.set(pos.getX(), pos.getY() - 1, pos.getZ());
			state = level.getBlockState(pos);
		}

		//make sure snow real magic still works by not moving the block position up when a flower/grass/etc. is at the position
		if(!(state.is(DTBlockTags.FOLIAGE) || state.is(BlockTags.FLOWERS) || state.getBlock() == Blocks.BROWN_MUSHROOM || state.getBlock() == Blocks.RED_MUSHROOM || state.getBlock() == Blocks.SWEET_BERRY_BUSH))
			pos.set(pos.getX(), pos.getY() + 1, pos.getZ()); //the block above the ground is needed, not the ground block itself

		return pos;
	}
}