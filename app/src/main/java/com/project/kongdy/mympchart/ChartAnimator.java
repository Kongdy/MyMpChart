package com.project.kongdy.mympchart;

import android.animation.ObjectAnimator;

/**
 * @author kongdy
 * @date 2016-07-28 23:16
 * @TIME 23:16
 **/

public class ChartAnimator {
    private ObjectAnimator.AnimatorUpdateListener mListener;

    protected float mPhaseX = 1f;
    protected float mPhaseY = 1f;


    public ChartAnimator() {
    }

    public ChartAnimator(ObjectAnimator.AnimatorUpdateListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 设置X轴动画
     * @param duration
     */
    public void animalX(long duration) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this,"phaseX",0f,1f);
        objectAnimator1.setDuration(duration);
        objectAnimator1.addUpdateListener(mListener);
        objectAnimator1.start();
    }

    /**
     * 设置Y轴动画
     * @param duration
     */
    public void animalY(long duration) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this,"phaseY",0f,1f);
        objectAnimator1.setDuration(duration);
        objectAnimator1.addUpdateListener(mListener);
        objectAnimator1.start();
    }

    /**
     * 设置X,Y轴动画
     * @param duration
     */
    public void animalXY(long duration) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this,"phaseX",0f,1f);
        objectAnimator1.setDuration(duration);
        objectAnimator1.addUpdateListener(mListener);

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this,"phaseY",0f,1f);
        objectAnimator2.setDuration(duration);
        objectAnimator2.addUpdateListener(mListener);

        objectAnimator1.start();
        objectAnimator2.start();
    }

    public float getPhaseX() {
        return mPhaseX;
    }

    public void setPhaseX(float phaseX) {
        this.mPhaseX = phaseX;
    }

    public float getPhaseY() {
        return mPhaseY;
    }

    public void setPhaseY(float phaseY) {
        this.mPhaseY = phaseY;
    }
}