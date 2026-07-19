package com.turenidk.mekits.part;

import appeng.api.parts.IPartItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.reporting.AbstractDisplayPart;
import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.logic.KitPatternEncoderHost;
import com.turenidk.mekits.logic.KitPatternEncoderLogic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class KitPatternEncoderPart
        extends AbstractDisplayPart
        implements KitPatternEncoderHost {

    private static final String ENCODER_LOGIC_TAG =
            "encoder_logic";

    private final KitPatternEncoderLogic encoderLogic =
            new KitPatternEncoderLogic(
                    this::markEncoderForSave
            );

    public KitPatternEncoderPart(
            @NotNull IPartItem<?> partItem
    ) {
        super(
                partItem,
                true
        );
    }

    @Override
    public @NotNull KitPatternEncoderLogic
    getEncoderLogic() {
        return encoderLogic;
    }

    @Override
    public boolean onUseWithoutItem(
            @NotNull Player player,
            @NotNull Vec3 position
    ) {
        boolean handledByBase =
                super.onUseWithoutItem(
                        player,
                        position
                );

        if (
                !handledByBase
                        && !player.level().isClientSide()
        ) {
            MenuOpener.open(
                    MEKits.KIT_PATTERN_ENCODER_MENU.get(),
                    player,
                    MenuLocators.forPart(
                            this
                    )
            );
        }

        return true;
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