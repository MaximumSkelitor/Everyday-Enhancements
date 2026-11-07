package com.weberpackage.trashcanmodsreal.client

import java.util.ArrayDeque

object CpsCounter {
    private const val WINDOW_NANOS = 1_000_000_000L
    private val leftClicks = ArrayDeque<Long>()
    private val rightClicks = ArrayDeque<Long>()

    fun record(button: Int) {
        val now = System.nanoTime()
        when (button) {
            0 -> leftClicks.addLast(now)
            1 -> rightClicks.addLast(now)
        }
        prune(leftClicks, now)
        prune(rightClicks, now)
    }

    fun clicksPerSecond(): Int = count(leftClicks) + count(rightClicks)

    private fun count(clicks: ArrayDeque<Long>): Int {
        prune(clicks, System.nanoTime())
        return clicks.size
    }

    private fun prune(clicks: ArrayDeque<Long>, now: Long) {
        while (clicks.isNotEmpty() && now - clicks.first() >= WINDOW_NANOS) {
            clicks.removeFirst()
        }
    }
}
