package com.turenidk.mekits.menu;

import appeng.core.definitions.AEItems;
import appeng.menu.AEBaseMenu;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.blockentity.MEKitPackagerBlockEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MEKitPackagerMenu extends AEBaseMenu {

    public static final int PATTERN_SLOT_START = 0;

    public static final int PATTERN_SLOT_END =
            PATTERN_SLOT_START
                    + MEKitPackagerBlockEntity.MAX_PATTERN_SLOT_COUNT;

    public static final int UPGRADE_SLOT_START =
            PATTERN_SLOT_END;

    public static final int UPGRADE_SLOT_END =
            UPGRADE_SLOT_START
                    + MEKitPackagerBlockEntity.UPGRADE_SLOT_COUNT;

    private final MEKitPackagerBlockEntity packager;

    private final DataSlot accessiblePatternSlots =
            DataSlot.standalone();

    public MEKitPackagerMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @NotNull MEKitPackagerBlockEntity packager
    ) {
        super(
                MEKits.ME_KIT_PACKAGER_MENU.get(),
                containerId,
                playerInventory,
                packager
        );

        this.packager = packager;

        updateAccessiblePatternSlots();

        addPatternSlots();
        addUpgradeSlots();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlot(
                accessiblePatternSlots
        );
    }

    public int getAccessiblePatternSlotCount() {
        return accessiblePatternSlots.get();
    }

    public boolean isPatternSlotUnlocked(
            int patternSlot
    ) {
        return patternSlot >= 0
                && patternSlot
                < getAccessiblePatternSlotCount();
    }

    private boolean isClientMenu() {
        return getPlayer().level().isClientSide();
    }

    private void updateAccessiblePatternSlots() {
        accessiblePatternSlots.set(
                packager.getAccessiblePatternSlotCount()
        );
    }

    @Override
    public void broadcastChanges() {
        updateAccessiblePatternSlots();
        super.broadcastChanges();
    }

    @Override
    public void broadcastFullState() {
        updateAccessiblePatternSlots();
        super.broadcastFullState();
    }

    private void addPatternSlots() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int patternSlot =
                        column + row * 9;

                addSlot(
                        new PatternSlot(
                                patternSlot,
                                8 + column * 18,
                                34 + row * 18
                        )
                );
            }
        }
    }

    private void addUpgradeSlots() {
        for (
                int upgradeSlot = 0;
                upgradeSlot
                        < MEKitPackagerBlockEntity.UPGRADE_SLOT_COUNT;
                upgradeSlot++
        ) {
            addSlot(
                    new CapacityCardSlot(
                            upgradeSlot,
                            174,
                            34 + upgradeSlot * 18
                    )
            );
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
                                106 + row * 18
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
                            164
                    )
            );
        }
    }

    private final class PatternSlot extends Slot {

        private final int machineSlot;

        private PatternSlot(
                int machineSlot,
                int x,
                int y
        ) {
            super(
                    new SimpleContainer(1),
                    0,
                    x,
                    y
            );

            this.machineSlot = machineSlot;
        }

        @Override
        public ItemStack getItem() {
            if (isClientMenu()) {
                return super.getItem();
            }

            return packager.getPatternStack(
                    machineSlot
            );
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public boolean mayPlace(
                @NotNull ItemStack stack
        ) {
            if (isClientMenu()) {
                return menuSlotIsUnlocked();
            }

            return packager.canInsertPatternAt(
                    machineSlot,
                    stack
            );
        }

        private boolean menuSlotIsUnlocked() {
            return machineSlot
                    < getAccessiblePatternSlotCount();
        }

        @Override
        public boolean mayPickup(
                @NotNull Player player
        ) {
            return !getItem().isEmpty();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(
                @NotNull ItemStack stack
        ) {
            return 1;
        }

        @Override
        public void set(
                @NotNull ItemStack stack
        ) {
            if (isClientMenu()) {
                super.set(stack);
                return;
            }

            packager.setPatternStack(
                    machineSlot,
                    stack
            );
        }

        @Override
        public ItemStack remove(
                int amount
        ) {
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }

            if (isClientMenu()) {
                return super.remove(amount);
            }

            return packager.removePatternStack(
                    machineSlot
            );
        }

        @Override
        public void setChanged() {
            if (isClientMenu()) {
                super.setChanged();
            }
        }
    }

    private final class CapacityCardSlot extends Slot {

        private final int machineSlot;

        private CapacityCardSlot(
                int machineSlot,
                int x,
                int y
        ) {
            super(
                    new SimpleContainer(1),
                    0,
                    x,
                    y
            );

            this.machineSlot = machineSlot;
        }

        @Override
        public ItemStack getItem() {
            if (isClientMenu()) {
                return super.getItem();
            }

            return packager.getUpgradeStack(
                    machineSlot
            );
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public boolean mayPlace(
                @NotNull ItemStack stack
        ) {
            if (isClientMenu()) {
                return AEItems.CAPACITY_CARD.is(stack);
            }

            return AEItems.CAPACITY_CARD.is(stack)
                    && packager.canInsertCapacityCardAt(
                    machineSlot,
                    stack
            );
        }

        @Override
        public boolean mayPickup(
                @NotNull Player player
        ) {
            if (isClientMenu()) {
                return !getItem().isEmpty();
            }

            return packager.canRemoveCapacityCardAt(
                    machineSlot
            );
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(
                @NotNull ItemStack stack
        ) {
            return 1;
        }

        @Override
        public void set(
                @NotNull ItemStack stack
        ) {
            if (isClientMenu()) {
                super.set(stack);
                return;
            }

            packager.setCapacityCardStack(
                    machineSlot,
                    stack
            );
        }

        @Override
        public ItemStack remove(
                int amount
        ) {
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }

            if (isClientMenu()) {
                return super.remove(amount);
            }

            return packager.removeCapacityCardStack(
                    machineSlot
            );
        }

        @Override
        public void setChanged() {
            if (isClientMenu()) {
                super.setChanged();
            }
        }
    }
}