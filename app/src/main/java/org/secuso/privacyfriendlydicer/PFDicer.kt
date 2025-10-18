package org.secuso.privacyfriendlydicer

import android.app.Activity
import android.util.Log
import androidx.work.Configuration
import org.secuso.pfacore.ui.PFApplication
import org.secuso.pfacore.ui.PFData
import org.secuso.privacyfriendlydicer.ui.MainActivity

class PFDicer: PFApplication() {

    override val name: String
        get() = getString(R.string.app_name)
    override val data: PFData
        get() = PFApplicationData.instance(this).data
    override val mainActivity: Class<out Activity> = MainActivity::class.java

    override val workManagerConfiguration = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
}