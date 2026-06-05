package com.example

import android.app.Application
import com.example.di.ServiceLocator
import com.google.firebase.FirebaseApp

class GustoMeetsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        ServiceLocator.init(this)
    }
}
