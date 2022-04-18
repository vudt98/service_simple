package com.example.myapplication.utils

import android.content.Context
import com.example.myapplication.R
import java.util.*


internal class Speed(val context: Context?) {
    private var mTotalSpeed: Long = 0
    private var mDownSpeed: Long = 0
    private var mUpSpeed: Long = 0
    var total = HumanSpeed()
    var down = HumanSpeed()
    var up = HumanSpeed()
    private var mIsSpeedUnitBits = false
    private fun updateHumanSpeeds() {
        total.setSpeed(mTotalSpeed)
        down.setSpeed(mDownSpeed)
        up.setSpeed(mUpSpeed)
    }

    fun calcSpeed(timeTaken: Long, downBytes: Long, upBytes: Long) {
        var totalSpeed: Long = 0
        var downSpeed: Long = 0
        var upSpeed: Long = 0
        val totalBytes = downBytes + upBytes
        if (timeTaken > 0) {
            totalSpeed = totalBytes * 1000 / timeTaken
            downSpeed = downBytes * 1000 / timeTaken
            upSpeed = upBytes * 1000 / timeTaken
        }
        mTotalSpeed = totalSpeed
        mDownSpeed = downSpeed
        mUpSpeed = upSpeed
        updateHumanSpeeds()
    }

    fun getHumanSpeed(name: String?): HumanSpeed {
        return when (name) {
            "up" -> up
            "down" -> down
            else -> total
        }
    }

    fun setIsSpeedUnitBits(isSpeedUnitBits: Boolean) {
        mIsSpeedUnitBits = isSpeedUnitBits
    }

    internal inner class HumanSpeed {
        var speedValue: String? = null
        var speedUnit: String? = null
        fun setSpeed(data: Long) {
            var speed = data
            if (context == null) return
            if (mIsSpeedUnitBits) {
                speed *= 8
            }
            when {
                speed < 1000000 -> {
                    speedUnit =
                        context.getString(if (mIsSpeedUnitBits) R.string.kbps else R.string.kBps)
                    speedValue = (speed / 1000).toString()
                }
                speed >= 1000000 -> {
                    speedUnit =
                        context.getString(if (mIsSpeedUnitBits) R.string.Mbps else R.string.MBps)
                    speedValue = when {
                        speed < 10000000 -> {
                            java.lang.String.format(Locale.ENGLISH, "%.1f", speed / 1000000.0)
                        }
                        speed < 100000000 -> {
                            (speed / 1000000).toString()
                        }
                        else -> {
                            context.getString(R.string.plus99)
                        }
                    }
                }
                else -> {
                    speedValue = context.getString(R.string.dash)
                    speedUnit = context.getString(R.string.dash)
                }
            }
        }
    }

    init {
        updateHumanSpeeds()
    }
}