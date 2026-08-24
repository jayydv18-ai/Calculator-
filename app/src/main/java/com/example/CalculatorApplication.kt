package com.example

import android.app.Application
import com.example.ads.AdsManager
import com.example.data.SettingsRepository
import com.example.data.db.AppDatabase

class CalculatorApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        settingsRepository = SettingsRepository(this)
        AdsManager.initialize(this)
    }
}
