package com.turenidk.mekits.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.turenidk.mekits.component.ModDataComponents;
import com.turenidk.mekits.component.KitContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

import java.util.List;

public class MEKitItem extends Item {
    public MEKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        String kitName = stack.get(ModDataComponents.KIT_NAME.get());

        if (kitName == null || kitName.isBlank()) {
            tooltipComponents.add(
                    Component.translatable("tooltip.mekits.me_kit.unnamed")
                            .withStyle(ChatFormatting.GRAY)
            );
        } else {
            tooltipComponents.add(
                    Component.literal(kitName)
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        KitContents contents = stack.getOrDefault(
                ModDataComponents.KIT_CONTENTS.get(),
                KitContents.EMPTY
        );

        if (contents.isEmpty()) {
            tooltipComponents.add(
                    Component.translatable("tooltip.mekits.me_kit.empty")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        } else {
            tooltipComponents.add(
                    Component.translatable(
                                    "tooltip.mekits.me_kit.contains",
                                    contents.stacks().size()
                            )
                            .withStyle(ChatFormatting.DARK_GRAY)
            );

            for (ItemStack containedStack : contents.stacks()) {
                tooltipComponents.add(
                        Component.literal("  ")
                                .append(
                                        Component.translatable(
                                                "tooltip.mekits.me_kit.entry",
                                                containedStack.getCount(),
                                                containedStack.getHoverName()
                                        )
                                )
                                .withStyle(ChatFormatting.GRAY)
                );
            }
        }

        super.appendHoverText(
                stack,
                context,
                tooltipComponents,
                tooltipFlag
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        ItemStack kitStack = player.getItemInHand(usedHand);

        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(kitStack, true);
        }

        KitContents contents = kitStack.getOrDefault(
                ModDataComponents.KIT_CONTENTS.get(),
                KitContents.EMPTY
        );

        if (contents.isEmpty()) {
            return InteractionResultHolder.pass(kitStack);
        }

        List<ItemStack> remainingStacks = new ArrayList<>();

        for (ItemStack containedStack : contents.stacks()) {
            ItemStack remainingStack = containedStack.copy();

            boolean fullyInserted = player.getInventory().add(remainingStack);

            if (!fullyInserted && !remainingStack.isEmpty()) {
                remainingStacks.add(remainingStack.copy());
            }
        }

        if (remainingStacks.isEmpty()) {
            kitStack.shrink(1);
        } else {
            kitStack.set(
                    ModDataComponents.KIT_CONTENTS.get(),
                    new KitContents(remainingStacks)
            );
        }

        return InteractionResultHolder.success(kitStack);
    }
}