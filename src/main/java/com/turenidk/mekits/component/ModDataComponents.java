package com.turenidk.mekits.component;

import com.mojang.serialization.Codec;
import com.turenidk.mekits.MEKits;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    MEKits.MODID
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> KIT_NAME =
            DATA_COMPONENTS.registerComponentType(
                    "kit_name",
                    builder -> builder.persistent(Codec.STRING)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KitContents>> KIT_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "kit_contents",
                    builder -> builder
                            .persistent(KitContents.CODEC)
                            .networkSynchronized(KitContents.STREAM_CODEC)
            );

    private ModDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}