package com.project.kongdy.mympchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;

import com.google.gson.annotations.SerializedName;

/**
 * @author kongdy
 *         on 2016/7/26
 *         统计图表主体部分
 */
public class MyChartView extends View {

    private Paint defaultPaint;
    private Paint XAxisPaint;
    private Paint YAxisPaint;
    private Paint chartNetPaint;
    private TextPaint XAxisMarkPaint;
    private TextPaint YAxisMarkPaint;

    private Path netPath;

    private boolean isHighQuality = true;// 高画质
    private boolean openHalo = true;
    private boolean XAxisNet;
    private boolean YAxisNet;
    private boolean leftBottomCornerShow = false;

    private int[] XAxisColors;
    private int[] YAxisColors;

    private int XAxisWidth = -1;
    private int YAxisWidth = -1;

    private float XLabelTextSize = -1;
    private float YLabelTextSize = -1;

    private int dataCount = -1;

    private int mWidth;
    private int mHeight;
    private int leftOffSet; // 左边距偏移

    private SparseArray<Float> XAxisLabel;
    private SparseArray<Float> YAxisLabel;

    private SparseArray<ChartData> datas;

    private DATA_STYLE dataStyle = DATA_STYLE.FOLD_LINE;

    public MyChartView(Context context) {
        super(context);
        init();
    }

    public MyChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        defaultPaint = new Paint();
        XAxisPaint = new Paint();
        YAxisPaint = new Paint();
        chartNetPaint = new Paint();
        XAxisMarkPaint = new TextPaint();
        YAxisMarkPaint = new TextPaint();

        XAxisLabel = new SparseArray<>();
        YAxisLabel = new SparseArray<>();
    }

    private void initProperty() {
        switch (dataStyle) {
            case FOLD_LINE:
                break;
            case COLUMNAR:
                break;
            case FILL_DOT:
                break;
        }
        if (YAxisNet || XAxisNet) {
            chartNetPaint = new Paint();
            chartNetPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            chartNetPaint.setPathEffect(new DashPathEffect(new float[]{4,4}, 1));
            chartNetPaint.setStrokeWidth(getRawSize(TypedValue.COMPLEX_UNIT_DIP,0.5f));
            netPath = new Path();
        }
        if (isHighQuality) {
            openHighQuality(defaultPaint);
            openHighQuality(XAxisPaint);
            openHighQuality(YAxisPaint);
            openHighQuality(chartNetPaint);
            openHighQuality(XAxisMarkPaint);
            openHighQuality(YAxisMarkPaint);
        }
        XAxisPaint.setStrokeWidth(getXAxisWidth());
        YAxisPaint.setStrokeWidth(getYAxisWidth());

        if (XLabelTextSize < 0)
            XLabelTextSize = calculateLabelTextSize();
        if (YLabelTextSize < 0)
            YLabelTextSize = calculateLabelTextSize();

        XAxisMarkPaint.setTextSize(XLabelTextSize);
        YAxisMarkPaint.setTextSize(YLabelTextSize);
        XAxisMarkPaint.setTextAlign(Paint.Align.CENTER);
        YAxisMarkPaint.setTextAlign(Paint.Align.CENTER);

        chartNetPaint.setColor(Color.GRAY);

        leftOffSet = (int) YAxisMarkPaint.measureText(YAxisLabel.get(YAxisLabel.size()-1)+"");
    }

    private float calculateLabelTextSize() {
        float labelsize;
        if (XLabelTextSize > 0) {
            labelsize = XLabelTextSize;
        } else if (YLabelTextSize > 0) {
            labelsize = YLabelTextSize;
        } else {
            labelsize = (mWidth > mHeight ? mHeight : mWidth) / 30;
        }
        return labelsize;
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        initProperty();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), defaultPaint, Canvas.ALL_SAVE_FLAG);

        int unitX = (mWidth -YAxisWidth- leftOffSet - getPaddingLeft() - getPaddingRight()) / (XAxisLabel.size()+1);
        int unitY = (int) ((mHeight -YLabelTextSize- XLabelTextSize - getPaddingTop() - getPaddingBottom()-XAxisWidth)
                / (YAxisLabel.size()+1));
        final int labelTop = (int) (mHeight-unitY*(YAxisLabel.size()+1)- getPaddingBottom()-XLabelTextSize);
        final int labelRight = mWidth-getPaddingRight();
        final int labelLeft = leftOffSet + getPaddingLeft()+YAxisWidth;

        final int[] XColors = getXAxisColors();
        int Xleft = labelLeft;
        int Xtop = (int) (mHeight - XLabelTextSize - getPaddingBottom());
        for (int i = 0; i < XAxisLabel.size(); i++) {
            int color;
            if (i >= XColors.length) {
                color = XColors[XColors.length - 1];
            } else {
                color = XColors[i];
            }
            XAxisPaint.setColor(color);
            if(i == 0 && leftBottomCornerShow) {
                canvas.drawLine(Xleft-leftOffSet, Xtop, Xleft + unitX, Xtop, XAxisPaint);
            } else {
                canvas.drawLine(Xleft, Xtop, Xleft + unitX, Xtop, XAxisPaint);
            }
            Xleft = Xleft + unitX;
            canvas.drawText(XAxisLabel.get(i) + "", Xleft, Xtop+XLabelTextSize, XAxisMarkPaint);
            if(XAxisNet) {
                netPath.reset();
                netPath.moveTo(Xleft,Xtop);
                netPath.lineTo(Xleft,labelTop);
                canvas.drawPath(netPath,chartNetPaint);
            }
        }
        XAxisPaint.setColor(XColors[XColors.length - 1]);
        canvas.drawLine(Xleft, Xtop, Xleft + unitX, Xtop, XAxisPaint);

        final int[] YColors = getYAxisColors();
        int Ytop = (int) (mHeight - XLabelTextSize - getPaddingBottom());
        for (int i = 0; i < YAxisLabel.size(); i++) {
            int color;
            if (i >= YColors.length) {
                color = YColors[YColors.length - 1];
            } else {
                color = YColors[i];
            }
            YAxisPaint.setColor(color);
            canvas.drawLine(labelLeft, Ytop, labelLeft, Ytop - unitY, YAxisPaint);
            Ytop = Ytop - unitY;
            canvas.drawText(YAxisLabel.get(i) + "", labelLeft-(YAxisWidth+leftOffSet)/2, Ytop, YAxisMarkPaint);
            if(YAxisNet) {
                netPath.reset();
                netPath.moveTo(labelLeft,Ytop);
                netPath.lineTo(labelRight,Ytop);
                canvas.drawPath(netPath,chartNetPaint);
            }
        }
        YAxisPaint.setColor(YColors[YColors.length - 1]);
        canvas.drawLine(labelLeft, Ytop, labelLeft, Ytop - unitY, YAxisPaint);

        canvas.restore();
    }


    public boolean isHighQuality() {
        return isHighQuality;
    }

    public void setHighQuality(boolean highQuality) {
        isHighQuality = highQuality;
    }

    public boolean isOpenHalo() {
        return openHalo;
    }

    public void setOpenHalo(boolean openHalo) {
        this.openHalo = openHalo;
    }

    public int[] getXAxisColors() {
        if (XAxisColors == null || XAxisColors.length < 1) {
            XAxisColors = new int[]{Color.BLACK};
        }
        return XAxisColors;
    }

    public void setXAxisColors(int[] XAxisColors) {
        this.XAxisColors = XAxisColors;
    }

    public int[] getYAxisColors() {
        if (YAxisColors == null || YAxisColors.length < 1) {
            YAxisColors = new int[]{Color.BLACK};
        }
        return YAxisColors;
    }

    public void setYAxisColors(int[] YAxisColors) {
        this.YAxisColors = YAxisColors;
    }

    public int getXAxisWidth() {
        if (XAxisWidth == -1) {
            XAxisWidth = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 1);
        }
        return XAxisWidth;
    }

    public void setXAxisWidth(int XAxisWidth) {
        this.XAxisWidth = XAxisWidth;
    }

    public int getYAxisWidth() {
        if (YAxisWidth == -1) {
            YAxisWidth = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 1);
        }
        return YAxisWidth;
    }

    public void setYAxisWidth(int YAxisWidth) {
        this.YAxisWidth = YAxisWidth;
    }

    public DATA_STYLE getDataStyle() {
        return dataStyle;
    }

    public void setDataStyle(DATA_STYLE dataStyle) {
        this.dataStyle = dataStyle;
    }

    /**
     * @param start 开始数字
     * @param end   最终数字
     * @param step  步长
     */
    public void setXAxisLabel(int start, int end, int step) {
        if (start > end) {
            return;
        }
        if (step <= 0 || end - start < step) {
            return;
        }
        int i = 0;
        float tempCursor = start;
        XAxisLabel.clear();
        while (tempCursor <= end) {
            XAxisLabel.put(i, tempCursor);
            tempCursor = tempCursor + step;
            ++i;
        }
    }

    /**
     * @param start 开始数字
     * @param end   最终数字
     * @param step  步长
     */
    public void setYAxisLabel(float start, float end, float step) {
        if (start > end) {
            return;
        }
        if (step <= 0 || end - start < step) {
            return;
        }
        int i = 0;
        float tempCursor = start;
        YAxisLabel.clear();
        while (tempCursor <= end) {
            YAxisLabel.put(i, tempCursor);
            tempCursor = tempCursor + step;
            ++i;
        }
    }

    public boolean isYAxisNet() {
        return YAxisNet;
    }

    public void setYAxisNet(boolean YAxisNet) {
        this.YAxisNet = YAxisNet;
    }

    public boolean isXAxisNet() {
        return XAxisNet;
    }

    public void setXAxisNet(boolean XAxisNet) {
        this.XAxisNet = XAxisNet;
    }

    public void addChartData(ChartData chartData) {
        datas.put(++dataCount, chartData);
    }

    public SparseArray<ChartData> getDatas() {
        return datas;
    }

    public float getXLabelTextSize() {
        return XLabelTextSize;
    }

    public void setXLabelTextSize(int XLabelTextSize) {
        this.XLabelTextSize = XLabelTextSize;
    }

    public float getYLabelTextSize() {
        return YLabelTextSize;
    }

    public void setYLabelTextSize(int YLabelTextSize) {
        this.YLabelTextSize = YLabelTextSize;
    }

    public void setLeftBottomCornerShow(boolean leftBottomCornerShow) {
        this.leftBottomCornerShow = leftBottomCornerShow;
    }

    private float getRawSize(int unit, float value) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(unit, value, displayMetrics);
    }

    private void reDraw() {
        if (Build.VERSION.SDK_INT > 15) {
            postInvalidateOnAnimation();
        } else {
            postInvalidate();
        }
    }

    /**
     * 数据呈现样式
     */
    public static enum DATA_STYLE {
        /**
         * 折线图
         */
        @SerializedName("1")
        FOLD_LINE,
        /**
         * 实心圆
         */
        @SerializedName("2")
        FILL_DOT,
        /**
         * 柱状图
         */
        @SerializedName("3")
        COLUMNAR,
    }
}
