package archives.tater.exclusivefurnaces;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue REMOVE_BLAST_FURNACE_RECIPES = BUILDER
            .comment("Whether to remove blast furnace recipes from the furnace")
            .define("remove_blast_furnace_recipes", true);

    public static final ModConfigSpec.BooleanValue REMOVE_SMOKER_RECIPES = BUILDER
            .comment("Whether to remove smoker recipes from the furnace")
            .define("remove_smoker_recipes", true);
    static final ModConfigSpec SPEC = BUILDER.build();
}
