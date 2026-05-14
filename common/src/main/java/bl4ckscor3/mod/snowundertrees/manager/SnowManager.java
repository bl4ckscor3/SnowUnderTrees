package bl4ckscor3.mod.snowundertrees.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface SnowManager {
	public boolean placeSnow(WorldGenLevel level, BlockPos pos);

	public boolean isSnow(WorldGenLevel level, BlockPos pos);

	public BlockState getStateAfterMelting(BlockState stateNow, WorldGenLevel level, BlockPos pos);
}
