package com.chrisfry.nerdnews.userinterface.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.presenters.ArticleListPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleListPresenter
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.model.ArticleDisplayModelParcelable
import com.chrisfry.nerdnews.userinterface.App
import com.chrisfry.nerdnews.userinterface.adapters.ArticleRecyclerViewAdapter
import com.chrisfry.nerdnews.userinterface.interfaces.ArticleSelectionListener
import com.chrisfry.nerdnews.userinterface.widgets.GridLayoutDecorator
import com.chrisfry.nerdnews.userinterface.widgets.LinearLayoutDecorator
import com.chrisfry.nerdnews.utils.LogUtils
import java.lang.Exception

class ArticleListFragment : Fragment(), ArticleListPresenter.IArticleListView, ArticleSelectionListener{
    companion object {
        private val TAG = ArticleListFragment::class.java.name
        const val KEY_ARTICLE_TYPE = "key_article_type"
    }

    // Presenter reference
    private var presenter: IArticleListPresenter? = null

    // RecyclerView elements
    private lateinit var newsRecyclerView: RecyclerView
    private val articleAdapter = ArticleRecyclerViewAdapter(this)
    private lateinit var layoutManager: RecyclerView.LayoutManager

    // Article type displayed by this fragment
    private lateinit var articleType: ArticleDisplayType

    // Reference to object that will listen to recycler view scrolling
    inner class NewsScrollListener: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val manager = recyclerView.layoutManager
            when (manager) {
                // If the layout manager says we're at the last item to display request more articles
                is LinearLayoutManager -> {
                    if (manager.findLastCompletelyVisibleItemPosition() == manager.itemCount - 1) {
                        presenter?.requestMoreArticles()
                    }
                }
                is GridLayoutManager -> {
                    if (manager.findLastCompletelyVisibleItemPosition() == manager.itemCount - 1) {
                        presenter?.requestMoreArticles()
                    }
                }
                else -> {
                    LogUtils.error(TAG, "Invalid layout manager")
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            // Not currently handling state change
        }
    }

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
                articleType = ArticleDisplayType.values()[typeOrdinal]
                val newPresenter = ArticleListPresenter.getInstance(articleType)
                val parentActivity = activity
                if (parentActivity != null) {
                    (parentActivity.application as App).appComponent.inject(newPresenter)
                }
                presenter = newPresenter
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initiate UI elements
        newsRecyclerView = view.findViewById(R.id.recycler_view_news_list)

        // Set adapter to recycler view
        articleAdapter.listener = this
        newsRecyclerView.adapter = articleAdapter

        // Add listener for scroll events
        newsRecyclerView.addOnScrollListener(NewsScrollListener())
        // Set contentDescription (used for testing)
        newsRecyclerView.contentDescription = "${articleType}_recycler_view"

        val currentContext = context
        if (currentContext != null) {
            val display = (currentContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            // Use linear list view for portrait mode and grid view for landscape mode
            if (display != null) {
                when (display.rotation) {
                    Surface.ROTATION_0,
                    Surface.ROTATION_180 -> {
                        setupLinearRecyclerView(currentContext)
                    }
                    Surface.ROTATION_90,
                    Surface.ROTATION_270 -> {
                        setupGridRecyclerView(currentContext)
                    }
                }
            } else {
                // If rotation can't be determined, default to linear list view
                setupLinearRecyclerView(currentContext)
            }
        }
        presenter?.attach(this)
        presenter?.requestArticles()
    }

    private fun setupLinearRecyclerView(currentContext: Context) {
        layoutManager = LinearLayoutManager(currentContext, RecyclerView.VERTICAL, false)
        newsRecyclerView.layoutManager = layoutManager
        newsRecyclerView.addItemDecoration(LinearLayoutDecorator())
    }

    private fun setupGridRecyclerView(currentContext: Context) {
        layoutManager = GridLayoutManager(currentContext, AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT)
        newsRecyclerView.layoutManager = layoutManager
        newsRecyclerView.addItemDecoration(GridLayoutDecorator())
    }

    override fun onDestroyView() {
        presenter?.detach()
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenter = null
        super.onDestroy()
    }

    override fun displayArticles(articles: List<ArticleDisplayModel>) {
        articleAdapter.updateAdapter(articles)
    }

    override fun displayNoArticles() {
        articleAdapter.updateAdapter(listOf())
    }

    override fun noMoreArticlesAvailable() {
        val currentContext = context
        if (currentContext != null) {
            Toast.makeText(currentContext, R.string.toast_no_more_articles_available, Toast.LENGTH_LONG).show()
        }
    }

    override fun onArticleSelected(article: ArticleDisplayModel) {
        LogUtils.debug(TAG, "Article selected with title: \"${article.title}\"")

        // Launch article item fragment to display selected article
        val itemFragment = ArticleItemFragment()
        val args = Bundle()
        args.putParcelable(AppConstants.KEY_ARGS_ARTICLE, ArticleDisplayModelParcelable(
            article.title,
            article.sourceName,
            article.imageUrl,
            article.author,
            article.articleUrl,
            article.articleContent,
            article.publishedAt
        ))
        itemFragment.arguments = args

        val parentActivity = activity
        if (parentActivity != null) {
            val transaction = parentActivity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frag_placeholder, itemFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}