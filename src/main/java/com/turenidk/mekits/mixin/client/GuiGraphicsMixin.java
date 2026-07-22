package com.turenidk.mekits.mixin.client;

import com.turenidk.mekits.client.render.KitGuiItemRendering;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        GuiGraphics.class
)
public abstract class GuiGraphicsMixin {

    @Inject(
            method =
                    "renderItem("
                            + "Lnet/minecraft/world/entity/LivingEntity;"
                            + "Lnet/minecraft/world/level/Level;"
                            + "Lnet/minecraft/world/item/ItemStack;"
                            + "III)V",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void mekits$renderGuiItem(
            @Nullable LivingEntity livingEntity,
            @Nullable Level level,
            ItemStack stack,
            int x,
            int y,
            int seed,
            CallbackInfo callbackInfo
    ) {
        GuiGraphics guiGraphics =
                (GuiGraphics)
                        (Object) this;

        if (
                KitGuiItemRendering.onRenderGuiItem(
                        guiGraphics,
                        livingEntity,
                        level,
                        stack,
                        x,
                        y,
                        seed
                )
        ) {
            callbackInfo.cancel();
        }
    }
}