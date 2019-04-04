package com.chrisfry.nerdnews.userinterface.adapters

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.chrisfry.nerdnews.business.InvalidPositionException
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.userinterface.fragments.NewsListFragment

class NewsPagerAdapter(fragmentManager: FragmentManager, private val context: Context) : FragmentPagerAdapter(fragmentManager) {
    companion object {
        private val TAG = NewsPagerAdapter::class.java.name
    }

    // List of fragments handled by this adapter (Tech, Science, Gaming)
    private val fragmentList = mutableListOf<NewsListFragment>()

    init {
        // Add a news list fragment for each article type to display (currently static number)
        for(articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            fragmentList.add(NewsListFragment())
        }
    }

    override fun getItem(position: Int): Fragment {

        if (position < 0 || position >= ArticleDisplayType.values().size) {
            throw InvalidPositionException("$TAG: Invalid position in getItem")
        } else {
            return fragmentList[position]
        }
    }

    override fun getCount(): Int {
        return ArticleDisplayType.values().size
    }

    /**
     * Updates the list of articles in the desired fragment (based on articleType)
     *
     * @param articleType: Article type of the list to be updated
     * @param articles: Updated article list
     */
    fun updateFragment(articleType: ArticleDisplayType, articles: List<Article>) {
        val articleTypeIndex = ArticleDisplayType.values().indexOf(articleType)

        if (articleTypeIndex < 0 || articleTypeIndex >= fragmentList.size){
            Log.e(TAG, "Error article type index is invalid. Something went very wrong")
        } else {
            fragmentList[articleTypeIndex].updateList(articles)
        }
    }

    /**
     * Refreshes the list of articles in the desired fragment (based on articleType)
     *
     * @param articleType: Article type of the list to be refreshed
     * @param articles: New article list
     */
    fun refreshFragment(articleType: ArticleDisplayType, articles: List<Article>) {
        val articleTypeIndex = ArticleDisplayType.values().indexOf(articleType)

        if (articleTypeIndex < 0 || articleTypeIndex >= fragmentList.size){
            Log.e(TAG, "Error article type index is invalid. Something went very wrong")
        } else {
            fragmentList[articleTypeIndex].refreshList(articles)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        if (position < 0 || position >= ArticleDisplayType.values().size) {
            throw InvalidPositionException("$TAG: Invalid position in getPageTitle")
        } else {
            return context.getString(ArticleDisplayType.values()[position].stringResourceId)
        }
    }
}