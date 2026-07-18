package com.turenidk.mekits.menu;

import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.blockentity.KitPatternEncoderBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitPatternEncoderMenu extends AbstractContainerMenu {

    public static final int MAX_KIT_NAME_LENGTH = 64;

    private static final int ENCODER_SLOT_COUNT = 2;

    private static final int GHOST_SLOT_START =
            ENCODER_SLOT_COUNT;

    private static final int GHOST_SLOT_END =
            GHOST_SLOT_START
                    + KitPatternEncoderBlockEntity
                    .MAX_INGREDIENT_DEFINITIONS;

    private static final int PLAYER_INVENTORY_START =
            GHOST_SLOT_END;

    private static final int PLAYER_INVENTORY_END =
            PLAYER_INVENTORY_START + 27;

    private static final int HOTBAR_START =
            PLAYER_INVENTORY_END;

    private static final int HOTBAR_END =
            HOTBAR_START + 9;

    private final ContainerLevelAccess access;

    @Nullable
    private final KitPatternEncoderBlockEntity encoder;

    private final String kitName;

    public KitPatternEncoderMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @NotNull RegistryFriendlyByteBuf extraData
    ) {
        this(
                containerId,
                playerInventory,
                createClientPatternHandler(),
                createClientIngredientHandler(),
                ContainerLevelAccess.NULL,
                null,
                extraData.readUtf(MAX_KIT_NAME_LENGTH)
        );
    }

    public KitPatternEncoderMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @NotNull KitPatternEncoderBlockEntity encoder
    ) {
        this(
                containerId,
                playerInventory,
                encoder.getPatternItemHandler(),
                encoder.getIngredientDefinitionHandler(),
                encoder.getLevel() == null
                        ? ContainerLevelAccess.NULL
                        : ContainerLevelAccess.create(
                        encoder.getLevel(),
                        encoder.getBlockPos()
                ),
                encoder,
                encoder.getKitName()
        );
    }

    private KitPatternEncoderMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @NotNull IItemHandler patternItemHandler,
            @NotNull IItemHandler ingredientDefinitionHandler,
            @NotNull ContainerLevelAccess access,
            @Nullable KitPatternEncoderBlockEntity encoder,
            @NotNull String kitName
    ) {
        super(
                MEKits.KIT_PATTERN_ENCODER_MENU.get(),
                containerId
        );

        this.access = access;
        this.encoder = encoder;
        this.kitName = kitName;

        addEncoderSlots(patternItemHandler);
        addGhostIngredientSlots(
                ingredientDefinitionHandler
        );
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    public @NotNull String getKitName() {
        return kitName;
    }

    public void updateKitName(
            @NotNull String newKitName
    ) {
        if (encoder == null) {
            return;
        }

        encoder.setKitName(
                newKitName
        );
    }

    public @NotNull KitPatternEncoderBlockEntity.EncodeResult
    encodePattern() {
        if (encoder == null) {
            return KitPatternEncoderBlockEntity
                    .EncodeResult
                    .INTERNAL_ERROR;
        }

        return encoder.encodePattern();
    }

    private void addEncoderSlots(
            @NotNull IItemHandler patternItemHandler
    ) {
        addSlot(
                new SlotItemHandler(
                        patternItemHandler,
                        KitPatternEncoderBlockEntity.BLANK_PATTERN_SLOT,
                        44,
                        20
                )
        );

        addSlot(
                new SlotItemHandler(
                        patternItemHandler,
                        KitPatternEncoderBlockEntity.ENCODED_PATTERN_SLOT,
                        116,
                        20
                )
        );
    }

    private void addGhostIngredientSlots(
            @NotNull IItemHandler ingredientDefinitionHandler
    ) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int definitionSlot =
                        column + row * 9;

                addSlot(
                        new GhostIngredientSlot(
                                ingredientDefinitionHandler,
                                definitionSlot,
                                8 + column * 18,
                                63 + row * 18
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
                                column + row * 9 + 9,
                                8 + column * 18,
                                127 + row * 18
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
                            185
                    )
            );
        }
    }

    private static @NotNull IItemHandler
    createClientPatternHandler() {
        return new ItemStackHandler(
                KitPatternEncoderBlockEntity.SLOT_COUNT
        ) {
            @Override
            public boolean isItemValid(
                    int slot,
                    @NotNull ItemStack stack
            ) {
                return switch (slot) {
                    case KitPatternEncoderBlockEntity.BLANK_PATTERN_SLOT ->
                            stack.is(
                                    MEKits.BLANK_ME_KIT_PATTERN.get()
                            );

                    case KitPatternEncoderBlockEntity.ENCODED_PATTERN_SLOT ->
                            stack.is(
                                    MEKits.ENCODED_ME_KIT_PATTERN.get()
                            );

                    default -> false;
                };
            }

            @Override
            public int getSlotLimit(int slot) {
                return switch (slot) {
                    case KitPatternEncoderBlockEntity.BLANK_PATTERN_SLOT ->
                            64;

                    case KitPatternEncoderBlockEntity.ENCODED_PATTERN_SLOT ->
                            1;

                    default -> 0;
                };
            }
        };
    }

    private static @NotNull IItemHandler
    createClientIngredientHandler() {
        return new ItemStackHandler(
                KitPatternEncoderBlockEntity
                        .MAX_INGREDIENT_DEFINITIONS
        );
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
                            || (button != 0 && button != 1)
            ) {
                return;
            }

            if (encoder == null) {
                return;
            }

            int definitionSlot =
                    slotId - GHOST_SLOT_START;

            ItemStack carriedStack =
                    getCarried();

            if (carriedStack.isEmpty()) {
                encoder.clearIngredientDefinition(
                        definitionSlot
                );
            } else {
                int quantity =
                        button == 1
                                ? 1
                                : carriedStack.getCount();

                encoder.setIngredientDefinition(
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
    public boolean stillValid(
            @NotNull Player player
    ) {
        return stillValid(
                access,
                player,
                MEKits.KIT_PATTERN_ENCODER.get()
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
                slots.get(clickedSlotIndex);

        if (!clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack clickedStack =
                clickedSlot.getItem();

        ItemStack originalStack =
                clickedStack.copy();

        if (clickedSlotIndex < ENCODER_SLOT_COUNT) {
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
                            KitPatternEncoderBlockEntity.BLANK_PATTERN_SLOT,
                            KitPatternEncoderBlockEntity.BLANK_PATTERN_SLOT + 1,
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
                            KitPatternEncoderBlockEntity.ENCODED_PATTERN_SLOT,
                            KitPatternEncoderBlockEntity.ENCODED_PATTERN_SLOT + 1,
                            false
                    )
            ) {
                return ItemStack.EMPTY;
            }
        } else if (
                clickedSlotIndex < PLAYER_INVENTORY_END
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