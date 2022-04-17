package com.example.myapplication

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.widget.AppCompatTextView


class TextAnimation : AppCompatTextView {
    private var mText: CharSequence? = null
    private var mIndex = 0
    private var mDelay: Long = 40 //Default delay in ms
    var isAnimationRunning = false
        private set
    private var mAnimationChangeListener: OnAnimationChangeListener? = null
    private var avoidTextOverflowAtEdge = true
    private var globalLayoutListener: OnGlobalLayoutListener? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    private val mHandler: Handler = Handler()
    private val characterAdder: Runnable = object : Runnable {
        override fun run() {
            text = mText!!.subSequence(0, mIndex++)
            if (mIndex <= mText!!.length) {
                mHandler.postDelayed(this, mDelay)
                isAnimationRunning = true
            } else {
                isAnimationRunning = false
                pingAnimationEnded()
            }
        }
    }

    fun animateText(text: CharSequence) {
        generateText(text.toString())
        mIndex = 0
        setText("")
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun stopAnimation() {
        if (isAnimationRunning) {
            isAnimationRunning = false
            mHandler.removeCallbacks(characterAdder)
            text = mText
            pingAnimationEnded()
        }
    }

    val isTextInitialised: Boolean
        get() = mText != null

    //To Explicitly Change the Delay
    fun setCharacterDelay(millis: Long) {
        mDelay = millis
    }

    private fun generateText(inpText: String) {
        if (avoidTextOverflowAtEdge) {
            globalLayoutListener = OnGlobalLayoutListener {
                mText = generateFormattedSequence(inpText)
                viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            }
            viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        }
        mText = inpText
    }

    private fun generateFormattedSequence(mText: String): String {
        val words = mText.split(" ").toTypedArray()
        val viewWidth = measuredWidth
        val finalSequence = StringBuilder()
        for (word in words) {
            val temp =
                finalSequence.substring(finalSequence.lastIndexOf("\n").coerceAtLeast(0)) + " " + word
            val textWidth = paint.measureText(temp)
            when {
                textWidth >= viewWidth -> finalSequence.append("\n")
                    .append(word)
                finalSequence.isEmpty() -> finalSequence.append(word)
                else -> finalSequence.append(
                    " "
                ).append(word)
            }
        }
        return finalSequence.toString()
    }

    private fun pingAnimationEnded() {
        if (mAnimationChangeListener != null) mAnimationChangeListener!!.onAnimationEnd()
    }

    //Explicitly turnoff "avoidTextOverflowAtEdge" to avoid weird text formatting in few cases (when view size is dynamic).
    fun avoidTextOverflowAtEdge(avoidTextOverflowAtEdge: Boolean) {
        this.avoidTextOverflowAtEdge = avoidTextOverflowAtEdge
    }

    interface OnAnimationChangeListener {
        fun onAnimationEnd()
    }

    fun setOnAnimationChangeListener(onAnimationChangeListener: OnAnimationChangeListener?) {
        mAnimationChangeListener = onAnimationChangeListener
    }
}