package com.example.zhouzixin;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhouzixin.CircularProgressButton.CircularProgressButton;
import com.example.zhouzixin.coms.TcpServer;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    TextView txtserverIp,txtserverId;
    EditText editserverport;
    CircularProgressButton circularButton;
    private static TcpServer tcpServer = null;
    ExecutorService exec = Executors.newCachedThreadPool();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindID();
        viewinit();
    }

    /*
     * 初始化文本框
     * */
    private void viewinit(){
        txtserverIp.setText(getHostIP());
    }
    private void bindID(){
        txtserverIp=findViewById(R.id.txt_serverip);
        txtserverId=findViewById(R.id.edit_serverid);
        editserverport=findViewById(R.id.edit_serverport);
        circularButton=findViewById(R.id.circularButton);
        circularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(circularButton.getProgress()==0){
                    simulateSuccessProgress(circularButton);
                    tcpServer=new TcpServer(getHost(editserverport.getText().toString()));
                    exec.execute(tcpServer);
                    boolean b1=startPing(getHostIP());
                    if(b1==true){
                        Handler handler=new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(MainActivity.this, MainActivity2.class);
                                startActivity(intent);
                            }
                        },2000);
                    }else{
                        circularButton.setProgress(0);
                        Toast.makeText(MainActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    tcpServer.closeSelf();
                    circularButton.setProgress(0);
                }
            }
        });
    }
    /**
     * 获取ip地址
     * @return
     */
    public String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("FuncTcpServer", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }
    //
    private int getHost(String msg){
        if (msg.equals("")){
            msg = "1234";
        }
        return Integer.parseInt(msg);
    }
    private void simulateSuccessProgress(final CircularProgressButton button) {
        ValueAnimator widthAnimation = ValueAnimator.ofInt(1, 100);
        widthAnimation.setDuration(1500);
        widthAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                button.setProgress(value);
            }
        });
        widthAnimation.start();
    }

    //检查TCP是否连接成功
    private boolean startPing(String ip){	Log.e("Ping", "startPing...");
        boolean success=false;
        Process p =null;
        try {
            p = Runtime.getRuntime().exec("ping -c 1 -i 0.2 -W 1 " +ip);
            int status = p.waitFor();
            if (status == 0) {
                success=true;
            } else {
                success=false;
            }
        } catch (IOException e) {
            success=false;
        } catch (InterruptedException e) {
            success=false;
        }finally{
            p.destroy();
        }
        return success;
    }
}