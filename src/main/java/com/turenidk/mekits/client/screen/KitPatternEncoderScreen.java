package com.turenidk.mekits.client.screen;

import com.turenidk.mekits.component.ModDataComponents;
import com.turenidk.mekits.logic.KitPatternEncoderLogic;
import com.turenidk.mekits.menu.KitPatternEncoderMenu;
import com.turenidk.mekits.network.payload.AdjustIngredientQuantityPayload;
import com.turenidk.mekits.network.payload.ClearEditorStatePayload;
import com.turenidk.mekits.network.payload.EncodePatternPayload;
import com.turenidk.mekits.network.payload.UpdateKitNamePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class KitPatternEncoderScreen
        extends AbstractContainerScreen<KitPatternEncoderMenu> {

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

    private static final int RECESS_OUTER =
            0xFF666666;

    private static final int RECESS_INNER =
            0xFFA2A2A2;

    private static final int FIELD_INNER =
            0xFFB8B8B8;

    private static final int LABEL_COLOUR =
            0xFF303030;

    private static final int SCROLLBAR_X =
            23;

    private static final int SCROLLBAR_Y =
            54;

    private static final int SCROLLBAR_WIDTH =
            12;

    private static final int SCROLLBAR_HEIGHT =
            56;

    private static final int SCROLLBAR_THUMB_HEIGHT =
            10;

    private EditBox kitNameField;

    private Button encodeButton;

    private Button clearButton;

    private boolean draggingScrollbar;

    /*
     * The name is not a normal menu slot, so the screen watches the
     * encoded-pattern slot for transitions and copies the synchronized
     * pattern name into the text field when a pattern is inserted.
     *
     * We deliberately do not continuously mirror the pattern name:
     * Clear must be allowed to empty the editor while the encoded
     * pattern remains in the output slot.
     */
    private boolean encodedPatternWasPresent;

    private String observedEncodedPatternName =
            "";

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
        imageHeight = 225;

        titleLabelX = 9;
        titleLabelY = 7;
    }

    @Override
    protected void init() {
        String displayedName =
                kitNameField == null
                        ? menu.getKitName()
                        : kitNameField.getValue();

        super.init();

        kitNameField =
                new EditBox(
                        font,
                        leftPos + 43,
                        topPos + 23,
                        124,
                        16,
                        Component.translatable(
                                "screen.mekits.kit_pattern_encoder.name_label"
                        )
                );

        kitNameField.setMaxLength(
                KitPatternEncoderMenu.MAX_KIT_NAME_LENGTH
        );

        kitNameField.setBordered(
                false
        );

        kitNameField.setTextColor(
                0xFF202020
        );

        kitNameField.setTextColorUneditable(
                0xFF404040
        );

        kitNameField.setTextShadow(
                false
        );

        kitNameField.setValue(
                displayedName
        );

        kitNameField.setResponder(
                this::sendKitNameUpdate
        );

        addRenderableWidget(
                kitNameField
        );

        encodeButton =
                Button.builder(
                                Component.literal("↓"),
                                button -> sendEncodeRequest()
                        )
                        .bounds(
                                leftPos + 145,
                                topPos + 72,
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

        clearButton =
                Button.builder(
                                Component.literal("×"),
                                button -> sendClearRequest()
                        )
                        .bounds(
                                leftPos + 101,
                                topPos + 55,
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

        ItemStack initialEncodedPattern =
                getEncodedPatternStack();

        encodedPatternWasPresent =
                !initialEncodedPattern.isEmpty();

        observedEncodedPatternName =
                getEncodedPatternName(
                        initialEncodedPattern
                );

        updateControlState();

        setInitialFocus(
                kitNameField
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        updateControlState();

        if (kitNameField == null) {
            return;
        }

        ItemStack currentEncodedPattern =
                getEncodedPatternStack();

        boolean encodedPatternIsPresent =
                !currentEncodedPattern.isEmpty();

        String currentEncodedPatternName =
                getEncodedPatternName(
                        currentEncodedPattern
                );

        boolean patternWasInserted =
                encodedPatternIsPresent
                        && !encodedPatternWasPresent;

        boolean insertedPatternChanged =
                encodedPatternIsPresent
                        && encodedPatternWasPresent
                        && !currentEncodedPatternName.equals(
                        observedEncodedPatternName
                );

        if (
                (patternWasInserted || insertedPatternChanged)
                        && !currentEncodedPatternName.isBlank()
        ) {
            kitNameField.setValue(
                    currentEncodedPatternName
            );
        }

        encodedPatternWasPresent =
                encodedPatternIsPresent;

        observedEncodedPatternName =
                currentEncodedPatternName;
    }

    private void updateControlState() {
        boolean operational =
                menu.isOperational();

        if (kitNameField != null) {
            kitNameField.setEditable(
                    operational
            );
        }

        if (encodeButton != null) {
            encodeButton.active =
                    operational;
        }

        if (clearButton != null) {
            clearButton.active =
                    operational;
        }
    }

    private @NotNull ItemStack getEncodedPatternStack() {
        int encodedPatternMenuSlot =
                KitPatternEncoderLogic
                        .ENCODED_PATTERN_SLOT;

        if (
                encodedPatternMenuSlot < 0
                        || encodedPatternMenuSlot
                        >= menu.slots.size()
        ) {
            return ItemStack.EMPTY;
        }

        return menu.getSlot(
                encodedPatternMenuSlot
        ).getItem();
    }

    private @NotNull String getEncodedPatternName(
            @NotNull ItemStack encodedPattern
    ) {
        if (encodedPattern.isEmpty()) {
            return "";
        }

        String encodedPatternName =
                encodedPattern.get(
                        ModDataComponents.KIT_NAME.get()
                );

        return encodedPatternName == null
                ? ""
                : encodedPatternName;
    }

    private void sendKitNameUpdate(
            @NotNull String newKitName
    ) {
        if (!menu.isOperational()) {
            return;
        }

        PacketDistributor.sendToServer(
                new UpdateKitNamePayload(
                        newKitName
                )
        );
    }

    private void sendClearRequest() {
        if (!menu.isOperational()) {
            return;
        }

        if (kitNameField != null) {
            kitNameField.setValue(
                    ""
            );
        }

        PacketDistributor.sendToServer(
                ClearEditorStatePayload.INSTANCE
        );
    }

    private void sendEncodeRequest() {
        if (!menu.isOperational()) {
            return;
        }

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

    private void selectIngredientPage(
            int requestedPage
    ) {
        if (!menu.isOperational()) {
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
            return;
        }

        /*
         * Change the client menu first so incoming synchronized slot
         * contents are written into the correct backing range.
         */
        menu.setIngredientPage(
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

    private void selectIngredientPageFromMouse(
            double mouseY
    ) {
        double relativeY =
                mouseY
                        - (
                        topPos
                                + SCROLLBAR_Y
                );

        double normalizedPosition =
                relativeY
                        / SCROLLBAR_HEIGHT;

        int selectedPage =
                (int) Math.floor(
                        normalizedPosition
                                * KitPatternEncoderMenu
                                .INGREDIENT_PAGE_COUNT
                );

        selectIngredientPage(
                selectedPage
        );
    }

    private boolean isMouseOverScrollbar(
            double mouseX,
            double mouseY
    ) {
        int absoluteLeft =
                leftPos
                        + SCROLLBAR_X;

        int absoluteTop =
                topPos
                        + SCROLLBAR_Y;

        return mouseX >= absoluteLeft
                && mouseX < absoluteLeft
                + SCROLLBAR_WIDTH
                && mouseY >= absoluteTop
                && mouseY < absoluteTop
                + SCROLLBAR_HEIGHT;
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (!menu.isOperational()) {
            return super.keyPressed(
                    keyCode,
                    scanCode,
                    modifiers
            );
        }

        if (
                kitNameField != null
                        && kitNameField.isFocused()
        ) {
            if (keyCode == 256) {
                return super.keyPressed(
                        keyCode,
                        scanCode,
                        modifiers
                );
            }

            if (
                    kitNameField.keyPressed(
                            keyCode,
                            scanCode,
                            modifiers
                    )
            ) {
                return true;
            }

            return kitNameField.canConsumeInput();
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (!menu.isOperational()) {
            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (
                button == 0
                        && isMouseOverScrollbar(
                        mouseX,
                        mouseY
                )
        ) {
            draggingScrollbar =
                    true;

            selectIngredientPageFromMouse(
                    mouseY
            );

            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (!menu.isOperational()) {
            return super.mouseDragged(
                    mouseX,
                    mouseY,
                    button,
                    dragX,
                    dragY
            );
        }

        if (
                button == 0
                        && draggingScrollbar
        ) {
            selectIngredientPageFromMouse(
                    mouseY
            );

            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (
                button == 0
                        && draggingScrollbar
        ) {
            draggingScrollbar =
                    false;

            return true;
        }

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (!menu.isOperational()) {
            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    scrollX,
                    scrollY
            );
        }

        if (
                isMouseOverScrollbar(
                        mouseX,
                        mouseY
                )
                        && scrollY != 0.0D
        ) {
            int pageDirection =
                    scrollY > 0.0D
                            ? -1
                            : 1;

            selectIngredientPage(
                    menu.getIngredientPage()
                            + pageDirection
            );

            return true;
        }

        int definitionSlot =
                menu.getIngredientDefinitionSlot(
                        hoveredSlot
                );

        if (
                definitionSlot < 0
                        || hoveredSlot == null
                        || !hoveredSlot.hasItem()
                        || scrollY == 0.0D
        ) {
            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    scrollX,
                    scrollY
            );
        }

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

    @Override
    protected void renderBg(
            @NotNull GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int left =
                leftPos;

        int top =
                topPos;

        drawTerminalFrame(
                guiGraphics,
                left,
                top
        );

        drawNameFieldRecess(
                guiGraphics,
                left,
                top
        );

        drawDefinitionArea(
                guiGraphics,
                left,
                top
        );

        drawScrollbar(
                guiGraphics,
                left,
                top
        );

        drawEncodingRail(
                guiGraphics,
                left,
                top
        );

        drawInventoryArea(
                guiGraphics,
                left,
                top
        );

        drawAllSlotRecesses(
                guiGraphics,
                left,
                top
        );
    }

    private void drawTerminalFrame(
            @NotNull GuiGraphics guiGraphics,
            int left,
            int top
    ) {
        guiGraphics.fill(
                left - 3,
                top - 3,
                left + imageWidth + 3,
                top + imageHeight + 3,
                PANEL_DARK_SHADOW
        );

        guiGraphics.fill(
                left,
                top,
                left + imageWidth,
                top + imageHeight,
                PANEL_COLOUR
        );

        guiGraphics.fill(
                left,
                top,
                left + imageWidth,
                top + 2,
                PANEL_LIGHT
        );

        guiGraphics.fill(
                left,
                top,
                left + 2,
                top + imageHeight,
                PANEL_LIGHT
        );

        guiGraphics.fill(
                left,
                top + imageHeight - 3,
                left + imageWidth,
                top + imageHeight,
                PANEL_SHADOW
        );

        guiGraphics.fill(
                left + imageWidth - 3,
                top,
                left + imageWidth,
                top + imageHeight,
                PANEL_SHADOW
        );
    }

    private void drawNameFieldRecess(
            @NotNull GuiGraphics guiGraphics,
            int left,
            int top
    ) {
        drawRecess(
                guiGraphics,
                left + 40,
                top + 21,
                left + 169,
                top + 41,
                FIELD_INNER
        );
    }

    private void drawDefinitionArea(
            @NotNull GuiGraphics guiGraphics,
            int left,
            int top
    ) {
        drawRecess(
                guiGraphics,
                left + 38,
                top + 52,
                left + 97,
                top + 111,
                RECESS_INNER
        );
    }

    private void drawScrollbar(
            @NotNull GuiGraphics guiGraphics,
            int left,
            int top
    ) {
        int trackLeft =
                left
                        + SCROLLBAR_X;

        int trackTop =
                top
                        + SCROLLBAR_Y;

        guiGraphics.fill(
                trackLeft,
                trackTop,
                trackLeft + SCROLLBAR_WIDTH,
                trackTop + SCROLLBAR_HEIGHT,
                PANEL_DARK_SHADOW
        );

        guiGraphics.fill(
                trackLeft + 2,
                trackTop + 2,
                trackLeft + SCROLLBAR_WIDTH - 2,
                trackTop + SCROLLBAR_HEIGHT - 2,
                RECESS_INNER
        );

        int availableTravel =
                SCROLLBAR_HEIGHT
                        - SCROLLBAR_THUMB_HEIGHT
                        - 4;

        int pageCountMinusOne =
                KitPatternEncoderMenu
                        .INGREDIENT_PAGE_COUNT
                        - 1;

        int thumbOffset =
                pageCountMinusOne == 0
                        ? 0
                        : availableTravel
                        * menu.getIngredientPage()
                        / pageCountMinusOne;

        int thumbTop =
                trackTop
                        + 2
                        + thumbOffset;

        guiGraphics.fill(
                trackLeft + 2,
                thumbTop,
                trackLeft + SCROLLBAR_WIDTH - 2,
                thumbTop + SCROLLBAR_THUMB_HEIGHT,
                PANEL_SHADOW
        );

        guiGraphics.fill(
                trackLeft + 3,
                thumbTop + 1,
                trackLeft + SCROLLBAR_WIDTH - 3,
                thumbTop + SCROLLBAR_THUMB_HEIGHT - 1,
                PANEL_COLOUR
        );

        guiGraphics.fill(
                trackLeft + 3,
                thumbTop + 1,
                trackLeft + SCROLLBAR_WIDTH - 3,
                thumbTop + 2,
                PANEL_LIGHT
        );

        guiGraphics.fill(
                trackLeft + 3,
                thumbTop + 1,
                trackLeft + 4,
                thumbTop + SCROLLBAR_THUMB_HEIGHT - 1,
                PANEL_LIGHT
        );
    }

    private void drawEncodingRail(
            @NotNull GuiGraphics guiGraphics,
            int left,
            int top
    ) {
        drawRecess(
                guiGraphics,
                left + 141,
                top + 45,
                left + 168,
                top + 115,
                RECESS_INNER
        );
    }

    private void drawInventoryArea(
            @NotNull GuiGraphics guiGraphics,
            int left,
            int top
    ) {
        guiGraphics.fill(
                left + 6,
                top + 127,
                left + imageWidth - 7,
                top + 128,
                PANEL_SHADOW
        );

        guiGraphics.fill(
                left + 6,
                top + 128,
                left + imageWidth - 7,
                top + 129,
                PANEL_LIGHT
        );

        drawRecess(
                guiGraphics,
                left + 5,
                top + 140,
                left + 171,
                top + 220,
                RECESS_INNER
        );
    }

    private void drawAllSlotRecesses(
            @NotNull GuiGraphics guiGraphics,
            int left,
            int top
    ) {
        for (Slot slot : menu.slots) {
            drawSlotRecess(
                    guiGraphics,
                    left + slot.x - 1,
                    top + slot.y - 1
            );
        }
    }

    private void drawSlotRecess(
            @NotNull GuiGraphics guiGraphics,
            int slotLeft,
            int slotTop
    ) {
        guiGraphics.fill(
                slotLeft,
                slotTop,
                slotLeft + 18,
                slotTop + 18,
                RECESS_OUTER
        );

        guiGraphics.fill(
                slotLeft + 1,
                slotTop + 1,
                slotLeft + 17,
                slotTop + 17,
                RECESS_INNER
        );

        guiGraphics.fill(
                slotLeft + 1,
                slotTop + 1,
                slotLeft + 17,
                slotTop + 2,
                PANEL_DARK_SHADOW
        );

        guiGraphics.fill(
                slotLeft + 1,
                slotTop + 1,
                slotLeft + 2,
                slotTop + 17,
                PANEL_DARK_SHADOW
        );

        guiGraphics.fill(
                slotLeft + 2,
                slotTop + 16,
                slotLeft + 17,
                slotTop + 17,
                PANEL_MID_LIGHT
        );

        guiGraphics.fill(
                slotLeft + 16,
                slotTop + 2,
                slotLeft + 17,
                slotTop + 17,
                PANEL_MID_LIGHT
        );
    }

    private void drawRecess(
            @NotNull GuiGraphics guiGraphics,
            int left,
            int top,
            int right,
            int bottom,
            int innerColour
    ) {
        guiGraphics.fill(
                left,
                top,
                right,
                bottom,
                PANEL_SHADOW
        );

        guiGraphics.fill(
                left + 1,
                top + 1,
                right - 1,
                bottom - 1,
                innerColour
        );

        guiGraphics.fill(
                left + 1,
                top + 1,
                right - 1,
                top + 2,
                PANEL_DARK_SHADOW
        );

        guiGraphics.fill(
                left + 1,
                top + 1,
                left + 2,
                bottom - 1,
                PANEL_DARK_SHADOW
        );
    }

    @Override
    protected void renderLabels(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.drawString(
                font,
                title,
                titleLabelX,
                titleLabelY,
                0xFF202020,
                false
        );

        guiGraphics.drawString(
                font,
                Component.translatable(
                        "screen.mekits.kit_pattern_encoder.name_label"
                ),
                8,
                27,
                LABEL_COLOUR,
                false
        );

        if (!menu.isPowered()) {
            Component outOfPower =
                    Component.translatable(
                            "screen.mekits.kit_pattern_encoder.out_of_power"
                    );

            guiGraphics.drawString(
                    font,
                    outOfPower,
                    (
                            imageWidth
                                    - font.width(
                                    outOfPower
                            )
                    ) / 2,
                    117,
                    0xFFFF5555,
                    false
            );
        }
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