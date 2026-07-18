package com.turenidk.mekits.network.payload;

import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.blockentity.KitPatternEncoderBlockEntity;
import com.turenidk.mekits.menu.KitPatternEncoderMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class EncodePatternPayload
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<
            EncodePatternPayload
            > TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            MEKits.MODID,
                            "encode_pattern"
                    )
            );

    public static final EncodePatternPayload INSTANCE =
            new EncodePatternPayload();

    public static final StreamCodec<
            ByteBuf,
            EncodePatternPayload
            > STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    private EncodePatternPayload() {
    }

    @Override
    public @NotNull CustomPacketPayload.Type<
            EncodePatternPayload
            > type() {
        return TYPE;
    }

    public static void handle(
            @NotNull EncodePatternPayload payload,
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

        KitPatternEncoderBlockEntity.EncodeResult result =
                menu.encodePattern();

        /*
         * Immediately synchronize the consumed blank and newly
         * created output pattern with the open client menu.
         */
        menu.broadcastChanges();

        String messageKey =
                switch (result) {
                    case SUCCESS ->
                            "message.mekits.encoder.success";

                    case OUTPUT_OCCUPIED ->
                            "message.mekits.encoder.output_occupied";

                    case NO_BLANK_PATTERN ->
                            "message.mekits.encoder.no_blank_pattern";

                    case NAME_REQUIRED ->
                            "message.mekits.encoder.name_required";

                    case CONTENTS_REQUIRED ->
                            "message.mekits.encoder.contents_required";

                    case INVALID_CONTENTS ->
                            "message.mekits.encoder.invalid_contents";

                    case INTERNAL_ERROR ->
                            "message.mekits.encoder.internal_error";
                };

        context.player().displayClientMessage(
                Component.translatable(messageKey),
                true
        );
    }
}
