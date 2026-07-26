package com.turenidk.mekits.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.turenidk.mekits.MEKits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class MEKitItemRenderer
        extends BlockEntityWithoutLevelRenderer {

    private static final float GROUND_SCALE =
            0.5F;

    private static final float FIRST_PERSON_SCALE =
            0.5F;

    private static final float THIRD_PERSON_SCALE =
            0.5F;

    public MEKitItemRenderer() {
        super(
                Minecraft.getInstance()
                        .getBlockEntityRenderDispatcher(),
                Minecraft.getInstance()
                        .getEntityModels()
        );
    }

    @Override
    public void renderByItem(
            @NotNull ItemStack kitStack,
            @NotNull ItemDisplayContext displayContext,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack iconStack =
                KitIconHelper.getFirstIcon(
                        kitStack
                );

        if (iconStack.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer =
                Minecraft.getInstance()
                        .getItemRenderer();

        float scale =
                getContextScale(
                        displayContext
                );

        poseStack.pushPose();

        try {
            poseStack.translate(
                    0.5F,
                    0.5F,
                    0.5F
            );

            poseStack.scale(
                    scale,
                    scale,
                    scale
            );

            poseStack.translate(
                    -0.5F,
                    -0.5F,
                    -0.5F
            );

            renderLayer(
                    itemRenderer,
                    iconStack,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay
            );

            renderLayer(
                    itemRenderer,
                    MEKits.KIT_ICON_OVERLAY
                            .get()
                            .getDefaultInstance(),
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay
            );
        } finally {
            poseStack.popPose();
        }
    }

    private static float getContextScale(
            @NotNull ItemDisplayContext displayContext
    ) {
        return switch (displayContext) {
            case GROUND ->
                    GROUND_SCALE;

            case FIRST_PERSON_LEFT_HAND,
                 FIRST_PERSON_RIGHT_HAND ->
                    FIRST_PERSON_SCALE;

            case THIRD_PERSON_LEFT_HAND,
                 THIRD_PERSON_RIGHT_HAND ->
                    THIRD_PERSON_SCALE;

            default ->
                    1.0F;
        };
    }

    private static void renderLayer(
            @NotNull ItemRenderer itemRenderer,
            @NotNull ItemStack stack,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();

        try {
            poseStack.translate(
                    0.5F,
                    0.5F,
                    0.5F
            );

            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.NONE,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    Minecraft.getInstance().level,
                    0
            );
        } finally {
            poseStack.popPose();
        }
    }
}