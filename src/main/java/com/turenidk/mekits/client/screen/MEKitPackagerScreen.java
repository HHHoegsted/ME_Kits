package com.turenidk.mekits.client.screen;

import com.turenidk.mekits.blockentity.MEKitPackagerBlockEntity;
import com.turenidk.mekits.menu.MEKitPackagerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class MEKitPackagerScreen
        extends AbstractContainerScreen<MEKitPackagerMenu> {

    private static final int PANEL_COLOUR =
            0xFFD0D0D0;

    private static final int PANEL_LIGHT =
            0xFFFFFFFF;

    private static final int PANEL_MID_LIGHT =
            0xFFE7E7E7;

    private static final int PANEL_SHADOW =
            0xFF666666;

    private static final int PANEL_DARK_SHADOW =
            0xFF2B2B2B;

    private static final int SLOT_BACKGROUND =
            0xFF8B8B8B;

    private static final int LOCKED_SLOT_BACKGROUND =
            0xFF707070;

    private static final int LOCKED_SLOT_MARK =
            0xFF4A4A4A;

    private static final int LABEL_COLOUR =
            0xFF303030;

    private static final int GHOST_CARD_DARK =
            0xFF686868;

    private static final int GHOST_CARD_LIGHT =
            0xFFA0A0A0;

    private static final int PATTERN_SLOT_COUNT =
            MEKitPackagerBlockEntity.MAX_PATTERN_SLOT_COUNT;

    public MEKitPackagerScreen(
            @NotNull MEKitPackagerMenu menu,
            @NotNull Inventory playerInventory,
            @NotNull Component title
    ) {
        super(
                menu,
                playerInventory,
                title
        );

        imageWidth = 200;
        imageHeight = 188;

        titleLabelX = 8;
        titleLabelY = 7;

        inventoryLabelX = 8;
        inventoryLabelY = 95;
    }

    @Override
    protected void renderBg(
            @NotNull GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        drawRaisedPanel(
                graphics,
                leftPos,
                topPos,
                imageWidth,
                imageHeight
        );

        drawPatternSlots(
                graphics
        );

        drawUpgradeSlots(
                graphics
        );

        drawPlayerSlots(
                graphics
        );
    }

    private void drawPatternSlots(
            @NotNull GuiGraphics graphics
    ) {
        for (
                int slotIndex = 0;
                slotIndex < PATTERN_SLOT_COUNT;
                slotIndex++
        ) {
            Slot slot =
                    menu.getSlot(slotIndex);

            boolean unlocked =
                    menu.isPatternSlotUnlocked(
                            slotIndex
                    );

            drawMachineSlot(
                    graphics,
                    leftPos + slot.x - 1,
                    topPos + slot.y - 1,
                    unlocked
            );

            if (!unlocked) {
                drawLockedSlotMark(
                        graphics,
                        leftPos + slot.x,
                        topPos + slot.y
                );
            }
        }
    }

    private void drawUpgradeSlots(
            @NotNull GuiGraphics graphics
    ) {
        for (
                int slotIndex =
                MEKitPackagerMenu.UPGRADE_SLOT_START;
                slotIndex
                        < MEKitPackagerMenu.UPGRADE_SLOT_END;
                slotIndex++
        ) {
            Slot slot =
                    menu.getSlot(slotIndex);

            drawMachineSlot(
                    graphics,
                    leftPos + slot.x - 1,
                    topPos + slot.y - 1,
                    true
            );

            if (slot.getItem().isEmpty()) {
                drawCapacityCardGhost(
                        graphics,
                        leftPos + slot.x,
                        topPos + slot.y
                );
            }
        }
    }

    private void drawPlayerSlots(
            @NotNull GuiGraphics graphics
    ) {
        for (
                int slotIndex =
                MEKitPackagerMenu.UPGRADE_SLOT_END;
                slotIndex < menu.slots.size();
                slotIndex++
        ) {
            Slot slot =
                    menu.getSlot(slotIndex);

            drawMachineSlot(
                    graphics,
                    leftPos + slot.x - 1,
                    topPos + slot.y - 1,
                    true
            );
        }
    }

    @Override
    protected void renderLabels(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawString(
                font,
                title,
                titleLabelX,
                titleLabelY,
                LABEL_COLOUR,
                false
        );

        graphics.drawString(
                font,
                playerInventoryTitle,
                inventoryLabelX,
                inventoryLabelY,
                LABEL_COLOUR,
                false
        );
    }

    @Override
    public void render(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }

    private void drawRaisedPanel(
            @NotNull GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                PANEL_COLOUR
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                PANEL_LIGHT
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                PANEL_LIGHT
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + width - 1,
                y + 2,
                PANEL_MID_LIGHT
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + 2,
                y + height - 1,
                PANEL_MID_LIGHT
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                PANEL_DARK_SHADOW
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                PANEL_DARK_SHADOW
        );

        graphics.fill(
                x + 1,
                y + height - 2,
                x + width - 1,
                y + height - 1,
                PANEL_SHADOW
        );

        graphics.fill(
                x + width - 2,
                y + 1,
                x + width - 1,
                y + height - 1,
                PANEL_SHADOW
        );
    }

    private void drawMachineSlot(
            @NotNull GuiGraphics graphics,
            int x,
            int y,
            boolean enabled
    ) {
        graphics.fill(
                x,
                y,
                x + 18,
                y + 18,
                PANEL_DARK_SHADOW
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + 18,
                y + 18,
                PANEL_SHADOW
        );

        graphics.fill(
                x + 2,
                y + 2,
                x + 17,
                y + 17,
                enabled
                        ? SLOT_BACKGROUND
                        : LOCKED_SLOT_BACKGROUND
        );

        graphics.fill(
                x + 2,
                y + 2,
                x + 17,
                y + 3,
                PANEL_DARK_SHADOW
        );

        graphics.fill(
                x + 2,
                y + 2,
                x + 3,
                y + 17,
                PANEL_DARK_SHADOW
        );

        graphics.fill(
                x + 3,
                y + 16,
                x + 17,
                y + 17,
                PANEL_LIGHT
        );

        graphics.fill(
                x + 16,
                y + 3,
                x + 17,
                y + 17,
                PANEL_LIGHT
        );
    }

    private void drawLockedSlotMark(
            @NotNull GuiGraphics graphics,
            int x,
            int y
    ) {
        graphics.fill(
                x + 4,
                y + 7,
                x + 12,
                y + 9,
                LOCKED_SLOT_MARK
        );

        graphics.fill(
                x + 7,
                y + 4,
                x + 9,
                y + 12,
                LOCKED_SLOT_MARK
        );
    }

    private void drawCapacityCardGhost(
            @NotNull GuiGraphics graphics,
            int x,
            int y
    ) {
        /*
         * A deliberately abstract Capacity Card silhouette.
         * No real item is rendered, so an empty slot cannot
         * be mistaken for an installed card.
         */

        graphics.fill(
                x + 4,
                y + 2,
                x + 12,
                y + 14,
                GHOST_CARD_DARK
        );

        graphics.fill(
                x + 5,
                y + 3,
                x + 11,
                y + 12,
                GHOST_CARD_LIGHT
        );

        graphics.fill(
                x + 6,
                y + 5,
                x + 10,
                y + 7,
                GHOST_CARD_DARK
        );

        graphics.fill(
                x + 6,
                y + 9,
                x + 10,
                y + 11,
                GHOST_CARD_DARK
        );

        graphics.fill(
                x + 5,
                y + 13,
                x + 7,
                y + 15,
                GHOST_CARD_DARK
        );

        graphics.fill(
                x + 9,
                y + 13,
                x + 11,
                y + 15,
                GHOST_CARD_DARK
        );
    }
}