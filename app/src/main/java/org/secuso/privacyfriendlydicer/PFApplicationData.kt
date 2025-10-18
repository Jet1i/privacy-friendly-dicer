package org.secuso.privacyfriendlydicer

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.map
import org.secuso.pfacore.model.Theme
import org.secuso.pfacore.model.about.About
import org.secuso.pfacore.model.preferences.Preferable
import org.secuso.pfacore.ui.PFData
import org.secuso.pfacore.ui.help.Help
import org.secuso.pfacore.ui.preferences.appPreferences
import org.secuso.pfacore.ui.preferences.settings.appearance
import org.secuso.pfacore.ui.preferences.settings.general
import org.secuso.pfacore.ui.preferences.settings.preferenceFirstTimeLaunch
import org.secuso.pfacore.ui.preferences.settings.radio
import org.secuso.pfacore.ui.preferences.settings.settingDeviceInformationOnErrorReport
import org.secuso.pfacore.ui.preferences.settings.settingThemeSelector
import org.secuso.pfacore.ui.preferences.settings.switch
import org.secuso.pfacore.ui.tutorial.buildTutorial

class PFApplicationData private constructor(context: Context) {

    // Preferences
    lateinit var theme: Preferable<String>
        private set
    lateinit var firstTimeLaunch: Preferable<Boolean>
        private set
    lateinit var includeDeviceDataInReport: Preferable<Boolean>
        private set
    lateinit var lastChosenPage: Preferable<Int>
        private set
    lateinit var rollByShaking: Preferable<Boolean>
        private set
    lateinit var shakeThreshold: Preferable<Float>
        private set
    lateinit var enableVibration: Preferable<Boolean>
        private set

    private val preferences = appPreferences(context) {
        preferences {
            firstTimeLaunch = preferenceFirstTimeLaunch
            lastChosenPage = preference {
                key = "lastChosenPage"
                default = 0
                backup = false
            }
        }
        settings {
            appearance {
                theme = settingThemeSelector
            }
            general {
                rollByShaking = switch {
                    key = "enable_shaking"
                    title { resource(R.string.enable_shaking) }
                    summary { resource(R.string.enable_shaking_desc) }
                    default = true
                }
                shakeThreshold = radio {
                    key = "shake_threshold"
                    dependency = {
                        "enable_shaking" on true
                    }
                    default = 1.4F
                    title { resource(R.string.shake_threshold_title) }
                    summary { transform { state, value -> state.entries.find { it.value == value }!!.entry } }
                    entries {
                        entries(R.array.shake_threshold_entries)
                        values(resources.getStringArray(R.array.shake_threshold_values).map { it.toFloat() })
                    }
                }
                enableVibration = switch {
                    key = "enable_vibration"
                    title { resource(R.string.vibration_title) }
                    summary { resource(R.string.vibration_desc) }
                    default = true
                }
                includeDeviceDataInReport = settingDeviceInformationOnErrorReport
            }
        }
    }

    private val help = Help.build(context) {
        listOf(
            R.string.help_usage to R.string.help_general_description,
            R.string.help_usage_roll to R.string.help_general_dicing,
            R.string.help_permissions_usage to R.string.help_permissions_description
        ).forEach { (q, a) ->
            item {
                title { resource(q) }
                description { resource(a) }
            }
        }
    }

    private val about = About(
        name = context.resources.getString(R.string.app_name),
        version = BuildConfig.VERSION_NAME,
        authors = context.resources.getString(R.string.about_author_names),
        repo = context.resources.getString(R.string.about_github)
    )

    private val tutorial = buildTutorial {
        stage {
            title = ContextCompat.getString(context, R.string.slide1_heading)
            images = listOf(R.mipmap.ic_splash)
            description = ContextCompat.getString(context, R.string.slide1_text)
        }
    }

    val data = PFData(
        about = about,
        help = help,
        settings = preferences.settings,
        tutorial = tutorial,
        theme = theme.state.map { Theme.valueOf(it) },
        firstLaunch = firstTimeLaunch,
        includeDeviceDataInReport = includeDeviceDataInReport,
    )

    companion object {
        private var _instance: PFApplicationData? = null
        fun instance(context: Context): PFApplicationData {
            if (_instance == null) {
                _instance = PFApplicationData(context)
            }
            return _instance!!
        }
    }
}