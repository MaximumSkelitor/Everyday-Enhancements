package com.weberpackage.trashcanmodsreal.client

import kotlin.math.pow

object ZoomFeature {
    private const val BASE_ZOOM = 2.0
    private const val SCROLL_STEP = 1.2
    private const val SMOOTHING_SPEED = 18.0

    private var currentMultiplier = 1.0
    val active: Boolean
        get() = multiplier > 1.001
    val multiplier: Double
        get() {
            advanceTween()
            return currentMultiplier
        }
    private var inputActive = false
    private var targetMultiplier = 1.0
    private var lastUpdateNanos = System.nanoTime()

    fun tick(held: Boolean, canZoom: Boolean) {
        inputActive = held && canZoom
        if (inputActive) {
            if (targetMultiplier < BASE_ZOOM) targetMultiplier = BASE_ZOOM
        } else {
            targetMultiplier = 1.0
        }

        advanceTween()
    }

    private fun advanceTween() {
        val now = System.nanoTime()
        val elapsedSeconds = ((now - lastUpdateNanos).coerceAtLeast(0L) / 1_000_000_000.0)
            .coerceAtMost(0.1)
        lastUpdateNanos = now

        val blend = 1.0 - kotlin.math.exp(-SMOOTHING_SPEED * elapsedSeconds)
        currentMultiplier += (targetMultiplier - currentMultiplier) * blend
        if (kotlin.math.abs(targetMultiplier - currentMultiplier) < 0.001) {
            currentMultiplier = targetMultiplier
        }
    }

    fun adjustFromScroll(amount: Double) {
        if (!inputActive || amount == 0.0) return
        val adjusted = targetMultiplier * SCROLL_STEP.pow(amount)
        targetMultiplier = when {
            adjusted.isNaN() -> Double.MAX_VALUE
            adjusted < BASE_ZOOM -> BASE_ZOOM
            else -> adjusted
        }
    }
}
