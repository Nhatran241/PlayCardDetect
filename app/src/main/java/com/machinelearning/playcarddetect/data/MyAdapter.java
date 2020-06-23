package com.machinelearning.playcarddetect.data;

import android.graphics.Bitmap;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.machinelearning.playcarddetect.R;
import com.machinelearning.playcarddetect.data.card.Card;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<Card> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imagesuit,imagelevel;
        public TextView cardname;
        public MyViewHolder(View view) {
            super(view);
            imagesuit = view.findViewById(R.id.iv_cardsuit);
            imagelevel = view.findViewById(R.id.iv_cardlevel);
            cardname = view.findViewById(R.id.tv_card);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<Card> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.imagesuit.setImageBitmap(mDataset.get(position).getCardsuit().getSuitImage());
        holder.imagelevel.setImageBitmap(mDataset.get(position).getCardLevel().getCardLevelBitmap());
        holder.cardname.setText(mDataset.get(position).getCardLevel().getCardLevel()+" : "+mDataset.get(position).getCardsuit().getSuitType().name());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}