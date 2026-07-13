package com.turenidk.mekits.block;

import com.mojang.serialization.MapCodec;
import com.turenidk.mekits.MEKits;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
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
}