package com.turenidk.mekits.logic;

import appeng.api.storage.ITerminalHost;
import org.jetbrains.annotations.NotNull;

public interface KitPatternEncoderHost
        extends ITerminalHost {

    @NotNull
    KitPatternEncoderLogic getEncoderLogic();

    boolean isEncoderPowered();

    boolean isEncoderActive();
}