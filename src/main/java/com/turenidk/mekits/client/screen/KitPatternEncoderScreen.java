package com.turenidk.mekits.client.screen;

import com.turenidk.mekits.menu.KitPatternEncoderMenu;
import com.turenidk.mekits.network.payload.EncodePatternPayload;
import com.turenidk.mekits.network.payload.UpdateKitNamePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class KitPatternEncoderScreen
        extends AbstractContainerScreen<KitPatternEncoderMenu> {

    private EditBox kitNameField;

    public KitPatternEncoderScreen(
            @NotNull KitPatternEncoderMenu menu,
            @NotNull Inventory playerInventory,
            @NotNull Component title
    ) {
        super(
                menu,
                playerInventory,
                title
        );

        imageWidth = 176;
        imageHeight = 209;

        titleLabelX = 8;
        titleLabelY = 6;

        inventoryLabelX = 8;
        inventoryLabelY = 115;
    }

    @Override
    protected void init() {
        /*
         * Preserve locally typed text if Minecraft reinitializes
         * the screen, for example after resizing the window.
         */
        String displayedName =
                kitNameField == null
                        ? menu.getKitName()
                        : kitNameField.getValue();

        super.init();

        kitNameField =
                new EditBox(
                        font,
                        leftPos + 42,
                        topPos + 39,
                        126,
                        18,
                        Component.translatable(
                                "screen.mekits.kit_pattern_encoder.name_label"
                        )
                );

        kitNameField.setMaxLength(
                KitPatternEncoderMenu.MAX_KIT_NAME_LENGTH
        );

        /*
         * Set the initial value before installing the responder,
         * so opening the screen does not send an unnecessary packet.
         */
        kitNameField.setValue(
                displayedName
        );

        kitNameField.setResponder(
                this::sendKitNameUpdate
        );

        addRenderableWidget(
                kitNameField
        );

        addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "screen.mekits.kit_pattern_encoder.encode"
                                ),
                                button -> sendEncodeRequest()
                        )
                        .bounds(
                                leftPos + 63,
                                topPos + 18,
                                50,
                                20
                        )
                        .build()
        );

        setInitialFocus(
                kitNameField
        );
    }

    private void sendKitNameUpdate(
            @NotNull String newKitName
    ) {
        PacketDistributor.sendToServer(
                new UpdateKitNamePayload(
                        newKitName
                )
        );
    }

    private void sendEncodeRequest() {
        /*
         * Send the current field value once more immediately before
         * the encode request. Both packets travel in order, ensuring
         * the encode operation uses the text currently displayed.
         */
        if (kitNameField != null) {
            PacketDistributor.sendToServer(
                    new UpdateKitNamePayload(
                            kitNameField.getValue()
                    )
            );
        }

        PacketDistributor.sendToServer(
                EncodePatternPayload.INSTANCE
        );
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (
                kitNameField != null
                        && kitNameField.isFocused()
        ) {
            /*
             * Escape retains its normal screen-closing behaviour.
             */
            if (keyCode == 256) {
                return super.keyPressed(
                        keyCode,
                        scanCode,
                        modifiers
                );
            }

            /*
             * Give the focused text box first opportunity to handle
             * navigation, deletion, clipboard shortcuts, and similar
             * key presses.
             */
            if (
                    kitNameField.keyPressed(
                            keyCode,
                            scanCode,
                            modifiers
                    )
            ) {
                return true;
            }

            /*
             * Ordinary letters are added through charTyped rather
             * than keyPressed. Returning true here prevents keys such
             * as E from activating Minecraft's inventory shortcut.
             */
            return kitNameField.canConsumeInput();
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    @Override
    protected void renderBg(
            @NotNull GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int left = leftPos;
        int top = topPos;

        guiGraphics.fill(
                left,
                top,
                left + imageWidth,
                top + imageHeight,
                0xFFC6C6C6
        );

        guiGraphics.fill(
                left,
                top,
                left + imageWidth,
                top + 1,
                0xFFFFFFFF
        );

        guiGraphics.fill(
                left,
                top,
                left + 1,
                top + imageHeight,
                0xFFFFFFFF
        );

        guiGraphics.fill(
                left,
                top + imageHeight - 1,
                left + imageWidth,
                top + imageHeight,
                0xFF555555
        );

        guiGraphics.fill(
                left + imageWidth - 1,
                top,
                left + imageWidth,
                top + imageHeight,
                0xFF555555
        );

        for (Slot slot : menu.slots) {
            int slotLeft =
                    left + slot.x - 1;

            int slotTop =
                    top + slot.y - 1;

            guiGraphics.fill(
                    slotLeft,
                    slotTop,
                    slotLeft + 18,
                    slotTop + 18,
                    0xFF555555
            );

            guiGraphics.fill(
                    slotLeft + 1,
                    slotTop + 1,
                    slotLeft + 17,
                    slotTop + 17,
                    0xFF8B8B8B
            );
        }
    }

    @Override
    protected void renderLabels(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        super.renderLabels(
                guiGraphics,
                mouseX,
                mouseY
        );

        guiGraphics.drawString(
                font,
                Component.translatable(
                        "screen.mekits.kit_pattern_encoder.name_label"
                ),
                8,
                44,
                0x404040,
                false
        );
    }

    @Override
    public void render(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                guiGraphics,
                mouseX,
                mouseY
        );
    }
}