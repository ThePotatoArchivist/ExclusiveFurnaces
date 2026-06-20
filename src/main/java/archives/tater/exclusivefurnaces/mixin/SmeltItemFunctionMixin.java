package archives.tater.exclusivefurnaces.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;

import java.util.Optional;

@Mixin(SmeltItemFunction.class)
public class SmeltItemFunctionMixin {
    @WrapOperation(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;")
    )
    private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> tryOtherSmeltingTypes(RecipeManager instance, RecipeType<T> recipeType, I input, Level level, Operation<Optional<RecipeHolder<T>>> original) {
        var originalResult = original.call(instance, recipeType, input, level);
        if (originalResult.isPresent()) return originalResult;

        var singleInput = (SingleRecipeInput) input;

        var smokeResult = instance.getRecipeFor(RecipeType.SMOKING, singleInput, level);
        if (smokeResult.isPresent()) return exclusiveFurnaces$repackage(smokeResult);

        var blastResult = instance.getRecipeFor(RecipeType.BLASTING, singleInput, level);
        if (blastResult.isPresent()) return exclusiveFurnaces$repackage(smokeResult);

        return exclusiveFurnaces$repackage(instance.getRecipeFor(RecipeType.CAMPFIRE_COOKING, singleInput, level));
    }

    @Unique
    private static SmeltingRecipe exclusiveFurnaces$repackage(AbstractCookingRecipe recipe) {
        return new SmeltingRecipe(recipe.getGroup(), recipe.category(), recipe.getIngredients().getFirst(), recipe.getResultItem(null), recipe.getExperience(), recipe.getCookingTime());
    }

    @Unique
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked"})
    private static <T extends Recipe<?>> Optional<RecipeHolder<T>> exclusiveFurnaces$repackage(Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> recipe) {
        return recipe.map(holder -> new RecipeHolder<>(holder.id(), (T) exclusiveFurnaces$repackage(holder.value())));
    }
}
