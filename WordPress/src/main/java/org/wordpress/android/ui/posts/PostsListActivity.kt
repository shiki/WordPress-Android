package org.wordpress.android.ui.posts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.post.PostStatus
import org.wordpress.android.fluxc.store.PostStore
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.ui.ActivityId
import org.wordpress.android.ui.RequestCodes
import org.wordpress.android.ui.posts.PostListType.DRAFTS
import org.wordpress.android.ui.posts.PostListType.PUBLISHED
import org.wordpress.android.ui.posts.PostListType.SCHEDULED
import org.wordpress.android.ui.posts.PostListType.TRASHED
import org.wordpress.android.ui.uploads.UploadUtils
import org.wordpress.android.util.LocaleManager
import org.wordpress.android.util.ToastUtils
import javax.inject.Inject

val postListTypes: List<PostListType> = listOf(PUBLISHED, DRAFTS, SCHEDULED, TRASHED)

class PostsListActivity : AppCompatActivity() {
    private lateinit var mSite: SiteModel

    @Inject lateinit var mSiteStore: SiteStore
    @Inject lateinit var mPostStore: PostStore
    @Inject lateinit var dispatcher: Dispatcher

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordPress).component().inject(this)

        setContentView(R.layout.post_list_activity)

        mSite = if (savedInstanceState == null) {
            intent.getSerializableExtra(WordPress.SITE) as SiteModel
        } else {
            savedInstanceState.getSerializable(WordPress.SITE) as SiteModel
        }
        val pager = findViewById<ViewPager>(R.id.post_list_pager)
        pager.adapter = PostListViewPagerAdapter(mSite, this, supportFragmentManager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        tabLayout.setupWithViewPager(pager)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.title = getString(R.string.my_site_btn_blog_posts)
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    public override fun onResume() {
        super.onResume()
        ActivityId.trackLastActivity(ActivityId.POSTS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCodes.EDIT_POST) {
            if (resultCode != Activity.RESULT_OK || data == null) {
                return
            }

            val localId = data.getIntExtra(EditPostActivity.EXTRA_POST_LOCAL_ID, 0)
            val post = mPostStore.getPostByLocalPostId(localId)

            if (post == null) {
                if (!data.getBooleanExtra(EditPostActivity.EXTRA_IS_DISCARDABLE, false)) {
                    ToastUtils.showToast(this, R.string.post_not_found, ToastUtils.Duration.LONG)
                }
                return
            }

            UploadUtils.handleEditPostResultSnackbars(this, findViewById(R.id.coordinator), data, post, mSite) {
                UploadUtils.publishPost(this, post, mSite, dispatcher)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(WordPress.SITE, mSite)
    }

    companion object {
        const val EXTRA_TARGET_POST_LOCAL_ID = "targetPostLocalId"
    }
}

class PostListViewPagerAdapter(
    private val site: SiteModel,
    private val context: Context,
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int = postListTypes.size

    override fun getItem(position: Int): Fragment {
        return PostListFragment.newInstance(site, postListTypes[position], null)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(postListTypes[position].title)
    }
}

enum class PostListType(val postStatusList: List<PostStatus>) {
    PUBLISHED(listOf(PostStatus.PUBLISHED, PostStatus.PRIVATE)),
    DRAFTS(listOf(PostStatus.DRAFT, PostStatus.PENDING)),
    SCHEDULED(listOf(PostStatus.SCHEDULED)),
    TRASHED(listOf(PostStatus.TRASHED));

    val title: Int
        get() = when (this) {
            PUBLISHED -> R.string.pages_published
            DRAFTS -> R.string.pages_drafts
            SCHEDULED -> R.string.pages_scheduled
            TRASHED -> R.string.pages_trashed
        }
}
