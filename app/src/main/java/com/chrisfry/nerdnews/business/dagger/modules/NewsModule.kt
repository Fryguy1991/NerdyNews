package com.chrisfry.nerdnews.business.dagger.modules

import com.chrisfry.nerdnews.business.network.NewsApi
import com.chrisfry.nerdnews.business.network.NewsService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NewsModule {

    @Provides
    @Singleton
    fun provideNewsService(): NewsService {
        return NewsApi().getService()
    }
}