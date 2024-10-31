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
import dokidoki.cloud.pm.IGuardInterface

// 常驻进程保活
@Suppress("DEPRECATION")
class GuardService : Service() {

    private val TAG = javaClass.simpleName

    private var guardServiceNotify: IGuardInterface? = null

    private val mainServiceObserver by lazy {
        object : IGuardInterface.Stub() {
            override fun killSelf() {
                guardServiceNotify = null
                stopSelf()
            }

            override fun serviceDied() {
                startMainService()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "guard service start")
        // notification for android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    "cloud-appstore-guard",
                    "App Store Guard Service",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            startForeground(
                2000,
                NotificationCompat.Builder(this, "cloud-appstore").setContentTitle("Amt GuardService")
                    .build()
            )
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return mainServiceObserver
    }

    private fun startMainService() {
        val intent = Intent(this, BackgroundService::class.java).apply {
            putExtra("guard", true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                guardServiceNotify = IGuardInterface.Stub.asInterface(service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                guardServiceNotify = null
            }

        }, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        Log.i(TAG, "guard service destroy")
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        guardServiceNotify?.serviceDied()
    }
}