package bl4ckscor3.mod.snowundertrees;

import java.lang.reflect.*;
import java.util.Random;
import java.util.function.*;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class SnowUnderTreesFeature extends Feature<NoFeatureConfig>
{
	public SnowUnderTreesFeature(Codec<NoFeatureConfig> codec)
	{
		super(codec);
	}

	@Override
	public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config)
	{
		BlockPos.Mutable mPos = new BlockPos.Mutable();

		Function<BlockPos, Boolean> placeSnow;
		try {
			Class<?> modSnowBlockClass = Class.forName("snownee.snow.block.ModSnowBlock");
			Method convert = modSnowBlockClass.getMethod("convert", IWorld.class, BlockPos.class, BlockState.class, Integer.TYPE, Integer.TYPE);
			placeSnow = (blockPos) -> {
				try {
					if (world.getBiome(blockPos).getTemperature() <= 0.15 && blockPos.getY() >= 0 && blockPos.getY() < 256 && world.getLightFor(LightType.BLOCK, blockPos) < 10) {
						return (Boolean) convert.invoke(null, world, blockPos, world.getBlockState(blockPos), 1, 2);
					} else {
						return false;
					}
				} catch (IllegalAccessException | InvocationTargetException e) {
					return false;
				}
			};
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			placeSnow = (blockPos) -> {
				if (world.getBiome(blockPos).doesSnowGenerate(world, blockPos)) {
					world.setBlockState(blockPos, Blocks.SNOW.getDefaultState(), 2);
					return true;
				} else {
					return false;
				}
			};
		}

		for(int xi = 0; xi < 16; xi++)
		{
			for(int zi = 0; zi < 16; zi++)
			{
				int x = pos.getX() + xi;
				int z = pos.getZ() + zi;

				mPos.setPos(x, world.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z) - 1, z);

				if(world.getBlockState(mPos).getBlock() instanceof LeavesBlock)
				{
					mPos.setPos(x, world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);

					if(placeSnow.apply(mPos))
					{
						BlockState stateBelow;
						mPos.move(Direction.DOWN);
						stateBelow = world.getBlockState(mPos);

						if(stateBelow.hasProperty(SnowyDirtBlock.SNOWY))
							world.setBlockState(mPos, stateBelow.with(SnowyDirtBlock.SNOWY, true), 2);
					}
				}
			}
		}

		return true;
	}
}
