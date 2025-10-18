package org.secuso.privacyfriendlydicer.ui

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.secuso.pfacore.model.DrawerElement
import org.secuso.pfacore.model.DrawerMenu
import org.secuso.pfacore.ui.activities.DrawerActivity
import org.secuso.privacyfriendlydicer.PFApplicationData
import org.secuso.privacyfriendlydicer.R
import org.secuso.privacyfriendlydicer.databinding.ActivityMainBinding
import org.secuso.privacyfriendlydicer.sensors.ShakeListener
import java.util.Locale

class MainActivity : DrawerActivity() {
    private val dicerViewModel by lazy { ViewModelProvider(this).get<DicerViewModel>(DicerViewModel::class.java) }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val adapter: DiceAdapter by lazy { DiceAdapter(dicerViewModel.dicerLiveData.value ?: IntArray(0), layoutInflater) }
    private val shakingEnabled by lazy { PFApplicationData.instance(this).rollByShaking }
    private val vibrationEnabled by lazy { PFApplicationData.instance(this).enableVibration }

    // for Shaking
    private val sensorManager: SensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private val accelerometer: Sensor? by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private val shakeListener = ShakeListener { PFApplicationData.instance(this).shakeThreshold.value }

    override fun drawer() = DrawerMenu.build {
        name = getString(R.string.app_name)
        icon = R.mipmap.ic_drawer
        section {
            activity {
                name = getString(R.string.activity_main_title)
                icon = R.drawable.ic_menu_dice
                clazz = MainActivity::class.java
            }
        }
        defaultDrawerSection(this)
    }

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.ic_menu_dice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        
        binding.dices.adapter = adapter

        initResources()

        dicerViewModel.dicerLiveData.observe(this, object : Observer<IntArray> {
            override fun onChanged(dice: IntArray) {
                adapter.dices = dice
                displaySum()

                if (vibrationEnabled.value) {
                    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(50)
                }
            }
        })

        dicerViewModel.diceNumberLiveData.observe(this, object : Observer<Int> {
            override fun onChanged(number: Int) {
                binding.chooseDiceNumber.text = String.format(Locale.ENGLISH, "%d", number)
            }
        })

        dicerViewModel.faceNumberLiveData.observe(this, object : Observer<Int> {
            override fun onChanged(number: Int) {
                binding.chooseFaceNumber.text = String.format(Locale.ENGLISH, "%d", number)
            }
        })
    }

    private fun initResources() {
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dicerViewModel.setDiceNumber(progress + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.seekBarFace.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dicerViewModel.setFaceNumber(progress + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        //Button
        binding.rollButton.setOnClickListener { rollDice() }

        //Shaking
        shakeListener.onShakeListener = object : ShakeListener.OnShakeListener {
            override fun onShake(count: Int) {
                if (shakingEnabled.value) {
                    rollDice()
                }
            }
        }
    }

    fun rollDice() {
        dicerViewModel.rollDice()
    }

    private fun displaySum() {
        binding.sumTextView.text = getString(R.string.main_dice_sum, adapter.dices.sum().toString())
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
}
