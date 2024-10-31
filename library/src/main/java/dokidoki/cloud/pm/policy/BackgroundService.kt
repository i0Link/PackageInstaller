package dokidoki.cloud.pm.policy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dokidoki.cloud.pm.IApkManagerInterface
import dokidoki.cloud.pm.IGuardInterface

@Suppress("DEPRECATION")
class BackgroundService : Service() {

    private val TAG = javaClass.simpleName

    private val mApkManagerImpl by lazy { ApkManagerImpl() }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "background service start")
        // notification for android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    "cloud-appstore",
                    "App Store Service",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            startForeground(
                1000,
                NotificationCompat.Builder(this, "cloud-appstore").setContentTitle("Amt MainService")
                    .build()
            )
        }
        startGuardService()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return if (intent.getBooleanExtra("guard", false)) {
            guardServiceObserver
        } else {
            mApkManagerImpl
        }
    }


    private inner class ApkManagerImpl : IApkManagerInterface.Stub()

    private fun stopAllService() {
        serviceNotify = null
        guardServiceObserver.killSelf()
        stopSelf()
    }


    //<editor-fold desc="守护进程操作相关">
    private var serviceNotify: IGuardInterface? = null
    private val mGuardServiceConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                serviceNotify = IGuardInterface.Stub.asInterface(service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceNotify = null
            }
        }
    }
    private val guardServiceObserver by lazy {
        object : IGuardInterface.Stub() {
            override fun killSelf() {

            }

            override fun serviceDied() {
                startGuardService()
            }
        }
    }

    private fun startGuardService() {
        val intent = Intent(this, GuardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, mGuardServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        Log.i(TAG, "background service destroy")
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        serviceNotify?.serviceDied()
    }
    //</editor-fold>
}