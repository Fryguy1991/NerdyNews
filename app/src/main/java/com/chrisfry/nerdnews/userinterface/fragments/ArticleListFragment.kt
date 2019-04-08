package com.chrisfry.nerdnews.userinterface.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.presenters.ArticleListPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleListPresenter
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.userinterface.adapters.ArticleRecyclerViewAdapter
import com.chrisfry.nerdnews.userinterface.widgets.LinearLayoutDecorator
import java.lang.Exception

class ArticleListFragment : Fragment(), ArticleListPresenter.IArticleListView {
    companion object {
        private val TAG = ArticleListFragment::class.java.name
        const val KEY_ARTICLE_TYPE = "key_article_type"
    }

    // Presenter reference
    private var presenter: IArticleListPresenter? = null

    // RecyclerView elements
    private lateinit var newsRecyclerView: RecyclerView
    private val articleAdapter = ArticleRecyclerViewAdapter(this)
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args == null) {
            throw Exception("$TAG: Error fragment was not provided an article type")
        } else {
            val typeOrdinal = args.getInt(KEY_ARTICLE_TYPE, -1)
            if (typeOrdinal < 0 || typeOrdinal >= ArticleDisplayType.values().size) {
                throw Exception("$TAG: Error invalid article type ordinal provided")
            } else {
                presenter = ArticleListPresenter.getInstance(ArticleDisplayType.values()[typeOrdinal])
                presenter?.attach(this)
            }
        }
    }

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
            newsRecyclerView.addItemDecoration(LinearLayoutDecorator())
        }
        presenter?.requestArticles()
    }

    override fun onDestroyView() {
        presenter?.detach()
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenter = null
        super.onDestroy()
    }

    override fun refreshArticles(articles: List<ArticleDisplayModel>) {
        articleAdapter.updateAdapter(articles)
        newsRecyclerView.scrollToPosition(0)
    }

    override fun updateArticleList(articles: List<ArticleDisplayModel>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun noMoreArticlesAvailable() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}