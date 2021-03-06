package com.bugsnag.android

import com.bugsnag.android.ndk.NativeBridge

internal class NdkPlugin : BugsnagPlugin {

    companion object {
        init {
            System.loadLibrary("bugsnag-ndk")
        }
    }

    override var loaded = false

    private external fun enableCrashReporting()
    private external fun disableCrashReporting()

    private var nativeBridge: NativeBridge? = null

    override fun loadPlugin(client: Client) {
        if (nativeBridge == null) {
            nativeBridge = NativeBridge()
            client.addObserver(nativeBridge)
            client.sendNativeSetupNotification()
        }
        enableCrashReporting()
        Logger.info("Initialised NDK Plugin")
    }

    override fun unloadPlugin() = disableCrashReporting()
}
