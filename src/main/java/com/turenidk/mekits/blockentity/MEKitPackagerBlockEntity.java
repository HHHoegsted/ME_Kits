package com.turenidk.mekits.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEItems;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.crafting.MEKitPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MEKitPackagerBlockEntity extends BlockEntity
        implements IInWorldGridNodeHost, IActionHost {

    private static final String GRID_NODE_TAG = "grid_node";
    private static final String PENDING_OUTPUT_TAG = "pending_output";
    private static final String PATTERN_INVENTORY_TAG = "pattern_inventory";
    private static final String UPGRADE_INVENTORY_TAG = "upgrade_inventory";

    public static final int BASE_PATTERN_SLOT_COUNT = 9;
    public static final int PATTERN_SLOTS_PER_CAPACITY_CARD = 9;
    public static final int MAX_PATTERN_SLOT_COUNT = 27;
    public static final int UPGRADE_SLOT_COUNT = 2;

    private final NonNullList<ItemStack> patternInventory =
            NonNullList.withSize(
                    MAX_PATTERN_SLOT_COUNT,
                    ItemStack.EMPTY
            );

    private final NonNullList<ItemStack> upgradeInventory =
            NonNullList.withSize(
                    UPGRADE_SLOT_COUNT,
                    ItemStack.EMPTY
            );

    private ItemStack pendingOutput = ItemStack.EMPTY;

    private static final IGridNodeListener<MEKitPackagerBlockEntity>
            NODE_LISTENER =
            new IGridNodeListener<>() {
                @Override
                public void onSaveChanges(
                        MEKitPackagerBlockEntity nodeOwner,
                        IGridNode node
                ) {
                    nodeOwner.setChanged();
                }
            };

    private final ICraftingProvider craftingProvider =
            new PackagerCraftingProvider(this);

    private final IManagedGridNode managedGridNode =
            GridHelper.createManagedNode(
                            this,
                            NODE_LISTENER
                    )
                    .setTagName(GRID_NODE_TAG)
                    .setInWorldNode(true)
                    .setIdlePowerUsage(1.0)
                    .addService(
                            ICraftingProvider.class,
                            craftingProvider
                    );

    public MEKitPackagerBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(
                MEKits.ME_KIT_PACKAGER_BLOCK_ENTITY.get(),
                blockPos,
                blockState
        );
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        GridHelper.onFirstTick(
                this,
                MEKitPackagerBlockEntity::onFirstTick
        );
    }

    private void onFirstTick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        managedGridNode.create(
                level,
                worldPosition
        );
    }

    @Override
    public void setRemoved() {
        managedGridNode.destroy();
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(
                tag,
                registries
        );

        CompoundTag nodeTag = new CompoundTag();
        managedGridNode.saveToNBT(nodeTag);
        tag.put(
                GRID_NODE_TAG,
                nodeTag
        );

        if (!pendingOutput.isEmpty()) {
            tag.put(
                    PENDING_OUTPUT_TAG,
                    pendingOutput.save(registries)
            );
        }

        CompoundTag patternInventoryTag =
                new CompoundTag();

        ContainerHelper.saveAllItems(
                patternInventoryTag,
                patternInventory,
                registries
        );

        tag.put(
                PATTERN_INVENTORY_TAG,
                patternInventoryTag
        );

        CompoundTag upgradeInventoryTag =
                new CompoundTag();

        ContainerHelper.saveAllItems(
                upgradeInventoryTag,
                upgradeInventory,
                registries
        );

        tag.put(
                UPGRADE_INVENTORY_TAG,
                upgradeInventoryTag
        );
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(
                tag,
                registries
        );

        if (tag.contains(GRID_NODE_TAG)) {
            managedGridNode.loadFromNBT(
                    tag.getCompound(GRID_NODE_TAG)
            );
        }

        if (tag.contains(PENDING_OUTPUT_TAG)) {
            pendingOutput =
                    ItemStack.parseOptional(
                            registries,
                            tag.getCompound(
                                    PENDING_OUTPUT_TAG
                            )
                    );
        } else {
            pendingOutput = ItemStack.EMPTY;
        }

        patternInventory.clear();

        if (tag.contains(PATTERN_INVENTORY_TAG)) {
            ContainerHelper.loadAllItems(
                    tag.getCompound(
                            PATTERN_INVENTORY_TAG
                    ),
                    patternInventory,
                    registries
            );
        }

        upgradeInventory.clear();

        if (tag.contains(UPGRADE_INVENTORY_TAG)) {
            ContainerHelper.loadAllItems(
                    tag.getCompound(
                            UPGRADE_INVENTORY_TAG
                    ),
                    upgradeInventory,
                    registries
            );
        }
    }

    public int getInstalledCapacityCardCount() {
        int installedCards = 0;

        for (ItemStack upgradeStack : upgradeInventory) {
            if (AEItems.CAPACITY_CARD.is(upgradeStack)) {
                installedCards++;
            }
        }

        return installedCards;
    }

    public int getAccessiblePatternSlotCount() {
        return Math.min(
                BASE_PATTERN_SLOT_COUNT
                        + getInstalledCapacityCardCount()
                        * PATTERN_SLOTS_PER_CAPACITY_CARD,
                MAX_PATTERN_SLOT_COUNT
        );
    }

    public boolean insertPattern(
            ItemStack patternStack
    ) {
        if (!isValidEncodedPattern(patternStack)) {
            return false;
        }

        int accessibleSlots =
                getAccessiblePatternSlotCount();

        for (int slot = 0; slot < accessibleSlots; slot++) {
            if (!patternInventory.get(slot).isEmpty()) {
                continue;
            }

            patternInventory.set(
                    slot,
                    patternStack.copyWithCount(1)
            );

            onPatternInventoryChanged();
            return true;
        }

        return false;
    }

    public ItemStack removePattern() {
        for (
                int slot = patternInventory.size() - 1;
                slot >= 0;
                slot--
        ) {
            ItemStack patternStack =
                    patternInventory.get(slot);

            if (patternStack.isEmpty()) {
                continue;
            }

            patternInventory.set(
                    slot,
                    ItemStack.EMPTY
            );

            onPatternInventoryChanged();
            return patternStack;
        }

        return ItemStack.EMPTY;
    }

    public boolean insertCapacityCard(
            ItemStack cardStack
    ) {
        if (!AEItems.CAPACITY_CARD.is(cardStack)) {
            return false;
        }

        for (
                int slot = 0;
                slot < upgradeInventory.size();
                slot++
        ) {
            if (!upgradeInventory.get(slot).isEmpty()) {
                continue;
            }

            upgradeInventory.set(
                    slot,
                    cardStack.copyWithCount(1)
            );

            onUpgradeInventoryChanged();
            return true;
        }

        return false;
    }

    public ItemStack removeCapacityCard() {
        for (
                int slot = upgradeInventory.size() - 1;
                slot >= 0;
                slot--
        ) {
            ItemStack removedStack =
                    removeCapacityCardStack(slot);

            if (!removedStack.isEmpty()) {
                return removedStack;
            }
        }

        return ItemStack.EMPTY;
    }

    public List<ItemStack> takePatternInventory() {
        List<ItemStack> removedPatterns =
                new ArrayList<>();

        for (
                int slot = 0;
                slot < patternInventory.size();
                slot++
        ) {
            ItemStack patternStack =
                    patternInventory.get(slot);

            if (patternStack.isEmpty()) {
                continue;
            }

            removedPatterns.add(patternStack);

            patternInventory.set(
                    slot,
                    ItemStack.EMPTY
            );
        }

        if (!removedPatterns.isEmpty()) {
            onPatternInventoryChanged();
        }

        return removedPatterns;
    }

    public List<ItemStack> takeUpgradeInventory() {
        List<ItemStack> removedUpgrades =
                new ArrayList<>();

        for (
                int slot = 0;
                slot < upgradeInventory.size();
                slot++
        ) {
            ItemStack upgradeStack =
                    upgradeInventory.get(slot);

            if (upgradeStack.isEmpty()) {
                continue;
            }

            removedUpgrades.add(upgradeStack);

            upgradeInventory.set(
                    slot,
                    ItemStack.EMPTY
            );
        }

        if (!removedUpgrades.isEmpty()) {
            onUpgradeInventoryChanged();
        }

        return removedUpgrades;
    }

    public List<IPatternDetails> getAvailablePatterns() {
        Set<IPatternDetails> availablePatterns =
                new LinkedHashSet<>();

        int accessibleSlots =
                getAccessiblePatternSlotCount();

        for (int slot = 0; slot < accessibleSlots; slot++) {
            ItemStack patternStack =
                    patternInventory.get(slot);

            if (patternStack.isEmpty()) {
                continue;
            }

            AEItemKey definition =
                    AEItemKey.of(patternStack);

            if (definition == null) {
                continue;
            }

            try {
                availablePatterns.add(
                        new MEKitPattern(definition)
                );
            } catch (IllegalArgumentException exception) {
                MEKits.LOGGER.warn(
                        "Ignoring invalid ME Kit Pattern in Packager at {}",
                        worldPosition,
                        exception
                );
            }
        }

        return List.copyOf(
                availablePatterns
        );
    }

    private boolean isValidEncodedPattern(
            ItemStack patternStack
    ) {
        if (
                patternStack.isEmpty()
                        || !patternStack.is(
                        MEKits.ENCODED_ME_KIT_PATTERN.get()
                )
        ) {
            return false;
        }

        AEItemKey definition =
                AEItemKey.of(patternStack);

        if (definition == null) {
            return false;
        }

        try {
            new MEKitPattern(definition);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void onPatternInventoryChanged() {
        setChanged();

        ICraftingProvider.requestUpdate(
                managedGridNode
        );
    }

    private void onUpgradeInventoryChanged() {
        setChanged();

        ICraftingProvider.requestUpdate(
                managedGridNode
        );
    }

    public boolean queueOutput(
            ItemStack outputStack
    ) {
        if (
                outputStack.isEmpty()
                        || !pendingOutput.isEmpty()
        ) {
            return false;
        }

        pendingOutput =
                outputStack.copy();

        setChanged();
        return true;
    }

    public boolean hasPendingOutput() {
        return !pendingOutput.isEmpty();
    }

    public ItemStack takePendingOutput() {
        if (pendingOutput.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack outputStack =
                pendingOutput;

        pendingOutput =
                ItemStack.EMPTY;

        setChanged();
        return outputStack;
    }

    public static void serverTick(
            Level level,
            BlockPos blockPos,
            BlockState blockState,
            MEKitPackagerBlockEntity blockEntity
    ) {
        blockEntity.processPendingOutput();
    }

    private void processPendingOutput() {
        if (
                level == null
                        || level.isClientSide()
                        || pendingOutput.isEmpty()
        ) {
            return;
        }

        var grid =
                managedGridNode.getGrid();

        if (grid == null) {
            return;
        }

        AEItemKey outputKey =
                AEItemKey.of(pendingOutput);

        if (outputKey == null) {
            return;
        }

        long inserted =
                grid.getStorageService()
                        .getInventory()
                        .insert(
                                outputKey,
                                pendingOutput.getCount(),
                                Actionable.MODULATE,
                                IActionSource.ofMachine(this)
                        );

        if (inserted <= 0) {
            return;
        }

        pendingOutput.shrink(
                (int) inserted
        );

        if (pendingOutput.isEmpty()) {
            pendingOutput =
                    ItemStack.EMPTY;
        }

        setChanged();
    }

    public ItemStack getPatternStack(
            int slot
    ) {
        if (
                slot < 0
                        || slot >= patternInventory.size()
        ) {
            return ItemStack.EMPTY;
        }

        ItemStack patternStack =
                patternInventory.get(
                        slot
                );

        return patternStack.isEmpty()
                ? ItemStack.EMPTY
                : patternStack.copy();
    }

    public boolean canInsertPatternAt(
            int slot,
            ItemStack patternStack
    ) {
        return slot >= 0
                && slot < getAccessiblePatternSlotCount()
                && slot < patternInventory.size()
                && patternInventory.get(slot).isEmpty()
                && isValidEncodedPattern(patternStack);
    }

    public boolean setPatternStack(
            int slot,
            ItemStack patternStack
    ) {
        if (
                slot < 0
                        || slot >= patternInventory.size()
        ) {
            return false;
        }

        if (patternStack.isEmpty()) {
            if (patternInventory.get(slot).isEmpty()) {
                return false;
            }

            patternInventory.set(
                    slot,
                    ItemStack.EMPTY
            );

            onPatternInventoryChanged();
            return true;
        }

        if (
                slot >= getAccessiblePatternSlotCount()
                        || !isValidEncodedPattern(patternStack)
        ) {
            return false;
        }

        patternInventory.set(
                slot,
                patternStack.copyWithCount(1)
        );

        onPatternInventoryChanged();
        return true;
    }

    public ItemStack removePatternStack(
            int slot
    ) {
        if (
                slot < 0
                        || slot >= patternInventory.size()
        ) {
            return ItemStack.EMPTY;
        }

        ItemStack patternStack =
                patternInventory.get(slot);

        if (patternStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        patternInventory.set(
                slot,
                ItemStack.EMPTY
        );

        onPatternInventoryChanged();
        return patternStack;
    }

    public ItemStack getUpgradeStack(
            int slot
    ) {
        if (
                slot < 0
                        || slot >= upgradeInventory.size()
        ) {
            return ItemStack.EMPTY;
        }

        ItemStack upgradeStack =
                upgradeInventory.get(
                        slot
                );

        return upgradeStack.isEmpty()
                ? ItemStack.EMPTY
                : upgradeStack.copy();
    }

    public boolean canInsertCapacityCardAt(
            int slot,
            ItemStack cardStack
    ) {
        return slot >= 0
                && slot < upgradeInventory.size()
                && upgradeInventory.get(slot).isEmpty()
                && AEItems.CAPACITY_CARD.is(cardStack);
    }

    public boolean setCapacityCardStack(
            int slot,
            ItemStack cardStack
    ) {
        if (
                slot < 0
                        || slot >= upgradeInventory.size()
        ) {
            return false;
        }

        if (cardStack.isEmpty()) {
            return removeCapacityCardStack(slot).isEmpty()
                    == false;
        }

        if (!canInsertCapacityCardAt(slot, cardStack)) {
            return false;
        }

        upgradeInventory.set(
                slot,
                cardStack.copyWithCount(1)
        );

        onUpgradeInventoryChanged();
        return true;
    }

    public boolean canRemoveCapacityCardAt(
            int upgradeSlot
    ) {
        if (
                upgradeSlot < 0
                        || upgradeSlot >= upgradeInventory.size()
                        || !AEItems.CAPACITY_CARD.is(
                        upgradeInventory.get(upgradeSlot)
                )
        ) {
            return false;
        }

        int capacityAfterRemoval =
                Math.min(
                        BASE_PATTERN_SLOT_COUNT
                                + (
                                getInstalledCapacityCardCount() - 1
                        )
                                * PATTERN_SLOTS_PER_CAPACITY_CARD,
                        MAX_PATTERN_SLOT_COUNT
                );

        for (
                int patternSlot = capacityAfterRemoval;
                patternSlot < patternInventory.size();
                patternSlot++
        ) {
            if (!patternInventory.get(patternSlot).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public ItemStack removeCapacityCardStack(
            int slot
    ) {
        if (!canRemoveCapacityCardAt(slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack cardStack =
                upgradeInventory.get(slot);

        upgradeInventory.set(
                slot,
                ItemStack.EMPTY
        );

        onUpgradeInventoryChanged();
        return cardStack;
    }

    public IManagedGridNode getManagedGridNode() {
        return managedGridNode;
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return managedGridNode.getNode();
    }

    @Nullable
    @Override
    public IGridNode getGridNode(
            Direction direction
    ) {
        return managedGridNode.getNode();
    }
}