package com.project.kongdy.mympchart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.TextPaint;
import android.util.SparseArray;

/**
 * @author kongdy
 *         on 2016/7/26
 * 单组chart数据
 */
public class ChartData {


    public CharSequence name;

    /**
     * x轴数据
     */
    public float[] xValue;
    /**
     * y轴数据
     */
    public float[] yValue;


    private SparseArray<Point> points;

    private Path foldPath; // 折线

    private Paint foldPaint; // 折线画笔

    private Paint entityPaint; // 实体画笔

    private TextPaint markTextPaint; // 图中数字标注画笔

    private MyMpChartDataProperty myMpChartDataProperty;

    public ChartData(float[] xValue, float[] yValue,CharSequence name) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.name = name;

        init();
    }

    private void init() {
        foldPaint = new Paint();
        entityPaint = new Paint();
        markTextPaint = new TextPaint();

        foldPath = new Path();

        points = new SparseArray<>();

        openHighQuality(foldPaint);
        openHighQuality(entityPaint);
        openHighQuality(markTextPaint);
    }


    public void initProperty(SparseArray<Float> xLabel) {

    }

    public void drawSelf(Canvas canvas) {


    }

    private void openHighQuality(Paint paint) {
        if (paint == null) {
            return;
        }
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        if (paint instanceof TextPaint) {
            paint.setSubpixelText(true);
        }
    }

    public interface MyMpChartDataProperty {
        /**
         * 颜色过滤器
         * @param i
         * @return
         */
        int getColorFilter(int i);
    }

}
