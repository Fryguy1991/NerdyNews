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
import com.chrisfry.nerdnews.userinterface.App
import com.chrisfry.nerdnews.userinterface.adapters.ArticleRecyclerViewAdapter
import com.chrisfry.nerdnews.userinterface.interfaces.ArticleSelectionListener
import com.chrisfry.nerdnews.userinterface.interfaces.IMainActivity
import com.chrisfry.nerdnews.userinterface.widgets.GridLayoutDecorator
import com.chrisfry.nerdnews.userinterface.widgets.LinearLayoutDecorator
import com.chrisfry.nerdnews.utils.LogUtils
import kotlinx.android.synthetic.main.fragment_news_list.*
import java.lang.Exception

class ArticleListFragment : Fragment(), ArticleListPresenter.IArticleListView, ArticleSelectionListener {
    companion object {
        private val TAG = ArticleListFragment::class.java.simpleName
        const val KEY_ARTICLE_TYPE = "key_article_type"

        /**
         * Method for creating an instance of ArticleListFragment
         *
         * @param articleType: The article type that the fragment will display
         */
        fun getInstance(articleType: ArticleDisplayType): ArticleListFragment {
            // Add article type as an argument (used for presenter retrieval)
            val fragment = ArticleListFragment()
            val args = Bundle()
            args.putInt(ArticleListFragment.KEY_ARTICLE_TYPE, articleType.ordinal)
            fragment.arguments = args

            return fragment
        }
    }

    // Presenter reference
    private var presenter: IArticleListPresenter? = null

    // RecyclerView elements
    private val articleAdapter = ArticleRecyclerViewAdapter(this)
    private lateinit var layoutManager: RecyclerView.LayoutManager

    // Article type displayed by this fragment
    private lateinit var articleType: ArticleDisplayType
    // Reference to communicate with the activity
    private var mainActivity: IMainActivity? = null

    // Reference to object that will listen to recycler view scrolling
    inner class NewsScrollListener : RecyclerView.OnScrollListener() {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentActivity = activity
        if (parentActivity == null || parentActivity !is IMainActivity) {
            throw Exception("Error invalid activity provided")
        } else {
            mainActivity = parentActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args == null) {
            // This should not be possible as our getInstance method requires an article type
            throw Exception("$TAG: Error fragment was not provided an article type")
        } else {
            // Create presenter and inject dependencies
            val typeOrdinal = args.getInt(KEY_ARTICLE_TYPE, -1)
            articleType = ArticleDisplayType.values()[typeOrdinal]
            val newPresenter = ArticleListPresenter(articleType)
            val parentActivity = activity
            if (parentActivity != null) {
                (parentActivity.application as App).appComponent.inject(newPresenter)
            }
            presenter = newPresenter
            presenter?.postDependencyInitiation()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set adapter to recycler view
        articleAdapter.listener = this
        recycler_view_news_list.adapter = articleAdapter

        // Add listener for scroll events
        recycler_view_news_list.addOnScrollListener(NewsScrollListener())
        // Set contentDescription (used for testing)
        recycler_view_news_list.contentDescription = "${articleType}_recycler_view"

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
        recycler_view_news_list.layoutManager = layoutManager
        recycler_view_news_list.addItemDecoration(LinearLayoutDecorator())
    }

    private fun setupGridRecyclerView(currentContext: Context) {
        layoutManager = GridLayoutManager(currentContext, AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT)
        recycler_view_news_list.layoutManager = layoutManager
        recycler_view_news_list.addItemDecoration(GridLayoutDecorator())
    }

    override fun onDestroyView() {
        presenter?.detach()
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenter?.breakDown()
        presenter = null
        mainActivity = null
        super.onDestroy()
    }

    override fun displayArticles(articles: List<ArticleDisplayModel>) {
        articleAdapter.updateAdapter(articles)

        recycler_view_news_list.visibility = View.VISIBLE
        tv_no_articles_message.visibility = View.GONE
    }

    override fun displayNoArticles() {
        recycler_view_news_list.visibility = View.GONE
        tv_no_articles_message.visibility = View.VISIBLE
    }

    override fun noMoreArticlesAvailable() {
        val currentContext = context
        if (currentContext != null) {
            Toast.makeText(currentContext, R.string.toast_no_more_articles_available, Toast.LENGTH_LONG).show()
        }
    }

    override fun onArticleSelected(article: ArticleDisplayModel) {
        LogUtils.debug(TAG, "Article selected with title: \"${article.title}\"")

        mainActivity?.navigateToArticle(article)
    }
}