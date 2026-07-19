package com.turenidk.mekits.blockentity;

import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.component.KitContents;
import com.turenidk.mekits.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KitPatternEncoderBlockEntity extends BlockEntity {

    private static final String PATTERN_INVENTORY_TAG =
            "pattern_inventory";

    private static final String KIT_NAME_TAG =
            "kit_name";

    private static final String INGREDIENT_DEFINITIONS_TAG =
            "ingredient_definitions";

    public static final int BLANK_PATTERN_SLOT = 0;
    public static final int ENCODED_PATTERN_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    public static final int MAX_INGREDIENT_DEFINITIONS = 27;

    public enum EncodeResult {
        SUCCESS,
        OUTPUT_OCCUPIED,
        NO_BLANK_PATTERN,
        NAME_REQUIRED,
        CONTENTS_REQUIRED,
        INVALID_CONTENTS,
        INTERNAL_ERROR
    }

    private final NonNullList<ItemStack> patternInventory =
            NonNullList.withSize(
                    SLOT_COUNT,
                    ItemStack.EMPTY
            );

    private String kitName = "";

    private final NonNullList<ItemStack> ingredientDefinitions =
            NonNullList.withSize(
                    MAX_INGREDIENT_DEFINITIONS,
                    ItemStack.EMPTY
            );

    private final ItemStackHandler patternItemHandler =
            new ItemStackHandler(patternInventory) {

                @Override
                public void setStackInSlot(
                        int slot,
                        @NotNull ItemStack stack
                ) {
                    if (
                            !stack.isEmpty()
                                    && !isItemValid(slot, stack)
                    ) {
                        return;
                    }

                    ItemStack storedStack = stack;

                    if (!stack.isEmpty()) {
                        int allowedCount =
                                Math.min(
                                        getSlotLimit(slot),
                                        stack.getMaxStackSize()
                                );

                        if (stack.getCount() > allowedCount) {
                            storedStack =
                                    stack.copyWithCount(
                                            allowedCount
                                    );
                        }
                    }

                    super.setStackInSlot(
                            slot,
                            storedStack
                    );
                }

                @Override
                public boolean isItemValid(
                        int slot,
                        @NotNull ItemStack stack
                ) {
                    return switch (slot) {
                        case BLANK_PATTERN_SLOT ->
                                stack.is(
                                        MEKits.BLANK_ME_KIT_PATTERN.get()
                                );

                        case ENCODED_PATTERN_SLOT ->
                                isValidEncodedPattern(stack);

                        default -> false;
                    };
                }

                @Override
                public int getSlotLimit(int slot) {
                    return switch (slot) {
                        case BLANK_PATTERN_SLOT -> 64;
                        case ENCODED_PATTERN_SLOT -> 1;
                        default -> 0;
                    };
                }

                @Override
                protected void onContentsChanged(int slot) {
                    if (slot == ENCODED_PATTERN_SLOT) {
                        ItemStack encodedPattern =
                                getStackInSlot(
                                        ENCODED_PATTERN_SLOT
                                );

                        if (!encodedPattern.isEmpty()) {
                            loadEditorDefinitionFromPattern(
                                    encodedPattern
                            );
                        }
                    }

                    setChanged();
                }
            };

    /*
     * This handler exposes the ghost definitions through normal
     * menu-slot synchronization.
     *
     * Definitions are changed only through the explicit methods
     * below. They are never inserted or extracted as real items.
     */
    private final ItemStackHandler ingredientDefinitionHandler =
            new ItemStackHandler(ingredientDefinitions) {

                @Override
                public boolean isItemValid(
                        int slot,
                        @NotNull ItemStack stack
                ) {
                    return false;
                }

                @Override
                protected void onContentsChanged(int slot) {
                    setChanged();
                }
            };

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
        return patternItemHandler;
    }

    public @NotNull IItemHandler getIngredientDefinitionHandler() {
        return ingredientDefinitionHandler;
    }

    public boolean setIngredientDefinition(
            int slot,
            @NotNull ItemStack representativeStack,
            int quantity
    ) {
        if (
                slot < 0
                        || slot >= MAX_INGREDIENT_DEFINITIONS
                        || representativeStack.isEmpty()
                        || quantity <= 0
                        || isForbiddenIngredientDefinition(
                        representativeStack
                )
        ) {
            return false;
        }

        int maximumQuantity =
                representativeStack.getMaxStackSize();

        int validatedQuantity =
                Math.min(
                        quantity,
                        maximumQuantity
                );

        ItemStack definition =
                representativeStack.copyWithCount(
                        validatedQuantity
                );

        ingredientDefinitionHandler.setStackInSlot(
                slot,
                definition
        );

        return true;
    }

    public boolean adjustIngredientDefinitionQuantity(
            int slot,
            int direction,
            boolean jumpToLimit
    ) {
        if (
                slot < 0
                        || slot >= MAX_INGREDIENT_DEFINITIONS
                        || direction == 0
        ) {
            return false;
        }

        ItemStack currentDefinition =
                ingredientDefinitionHandler.getStackInSlot(
                        slot
                );

        if (currentDefinition.isEmpty()) {
            return false;
        }

        int currentQuantity =
                currentDefinition.getCount();

        int maximumQuantity =
                currentDefinition.getMaxStackSize();

        int newQuantity;

        if (jumpToLimit) {
            newQuantity =
                    direction > 0
                            ? maximumQuantity
                            : 1;
        } else if (direction > 0) {
            newQuantity =
                    Math.min(
                            currentQuantity + 1,
                            maximumQuantity
                    );
        } else {
            newQuantity =
                    Math.max(
                            currentQuantity - 1,
                            1
                    );
        }

        if (newQuantity == currentQuantity) {
            return false;
        }

        ingredientDefinitionHandler.setStackInSlot(
                slot,
                currentDefinition.copyWithCount(
                        newQuantity
                )
        );

        return true;
    }

    public boolean clearIngredientDefinition(int slot) {
        if (
                slot < 0
                        || slot >= MAX_INGREDIENT_DEFINITIONS
        ) {
            return false;
        }

        if (
                ingredientDefinitionHandler
                        .getStackInSlot(slot)
                        .isEmpty()
        ) {
            return false;
        }

        ingredientDefinitionHandler.setStackInSlot(
                slot,
                ItemStack.EMPTY
        );

        return true;
    }

    public boolean clearEditorState() {
        boolean changed =
                !kitName.isEmpty();

        kitName = "";

        for (int slot = 0; slot < MAX_INGREDIENT_DEFINITIONS; slot++) {
            if (
                    !ingredientDefinitionHandler
                            .getStackInSlot(slot)
                            .isEmpty()
            ) {
                ingredientDefinitions.set(
                        slot,
                        ItemStack.EMPTY
                );

                changed = true;
            }
        }

        if (!changed) {
            return false;
        }

        setChanged();

        return true;
    }

    private boolean isForbiddenIngredientDefinition(
            @NotNull ItemStack stack
    ) {
        return stack.is(MEKits.ME_KIT.get())
                || stack.is(
                MEKits.BLANK_ME_KIT_PATTERN.get()
        )
                || stack.is(
                MEKits.ENCODED_ME_KIT_PATTERN.get()
        );
    }

    public boolean insertBlankPattern(
            @NotNull ItemStack sourceStack
    ) {
        if (!sourceStack.is(MEKits.BLANK_ME_KIT_PATTERN.get())) {
            return false;
        }

        ItemStack remainder =
                patternItemHandler.insertItem(
                        BLANK_PATTERN_SLOT,
                        sourceStack.copyWithCount(1),
                        false
                );

        return remainder.isEmpty();
    }

    public boolean insertEncodedPattern(
            @NotNull ItemStack sourceStack
    ) {
        if (!isValidEncodedPattern(sourceStack)) {
            return false;
        }

        ItemStack remainder =
                patternItemHandler.insertItem(
                        ENCODED_PATTERN_SLOT,
                        sourceStack.copyWithCount(1),
                        false
                );

        return remainder.isEmpty();
    }

    private boolean isValidEncodedPattern(
            @NotNull ItemStack patternStack
    ) {
        if (
                !patternStack.is(
                        MEKits.ENCODED_ME_KIT_PATTERN.get()
                )
        ) {
            return false;
        }

        String encodedKitName =
                patternStack.get(
                        ModDataComponents.KIT_NAME.get()
                );

        KitContents encodedContents =
                patternStack.get(
                        ModDataComponents.KIT_CONTENTS.get()
                );

        if (
                encodedKitName == null
                        || encodedKitName.isBlank()
                        || encodedContents == null
                        || encodedContents.isEmpty()
        ) {
            return false;
        }

        List<ItemStack> encodedStacks =
                encodedContents.stacks();

        if (
                encodedStacks.size()
                        > MAX_INGREDIENT_DEFINITIONS
        ) {
            return false;
        }

        for (ItemStack encodedStack : encodedStacks) {
            if (
                    encodedStack.isEmpty()
                            || isForbiddenIngredientDefinition(
                            encodedStack
                    )
            ) {
                return false;
            }
        }

        return true;
    }

    private boolean loadEditorDefinitionFromPattern(
            @NotNull ItemStack patternStack
    ) {
        if (!isValidEncodedPattern(patternStack)) {
            return false;
        }

        String encodedKitName =
                patternStack.get(
                        ModDataComponents.KIT_NAME.get()
                );

        KitContents encodedContents =
                patternStack.get(
                        ModDataComponents.KIT_CONTENTS.get()
                );

        if (
                encodedKitName == null
                        || encodedContents == null
        ) {
            return false;
        }

        List<ItemStack> encodedStacks =
                encodedContents.stacks();

        kitName = encodedKitName;
        ingredientDefinitions.clear();

        for (int slot = 0; slot < encodedStacks.size(); slot++) {
            ingredientDefinitions.set(
                    slot,
                    encodedStacks.get(slot).copy()
            );
        }

        return true;
    }

    public int getBlankPatternCount() {
        return patternItemHandler
                .getStackInSlot(BLANK_PATTERN_SLOT)
                .getCount();
    }

    public boolean hasEncodedPattern() {
        return !patternItemHandler
                .getStackInSlot(ENCODED_PATTERN_SLOT)
                .isEmpty();
    }

    public @NotNull String getKitName() {
        return kitName;
    }

    public void setKitName(
            @NotNull String newKitName
    ) {
        if (kitName.equals(newKitName)) {
            return;
        }

        kitName = newKitName;
        setChanged();
    }

    public @NotNull EncodeResult encodePattern() {
        /*
         * This is deliberately the first check. An occupied output
         * slot makes the operation fail without consuming anything.
         */
        if (
                !patternItemHandler
                        .getStackInSlot(ENCODED_PATTERN_SLOT)
                        .isEmpty()
        ) {
            return EncodeResult.OUTPUT_OCCUPIED;
        }

        if (kitName.isBlank()) {
            return EncodeResult.NAME_REQUIRED;
        }

        List<ItemStack> definitions =
                getIngredientDefinitions();

        if (definitions.isEmpty()) {
            return EncodeResult.CONTENTS_REQUIRED;
        }

        for (ItemStack definition : definitions) {
            if (
                    definition.isEmpty()
                            || definition.getCount() <= 0
                            || isForbiddenIngredientDefinition(
                            definition
                    )
            ) {
                return EncodeResult.INVALID_CONTENTS;
            }
        }

        ItemStack availableBlankPatterns =
                patternItemHandler.getStackInSlot(
                        BLANK_PATTERN_SLOT
                );

        if (
                availableBlankPatterns.isEmpty()
                        || !availableBlankPatterns.is(
                        MEKits.BLANK_ME_KIT_PATTERN.get()
                )
                        || availableBlankPatterns.getCount() < 1
        ) {
            return EncodeResult.NO_BLANK_PATTERN;
        }

        ItemStack encodedPattern =
                new ItemStack(
                        MEKits.ENCODED_ME_KIT_PATTERN.get(),
                        1
                );

        encodedPattern.set(
                ModDataComponents.KIT_NAME.get(),
                kitName
        );

        encodedPattern.set(
                ModDataComponents.KIT_CONTENTS.get(),
                new KitContents(definitions)
        );

        /*
         * Validate the fully constructed output before consuming
         * the blank pattern.
         */
        if (!isValidEncodedPattern(encodedPattern)) {
            return EncodeResult.INVALID_CONTENTS;
        }

        ItemStack simulatedOutputRemainder =
                patternItemHandler.insertItem(
                        ENCODED_PATTERN_SLOT,
                        encodedPattern,
                        true
                );

        if (!simulatedOutputRemainder.isEmpty()) {
            return EncodeResult.INTERNAL_ERROR;
        }

        ItemStack simulatedBlankExtraction =
                patternItemHandler.extractItem(
                        BLANK_PATTERN_SLOT,
                        1,
                        true
                );

        if (
                simulatedBlankExtraction.getCount() != 1
                        || !simulatedBlankExtraction.is(
                        MEKits.BLANK_ME_KIT_PATTERN.get()
                )
        ) {
            return EncodeResult.NO_BLANK_PATTERN;
        }

        /*
         * All validation has completed. Only now do we consume
         * exactly one real blank pattern.
         */
        ItemStack consumedBlankPattern =
                patternItemHandler.extractItem(
                        BLANK_PATTERN_SLOT,
                        1,
                        false
                );

        if (
                consumedBlankPattern.getCount() != 1
                        || !consumedBlankPattern.is(
                        MEKits.BLANK_ME_KIT_PATTERN.get()
                )
        ) {
            return EncodeResult.INTERNAL_ERROR;
        }

        ItemStack outputRemainder =
                patternItemHandler.insertItem(
                        ENCODED_PATTERN_SLOT,
                        encodedPattern,
                        false
                );

        if (!outputRemainder.isEmpty()) {
            /*
             * Defensive rollback. Under normal server-thread menu
             * handling this should never be necessary, but it prevents
             * losing the blank if output insertion unexpectedly fails.
             */
            ItemStack restoreRemainder =
                    patternItemHandler.insertItem(
                            BLANK_PATTERN_SLOT,
                            consumedBlankPattern,
                            false
                    );

            if (!restoreRemainder.isEmpty()) {
                MEKits.LOGGER.error(
                        "Failed to restore a Blank ME Kit Pattern "
                                + "after encoder output insertion failed at {}",
                        worldPosition
                );
            }

            return EncodeResult.INTERNAL_ERROR;
        }

        setChanged();

        return EncodeResult.SUCCESS;
    }

    public @NotNull List<ItemStack> getIngredientDefinitions() {
        return ingredientDefinitions.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    public int getIngredientDefinitionCount() {
        int definitionCount = 0;

        for (ItemStack definition : ingredientDefinitions) {
            if (!definition.isEmpty()) {
                definitionCount++;
            }
        }

        return definitionCount;
    }

    public @NotNull ItemStack removeNextPattern() {
        ItemStack encodedPattern =
                patternItemHandler.extractItem(
                        ENCODED_PATTERN_SLOT,
                        1,
                        false
                );

        if (!encodedPattern.isEmpty()) {
            return encodedPattern;
        }

        return patternItemHandler.extractItem(
                BLANK_PATTERN_SLOT,
                Integer.MAX_VALUE,
                false
        );
    }

    public @NotNull List<ItemStack> takePatternInventory() {
        List<ItemStack> removedPatterns =
                new ArrayList<>();

        for (
                int slot = 0;
                slot < patternItemHandler.getSlots();
                slot++
        ) {
            ItemStack storedStack =
                    patternItemHandler.extractItem(
                            slot,
                            Integer.MAX_VALUE,
                            false
                    );

            if (!storedStack.isEmpty()) {
                removedPatterns.add(storedStack);
            }
        }

        return removedPatterns;
    }

    @Override
    protected void saveAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        CompoundTag patternInventoryTag =
                new CompoundTag();

        ContainerHelper.saveAllItems(
                patternInventoryTag,
                patternInventory,
                registries
        );

        tag.put(
                PATTERN_INVENTORY_TAG,
                patternInventoryTag
        );

        tag.putString(
                KIT_NAME_TAG,
                kitName
        );

        CompoundTag ingredientDefinitionsTag =
                new CompoundTag();

        ContainerHelper.saveAllItems(
                ingredientDefinitionsTag,
                ingredientDefinitions,
                registries
        );

        tag.put(
                INGREDIENT_DEFINITIONS_TAG,
                ingredientDefinitionsTag
        );
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        patternInventory.clear();

        if (tag.contains(PATTERN_INVENTORY_TAG)) {
            ContainerHelper.loadAllItems(
                    tag.getCompound(PATTERN_INVENTORY_TAG),
                    patternInventory,
                    registries
            );
        }

        boolean hasSavedEditorState =
                tag.contains(KIT_NAME_TAG)
                        || tag.contains(
                        INGREDIENT_DEFINITIONS_TAG
                );

        kitName = tag.getString(KIT_NAME_TAG);
        ingredientDefinitions.clear();

        if (tag.contains(INGREDIENT_DEFINITIONS_TAG)) {
            ContainerHelper.loadAllItems(
                    tag.getCompound(
                            INGREDIENT_DEFINITIONS_TAG
                    ),
                    ingredientDefinitions,
                    registries
            );
        }

        /*
         * Compatibility for encoder blocks saved before the
         * independent editor state existed.
         */
        if (!hasSavedEditorState) {
            loadEditorDefinitionFromPattern(
                    patternInventory.get(
                            ENCODED_PATTERN_SLOT
                    )
            );
        }
    }
}