package com.dergoogler.mmrl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.datastore.WorkingMode
import com.topjohnwu.superuser.Shell
import dev.dergoogler.mmrl.compat.ServiceManagerCompat
import dev.dergoogler.mmrl.compat.stub.IFileManager
import dev.dergoogler.mmrl.compat.stub.IKsuService
import dev.dergoogler.mmrl.compat.stub.IModuleManager
import dev.dergoogler.mmrl.compat.stub.IServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

object Compat {
    private var mServiceOrNull: IServiceManager? = null
    private val mService
        get() = checkNotNull(mServiceOrNull) {
            "IServiceManager haven't been received"
        }

    var isAlive by mutableStateOf(false)
        private set

    private val _isAliveFlow = MutableStateFlow(false)
    val isAliveFlow get() = _isAliveFlow.asStateFlow()

    suspend fun init(mode: WorkingMode) = when {
        isAlive -> true
        else -> try {
            mServiceOrNull = when (mode) {
                WorkingMode.MODE_MAGISK,
                WorkingMode.MODE_KERNEL_SU,
                WorkingMode.MODE_KERNEL_SU_NEXT,
                WorkingMode.MODE_APATCH,
                -> ServiceManagerCompat.fromLibSu(mode)

                WorkingMode.MODE_SHIZUKU -> ServiceManagerCompat.fromShizuku()
                else -> null
            }

            state()
        } catch (e: Exception) {
            Timber.e(e)

            mServiceOrNull = null
            state()
        }
    }

    val moduleManager: IModuleManager get() = mService.moduleManager
    val fileManager: IFileManager get() = mService.fileManager
    val ksuService: IKsuService get() = mService.ksuService

    val platform: Platform
        get() = if (mServiceOrNull != null) Platform(mService.currentPlatform()) else Platform(
            ""
        )

    private fun state(): Boolean {
        isAlive = mServiceOrNull != null
        _isAliveFlow.value = isAlive

        return isAlive
    }

    fun <T> get(fallback: T, block: Compat.() -> T): T {
        return when {
            isAlive -> block(this)
            else -> fallback
        }
    }

    val ServiceShell: Shell = createRootShell(commands = arrayOf("sh"))
//    val RootShell: Shell = createRootShell(commands = arrayOf("su"))

    inline fun <T> withNewRootShell(
        globalMnt: Boolean = false,
        devMode: Boolean = BuildConfig.IS_DEV_VERSION,
        commands: Array<String> = arrayOf("su"),
        block: Shell.() -> T,
    ): T {
        return createRootShell(globalMnt, devMode, commands).use(block)
    }

    fun createRootShell(
        globalMnt: Boolean = false,
        devMode: Boolean = BuildConfig.IS_DEV_VERSION,
        commands: Array<String> = arrayOf("su"),
    ): Shell {
        Shell.enableVerboseLogging = devMode
        val builder = Shell.Builder.create()
        if (globalMnt) {
            builder.setFlags(Shell.FLAG_MOUNT_MASTER)
        }
        return builder.build(*commands)
    }
}