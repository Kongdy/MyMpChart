package com.project.kongdy.mympchart;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;

import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * @author kongdy
 *         on 2016/7/26
 *         统计图表主体部分
 */
public class MyChartView extends View implements ObjectAnimator.AnimatorUpdateListener {

    private Paint defaultPaint;
    private Paint XAxisPaint;
    private Paint YAxisPaint;
    private Paint chartNetPaint;
    private TextPaint XAxisMarkPaint;
    private TextPaint YAxisMarkPaint;
    private Paint linkPaint;

    private Path netPath;

    private boolean openHalo = true;
    private boolean XAxisNet;
    private boolean YAxisNet;
    private boolean leftBottomCornerShow = false;
    private boolean showXAxisFirst;
    private boolean showYAxisFirst;
    private boolean showTimeXAxis; // 设置时间X轴

    private int[] XAxisColors;
    private int[] YAxisColors;

    private int XAxisWidth = -1;
    private int YAxisWidth = -1;

    private float XLabelTextSize = -1;
    private float YLabelTextSize = -1;

    private float labelValueHeight;
    private float labelvalueWidth;
    private long labelValueWidthLong; // 时间轴的长度

    private int dataCount = -1;

    private int mWidth;
    private int mHeight;
    private int leftOffSet; // 左边距偏移
    private int labelTop;
    private int labelRight;
    private int labelLeft;
    private int labelBottom;
    private int unitX;
    private int unitY;

    private ChartAnimator animator;

    private SparseArray<Float> XAxisLabel;
    private SparseArray<Float> YAxisLabel;
    private SparseArray<Long> XAxisLabelLong;
    private SparseArray<Point> XPoints;
    private SparseArray<Point> YPoints;
    private String XFrontUnit;
    private String YFrontUnit;
    private String XBehindUnit;
    private String YBehindUnit;
    private int XRetainCount; // x轴保留小数
    private int YRetainCount; // y轴保留小数
    private StringBuilder XAxisFormat;
    private StringBuilder YAxisFormat;

    private SparseArray<ChartData> datas;

    /**
     * 用于产生多个数据之间的连接条
     */
    private String link1;
    private String link2;
    ChartData chartDataLink1;
    ChartData chartDataLink2;

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
        linkPaint = new Paint();
        XAxisMarkPaint = new TextPaint();
        YAxisMarkPaint = new TextPaint();

        XAxisLabel = new SparseArray<>();
        YAxisLabel = new SparseArray<>();
        XPoints = new SparseArray<>();
        YPoints = new SparseArray<>();
        datas = new SparseArray<>();

        animator = new ChartAnimator(this);

    }

    private void initProperty() {
        if (YAxisNet || XAxisNet) {
            chartNetPaint = new Paint();
            chartNetPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            chartNetPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 1));
            chartNetPaint.setStrokeWidth(getRawSize(TypedValue.COMPLEX_UNIT_DIP, 0.5f));
            netPath = new Path();
        }

        openHighQuality(defaultPaint);
        openHighQuality(XAxisPaint);
        openHighQuality(YAxisPaint);
        openHighQuality(chartNetPaint);
        openHighQuality(XAxisMarkPaint);
        openHighQuality(YAxisMarkPaint);
        openHighQuality(linkPaint);

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

        linkPaint.setStrokeWidth(getRawSize(TypedValue.COMPLEX_UNIT_DIP, 2));

        leftOffSet = (int) YAxisMarkPaint.measureText(YAxisLabel.get(YAxisLabel.size() - 1) + "");

        // 计算位数
        XAxisFormat = new StringBuilder("#");
        if (XRetainCount > 0)
            XAxisFormat.append(".");
        for (int i = 0; i < XRetainCount; i++) {
            XAxisFormat.append("#");
        }
        YAxisFormat = new StringBuilder("#");
        if (YRetainCount > 0)
            YAxisFormat.append(".");
        for (int i = 0; i < YRetainCount; i++) {
            YAxisFormat.append("#");
        }
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

    public void openHighQuality(Paint paint) {
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
        calculateCoords();
    }

    private void calculateCoords() {
        XPoints.clear();
        YPoints.clear();
        int Xcount;
        if(showTimeXAxis) {
            Xcount = XAxisLabelLong.size();
        } else {
            Xcount = XAxisLabel.size();
        }

        unitX = (mWidth - YAxisWidth - leftOffSet - getPaddingLeft() - getPaddingRight()) / Xcount;
        unitY = (int) ((mHeight - YLabelTextSize - XLabelTextSize - getPaddingTop() - getPaddingBottom() - XAxisWidth)
                / (YAxisLabel.size()));
        labelTop = (int) (mHeight - unitY * (YAxisLabel.size()) - getPaddingBottom() - XLabelTextSize);
        labelRight = mWidth - getPaddingRight();
        labelLeft = leftOffSet + getPaddingLeft() + YAxisWidth;
        labelBottom = (int) (mHeight - XLabelTextSize - getPaddingBottom());
        // X
        int XLeft = labelLeft;
        for (int i = 0; i <= Xcount; i++) {
            Point point = new Point();
            if (leftBottomCornerShow && i == 0) {
                point.x = XLeft - leftOffSet;
            } else {
                point.x = XLeft;
            }
            point.y = labelBottom;
            XLeft = XLeft + unitX;
            XPoints.put(i, point);
        }
        // Y
        int YBottom = labelBottom;
        for (int i = 0; i <= YAxisLabel.size(); i++) {
            Point point = new Point();
            point.x = labelLeft;
            point.y = YBottom;
            YBottom = YBottom - unitY;
            YPoints.put(i, point);
        }
        // data
        if (datas != null) {
            int i = 0;
            while (null != datas.get(i)) {
                datas.get(i).initProperty(this);
                datas.get(i).calculateCoords();
                ++i;
            }
        }

        // check data link
        initLink();
    }

    private String getFormatLabel(StringBuilder format, float text) {
        DecimalFormat df = new DecimalFormat(format.toString());
        return df.format(text);
    }

    private void initLink() {
        if (datas == null)
            return;
        if (TextUtils.isEmpty(link1) || TextUtils.isEmpty(link2))
            return;
        if (link1.equals(link2))
            return;

        chartDataLink1 = null;
        chartDataLink2 = null;
        for (int i = 0; i < datas.size(); i++) {
            if (link1.equals(datas.get(i).name)) {
                chartDataLink1 = datas.get(i);
            } else if (link2.equals(datas.get(i).name)) {
                chartDataLink2 = datas.get(i);
            } else if (chartDataLink1 != null && chartDataLink2 != null) {
                break;
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), defaultPaint, Canvas.ALL_SAVE_FLAG);
        // draw X
        final int[] XColors = getXAxisColors();
        for (int i = 0; i < XPoints.size() - 1; i++) {
            XAxisPaint.setColor(getColor(i, XColors));
            canvas.drawLine(XPoints.get(i).x, XPoints.get(i).y, XPoints.get(i + 1).x, XPoints.get(i + 1).y, XAxisPaint);
            if (showXAxisFirst || i > 0) {
                String text;
                if(showTimeXAxis) {
                    text = getDateStr(XAxisLabelLong.get(i));
                } else {
                    text =   XFrontUnit + getFormatLabel(XAxisFormat, XAxisLabel.get(i)) + XBehindUnit;
                }
                canvas.drawText(text, XPoints.get(i).x, XPoints.get(i).y + XLabelTextSize, XAxisMarkPaint);
            }
            if (i != 0 && i != XPoints.size() - 1) {
                if (XAxisNet) {
                    netPath.rewind();
                    netPath.moveTo(XPoints.get(i).x, XPoints.get(i).y);
                    netPath.lineTo(XPoints.get(i).x, labelTop);
                    canvas.drawPath(netPath, chartNetPaint);
                }
            }
        }

        // drawY
        final int[] YColors = getYAxisColors();
        for (int i = 0; i < YPoints.size() - 1; i++) {
            YAxisPaint.setColor(getColor(i, YColors));
            canvas.drawLine(YPoints.get(i).x, YPoints.get(i).y, YPoints.get(i + 1).x, YPoints.get(i + 1).y, YAxisPaint);
            if (showYAxisFirst || i > 0) {
                String text = YFrontUnit + getFormatLabel(YAxisFormat, YAxisLabel.get(i)) + YBehindUnit;
                canvas.drawText(text, YPoints.get(i).x - (YAxisWidth + leftOffSet) / 2, YPoints.get(i).y, YAxisMarkPaint);
            }
            if (i != 0 && i != YPoints.size() - 1) {
                if (YAxisNet) {
                    netPath.rewind();
                    netPath.moveTo(labelLeft, YPoints.get(i).y);
                    netPath.lineTo(labelRight, YPoints.get(i).y);
                    canvas.drawPath(netPath, chartNetPaint);
                }
            }
        }

        // draw link
        if (chartDataLink1 != null && chartDataLink2 != null) {
            int linkCount = chartDataLink1.getTruthCount() > chartDataLink2.getTruthCount() ?
                    chartDataLink1.getTruthCount() : chartDataLink2.getTruthCount();
            for (int i = 0; i < linkCount; i++) {
                if (chartDataLink1.mPointsBuffer.get(i).x == chartDataLink2.mPointsBuffer.get(i).x) {
                    drawLink(i, linkPaint, canvas);
                }
            }
        }

        // draw data
        if (datas != null) {
            int i = 0;
            while (null != datas.get(i)) {
                datas.get(i).drawSelf(canvas);
                ++i;
            }
        }
        canvas.restore();
    }

    private void drawLink(int i, Paint paint, Canvas canvas) {
        final int sX = chartDataLink1.mPointsBuffer.get(i).x;
        final int sY = (int) chartDataLink1.getBufferY(i);
        final int eY = (int) chartDataLink2.getBufferY(i);
        final int[] colors = {chartDataLink1.initEntityColor(i), chartDataLink2.initEntityColor(i)};
        LinearGradient linearGradient = new LinearGradient(sX, sY, sX, eY
                , colors, null, Shader.TileMode.MIRROR);
        paint.setShader(linearGradient);
        canvas.drawLine(sX, sY, sX, eY, linkPaint);
    }

    private int getColor(int i, int[] colors) {
        if (i >= colors.length) {
            return colors[colors.length - 1];
        } else {
            return colors[i];
        }
    }

    public ChartAnimator getAnimator() {
        return animator;
    }

    public void animalX(long time) {
        animator.animalX(time);
    }

    public void animalY(long time){
        animator.animalY(time);
    }

    public void animalXY(long time) {
        animator.animalXY(time);
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

    /**
     * @param end  最终数字
     * @param step 步长
     */
    public void setXAxisLabel(int end, int step, String frontUnit, String behindUnit) {
        showTimeXAxis = false;
        int start = 0;
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
        labelvalueWidth = XAxisLabel.valueAt(XAxisLabel.size() - 1) - start;
        this.XFrontUnit = frontUnit == null ? "" : frontUnit;
        this.XBehindUnit = behindUnit == null ? "" : behindUnit;
    }

    /**
     * @param end  最终数字
     * @param step 步长
     */
    public void setYAxisLabel(float end, float step, String frontUnit, String behindUnit) {
        float start = 0;
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
        labelValueHeight = YAxisLabel.valueAt(YAxisLabel.size() - 1) - start;
        this.YFrontUnit = frontUnit == null ? "" : frontUnit;
        this.YBehindUnit = behindUnit == null ? "" : behindUnit;
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

    public void remmoveData(int i) {
        datas.removeAt(i);
        dataCount--;
    }

    /**
     * 直接设定外部设置好的整组数据，如果之前有设定数据，会覆盖原有数据
     * @param datas
     */
    public void setChartDatas(SparseArray<ChartData> datas) {
        this.datas = datas;
    }

    public SparseArray<ChartData> getChartDatas() {
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

    public void setShowYAxisFirst(boolean showYAxisFirst) {
        this.showYAxisFirst = showYAxisFirst;
    }

    public void setShowXAxisFirst(boolean showXAxisFirst) {
        this.showXAxisFirst = showXAxisFirst;
    }

    public float getRawSize(int unit, float value) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(unit, value, displayMetrics);
    }

    public void reDraw() {
        if (Build.VERSION.SDK_INT > 15) {
            postInvalidateOnAnimation();
        } else {
            postInvalidate();
        }
    }


    public int getLeftOffSet() {
        return leftOffSet;
    }

    public int getLabelTop() {
        return labelTop;
    }

    public int getLabelRight() {
        return labelRight;
    }

    public int getLabelLeft() {
        return labelLeft;
    }

    public int getLabelBottom() {
        return labelBottom;
    }

    public SparseArray<Point> getXPoints() {
        return XPoints;
    }

    public SparseArray<Point> getYPoints() {
        return YPoints;
    }

    public SparseArray<Float> getYAxisLabel() {
        return YAxisLabel;
    }

    public SparseArray<Float> getXAxisLabel() {
        return XAxisLabel;
    }

    public boolean isLeftBottomCornerShow() {
        return leftBottomCornerShow;
    }

    public int getmWidth() {
        return mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    public float getLabelvalueWidth() {
        return labelvalueWidth;
    }

    public float getLabelValueHeight() {
        return labelValueHeight;
    }

    public int getUnitX() {
        return unitX;
    }

    public int getUnitY() {
        return unitY;
    }

    /**
     * 设置x轴小数点保留位数
     *
     * @param XRetainCount
     */
    public void setXRetainCount(int XRetainCount) {
        this.XRetainCount = XRetainCount;
    }

    /**
     * 设置y轴小数点保留位数
     *
     * @param YRetaincount
     */
    public void setYRetaincount(int YRetaincount) {
        this.YRetainCount = YRetaincount;
    }

    /**
     * 设置时间根据天数来产生坐标的X轴
     * 该设置会与直接设置X轴产生冲突，
     * 最终显示结果依据先后顺序，后者会被配置
     * @param showTimeXAxis 是否显示
     * @param dayCount 需要显示得天数
     * @param step 步长
     */
    public void setShowTimeXAxis(boolean showTimeXAxis,int dayCount,int step) {
        this.showTimeXAxis = showTimeXAxis;
        int i = 0;
        int count = 0;
        XAxisLabelLong = new SparseArray<>();
        while(i < dayCount) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH,i-dayCount);
            XAxisLabelLong.put(count,calendar.getTimeInMillis());
            i = i+step;
            ++count;
        }
        labelValueWidthLong = XAxisLabelLong.get(XAxisLabelLong.size()-1)-XAxisLabelLong.get(0);
    }

    public String getDateStr(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        return month+"-"+day;
    }

    public long getLabelValueWidthLong() {
        return labelValueWidthLong;
    }

    public boolean isShowTimeXAxis() {
        return showTimeXAxis;
    }

    public SparseArray<Long> getXAxisLabelLong() {
        return XAxisLabelLong;
    }

    /**
     * 将两组数据连接起来
     *
     * @param name1
     * @param name2
     */
    public void linkTwoData(String name1, String name2) {
        this.link1 = name1;
        this.link2 = name2;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        reDraw();
    }


    /**
     * 数据呈现样式
     */
    public static enum DATA_STYLE {
        /**
         * 折线图,直线
         */
        @SerializedName("1")
        FOLD_LINE_BEVEL,
        /**
         * 折线图,曲线
         */
        @SerializedName("2")
        FOLD_LINE_ROUND,

        /**
         * 柱状图
         */
        @SerializedName("4")
        COLUMNAR,
    }

    /**
     * 点的样式
     */
    public static enum POINT_STYLE {
        /**
         * 实心圆点
         */
        @SerializedName("1")
        FILL_DOT,

        /**
         * 空心圆点，内实外空
         */
        @SerializedName("2")
        HOLLOW_OUT_DOT,

        /**
         * 空心圆点，外空内实
         */
        @SerializedName("5")
        HOLLOW_IN_DOT,

        /**
         * 方形点
         */
        @SerializedName("3")
        SQUARE_DOT,

        /**
         * 不画圆点
         */
        @SerializedName("4")
        NONE_DOT,
    }

    public interface MyMpChartDataProperty {
        /**
         * 颜色过滤器
         *
         * @param i
         * @param XValue
         * @param YValue
         * @return
         */
        int getColorFilter(int i, float XValue, float YValue);
    }

}
