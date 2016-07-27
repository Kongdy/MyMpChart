package com.project.kongdy.mympchart;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.SparseArray;


/**
 * @author kongdy
 *         on 2016/7/27
 * 单组chart数据
 */
public class ChartData {

    public CharSequence name;

    public SparseArray<Float> values;

    private SparseArray<Point> points;

    private Path foldPath; // 折线
    private Paint foldPaint; // 折线画笔
    private Paint entityPaint; // 实体画笔
    private TextPaint markTextPaint; // 图中数字标注画笔

    private int foldColor = Color.GREEN;
    private int circleRadius = -1;
    private int foldWidth = 5;
    private float markTextSize = -1;

    private boolean isShowMark;

    private MyChartView.MyMpChartDataProperty myMpChartDataProperty;

    private MyChartView chartView;

    private SparseArray<Point> XPoints;
    private SparseArray<Point> YPoints;
    private int leftOffSet;
    private float labelValueHeight;
    private float labelvalueWidth;
    private int labelTop;
    private int labelRight;
    private int labelLeft;
    private int labelBottom;
    private int mHeight;
    private int mWidth;
    private int unitX;
    private int unitY;
    private SparseArray<Float> XAxisLabel;
    private SparseArray<Float> YAxisLabel;
    private boolean leftBottomCornerShow;
    private boolean showHalo;

    private MyChartView.DATA_STYLE dataStyle = MyChartView.DATA_STYLE.FOLD_LINE_BEVEL;
    private MyChartView.POINT_STYLE pointStyle = MyChartView.POINT_STYLE.HOLLOW_IN_DOT;


    public ChartData(SparseArray<Float> values,CharSequence name,MyChartView.MyMpChartDataProperty myMpChartDataProperty) {
        this.values = values;
        this.name = name;
        this.myMpChartDataProperty = myMpChartDataProperty;

        init();
    }

    private void init() {
        foldPaint = new Paint();
        entityPaint = new Paint();
        markTextPaint = new TextPaint();
        foldPath = new Path();

        entityPaint.setColor(Color.RED);

        points = new SparseArray<>();
    }

    public void initProperty(MyChartView chartView) {
        this.chartView = chartView;
        XPoints = chartView.getXPoints();
        YPoints = chartView.getYPoints();
        leftOffSet = chartView.getLeftOffSet();
        labelValueHeight = chartView.getLabelValueHeight();
        labelvalueWidth = chartView.getLabelvalueWidth();
        labelTop = chartView.getLabelTop();
        labelRight = chartView.getLabelRight();
        labelLeft = chartView.getLabelLeft();
        labelBottom = chartView.getLabelBottom();
        mHeight = chartView.getmHeight();
        mWidth = chartView.getmWidth();
        unitX = chartView.getUnitX();
        unitY = chartView.getUnitY();
        XAxisLabel = chartView.getXAxisLabel();
        YAxisLabel = chartView.getYAxisLabel();
        leftBottomCornerShow = chartView.isLeftBottomCornerShow();
        showHalo = chartView.isOpenHalo();

        chartView.openHighQuality(foldPaint);
        chartView.openHighQuality(entityPaint);
        chartView.openHighQuality(markTextPaint);
    }


    public void calculateCoords() {
        if(values == null) {
            return;
        }
        float YAxisDistance = Math.abs(YPoints.get(0).y-YPoints.get(YPoints.size()-1).y)-unitY;
        float XAxisDistance = Math.abs(XPoints.get(0).x-XPoints.get(XPoints.size()-1).x)-unitX;
        if(leftBottomCornerShow) {
            XAxisDistance = XAxisDistance-leftOffSet;
        }
        for (int i = 0;i < values.size();i++) {
            Point point = new Point();
            point.x = (int)((values.keyAt(i)/labelvalueWidth)*XAxisDistance+labelLeft);
            final float valueY = values.valueAt(i);
            point.y = (int)(YAxisDistance-(valueY/labelValueHeight)*YAxisDistance+labelTop+unitY);
            points.put(i,point);
        }
        if(markTextSize == -1)
            markTextSize = (XAxisDistance>YAxisDistance?YAxisDistance:XAxisDistance)/20;

        foldPaint.setColor(foldColor);
        foldPaint.setStyle(Paint.Style.STROKE);
        foldPaint.setStrokeWidth(foldWidth);
        markTextPaint.setColor(Color.BLACK);
        markTextPaint.setTextSize(markTextSize);

        if(circleRadius == -1)
            circleRadius = 10;
        switch (pointStyle) {
            case FILL_DOT:
                break;
            case HOLLOW_IN_DOT:
                break;
            case HOLLOW_OUT_DOT:
                break;
            case SQUARE_DOT:
                break;
            case NONE_DOT:
                break;
        }
    }

    private void openHalo() {
        if(showHalo) {
            BlurMaskFilter blurMaskFilter = new BlurMaskFilter(50, BlurMaskFilter.Blur.SOLID);
            entityPaint.setMaskFilter(blurMaskFilter);
        }
    }

    private void closeHalo() {
        entityPaint.setMaskFilter(null);
    }

    public void drawSelf(Canvas canvas) {
        foldPath.reset();
        int i = 0;
        while(null != points.get(i)) {
            Point point = points.get(i);
            switch (dataStyle) {
                case FOLD_LINE_BEVEL:
                    if(i == 0)
                        foldPath.moveTo(point.x,point.y);
                    else
                        foldPath.lineTo(point.x,point.y);
                    break;
                case FOLD_LINE_ROUND:
                    break;
                case COLUMNAR:
                    break;
            }
            canvas.drawPath(foldPath,foldPaint);
            ++i;
        }
        i = 0;
        while(null != points.get(i)) {
            Point point = points.get(i);
            if(pointStyle == MyChartView.POINT_STYLE.HOLLOW_IN_DOT || pointStyle == MyChartView.POINT_STYLE.HOLLOW_OUT_DOT) {
                int innerRadius = 0;
                int externalRadius = 0;
                if(pointStyle == MyChartView.POINT_STYLE.HOLLOW_IN_DOT) {
                    innerRadius = circleRadius;
                    externalRadius = circleRadius*3/4;
                } else {
                    externalRadius = circleRadius;
                    innerRadius = circleRadius*3/4;
                }
                entityPaint.setColor(Color.WHITE);
                canvas.drawCircle(point.x, point.y, innerRadius, entityPaint);
                if (myMpChartDataProperty != null) {
                    entityPaint.setColor(myMpChartDataProperty.getColorFilter(i, values.keyAt(i), values.valueAt(i)));
                } else {
                    entityPaint.setColor(Color.BLACK);
                }
                openHalo();
                canvas.drawCircle(point.x, point.y,externalRadius, entityPaint);
                closeHalo();
            }

            String text = values.valueAt(i) + "";
            if (!TextUtils.isEmpty(text) && !"null".equals(text)) {
                canvas.drawText(text, point.x + 20, point.y + 20, markTextPaint);
            } else {
                canvas.drawText("0", point.x + 20, point.y + 20, markTextPaint);
            }
            ++i;
        }

    }

    public void setDataStyle(MyChartView.DATA_STYLE dataStyle) {
        this.dataStyle = dataStyle;
    }

    public void setPointStyle(MyChartView.POINT_STYLE pointStyle) {
        this.pointStyle = pointStyle;
    }

    public void setFoldColor(int foldColor) {
        this.foldColor = foldColor;
    }

    public void setFoldWidth(int foldWidth) {
        this.foldWidth = foldWidth;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
    }

    public void setShowMark(boolean showMark) {
        isShowMark = showMark;
    }

    public void setMarkTextSize(float markTextSize) {
        this.markTextSize = markTextSize;
    }
}