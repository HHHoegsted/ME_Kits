package com.turenidk.mekits.logic;

import org.jetbrains.annotations.NotNull;

public interface KitPatternEncoderHost {

    @NotNull
    KitPatternEncoderLogic getEncoderLogic();

    boolean isEncoderPowered();

    boolean isEncoderActive();
}