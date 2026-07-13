package com.turenidk.mekits.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.component.KitContents;
import com.turenidk.mekits.component.ModDataComponents;
import com.turenidk.mekits.crafting.MEKitPattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class PackagerCraftingProvider implements ICraftingProvider {
    private final MEKitPackagerBlockEntity owner;
    private final List<IPatternDetails> availablePatterns;

    public PackagerCraftingProvider(
            MEKitPackagerBlockEntity owner
    ) {
        this.owner = owner;

        ItemStack encodedPatternStack =
                MEKits.ENCODED_ME_KIT_PATTERN.get()
                        .getDefaultInstance();

        encodedPatternStack.set(
                ModDataComponents.KIT_NAME.get(),
                "Test Kit"
        );

        encodedPatternStack.set(
                ModDataComponents.KIT_CONTENTS.get(),
                new KitContents(List.of(
                        new ItemStack(Items.CHEST, 1),
                        new ItemStack(Items.PISTON, 2),
                        new ItemStack(Items.IRON_INGOT, 3)
                ))
        );

        AEItemKey definition =
                AEItemKey.of(encodedPatternStack);

        if (definition == null) {
            throw new IllegalStateException(
                    "Could not create AE2 key for the ME Kit pattern"
            );
        }

        this.availablePatterns = List.of(
                new MEKitPattern(definition)
        );
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return availablePatterns;
    }

    @Override
    public boolean pushPattern(
            IPatternDetails patternDetails,
            KeyCounter[] inputHolder
    ) {
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

        if (primaryOutput.amount() <= 0
                || primaryOutput.amount() > Integer.MAX_VALUE) {
            return false;
        }

        ItemStack outputStack =
                outputKey.toStack((int) primaryOutput.amount());

        return owner.queueOutput(outputStack);
    }

    @Override
    public boolean isBusy() {
        return owner.hasPendingOutput();
    }
}