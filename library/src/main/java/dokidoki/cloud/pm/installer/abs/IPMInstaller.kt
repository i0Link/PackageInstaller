package dokidoki.cloud.pm.installer.abs

import android.content.Context
import java.lang.ref.WeakReference

/**
 * 抽象PackageManager管理器，提供安装卸载等操作
 * 安装方式：反射静默安装、Root权限安装、传统安装
 */
abstract class IPMInstaller(context: Context) {

    internal val TAG = javaClass.simpleName

    internal val weakContext: WeakReference<Context> = WeakReference(context)

    abstract fun install(filePath: String)

    abstract fun uninstall(packageName: String)

    abstract fun reset(packageName: String)
}