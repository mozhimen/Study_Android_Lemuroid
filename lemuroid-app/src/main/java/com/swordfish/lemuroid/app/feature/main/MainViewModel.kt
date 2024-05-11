package com.swordfish.lemuroid.app.feature.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.feature.library.LibraryIndexMonitor

class MainViewModel(appContext: Context) : ViewModel() {

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(appContext) as T
        }
    }

    val indexingInProgress = LibraryIndexMonitor(appContext).getLiveData()
}
