package com.example.android.booklistingapp;

import android.app.LoaderManager;
import android.content.Loader;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<List<Book>>{

    private String mSearchQuery;
    private RecyclerView mRecyclerView;
    private String searchInput;
    private EditText mSearchEditText;
    private TextView mEmptyStateTextView;
    private ProgressBar mLoadingIndicator;
    private BookRecyclerAdapter mAdapter;
    private static final String GOOGLE_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final int BOOK_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);


        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = new BookRecyclerAdapter(this, new ArrayList<Book>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mSearchQuery = query;
        getLoaderManager().restartLoader(BOOK_LOADER_ID, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.btn_search));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle bundle) {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(GONE);
        searchInput = mSearchEditText.getText().toString();

        if(searchInput.length() == 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.search_nothing), Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }

        searchInput = searchInput.replace(" ", "+");

        Uri baseUri = Uri.parse(GOOGLE_REQUEST_URL+searchInput);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        if(mSearchQuery!=null)
            uriBuilder.appendQueryParameter("q", mSearchQuery);

        ProgressBar loadingSpinner = (ProgressBar) findViewById(R.id.loading_indicator);
        loadingSpinner.setVisibility(View.VISIBLE);
        return new BookLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> book) {
        // Hide loading indicator because the data has been loaded
        mLoadingIndicator.setVisibility(View.GONE);
        mEmptyStateTextView.setVisibility(View.VISIBLE);

        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText(R.string.no_found);

        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (book != null && !book.isEmpty()) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setVisibility(View.GONE);
            mAdapter.addAll(book);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }
}
