package com.turenidk.mekits.client.render;

import com.turenidk.mekits.MEKits;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class KitGuiItemRendering {

    private static final ThreadLocal<Boolean>
            RENDERING_REPLACEMENT =
            ThreadLocal.withInitial(
                    () -> false
            );

    private KitGuiItemRendering() {
    }

    public static boolean onRenderGuiItem(
            GuiGraphics guiGraphics,
            @Nullable LivingEntity livingEntity,
            @Nullable Level level,
            ItemStack stack,
            int x,
            int y,
            int seed
    ) {
        if (RENDERING_REPLACEMENT.get()) {
            return false;
        }

        boolean renderKitIdentity =
                stack.is(
                        MEKits.ME_KIT.get()
                );

        boolean renderShiftedPatternIdentity =
                stack.is(
                        MEKits
                                .ENCODED_ME_KIT_PATTERN
                                .get()
                )
                        && Screen.hasShiftDown();

        if (
                !renderKitIdentity
                        && !renderShiftedPatternIdentity
        ) {
            return false;
        }

        ItemStack iconStack =
                KitIconHelper.getFirstIcon(
                        stack
                );

        if (iconStack.isEmpty()) {
            return false;
        }

        renderComposite(
                guiGraphics,
                livingEntity,
                iconStack,
                x,
                y,
                seed
        );

        return true;
    }

    private static void renderComposite(
            GuiGraphics guiGraphics,
            @Nullable LivingEntity livingEntity,
            ItemStack iconStack,
            int x,
            int y,
            int seed
    ) {
        RENDERING_REPLACEMENT.set(
                true
        );

        try {
            if (livingEntity == null) {
                guiGraphics.renderItem(
                        iconStack,
                        x,
                        y,
                        seed
                );

                guiGraphics.renderItem(
                        MEKits.KIT_ICON_OVERLAY
                                .get()
                                .getDefaultInstance(),
                        x,
                        y,
                        seed
                );
            } else {
                guiGraphics.renderItem(
                        livingEntity,
                        iconStack,
                        x,
                        y,
                        seed
                );

                guiGraphics.renderItem(
                        livingEntity,
                        MEKits.KIT_ICON_OVERLAY
                                .get()
                                .getDefaultInstance(),
                        x,
                        y,
                        seed
                );
            }
        } finally {
            RENDERING_REPLACEMENT.remove();
        }
    }
}