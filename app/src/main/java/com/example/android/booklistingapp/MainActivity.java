package com.example.android.booklistingapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>>{

    private RecyclerView mRecyclerView;
    private BookRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private EditText mSearchEditText;
    private ImageView mSearch;
    private TextView mEmptyStateTextView;
    private ProgressBar mLoadingIndicator;

    private static final String GOOGLE_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final int BOOK_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Create a new adapter that takes an empty list of book as input
        mAdapter = new BookRecyclerAdapter(this, new ArrayList<Book>(), new BookRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                String url = book.getInfoLinkId();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        // Set the adapter on the {@link RecyclerView}
        // so the list can be populated in the user interface
        mRecyclerView.setAdapter(mAdapter);

        mSearch = (ImageView) findViewById(R.id.search);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        mSearchEditText = (EditText) findViewById(R.id.edit_text_search);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(BOOK_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadingIndicator.setVisibility(View.GONE);

            mRecyclerView.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle bundle) {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(GONE);

        String searchInput = mSearchEditText.getText().toString();

        if(searchInput.length() == 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.search_nothing), Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }

        searchInput = searchInput.replace(" ", "+");

        Uri baseUri = Uri.parse(GOOGLE_REQUEST_URL + searchInput);
        Uri.Builder uriBuilder = baseUri.buildUpon();

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
