package com.chrisfry.nerdnews.userinterface

import android.app.Application
import com.chrisfry.nerdnews.business.dagger.components.DaggerNewsComponent
import com.chrisfry.nerdnews.business.dagger.components.DaggerPresenterComponent
import com.chrisfry.nerdnews.business.dagger.components.NewsComponent
import com.chrisfry.nerdnews.business.dagger.components.PresenterComponent
import com.chrisfry.nerdnews.business.dagger.modules.NewsModule
import com.chrisfry.nerdnews.business.dagger.modules.PresenterModule
import com.chrisfry.nerdnews.business.presenters.ArticleListPresenter

class App : Application() {

    private lateinit var newsComponent: NewsComponent
    lateinit var presenterComponent: PresenterComponent

    override fun onCreate() {
        super.onCreate()

        newsComponent = DaggerNewsComponent.builder().newsModule(NewsModule()).build()
        presenterComponent = DaggerPresenterComponent.builder().presenterModule(PresenterModule()).build()

        // Instantiate article list presenters
        ArticleListPresenter.createPresenters(newsComponent)
    }
}