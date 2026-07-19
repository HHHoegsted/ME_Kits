package com.turenidk.mekits.menu;

import appeng.menu.AEBaseMenu;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.logic.KitPatternEncoderHost;
import com.turenidk.mekits.logic.KitPatternEncoderLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitPatternEncoderMenu
        extends AEBaseMenu {

    public static final int MAX_KIT_NAME_LENGTH = 64;

    public static final int VISIBLE_INGREDIENT_SLOT_COUNT = 9;

    public static final int INGREDIENT_PAGE_STRIDE = 3;

    public static final int INGREDIENT_PAGE_COUNT =
            (
                    KitPatternEncoderLogic.MAX_INGREDIENT_DEFINITIONS
                            - VISIBLE_INGREDIENT_SLOT_COUNT
            )
                    / INGREDIENT_PAGE_STRIDE
                    + 1;

    private static final int ENCODER_SLOT_COUNT = 2;

    private static final int GHOST_SLOT_START =
            ENCODER_SLOT_COUNT;

    private static final int GHOST_SLOT_END =
            GHOST_SLOT_START
                    + VISIBLE_INGREDIENT_SLOT_COUNT;

    private static final int PLAYER_INVENTORY_START =
            GHOST_SLOT_END;

    private static final int PLAYER_INVENTORY_END =
            PLAYER_INVENTORY_START + 27;

    private static final int HOTBAR_START =
            PLAYER_INVENTORY_END;

    private static final int HOTBAR_END =
            HOTBAR_START + 9;

    @NotNull
    private final KitPatternEncoderLogic encoderLogic;

    @NotNull
    private String kitName;

    private final DataSlot ingredientPage =
            DataSlot.standalone();

    public KitPatternEncoderMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @NotNull KitPatternEncoderHost host
    ) {
        super(
                MEKits.KIT_PATTERN_ENCODER_MENU.get(),
                containerId,
                playerInventory,
                host
        );

        this.encoderLogic =
                host.getEncoderLogic();

        this.kitName =
                encoderLogic.getKitName();

        ingredientPage.set(
                0
        );

        addEncoderSlots(
                encoderLogic.getPatternItemHandler()
        );

        addGhostIngredientSlots(
                new PagedIngredientHandler(
                        requireModifiableIngredientHandler(
                                encoderLogic
                                        .getIngredientDefinitionHandler()
                        )
                )
        );

        addPlayerInventory(
                playerInventory
        );

        addPlayerHotbar(
                playerInventory
        );

        addDataSlot(
                ingredientPage
        );
    }

    private static @NotNull IItemHandlerModifiable
    requireModifiableIngredientHandler(
            @NotNull IItemHandler handler
    ) {
        if (
                handler
                        instanceof IItemHandlerModifiable
                        modifiableHandler
        ) {
            return modifiableHandler;
        }

        throw new IllegalStateException(
                "Kit Pattern Encoder ingredient handler "
                        + "must be modifiable"
        );
    }

    public @NotNull String getKitName() {
        return kitName;
    }

    public void setInitialKitName(
            @NotNull String initialKitName
    ) {
        this.kitName =
                initialKitName;
    }

    public int getIngredientPage() {
        return ingredientPage.get();
    }

    public void setIngredientPage(
            int requestedPage
    ) {
        ingredientPage.set(
                clampIngredientPage(
                        requestedPage
                )
        );
    }

    private int clampIngredientPage(
            int requestedPage
    ) {
        return Math.max(
                0,
                Math.min(
                        requestedPage,
                        INGREDIENT_PAGE_COUNT - 1
                )
        );
    }

    private int getFirstDefinitionSlotForPage(
            int page
    ) {
        return clampIngredientPage(
                page
        ) * INGREDIENT_PAGE_STRIDE;
    }

    private int getDefinitionSlotForVisibleSlot(
            int visibleSlot
    ) {
        if (
                visibleSlot < 0
                        || visibleSlot
                        >= VISIBLE_INGREDIENT_SLOT_COUNT
        ) {
            return -1;
        }

        return getFirstDefinitionSlotForPage(
                getIngredientPage()
        ) + visibleSlot;
    }

    @Override
    public boolean clickMenuButton(
            @NotNull Player player,
            int buttonId
    ) {
        if (
                buttonId < 0
                        || buttonId >= INGREDIENT_PAGE_COUNT
        ) {
            return false;
        }

        int previousPage =
                getIngredientPage();

        setIngredientPage(
                buttonId
        );

        if (
                getIngredientPage()
                        != previousPage
        ) {
            broadcastFullState();
        }

        return true;
    }

    public int getIngredientDefinitionSlot(
            @Nullable Slot menuSlot
    ) {
        if (menuSlot == null) {
            return -1;
        }

        int menuSlotIndex =
                slots.indexOf(
                        menuSlot
                );

        if (
                menuSlotIndex < GHOST_SLOT_START
                        || menuSlotIndex >= GHOST_SLOT_END
        ) {
            return -1;
        }

        int visibleSlot =
                menuSlotIndex
                        - GHOST_SLOT_START;

        return getDefinitionSlotForVisibleSlot(
                visibleSlot
        );
    }

    public void updateKitName(
            @NotNull String newKitName
    ) {
        encoderLogic.setKitName(
                newKitName
        );

        kitName =
                encoderLogic.getKitName();
    }

    public boolean adjustIngredientQuantity(
            int definitionSlot,
            int direction,
            boolean jumpToLimit
    ) {
        boolean changed =
                encoderLogic
                        .adjustIngredientDefinitionQuantity(
                                definitionSlot,
                                direction,
                                jumpToLimit
                        );

        if (changed) {
            broadcastChanges();
        }

        return changed;
    }

    public boolean clearEditorState() {
        boolean changed =
                encoderLogic.clearEditorState();

        if (changed) {
            kitName =
                    encoderLogic.getKitName();

            broadcastFullState();
        }

        return changed;
    }

    public @NotNull KitPatternEncoderLogic.EncodeResult
    encodePattern() {
        return encoderLogic.encodePattern();
    }

    private void addEncoderSlots(
            @NotNull IItemHandler patternItemHandler
    ) {
        addSlot(
                new SlotItemHandler(
                        patternItemHandler,
                        KitPatternEncoderLogic.BLANK_PATTERN_SLOT,
                        145,
                        49
                )
        );

        addSlot(
                new SlotItemHandler(
                        patternItemHandler,
                        KitPatternEncoderLogic.ENCODED_PATTERN_SLOT,
                        145,
                        93
                )
        );
    }

    private void addGhostIngredientSlots(
            @NotNull IItemHandlerModifiable pagedIngredientHandler
    ) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int visibleSlot =
                        column
                                + row * 3;

                addSlot(
                        new GhostIngredientSlot(
                                pagedIngredientHandler,
                                visibleSlot,
                                41 + column * 18,
                                55 + row * 18
                        )
                );
            }
        }
    }

    private void addPlayerInventory(
            @NotNull Inventory playerInventory
    ) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(
                        new Slot(
                                playerInventory,
                                column
                                        + row * 9
                                        + 9,
                                8 + column * 18,
                                143 + row * 18
                        )
                );
            }
        }
    }

    private void addPlayerHotbar(
            @NotNull Inventory playerInventory
    ) {
        for (int column = 0; column < 9; column++) {
            addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            8 + column * 18,
                            201
                    )
            );
        }
    }

    @Override
    public void clicked(
            int slotId,
            int button,
            @NotNull ClickType clickType,
            @NotNull Player player
    ) {
        if (
                slotId >= GHOST_SLOT_START
                        && slotId < GHOST_SLOT_END
        ) {
            if (
                    clickType != ClickType.PICKUP
                            || (
                            button != 0
                                    && button != 1
                    )
            ) {
                return;
            }

            int visibleSlot =
                    slotId
                            - GHOST_SLOT_START;

            int definitionSlot =
                    getDefinitionSlotForVisibleSlot(
                            visibleSlot
                    );

            if (definitionSlot < 0) {
                return;
            }

            ItemStack carriedStack =
                    getCarried();

            if (carriedStack.isEmpty()) {
                encoderLogic.clearIngredientDefinition(
                        definitionSlot
                );
            } else {
                int quantity =
                        button == 1
                                ? 1
                                : carriedStack.getCount();

                encoderLogic.setIngredientDefinition(
                        definitionSlot,
                        carriedStack,
                        quantity
                );
            }

            broadcastChanges();
            return;
        }

        super.clicked(
                slotId,
                button,
                clickType,
                player
        );
    }

    @Override
    public @NotNull ItemStack quickMoveStack(
            @NotNull Player player,
            int clickedSlotIndex
    ) {
        if (
                clickedSlotIndex < 0
                        || clickedSlotIndex >= slots.size()
        ) {
            return ItemStack.EMPTY;
        }

        if (
                clickedSlotIndex >= GHOST_SLOT_START
                        && clickedSlotIndex < GHOST_SLOT_END
        ) {
            return ItemStack.EMPTY;
        }

        Slot clickedSlot =
                slots.get(
                        clickedSlotIndex
                );

        if (!clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack clickedStack =
                clickedSlot.getItem();

        ItemStack originalStack =
                clickedStack.copy();

        if (
                clickedSlotIndex
                        < ENCODER_SLOT_COUNT
        ) {
            if (
                    !moveItemStackTo(
                            clickedStack,
                            PLAYER_INVENTORY_START,
                            HOTBAR_END,
                            true
                    )
            ) {
                return ItemStack.EMPTY;
            }
        } else if (
                clickedStack.is(
                        MEKits.BLANK_ME_KIT_PATTERN.get()
                )
        ) {
            if (
                    !moveItemStackTo(
                            clickedStack,
                            KitPatternEncoderLogic.BLANK_PATTERN_SLOT,
                            KitPatternEncoderLogic.BLANK_PATTERN_SLOT
                                    + 1,
                            false
                    )
            ) {
                return ItemStack.EMPTY;
            }
        } else if (
                clickedStack.is(
                        MEKits.ENCODED_ME_KIT_PATTERN.get()
                )
        ) {
            if (
                    !moveItemStackTo(
                            clickedStack,
                            KitPatternEncoderLogic.ENCODED_PATTERN_SLOT,
                            KitPatternEncoderLogic.ENCODED_PATTERN_SLOT
                                    + 1,
                            false
                    )
            ) {
                return ItemStack.EMPTY;
            }
        } else if (
                clickedSlotIndex
                        < PLAYER_INVENTORY_END
        ) {
            if (
                    !moveItemStackTo(
                            clickedStack,
                            HOTBAR_START,
                            HOTBAR_END,
                            false
                    )
            ) {
                return ItemStack.EMPTY;
            }
        } else if (
                !moveItemStackTo(
                        clickedStack,
                        PLAYER_INVENTORY_START,
                        PLAYER_INVENTORY_END,
                        false
                )
        ) {
            return ItemStack.EMPTY;
        }

        if (clickedStack.isEmpty()) {
            clickedSlot.set(
                    ItemStack.EMPTY
            );
        } else {
            clickedSlot.setChanged();
        }

        if (
                clickedStack.getCount()
                        == originalStack.getCount()
        ) {
            return ItemStack.EMPTY;
        }

        clickedSlot.onTake(
                player,
                clickedStack
        );

        return originalStack;
    }

    private final class PagedIngredientHandler
            implements IItemHandlerModifiable {

        private final IItemHandlerModifiable backingHandler;

        private PagedIngredientHandler(
                @NotNull IItemHandlerModifiable backingHandler
        ) {
            this.backingHandler =
                    backingHandler;
        }

        private int getBackingSlot(
                int visibleSlot
        ) {
            int definitionSlot =
                    getDefinitionSlotForVisibleSlot(
                            visibleSlot
                    );

            if (definitionSlot < 0) {
                throw new IndexOutOfBoundsException(
                        "Visible ingredient slot "
                                + visibleSlot
                                + " is outside 0-"
                                + (
                                VISIBLE_INGREDIENT_SLOT_COUNT
                                        - 1
                        )
                );
            }

            return definitionSlot;
        }

        @Override
        public void setStackInSlot(
                int slot,
                @NotNull ItemStack stack
        ) {
            backingHandler.setStackInSlot(
                    getBackingSlot(slot),
                    stack
            );
        }

        @Override
        public int getSlots() {
            return VISIBLE_INGREDIENT_SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(
                int slot
        ) {
            return backingHandler.getStackInSlot(
                    getBackingSlot(slot)
            );
        }

        @Override
        public @NotNull ItemStack insertItem(
                int slot,
                @NotNull ItemStack stack,
                boolean simulate
        ) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(
                int slot,
                int amount,
                boolean simulate
        ) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(
                int slot
        ) {
            return backingHandler.getSlotLimit(
                    getBackingSlot(slot)
            );
        }

        @Override
        public boolean isItemValid(
                int slot,
                @NotNull ItemStack stack
        ) {
            return false;
        }
    }

    private static final class GhostIngredientSlot
            extends SlotItemHandler {

        private GhostIngredientSlot(
                @NotNull IItemHandler itemHandler,
                int slot,
                int x,
                int y
        ) {
            super(
                    itemHandler,
                    slot,
                    x,
                    y
            );
        }

        @Override
        public boolean mayPlace(
                @NotNull ItemStack stack
        ) {
            return false;
        }

        @Override
        public boolean mayPickup(
                @NotNull Player player
        ) {
            return false;
        }
    }
}