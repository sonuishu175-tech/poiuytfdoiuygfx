package com.example

import android.app.Application
import com.example.data.NotificationHelper
import com.google.android.gms.ads.MobileAds

class AiAssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        MobileAds.initialize(this)
    }
}
