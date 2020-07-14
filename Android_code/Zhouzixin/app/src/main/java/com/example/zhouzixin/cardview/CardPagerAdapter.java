package com.example.zhouzixin.cardview;

import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

import com.example.zhouzixin.MainActivity2;
import com.example.zhouzixin.MainActivity3;
import com.example.zhouzixin.R;
import com.example.zhouzixin.ui.LineChartActivity;
import com.example.zhouzixin.ui.ShareButtonActivity;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;
    //Main3Activity active;
    Context context;
    public CardPagerAdapter() {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.adapter, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);
        this.context= MainActivity2.context;
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position==0){
                    Intent intent=new Intent(context,LineChartActivity.class);
                    context.startActivity(intent);
                }else if(position==1){
                    Intent intent=new Intent(context, ShareButtonActivity.class);
                    context.startActivity(intent);
                }else if (position==2){
                    Intent intent=new Intent(context, MainActivity3.class);
                    context.startActivity(intent);
                }/*else if (position==3){
                    Intent intent=new Intent(context, LineChartActivity.class);
                    context.startActivity(intent);
                }*/

            }
        });

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(final CardItem item, View view) {
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView contentTextView =view.findViewById(R.id.contentTextView);
        titleTextView.setText(item.getTitle());
        contentTextView.setText(item.getText());
        Button btn= (Button) view.findViewById(R.id.button1);
        btn.setText(item.getButton());
        /*btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(item.getButton())=="温度曲线"){
                    Intent intent=new Intent(active.getBaseContext(),CharsUI.class);
                    active.startActivity(intent);
                }
            }
        });*/
    }

}
