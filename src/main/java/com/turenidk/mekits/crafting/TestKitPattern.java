package com.turenidk.mekits.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.component.KitContents;
import com.turenidk.mekits.component.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TestKitPattern implements IPatternDetails {
    private final AEItemKey definition;
    private final IInput[] inputs;
    private final List<GenericStack> outputs;

    public TestKitPattern(AEItemKey definition) {
        this.definition = definition;

        this.inputs = new IInput[] {
                new ExactInput(AEItemKey.of(Items.CHEST), 1),
                new ExactInput(AEItemKey.of(Items.PISTON), 2),
                new ExactInput(AEItemKey.of(Items.IRON_INGOT), 3)
        };

        ItemStack outputKit = MEKits.ME_KIT.get().getDefaultInstance();

        outputKit.set(
                ModDataComponents.KIT_NAME.get(),
                "Test Kit"
        );

        outputKit.set(
                ModDataComponents.KIT_CONTENTS.get(),
                new KitContents(List.of(
                        new ItemStack(Items.CHEST, 1),
                        new ItemStack(Items.PISTON, 2),
                        new ItemStack(Items.IRON_INGOT, 3)
                ))
        );

        this.outputs = List.of(
                new GenericStack(
                        AEItemKey.of(outputKit),
                        1
                )
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
        return object instanceof TestKitPattern other
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