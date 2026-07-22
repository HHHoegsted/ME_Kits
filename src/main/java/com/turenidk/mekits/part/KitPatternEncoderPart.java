package com.turenidk.mekits.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.logic.KitPatternEncoderHost;
import com.turenidk.mekits.logic.KitPatternEncoderLogic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class KitPatternEncoderPart
        extends AbstractTerminalPart
        implements KitPatternEncoderHost {

    private static final String ENCODER_LOGIC_TAG =
            "encoder_logic";

    public static final ResourceLocation MODEL_OFF =
            ResourceLocation.fromNamespaceAndPath(
                    MEKits.MODID,
                    "part/kit_pattern_encoder_off"
            );

    public static final ResourceLocation MODEL_ON =
            ResourceLocation.fromNamespaceAndPath(
                    MEKits.MODID,
                    "part/kit_pattern_encoder_on"
            );

    private static final IPartModel MODELS_OFF =
            new PartModel(
                    MODEL_BASE,
                    MODEL_OFF,
                    MODEL_STATUS_OFF
            );

    private static final IPartModel MODELS_ON =
            new PartModel(
                    MODEL_BASE,
                    MODEL_ON,
                    MODEL_STATUS_ON
            );

    private static final IPartModel MODELS_HAS_CHANNEL =
            new PartModel(
                    MODEL_BASE,
                    MODEL_ON,
                    MODEL_STATUS_HAS_CHANNEL
            );

    private final KitPatternEncoderLogic encoderLogic =
            new KitPatternEncoderLogic(
                    this::markEncoderForSave
            );

    public KitPatternEncoderPart(
            @NotNull IPartItem<?> partItem
    ) {
        super(
                partItem
        );
    }

    @Override
    public @NotNull KitPatternEncoderLogic
    getEncoderLogic() {
        return encoderLogic;
    }

    @Override
    public boolean isEncoderPowered() {
        return getMainNode().isPowered();
    }

    @Override
    public boolean isEncoderActive() {
        return getMainNode().isActive();
    }

    @Override
    public @NotNull IPartModel getStaticModels() {
        return selectModel(
                MODELS_OFF,
                MODELS_ON,
                MODELS_HAS_CHANNEL
        );
    }

    @Override
    public MenuType<?> getMenuType(
            @NotNull Player player
    ) {
        return MEKits.KIT_PATTERN_ENCODER_MENU.get();
    }

    @Override
    public void addAdditionalDrops(
            @NotNull List<ItemStack> drops,
            boolean wrenched
    ) {
        super.addAdditionalDrops(
                drops,
                wrenched
        );

        drops.addAll(
                encoderLogic.takePatternInventory()
        );
    }

    @Override
    public void clearContent() {
        super.clearContent();

        encoderLogic.takePatternInventory();
    }

    @Override
    public void readFromNBT(
            @NotNull CompoundTag data,
            @NotNull HolderLookup.Provider registries
    ) {
        super.readFromNBT(
                data,
                registries
        );

        encoderLogic.load(
                data.getCompound(
                        ENCODER_LOGIC_TAG
                ),
                registries
        );
    }

    @Override
    public void writeToNBT(
            @NotNull CompoundTag data,
            @NotNull HolderLookup.Provider registries
    ) {
        super.writeToNBT(
                data,
                registries
        );

        CompoundTag encoderTag =
                new CompoundTag();

        encoderLogic.save(
                encoderTag,
                registries
        );

        data.put(
                ENCODER_LOGIC_TAG,
                encoderTag
        );
    }

    private void markEncoderForSave() {
        if (getHost() != null) {
            getHost().markForSave();
        }
    }
}