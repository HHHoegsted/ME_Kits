package com.turenidk.mekits.network.payload;

import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.menu.KitPatternEncoderMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record AdjustIngredientQuantityPayload(
        int definitionSlot,
        int direction,
        boolean jumpToLimit
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<
            AdjustIngredientQuantityPayload
            > TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            MEKits.MODID,
                            "adjust_ingredient_quantity"
                    )
            );

    public static final StreamCodec<
            ByteBuf,
            AdjustIngredientQuantityPayload
            > STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    AdjustIngredientQuantityPayload::definitionSlot,
                    ByteBufCodecs.VAR_INT,
                    AdjustIngredientQuantityPayload::direction,
                    ByteBufCodecs.BOOL,
                    AdjustIngredientQuantityPayload::jumpToLimit,
                    AdjustIngredientQuantityPayload::new
            );

    @Override
    public @NotNull CustomPacketPayload.Type<
            ? extends CustomPacketPayload
            > type() {
        return TYPE;
    }

    public static void handle(
            @NotNull AdjustIngredientQuantityPayload payload,
            @NotNull IPayloadContext context
    ) {
        if (
                !(context.player().containerMenu
                        instanceof KitPatternEncoderMenu menu)
        ) {
            return;
        }

        if (!menu.stillValid(context.player())) {
            return;
        }

        menu.adjustIngredientQuantity(
                payload.definitionSlot(),
                payload.direction(),
                payload.jumpToLimit()
        );
    }
}