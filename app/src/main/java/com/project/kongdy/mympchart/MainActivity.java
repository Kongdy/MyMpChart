package com.project.kongdy.mympchart;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;


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
        chart1.setYAxisColors(new int[]{Color.rgb(254,145,147)
        ,Color.rgb(255,234,0),Color.rgb(157,222,106),Color.rgb(252,181,3)
        ,Color.rgb(130,151,232),Color.rgb(107,202,220)}); // y轴颜色
        chart1.setYAxisLabel(200,40,null,null); // y轴数据
        chart1.setXAxisLabel(7,1,"3-",null); // x轴数据
        chart1.setYAxisWidth(20); // y轴宽度
        chart1.setXAxisNet(true); // 显示x轴网格
        chart1.setYAxisNet(true); // 显示y轴网格
        chart1.setLeftBottomCornerShow(true); // 让x轴在左下角伸出Y轴显示
        SparseArray<Float> data1 = new SparseArray<>();
        data1.put(1,90f);
        data1.put(2,130f);
        data1.put(3,110f);
        data1.put(4,160f);
        data1.put(5,140f);
        data1.put(6,158f);
        data1.put(7,125f);
        ChartData chartData1 = new ChartData(data1, "data1", new MyChartView.MyMpChartDataProperty() {

            @Override
            public int getColorFilter(int i, float XValue, float YValue) {
                return Color.rgb(203,231,194);
            }
        });
        chartData1.setPointStyle(MyChartView.POINT_STYLE.HOLLOW_OUT_DOT);
        chartData1.setFoldColor(Color.rgb(203,231,194));
        chartData1.setShowMark(false);
        SparseArray<Float> data2 = new SparseArray<>();
        data2.put(1,150f);
        data2.put(2,170f);
        data2.put(3,150f);
        data2.put(4,195f);
        data2.put(5,165f);
        data2.put(6,195f);
        data2.put(7,158f);
        ChartData chartData2 = new ChartData(data2, "data2", new MyChartView.MyMpChartDataProperty() {

            @Override
            public int getColorFilter(int i, float XValue, float YValue) {
                return Color.rgb(252,126,3);
            }
        });
        chartData2.setPointStyle(MyChartView.POINT_STYLE.FILL_DOT);
        chartData2.setFoldColor(Color.TRANSPARENT);

        SparseArray<Float> data3 = new SparseArray<>();
        data3.put(1,60f);
        data3.put(2,85f);
        data3.put(3,65f);
        data3.put(4,110f);
        data3.put(5,80f);
        data3.put(6,110f);
        data3.put(7,75f);
        ChartData chartData3 = new ChartData(data3, "data3", new MyChartView.MyMpChartDataProperty() {

            @Override
            public int getColorFilter(int i, float XValue, float YValue) {
                return Color.rgb(117,191,173);
            }
        });
        chartData3.setPointStyle(MyChartView.POINT_STYLE.FILL_DOT);
        chartData3.setFoldColor(Color.TRANSPARENT);
        chart1.addChartData(chartData1);
       // chart1.addChartData(chartData2);
        //chart1.addChartData(chartData3);
      //  chart1.linkTwoData("data2","data3");
    }
}
