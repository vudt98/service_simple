package com.example.myapplication

import android.Manifest
import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.AppOpsManager.OnOpChangedListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication.databinding.ActivityMainBinding


const val READ_PHONE_STATE_REQUEST = 37

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var testIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val service = ServiceTest::class.java
        testIntent = Intent(applicationContext, service)

        requestPermissions()

        binding.stop.setOnClickListener {
            testIntent?.action = "stop"
            startService(testIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isRunning()) {
            startService(testIntent)
        }
    }

    private fun isRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.example.myapplication.ServiceTest" == service.service.className)
                return true
        }
        return false
    }

    private fun hasPermissionToReadPhoneStats(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_DENIED
    }

    private fun requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats()
        }
    }

    private fun hasPermissionToReadNetworkHistory(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
            applicationContext.packageName,
            object : OnOpChangedListener {
                @TargetApi(Build.VERSION_CODES.M)
                override fun onOpChanged(op: String, packageName: String) {
                    val mode = appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(), getPackageName()
                    )
                    if (mode != AppOpsManager.MODE_ALLOWED) {
                        return
                    }
                    appOps.stopWatchingMode(this)
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    if (getIntent().extras != null) {
                        intent.putExtras(getIntent().extras!!)
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    applicationContext.startActivity(intent)
                }
            })
        requestReadNetworkHistoryAccess()
        return false
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun requestReadNetworkHistoryAccess() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun requestPhoneStateStats() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_PHONE_STATE), READ_PHONE_STATE_REQUEST
        )
    }
}