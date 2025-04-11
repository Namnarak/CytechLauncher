package com.movtery.zalithlauncher.utils.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.BaseInterpolator;

public class JellyBounceInterpolator extends BaseInterpolator {

    public JellyBounceInterpolator() {
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public JellyBounceInterpolator(Context context, AttributeSet attrs) {
    }

    public float getInterpolation(float t) {
        final float PHASE1_END = 0.3f;
        final float PHASE2_END = 0.5f;
        final float BOUNCE_HEIGHT = 1.1f;
        final float SETTLE_HEIGHT = 0.9f;

        if (t < PHASE1_END) {
            float progress = t / PHASE1_END;
            return (float) Math.pow(progress, 2) * BOUNCE_HEIGHT;
        } else if (t < PHASE2_END) {
            float progress = (t - PHASE1_END) / (PHASE2_END - PHASE1_END);
            return BOUNCE_HEIGHT - (float) Math.pow(progress, 2) * (BOUNCE_HEIGHT - SETTLE_HEIGHT);
        } else {
            float progress = (t - PHASE2_END) / (1 - PHASE2_END);
            return SETTLE_HEIGHT + progress * (1 - SETTLE_HEIGHT);
        }
    }
}