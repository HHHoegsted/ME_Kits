package com.turenidk.mekits.client;

import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.client.screen.KitPatternEncoderScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
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
        modEventBus.addListener(
                MEKitsClient::registerMenuScreens
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
}