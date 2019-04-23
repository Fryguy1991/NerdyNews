package com.chrisfry.nerdnews.userinterface

import android.app.Application
import com.chrisfry.nerdnews.business.dagger.components.AppComponent
import com.chrisfry.nerdnews.business.dagger.components.DaggerAppComponent
import com.chrisfry.nerdnews.business.dagger.modules.ArticleModelModule
import com.chrisfry.nerdnews.business.dagger.modules.EventModule
import com.chrisfry.nerdnews.business.dagger.modules.NewsModule

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        val articleModelModule = ArticleModelModule()
        // TODO: Should look into replace event bus pattern (Look into using RxJava)
        val eventModule = EventModule()
        val newsModule = NewsModule(eventModule.provideEventBus())

        appComponent = DaggerAppComponent.builder()
            .newsModule(newsModule)
            .articleModelModule(articleModelModule)
            .eventModule(eventModule)
            .build()
    }
}