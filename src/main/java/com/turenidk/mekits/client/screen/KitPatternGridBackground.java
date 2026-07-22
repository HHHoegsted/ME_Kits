package com.turenidk.mekits.client.screen;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.Blitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

public final class KitPatternGridBackground
        implements ICompositeWidget {

    /*
     * This is the input-grid portion of AE2's native processing-pattern
     * panel. It includes the three-by-three framed grid and its
     * scrollbar track, but excludes the processing-output area.
     */
    private static final Blitter BACKGROUND =
            Blitter.texture(
                    "guis/pattern_modes.png"
            ).src(
                    0,
                    70,
                    70,
                    66
            );

    private static final int DEFAULT_WIDTH =
            70;

    private static final int DEFAULT_HEIGHT =
            66;

    @NotNull
    private Point position =
            new Point(
                    0,
                    0
            );

    private int width =
            DEFAULT_WIDTH;

    private int height =
            DEFAULT_HEIGHT;

    @Override
    public void setPosition(
            @NotNull Point position
    ) {
        this.position =
                position;
    }

    @Override
    public void setSize(
            int width,
            int height
    ) {
        if (width > 0) {
            this.width =
                    width;
        }

        if (height > 0) {
            this.height =
                    height;
        }
    }

    @Override
    public @NotNull Rect2i getBounds() {
        return new Rect2i(
                position.getX(),
                position.getY(),
                width,
                height
        );
    }

    @Override
    public void drawBackgroundLayer(
            @NotNull GuiGraphics guiGraphics,
            @NotNull Rect2i screenBounds,
            @NotNull Point mouse
    ) {
        BACKGROUND.dest(
                screenBounds.getX()
                        + position.getX(),
                screenBounds.getY()
                        + position.getY()
        ).blit(
                guiGraphics
        );
    }
}