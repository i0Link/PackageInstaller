@file:Suppress("UNCHECKED_CAST")

package dokidoki.cloud.pm.utils

import android.util.Log

/**
 * 反射类，主要用于SDK代码综合与编译区分
 */
@Suppress("UNCHECKED_CAST")
class Reflect private constructor() {
    companion object {

        private const val TAG = "Reflect"

        const val DEBUG = true

        fun classOf(className: String): Class<*> = try {
            Class.forName(className)
        } catch (e: Exception) {
            Log.e(TAG, "No class[$className] found -> [${e.javaClass.simpleName}] [${e.message}]")
            Any::class.java
        }
    }
}

fun <T> Class<*>?.get(classArray: Array<Class<*>>, vararg args: Any?): T? {
    return try {
        this?.getConstructor(*classArray)?.apply { isAccessible = true }?.newInstance(*args) as T?
    } catch (e: Exception) {
        try {
            this?.getDeclaredConstructor(*classArray)?.apply { isAccessible = true }
                ?.newInstance(*args) as T?
        } catch (e: Exception) {
            Log.e(
                "Reflect",
                "No default constructor found  -> [${e.javaClass.simpleName}] [${e.message}]",
                if (Reflect.DEBUG) e else null
            )
            null
        }
    }
}

fun <T> Class<*>?.getByMethod(
    methodName: String, classArray: Array<Class<*>>, vararg args: Any?
): T? {
    return try {
        this?.getMethod(methodName, *classArray)?.apply { isAccessible = true }
            ?.invoke(null, *args) as T?
    } catch (e: NoSuchMethodException) {
        try {
            this?.getDeclaredMethod(methodName, *classArray)?.apply { isAccessible = true }
                ?.invoke(null, *args) as T?
        } catch (e: NoSuchMethodException) {
            Log.e(
                "Reflect",
                "No method constructor [$methodName] found  -> [${e.javaClass.simpleName}] [${e.message}]",
                if (Reflect.DEBUG) e else null
            )
            null
        }
    }
}

fun <T> Class<*>?.callStaticMethod(
    methodName: String, classArray: Array<Class<*>>, vararg args: Any?
): T? {
    return try {
        this?.getMethod(methodName, *classArray)?.apply {
            isAccessible = true
        }?.invoke(null, *args) as T?
    } catch (e: NoSuchMethodException) {
        try {
            this?.getDeclaredMethod(methodName, *classArray)?.apply {
                isAccessible = true
            }?.invoke(null, *args) as T?
        } catch (e: NoSuchMethodException) {
            Log.e(
                "Reflect",
                "No direct method[$methodName] found  -> [${e.javaClass.simpleName}] [${e.message}]",
                if (Reflect.DEBUG) e else null
            )
            null
        }
    }
}

fun <T> Class<*>?.getStaticValue(name: String): T? {
    return try {
        this?.getField(name)?.apply { isAccessible = true }?.get(null) as T?
    } catch (e: NoSuchFileException) {
        try {
            this?.getDeclaredField(name)?.apply { isAccessible = true }?.get(null) as T?
        } catch (e: NoSuchFileException) {
            Log.e(
                "Reflect",
                "No field[$name] found  -> [${e.javaClass.simpleName}] [${e.message}]",
                if (Reflect.DEBUG) e else null
            )
            null
        }
    }
}

fun Class<*>?.setStaticValue(name: String, value: Any?) {
    try {
        this?.getField(name)?.apply { isAccessible = true }?.set(null, value)
    } catch (e: NoSuchFileException) {
        try {
            this?.getDeclaredField(name)?.apply { isAccessible = true }?.set(null, value)
        } catch (e: NoSuchFileException) {
            Log.e(
                "Reflect",
                "No field[$name] found  -> [${e.javaClass.simpleName}] [${e.message}]",
                if (Reflect.DEBUG) e else null
            )
        }
    }
}

fun <T> Any?.callMethod(
    methodName: String, classArray: Array<Class<*>>, vararg args: Any?
): T? {
    return try {
        this?.javaClass?.getMethod(methodName, *classArray)?.apply {
            isAccessible = true
        }?.invoke(this, *args) as T?
    } catch (e: NoSuchMethodException) {
        try {
            this?.javaClass?.getDeclaredMethod(methodName, *classArray)?.apply {
                isAccessible = true
            }?.invoke(this, *args) as T?
        } catch (e: NoSuchMethodException) {
            Log.e(
                "Reflect",
                "No method[$methodName] found  -> [${e.javaClass.simpleName}] [${e.message}]",
                if (Reflect.DEBUG) e else null
            )
            null
        }
    }
}

fun <T> Any?.getValue(name: String): T? {
    return try {
        this?.javaClass?.getField(name)?.apply { isAccessible = true }?.get(this) as T?
    } catch (e: NoSuchFileException) {
        try {
            this?.javaClass?.getDeclaredField(name)?.apply { isAccessible = true }?.get(this) as T?
        } catch (e: NoSuchFileException) {
            Log.e(
                "Reflect",
                "No field[$name] found  -> [${e.javaClass.simpleName}] [${e.message}]",
                if (Reflect.DEBUG) e else null
            )
            null
        }
    }
}

fun Any?.setValue(name: String, value: Any) {
    try {
        this?.javaClass?.getField(name)?.apply { isAccessible = true }?.set(this, value)
    } catch (e: NoSuchFileException) {
        try {
            this?.javaClass?.getDeclaredField(name)?.apply { isAccessible = true }?.set(this, value)
        } catch (e: NoSuchFileException) {
            Log.e(
                "Reflect",
                "No field[$name] found  -> [${e.javaClass.simpleName}] [${e.message}]",
                if (Reflect.DEBUG) e else null
            )
        }
    }
}