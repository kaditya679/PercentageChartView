package com.ramijemli.percentagechartview.renderer;

import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.R;

import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class RingModeRenderer extends BaseModeRenderer {


    public RingModeRenderer(IPercentageChartView view) {
        super(view);
        init();
    }

    public RingModeRenderer(IPercentageChartView view, TypedArray attrs) {
        super(view);
        init(attrs);
    }

    private void init(TypedArray attrs) {
        //DRAWING ORIENTATION
        orientation = attrs.getInt(R.styleable.PercentageChartView_pcv_orientation, ORIENTATION_CLOCKWISE);

        //BACKGROUND DRAW STATE
        drawBackground = attrs.getBoolean(R.styleable.PercentageChartView_pcv_drawBackground, true);

        //BACKGROUND COLOR
        mBackgroundColor = attrs.getColor(R.styleable.PercentageChartView_pcv_backgroundColor, DEFAULT_BACKGROUND_COLOR);


        //PROGRESS COLOR
        mProgressColor = attrs.getColor(R.styleable.PercentageChartView_pcv_progressColor, DEFAULT_PROGRESS_COLOR);

        //PROGRESS
        mProgress = mTextProgress = attrs.getInt(R.styleable.PercentageChartView_pcv_progress, 0);

        //TEXT COLOR
        mTextColor = attrs.getColor(R.styleable.PercentageChartView_pcv_textColor, DEFAULT_PROGRESS_COLOR);

        //TEXT SIZE
        mTextSize = attrs.getDimensionPixelSize(
                R.styleable.PercentageChartView_pcv_textSize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        DEFAULT_TEXT_SP_SIZE,
                        mView.getViewContext().getResources().getDisplayMetrics()
                ));

        //TEXT TYPEFACE
        String typeface = attrs.getString(R.styleable.PercentageChartView_pcv_typeface);
        if (typeface != null && !typeface.isEmpty()) {
            mTypeface = Typeface.createFromAsset(mView.getViewContext().getResources().getAssets(), typeface);
        }

        //TEXT STYLE
        mTextStyle = attrs.getInt(R.styleable.PercentageChartView_pcv_textStyle, Typeface.NORMAL);
        if (mTextStyle > 0) {
            if (mTypeface == null) {
                mTypeface = Typeface.defaultFromStyle(mTextStyle);
            } else {
                mTypeface = Typeface.create(mTypeface, mTextStyle);
            }
        }

        //START DRAWING ANGLE
        startAngle = attrs.getInt(R.styleable.PercentageChartView_pcv_startAngle, DEFAULT_START_ANGLE);
        if (startAngle < 0 || startAngle > 360) {
            startAngle = DEFAULT_START_ANGLE;
        }

        //PROGRESS ANIMATION DURATION
        mAnimDuration = attrs.getInt(R.styleable.PercentageChartView_pcv_animDuration, DEFAULT_ANIMATION_DURATION);

        //PROGRESS ANIMATION INTERPOLATOR
        int interpolator = attrs.getInt(R.styleable.PercentageChartView_pcv_animInterpolator, DEFAULT_ANIMATION_INTERPOLATOR);
        switch (interpolator) {
            case LINEAR:
                mAnimInterpolator = new LinearInterpolator();
                break;
            case ACCELERATE:
                mAnimInterpolator = new AccelerateInterpolator();
                break;
            case DECELERATE:
                mAnimInterpolator = new DecelerateInterpolator();
                break;
            case ACCELERATE_DECELERATE:
                mAnimInterpolator = new AccelerateDecelerateInterpolator();
                break;
            case ANTICIPATE:
                mAnimInterpolator = new AnticipateInterpolator();
                break;
            case OVERSHOOT:
                mAnimInterpolator = new OvershootInterpolator();
                break;
            case ANTICIPATE_OVERSHOOT:
                mAnimInterpolator = new AnticipateOvershootInterpolator();
                break;
            case BOUNCE:
                mAnimInterpolator = new BounceInterpolator();
                break;
            case FAST_OUT_LINEAR_IN:
                mAnimInterpolator = new FastOutLinearInInterpolator();
                break;
            case FAST_OUT_SLOW_IN:
                mAnimInterpolator = new FastOutSlowInInterpolator();
                break;
            case LINEAR_OUT_SLOW_IN:
                mAnimInterpolator = new LinearOutSlowInInterpolator();
                break;
        }

        prepare();
    }

    private void init() {
        orientation = ORIENTATION_CLOCKWISE;

        mBackgroundColor = DEFAULT_BACKGROUND_COLOR;

        mProgressColor = DEFAULT_PROGRESS_COLOR;
        mProgress = mTextProgress = 0;

        mTextColor = mProgressColor;
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_TEXT_SP_SIZE,
                mView.getViewContext().getResources().getDisplayMetrics()
        );
        mTextStyle = Typeface.NORMAL;

        startAngle = DEFAULT_START_ANGLE;

        mAnimDuration = DEFAULT_ANIMATION_DURATION;
        mAnimInterpolator = new LinearInterpolator();

        prepare();
    }

    private void prepare() {
        mCircleBounds = new RectF();
        mTextBounds = new Rect();
        arcAngle = mProgress / DEFAULT_MAX * 360;

        //BACKGROUND
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        //PROGRESS
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.FILL);

        //TEXT
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        if (mTypeface != null) {
            mTextPaint.setTypeface(mTypeface);
        }

        //ANIMATION
        mValueAnimator = ValueAnimator.ofFloat(0, mProgress);
        mValueAnimator.setDuration(mAnimDuration);
        mValueAnimator.setInterpolator(mAnimInterpolator);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mProgress = (float) valueAnimator.getAnimatedValue();

                if (mProgress > 0 && mProgress <= 100)
                    mTextProgress = (int) mProgress;
                else if (mProgress > 100)
                    mTextProgress = 100;
                else mTextProgress = 0;

                mView.onProgressUpdated(mProgress);
                mView.requestInvalidate();
            }
        });
    }

    @Override
    public void mesure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        int centerX = w / 2;
        int centerY = h / 2;
        float radius = (float) Math.min(w, h) / 2;

        mCircleBounds.left = centerX - radius;
        mCircleBounds.top = centerY - radius;
        mCircleBounds.right = centerX + radius;
        mCircleBounds.bottom = centerY + radius;
    }

    @Override
    public void draw(Canvas canvas) {

        //FOREGROUND
        if (mProgress != 0) {
            if (orientation == ORIENTATION_COUNTERCLOCKWISE)
                arcAngle = -(this.mProgress / DEFAULT_MAX * 360);
            else
                arcAngle = this.mProgress / DEFAULT_MAX * 360;
            canvas.drawArc(mCircleBounds, startAngle, arcAngle, true, mProgressPaint);
        }

        //BACKGROUND
        if (drawBackground) {
            canvas.drawArc(mCircleBounds, startAngle + arcAngle, 360 - arcAngle, true, mBackgroundPaint);
        }

        //TEXT
        String text = String.valueOf(mTextProgress) + "%";
        mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        int textHeight = mTextBounds.height();
        canvas.drawText(text, mCircleBounds.centerX(), mCircleBounds.centerY() + (textHeight / 2f), mTextPaint);
    }

    @Override
    public void destroy() {
        if (mValueAnimator != null) {
            if (mValueAnimator.isRunning())
                mValueAnimator.cancel();

            mValueAnimator.removeAllUpdateListeners();
        }

        mValueAnimator = null;
        mCircleBounds = null;
        mTextBounds = null;
        mBackgroundPaint = mProgressPaint = mTextPaint = null;
    }

    @Override
    public void setProgress(float progress, boolean animate) {
        if (this.mProgress == progress) return;

        if (mValueAnimator.isRunning()) mValueAnimator.cancel();

        if (!animate) {
            this.mProgress = progress;
            this.mTextProgress = (int) progress;
            mView.onProgressUpdated(mProgress);
            mView.requestInvalidate();
            return;
        }

        mValueAnimator.setFloatValues(mProgress, progress);
        mValueAnimator.start();
    }
}
