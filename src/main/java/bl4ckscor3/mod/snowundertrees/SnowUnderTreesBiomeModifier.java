package bl4ckscor3.mod.snowundertrees;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ClimateSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record SnowUnderTreesBiomeModifier(Holder<PlacedFeature> snowUnderTreesFeature) implements BiomeModifier {
	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD && Configuration.CONFIG.enableBiomeFeature.get()) {
			boolean isEternalWinterActive = SnowUnderTrees.isEternalWinterLoaded() && EternalWinterHandler.isActive(biome);

			if (isEternalWinterActive || shouldAddToBiome(biome, builder.getClimateSettings()))
				builder.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal(), snowUnderTreesFeature);
		}
	}

	public boolean shouldAddToBiome(Holder<Biome> biome, ClimateSettingsBuilder climate) {
		if (isFiltered(biome))
			return false;

		return isPrecipitationSnow(climate) || isManuallyAdded(biome);
	}

	public boolean isPrecipitationSnow(ClimateSettingsBuilder climate) {
		return climate.hasPrecipitation() && climate.getTemperature() < 0.15F;
	}

	public boolean isManuallyAdded(Holder<Biome> biome) {
		return SnowUnderTrees.biomesToAddTo.stream().anyMatch(biome::is);
	}

	public boolean isFiltered(Holder<Biome> biome) {
		//@formatter:off
		return Configuration.CONFIG.filteredBiomes.get()
				.stream()
				.map(ResourceLocation::parse)
				.anyMatch(biome::is);
		//@formatter:on
	}

	@Override
	public MapCodec<? extends BiomeModifier> codec() {
		return SnowUnderTrees.SNOW_UNDER_TREES_BIOME_MODIFIER_CODEC.get();
	}
}
