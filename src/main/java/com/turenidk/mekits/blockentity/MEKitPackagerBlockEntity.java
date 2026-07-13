package com.turenidk.mekits.blockentity;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import com.turenidk.mekits.MEKits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import appeng.api.networking.IInWorldGridNodeHost;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class MEKitPackagerBlockEntity extends BlockEntity implements IInWorldGridNodeHost {
    private static final String GRID_NODE_TAG = "grid_node";

    private static final IGridNodeListener<MEKitPackagerBlockEntity> NODE_LISTENER =
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
            GridHelper.createManagedNode(this, NODE_LISTENER)
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
        if (level != null && !level.isClientSide()) {
            managedGridNode.create(level, worldPosition);
        }
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
        super.saveAdditional(tag, registries);

        CompoundTag nodeTag = new CompoundTag();
        managedGridNode.saveToNBT(nodeTag);
        tag.put(GRID_NODE_TAG, nodeTag);
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        if (tag.contains(GRID_NODE_TAG)) {
            managedGridNode.loadFromNBT(
                    tag.getCompound(GRID_NODE_TAG)
            );
        }
    }

    public IManagedGridNode getManagedGridNode() {
        return managedGridNode;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction direction) {
        return managedGridNode.getNode();
    }
}