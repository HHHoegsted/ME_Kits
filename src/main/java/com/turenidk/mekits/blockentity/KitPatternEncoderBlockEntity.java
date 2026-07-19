package com.turenidk.mekits.blockentity;

import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.logic.KitPatternEncoderLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KitPatternEncoderBlockEntity extends BlockEntity {

    public static final int BLANK_PATTERN_SLOT =
            KitPatternEncoderLogic.BLANK_PATTERN_SLOT;

    public static final int ENCODED_PATTERN_SLOT =
            KitPatternEncoderLogic.ENCODED_PATTERN_SLOT;

    public static final int SLOT_COUNT =
            KitPatternEncoderLogic.PATTERN_SLOT_COUNT;

    public static final int MAX_INGREDIENT_DEFINITIONS =
            KitPatternEncoderLogic.MAX_INGREDIENT_DEFINITIONS;

    private final KitPatternEncoderLogic encoderLogic =
            new KitPatternEncoderLogic(
                    this::setChanged
            );

    public KitPatternEncoderBlockEntity(
            @NotNull BlockPos blockPos,
            @NotNull BlockState blockState
    ) {
        super(
                MEKits.KIT_PATTERN_ENCODER_BLOCK_ENTITY.get(),
                blockPos,
                blockState
        );
    }

    public @NotNull IItemHandler getPatternItemHandler() {
        return encoderLogic.getPatternItemHandler();
    }

    public @NotNull IItemHandler
    getIngredientDefinitionHandler() {
        return encoderLogic
                .getIngredientDefinitionHandler();
    }

    public boolean setIngredientDefinition(
            int slot,
            @NotNull ItemStack representativeStack,
            int quantity
    ) {
        return encoderLogic.setIngredientDefinition(
                slot,
                representativeStack,
                quantity
        );
    }

    public boolean adjustIngredientDefinitionQuantity(
            int slot,
            int direction,
            boolean jumpToLimit
    ) {
        return encoderLogic
                .adjustIngredientDefinitionQuantity(
                        slot,
                        direction,
                        jumpToLimit
                );
    }

    public boolean clearIngredientDefinition(
            int slot
    ) {
        return encoderLogic
                .clearIngredientDefinition(
                        slot
                );
    }

    public boolean clearEditorState() {
        return encoderLogic.clearEditorState();
    }

    public boolean insertBlankPattern(
            @NotNull ItemStack sourceStack
    ) {
        return encoderLogic.insertBlankPattern(
                sourceStack
        );
    }

    public boolean insertEncodedPattern(
            @NotNull ItemStack sourceStack
    ) {
        return encoderLogic.insertEncodedPattern(
                sourceStack
        );
    }

    public int getBlankPatternCount() {
        return encoderLogic.getBlankPatternCount();
    }

    public boolean hasEncodedPattern() {
        return encoderLogic.hasEncodedPattern();
    }

    public @NotNull String getKitName() {
        return encoderLogic.getKitName();
    }

    public void setKitName(
            @NotNull String newKitName
    ) {
        encoderLogic.setKitName(
                newKitName
        );
    }

    public @NotNull KitPatternEncoderLogic.EncodeResult
    encodePattern() {
        return encoderLogic.encodePattern();
    }

    public @NotNull List<ItemStack>
    getIngredientDefinitions() {
        return encoderLogic
                .getIngredientDefinitions();
    }

    public int getIngredientDefinitionCount() {
        return encoderLogic
                .getIngredientDefinitionCount();
    }

    public @NotNull ItemStack removeNextPattern() {
        return encoderLogic.removeNextPattern();
    }

    public @NotNull List<ItemStack>
    takePatternInventory() {
        return encoderLogic.takePatternInventory();
    }

    @Override
    protected void saveAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.saveAdditional(
                tag,
                registries
        );

        encoderLogic.save(
                tag,
                registries
        );
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(
                tag,
                registries
        );

        encoderLogic.load(
                tag,
                registries
        );
    }
}