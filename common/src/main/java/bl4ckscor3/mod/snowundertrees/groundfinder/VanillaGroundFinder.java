package bl4ckscor3.mod.snowundertrees.groundfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class VanillaGroundFinder implements GroundFinder {
	@Override
	public BlockPos findGround(WorldGenLevel level, BlockPos.MutableBlockPos pos) {
		BlockState state;

		pos.setY(pos.getY() - 1);
		state = level.getBlockState(pos);

		while (shouldContinueLookingForGround(state)) {
			int newY = pos.getY() - 1;

			if (newY < level.getMinY())
				return null;

			pos.setY(newY);
			state = level.getBlockState(pos);
		}

		//make sure snow real magic still works by not moving the block position up when a flower/grass/etc. is at the position
		if (!isVegetation(state))
			pos.setY(pos.getY() + 1); //the block above the ground is needed, not the ground block itself

		return pos;
	}

	public boolean isLog(BlockState state) {
		return state.is(BlockTags.LOGS);
	}

	public boolean shouldContinueLookingForGround(BlockState state) {
		return state.isAir() || state.is(BlockTags.LEAVES) || isLog(state);
	}

	public boolean isVegetation(BlockState state) {
		return state.getBlock() == Blocks.SWEET_BERRY_BUSH;
	}
}