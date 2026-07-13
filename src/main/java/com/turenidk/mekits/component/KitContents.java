package com.turenidk.mekits.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record KitContents(List<ItemStack> stacks) {
    public static final KitContents EMPTY = new KitContents(List.of());

    public static final Codec<KitContents> CODEC =
            ItemStack.CODEC.listOf()
                    .xmap(KitContents::new, KitContents::stacks);

    public static final StreamCodec<RegistryFriendlyByteBuf, KitContents> STREAM_CODEC =
            ItemStack.STREAM_CODEC
                    .apply(ByteBufCodecs.list())
                    .map(KitContents::new, KitContents::stacks);

    public KitContents {
        stacks = stacks.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    @Override
    public List<ItemStack> stacks() {
        return stacks.stream()
                .map(ItemStack::copy)
                .toList();
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }
}