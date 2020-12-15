package com.example.quakealert;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    // Global Variables:
    LinearLayout loadingView, noDataView;
    RecyclerView recyclerView;
    EarthquakeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // HOOKS
        loadingView = findViewById(R.id.loadingView);
        noDataView = findViewById(R.id.noDataView);
        recyclerView = findViewById(R.id.recyclerView);

        // Calling fetchingEarthquake Method();
        fetchingEarthquake();

        // Setting pullToRefresh method:
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {

            // Calling fetchingEarthquake Method();
            fetchingEarthquake();
            pullToRefresh.setRefreshing(false);
        });
    }

    //TODO Setting up ToolBar Icons:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        // Toolbar Search Icon:
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Earthquake");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void fetchingEarthquake() {

        // Setting LayoutManager:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Checking Internet Connection:
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Setting Loading Page:
            setLoadingViewMethod();

            // Fetching earthquakes Data:
            AsyncHttpClient client = new AsyncHttpClient();
            String USGS_URL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/4.5_week.geojson";
            client.get(USGS_URL, new JsonHttpResponseHandler() {

                // Member Variables:
                String[] splitLocation;
                String magnitude, location, timeMilliSecond, url, time, location1, location2;

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    // Earthquake ArrayList:
                    ArrayList<EarthquakeItem> earthquakeItemArrayList = new ArrayList<>();

                    // Setting RecyclerView Page:
                    setRecyclerViewMethod();

                    try {
                        JSONArray jsonArray = response.getJSONArray("features");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONObject jsonProperty = jsonObject.getJSONObject("properties");

                            // JSON Variables:
                            String mag = jsonProperty.getString("mag");
                            location = jsonProperty.getString("place");
                            timeMilliSecond = jsonProperty.getString("time");
                            url = jsonProperty.getString("url");

                            // Formatting Magnitude:
                            if (!mag.contains(".")) {
                                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                                magnitude = decimalFormat.format(Integer.valueOf(mag));
                            } else {
                                magnitude = mag;
                            }

                            // Formatting Locations:
                            if (location.contains("of")) {
                                splitLocation = location.split("of");
                                location1 = splitLocation[0] + "of";
                                location2 = splitLocation[1].trim();
                            } else {
                                location1 = "Nearest of ";
                                location2 = location;
                            }

                            // Formatting TimMilliSecond:
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a");
                            time = sdf.format(Long.valueOf(timeMilliSecond));

                            // Setting RecyclerView:
                            earthquakeItemArrayList.add(new EarthquakeItem(magnitude, location1, location2, time, url));
                            adapter = new EarthquakeAdapter(earthquakeItemArrayList, getApplicationContext());
                            recyclerView.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    // Setting No Internet Connection Warning:
                    setNoDataViewMethod();
                    Toast.makeText(MainActivity.this, "Fetching error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Setting No Internet Connection Warning:
            setNoDataViewMethod();
            Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoadingViewMethod() {
        // Setting Loading Page:
        noDataView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
    }

    private void setNoDataViewMethod() {
        // Setting No Internet Connection Warning Page:
        noDataView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
    }

    private void setRecyclerViewMethod() {
        // Setting RecyclerView Page:
        noDataView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
    }
}

