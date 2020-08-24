package com.machinelearning.playcarddetect.modules.admin.binding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.machinelearning.playcarddetect.R
import com.machinelearning.playcarddetect.databinding.ItemCardBinding
import com.machinelearning.playcarddetect.modules.admin.business.ItemCardViewModel
import com.nhatran241.lib_recyclerview_databinding.adapter.BaseAdapter

@BindingAdapter("setUpAdminAdapter")
fun RecyclerView.setUpAdminAdapter(cards : List<ItemCardViewModel>?){
    layoutManager =GridLayoutManager(context,13)
    adapter = cards?.let { CardAdapter(cards) }
}
class CardAdapter( data: List<ItemCardViewModel>) :
    BaseAdapter<ItemCardViewModel>( data) {
    lateinit var binding : ItemCardBinding
    override fun getBindingRoot(parent: ViewGroup, viewType: Int): View {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_card,parent ,false)
        return binding.root
    }
    override fun setBindingViewModel(position: ItemCardViewModel) {
        binding.model =position
    }
}