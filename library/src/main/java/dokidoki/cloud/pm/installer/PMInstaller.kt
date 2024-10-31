package dokidoki.cloud.pm.installer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import androidx.lifecycle.Observer
import dokidoki.cloud.pm.installer.abs.IPMInstaller
import dokidoki.cloud.pm.installer.abs.IPMObserver
import dokidoki.cloud.pm.utils.get
import com.jeremyliao.liveeventbus.LiveEventBus
import java.io.File
import java.lang.ref.WeakReference

/**
 * 安装器分发类
 */
class PMInstaller : Observer<InstallerLog> {

    /**
     * 观察者列表
     */
    private val observerList: ArrayList<IPMObserver> by lazy { ArrayList() }

    private var weakContext: WeakReference<Context>? = null

    /**
     * 当前安装器
     */
    private var currentInstaller: IPMInstaller? = null
    private val installActionReceiver by lazy { InstallActionReceiver() }

    companion object {
        const val PACKAGE_INSTALL_ACTION = "dokidoki.cloud.pm.PACKAGE_INSTALL"

        private const val TAG = "PMInstaller"
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { PMInstaller() }
        private val INSTALLER = arrayListOf(
            SilentPMInstaller::class.java,
            NormalPMInstaller::class.java
        )

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun init(appContext: Context) {
            instance.weakContext = WeakReference(appContext)
            LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG).observeForever(instance)
            appContext.registerReceiver(
                instance.installActionReceiver,
                IntentFilter(PACKAGE_INSTALL_ACTION)
            )
        }
    }

    override fun onChanged(value: InstallerLog) {
        when (value.type) {
            InstallerLog.Companion.TYPE_INSTALL -> {
                if (value.success) {
                    observerList.forEach { it.onInstallSuccess(value.packageName) }
                } else {
                    val nextInstaller = findNextInstaller(value.installer)
                    if (nextInstaller != null) {
                        nextInstaller.install(value.path)
                    } else {
                        observerList.forEach { it.onInstallFailed(value.path, value.message) }
                    }
                }

            }

            InstallerLog.Companion.TYPE_UNINSTALL -> {
                if (value.success) {
                    observerList.forEach { it.onUninstallSuccess(value.packageName) }
                } else {
                    val nextInstaller = findNextInstaller(value.installer)
                    if (nextInstaller != null) {
                        nextInstaller.uninstall(value.packageName)
                    } else {
                        observerList.forEach {
                            it.onUninstallFailed(
                                value.packageName, value.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 安装apk
     */
    fun install(path: String) {
        val apkFile = File(path)
        if (!apkFile.exists()) {
            Log.e(TAG, "apk file not exists")
            observerList.forEach { it.onInstallFailed(path, "apk file not exists") }
            return
        }
        findNextInstaller(null)?.install(path)
    }

    /**
     * 线上安装apk
     */
    fun installFromUrl(url: String, callback: IPMObserver) {

    }

    /**
     * 卸载apk
     */
    fun uninstall(packageName: String) {
        try {
            weakContext?.get()?.packageManager?.getPackageInfo(packageName, 0)
        } catch (e: Exception) {
            observerList.forEach { it.onUninstallFailed(packageName, "Package not found!") }
            return
        }
        findNextInstaller(null)?.uninstall(packageName)
    }

    /**
     * 尝试查找下一个安装器
     */
    private fun findNextInstaller(pm: IPMInstaller?): IPMInstaller? {
        val context = weakContext?.get()
        val installer = if (context == null) {
            null
        } else {
            if (pm == null) {
                INSTALLER[0].get<IPMInstaller>(arrayOf(Context::class.java), context)
            } else {
                val index = INSTALLER.indexOf(pm.javaClass)
                if (index < INSTALLER.size - 1) {
                    INSTALLER[index + 1].get<IPMInstaller>(
                        arrayOf(Context::class.java),
                        context
                    )
                } else {
                    null
                }
            }
        }
        currentInstaller = installer
        return installer
    }

    fun addObserver(observer: IPMObserver) {
        observerList.add(observer)
    }

    fun removeObserver(observer: IPMObserver) {
        observerList.remove(observer)
    }

    protected fun finalize() {
        observerList.clear()
        LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG).removeObserver(this)
        weakContext?.get()?.unregisterReceiver(installActionReceiver)
    }

    private inner class InstallActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            intent ?: return
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                return
            }
            if (intent.action == PACKAGE_INSTALL_ACTION) {
                val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
                val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                val path = intent.getStringExtra(PackageInstaller.EXTRA_STORAGE_PATH)
                LiveEventBus.get<InstallerLog>(InstallerLog.Companion.LOG_TAG).post(
                    InstallerLog(
                        currentInstaller,
                        InstallerLog.Companion.TYPE_INSTALL,
                        status == PackageInstaller.STATUS_SUCCESS,
                        packageName = packageName ?: "",
                        message = message ?: "",
                        path = path ?: ""
                    )
                )
            }
        }
    }
}