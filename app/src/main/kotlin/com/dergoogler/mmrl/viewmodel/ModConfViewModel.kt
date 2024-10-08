package com.dergoogler.mmrl.viewmodel

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Recomposer
import androidx.lifecycle.ViewModel
import com.dergoogler.mmrl.Compat
import dagger.hilt.android.lifecycle.HiltViewModel
import dalvik.system.DexClassLoader
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ModConfViewModel @Inject constructor(
) : ViewModel() {
    val isProviderAlive get() = Compat.isAlive
    private lateinit var pluginClass: Class<*>

    val versionName: String
        get() = Compat.get("") {
            with(moduleManager) { version }
        }

    val versionCode
        get() = Compat.get("") {
            with(moduleManager) { versionCode }
        }

    val managerName: String
        get() = Compat.get("") {
            with(moduleManager) { managerName }
        }

    fun loadComposablePlugin(
        context: Context,
        isDebug: Boolean,
        id: String,
        fixedModId: String,
        composer: Composer,
        hash: Int
    ): Composable? {
        return try {
            val optimizedDir = File(context.cacheDir, "dex_optimized").apply { mkdirs() }

            val dexFilePath = if (isDebug) {
                File(context.filesDir, "$fixedModId.dex")
            } else {
                if (Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()) {
                    File("/system/lib64", "$fixedModId.dex")
                } else {
                    File("/system/lib", "$fixedModId.dex")
                }
            }

            Timber.d("Dex file path: ${dexFilePath.path}")

            try {
                dexFilePath.setReadOnly()
            } catch (e: Exception) {
                Timber.e("Unable to set readonly to ${dexFilePath.path}")
            }

            val classLoader = DexClassLoader(
                dexFilePath.path, optimizedDir.absolutePath, null, context.classLoader
            )

            val pluginClassName = "com.dergoogler.modconf.$fixedModId.ModConfScreenKt"
            pluginClass = classLoader.loadClass(pluginClassName)

            setField("isProviderAlive", isProviderAlive)
            setField("managerName", managerName)
            setField("versionName", versionName)
            setField("versionCode", versionCode)
            setField("modId", id)
            setField("fixedModId", fixedModId)

            val pluginInstance = pluginClass.getDeclaredMethod(
                "ModConfScreen", Composer::class.java, Int::class.java
            )

            pluginInstance.isAccessible = true

            pluginInstance.invoke(null, composer, hash) as? Composable
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setField(fieldName: String, value: Any) {
        try {
            val field = pluginClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(null, value)
        } catch (e: Exception) {
            Timber.e("Failed to set field $fieldName")
        }
    }


}
