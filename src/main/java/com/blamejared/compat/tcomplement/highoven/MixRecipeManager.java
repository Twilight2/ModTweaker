package com.blamejared.compat.tcomplement.highoven;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.blamejared.compat.mantle.RecipeMatchIIngredient;
import com.blamejared.mtlib.helpers.InputHelper;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.liquid.ILiquidStack;
import knightminer.tcomplement.library.events.TCompRegisterEvent;
import knightminer.tcomplement.library.steelworks.MixAdditive;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.mantle.util.RecipeMatch;
import stanhebben.zenscript.annotations.NotNull;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.tcomplement.highoven.MixRecipeManager")
@ZenRegister
@ModOnly("tcomplement")
public class MixRecipeManager {
	private boolean init;
	private FluidStack input, output;
	private List<SimpleEntry<RecipeMatch, MixAdditive>> additives;
	private Map<MixAdditive, List<RecipeMatch>> removedAdditives;

	public MixRecipeManager(ILiquidStack output, ILiquidStack input) {
		this.init = false;
		this.input = InputHelper.toFluid(input);
		this.output = InputHelper.toFluid(output);
		this.additives = new LinkedList<>();
		this.removedAdditives = new HashMap<>(MixAdditive.values().length, 1.0f);
		for (MixAdditive type : MixAdditive.values()) {
			this.removedAdditives.put(type, new LinkedList<>());
		}
	}

	@ZenMethod
	public MixRecipeManager addOxidizer(@NotNull IIngredient oxidizer, int consumeChance) {
		this.additives.add(new SimpleEntry<>(new RecipeMatchIIngredient(oxidizer, consumeChance), MixAdditive.OXIDIZER));
		return this;
	}

	@ZenMethod
	public MixRecipeManager addReducer(@NotNull IIngredient reducer, int consumeChance) {
		this.additives.add(new SimpleEntry<>(new RecipeMatchIIngredient(reducer, consumeChance), MixAdditive.REDUCER));
		return this;
	}

	@ZenMethod
	public MixRecipeManager addPurifier(@NotNull IIngredient purifier, int consumeChance) {
		this.additives.add(new SimpleEntry<>(new RecipeMatchIIngredient(purifier, consumeChance), MixAdditive.PURIFIER));
		return this;
	}

	@ZenMethod
	public MixRecipeManager removeOxidizer(IIngredient oxidizer) {
		if (oxidizer != null && removedAdditives.get(MixAdditive.OXIDIZER) != null) {
			removedAdditives.get(MixAdditive.OXIDIZER).add(new RecipeMatchIIngredient(oxidizer));
		} else {
			removedAdditives.put(MixAdditive.OXIDIZER, null);
		}
		return this;
	}

	@ZenMethod
	public MixRecipeManager removeReducer(IIngredient reducer) {
		if (reducer != null && removedAdditives.get(MixAdditive.REDUCER) != null) {
			removedAdditives.get(MixAdditive.REDUCER).add(new RecipeMatchIIngredient(reducer));
		} else {
			removedAdditives.put(MixAdditive.REDUCER, null);
		}
		return this;
	}

	@ZenMethod
	public MixRecipeManager removePurifier(IIngredient purifier) {
		if (purifier != null && removedAdditives.get(MixAdditive.PURIFIER) != null) {
			removedAdditives.get(MixAdditive.PURIFIER).add(new RecipeMatchIIngredient(purifier));
		} else {
			removedAdditives.put(MixAdditive.PURIFIER, null);
		}
		return this;
	}

	public void register() {
		if (!this.init) {
			MinecraftForge.EVENT_BUS.register(this);
			init = true;
		}
	}

	@SubscribeEvent
	public void onTinkerRegister(TCompRegisterEvent.HighOvenMixRegisterEvent event) {
		if (event.getRecipe().matches(this.input, this.output)) {
			this.additives.forEach((SimpleEntry<RecipeMatch, MixAdditive> entry) -> event.getRecipe()
					.addAdditive(entry.getValue(), entry.getKey()));
		}
	}

	@SubscribeEvent
	public void onTinkerRegister(TCompRegisterEvent.HighOvenMixAdditiveEvent event) {
		if (event.getRecipe().matches(input, output)) {
			List<RecipeMatch> removals = removedAdditives.get(event.getType());
			if (removals != null) {
				for (RecipeMatch removal : removals) {
					if (removal.matches((NonNullList<ItemStack>) event.getAdditive().getInputs()).isPresent()) {
						event.setCanceled(true);
						break;
					}
				}
			} else {
				event.setCanceled(true);
			}
		}
	}

}
