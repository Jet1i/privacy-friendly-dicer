package org.secuso.privacyfriendlydicer.dicer

import java.security.SecureRandom

/**
 * Created by yonjuni on 5/6/15.
 */
class Dicer {
    fun rollDice(poolSize: Int, faceNum: Int): IntArray {
        val dice = IntArray(poolSize)

        for (i in dice.indices) {
            dice[i] = random.nextInt(faceNum) + 1
        }

        return dice
    }

    companion object {
        private val random = SecureRandom()
    }
}
