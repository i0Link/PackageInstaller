package dokidoki.cloud.pm.installer

import dokidoki.cloud.pm.installer.abs.IPMInstaller

/**
 * 安装事件，用于LiveEvent通信，附带安装信息
 */
class InstallerLog(
    val installer: IPMInstaller?,
    val type: Int,
    val success: Boolean,
    val message: String = "",
    val path: String = "",
    val packageName: String = "",
) {
    companion object {

        const val LOG_TAG = "InstallerLog"

        const val TYPE_INSTALL = 0x01
        const val TYPE_UNINSTALL = 0x02
    }

    override fun toString(): String {
        return "InstallerLog(installer=${installer?.javaClass?.simpleName}, type=$type, success=$success, message='$message', path='$path', packageName='$packageName')"
    }
}
