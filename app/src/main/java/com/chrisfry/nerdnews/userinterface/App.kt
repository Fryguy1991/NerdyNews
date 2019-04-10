package com.chrisfry.nerdnews.userinterface

import android.app.Application
import com.chrisfry.nerdnews.business.dagger.components.DaggerNewsComponent
import com.chrisfry.nerdnews.business.dagger.components.NewsComponent
import com.chrisfry.nerdnews.business.dagger.modules.NewsModule

class App : Application() {

    lateinit var newsComponent: NewsComponent

    override fun onCreate() {
        super.onCreate()

        newsComponent = DaggerNewsComponent.builder().newsModule(NewsModule()).build()
    }
}