package com.machinelearning.playcarddetect.modules.accessibilityaction.action

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.nhatran241.accessibilityactionmodule.model.Action

class OpenApp(@NonNull var packagename: String, @Nullable delaytime : Long) : Action(){
    init {
        this.delayTime = delaytime
    }
}