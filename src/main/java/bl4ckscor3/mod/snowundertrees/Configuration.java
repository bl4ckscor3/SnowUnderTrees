package bl4ckscor3.mod.snowundertrees;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Configuration {
	public static final ForgeConfigSpec CONFIG_SPEC;
	public static final Configuration CONFIG;
	public final BooleanValue enableBiomeFeature;
	public final BooleanValue enableWhenSnowing;
	public final ConfigValue<List<? extends String>> filteredBiomes;

	static {
		Pair<Configuration, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Configuration::new);

		CONFIG_SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
	}

	Configuration(ForgeConfigSpec.Builder builder) {
		//@formatter:off
		enableBiomeFeature = builder
				.comment("Set this to false to disable snow under trees when first generating chunks.")
				.define("enable_biome_feature", true);
		enableWhenSnowing = builder
				.comment("Set this to false to disable snow under trees when it's snowing.")
				.define("enable_when_snowing", true);
		filteredBiomes = builder
				.comment("Add biome IDs here to exempt biomes from being affected by the mod (surrounded by \"\"). You can find the biome ID of the biome you're currently in on the F3 screen.",
						"For example, the biome ID of the plains biome looks like this: minecraft:plains")
				.defineList("filtered_biomes", Lists.newArrayList(), e -> e instanceof String);
		//@formatter:on
	}
}
