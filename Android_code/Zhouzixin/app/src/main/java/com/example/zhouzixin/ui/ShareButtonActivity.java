package com.example.zhouzixin.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.zhouzixin.MainActivity2;
import com.example.zhouzixin.R;
import com.example.zhouzixin.sharebutton.ShareButtonView;

public class ShareButtonActivity extends AppCompatActivity {

    ShareButtonView mShareButtonView;
    int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_button);
        mShareButtonView =findViewById(R.id.share_button);
        mShareButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count % 2 == 0)
                {mShareButtonView.reset();
                    Toast.makeText(ShareButtonActivity.this,"ok",Toast.LENGTH_SHORT).show();}
                else
                    mShareButtonView.startAnimation();
                count++;
            }
        });
        /*mShareButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count % 2 == 0)
                    mShareButtonView.reset();
                else
                    mShareButtonView.startAnimation();
                count++;
            }
        });*/
    }
}
