package com.turenidk.mekits.block;

import appeng.core.definitions.AEItems;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.mojang.serialization.MapCodec;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.blockentity.MEKitPackagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MEKitPackagerBlock extends BaseEntityBlock {

    public static final MapCodec<MEKitPackagerBlock> CODEC =
            simpleCodec(
                    MEKitPackagerBlock::new
            );

    public MEKitPackagerBlock(
            Properties properties
    ) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(
            BlockState state
    ) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        return MEKits.ME_KIT_PACKAGER_BLOCK_ENTITY
                .get()
                .create(
                        blockPos,
                        blockState
                );
    }

    @Nullable
    @Override
    public <T extends BlockEntity>
    BlockEntityTicker<T> getTicker(
            Level level,
            BlockState blockState,
            BlockEntityType<T> blockEntityType
    ) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(
                blockEntityType,
                MEKits.ME_KIT_PACKAGER_BLOCK_ENTITY.get(),
                MEKitPackagerBlockEntity::serverTick
        );
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack heldStack,
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Player player,
            InteractionHand interactionHand,
            BlockHitResult hitResult
    ) {
        boolean isCapacityCard =
                AEItems.CAPACITY_CARD.is(
                        heldStack
                );

        boolean isEncodedPattern =
                heldStack.is(
                        MEKits.ENCODED_ME_KIT_PATTERN.get()
                );

        if (!isCapacityCard && !isEncodedPattern) {
            return ItemInteractionResult
                    .PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(blockPos);

        if (!(blockEntity
                instanceof MEKitPackagerBlockEntity packager)) {
            return ItemInteractionResult.FAIL;
        }

        boolean inserted;

        if (isCapacityCard) {
            inserted =
                    packager.insertCapacityCard(
                            heldStack
                    );
        } else {
            inserted =
                    packager.insertPattern(
                            heldStack
                    );
        }

        if (!inserted) {
            return ItemInteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(blockPos);

        if (!(blockEntity
                instanceof MEKitPackagerBlockEntity packager)) {
            return InteractionResult.PASS;
        }

        boolean opened =
                MenuOpener.open(
                        MEKits.ME_KIT_PACKAGER_MENU.get(),
                        player,
                        MenuLocators.forBlockEntity(packager)
                );

        return opened
                ? InteractionResult.CONSUME
                : InteractionResult.PASS;
    }

    @Override
    protected void onRemove(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            BlockState newBlockState,
            boolean movedByPiston
    ) {
        if (blockState.is(newBlockState.getBlock())) {
            return;
        }

        if (!level.isClientSide()) {
            BlockEntity blockEntity =
                    level.getBlockEntity(blockPos);

            if (blockEntity
                    instanceof MEKitPackagerBlockEntity packager) {
                ItemStack pendingOutput =
                        packager.takePendingOutput();

                if (!pendingOutput.isEmpty()) {
                    popResource(
                            level,
                            blockPos,
                            pendingOutput
                    );
                }

                for (
                        ItemStack patternStack
                        : packager.takePatternInventory()
                ) {
                    popResource(
                            level,
                            blockPos,
                            patternStack
                    );
                }

                for (
                        ItemStack upgradeStack
                        : packager.takeUpgradeInventory()
                ) {
                    popResource(
                            level,
                            blockPos,
                            upgradeStack
                    );
                }
            }
        }

        super.onRemove(
                blockState,
                level,
                blockPos,
                newBlockState,
                movedByPiston
        );
    }
}