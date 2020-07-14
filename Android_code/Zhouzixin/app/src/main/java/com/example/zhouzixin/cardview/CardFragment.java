package com.example.zhouzixin.cardview;

import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.example.zhouzixin.R;


public class CardFragment extends Fragment {

    private CardView mCardView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adapter, container, false);
        mCardView = (CardView) view.findViewById(R.id.cardView);
        mCardView.setMaxCardElevation(mCardView.getCardElevation()
                * CardAdapter.MAX_ELEVATION_FACTOR);
       /* Button btn= (Button) view.findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),CharsUI.class);
                startActivity(intent);
            }
        });*/
        return view;
    }

    public CardView getCardView() {
        return mCardView;
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
        private LayoutInflater mLayoutInflater;
        public class ViewHolder extends RecyclerView.ViewHolder{
            public Button btn;
            public ViewHolder(View view){
                super(view);
                btn= (Button) view.findViewById(R.id.button1);
            }

        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=mLayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_adapter,parent,false);
            ViewHolder holder=new ViewHolder(view);
            return holder;
        }


        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent=new Intent(getActivity(),CharsUI.class);
                    startActivity(intent);*/
                }
            });

        }


        public int getItemCount() {
            return 1;
        }
    }
}
