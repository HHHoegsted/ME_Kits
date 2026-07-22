package com.turenidk.mekits.client.render;

import com.turenidk.mekits.component.KitContents;
import com.turenidk.mekits.component.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class KitIconHelper {

    private KitIconHelper() {
    }

    public static @NotNull ItemStack getFirstIcon(
            @NotNull ItemStack stack
    ) {
        KitContents contents =
                stack.getOrDefault(
                        ModDataComponents
                                .KIT_CONTENTS
                                .get(),
                        KitContents.EMPTY
                );

        if (contents.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (
                ItemStack containedStack
                : contents.stacks()
        ) {
            if (containedStack.isEmpty()) {
                continue;
            }

            return containedStack.copyWithCount(
                    1
            );
        }

        return ItemStack.EMPTY;
    }
}
