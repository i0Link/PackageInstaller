package dokidoki.cloud.pm.installer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import dokidoki.cloud.pm.installer.abs.IPMInstaller
import com.jeremyliao.liveeventbus.LiveEventBus
import java.io.File

/**
 * 无权限调用安装窗口安装apk
 */
internal class NormalPMInstaller(context: Context) : IPMInstaller(context) {
    override fun install(filePath: String) {
        val context = weakContext.get()
        context ?: return
        context.startActivity(Intent().apply {
            action = "android.intent.action.VIEW"
            setDataAndType(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        context, "${context.packageName}.acd38.fileprovider", File(filePath)
                    )
                } else {
                    Uri.fromFile(File(filePath))
                }, "application/vnd.android.package-archive"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            addCategory("android.intent.category.DEFAULT")
            putExtra("SY_APPMARKEY", "sy_appmarket")
        })
        LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG)
            .post(InstallerLog(this, InstallerLog.Companion.TYPE_INSTALL, true, path = filePath))
    }

    override fun uninstall(packageName: String) {
        val context = weakContext.get()
        context ?: return
        context.startActivity(Intent().apply {
            action = "android.intent.action.DELETE"
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG).post(
            InstallerLog(
                this, InstallerLog.Companion.TYPE_UNINSTALL, true, message = "Uninstall success!"
            )
        )
    }

    override fun reset(packageName: String) {

    }
}