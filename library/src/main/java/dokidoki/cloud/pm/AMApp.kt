package dokidoki.cloud.pm

import android.content.Context
import dokidoki.cloud.pm.config.GlobalValues
import dokidoki.cloud.pm.installer.PMInstaller

object AMApp {
    fun onAppCreate(context: Context) {
        GlobalValues.init(context)
        PMInstaller.init(context)
        // 开启后台常驻
        GlobalValues.instance.setConfig(GlobalValues.CONFIG_AUTO_UPDATE, true.toString())
    }
}