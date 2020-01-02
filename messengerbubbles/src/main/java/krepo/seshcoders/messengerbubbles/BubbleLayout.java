/*
 * Copyright Txus Ballesteros 2015 (@txusballesteros)
 *
 * This file is part of some open source application.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contact: Txus Ballesteros <txus.ballesteros@gmail.com>
 */
package krepo.seshcoders.messengerbubbles;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

public class BubbleLayout extends BubbleBaseLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private float initialTouchX;
    private float initialTouchY;
    private int initialX;
    private int initialY;
    private OnBubbleRemoveListener onBubbleRemoveListener;
    private OnBubbleClickListener onBubbleClickListener;
    private OnBubbleStickToWallListener onBubbleStickToWallListener;
    private static final int TOUCH_TIME_THRESHOLD = 150;
    private long lastTouchDown;
    private MoveAnimator animator;
    private int width;
    private WindowManager windowManager;
    private boolean shouldStickToWall = true;
    private MessCloudView.BubbleCurrentWall currentWall;

    private MessCloudView cloudView;

    private static final String TAG = "BubbleLayout";

    public void setOnBubbleRemoveListener(OnBubbleRemoveListener listener) {
        onBubbleRemoveListener = listener;
    }

    public void setOnBubbleClickListener(OnBubbleClickListener listener) {
        onBubbleClickListener = listener;
    }

    public void setOnBubbleStickToWallListener(OnBubbleStickToWallListener listener) {
        onBubbleStickToWallListener = listener;
    }

    public BubbleLayout(Context context) {
        super(context);
        animator = new MoveAnimator();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initializeView();
    }

    public BubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        animator = new MoveAnimator();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initializeView();
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        animator = new MoveAnimator();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initializeView();
    }

    public void setShouldStickToWall(boolean shouldStick) {
        this.shouldStickToWall = shouldStick;
    }

    void notifyBubbleRemoved() {
        if (onBubbleRemoveListener != null) {
            onBubbleRemoveListener.onBubbleRemoved(this);
        }
    }

    private void initializeView() {
        this.getViewTreeObserver().addOnGlobalLayoutListener(this);
        initializeCloudView();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {
            if (cloudView.getVisibility() == VISIBLE)
                cloudView.setVisibility(GONE);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = getViewParams().x;
                    initialY = getViewParams().y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    playAnimationClickDown();
                    lastTouchDown = System.currentTimeMillis();
                    updateSize();
                    animator.stop();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = initialX + (int) (event.getRawX() - initialTouchX);
                    int y = initialY + (int) (event.getRawY() - initialTouchY);
                    getViewParams().x = x;
                    getViewParams().y = y;
                    getWindowManager().updateViewLayout(this, getViewParams());
                    if (getLayoutCoordinator() != null) {
                        getLayoutCoordinator().notifyBubblePositionChanged(this, x, y);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    goToWall();
                    if (getLayoutCoordinator() != null) {
                        getLayoutCoordinator().notifyBubbleRelease(this);
                        playAnimationClickUp();
                    }
                    if (System.currentTimeMillis() - lastTouchDown < TOUCH_TIME_THRESHOLD) {
                        if (onBubbleClickListener != null) {
                            onBubbleClickListener.onBubbleClick(this);
                        }
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void initializeCloudView() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_mess_cloud, null, false);
        cloudView = rootView.findViewById(R.id.cloudView);
        if (cloudView.getParent() != null) {
            ((ViewGroup) cloudView.getParent()).removeView(cloudView);
        }
    }


    private void addMessageCloud() {
        LayoutParams p = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL);
        p.setMargins(this.getWidth() + (int) dpToPx(2, getContext()), 0, 0, 0);
        cloudView.setLayoutParams(p);
        this.addView(cloudView);
        cloudView.setVisibility(VISIBLE);
    }

    private void playAnimation() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.bubble_shown_animator);
            animator.setTarget(this);
            animator.start();
        }
    }


    private void playAnimationClickDown() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.bubble_click_down_animator);
            animator.setTarget(this);
            animator.start();
        }
    }

    private void playAnimationClickUp() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.bubble_up_click_animator);
            animator.setTarget(this);
            animator.start();
        }
    }

    private void updateSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = (size.x - this.getWidth());

    }

    @Override
    public void onGlobalLayout() {
        //after drawing of users layout and getting its measurements
        this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        addMessageCloud();
    }

    public interface OnBubbleRemoveListener {
        void onBubbleRemoved(BubbleLayout bubble);
    }

    public interface OnBubbleClickListener {
        void onBubbleClick(BubbleLayout bubble);
    }

    public interface OnBubbleStickToWallListener {
        void onBubbleStickToWall(MessCloudView.BubbleCurrentWall wall);
    }

    public void goToWall() {
        if (shouldStickToWall) {
            int middle = width / 2;
            if (getViewParams().x >= middle) {
                //right wall
                currentWall = MessCloudView.BubbleCurrentWall.RIGHT;
                animator.start(width + getViewParams().width, getViewParams().y);
            } else {
                //left wall
                currentWall = MessCloudView.BubbleCurrentWall.LEFT;
                animator.start(0 - getViewParams().width, getViewParams().y);
            }
            if (onBubbleStickToWallListener != null) {
                onBubbleStickToWallListener.onBubbleStickToWall(currentWall);
            }
        }
    }

    public void displayMessage(String cloudMessage) {
        if (cloudView != null) {
            cloudView.setCloudMessage(cloudMessage);
            if (cloudView.getVisibility() != VISIBLE)
                cloudView.setVisibility(VISIBLE);
        }
    }

    private void move(float deltaX, float deltaY) {
        getViewParams().x += deltaX;
        getViewParams().y += deltaY;
        windowManager.updateViewLayout(this, getViewParams());
    }

    private static float dpToPx(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private class MoveAnimator implements Runnable {
        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        private void start(float x, float y) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (getRootView() != null && getRootView().getParent() != null) {
                float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
                float deltaX = (destinationX - getViewParams().x) * progress;
                float deltaY = (destinationY - getViewParams().y) * progress;
                move(deltaX, deltaY);
                if (progress < 1) {
                    handler.post(this);
                }
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }
    }

    static class BubblePojo {
        int xPos;
        int yPos;
        BubbleLayout bubbleLayout;

        BubblePojo(int xPos, int yPos, BubbleLayout bubbleLayout) {
            this.bubbleLayout = bubbleLayout;
            this.xPos = xPos;
            this.yPos = yPos;
        }
    }
}
