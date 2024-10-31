package dokidoki.cloud.pm.config

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * 全局配置存取类
 */
@Suppress("UNCHECKED_CAST")
class GlobalValues {

    private val configManager: MMKV by lazy { MMKV.defaultMMKV() }

    companion object {

        const val CONFIG_AUTO_UPDATE = "config_auto_update"

        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { GlobalValues() }

        @JvmStatic
        fun sInstance() = instance

        fun init(context: Context) {
            MMKV.initialize(context)
        }
    }

    fun setConfig(key: String, value: Any) {
        when (value) {
            is Int -> configManager.encode(key, value)
            is Boolean -> configManager.encode(key, value)
            is Float -> configManager.encode(key, value)
            is Long -> configManager.encode(key, value)
            else -> configManager.encode(key, value.toString())
        }
    }

    fun <T> getConfig(key: String, defaultValue: Any): T {
        return when (defaultValue) {
            is Int -> configManager.decodeInt(key, defaultValue) as T
            is Boolean -> configManager.decodeBool(key, defaultValue) as T
            is Float -> configManager.decodeFloat(key, defaultValue) as T
            is Long -> configManager.decodeLong(key, defaultValue) as T
            else -> configManager.decodeString(key, defaultValue.toString()) as T
        }
    }

}