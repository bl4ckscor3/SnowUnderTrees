package bl4ckscor3.mod.snowundertrees.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.SnowVariant;

public class FabricSnowRealMagicManager implements SnowRealMagicManager {
	@Override
	public boolean isSnowVariant(Block block) {
		return block instanceof SnowVariant;
	}

	@Override
	public BlockState getRaw(Block block, BlockState stateNow, WorldGenLevel level, BlockPos pos) {
		return ((SnowVariant) block).srm$getRaw(stateNow, level, pos);
	}
}
