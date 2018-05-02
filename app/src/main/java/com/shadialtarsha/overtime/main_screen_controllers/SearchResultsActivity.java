package com.shadialtarsha.overtime.main_screen_controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.google.firebase.database.Query;

public class SearchResultsActivity extends AppCompatActivity {

    private static final String SEARCH_QUERY_EXTRA = "com.shadialtarsha.overtime.main_screen_controllers.SearchResultsActivity.search_query";

    private Toolbar mSearchResultsToolbar;
    private RecyclerView mSearchResultsRecyclerView;
    private NestedScrollView mSearchResultsEmptyLayout;
    private LinearLayoutManager mRecyclerViewLayoutManager;

    private Query mQuery;
    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private UsersAdapter mUsersAdapter;
    private String mSearchQuery;

    public static Intent newIntent(Context packageContext, String query) {
        Intent intent = new Intent(packageContext, SearchResultsActivity.class);
        intent.putExtra(SEARCH_QUERY_EXTRA, query);
        return intent;
    }

    private void setSearchQuery() {
        mSearchQuery = getIntent().getStringExtra(SEARCH_QUERY_EXTRA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        mSearchResultsToolbar = (Toolbar) findViewById(R.id.search_results_toolbar);
        mSearchResultsToolbar.setTitle(getString(R.string.search_results_activity));
        setSupportActionBar(mSearchResultsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSearchQuery();

        mSearchResultsRecyclerView = (RecyclerView) findViewById(R.id.search_results_recycler_view);
        setUpSearchResultsRecyclerView();
        setUpSearchResultsRecyclerView();
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mQuery = mFirebaseDatabaseOperations.getUsersNodeReference()
                .orderByChild("userNameLowerCase")
                .startAt(mSearchQuery)
                .endAt(mSearchQuery+"\uf8ff")
        ;
        mUsersAdapter = new UsersAdapter(mQuery);
        mSearchResultsEmptyLayout = (NestedScrollView) findViewById(R.id.search_results_empty_layout);
        mUsersAdapter.setUpSearchAdapter(mSearchResultsRecyclerView, mSearchResultsEmptyLayout);
    }

    private void setUpSearchResultsRecyclerView() {
        mRecyclerViewLayoutManager = new LinearLayoutManager(this);
        mSearchResultsRecyclerView.setLayoutManager(mRecyclerViewLayoutManager);
        mSearchResultsRecyclerView.setHasFixedSize(false);
        mSearchResultsRecyclerView.setItemViewCacheSize(20);
        mSearchResultsRecyclerView.setItemAnimator(null);
        ItemAnimator animator = mSearchResultsRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, mRecyclerViewLayoutManager.getOrientation());
        mSearchResultsRecyclerView.addItemDecoration(dividerItemDecoration);
    }
}
