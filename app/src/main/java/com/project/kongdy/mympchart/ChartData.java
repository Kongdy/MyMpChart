package com.project.kongdy.mympchart;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;

/**
 * @author kongdy
 *         on 2016/7/27
 * 单组chart数据
 */
public class ChartData implements ValueAnimator.AnimatorUpdateListener {

    public CharSequence name;

    public SparseArray<Float> values;

    public SparseArray<Point> points;

    private Path foldPath; // 折线
    private Paint foldPaint; // 折线画笔
    private Paint entityPaint; // 实体画笔
    private TextPaint markTextPaint; // 图中数字标注画笔


    private float[] mLineBuffer = new float[4]; //

    private int foldColor = Color.GREEN;
    private int circleRadius = -1;
    private int foldWidth = 5;
    private float markTextSize = -1;

    private final long DEFAULT_ANIMAL_TIME = 2000;

    private boolean isShowMark = true; // 是否显示标注
    public boolean openUpAnimal = true; // 开启上升动画
    public boolean openfoldAnimal = true; // 开启展开动画

    private PathMeasure animalPathMeasure;
    private float[] animalPos = new float[2];
    private Path animalPath;
    private float pathAllLength;
    private float currentAnimalPathLength;

    private Matrix mMatrixValueToPx;
    private Matrix mMatrixOffset;

    private MyChartView.MyMpChartDataProperty myMpChartDataProperty;
    private MyChartView chartView;

    // from parent
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
        animalPath = new Path();

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
        chartView.setLayerType(View.LAYER_TYPE_SOFTWARE,entityPaint);

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

      //  openHalo();
        foldPaint.setColor(foldColor);
        foldPaint.setStyle(Paint.Style.STROKE);
        foldPaint.setStrokeWidth(foldWidth);

        markTextPaint.setTextSize(markTextSize);

        if(circleRadius == -1)
            circleRadius = (int) chartView.getRawSize(TypedValue.COMPLEX_UNIT_DIP,4);
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

        // calculate path
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
                    if(i == 0)
                        foldPath.moveTo(point.x,point.y);
                    else
                        foldPath.quadTo(point.x,point.y,point.x,point.y);
                    break;
                case COLUMNAR:
                    break;
            }
          //  canvas.drawPath(foldPath,transparentPaint);
            ++i;
        }
        mMatrixValueToPx = new Matrix();
        mMatrixOffset = new Matrix();
        foldPath.transform(mMatrixValueToPx);
        foldPath.transform(mMatrixOffset);


//        animalPathMeasure = new PathMeasure();
//        animalPathMeasure.setPath(foldPath,false);
//        pathAllLength = animalPathMeasure.getLength();
//        animalPath.moveTo(points.get(0).x,points.get(0).y);
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
        // draw fold
//        foldPath.reset();
        int i = 0;
        while(null != points.get(i+1)) {
            Point point = points.get(i);
//            switch (dataStyle) {
//                case FOLD_LINE_BEVEL:
//                    if(i == 0)
//                        foldPath.moveTo(point.x,point.y);
//                    else
//                        foldPath.lineTo(point.x,point.y);
//                    break;
//                case FOLD_LINE_ROUND:
//                    if(i == 0)
//                        foldPath.moveTo(point.x,point.y);
//                    else
//                        foldPath.quadTo(point.x,point.y,point.x,point.y);
//                    break;
//                case COLUMNAR:
//                    break;
//            }
            //canvas.drawPath(foldPath,foldPaint);
            mLineBuffer = new float[2*4];
            mLineBuffer[0] = point.x;
            mLineBuffer[1] = point.y*currentAnimalPathLength;
            point = points.get(i+1);
            mLineBuffer[2] = point.x;
            mLineBuffer[3] = point.y*currentAnimalPathLength;
            mMatrixValueToPx.mapPoints(mLineBuffer);
            mMatrixOffset.mapPoints(mLineBuffer);
            canvas.drawLines(mLineBuffer, 0, 2 * 2, foldPaint);
            ++i;
        }
        // draw animal path
//        animalPathMeasure.getPosTan(currentAnimalPathLength,animalPos,null);
//        animalPath.lineTo(animalPos[0],animalPos[1]);
//        canvas.translate(currentAnimalPathLength,0);
//        canvas.drawPath(foldPath,foldPaint);


        // draw dot
        i = 0;
        while(null != points.get(i)) {
            Point point = points.get(i);
            switch (pointStyle) {
                case FILL_DOT:
                    initEntityColor(i,entityPaint);
                    canvas.drawCircle(point.x, point.y,circleRadius, entityPaint);
                    break;
                case HOLLOW_IN_DOT:
                    entityPaint.setColor(Color.WHITE);
                    canvas.drawCircle(point.x, point.y, circleRadius, entityPaint);
                    initEntityColor(i,entityPaint);
                    canvas.drawCircle(point.x, point.y,circleRadius*3/5, entityPaint);
                    break;
                case HOLLOW_OUT_DOT:
                    initEntityColor(i,entityPaint);
                    canvas.drawCircle(point.x, point.y, circleRadius, entityPaint);
                    entityPaint.setColor(Color.WHITE);
                    canvas.drawCircle(point.x, point.y,circleRadius*3/5, entityPaint);
                    break;
                case SQUARE_DOT:
                    initEntityColor(i,entityPaint);
                    canvas.drawCircle(point.x, point.y,circleRadius, entityPaint);
                    canvas.drawRect(point.x-circleRadius,point.y-circleRadius,point.x+circleRadius,
                            point.y+circleRadius,entityPaint);
                    break;
                case NONE_DOT:
                    break;
            }
            ++i;
        }

        // draw mark
        i = 0;
        while(null != points.get(i) && isShowMark) {
            Point point = points.get(i);
            canvas.drawText(safeIntText(values.valueAt(i) + ""), point.x + 20, point.y + 20, markTextPaint);
            ++i;
        }
    }

    private String safeIntText(String text) {
        if (!TextUtils.isEmpty(text) && !"null".equals(text)) {
           return text;
        } else {
            return "0";
        }
    }

    public void initEntityColor(int i,Paint paint) {
        if (myMpChartDataProperty != null) {
            paint.setColor(myMpChartDataProperty.getColorFilter(i, values.keyAt(i), values.valueAt(i)));
        } else {
            paint.setColor(Color.BLACK);
        }
    }

    public int initEntityColor(int i) {
        if (myMpChartDataProperty != null) {
            return myMpChartDataProperty.getColorFilter(i, values.keyAt(i), values.valueAt(i));
        } else {
            return Color.BLACK;
        }
    }

    public void startOpenFoldAnimal() {
        if(openUpAnimal) {
            ObjectAnimator valueAnimator = ObjectAnimator.ofFloat(chartView,"a",0f,1f);
            valueAnimator.addUpdateListener(this);
            valueAnimator.setDuration(DEFAULT_ANIMAL_TIME);
            valueAnimator.setStartDelay(100);
            valueAnimator.start();
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

    public MyChartView.MyMpChartDataProperty getMyMpChartDataProperty() {
        return myMpChartDataProperty;
    }

    public void setMarkColor(int color) {
        markTextPaint.setColor(color);
    }

    public void setOpenUpAnimal(boolean openUpAnimal) {
        this.openUpAnimal = openUpAnimal;
    }

    public void setOpenfoldAnimal(boolean openfoldAnimal) {
        this.openfoldAnimal = openfoldAnimal;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        currentAnimalPathLength = (float) valueAnimator.getAnimatedValue();
        chartView.reDraw();
    }
}