package com.turenidk.mekits.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.component.KitContents;
import com.turenidk.mekits.component.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MEKitPattern implements IPatternDetails {
    private final AEItemKey definition;
    private final IInput[] inputs;
    private final List<GenericStack> outputs;

    public MEKitPattern(AEItemKey definition) {
        this.definition = definition;

        ItemStack patternStack = definition.toStack();

        String kitName = patternStack.get(
                ModDataComponents.KIT_NAME.get()
        );

        KitContents kitContents = patternStack.get(
                ModDataComponents.KIT_CONTENTS.get()
        );

        if (kitName == null || kitName.isBlank()) {
            throw new IllegalArgumentException(
                    "Encoded ME Kit Pattern has no kit name"
            );
        }

        if (kitContents == null || kitContents.isEmpty()) {
            throw new IllegalArgumentException(
                    "Encoded ME Kit Pattern has no contents"
            );
        }

        Map<AEItemKey, Long> condensedInputs =
                new LinkedHashMap<>();

        for (ItemStack containedStack : kitContents.stacks()) {
            AEItemKey key = AEItemKey.of(containedStack);

            if (key == null) {
                throw new IllegalArgumentException(
                        "ME Kit Pattern contains an invalid item stack"
                );
            }

            condensedInputs.merge(
                    key,
                    (long) containedStack.getCount(),
                    Long::sum
            );
        }

        List<IInput> inputList = new ArrayList<>();

        for (Map.Entry<AEItemKey, Long> entry :
                condensedInputs.entrySet()) {
            inputList.add(
                    new ExactInput(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        this.inputs = inputList.toArray(IInput[]::new);

        ItemStack outputKit =
                MEKits.ME_KIT.get().getDefaultInstance();

        outputKit.set(
                ModDataComponents.KIT_NAME.get(),
                kitName
        );

        outputKit.set(
                ModDataComponents.KIT_CONTENTS.get(),
                kitContents
        );

        AEItemKey outputKey = AEItemKey.of(outputKit);

        if (outputKey == null) {
            throw new IllegalStateException(
                    "Could not create the ME Kit output key"
            );
        }

        this.outputs = List.of(
                new GenericStack(outputKey, 1)
        );
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return outputs;
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof MEKitPattern other
                && definition.equals(other.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    private static final class ExactInput implements IInput {
        private final GenericStack[] possibleInputs;
        private final long multiplier;

        private ExactInput(AEKey key, long multiplier) {
            this.possibleInputs = new GenericStack[] {
                    new GenericStack(key, 1)
            };
            this.multiplier = multiplier;
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return possibleInputs;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input.matches(possibleInputs[0]);
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}