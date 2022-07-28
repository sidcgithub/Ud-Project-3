package com.udacity

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var colorWidth = 0.0f
    private var paint = Paint()
    private var progressPercent = 0f
    private var buttonText = "Download"
    private var progressColor: Int?
    private var circleBackgroundColor: Int?
    private var circleProgressColor: Int?
    private var textColor: Int?

    private val valueAnimator = ValueAnimator()
    var attributes: TypedArray? = context.getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.LoadingButton,
        0, 0
    )

    init {
        try {
            circleBackgroundColor =
                attributes?.getColor(R.styleable.LoadingButton_circleBackgroundColor, Color.CYAN);
            circleProgressColor =
                attributes?.getColor(R.styleable.LoadingButton_circleProgressColor, Color.LTGRAY);
            progressColor =
                attributes?.getColor(R.styleable.LoadingButton_progressColor, Color.LTGRAY);
            textColor =
                attributes?.getColor(R.styleable.LoadingButton_animatedTextColor, Color.LTGRAY);
        } finally {
            attributes?.recycle();
        }
    }


    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

        if (old == ButtonState.Completed && new == ButtonState.Clicked || old == ButtonState.Clicked && new == ButtonState.Completed) {
            progressPercent = 0f
        } else if (old == ButtonState.Clicked && new == ButtonState.Loading) {
            buttonText = "We are Loading"
            valueAnimator.setFloatValues(0.0f, 100.0f)
            valueAnimator.interpolator =
                TimeInterpolator { input -> Math.sqrt(input.toDouble()).toFloat() }
            valueAnimator.duration = 10000
            valueAnimator.addUpdateListener { animation ->
                progressPercent = animation.animatedValue as Float
            }
            valueAnimator.addListener(object :
                Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    progressPercent = 0f
                    p0?.start()
                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationRepeat(p0: Animator?) {

                }

            })
            valueAnimator.start()
        } else if (old == ButtonState.Loading && new == ButtonState.Completed) {
            valueAnimator.removeAllUpdateListeners()
            valueAnimator.removeAllListeners()
            valueAnimator.setFloatValues(progressPercent, 100.0f)
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.duration = 1000
            valueAnimator.addUpdateListener { animation ->
                progressPercent = animation.animatedValue as Float
            }
            valueAnimator.addListener(object :
                Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    progressPercent = 0f
                    buttonText = "Download"
                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationRepeat(p0: Animator?) {

                }

            })
            valueAnimator.start()

        }
    }


    override fun onDraw(canvas: Canvas?) {
        invalidate()
        val textPaint = Paint()
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 100.0f
        textPaint.color = textColor ?: Color.WHITE
        paint.style = Paint.Style.FILL
        paint.color = progressColor ?: Color.RED
        colorWidth = (progressPercent / 100.0f) * widthSize
        canvas?.drawRect(Rect(0, 0, colorWidth.toInt(), heightSize), paint)
        canvas?.drawText(
            buttonText,
            (widthSize / 2).toFloat(),
            (heightSize / 2 + 20).toFloat(),
            textPaint
        )
        val circlePaint = Paint()
        circlePaint.color = circleBackgroundColor ?: Color.RED
        circlePaint.style = Paint.Style.FILL
        canvas?.drawCircle(
            (widthSize - 100).toFloat(),
            (heightSize / 2).toFloat(),
            50f,
            circlePaint
        )
        val arcPaint = Paint()
        arcPaint.color = circleProgressColor ?: Color.BLACK
        arcPaint.style = Paint.Style.FILL
        canvas?.drawArc(
            RectF(
                (widthSize - 100).toFloat() - 50f,
                (heightSize / 2).toFloat() - 50,
                (widthSize - 50).toFloat(),
                (heightSize / 2).toFloat() + 50
            ), 0f, 360f * progressPercent / 100f, true, arcPaint
        )
        super.onDraw(canvas)

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}