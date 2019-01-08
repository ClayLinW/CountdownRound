package com.beauty.circleprogressbarproject;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Android自定义view实现圆环倒计时
 */
public class CircleProgressbar extends View {

    /*绘制内部圆*/
    private Paint innerCircle = new Paint();
    /*绘制外部圆*/
    private Paint outerCircle = new Paint();
    private RectF rectF = new RectF();
    /*view的大小*/
    private int width, height;
    /*转圈背景和前景宽度*/
    private float backgroundProgressWidth;
    private float foregroundProgressWidth;
    /*转圈背景和前景默认宽度*/
    private final int DEFAULT_FOREGROUND_PROGRESS_WIDTH = 10;
    private final int DEFAULT_BACKGROUND_CIRCLE_WIDTH = 10;
    /*转圈背景和前景默认颜色值*/
    private int DEFAULT_BACKGROUND_PROGRESS_COLOR = Color.GRAY;
    private int DEFAULT_FOREGROUND_PROGRESS_COLOR = Color.RED;
    /*转圈背景和前景颜色值*/
    private int backgroundProgressColor;
    private int foregroundProgressColor;
    /*触摸时是否可移动*/
    private boolean moveCorrect;
    /*顺时针方向*/
    private boolean clockWise;
    /*进度*/
    private float progress = 0;
    private float maxProgress = 100;
    /*起始角度位置*/
    private int startAngle = 0;
    /*旋转角度位置*/
    private float sweepAngle = 0;
    /*绘制圆的中心点*/
    private int centerPoint;
    /*获取转圈背景和前景默认宽度的最大值，为了计算绘制圆的位置*/
    private float subtractingValue;
    /*计算绘制外部圆的半径*/
    private float drawRadius;
    /*计算内部圆的半径*/
    private float drawOuterRadius;

    /*是否可触摸改变进度*/
    private boolean isTouchEnabled = false;
    private boolean roundedCorner;

    /*圆环进度颜色，可设置渐变*/
    private int[] doughnutColors;
    /*圆环进度监听*/
    private OnProgressbarChangeListener onProgressbarChangeListener;

    public CircleProgressbar(Context context) {
        super(context);
        init();
    }

    public CircleProgressbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressbar(Context context, AttributeSet attrs, int i) {
        super(context, attrs, i);
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressbar, 0, 0);
        backgroundProgressWidth = typeArray.getDimension(R.styleable.CircleProgressbar_cpb_backgroundProgressWidth, DEFAULT_BACKGROUND_CIRCLE_WIDTH);
        foregroundProgressWidth = typeArray.getDimension(R.styleable.CircleProgressbar_cpb_foregroundProgressWidth, DEFAULT_FOREGROUND_PROGRESS_WIDTH);
        backgroundProgressColor = typeArray.getColor(R.styleable.CircleProgressbar_cpb_backgroundProgressColor, DEFAULT_BACKGROUND_PROGRESS_COLOR);
        foregroundProgressColor = typeArray.getColor(R.styleable.CircleProgressbar_cpb_foregroundProgressColor, DEFAULT_FOREGROUND_PROGRESS_COLOR);
        this.progress = typeArray.getFloat(R.styleable.CircleProgressbar_cpb_progress, progress);
        this.maxProgress = typeArray.getFloat(R.styleable.CircleProgressbar_cpb_maxProgress, maxProgress);
        this.roundedCorner = typeArray.getBoolean(R.styleable.CircleProgressbar_cpb_roundedCorner, false);
        this.clockWise = typeArray.getBoolean(R.styleable.CircleProgressbar_cpb_clockwise, false);
        this.isTouchEnabled = typeArray.getBoolean(R.styleable.CircleProgressbar_cpb_touchEnabled, false);
        typeArray.recycle();
        // 圆环默认颜色为主题色,可设置为颜色渐变
        doughnutColors = new int[]{foregroundProgressColor, foregroundProgressColor};
        init();
        if (roundedCorner) {
            setRoundedCorner(roundedCorner);
        }
        if (this.progress > 0) {
            setProgress(this.progress);
        }

        if (clockWise) {
            setClockwise(clockWise);
        }

        if (isTouchEnabled) {
            enabledTouch(isTouchEnabled);
        }
    }

    private void init() {
        innerCircle.setStrokeWidth(foregroundProgressWidth); //圆环宽度
        innerCircle.setAntiAlias(true);                //抗锯齿
        innerCircle.setStyle(Paint.Style.STROKE);   //设置图形为空心
        innerCircle.setColor(foregroundProgressColor); //设置颜色

        outerCircle.setStrokeWidth(backgroundProgressWidth);
        outerCircle.setAntiAlias(true);
        outerCircle.setColor(backgroundProgressColor);
        outerCircle.setStyle(Paint.Style.STROKE);
    }

    /**
     * 倒计时转圈颜色类型
     *
     * @param selectColor 根据各种条件，可变换倒计时圆圈的渐变颜色
     */
    public void setDoughnutColors(int selectColor) {
        if (selectColor == 1) {
            doughnutColors = new int[]{Color.parseColor("#60B4FD"), Color.parseColor("#5C72F2")};
        } else if (selectColor == 2) {
            doughnutColors = new int[]{Color.parseColor("#FCC95C"), Color.parseColor("#FD80A8")};
        } else {
            doughnutColors = new int[]{Color.parseColor("#5DC072"), Color.parseColor("#5DC072")};
        }
        invalidate();
    }

    //    @Override
    //    protected void onDraw(Canvas canvas) {
    //        canvas.drawCircle(centerPoint, centerPoint, drawRadius, outerCircle);
    //        canvas.drawArc(rectF, startAngle, sweepAngle, false, innerCircle);
    //        super.onDraw(canvas);
    //    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画布旋转90度，根据view的中心点进行旋转，使倒计时的开始位置位于view的顶部
        canvas.rotate(-90, width / 2, height / 2);
        canvas.drawCircle(centerPoint, centerPoint, drawRadius, outerCircle);
        //设置颜色渐变
        SweepGradient sweepGradient = new SweepGradient(centerPoint, centerPoint, doughnutColors, null);
        innerCircle.setShader(sweepGradient);
        //指定矩阵旋转，处理设置两端为圆角时分开后的颜色问题
        Matrix matrix = new Matrix();
        matrix.postRotate(2);
        sweepGradient.setLocalMatrix(matrix);
        canvas.drawArc(rectF, startAngle, sweepAngle, false, innerCircle);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        centerPoint = Math.min(width, height);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        setRadiusRect();
    }

    /**
     * 测量，主要是计算获得内外转圈的半径，中心点，位置
     */
    private void setRadiusRect() {
        centerPoint = Math.min(width, height) / 2;
        subtractingValue = (backgroundProgressWidth > foregroundProgressWidth) ? backgroundProgressWidth : foregroundProgressWidth;
        float newSeekWidth = subtractingValue / 2;
        drawRadius = Math.min((width - subtractingValue) / 2, (height - subtractingValue) / 2);
        drawOuterRadius = Math.min((width - newSeekWidth), (height - newSeekWidth));
        rectF.set(subtractingValue / 2, subtractingValue / 2, drawOuterRadius, drawOuterRadius);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isTouchEnabled) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (onProgressbarChangeListener != null) {
                        onProgressbarChangeListener.onStartTracking(this);
                    }
                    checkForCorrect(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (moveCorrect) {
                        justMove(event.getX(), event.getY());
                    }
                    upgradeProgress(this.progress, true);

                    break;
                case MotionEvent.ACTION_UP:
                    if (onProgressbarChangeListener != null) {
                        onProgressbarChangeListener.onStopTracking(this);
                    }
                    moveCorrect = false;
                    break;
            }
            return true;
        }
        return false;
    }

    /**
     * 更新进度
     *
     * @param progress
     * @param b
     */
    private void upgradeProgress(float progress, boolean b) {
        if (progress < 0) {
            if (b) {
                progress = -progress;
            } else {
                progress = 0;
            }
        }
        this.progress = (progress <= maxProgress) ? progress : maxProgress;
        sweepAngle = (360 * progress / maxProgress);

        if (this.clockWise) {
            if (sweepAngle > 0) {
                sweepAngle = -sweepAngle;
            }
        }
        if (onProgressbarChangeListener != null) {
            onProgressbarChangeListener.onProgressChanged(this, progress, b);
        }
        invalidate();
    }

    /**
     * 触摸时根据坐标获取进度
     *
     * @param x
     * @param y
     */
    private void justMove(float x, float y) {
        if (clockWise) {
            float degree = (float) Math.toDegrees(Math.atan2(x - centerPoint, centerPoint - y));
            if (degree > 0) {
                degree -= 360;
            }
            sweepAngle = degree;
        } else {
            float degree = (float) Math.toDegrees(Math.atan2(x - centerPoint, centerPoint - y));
            if (degree < 0) {
                degree += 360;
            }

            sweepAngle = degree;
        }
        progress = (sweepAngle * maxProgress / 360);
        invalidate();
    }

    /**
     * 设置可触摸点击时检查坐标时是否在规定的范围内，是则获取角度从而计算得到进度值
     *
     * @param x
     * @param y
     */
    private void checkForCorrect(float x, float y) {
        float distance = (float) Math.sqrt(Math.pow((x - centerPoint), 2) + Math.pow((y - centerPoint), 2));
        if (distance < drawOuterRadius / 2 + subtractingValue && distance > drawOuterRadius / 2 - subtractingValue * 2) {
            moveCorrect = true;
            if (clockWise) {
                float degree = (float) Math.toDegrees(Math.atan2(x - centerPoint, centerPoint - y));

                if (degree > 0) {
                    degree -= 360;
                }
                sweepAngle = degree;
            } else {
                float degree = (float) Math.toDegrees(Math.atan2(x - centerPoint, centerPoint - y));
                if (degree < 0) {
                    degree += 360;
                }

                sweepAngle = degree;
            }
            progress = (sweepAngle * maxProgress / 360);
            invalidate();
        }
    }

    /**
     * 设置是顺时针还是逆时针
     *
     * @param clockwise
     */
    public void setClockwise(boolean clockwise) {
        this.clockWise = clockwise;
        if (this.clockWise) {
            if (sweepAngle > 0) {
                sweepAngle = -sweepAngle;
            }
        }
        invalidate();
    }

    /**
     * 设置转圈背景的宽度
     *
     * @param width
     */
    public void setBackgroundProgressWidth(int width) {
        this.backgroundProgressWidth = width;
        outerCircle.setStrokeWidth(backgroundProgressWidth);
        requestLayout();
        invalidate();
    }

    /**
     * 设置转圈前景的宽度
     *
     * @param width
     */
    public void setForegroundProgressWidth(int width) {
        this.foregroundProgressWidth = width;
        innerCircle.setStrokeWidth(foregroundProgressWidth);
        requestLayout();
        invalidate();
    }

    /**
     * 设置转圈背景颜色
     *
     * @param color
     */
    public void setBackgroundProgressColor(int color) {
        this.backgroundProgressColor = color;
        outerCircle.setColor(color);
        requestLayout();
        invalidate();
    }

    /**
     * 设置转圈前景颜色
     *
     * @param color
     */
    public void setForegroundProgressColor(int color) {
        this.foregroundProgressColor = color;
        innerCircle.setColor(color);
        requestLayout();
        invalidate();
    }

    /**
     * 设置进度的最大值
     *
     * @param maxProgress
     */
    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

    /**
     * 返回进度的最大值
     */
    public float getMaxProgress() {
        return maxProgress;
    }

    /**
     * 获取当前进度
     *
     * @return
     */
    public float getProgress() {
        return progress;
    }

    /**
     * 设置进度
     *
     * @param progress
     */
    public void setProgress(float progress) {
        upgradeProgress(progress, false);
    }

    /**
     * 设置进度，可以改变进度的渐变颜色
     *
     * @param progress
     * @param selectColor
     */
    public void setProgress(float progress, int selectColor) {
        setDoughnutColors(selectColor);
        upgradeProgress(progress, false);
    }

    /**
     * 设置进度的动画和动画时间
     *
     * @param progress
     * @param duration
     */
    public void setProgressWithAnimation(float progress, int duration) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }

    /**
     * 设置可触摸改变进度
     *
     * @param enabled
     */
    public void enabledTouch(boolean enabled) {
        this.isTouchEnabled = enabled;
        invalidate();
    }

    /**
     * 设置两端为圆角还是矩形
     *
     * @param roundedCorner
     */
    public void setRoundedCorner(boolean roundedCorner) {
        if (roundedCorner) {
            //线帽，即画的两端是否带有圆角
            innerCircle.setStrokeCap(Paint.Cap.ROUND);
            outerCircle.setStrokeCap(Paint.Cap.ROUND);
        } else {
            // 线帽，即画的两端是否带有矩形
            innerCircle.setStrokeCap(Paint.Cap.SQUARE);
            outerCircle.setStrokeCap(Paint.Cap.SQUARE);
        }
        invalidate();
    }

    /**
     * 进度监听
     *
     * @param onProgressbarChangeListener
     */
    public void setOnProgressbarChangeListener(OnProgressbarChangeListener onProgressbarChangeListener) {
        this.onProgressbarChangeListener = onProgressbarChangeListener;
    }

    public interface OnProgressbarChangeListener {
        void onProgressChanged(CircleProgressbar circleSeekbar, float progress, boolean fromUser);

        void onStartTracking(CircleProgressbar circleSeekbar);

        void onStopTracking(CircleProgressbar circleSeekbar);
    }
}

