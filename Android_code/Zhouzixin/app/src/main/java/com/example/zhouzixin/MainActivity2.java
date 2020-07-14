package com.example.zhouzixin;

import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhouzixin.cardview.CardFragmentPagerAdapter;
import com.example.zhouzixin.cardview.CardItem;
import com.example.zhouzixin.cardview.CardPagerAdapter;
import com.example.zhouzixin.cardview.ShadowTransformer;
import com.example.zhouzixin.mqtt.MqttManager;
import com.example.zhouzixin.tinker.BaseActivity;
import com.example.zhouzixin.tinker.TickerView;
import com.lichfaker.log.Logger;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Random;


public class MainActivity2 extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private Button btnconnect;
    private Button btndisconnect;
    private ViewPager mViewPager;
    private ImageView imageView;
    private Switch aSwitch;

    private TextView txttemp, txthumid;
    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private CardFragmentPagerAdapter mFragmentCardAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;

    private boolean mShowingFragments = false;

    //private MyBtnClicker myBtnClicker = new MyBtnClicker();
    private final MyHandler myHandler = new MyHandler(this);
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    public static Context context;
    //DialChart03View chart = null;
    private TickerView ticker1, ticker2;
    public static final String URL = "tcp://***:1883";
    private String username = "";
    private String password = "";
    private String clientId = "";
    String cmdon = "1";
    String cmdoff = "0";
    boolean b;
    String strtemp;
    String strhumid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btnconnect = findViewById(R.id.connectMQTT);
        btndisconnect = findViewById(R.id.disconnectMQTT);
        imageView=findViewById(R.id.imageView);
        aSwitch=findViewById(R.id.switch1);
        btndisconnect.setEnabled(false);
        btnconnect.setEnabled(true);
        //mButton.setOnClickListener(this);
        //MQTT连接
        btnconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        b = MqttManager.getInstance().creatConnect(URL, username, password, clientId);
                        Logger.d("isConnected: " + b);
                    }
                }).start();
                if (b == true) {
                    btndisconnect.setEnabled(true);
                    btnconnect.setEnabled(false);
                    Toast.makeText(MainActivity2.this, "连接成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //MQTT断开连接
        btndisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MqttManager.getInstance().disConnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Toast.makeText(MainActivity2.this, "连接断开", Toast.LENGTH_SHORT).show();
                btnconnect.setEnabled(true);
                btndisconnect.setEnabled(false);
            }
        });

        //MQTT控制
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.f_on));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MqttManager.getInstance().publish("test",1,cmdon.getBytes());
                        }
                    }).start();
                    Toast.makeText(MainActivity2.this,"开灯!", Toast.LENGTH_SHORT).show();
                }else{
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.f_off));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MqttManager.getInstance().publish("test",1,cmdoff.getBytes());
                        }
                    }).start();
                    Toast.makeText(MainActivity2.this,"关灯!", Toast.LENGTH_SHORT).show();
                }
            }
        });



        txttemp = findViewById(R.id.txttemp);
        txthumid = findViewById(R.id.txthumid);
        ticker1 = findViewById(R.id.ticker1);
        ticker2 = findViewById(R.id.ticker2);
        ticker1.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        ticker2.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);

        context = this;
        bindReceiver();
    }

    @Override
    public void onClick(View view) {

    }

    public static float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mCardShadowTransformer.enableScaling(b);
        mFragmentCardShadowTransformer.enableScaling(b);
    }

    /*
     *   TCP连接
     */
    private class MyHandler extends android.os.Handler {
        private final WeakReference<MainActivity2> mActivity;

        MyHandler(MainActivity2 activity) {
            mActivity = new WeakReference<MainActivity2>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity2 activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 1:
                        String str = msg.obj.toString();
                        String temp = str.substring(0, str.length() - 2);
                        String humid = str.substring(str.length() - 2, str.length());
                        //txttemp.setText(temp);
                        ticker1.setText(temp);
                        strtemp=ticker1.getText().toString();
                        //txthumid.setText(humid);
                        ticker2.setText(humid);
                        strhumid=ticker2.getText().toString();
                        //txttemp.append(msg.obj.toString());
                        break;
                    /*case 2:
                        txtSend.append(msg.obj.toString());
                        break;*/
                }
            }
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            switch (mAction) {
                case "tcpServerReceiver":
                    String msg = intent.getStringExtra("tcpServerReceiver");
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = msg;
                    myHandler.sendMessage(message);
                    break;
            }
        }
    }

    private void bindReceiver() {
        IntentFilter intentFilter = new IntentFilter("tcpServerReceiver");
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    protected void onUpdate() {
        final int digits = RANDOM.nextInt(2) + 6;
        //ticker1.setText(getRandomNumber(digits));
        //ticker2.setText(getRandomNumber(digits));
        //final String currencyFloat = Float.toString(RANDOM.nextFloat() * 100);
    }

    private String generateChars(Random random, String list, int numDigits) {
        final char[] result = new char[numDigits];
        for (int i = 0; i < numDigits; i++) {
            result[i] = list.charAt(random.nextInt(list.length()));
        }
        return new String(result);
    }

    /*
    *   MQTT
     */
    /**
     * 订阅接收到的消息
     * 这里的Event类型可以根据需要自定义, 这里只做基础的演示
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showdata ( String event){
        String[] msg=event.split(",");
        if(msg[0].equals("w"))
        {
            //txtWenDu.setText(msg[1]);
        }
        if (msg[0].equals("s"))
        {
            //txtShiDu.setText(msg[1]);
        }
        Toast.makeText( this ,msg[0]+msg[1], Toast.LENGTH_SHORT).show();
    };

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}