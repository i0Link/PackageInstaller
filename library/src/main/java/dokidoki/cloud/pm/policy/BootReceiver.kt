package dokidoki.cloud.pm.policy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dokidoki.cloud.pm.config.GlobalValues

class BootReceiver : BroadcastReceiver() {

    private val TAG = javaClass.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.i(TAG, "boot completed ${intent.action}")
            val isAutoUpdate =
                GlobalValues.instance.getConfig<Boolean>(GlobalValues.CONFIG_AUTO_UPDATE, false)
            if (!isAutoUpdate) {
                Log.i(TAG, "not auto update")
                return
            }
            // 启动前台服务
            val serviceIntent = Intent(context, BackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}