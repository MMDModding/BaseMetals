package com.mcmoddev.lib.integration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mcmoddev.basemetals.BaseMetals;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public enum IntegrationManager {

	INSTANCE;

	private final List<IIntegration> integrations = Lists.newArrayList();
	private final Map<String, List<Map<IIntegration, Method>>> callbacks = new HashMap<>();

	private String getAnnotationItem(@Nonnull final String item, @Nonnull final ASMData asmData) {
		if (asmData.getAnnotationInfo().get(item) != null) {
			return asmData.getAnnotationInfo().get(item).toString();
		} else {
			return "";
		}
	}

	public void preInit(@Nonnull final FMLPreInitializationEvent event) {
		for (final ASMData asmDataItem : event.getAsmData()
				.getAll(MMDPlugin.class.getCanonicalName())) {
			final String addonId = this.getAnnotationItem("addonId", asmDataItem);
			final String pluginId = this.getAnnotationItem("pluginId", asmDataItem);
			final String preInitCallback = this.getAnnotationItem("preInitCallback", asmDataItem);
			final String initCallback = this.getAnnotationItem("initCallback", asmDataItem);
			final String postInitCallback = this.getAnnotationItem("postInitCallback", asmDataItem);
			final String clazz = asmDataItem.getClassName();

			if ((event.getModMetadata().modId.equals(addonId)) && (Loader.isModLoaded(pluginId))) {
				IIntegration integration;
				try {
					integration = Class.forName(clazz).asSubclass(IIntegration.class).newInstance();
					this.integrations.add(integration);
					integration.init();

					BaseMetals.logger.debug("Loaded " + pluginId + " for " + addonId);
					this.setCallback(integration, preInitCallback, "preInit");
					this.setCallback(integration, initCallback, "init");
					this.setCallback(integration, postInitCallback, "postInit");
				} catch (final Exception ex) {
					BaseMetals.logger.error("Couldn't load " + pluginId + " for " + addonId, ex);
				}
			}
		}
	}

	private void setCallback(@Nonnull final IIntegration i, @Nonnull final String name,
			@Nonnull final String phase) {
		if ("".equals(name)) {
			return;
		}

		List<Map<IIntegration, Method>> k;
		if (this.callbacks.containsKey(phase)) {
			k = this.callbacks.get(phase);
		} else {
			k = Lists.newArrayList();
		}
		final Map<IIntegration, Method> mmm = new HashMap<>();
		try {
			mmm.put(i, i.getClass().getMethod(name));
			k.add(mmm);
			this.callbacks.put(phase, k);
		} catch (final Exception ex) {
			BaseMetals.logger
					.debug("Exception adding callback " + name + " for loading phase " + phase, ex);
		}
	}

	private void runCallback(@Nonnull final Map<IIntegration, Method> cbs) {
		for (final Entry<IIntegration, Method> ent : cbs.entrySet()) {
			try {
				ent.getValue().invoke(ent.getKey());
			} catch (final Exception ex) {
				BaseMetals.logger.debug("Exception running callback: ", ex);
			}
		}
	}

	public void runCallbacks(@Nonnull final String phase) {
		if (this.callbacks.containsKey(phase)) {
			final List<Map<IIntegration, Method>> cbs = this.callbacks.get(phase);
			for (final Map<IIntegration, Method> map : cbs) {
				this.runCallback(map);
			}
		}
	}
}
