package dokidoki.cloud.pm.installer.abs

/**
 * 安装结果观察者
 * 无论是否是当前对象调用安装命令，都会分发给此对象
 */
interface IPMObserver {
    fun onInstallSuccess(packageName: String) {}

    fun onInstallFailed(filePath: String, error: String) {}

    fun onUninstallSuccess(packageName: String) {}

    fun onUninstallFailed(packageName: String, error: String) {}
}