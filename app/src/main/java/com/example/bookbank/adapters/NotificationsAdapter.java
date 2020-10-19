package com.example.bookbank.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookbank.models.Notification;

import java.util.ArrayList;

public class NotificationsAdapter extends ArrayAdapter {

    private ArrayList<Notification> notificationList;

    public NotificationsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Notification> notificationList) {
        super(context, resource, notificationList);
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // custom array adapter for formatting each item in our list
        // inflate our custom layout (R.layout.gear_list_view) instead of the default view
        // LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // View view = inflater.inflate(R.layout.list_item, null);

        return convertView;
    }
}
