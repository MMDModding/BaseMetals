package com.mcmoddev.lib.integration.plugins;

import javax.annotation.Nonnull;

import com.mcmoddev.basemetals.util.Config.Options;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.lib.integration.IIntegration;
import com.mcmoddev.lib.material.MMDMaterial;

import cofh.api.util.ThermalExpansionHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;

// Parts of this are based on code from older versions of CoFHLib
// Other parts are lifted from the "ThermalExpansionHelper" of the 1.7 versions
// of CoFHLib - they were efficient and did the integration in the simplest
// manner. Why they've been removed, I don't know.

public class ThermalExpansionBase implements IIntegration {

	public static final String PLUGIN_MODID = "thermalexpansion";
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	private static final String ENERGY = "energy";
	
	private static boolean initDone = false;

	@Override
	public void init() {
		if (initDone || !com.mcmoddev.basemetals.util.Config.Options.isModEnabled(PLUGIN_MODID)) {
			return;
		}

		initDone = true;
	}

	public static void addCompactorPressRecipe(@Nonnull final int energy, @Nonnull final ItemStack input, @Nonnull final ItemStack output) {
		if (input == null || output == null) {
			return;
		}

		if (input.getItem() == null || output.getItem() == null) {
			return;
		}

		final NBTTagCompound toSend = new NBTTagCompound();

		toSend.setInteger(ENERGY, energy);
		toSend.setTag(INPUT, new NBTTagCompound());
		toSend.setTag(OUTPUT, new NBTTagCompound());
		input.writeToNBT(toSend.getCompoundTag(INPUT));
		output.writeToNBT(toSend.getCompoundTag(OUTPUT));
		FMLInterModComms.sendMessage(PLUGIN_MODID, "addcompactorpressrecipe", toSend);
	}

	public static void addCompactorStorageRecipe(@Nonnull final int energy, @Nonnull final ItemStack input, @Nonnull final ItemStack output) {
		if (input == null || output == null) {
			return;
		}

		if (input.getItem() == null || output.getItem() == null) {
			return;
		}

		final NBTTagCompound toSend = new NBTTagCompound();

		toSend.setInteger(ENERGY, energy);
		toSend.setTag(INPUT, new NBTTagCompound());
		toSend.setTag(OUTPUT, new NBTTagCompound());
		input.writeToNBT(toSend.getCompoundTag(INPUT));
		output.writeToNBT(toSend.getCompoundTag(OUTPUT));
		FMLInterModComms.sendMessage(PLUGIN_MODID, "addcompactorstoragerecipe", toSend);

	}

	public static void addFurnace(@Nonnull final boolean enabled, @Nonnull final String materialName) {
		// anything we'd want to add here is likely already pulled in by TE's
		// import of all vanilla furnace recipes.
		return;
/*		if (enabled) {
			final MMDMaterial mat = Materials.getMaterialByName(materialName.toLowerCase());

			 * Ore -> Ingot default, according to TE source, is 2000
			 * dust -> Ingot default, according to same, is DEFAULT * 14 / 20 - at the 2000RF default, this is 1400

			final int ENERGY_ORE = 2000;
			final int ENERGY_DUST = 1400;
			ItemStack ore;
			if (mat.hasBlock(Names.ORE) && mat.getBlock(Names.ORE) != null) {
				ore = new ItemStack(mat.getBlock(Names.ORE), 1, 0);
			} else if (mat.hasItem(Names.BLEND) && mat.getItem(Names.BLEND) != null) {
				ore = new ItemStack(mat.getItem(Names.BLEND), 1, 0);
			} else {
				return;
			}

			if ((!mat.hasItem(Names.INGOT)) || mat.getItem(Names.INGOT) == null) {
				return;
			}

			final ItemStack ingot = new ItemStack(mat.getItem(Names.INGOT), 1, 0);
			ThermalExpansionHelper.addFurnaceRecipe(ENERGY_ORE, ore, ingot);
			if (Options.thingEnabled("Basics")) {
				if (mat.hasItem(Names.POWDER) && mat.getItem(Names.POWDER) != null) {
					final ItemStack dust = new ItemStack(mat.getItem(Names.POWDER), 1, 0);
					ThermalExpansionHelper.addFurnaceRecipe(ENERGY_DUST, dust, ingot);
				}
			}
		}
*/	}

	public static void addCrucible(@Nonnull final boolean enabled, @Nonnull final String materialName) {
		addCrucible(enabled, Materials.getMaterialByName(materialName.toLowerCase()));
	}

	public static void addCrucible(@Nonnull final boolean enabled, @Nonnull final MMDMaterial material) {
		if ((enabled) && (material != null)) {
			/*
			 * Default power, according to TE source, is 8000
			 * This is used for Pyrotheum, Cryotheum, Aerotheum, Petrotheum and Redstone.
			 * Redstone Block is 72000
			 * Glowstone and Ender Pearl are 20000
			 * Glowstone block is 80000
			 * Cobblestone/Stone/Obsidian is 320000
			 */
			final int ENERGY_QTY = 8000;

			final String materialName = material.getName();
			final ItemStack ingot = new ItemStack(material.getItem(Names.INGOT));
			final Item dust = material.getItem(Names.POWDER);
			final FluidStack oreFluid = FluidRegistry.getFluidStack(materialName, 288);
			final FluidStack baseFluid = FluidRegistry.getFluidStack(materialName, 144);
			final FluidStack nuggetFluid = FluidRegistry.getFluidStack(materialName, 16);

			if (material.getBlock(Names.ORE) != null) {
				final ItemStack ore = new ItemStack(material.getBlock(Names.ORE));
				ThermalExpansionHelper.addCrucibleRecipe(ENERGY_QTY, ore, oreFluid);
			}

			ThermalExpansionHelper.addCrucibleRecipe(ENERGY_QTY, ingot, baseFluid);

			if (Options.isThingEnabled("Basics") && dust != null) {
				final ItemStack dustStack = new ItemStack(dust);
				ThermalExpansionHelper.addCrucibleRecipe(ENERGY_QTY, dustStack, baseFluid);
			}

			addCrucibleExtra(Options.isThingEnabled("Plate"), Item.getItemFromBlock(material.getBlock(Names.PLATE)), baseFluid, ENERGY_QTY);
			addCrucibleExtra(Options.isThingEnabled("Basics"), material.getItem(Names.NUGGET), nuggetFluid, ENERGY_QTY);
		}
	}

	private static void addCrucibleExtra(@Nonnull final boolean enabled, @Nonnull final Item input, @Nonnull final FluidStack output, @Nonnull final int energy) {
		if ((enabled) && (input != null && output != null)) {
			final ItemStack inItems = new ItemStack(input);
			ThermalExpansionHelper.addCrucibleRecipe(energy, inItems, output);
		}
	}

	public static void addPlatePress(@Nonnull final boolean enabled, @Nonnull final String materialName) {
		addPlatePress(enabled, Materials.getMaterialByName(materialName.toLowerCase()));
	}

	public static void addPlatePress(@Nonnull final boolean enabled, @Nonnull final MMDMaterial material) {
		if ((enabled) && (Options.isThingEnabled("Plate") && material != null)) {

			/*
			 * Compactors default is 4000RF per operation
			 */
			final int ENERGY_QTY = 4000;
			addCompactorPressRecipe(ENERGY_QTY, new ItemStack(material.getItem(Names.INGOT)), new ItemStack(material.getBlock(Names.PLATE)));
		}
	}

	public static void addPressStorage(@Nonnull final boolean enabled, @Nonnull final String materialName) {
		addPressStorage(enabled, Materials.getMaterialByName(materialName.toLowerCase()));
	}

	public static void addPressStorage(@Nonnull final boolean enabled, @Nonnull final MMDMaterial material) {
		if (enabled && material != null) {

			/*
			 * Compactors default is 4000RF per operation
			 */
			final int ENERGY_QTY = 4000;
			final Item ingot = material.getItem(Names.INGOT);
			final ItemStack ingotsStack = new ItemStack(ingot, 9);
			final ItemStack nuggetsStack = new ItemStack(material.getItem(Names.NUGGET), 9);
			final ItemStack blockStack = new ItemStack(material.getBlock(Names.BLOCK), 1);
			final ItemStack ingotStack = new ItemStack(ingot, 1);

			addCompactorStorageRecipe(ENERGY_QTY, ingotsStack, blockStack);
			addCompactorStorageRecipe(ENERGY_QTY, nuggetsStack, ingotStack);
		}
	}
}
