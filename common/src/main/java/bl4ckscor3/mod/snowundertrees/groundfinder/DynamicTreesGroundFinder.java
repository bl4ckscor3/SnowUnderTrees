package bl4ckscor3.mod.snowundertrees.groundfinder;

import com.dtteam.dynamictrees.data.tags.DTBlockTags;

import net.minecraft.world.level.block.state.BlockState;

public class DynamicTreesGroundFinder extends VanillaGroundFinder {
	@Override
	public boolean isLog(BlockState state) {
		return state.is(DTBlockTags.BRANCHES) || super.isLog(state);
	}

	@Override
	public boolean shouldContinueLookingForGround(BlockState state) {
		return super.shouldContinueLookingForGround(state) || state.is(DTBlockTags.LEAVES);
	}

	@Override
	public boolean isVegetation(BlockState state) {
		return state.is(DTBlockTags.FOLIAGE) || super.isVegetation(state);
	}
}