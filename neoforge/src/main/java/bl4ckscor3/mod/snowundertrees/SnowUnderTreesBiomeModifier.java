package bl4ckscor3.mod.snowundertrees;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ClimateSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record SnowUnderTreesBiomeModifier(Holder<PlacedFeature> snowUnderTreesFeature) implements BiomeModifier {
	public static final MapCodec<SnowUnderTreesBiomeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(i ->
		i.group(
			PlacedFeature.CODEC.fieldOf("feature").forGetter(SnowUnderTreesBiomeModifier::snowUnderTreesFeature)
		).apply(i, SnowUnderTreesBiomeModifier::new));

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD) {
			ClimateSettingsBuilder climateSettings = builder.getClimateSettings();

			if (SnowUnderTrees.shouldAddToBiome(biome, climateSettings.hasPrecipitation(), climateSettings.getTemperature()))
				builder.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, snowUnderTreesFeature);
		}
	}

	@Override
	public MapCodec<? extends BiomeModifier> codec() {
		return MAP_CODEC;
	}
}
