package archives.tater.exclusivefurnaces.mixin;

import archives.tater.exclusivefurnaces.Config;

import net.neoforged.neoforge.common.conditions.WithConditions;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD")
    )
    private void setRemovedRecipes(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci, @Share("removedRecipes") LocalRef<Map<Ingredient, ItemStack>> removedRecipes) {
        removedRecipes.set(new HashMap<>());
    }

    @ModifyArg(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresentOrElse(Ljava/util/function/Consumer;Ljava/lang/Runnable;)V"),
            index = 0
    )
    private Consumer<WithConditions<Recipe<?>>> addRemovedFurnaceRecipe(Consumer<WithConditions<Recipe<?>>> action, @Share("removedRecipes") LocalRef<Map<Ingredient, ItemStack>> removedRecipes) {
        return r -> {
            switch (r.carrier()) {
                case BlastingRecipe blastingRecipe when Config.REMOVE_BLAST_FURNACE_RECIPES.getAsBoolean() ->
                        removedRecipes.get().put(blastingRecipe.getIngredients().getFirst(), blastingRecipe.getResultItem(registries));
                case SmokingRecipe smokingRecipe when Config.REMOVE_SMOKER_RECIPES.getAsBoolean() ->
                        removedRecipes.get().put(smokingRecipe.getIngredients().getFirst(), smokingRecipe.getResultItem(registries));
                case CampfireCookingRecipe campfireRecipe when Config.REMOVE_CAMPFIRE_RECIPES.getAsBoolean() ->
                        removedRecipes.get().put(campfireRecipe.getIngredients().getFirst(), campfireRecipe.getResultItem(registries));
                default -> {}
            }
            action.accept(r);
        };
    }

    @ModifyExpressionValue(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;")
    )
    private ImmutableMultimap<RecipeType<?>, RecipeHolder<?>> removeByTypeFurnaceRecipes(ImmutableMultimap<RecipeType<?>, RecipeHolder<?>> original, @Share("removedRecipes") LocalRef<Map<Ingredient, ItemStack>> removedRecipes) {
        var builder = new ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>>();
        original.forEach((type, recipe) -> {
            if (recipe.value() instanceof SmeltingRecipe smeltingRecipe) {
                var removedResult = removedRecipes.get().get(smeltingRecipe.getIngredients().getFirst());
                if (removedResult != null && ItemStack.matches(removedResult, smeltingRecipe.getResultItem(registries))) return;
            }

            builder.put(type, recipe);
        });
        return builder.build();
    }

    @ModifyExpressionValue(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;")
    )
    private ImmutableMap<ResourceLocation, RecipeHolder<?>> removeByNameFurnaceRecipes(ImmutableMap<ResourceLocation, RecipeHolder<?>> original, @Share("removedRecipes") LocalRef<Map<Ingredient, ItemStack>> removedRecipes) {
        var builder = new ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>>();
        original.forEach((type, recipe) -> {
            if (recipe.value() instanceof SmeltingRecipe smeltingRecipe) {
                var removedResult = removedRecipes.get().get(smeltingRecipe.getIngredients().getFirst());
                if (removedResult != null && ItemStack.matches(removedResult, smeltingRecipe.getResultItem(registries))) return;
            }

            builder.put(type, recipe);
        });
        return builder.build();
    }
}
