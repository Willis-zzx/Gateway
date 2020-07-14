package com.example.zhouzixin.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.zhouzixin.MainActivity2;
import com.example.zhouzixin.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LineChartActivity extends AppCompatActivity {
    private LineChart lineChart;
    private List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
    private List<HashMap<String, Object>> mData = new ArrayList<HashMap<String, Object>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);
        initchart();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent=new Intent(LineChartActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });
    }
    public void initchart() {
        lineChart =findViewById(R.id.linechart);
        //设置支持触控手势
        lineChart.setTouchEnabled(true);
        //设置缩放
        lineChart.setDragEnabled(true);
        //设置推动
        lineChart.setScaleEnabled(true);
        //如果禁用,扩展可以在x轴和y轴分别完成
        lineChart.setPinchZoom(true);
        //showData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showData();
            }
        },1000);
        //setLineChartData();
    }
    /**
     * 设置折线图的数据
     */
    private void showData() {
        String url = "http://101.200.164.203/json/select1.php";
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                list = JSON.parseObject(result,
                        new TypeReference<List<HashMap<String, Object>>>() {
                        });
                mData.addAll(list);
                //填充数据，在这里换成自己的数据源
                List<Entry> valsComp1 = new ArrayList<>();
                List<Entry> valsComp2 = new ArrayList<>();
                for (int i=0;i<mData.size();i++)
                {
                    try{
                        valsComp1.add(new Entry(i,Integer.parseInt(mData.get(i).get("temp").toString())));
                    }catch (Exception e){
                        valsComp1.add(new Entry(i,0));
                    }
                    try {
                        valsComp2.add(new Entry(i,Integer.parseInt(mData.get(i).get("humid").toString())));
                    }catch (Exception e){
                        valsComp2.add(new Entry(i,0));
                    }
                }
                //这里，每重新new一个LineDataSet，相当于重新画一组折线
                //每一个LineDataSet相当于一组折线。比如:这里有两个LineDataSet：setComp1，setComp2。
                //则在图像上会有两条折线图，分别表示公司1 和 公司2 的情况.还可以设置更多
                LineDataSet setComp1 = new LineDataSet(valsComp1, "温度");
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setColor(getResources().getColor(android.R.color.holo_blue_light));
                setComp1.setDrawCircles(false);
                setComp1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

                LineDataSet setComp2 = new LineDataSet(valsComp2, "湿度");
                setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp2.setDrawCircles(true);
                setComp2.setColor(getResources().getColor(android.R.color.holo_red_dark));
                setComp2.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

                List<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(setComp1);
                dataSets.add(setComp2);
                //dataSets.add(setComp2);
                LineData lineData = new LineData(dataSets);
                lineChart.setData(lineData);
                lineChart.invalidate();
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                Toast.makeText(x.app(),throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException e) {
                Toast.makeText(x.app(),"cancelled",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinished() {

            }
        });
    }
}