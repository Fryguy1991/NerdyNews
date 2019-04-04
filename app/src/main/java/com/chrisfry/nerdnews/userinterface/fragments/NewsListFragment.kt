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
import com.chrisfry.nerdnews.userinterface.adapters.ArticleRecyclerViewAdapter
import com.chrisfry.nerdnews.userinterface.widgets.LinearLayoutDecorator

class NewsListFragment : Fragment(){
    companion object {
        private val TAG = NewsListFragment::class.java.name
    }

    // RecyclerView elements
    private lateinit var newsRecyclerView: RecyclerView
    private val articleAdapter = ArticleRecyclerViewAdapter(this)
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        newsRecyclerView = view.findViewById(R.id.recycler_view_news_list)
        newsRecyclerView.adapter = articleAdapter

        val currentContext = context
        if (currentContext != null) {
            linearLayoutManager = LinearLayoutManager(currentContext, RecyclerView.VERTICAL, false)
            newsRecyclerView.layoutManager = linearLayoutManager
            newsRecyclerView.addItemDecoration(LinearLayoutDecorator(currentContext))
        }
    }

    fun refreshList(articles: List<Article>) {
        articleAdapter.updateAdapter(articles)
    }

    fun updateList(articles: List<Article>) {
        // TODO: update recyclerview adapter (maintain list position)
    }
}