package com.turenidk.mekits.client;

import appeng.api.parts.PartModels;
import appeng.api.util.AEColor;
import appeng.client.gui.style.StyleManager;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.client.screen.KitPatternEncoderScreen;
import com.turenidk.mekits.client.screen.MEKitPackagerScreen;
import com.turenidk.mekits.menu.MEKitPackagerMenu;
import com.turenidk.mekits.part.KitPatternEncoderPart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
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

    private static final String PACKAGER_SCREEN_STYLE =
            "/screens/me_kit_packager.json";

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

        event.<MEKitPackagerMenu, MEKitPackagerScreen>register(
                MEKits.ME_KIT_PACKAGER_MENU.get(),
                (
                        MEKitPackagerMenu menu,
                        Inventory playerInventory,
                        Component title
                ) -> new MEKitPackagerScreen(
                        menu,
                        playerInventory,
                        title,
                        StyleManager.loadStyleDoc(
                                PACKAGER_SCREEN_STYLE
                        )
                )
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