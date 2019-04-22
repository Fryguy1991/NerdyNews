package com.chrisfry.nerdnews.userinterface.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.chrisfry.nerdnews.business.exceptions.InvalidPositionException
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.userinterface.fragments.ArticleListFragment

class NewsPagerAdapter(fragmentManager: FragmentManager, private val context: Context) : FragmentPagerAdapter(fragmentManager) {
    companion object {
        private val TAG = NewsPagerAdapter::class.java.simpleName
    }

    // List of fragments handled by this adapter (Tech, Science, Gaming)
    private val fragmentList = mutableListOf<ArticleListFragment>()

    init {
        // Add a news list fragment for each article type to display (currently static number)
        for(articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            fragmentList.add(ArticleListFragment.getInstance(articleType))
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

    override fun getPageTitle(position: Int): CharSequence? {
        if (position < 0 || position >= ArticleDisplayType.values().size) {
            throw InvalidPositionException("$TAG: Invalid position in getPageTitle")
        } else {
            return context.getString(ArticleDisplayType.values()[position].stringResourceId)
        }
    }
}