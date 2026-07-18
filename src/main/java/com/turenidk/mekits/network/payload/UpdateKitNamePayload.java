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

public record UpdateKitNamePayload(
        @NotNull String kitName
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<
            UpdateKitNamePayload
            > TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            MEKits.MODID,
                            "update_kit_name"
                    )
            );

    public static final StreamCodec<
            ByteBuf,
            UpdateKitNamePayload
            > STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    UpdateKitNamePayload::kitName,
                    UpdateKitNamePayload::new
            );

    @Override
    public @NotNull CustomPacketPayload.Type<
            ? extends CustomPacketPayload
            > type() {
        return TYPE;
    }

    public static void handle(
            @NotNull UpdateKitNamePayload payload,
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

        String sanitizedName =
                payload.kitName()
                        .replace('\n', ' ')
                        .replace('\r', ' ');

        if (
                sanitizedName.length()
                        > KitPatternEncoderMenu.MAX_KIT_NAME_LENGTH
        ) {
            sanitizedName =
                    sanitizedName.substring(
                            0,
                            KitPatternEncoderMenu.MAX_KIT_NAME_LENGTH
                    );
        }

        menu.updateKitName(
                sanitizedName
        );
    }
}