package com.machinelearning.playcarddetect.modules.admin.business

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.machinelearning.playcarddetect.common.model.CardBase64

class AdminActivityViewModel{
    var listCard = MutableLiveData<List<ItemCardViewModel>>()
}