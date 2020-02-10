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
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import static krepo.seshcoders.messengerbubbles.MessCloudView.BubbleCurrentWall.LEFT;
import static krepo.seshcoders.messengerbubbles.MessCloudView.BubbleCurrentWall.RIGHT;

public class BubbleLayout extends BubbleBaseLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private float initialTouchX;
    private float initialTouchY;
    private int initialX;
    private int initialY;
    private OnBubbleRemoveListener onBubbleRemoveListener;
    private OnBubbleClickListener onBubbleClickListener;
    private OnBubbleStickToWallListener onBubbleStickToWallListener;
    private OnMainBubbleActionListener onMainBubbleActionListener;
    private static final int TOUCH_TIME_THRESHOLD = 150;
    private long lastTouchDown;
    private MoveAnimator animator;
    private int width;
    private WindowManager windowManager;
    private boolean shouldStickToWall = true;
    private MessCloudView.BubbleCurrentWall currentWall;
    private boolean isInMotion = false;
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

    void setOnMainBubbleActionListener(OnMainBubbleActionListener listener) {
        onMainBubbleActionListener = listener;
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

    void notifyBubbleRemoved() {
        if (onBubbleRemoveListener != null) {
            onBubbleRemoveListener.onBubbleRemoved(this);
        }
    }

    void notifyStickToWall(MessCloudView.BubbleCurrentWall currentWall) {
        if (onBubbleStickToWallListener != null) {
            onBubbleStickToWallListener.onBubbleStickToWall(currentWall, this);
        }
    }

    private void initializeView() {
        this.getViewTreeObserver().addOnGlobalLayoutListener(this);
//        initializeCloudView();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        getWindowManager().removeView(this);
//        getWindowManager().addView(this, getLayoutParams());
        if (event != null && getStackPosition() == 0) {
            if (cloudView != null && cloudView.getVisibility() == VISIBLE)
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
                    isInMotion = true;
                    if (onMainBubbleActionListener != null) {
                        onMainBubbleActionListener.onMainBubbleMove(x, y, 0);
                    }
                    if (getLayoutCoordinator() != null) {
                        getLayoutCoordinator().notifyBubblePositionChanged(this, x, y);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    goToWall(null, 400f);
                    if (onMainBubbleActionListener != null) {
                    }
                    isInMotion = false;
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


//    private void initializeCloudView() {
//        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_mess_cloud, null, false);
//        cloudView = rootView.findViewById(R.id.cloudView);
//        if (cloudView.getParent() != null) {
//            ((ViewGroup) cloudView.getParent()).removeView(cloudView);
//        }
//    }


//    private void addMessageCloud() {
//        LayoutParams p = new LayoutParams(
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT,
//                Gravity.CENTER_VERTICAL);
//        p.setMargins(this.getWidth() + (int) dpToPx(2, getContext()), 0, 0, 0);
//        cloudView.setLayoutParams(p);
//        this.addView(cloudView);
//    }

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
        updateSize();
        goToWall(null, 100f);
//        addMessageCloud();
    }

    public interface OnBubbleRemoveListener {
        void onBubbleRemoved(BubbleLayout bubble);
    }

    public interface OnBubbleClickListener {
        void onBubbleClick(BubbleLayout bubble);
    }

    public interface OnBubbleStickToWallListener {
        void onBubbleStickToWall(MessCloudView.BubbleCurrentWall wall, BubbleLayout bubble);
    }

    public void goToWall(@Nullable MessCloudView.BubbleCurrentWall wall, float animationTime) {
        if (shouldStickToWall) {
            int middle = width / 2;
            int x, y;
            if (wall == RIGHT || getViewParams().x >= middle) {
                //right wall
                currentWall = RIGHT;
//                x = width + getViewParams().width * 2;
//                x = width + this.getWidth();
                x = width;
                y = getViewParams().y;
            } else {
                //left wall
                currentWall = LEFT;
                x = 0 - getViewParams().width;
                y = getViewParams().y;
            }
            animator.start(x, y, animationTime);
            notifyStickToWall(currentWall);
            if (onMainBubbleActionListener != null && getStackPosition() == 0) {
                onMainBubbleActionListener.onMainBubbleStickToWall(x, y, currentWall);
            }
            isInMotion = false;
        }
    }

    public void displayMessage(String cloudMessage) {
        if (cloudView != null && !isInMotion && currentWall != null) {
            cloudView.setCurrentWall(currentWall);
            int x;
            if (currentWall == LEFT) {
                x = this.getViewParams().x + this.getWidth() + 15;
            } else {
                x = this.getViewParams().x
                        - cloudView.getFullWidth() - 15 - this.getWidth()/2;
            }

//            int x = width - (this.getWidth() + cloudView.getWidth());
            cloudView.setCloudMessage(cloudMessage);
//            int y = getViewParams().y;
            int y = (getViewParams().y + this.getHeight() / 2) - cloudView.getFullHeight() / 2;
//            Log.d(TAG, "displayMessage: height" + cloudView.getFullHeight() + " width " + cloudView.getFullWidth());
            cloudView.move(x, y);
            if (cloudView.getVisibility() != VISIBLE)
                cloudView.setVisibility(VISIBLE);
//            Toast.makeText(getContext(), "shown", Toast.LENGTH_SHORT).show();
        }
    }


    public void setShouldStickToWall(boolean shouldStick) {
        this.shouldStickToWall = shouldStick;
    }

    public void setCloudView(MessCloudView cloudView) {
        this.cloudView = cloudView;
    }

    public MessCloudView.BubbleCurrentWall getCurrentWall() {
        return currentWall;
    }

    MoveAnimator getAnimator() {
        return animator;
    }

    synchronized void move(float deltaX, float deltaY) {
        getViewParams().x += deltaX;
        getViewParams().y += deltaY;
        windowManager.updateViewLayout(this, getViewParams());
        isInMotion = true;

    }

    class MoveAnimator implements Runnable {
        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;
        private float animationTime;

        void start(float x, float y, float animationTime) {
            this.destinationX = x;
            this.destinationY = y;
            this.animationTime = animationTime;
            startingTime = System.currentTimeMillis();
            handler.post(this);
            if (onMainBubbleActionListener != null && getStackPosition() == 0) {
                onMainBubbleActionListener.onMainBubbleMove((int) x, (int) y, animationTime);
            }
        }

        @Override
        public void run() {
            if (getRootView() != null && getRootView().getParent() != null) {
                float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / animationTime);
                float deltaX = (destinationX - getViewParams().x) * progress;
                float deltaY = (destinationY - getViewParams().y) * progress;
                move(deltaX, deltaY);
                if (progress < 1) {
                    handler.post(this);
                } else {
                    isInMotion = false;
                    if (onMainBubbleActionListener != null && getStackPosition() == 0) {
                        onMainBubbleActionListener.onMainBubbleAnimationFinish();
                    }
                }
            }
        }

        void stop() {
            handler.removeCallbacks(this);
        }
    }

    static class BubblePojo {
        int xPos;
        int yPos;
        Object bubbleObject;

        BubblePojo(int xPos, int yPos, Object bubbleObject) {
            this.bubbleObject = bubbleObject;
            this.xPos = xPos;
            this.yPos = yPos;
        }
    }
}
