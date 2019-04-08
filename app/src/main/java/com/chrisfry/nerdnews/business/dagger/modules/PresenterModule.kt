package com.chrisfry.nerdnews.business.dagger.modules

import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class PresenterModule {
    @Provides
    @Singleton
    fun providesNewsListPresenter(): INewsPagingPresenter {
        return NewsPagingPresenter.getInstance()
    }
}