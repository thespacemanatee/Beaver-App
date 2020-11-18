package com.example.beever.feature;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.example.beever.R;

public class Utils {

    Context context;

    public Utils(Context context) {
        this.context = context;
    }

    public void fadeIn() {

        View view = ((Activity)context).findViewById(R.id.bottom_menu);

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(1f)
                .setDuration(((Activity)context).getResources().getInteger(
                        android.R.integer.config_shortAnimTime))
                .setListener(null);
    }

    public void fadeOut() {

        View view = ((Activity)context).findViewById(R.id.bottom_menu);

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(0f)
                .setDuration(((Activity)context).getResources().getInteger(
                        android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }
}
