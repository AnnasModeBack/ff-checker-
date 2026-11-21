package com.annas.aifloating;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class Animation3DHelper {

    public static void apply3DTiltEntrance(View view, long delayMs) {
        if (view == null) return;
        float density = view.getResources().getDisplayMetrics().density;
        view.setAlpha(0f);
        view.setScaleX(0.68f);
        view.setScaleY(0.68f);
        view.setRotationX(24f);
        view.setRotationY(-20f);
        view.setTranslationY(24f * density);
        view.setCameraDistance(18000f * density);

        view.animate()
                .setStartDelay(delayMs)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .rotationX(0f)
                .rotationY(0f)
                .translationY(0f)
                .setDuration(520L)
                .setInterpolator(new OvershootInterpolator(1.35f))
                .start();
    }

    public static void start3DFloatingIconLoop(View view) {
        if (view == null) return;
        float density = view.getResources().getDisplayMetrics().density;
        view.setCameraDistance(15000f * density);

        PropertyValuesHolder rotY = PropertyValuesHolder.ofFloat(View.ROTATION_Y, -24f, 24f);
        PropertyValuesHolder rotX = PropertyValuesHolder.ofFloat(View.ROTATION_X, -8f, 8f);
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.93f, 1.07f);
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.93f, 1.07f);

        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, rotY, rotX, sx, sy);
        anim.setDuration(1550L);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }

    public static void pop3DClick(final View view) {
        if (view == null) return;
        try {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } catch (Exception ignored) {
        }
        view.animate()
                .scaleX(0.86f)
                .scaleY(0.86f)
                .rotationY(18f)
                .setDuration(85L)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .rotationY(0f)
                                .setDuration(160L)
                                .setInterpolator(new OvershootInterpolator(1.25f))
                                .start();
                    }
                })
                .start();
    }
}
