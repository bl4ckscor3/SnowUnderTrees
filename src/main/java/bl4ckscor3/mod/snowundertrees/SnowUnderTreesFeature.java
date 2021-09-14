package bl4ckscor3.mod.snowundertrees;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
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

		for(int xi = 0; xi < 16; xi++)
		{
			for(int zi = 0; zi < 16; zi++)
			{
				int x = pos.getX() + xi;
				int z = pos.getZ() + zi;

				mPos.setPos(x, world.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z) - 1, z);

				if(world.getBlockState(mPos).isIn(BlockTags.LEAVES))
				{
					mPos.setPos(x, world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);

					if(SnowUnderTrees.isDynamicTreesLoaded())
						DynamicTreesHandler.findGround(world, mPos);

					if(SnowUnderTrees.placeSnow(world, mPos))
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
