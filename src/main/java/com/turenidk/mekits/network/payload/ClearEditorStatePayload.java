package com.turenidk.mekits.network.payload;

import com.turenidk.mekits.MEKits;
import com.turenidk.mekits.menu.KitPatternEncoderMenu;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ClearEditorStatePayload()
        implements CustomPacketPayload {

    public static final ClearEditorStatePayload INSTANCE =
            new ClearEditorStatePayload();

    public static final CustomPacketPayload.Type<
            ClearEditorStatePayload
            > TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            MEKits.MODID,
                            "clear_editor_state"
                    )
            );

    public static final StreamCodec<
            net.minecraft.network.FriendlyByteBuf,
            ClearEditorStatePayload
            > STREAM_CODEC =
            StreamCodec.unit(
                    INSTANCE
            );

    @Override
    public @NotNull CustomPacketPayload.Type<
            ? extends CustomPacketPayload
            > type() {
        return TYPE;
    }

    public static void handle(
            @NotNull ClearEditorStatePayload payload,
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

        menu.clearEditorState();
    }
}