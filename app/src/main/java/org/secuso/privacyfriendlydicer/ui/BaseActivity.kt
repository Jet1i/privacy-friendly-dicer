package org.secuso.privacyfriendlydicer.ui

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import androidx.lifecycle.ViewModelProvider
import org.secuso.pfacore.model.DrawerMenu
import org.secuso.pfacore.ui.activities.DrawerActivity
import org.secuso.privacyfriendlydicer.PFApplicationData
import org.secuso.privacyfriendlydicer.R
import org.secuso.privacyfriendlydicer.sensors.ShakeListener

abstract class BaseActivity: DrawerActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[BaseActivityViewModel::class.java] }
    private val shakingEnabled by lazy { PFApplicationData.instance(this).rollByShaking }
    private val vibrationEnabled by lazy { PFApplicationData.instance(this).enableVibration }

    // for Shaking
    private val sensorManager: SensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private val accelerometer: Sensor? by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private val shakeListener = ShakeListener { PFApplicationData.instance(this).shakeThreshold.value }

    abstract val onShake: (() -> Unit)?

    override fun drawer() = DrawerMenu.build {
        name = getString(R.string.app_name)
        icon = R.mipmap.ic_drawer
        section {
            activity {
                name = getString(R.string.activity_main_title)
                icon = R.drawable.ic_menu_dice
                clazz = MainActivity::class.java
            }
            val diceMode = PFApplicationData.instance(this@BaseActivity).selectedDiceMode.value
            if (viewModel.isValidDiceMode(diceMode)) {
                activity {
                    name = viewModel.getDiceModeName(diceMode)
                    icon = R.drawable.baseline_play_circle_outline_24
                    clazz = DiceModeActivity::class.java
                }
            }
            activity {
                name = getString(R.string.activity_manage_dice_modes_title)
                icon = R.drawable.puzzle
                clazz = ManageDiceModesActivity::class.java
            }
        }
        defaultDrawerSection(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Shaking
        shakeListener.onShakeListener = object : ShakeListener.OnShakeListener {
            override fun onShake(count: Int) {
                if (shakingEnabled.value && onShake != null) {
                    onShake!!()
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            shakeListener, accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    public override fun onPause() {
        sensorManager.unregisterListener(shakeListener)
        super.onPause()
    }

    fun vibrate(time: Long) {
        if (vibrationEnabled.value) {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(time)
        }
    }
}