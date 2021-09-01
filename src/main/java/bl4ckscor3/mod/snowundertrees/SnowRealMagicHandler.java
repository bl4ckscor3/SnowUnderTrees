package bl4ckscor3.mod.snowundertrees;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.fml.ModList;
import snownee.snow.block.ModSnowBlock;

public class SnowRealMagicHandler {
    public static boolean placeSnow(IWorld world, BlockPos blockPos) {
        if (ModList.get().isLoaded("snowrealmagic")) {
            if (world.getBiome(blockPos).getTemperature() <= 0.15 && blockPos.getY() >= 0 && blockPos.getY() < 256 && world.getLightFor(LightType.BLOCK, blockPos) < 10) {
                return ModSnowBlock.convert(world, blockPos, world.getBlockState(blockPos), 1, 2);
            } else {
                return false;
            }
        } else {
            if (world.getBiome(blockPos).doesSnowGenerate(world, blockPos)) {
                world.setBlockState(blockPos, Blocks.SNOW.getDefaultState(), 2);
                return true;
            } else {
                return false;
            }
        }
    }
}
