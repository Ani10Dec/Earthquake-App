package com.example.quakealert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private LinearLayout loadingView, noDataView;
    private RecyclerView recyclerView;
    private EarthquakeAdapter adapter;

    // EarthQuake URL:
    final String mutableUrl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&endtime";
    final String latestUrl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&endtime&minmagnitude=5";

    // State of Screen
    int stateOfScreen = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // HOOKS
        loadingView = findViewById(R.id.loadingView);
        noDataView = findViewById(R.id.noDataView);
        recyclerView = findViewById(R.id.recyclerView);

        // Setting pullToRefresh method:
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {

            // Calling fetchingEarthquake Method();
            if (stateOfScreen == 1) {
                getMutableUrlJson();
            } else if (stateOfScreen == 2) {
                fetchingEarthquake(latestUrl);
            }
            pullToRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        stateOfScreen = 2;
        fetchingEarthquake(latestUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stateOfScreen = 1;
        getMutableUrlJson();
    }

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuFilter) {
            Toast.makeText(this, "Set Desired Earthquake", Toast.LENGTH_SHORT).show();
            Intent filterIntent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(filterIntent);
//            return true;
        } else if (id == R.id.menuAbout) {
            Toast.makeText(this, "About Quake Alert", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuLatest) {
            stateOfScreen = 2;
            fetchingEarthquake(latestUrl);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMutableUrlJson() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String min_mag = preferences.getString(getString(R.string.settings_min_magnitude_key), "5");
        String no_of_quake = preferences.getString(getString(R.string.settings_no_of_earthquake_key), "12");
        String orderBy = preferences.getString(getString(R.string.settings_order_by_key), "Descending");

        String sorting;
        if (!orderBy.equals("Descending")) {
            sorting = "magnitude-asc";
        } else sorting = "magnitude";

        Uri uri = Uri.parse(mutableUrl).buildUpon().
                appendQueryParameter("minmagnitude", min_mag).
                appendQueryParameter("limit", no_of_quake).
                appendQueryParameter("orderby", sorting).
                build();
        Log.d("url", String.valueOf(uri));
        String editedUrl = uri.toString();
        fetchingEarthquake(editedUrl);
    }

    private void fetchingEarthquake(String url) {

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
            client.get(url, new JsonHttpResponseHandler() {

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

