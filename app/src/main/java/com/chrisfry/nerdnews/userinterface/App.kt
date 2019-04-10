package com.chrisfry.nerdnews.userinterface

import android.app.Application
import com.chrisfry.nerdnews.business.dagger.components.AppComponent
import com.chrisfry.nerdnews.business.dagger.components.DaggerAppComponent
import com.chrisfry.nerdnews.business.dagger.modules.ArticleModelModule
import com.chrisfry.nerdnews.business.dagger.modules.NewsModule

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .newsModule(NewsModule())
            .articleModelModule(ArticleModelModule())
            .build()
    }
}