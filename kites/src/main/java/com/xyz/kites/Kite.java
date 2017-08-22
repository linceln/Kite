package com.xyz.kites;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 2017/8/8.
 */
public class Kite {

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private View view;
    private Timer timer;
    private long duration;
    private boolean isShowing;
    private boolean isTouching = false;

    private Kite(View view, WindowManager windowManager, WindowManager.LayoutParams params,
                 Timer timer, long duration, boolean isShowing, boolean isTouching) {
        this.windowManager = windowManager;
        this.view = view;
        this.params = params;
        this.duration = duration;
        this.timer = timer;
        this.isTouching = isTouching;
        this.isShowing = isShowing;
    }

    private void setShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    private void setTouching(boolean isTouching) {
        this.isTouching = isTouching;
    }


    public void show() {
        if (!isShowing) {
            isShowing = true;
            windowManager.addView(view, params);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isShowing && !isTouching) {
                        windowManager.removeView(view);
                        isShowing = false;
                    }
                }
            }, duration);
        }
    }

    public static class Builder {

        private Kite kite;
        private Context context;
        private View view;
        private WindowManager windowManager;
        private WindowManager.LayoutParams params;
        private Timer timer;
        private Callback callback;
        private long duration = 2000;
        private boolean isShowing = false;

        // 手势相关
        private boolean isTouching = false;
        private long touchDownTime;
        private long touchUpTime;
        private float touchDownY;
        private float currentY;

        public Builder(Context context) {
            this.context = context;
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            timer = new Timer();
            params = new WindowManager.LayoutParams();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.gravity = Gravity.TOP;
        }

        /**
         * 设置进入退出动画效果
         */
        public Builder setWindowAnimations(@StyleRes int styleRes) {
            params.windowAnimations = styleRes;
            return this;
        }

        public Builder setGravity(int gravity) {
            params.gravity = gravity;
            return this;
        }

        public Builder setParams(WindowManager.LayoutParams params) {
            this.params = params;
            return this;
        }

        public Builder setLayoutRes(@LayoutRes int layoutRes) {
            view = LayoutInflater.from(context).inflate(layoutRes, null);
            setOnTouchListener();
            return this;
        }

        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            if (this.callback != null) {
                this.callback.onCreateView(view);
            }
            return this;
        }

        private void setOnTouchListener() {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            isTouching = true;
                            if (kite != null) {
                                kite.setTouching(isTouching);
                            }
                            touchDownTime = System.currentTimeMillis();
                            touchDownY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            currentY = event.getRawY();

                            if (Math.abs(currentY - touchDownY) > 50) {

                                if (currentY - touchDownY < 0) {
                                    // 向上滑动 消除
                                } else {
                                    // 向下滑动 打开聊天页面
                                    if (callback != null) {
                                        callback.onDragDown(view);
                                    }
                                }

                                windowManager.removeView(view);
                                isShowing = false;
                                if (kite != null) {
                                    kite.setTouching(isShowing);
                                }
                                timer.cancel();
                            }

                            break;
                        case MotionEvent.ACTION_UP:
                            isTouching = false;
                            if (kite != null) {
                                kite.setTouching(isTouching);
                            }
                            touchUpTime = System.currentTimeMillis();
                            if (touchUpTime - touchDownTime > 100) {// 停留时间超过 100 ms，则表示不是点击事件
                                if (touchUpTime - touchDownTime > duration) {// 停留时间超过 duration，需要重新启动延时
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (isShowing && !isTouching) {
                                                windowManager.removeView(view);
                                                isShowing = false;
                                                if (kite != null) {
                                                    kite.setTouching(isShowing);
                                                }
                                            }
                                        }
                                    }, duration);
                                }
                                return true;
                            }
                            break;
                    }
                    return false;
                }
            });
        }

        public Kite build() {
            kite = new Kite(view, windowManager, params, timer, duration, isShowing, isTouching);
            return kite;
        }

        public interface Callback {
            void onCreateView(View view);

            void onDragDown(View view);
        }
    }
}