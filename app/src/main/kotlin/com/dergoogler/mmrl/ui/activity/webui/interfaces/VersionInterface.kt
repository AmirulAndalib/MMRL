package com.dergoogler.mmrl.ui.activity.webui.interfaces

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.ext.managerVersion
import com.dergoogler.mmrl.webui.util.WebUIOptions

class VersionInterface(
    private val context: Context,
    private val webView: WebView,
    private val options: WebUIOptions,
) {
    inner class BuildConfigDetails {
        @get:JavascriptInterface
        val applicationId = BuildConfig.APPLICATION_ID

        @get:JavascriptInterface
        val buildType = BuildConfig.BUILD_TYPE

        @get:JavascriptInterface
        val versionCode = context.managerVersion.second

        @get:JavascriptInterface
        val versionName = context.managerVersion.first

        @get:JavascriptInterface
        val isDevVersion: Boolean = BuildConfig.IS_DEV_VERSION

        @get:JavascriptInterface
        val isGooglePlayBuild: Boolean = BuildConfig.IS_GOOGLE_PLAY_BUILD
    }

    inner class RootConfigDetails {
        @get:JavascriptInterface
        val platform = options.platform.current

        @get:JavascriptInterface
        val versionName = options.versionName

        @get:JavascriptInterface
        val versionCode = options.versionCode
    }

    @get:JavascriptInterface
    val buildConfig get() = BuildConfigDetails()

    @get:JavascriptInterface
    val rootConfig get() = RootConfigDetails()

    inner class ToastBuilderInterface {
        private val toast = Toast(context)

        @JavascriptInterface
        fun setText(msg: String) {
            toast.setText(msg)
        }

        @JavascriptInterface
        fun show() {
            webView.post {
                toast.show()
            }
        }

        @JavascriptInterface
        fun cancel() {
            webView.post {
                toast.cancel()
            }
        }

        @JavascriptInterface
        fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
            toast.setGravity(gravity, xOffset, yOffset)
        }

        @JavascriptInterface
        fun setDuration(duration: Int) {
            toast.duration = duration
        }
    }

    @JavascriptInterface
    fun toastBuilder(): ToastBuilderInterface {
        return ToastBuilderInterface()
    }
}