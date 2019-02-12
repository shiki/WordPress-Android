package org.wordpress.android.ui.posts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;
import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.store.PostStore;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.ui.ActivityId;
import org.wordpress.android.ui.RequestCodes;
import org.wordpress.android.ui.posts.BasicFragmentDialog.BasicDialogNegativeClickInterface;
import org.wordpress.android.ui.posts.BasicFragmentDialog.BasicDialogOnDismissByOutsideTouchInterface;
import org.wordpress.android.ui.posts.BasicFragmentDialog.BasicDialogPositiveClickInterface;
import org.wordpress.android.ui.posts.GutenbergWarningFragmentDialog.GutenbergWarningDialogClickInterface;
import org.wordpress.android.util.LocaleManager;

import javax.inject.Inject;

public class PostsListActivity extends AppCompatActivity implements BasicDialogPositiveClickInterface,
        BasicDialogNegativeClickInterface, BasicDialogOnDismissByOutsideTouchInterface,
        GutenbergWarningDialogClickInterface {
    public static final String EXTRA_TARGET_POST_LOCAL_ID = "targetPostLocalId";

    private SiteModel mSite;

    @Inject SiteStore mSiteStore;
    @Inject PostStore mPostStore;
    private PostsPagerAdapter mPostsPagerAdapter;
    private ViewPager mPager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WordPress) getApplication()).component().inject(this);

        setContentView(R.layout.post_list_activity);


        if (savedInstanceState == null) {
            mSite = (SiteModel) getIntent().getSerializableExtra(WordPress.SITE);
        } else {
            mSite = (SiteModel) savedInstanceState.getSerializable(WordPress.SITE);
        }

        setupActionBar();
        setupContent();
        handleIntent(getIntent());
    }

    private void setupContent() {
        mPager = findViewById(R.id.postPager);
        mPostsPagerAdapter = new PostsPagerAdapter(mSite, this, getSupportFragmentManager());
        mPager.setAdapter(mPostsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mPager);
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            setTitle(getString(R.string.my_site_btn_blog_posts));
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // TODO site has changed and postListActivity opened with a target post is not implemented
//        boolean siteHasChanged = false;
//        if (intent.hasExtra(WordPress.SITE)) {
//            SiteModel site = (SiteModel) intent.getSerializableExtra(WordPress.SITE);
//            if (mSite != null && site != null) {
//                siteHasChanged = site.getId() != mSite.getId();
//            }
//            mSite = site;
//        }
//
//        if (mSite == null) {
//            ToastUtils.showToast(this, R.string.blog_not_found, ToastUtils.Duration.SHORT);
//            finish();
//            return;
//        }
//        PostModel targetPost = null;
//        int targetPostId = intent.getIntExtra(EXTRA_TARGET_POST_LOCAL_ID, 0);
//        if (targetPostId > 0) {
//            targetPost = mPostStore.getPostByLocalPostId(intent.getIntExtra(EXTRA_TARGET_POST_LOCAL_ID, 0));
//            if (targetPost == null) {
//                String errorMessage = getString(R.string.error_post_does_not_exist);
//                ToastUtils.showToast(this, errorMessage);
//            }
//        }
//
//        mPostList = (PostListFragment) getSupportFragmentManager().findFragmentByTag(PostListFragment.TAG);
//        if (mPostList == null || siteHasChanged || targetPost != null) {
//            PostListFragment oldFragment = mPostList;
//            mPostList = PostListFragment.newInstance(mSite, targetPost);
//            if (oldFragment == null) {
//                getSupportFragmentManager().beginTransaction()
//                                           .add(R.id.post_list_container, mPostList, PostListFragment.TAG)
//                                           .commit();
//            } else {
//                getSupportFragmentManager().beginTransaction()
//                                           .replace(R.id.post_list_container, mPostList, PostListFragment.TAG)
//                                           .commit();
//            }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityId.trackLastActivity(ActivityId.POSTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.EDIT_POST) {
            mPostsPagerAdapter.getItem(mPager.getCurrentItem()).handleEditPostResult(resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(WordPress.SITE, mSite);
    }

    // BasicDialogFragment Callbacks

    @Override
    public void onPositiveClicked(@NotNull String instanceTag) {
        mPostsPagerAdapter.getItem(mPager.getCurrentItem()).onPositiveClickedForBasicDialog(instanceTag);
    }

    @Override
    public void onNegativeClicked(@NotNull String instanceTag) {
        mPostsPagerAdapter.getItem(mPager.getCurrentItem()).onNegativeClickedForBasicDialog(instanceTag);
    }

    @Override
    public void onDismissByOutsideTouch(@NotNull String instanceTag) {
        mPostsPagerAdapter.getItem(mPager.getCurrentItem()).onDismissByOutsideTouchForBasicDialog(instanceTag);
    }

// GutenbergWarningDialogClickInterface Callbacks

    @Override
    public void onGutenbergWarningDialogEditPostClicked(long gutenbergRemotePostId) {
        mPostsPagerAdapter.getItem(mPager.getCurrentItem())
                          .onGutenbergWarningDialogEditPostClicked(gutenbergRemotePostId);
    }

    @Override
    public void onGutenbergWarningDialogCancelClicked(long gutenbergRemotePostId) {
        mPostsPagerAdapter.getItem(mPager.getCurrentItem())
                          .onGutenbergWarningDialogCancelClicked(gutenbergRemotePostId);
    }

    @Override
    public void onGutenbergWarningDialogLearnMoreLinkClicked(long gutenbergRemotePostId) {
        mPostsPagerAdapter.getItem(mPager.getCurrentItem())
                          .onGutenbergWarningDialogLearnMoreLinkClicked(gutenbergRemotePostId);
    }

    @Override
    public void onGutenbergWarningDialogDontShowAgainClicked(long gutenbergRemotePostId, boolean checked) {
        mPostsPagerAdapter.getItem(mPager.getCurrentItem())
                          .onGutenbergWarningDialogDontShowAgainClicked(gutenbergRemotePostId, checked);
    }
}
