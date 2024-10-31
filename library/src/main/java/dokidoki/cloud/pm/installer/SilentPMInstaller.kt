package dokidoki.cloud.pm.installer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageDataObserver
import android.content.pm.IPackageDeleteObserver
import android.content.pm.IPackageInstallObserver
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import dokidoki.cloud.pm.installer.PMInstaller.Companion.PACKAGE_INSTALL_ACTION
import dokidoki.cloud.pm.installer.abs.IPMInstaller
import dokidoki.cloud.pm.utils.callMethod
import com.jeremyliao.liveeventbus.LiveEventBus
import java.io.File

/**
 * 系统权限静默安装器
 */
internal class SilentPMInstaller(context: Context) : IPMInstaller(context) {

    private val mPackageManager by lazy { context.packageManager }

    override fun install(filePath: String) {

        when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.BASE..Build.VERSION_CODES.M ->
                installBeforeM(filePath)

            else -> installAfterM(filePath)
        }
    }

    override fun uninstall(packageName: String) {
        try {
            mPackageManager.callMethod<Any>(
                "deletePackage", arrayOf(
                    String::class.java,
                    IPackageDeleteObserver::class.java,
                    Int::class.java
                ), packageName, object : IPackageDeleteObserver.Stub() {
                    override fun packageDeleted(packageName: String?, returnCode: Int) {
                        LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG).post(
                            InstallerLog(
                                this@SilentPMInstaller,
                                InstallerLog.Companion.TYPE_UNINSTALL,
                                returnCode == 1,
                                packageName = packageName ?: ""
                            )
                        )
                    }
                }, 0
            )
        } catch (e: Exception) {
            LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG).post(
                InstallerLog(
                    this@SilentPMInstaller,
                    InstallerLog.Companion.TYPE_UNINSTALL,
                    false,
                    packageName = packageName, message = e.message.toString()
                )
            )
        }
    }

    override fun reset(packageName: String) {
        mPackageManager.callMethod<Any>("deleteApplicationCacheFiles",
            arrayOf(String::class.java, IPackageDataObserver::class.java),
            packageName,
            object : IPackageDataObserver.Stub() {
                override fun onRemoveCompleted(packageName: String?, succeeded: Boolean) {

                }
            })
    }

    private fun installBeforeM(filePath: String) {
        val installObserver = object : IPackageInstallObserver.Stub() {
            override fun packageInstalled(packageName: String?, returnCode: Int) {
                LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG).post(
                    InstallerLog(
                        this@SilentPMInstaller,
                        InstallerLog.Companion.TYPE_INSTALL,
                        returnCode == 1,
                        path = filePath,
                        packageName = packageName ?: ""
                    )
                )
            }
        }
        try {
            mPackageManager?.callMethod<Any>(
                "installPackage",
                arrayOf(
                    Uri::class.java,
                    IPackageInstallObserver::class.java,
                    Int::class.java,
                    String::class.java
                ),
                Uri.fromFile(File(filePath)),
                installObserver,
                0x2,
                weakContext.get()?.packageName
            )
        } catch (e: Exception) {
            LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG).post(
                InstallerLog(
                    this@SilentPMInstaller,
                    InstallerLog.Companion.TYPE_INSTALL,
                    false,
                    path = filePath,
                    message = e.message.toString()
                )
            )
            Log.e(TAG, e.message ?: "", e)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun installAfterM(filePath: String) {
        val context = weakContext.get() ?: return
        val installer = mPackageManager.packageInstaller
        installer.openSession(
            installer.createSession(
                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            )
        ).apply {
            openWrite("test.apk", 0, -1).use {
                it.write(File(filePath).readBytes())
            }
            commit(
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(PACKAGE_INSTALL_ACTION),
                    0
                ).intentSender
            )
        }
    }
}