package com.turenidk.mekits.block;

import com.mojang.serialization.MapCodec;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.blockentity.MEKitPackagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MEKitPackagerBlock extends BaseEntityBlock {

    public static final MapCodec<MEKitPackagerBlock> CODEC =
            simpleCodec(MEKitPackagerBlock::new);

    public MEKitPackagerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        return MEKits.ME_KIT_PACKAGER_BLOCK_ENTITY.get()
                .create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
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

            if (blockEntity instanceof MEKitPackagerBlockEntity packager) {
                ItemStack pendingOutput =
                        packager.takePendingOutput();

                if (!pendingOutput.isEmpty()) {
                    popResource(
                            level,
                            blockPos,
                            pendingOutput
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