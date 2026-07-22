package com.turenidk.mekits.item;

import com.turenidk.mekits.MEKits;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class EncodedMEKitPatternItem
        extends Item {

    public EncodedMEKitPatternItem(
            Properties properties
    ) {
        super(
                properties
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack heldStack =
                player.getItemInHand(
                        hand
                );

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(
                    heldStack
            );
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.success(
                    heldStack
            );
        }

        boolean cleared =
                clearPattern(
                        heldStack,
                        player
                );

        if (!cleared) {
            return InteractionResultHolder.fail(
                    heldStack
            );
        }

        return InteractionResultHolder.success(
                player.getItemInHand(
                        hand
                )
        );
    }

    @Override
    public InteractionResult onItemUseFirst(
            ItemStack stack,
            UseOnContext context
    ) {
        Player player =
                context.getPlayer();

        if (
                player == null
                        || !player.isShiftKeyDown()
        ) {
            return InteractionResult.PASS;
        }

        if (
                context.getLevel()
                        .isClientSide()
        ) {
            return InteractionResult.SUCCESS;
        }

        return clearPattern(
                stack,
                player
        )
                ? InteractionResult.SUCCESS
                : InteractionResult.FAIL;
    }

    private boolean clearPattern(
            ItemStack stack,
            Player player
    ) {
        if (
                stack.isEmpty()
                        || !stack.is(
                        MEKits
                                .ENCODED_ME_KIT_PATTERN
                                .get()
                )
        ) {
            return false;
        }

        Inventory inventory =
                player.getInventory();

        ItemStack blankPatterns =
                new ItemStack(
                        MEKits
                                .BLANK_ME_KIT_PATTERN
                                .get(),
                        stack.getCount()
                );

        for (
                int slot = 0;
                slot
                        < inventory
                        .getContainerSize();
                slot++
        ) {
            if (
                    inventory.getItem(
                            slot
                    )
                            != stack
            ) {
                continue;
            }

            inventory.setItem(
                    slot,
                    blankPatterns
            );

            return true;
        }

        return false;
    }
}