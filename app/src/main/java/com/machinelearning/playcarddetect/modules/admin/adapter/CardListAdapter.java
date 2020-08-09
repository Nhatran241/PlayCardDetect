package com.machinelearning.playcarddetect.modules.admin.adapter;

import android.app.Activity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.machinelearning.playcarddetect.R;
import com.machinelearning.playcarddetect.common.ImageUtil;
import com.machinelearning.playcarddetect.common.model.Card;
import com.machinelearning.playcarddetect.common.model.CardBase64;
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager;

import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder> {

    private List<CardBase64> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Activity context;
    private String clientID;

    // data is passed into the constructor
    public CardListAdapter(Activity context, List<CardBase64> data,String clientID) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = data;
        this.clientID =clientID;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycleviewitem, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.cardImageView.setImageBitmap(ImageUtil.convert(mData.get(position).getCardBitmap64()));
        holder.cardImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClientDataManager.getInstance().putRemote(clientID,position);
            }
        });
    }
    public static int getImageId(Activity context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView cardImageView;

        ViewHolder(View itemView) {
            super(itemView);
            cardImageView = itemView.findViewById(R.id.iv_card);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}