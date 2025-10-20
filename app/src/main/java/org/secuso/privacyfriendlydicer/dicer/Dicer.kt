package org.secuso.privacyfriendlydicer.dicer

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import java.security.SecureRandom

/**
 * Created by yonjuni on 5/6/15.
 */
class Dicer(val onChange: (List<Dice>) -> Unit) {

    private var dices: MutableList<Dice> = mutableListOf()

    // This opt-in usage is a bug with the used kotlinx.serialization version.
    // Therefore this is safe.
    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    class Dice(internal var _value: Int, var faces: Int, var locked: Boolean = false) {
        val value: Int
            get() = _value
    }

    enum class SortOptions {
        ASCENDING,
        DESCENDING,
        NONE
    }

    fun reloadDice(diceNumber: Int, faceNum: Int) {
        dices = (0 until diceNumber).map { Dice(random.nextInt(faceNum) + 1, faceNum) }.toMutableList()
    }

    fun loadDice(dice: List<Dice>) {
        dices = dice.toMutableList()
    }

    fun rollDice() {
        onChange(rollDiceOnly())
    }

    fun rollDiceOnly(): List<Dice> {
        for (dice in dices) {
            if (!dice.locked) {
                dice._value = random.nextInt(dice.faces) + 1
            }
        }
        return dices
    }

    fun lock(index: Int) {
        if (dices.size < index || index < 0) {
            return
        }

        dices[index].locked = true
        onChange(dices)
    }

    fun unlock(index: Int) {
        if (dices.size < index || index < 0) {
            return
        }

        dices[index].locked = false
        onChange(dices)
    }

    fun toggleLock(index: Int) {
        if (dices.size < index || index < 0) {
            return
        }

        dices[index].apply {
            locked = !locked
        }
    }

    companion object {
        private val random = SecureRandom()
    }
}
