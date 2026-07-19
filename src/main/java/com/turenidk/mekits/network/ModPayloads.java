package com.turenidk.mekits.network;

import com.turenidk.mekits.network.payload.AdjustIngredientQuantityPayload;
import com.turenidk.mekits.network.payload.ClearEditorStatePayload;
import com.turenidk.mekits.network.payload.EncodePatternPayload;
import com.turenidk.mekits.network.payload.UpdateKitNamePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;

public final class ModPayloads {

    private static final String NETWORK_VERSION = "3";

    private ModPayloads() {
    }

    public static void register(
            @NotNull IEventBus modEventBus
    ) {
        modEventBus.addListener(
                ModPayloads::registerPayloads
        );
    }

    private static void registerPayloads(
            @NotNull RegisterPayloadHandlersEvent event
    ) {
        var registrar =
                event.registrar(
                        NETWORK_VERSION
                );

        registrar.playToServer(
                UpdateKitNamePayload.TYPE,
                UpdateKitNamePayload.STREAM_CODEC,
                UpdateKitNamePayload::handle
        );

        registrar.playToServer(
                EncodePatternPayload.TYPE,
                EncodePatternPayload.STREAM_CODEC,
                EncodePatternPayload::handle
        );

        registrar.playToServer(
                AdjustIngredientQuantityPayload.TYPE,
                AdjustIngredientQuantityPayload.STREAM_CODEC,
                AdjustIngredientQuantityPayload::handle
        );

        registrar.playToServer(
                ClearEditorStatePayload.TYPE,
                ClearEditorStatePayload.STREAM_CODEC,
                ClearEditorStatePayload::handle
        );
    }
}