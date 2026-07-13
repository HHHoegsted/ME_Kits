package com.turenidk.mekits.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import com.turenidk.mekits.MEKits;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class MEKitPatternDecoder
        implements IPatternDetailsDecoder {

    public static final MEKitPatternDecoder INSTANCE =
            new MEKitPatternDecoder();

    private MEKitPatternDecoder() {
    }

    @Override
    public boolean isEncodedPattern(
            ItemStack stack
    ) {
        return stack.is(
                MEKits.ENCODED_ME_KIT_PATTERN.get()
        );
    }

    @Nullable
    @Override
    public IPatternDetails decodePattern(
            AEItemKey definition,
            Level level
    ) {
        if (definition == null) {
            return null;
        }

        ItemStack patternStack =
                definition.toStack();

        if (!isEncodedPattern(patternStack)) {
            return null;
        }

        try {
            return new MEKitPattern(definition);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}