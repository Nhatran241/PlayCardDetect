package com.machinelearning.playcarddetect.modules.accessibilityaction.action;

import android.graphics.RectF;

import com.machinelearning.playcarddetect.modules.accessibilityaction.Verify;

public class OpenChonBanScreenAction extends ClickActionWithVerify {
    public OpenChonBanScreenAction(RectF clickRectF, long startTime, long durationTime, int delayTime, String actionType, Verify verify) {
        super(clickRectF, startTime, durationTime, delayTime, actionType, verify);
    }

    public OpenChonBanScreenAction() {
    }
}
