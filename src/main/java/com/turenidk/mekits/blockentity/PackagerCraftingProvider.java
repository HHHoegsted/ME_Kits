package com.turenidk.mekits.blockentity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PackagerCraftingProvider
        implements ICraftingProvider {

    private final MEKitPackagerBlockEntity owner;

    public PackagerCraftingProvider(
            MEKitPackagerBlockEntity owner
    ) {
        this.owner = owner;
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return owner.getAvailablePatterns();
    }

    @Override
    public boolean pushPattern(
            IPatternDetails patternDetails,
            KeyCounter[] inputHolder
    ) {
        List<IPatternDetails> availablePatterns =
                owner.getAvailablePatterns();

        if (!availablePatterns.contains(patternDetails)) {
            return false;
        }

        if (owner.hasPendingOutput()) {
            return false;
        }

        boolean receivedAnyInput = false;

        for (KeyCounter inputCounter : inputHolder) {
            for (var entry : inputCounter) {
                if (!(entry.getKey() instanceof AEItemKey)) {
                    return false;
                }

                if (entry.getLongValue() <= 0) {
                    return false;
                }

                receivedAnyInput = true;
            }
        }

        if (!receivedAnyInput) {
            return false;
        }

        GenericStack primaryOutput =
                patternDetails.getPrimaryOutput();

        if (!(primaryOutput.what() instanceof AEItemKey outputKey)) {
            return false;
        }

        if (
                primaryOutput.amount() <= 0
                        || primaryOutput.amount() > Integer.MAX_VALUE
        ) {
            return false;
        }

        ItemStack outputStack =
                outputKey.toStack(
                        (int) primaryOutput.amount()
                );

        return owner.queueOutput(outputStack);
    }

    @Override
    public boolean isBusy() {
        return owner.hasPendingOutput();
    }
}