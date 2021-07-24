package bl4ckscor3.mod.snowundertrees;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowUnderTreesFeature extends Feature<NoneFeatureConfiguration>
{
	public SnowUnderTreesFeature(Codec<NoneFeatureConfiguration> codec)
	{
		super(codec);
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator generator, Random rand, BlockPos pos, NoneFeatureConfiguration config)
	{
		BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

		for(int xi = 0; xi < 16; xi++)
		{
			for(int zi = 0; zi < 16; zi++)
			{
				int x = pos.getX() + xi;
				int z = pos.getZ() + zi;

				mPos.set(x, world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);

				if(world.getBlockState(mPos).getBlock() instanceof LeavesBlock)
				{
					mPos.set(x, world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);

					if(world.getBiome(mPos).shouldSnow(world, mPos))
					{
						BlockState stateBelow;

						world.setBlock(mPos, Blocks.SNOW.defaultBlockState(), 2);
						mPos.move(Direction.DOWN);
						stateBelow = world.getBlockState(mPos);

						if(stateBelow.hasProperty(SnowyDirtBlock.SNOWY))
							world.setBlock(mPos, stateBelow.setValue(SnowyDirtBlock.SNOWY, true), 2);
					}
				}
			}
		}

		return true;
	}
}
