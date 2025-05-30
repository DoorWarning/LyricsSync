package com.example.catch_pixel_ai;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

public class Animation {

    public static void xAnimation(View view){
        ValueAnimator animatorX = ValueAnimator.ofFloat(1.2f, 1.0f);
        animatorX.setDuration(1000);
        animatorX.setInterpolator(new AnticipateOvershootInterpolator());
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
            }
        });
        animatorX.start();
    }

    public static void timerAnimation(View view){
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0.8f, 1.0f);
        alphaAnimator.setDuration(500);
        alphaAnimator.setInterpolator(new AnticipateOvershootInterpolator());
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                view.setAlpha((float) animation.getAnimatedValue());
            }
        });
        alphaAnimator.start();
    }

    public static void chattingAnimation(View view){
        ValueAnimator animatorX = ValueAnimator.ofFloat(1.0f, 0.9f);
        ValueAnimator animatorX1 = ValueAnimator.ofFloat(0.9f, 1.0f);
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animatorX.setDuration(250);
        animatorX1.setDuration(250);
        alphaAnimator.setDuration(500);
        animatorX1.setInterpolator(new AnticipateInterpolator());
        animatorX.setInterpolator(new AnticipateInterpolator());
        alphaAnimator.setInterpolator(new LinearInterpolator());
        animatorX1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
            }
        });
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
            }
        });
        animatorX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                animatorX1.start();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                view.setAlpha((float) animation.getAnimatedValue());
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, animatorX);
        animatorSet.start();
    }
    public static void btnAnimation(View view){
        ValueAnimator animatorX = ValueAnimator.ofFloat(1.2f, 1.0f);
        ValueAnimator animatorY = ValueAnimator.ofFloat(1.1f, 1.0f);
        animatorX.setDuration(1000);
        animatorY.setDuration(1000);
        animatorY.setInterpolator(new AnticipateOvershootInterpolator());
        animatorX.setInterpolator(new AnticipateOvershootInterpolator());
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
            }
        });
        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setScaleY(value);
            }
        });
        animatorX.start();
        animatorY.start();
    }
}
