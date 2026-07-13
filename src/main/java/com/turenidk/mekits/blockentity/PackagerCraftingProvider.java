package com.turenidk.mekits.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.component.KitContents;
import com.turenidk.mekits.component.ModDataComponents;
import com.turenidk.mekits.crafting.TestKitPattern;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PackagerCraftingProvider implements ICraftingProvider {
    private final MEKitPackagerBlockEntity owner;
    private final List<IPatternDetails> availablePatterns;

    public PackagerCraftingProvider(
            MEKitPackagerBlockEntity owner
    ) {
        this.owner = owner;

        ItemStack encodedPatternStack =
                MEKits.ENCODED_ME_KIT_PATTERN.get().getDefaultInstance();

        AEItemKey definition = AEItemKey.of(encodedPatternStack);

        if (definition == null) {
            throw new IllegalStateException(
                    "Could not create AE2 key for the Test Kit pattern"
            );
        }

        this.availablePatterns = List.of(
                new TestKitPattern(definition)
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

        var grid = owner.getManagedGridNode().getGrid();

        if (grid == null) {
            return false;
        }

        List<ItemStack> receivedStacks = new ArrayList<>();

        for (KeyCounter inputCounter : inputHolder) {
            for (var entry : inputCounter) {
                if (!(entry.getKey() instanceof AEItemKey itemKey)) {
                    return false;
                }

                long amount = entry.getLongValue();

                if (amount <= 0 || amount > Integer.MAX_VALUE) {
                    return false;
                }

                receivedStacks.add(
                        itemKey.toStack((int) amount)
                );
            }
        }

        if (receivedStacks.isEmpty()) {
            return false;
        }

        ItemStack outputKit =
                MEKits.ME_KIT.get().getDefaultInstance();

        outputKit.set(
                ModDataComponents.KIT_NAME.get(),
                "Test Kit"
        );

        outputKit.set(
                ModDataComponents.KIT_CONTENTS.get(),
                new KitContents(receivedStacks)
        );

        AEItemKey outputKey = AEItemKey.of(outputKit);

        if (outputKey == null) {
            return false;
        }

        long inserted = grid
                .getStorageService()
                .getInventory()
                .insert(
                        outputKey,
                        1,
                        Actionable.MODULATE,
                        IActionSource.empty()
                );

        return inserted == 1;
    }

    @Override
    public boolean isBusy() {
        return false;
    }
}