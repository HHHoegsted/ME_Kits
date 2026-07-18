package com.turenidk.mekits.block;

import com.mojang.serialization.MapCodec;
import com.turenidk.mekits.blockentity.KitPatternEncoderBlockEntity;
import com.turenidk.mekits.menu.KitPatternEncoderMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitPatternEncoderBlock extends BaseEntityBlock {

    public static final MapCodec<KitPatternEncoderBlock> CODEC =
            simpleCodec(KitPatternEncoderBlock::new);

    public KitPatternEncoderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(
            @NotNull BlockState blockState
    ) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            @NotNull BlockPos blockPos,
            @NotNull BlockState blockState
    ) {
        return new KitPatternEncoderBlockEntity(
                blockPos,
                blockState
        );
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(
            @NotNull BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos blockPos
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(blockPos);

        if (!(blockEntity instanceof KitPatternEncoderBlockEntity encoder)) {
            return null;
        }

        return new SimpleMenuProvider(
                (
                        containerId,
                        playerInventory,
                        player
                ) -> new KitPatternEncoderMenu(
                        containerId,
                        playerInventory,
                        encoder
                ),
                Component.translatable(
                        "menu.mekits.kit_pattern_encoder"
                )
        );
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos blockPos,
            @NotNull Player player,
            @NotNull BlockHitResult hitResult
    ) {
        if (
                !level.isClientSide()
                        && player instanceof ServerPlayer serverPlayer
        ) {
            BlockEntity blockEntity =
                    level.getBlockEntity(blockPos);

            if (!(blockEntity instanceof KitPatternEncoderBlockEntity encoder)) {
                return InteractionResult.FAIL;
            }

            MenuProvider menuProvider =
                    getMenuProvider(
                            blockState,
                            level,
                            blockPos
                    );

            if (menuProvider == null) {
                return InteractionResult.FAIL;
            }

            String storedKitName =
                    encoder.getKitName();

            String syncedKitName =
                    storedKitName.length()
                            <= KitPatternEncoderMenu.MAX_KIT_NAME_LENGTH
                            ? storedKitName
                            : storedKitName.substring(
                            0,
                            KitPatternEncoderMenu.MAX_KIT_NAME_LENGTH
                    );

            serverPlayer.openMenu(
                    menuProvider,
                    buffer -> buffer.writeUtf(
                            syncedKitName,
                            KitPatternEncoderMenu.MAX_KIT_NAME_LENGTH
                    )
            );
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide()
        );
    }

    @Override
    protected void onRemove(
            @NotNull BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos blockPos,
            @NotNull BlockState newBlockState,
            boolean movedByPiston
    ) {
        if (blockState.is(newBlockState.getBlock())) {
            return;
        }

        if (!level.isClientSide()) {
            BlockEntity blockEntity =
                    level.getBlockEntity(blockPos);

            if (blockEntity instanceof KitPatternEncoderBlockEntity encoder) {
                for (
                        ItemStack storedStack
                        : encoder.takePatternInventory()
                ) {
                    popResource(
                            level,
                            blockPos,
                            storedStack
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