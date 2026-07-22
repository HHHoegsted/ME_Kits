package com.turenidk.mekits.client.screen;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import com.turenidk.mekits.menu.KitPatternEncoderMenu;
import com.turenidk.mekits.network.payload.AdjustIngredientQuantityPayload;
import com.turenidk.mekits.network.payload.ClearEditorStatePayload;
import com.turenidk.mekits.network.payload.EncodePatternPayload;
import com.turenidk.mekits.network.payload.UpdateKitNamePayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public final class KitPatternEncoderScreen
        extends MEStorageScreen<KitPatternEncoderMenu> {

    private static final String DIALOG_TITLE_TEXT_ID =
            "dialog_title";

    /*
     * Pattern names are no longer part of the user-facing workflow.
     *
     * The current server-side pattern format still expects a non-empty
     * compatibility value. This can be removed when the legacy name
     * data component is removed from the encoded-pattern data model.
     */
    private static final String LEGACY_INTERNAL_KIT_NAME =
            "ME Kit";

    private static final int CLEAR_BUTTON_LEFT =
            79;

    private static final int CLEAR_BUTTON_BOTTOM =
            159;

    private static final int ENCODE_BUTTON_LEFT =
            147;

    private static final int ENCODE_BUTTON_BOTTOM =
            145;

    private final Scrollbar ingredientScrollbar;

    private Button encodeButton;

    private Button clearButton;

    public KitPatternEncoderScreen(
            @NotNull KitPatternEncoderMenu menu,
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

        /*
         * AE2's native processing-pattern grid is painted by its
         * ProcessingEncodingPanel rather than by slot semantics.
         *
         * Our specialised terminal has no encoding-mode panel, so it
         * registers the relevant native background slice directly.
         */
        widgets.add(
                "ingredientGridBackground",
                new KitPatternGridBackground()
        );

        ingredientScrollbar =
                new Scrollbar(
                        Scrollbar.SMALL
                );

        ingredientScrollbar.setRange(
                0,
                KitPatternEncoderMenu
                        .INGREDIENT_PAGE_COUNT
                        - 1,
                1
        );

        /*
         * The main terminal scrollbar owns general mouse-wheel input.
         * The ingredient scrollbar reacts only while the pointer is
         * over its own track.
         */
        ingredientScrollbar.setCaptureMouseWheel(
                false
        );

        widgets.add(
                "ingredientScrollbar",
                ingredientScrollbar
        );
    }

    @Override
    public void init() {
        super.init();

        ingredientScrollbar.setCurrentScroll(
                menu.getIngredientPage()
        );

        clearButton =
                Button.builder(
                                Component.literal("×"),
                                button -> sendClearRequest()
                        )
                        .bounds(
                                leftPos
                                        + CLEAR_BUTTON_LEFT,
                                topPos
                                        + imageHeight
                                        - CLEAR_BUTTON_BOTTOM,
                                14,
                                14
                        )
                        .tooltip(
                                Tooltip.create(
                                        Component.translatable(
                                                "screen.mekits.kit_pattern_encoder.clear"
                                        )
                                )
                        )
                        .build();

        addRenderableWidget(
                clearButton
        );

        encodeButton =
                Button.builder(
                                Component.literal("↓"),
                                button -> sendEncodeRequest()
                        )
                        .bounds(
                                leftPos
                                        + ENCODE_BUTTON_LEFT,
                                topPos
                                        + imageHeight
                                        - ENCODE_BUTTON_BOTTOM,
                                18,
                                18
                        )
                        .tooltip(
                                Tooltip.create(
                                        Component.translatable(
                                                "screen.mekits.kit_pattern_encoder.encode"
                                        )
                                )
                        )
                        .build();

        addRenderableWidget(
                encodeButton
        );

        updateControlState();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        /*
         * MEStorageScreen normally replaces the JSON heading with the
         * menu title. The specialised title is too wide beside the
         * native terminal search field.
         */
        setTextContent(
                DIALOG_TITLE_TEXT_ID,
                Component.translatable(
                        "gui.ae2.Terminal"
                )
        );
    }

    @Override
    public void containerTick() {
        super.containerTick();

        updateControlState();
        synchronizeIngredientPage();
    }

    private void synchronizeIngredientPage() {
        int scrollbarPage =
                ingredientScrollbar.getCurrentScroll();

        if (
                scrollbarPage
                        != menu.getIngredientPage()
        ) {
            selectIngredientPage(
                    scrollbarPage
            );
        }
    }

    private void updateControlState() {
        boolean operational =
                menu.isOperational();

        if (encodeButton != null) {
            encodeButton.active =
                    operational;
        }

        if (clearButton != null) {
            clearButton.active =
                    operational;
        }

        ingredientScrollbar.setVisible(
                operational
        );
    }

    private void sendClearRequest() {
        if (!menu.isOperational()) {
            return;
        }

        PacketDistributor.sendToServer(
                ClearEditorStatePayload.INSTANCE
        );
    }

    private void sendEncodeRequest() {
        if (!menu.isOperational()) {
            return;
        }

        PacketDistributor.sendToServer(
                new UpdateKitNamePayload(
                        LEGACY_INTERNAL_KIT_NAME
                )
        );

        PacketDistributor.sendToServer(
                EncodePatternPayload.INSTANCE
        );
    }

    private void selectIngredientPage(
            int requestedPage
    ) {
        if (!menu.isOperational()) {
            ingredientScrollbar.setCurrentScroll(
                    menu.getIngredientPage()
            );

            return;
        }

        int validatedPage =
                Math.max(
                        0,
                        Math.min(
                                requestedPage,
                                KitPatternEncoderMenu
                                        .INGREDIENT_PAGE_COUNT
                                        - 1
                        )
                );

        if (
                validatedPage
                        == menu.getIngredientPage()
        ) {
            ingredientScrollbar.setCurrentScroll(
                    validatedPage
            );

            return;
        }

        menu.setIngredientPage(
                validatedPage
        );

        ingredientScrollbar.setCurrentScroll(
                validatedPage
        );

        if (
                minecraft != null
                        && minecraft.gameMode != null
        ) {
            minecraft.gameMode
                    .handleInventoryButtonClick(
                            menu.containerId,
                            validatedPage
                    );
        }
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        int definitionSlot =
                menu.getIngredientDefinitionSlot(
                        hoveredSlot
                );

        if (
                menu.isOperational()
                        && definitionSlot >= 0
                        && hoveredSlot != null
                        && hoveredSlot.hasItem()
                        && scrollY != 0.0D
        ) {
            int direction =
                    scrollY > 0.0D
                            ? 1
                            : -1;

            PacketDistributor.sendToServer(
                    new AdjustIngredientQuantityPayload(
                            definitionSlot,
                            direction,
                            hasShiftDown()
                    )
            );

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                scrollX,
                scrollY
        );
    }
}