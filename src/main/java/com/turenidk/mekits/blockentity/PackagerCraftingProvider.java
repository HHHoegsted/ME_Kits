package com.turenidk.mekits.blockentity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PackagerCraftingProvider
        implements appeng.api.networking.crafting.ICraftingProvider {

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
        IPatternDetails availablePattern =
                findAvailablePattern(
                        patternDetails
                );

        if (
                availablePattern == null
                        || owner.hasPendingOutput()
        ) {
            return false;
        }

        Map<AEItemKey, Long> expectedInputs =
                collectExpectedInputs(
                        availablePattern
                );

        if (expectedInputs == null || expectedInputs.isEmpty()) {
            return false;
        }

        Map<AEItemKey, Long> receivedInputs =
                collectReceivedInputs(
                        inputHolder
                );

        if (
                receivedInputs == null
                        || !receivedInputs.equals(
                        expectedInputs
                )
        ) {
            return false;
        }

        GenericStack primaryOutput =
                availablePattern.getPrimaryOutput();

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

        return owner.queueOutput(
                outputStack
        );
    }

    @Override
    public boolean isBusy() {
        return owner.hasPendingOutput();
    }

    private IPatternDetails findAvailablePattern(
            IPatternDetails requestedPattern
    ) {
        for (
                IPatternDetails availablePattern
                : owner.getAvailablePatterns()
        ) {
            if (availablePattern.equals(requestedPattern)) {
                return availablePattern;
            }
        }

        return null;
    }

    private Map<AEItemKey, Long> collectExpectedInputs(
            IPatternDetails patternDetails
    ) {
        Map<AEItemKey, Long> expectedInputs =
                new LinkedHashMap<>();

        for (
                IPatternDetails.IInput input
                : patternDetails.getInputs()
        ) {
            GenericStack[] possibleInputs =
                    input.getPossibleInputs();

            /*
             * ME Kit patterns currently use exact inputs only.
             * Reject substitutions or malformed input definitions
             * rather than guessing which candidate AE2 selected.
             */
            if (possibleInputs.length != 1) {
                return null;
            }

            GenericStack possibleInput =
                    possibleInputs[0];

            if (!(possibleInput.what() instanceof AEItemKey inputKey)) {
                return null;
            }

            long requiredAmount;

            try {
                requiredAmount =
                        Math.multiplyExact(
                                possibleInput.amount(),
                                input.getMultiplier()
                        );
            } catch (ArithmeticException exception) {
                return null;
            }

            if (requiredAmount <= 0) {
                return null;
            }

            try {
                expectedInputs.merge(
                        inputKey,
                        requiredAmount,
                        Math::addExact
                );
            } catch (ArithmeticException exception) {
                return null;
            }
        }

        return expectedInputs;
    }

    private Map<AEItemKey, Long> collectReceivedInputs(
            KeyCounter[] inputHolder
    ) {
        Map<AEItemKey, Long> receivedInputs =
                new LinkedHashMap<>();

        for (KeyCounter inputCounter : inputHolder) {
            if (inputCounter == null) {
                return null;
            }

            for (var entry : inputCounter) {
                if (!(entry.getKey() instanceof AEItemKey inputKey)) {
                    return null;
                }

                long receivedAmount =
                        entry.getLongValue();

                if (receivedAmount <= 0) {
                    return null;
                }

                try {
                    receivedInputs.merge(
                            inputKey,
                            receivedAmount,
                            Math::addExact
                    );
                } catch (ArithmeticException exception) {
                    return null;
                }
            }
        }

        return receivedInputs;
    }
}