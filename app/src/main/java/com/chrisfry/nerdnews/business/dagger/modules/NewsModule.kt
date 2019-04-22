package com.chrisfry.nerdnews.business.dagger.modules

import com.chrisfry.nerdnews.business.network.INewsApi
import com.chrisfry.nerdnews.business.network.NewsApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NewsModule {

    @Provides
    @Singleton
    fun provideNewsService(): INewsApi {
        return NewsApi()
    }
}