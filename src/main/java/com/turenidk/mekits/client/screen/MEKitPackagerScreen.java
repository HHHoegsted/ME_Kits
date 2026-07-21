package com.turenidk.mekits.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.turenidk.mekits.menu.MEKitPackagerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public final class MEKitPackagerScreen
        extends AEBaseScreen<MEKitPackagerMenu> {

    public MEKitPackagerScreen(
            @NotNull MEKitPackagerMenu menu,
            @NotNull Inventory playerInventory,
            @NotNull Component title,
            @NotNull ScreenStyle style
    ) {
        super(
                menu,
                playerInventory,
                title,
                style
        );
    }
}