package bl4ckscor3.mod.snowundertrees;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ClimateSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;

public record SnowUnderTreesBiomeModifier(Holder<PlacedFeature> snowUnderTreesFeature) implements BiomeModifier {
	@Override
	public void modify(Holder<Biome> biome, Phase phase, Builder builder) {
		if (phase == Phase.ADD && Configuration.CONFIG.enableBiomeFeature.get()) {
			ClimateSettingsBuilder climate = builder.getClimateSettings();
			boolean isEternalWinterActive = SnowUnderTrees.isEternalWinterLoaded() && EternalWinterHandler.isActive(biome);

			if (isEternalWinterActive || ((climate.hasPrecipitation() && climate.getTemperature() < 0.15F || SnowUnderTrees.biomesToAddTo.stream().anyMatch(biome::is)) && !Configuration.CONFIG.filteredBiomes.get().stream().anyMatch(string -> biome.is(new ResourceLocation(string)))))
				builder.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal(), snowUnderTreesFeature);
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return SnowUnderTrees.SNOW_UNDER_TREES_BIOME_MODIFIER_CODEC.get();
	}
}
