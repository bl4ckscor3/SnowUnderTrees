package bl4ckscor3.mod.snowundertrees.groundfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

public interface GroundFinder {
	BlockPos findGround(WorldGenLevel level, BlockPos.MutableBlockPos pos);
}
