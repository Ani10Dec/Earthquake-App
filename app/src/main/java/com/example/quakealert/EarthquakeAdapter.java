package com.example.quakealert;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EarthquakeAdapter extends RecyclerView.Adapter<EarthquakeAdapter.ViewHolder> implements Filterable {

    private final ArrayList<EarthquakeItem> earthquakes;
    private final Context context;
    private final ArrayList<EarthquakeItem> backupQuake;

    public EarthquakeAdapter(ArrayList<EarthquakeItem> earthquakes, Context context) {
        this.earthquakes = earthquakes;
        this.context = context;
        this.backupQuake = new ArrayList<>(earthquakes);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_earthquake_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Getting current position:
        EarthquakeItem earthquake = earthquakes.get(position);

        // Setting earthquakeView Layout:
        holder.magnitude.setText(earthquake.getMag());
        holder.primaryLocation.setText(earthquake.getLocation2());
        holder.locationOffset.setText(earthquake.getLocation1());
        holder.time.setText(earthquake.getTime());


        // Setting OnClickListener on Earthquake:
        holder.itemLayout.setOnClickListener(v -> {
            Intent browseURL = new Intent(Intent.ACTION_VIEW, Uri.parse(earthquake.getUrl()));
            browseURL.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browseURL);
        });
    }

    @Override
    public int getItemCount() {
        return earthquakes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Global Variables:
        TextView magnitude, primaryLocation, locationOffset, time;
        LinearLayout itemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // HOOKS
            magnitude = itemView.findViewById(R.id.tvMag);
            primaryLocation = itemView.findViewById(R.id.tvPrimaryLocation);
            locationOffset = itemView.findViewById(R.id.tvLocationOffset);
            time = itemView.findViewById(R.id.tvTime);
            itemLayout = itemView.findViewById(R.id.itemContainer);

        }
    }

    // Getting Filtered ArrayList
    @Override
    public Filter getFilter() {
        return filterQuake;
    }

    private Filter filterQuake = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence input) {
            ArrayList<EarthquakeItem> filteredArray = new ArrayList<>();

            if (input.toString().isEmpty()) {
                filteredArray.addAll(backupQuake);
            } else {
                for (EarthquakeItem item : backupQuake) {
                    if (item.getLocation1().toLowerCase().contains(input.toString().toLowerCase())
                            || item.getLocation2().toLowerCase().contains(input.toString().toLowerCase())) {
                        filteredArray.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredArray;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            earthquakes.clear();
            earthquakes.addAll((ArrayList<EarthquakeItem>)results.values);
            notifyDataSetChanged();
        }
    };
}
