package com.turenidk.mekits.client;

import appeng.api.parts.PartModels;
import appeng.api.util.AEColor;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.client.screen.KitPatternEncoderScreen;
import com.turenidk.mekits.part.KitPatternEncoderPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jetbrains.annotations.NotNull;

@Mod(
        value = MEKits.MODID,
        dist = Dist.CLIENT
)
public final class MEKitsClient {

    public MEKitsClient(
            @NotNull IEventBus modEventBus
    ) {
        PartModels.registerModels(
                KitPatternEncoderPart.MODEL_OFF,
                KitPatternEncoderPart.MODEL_ON
        );

        modEventBus.addListener(
                MEKitsClient::registerMenuScreens
        );

        modEventBus.addListener(
                MEKitsClient::registerItemColours
        );
    }

    private static void registerMenuScreens(
            @NotNull RegisterMenuScreensEvent event
    ) {
        event.register(
                MEKits.KIT_PATTERN_ENCODER_MENU.get(),
                KitPatternEncoderScreen::new
        );
    }

    private static void registerItemColours(
            @NotNull RegisterColorHandlersEvent.Item event
    ) {
        event.register(
                (
                        stack,
                        tintIndex
                ) -> {
                    int colour =
                            AEColor.TRANSPARENT
                                    .getVariantByTintIndex(
                                            tintIndex
                                    );

                    if (colour == -1) {
                        return -1;
                    }

                    return 0xFF000000 | colour;
                },
                MEKits.KIT_PATTERN_ENCODER_PART.get()
        );
    }
}