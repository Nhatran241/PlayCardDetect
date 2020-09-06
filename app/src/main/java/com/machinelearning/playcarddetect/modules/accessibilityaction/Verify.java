package com.machinelearning.playcarddetect.modules.accessibilityaction;

import android.graphics.RectF;

public class Verify {
    public RectF verifyZone;
    public String verifyText;

    public Verify(RectF verifyZone, String verifyText) {
        this.verifyZone = verifyZone;
        this.verifyText = verifyText;
    }
    public Verify(){
    }

    public RectF getVerifyZone() {
        return verifyZone;
    }

    public void setVerifyZone(RectF verifyZone) {
        this.verifyZone = verifyZone;
    }

    public String getVerifyText() {
        return verifyText;
    }

    public void setVerifyText(String verifyText) {
        this.verifyText = verifyText;
    }
}
