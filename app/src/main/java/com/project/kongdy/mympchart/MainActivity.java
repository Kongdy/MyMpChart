package com.project.kongdy.mympchart;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private MyChartView chart1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chart1 = (MyChartView) findViewById(R.id.chart1);

        // style1
//        chart1.setXAxisColors(new int[]{Color.RED,Color.BLUE,
//        Color.GREEN,Color.BLACK}); // x轴颜色
        chart1.setYAxisColors(new int[]{Color.RED,Color.BLUE,
                Color.GREEN,Color.BLACK,Color.YELLOW}); // y轴颜色
        chart1.setYAxisLabel(50,200,50); // y轴数据
        chart1.setXAxisLabel(0,8,1); // x轴数据
        chart1.setYAxisWidth(20); // y轴宽度
        chart1.setXAxisNet(true); // 显示x轴网格
        chart1.setYAxisNet(true); // 显示y轴网格
        chart1.setLeftBottomCornerShow(true); // 让x轴在左下角伸出Y轴显示
    }
}
