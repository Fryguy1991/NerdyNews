package com.chrisfry.nerdnews.userinterface.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.model.Article

class NewsListFragment : Fragment(){
    companion object {
        private val TAG = this::class.java.name
    }

    private lateinit var newsRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        newsRecyclerView = view.findViewById(R.id.recycler_view_news_list)
        // TODO: Add adapter for recycler view

        val currentContext = context
        if (currentContext != null) {
            newsRecyclerView.layoutManager = LinearLayoutManager(currentContext)
        }
    }

    fun refreshList(articles: List<Article>) {
        // TODO: Refresh recyclerview adapter
    }

    fun updateList(articles: List<Article>) {
        // TODO: update recyclerview adapter (maintain list position)
    }
}