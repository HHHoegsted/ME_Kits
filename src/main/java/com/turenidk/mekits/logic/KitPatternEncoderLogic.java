package com.turenidk.mekits.logic;

import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.component.KitContents;
import com.turenidk.mekits.component.ModDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class KitPatternEncoderLogic {

    private static final String PATTERN_INVENTORY_TAG =
            "pattern_inventory";

    private static final String INGREDIENT_DEFINITIONS_TAG =
            "ingredient_definitions";

    public static final int BLANK_PATTERN_SLOT =
            0;

    public static final int ENCODED_PATTERN_SLOT =
            1;

    public static final int PATTERN_SLOT_COUNT =
            2;

    public static final int MAX_INGREDIENT_DEFINITIONS =
            27;

    public enum EncodeResult {
        SUCCESS,
        OUTPUT_OCCUPIED,
        NO_BLANK_PATTERN,
        CONTENTS_REQUIRED,
        INVALID_CONTENTS,
        INTERNAL_ERROR
    }

    private final Runnable changeListener;

    private final NonNullList<ItemStack> patternInventory =
            NonNullList.withSize(
                    PATTERN_SLOT_COUNT,
                    ItemStack.EMPTY
            );

    private final NonNullList<ItemStack> ingredientDefinitions =
            NonNullList.withSize(
                    MAX_INGREDIENT_DEFINITIONS,
                    ItemStack.EMPTY
            );

    private final ItemStackHandler patternItemHandler =
            new ItemStackHandler(
                    patternInventory
            ) {

                @Override
                public void setStackInSlot(
                        int slot,
                        @NotNull ItemStack stack
                ) {
                    if (
                            !stack.isEmpty()
                                    && !isItemValid(
                                    slot,
                                    stack
                            )
                    ) {
                        return;
                    }

                    ItemStack storedStack =
                            stack;

                    if (!stack.isEmpty()) {
                        int allowedCount =
                                Math.min(
                                        getSlotLimit(
                                                slot
                                        ),
                                        stack.getMaxStackSize()
                                );

                        if (
                                stack.getCount()
                                        > allowedCount
                        ) {
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
                                        MEKits
                                                .BLANK_ME_KIT_PATTERN
                                                .get()
                                );

                        case ENCODED_PATTERN_SLOT ->
                                isValidEncodedPattern(
                                        stack
                                );

                        default ->
                                false;
                    };
                }

                @Override
                public int getSlotLimit(
                        int slot
                ) {
                    return switch (slot) {
                        case BLANK_PATTERN_SLOT ->
                                64;

                        case ENCODED_PATTERN_SLOT ->
                                1;

                        default ->
                                0;
                    };
                }

                @Override
                protected void onContentsChanged(
                        int slot
                ) {
                    if (
                            slot
                                    == ENCODED_PATTERN_SLOT
                    ) {
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

                    notifyChanged();
                }
            };

    private final ItemStackHandler ingredientDefinitionHandler =
            new ItemStackHandler(
                    ingredientDefinitions
            ) {

                @Override
                public boolean isItemValid(
                        int slot,
                        @NotNull ItemStack stack
                ) {
                    return false;
                }

                @Override
                protected void onContentsChanged(
                        int slot
                ) {
                    notifyChanged();
                }
            };

    public KitPatternEncoderLogic(
            @NotNull Runnable changeListener
    ) {
        this.changeListener =
                changeListener;
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
                        || slot
                        >= MAX_INGREDIENT_DEFINITIONS
                        || representativeStack.isEmpty()
                        || quantity <= 0
                        || isForbiddenIngredientDefinition(
                        representativeStack
                )
        ) {
            return false;
        }

        int validatedQuantity =
                Math.min(
                        quantity,
                        representativeStack
                                .getMaxStackSize()
                );

        ingredientDefinitionHandler.setStackInSlot(
                slot,
                representativeStack.copyWithCount(
                        validatedQuantity
                )
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
                        || slot
                        >= MAX_INGREDIENT_DEFINITIONS
                        || direction == 0
        ) {
            return false;
        }

        ItemStack currentDefinition =
                ingredientDefinitionHandler
                        .getStackInSlot(
                                slot
                        );

        if (currentDefinition.isEmpty()) {
            return false;
        }

        int currentQuantity =
                currentDefinition.getCount();

        int maximumQuantity =
                currentDefinition
                        .getMaxStackSize();

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

        if (
                newQuantity
                        == currentQuantity
        ) {
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

    public boolean clearIngredientDefinition(
            int slot
    ) {
        if (
                slot < 0
                        || slot
                        >= MAX_INGREDIENT_DEFINITIONS
        ) {
            return false;
        }

        if (
                ingredientDefinitionHandler
                        .getStackInSlot(
                                slot
                        )
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
                false;

        for (
                int slot = 0;
                slot
                        < MAX_INGREDIENT_DEFINITIONS;
                slot++
        ) {
            if (
                    ingredientDefinitions
                            .get(
                                    slot
                            )
                            .isEmpty()
            ) {
                continue;
            }

            ingredientDefinitions.set(
                    slot,
                    ItemStack.EMPTY
            );

            changed =
                    true;
        }

        if (!changed) {
            return false;
        }

        notifyChanged();

        return true;
    }

    public boolean insertBlankPattern(
            @NotNull ItemStack sourceStack
    ) {
        if (
                !sourceStack.is(
                        MEKits
                                .BLANK_ME_KIT_PATTERN
                                .get()
                )
        ) {
            return false;
        }

        ItemStack remainder =
                patternItemHandler.insertItem(
                        BLANK_PATTERN_SLOT,
                        sourceStack.copyWithCount(
                                1
                        ),
                        false
                );

        return remainder.isEmpty();
    }

    public boolean insertEncodedPattern(
            @NotNull ItemStack sourceStack
    ) {
        if (
                !isValidEncodedPattern(
                        sourceStack
                )
        ) {
            return false;
        }

        ItemStack remainder =
                patternItemHandler.insertItem(
                        ENCODED_PATTERN_SLOT,
                        sourceStack.copyWithCount(
                                1
                        ),
                        false
                );

        return remainder.isEmpty();
    }

    public int getBlankPatternCount() {
        return patternItemHandler
                .getStackInSlot(
                        BLANK_PATTERN_SLOT
                )
                .getCount();
    }

    public boolean hasEncodedPattern() {
        return !patternItemHandler
                .getStackInSlot(
                        ENCODED_PATTERN_SLOT
                )
                .isEmpty();
    }

    /*
     * Temporary compatibility methods.
     *
     * The menu and the old name-update packet still reference these
     * during this migration step. They no longer affect encoder state
     * or generated patterns.
     */
    public @NotNull String getKitName() {
        return "";
    }

    public void setKitName(
            @NotNull String newKitName
    ) {
        // Kit names are no longer part of the encoded-pattern model.
    }

    public @NotNull EncodeResult encodePattern() {
        if (
                !patternItemHandler
                        .getStackInSlot(
                                ENCODED_PATTERN_SLOT
                        )
                        .isEmpty()
        ) {
            return EncodeResult
                    .OUTPUT_OCCUPIED;
        }

        List<ItemStack> definitions =
                getIngredientDefinitions();

        if (definitions.isEmpty()) {
            return EncodeResult
                    .CONTENTS_REQUIRED;
        }

        for (
                ItemStack definition
                : definitions
        ) {
            if (
                    definition.isEmpty()
                            || definition.getCount()
                            <= 0
                            || isForbiddenIngredientDefinition(
                            definition
                    )
            ) {
                return EncodeResult
                        .INVALID_CONTENTS;
            }
        }

        ItemStack availableBlankPatterns =
                patternItemHandler.getStackInSlot(
                        BLANK_PATTERN_SLOT
                );

        if (
                availableBlankPatterns.isEmpty()
                        || !availableBlankPatterns.is(
                        MEKits
                                .BLANK_ME_KIT_PATTERN
                                .get()
                )
                        || availableBlankPatterns
                        .getCount()
                        < 1
        ) {
            return EncodeResult
                    .NO_BLANK_PATTERN;
        }

        ItemStack encodedPattern =
                new ItemStack(
                        MEKits
                                .ENCODED_ME_KIT_PATTERN
                                .get(),
                        1
                );

        /*
         * The first non-empty definition is now the kit's implicit
         * identity. It is already the first stack in KitContents, so
         * no separate name or icon component is required.
         */
        encodedPattern.set(
                ModDataComponents
                        .KIT_CONTENTS
                        .get(),
                new KitContents(
                        definitions
                )
        );

        if (
                !isValidEncodedPattern(
                        encodedPattern
                )
        ) {
            return EncodeResult
                    .INVALID_CONTENTS;
        }

        ItemStack simulatedOutputRemainder =
                patternItemHandler.insertItem(
                        ENCODED_PATTERN_SLOT,
                        encodedPattern,
                        true
                );

        if (
                !simulatedOutputRemainder
                        .isEmpty()
        ) {
            return EncodeResult
                    .INTERNAL_ERROR;
        }

        ItemStack simulatedBlankExtraction =
                patternItemHandler.extractItem(
                        BLANK_PATTERN_SLOT,
                        1,
                        true
                );

        if (
                simulatedBlankExtraction
                        .getCount()
                        != 1
                        || !simulatedBlankExtraction.is(
                        MEKits
                                .BLANK_ME_KIT_PATTERN
                                .get()
                )
        ) {
            return EncodeResult
                    .NO_BLANK_PATTERN;
        }

        ItemStack consumedBlankPattern =
                patternItemHandler.extractItem(
                        BLANK_PATTERN_SLOT,
                        1,
                        false
                );

        if (
                consumedBlankPattern
                        .getCount()
                        != 1
                        || !consumedBlankPattern.is(
                        MEKits
                                .BLANK_ME_KIT_PATTERN
                                .get()
                )
        ) {
            return EncodeResult
                    .INTERNAL_ERROR;
        }

        ItemStack outputRemainder =
                patternItemHandler.insertItem(
                        ENCODED_PATTERN_SLOT,
                        encodedPattern,
                        false
                );

        if (!outputRemainder.isEmpty()) {
            ItemStack restoreRemainder =
                    patternItemHandler.insertItem(
                            BLANK_PATTERN_SLOT,
                            consumedBlankPattern,
                            false
                    );

            if (!restoreRemainder.isEmpty()) {
                MEKits.LOGGER.error(
                        "Failed to restore a Blank ME Kit Pattern "
                                + "after encoder output insertion failed"
                );
            }

            return EncodeResult
                    .INTERNAL_ERROR;
        }

        notifyChanged();

        return EncodeResult.SUCCESS;
    }

    public @NotNull List<ItemStack> getIngredientDefinitions() {
        return ingredientDefinitions
                .stream()
                .filter(
                        stack ->
                                !stack.isEmpty()
                )
                .map(
                        ItemStack::copy
                )
                .toList();
    }

    public int getIngredientDefinitionCount() {
        int definitionCount =
                0;

        for (
                ItemStack definition
                : ingredientDefinitions
        ) {
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
                slot
                        < patternItemHandler
                        .getSlots();
                slot++
        ) {
            ItemStack storedStack =
                    patternItemHandler.extractItem(
                            slot,
                            Integer.MAX_VALUE,
                            false
                    );

            if (!storedStack.isEmpty()) {
                removedPatterns.add(
                        storedStack
                );
            }
        }

        return removedPatterns;
    }

    public void save(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
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

    public void load(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        patternInventory.clear();

        if (
                tag.contains(
                        PATTERN_INVENTORY_TAG
                )
        ) {
            ContainerHelper.loadAllItems(
                    tag.getCompound(
                            PATTERN_INVENTORY_TAG
                    ),
                    patternInventory,
                    registries
            );
        }

        boolean hasSavedEditorState =
                tag.contains(
                        INGREDIENT_DEFINITIONS_TAG
                );

        ingredientDefinitions.clear();

        if (
                tag.contains(
                        INGREDIENT_DEFINITIONS_TAG
                )
        ) {
            ContainerHelper.loadAllItems(
                    tag.getCompound(
                            INGREDIENT_DEFINITIONS_TAG
                    ),
                    ingredientDefinitions,
                    registries
            );
        }

        if (!hasSavedEditorState) {
            loadEditorDefinitionFromPattern(
                    patternInventory.get(
                            ENCODED_PATTERN_SLOT
                    )
            );
        }
    }

    private boolean isValidEncodedPattern(
            @NotNull ItemStack patternStack
    ) {
        if (
                !patternStack.is(
                        MEKits
                                .ENCODED_ME_KIT_PATTERN
                                .get()
                )
        ) {
            return false;
        }

        KitContents encodedContents =
                patternStack.get(
                        ModDataComponents
                                .KIT_CONTENTS
                                .get()
                );

        if (
                encodedContents == null
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

        for (
                ItemStack encodedStack
                : encodedStacks
        ) {
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
        if (
                !isValidEncodedPattern(
                        patternStack
                )
        ) {
            return false;
        }

        KitContents encodedContents =
                patternStack.get(
                        ModDataComponents
                                .KIT_CONTENTS
                                .get()
                );

        if (encodedContents == null) {
            return false;
        }

        List<ItemStack> encodedStacks =
                encodedContents.stacks();

        ingredientDefinitions.clear();

        for (
                int slot = 0;
                slot < encodedStacks.size();
                slot++
        ) {
            ingredientDefinitions.set(
                    slot,
                    encodedStacks
                            .get(
                                    slot
                            )
                            .copy()
            );
        }

        notifyChanged();

        return true;
    }

    private boolean isForbiddenIngredientDefinition(
            @NotNull ItemStack stack
    ) {
        return stack.is(
                MEKits.ME_KIT.get()
        )
                || stack.is(
                MEKits
                        .BLANK_ME_KIT_PATTERN
                        .get()
        )
                || stack.is(
                MEKits
                        .ENCODED_ME_KIT_PATTERN
                        .get()
        );
    }

    private void notifyChanged() {
        changeListener.run();
    }
}