package com.example.bookbank.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookbank.R;
import com.example.bookbank.adapters.NotificationsAdapter;
import com.example.bookbank.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private ListView notificationList;
    private ArrayList<Notification> notifications;
    private ArrayAdapter<Notification> notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        firebaseAuth = FirebaseAuth.getInstance();

        /** Get the current user that is signed in */
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // .setText(user.getUid());

        /** Get instance of Firestore */
        db = FirebaseFirestore.getInstance();

        /** Get top level reference to the collection Notification */
        final CollectionReference notificationReference = db.collection("Notification");

        /** Find reference to the ListView */
        notificationList = findViewById(R.id.notification_list);
        notifications = new ArrayList<>();

        notificationAdapter = new NotificationsAdapter(this, R.layout.notification_block, notifications);
        notificationList.setAdapter(notificationAdapter);

        /** If the notification in list view is long clicked */
        notificationList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Notification clickedNotification = notifications.get(position);
                db.collection("Notification").document(clickedNotification.getId()).delete();
                notifications.remove(clickedNotification);
                notificationAdapter.notifyDataSetChanged();

                return false;
            }
        });

        /**  Realtime updates, snapshot is the state of the database at any given point of time */
        notificationReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            /** Method is executed whenever any new event occurs in the remote database */
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                // Clear the old list
                notifications.clear();

                for (QueryDocumentSnapshot doc: queryDocumentSnapshots)
                {
                    // Tests
                    Log.d("ID", String.valueOf(doc.getData().get("id")));
                    Log.d("MESSAGE", String.valueOf(doc.getData().get("message")));
                    Log.d("USERID", String.valueOf(doc.getData().get("userId")));

                    String id = (String) doc.getData().get("id");
                    String message = (String) doc.getData().get("message");
                    String userId = (String) doc.getData().get("userId");

                    if (userId.equals(user.getUid())) { //Display books that only belong to that user
                        notifications.add(new Notification(id, userId, message)); // Add notification from FireStore
                    }
                }
                notificationAdapter.notifyDataSetChanged(); //Notify the adapter of data change
            }
        });



        // --------------------------Required for Toolbar---------------------------------//
        // set tool bar
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
    }

    // --------------------------Create Toolbar Menu---------------------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.inflateMenu(R.menu.activity_main_drawer);
        tb.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onOptionsItemSelected(item);
                    }
                });
        return true;
    }

    // --------------------------Create Toolbar Menu---------------------------------//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.nav_my_profile:
                startActivity(new Intent(NotificationsActivity.this, EditProfileActivity.class));
                break;
            case R.id.nav_my_books:
                startActivity(new Intent(NotificationsActivity.this, OwnerBooksActivity.class));
                break;
            case R.id.nav_borrowed_books:
                startActivity(new Intent(NotificationsActivity.this, BorrowedBooksActivity.class));
                break;
            case R.id.nav_search_books:
                startActivity(new Intent(NotificationsActivity.this, SearchBooksActivity.class));
                break;
            case R.id.nav_notifications:
                startActivity(new Intent(NotificationsActivity.this, NotificationsActivity.class));
                break;
            case R.id.nav_search_users:
                startActivity(new Intent(NotificationsActivity.this, SearchUsernameActivity.class));
                break;
            case R.id.nav_my_requests:
                startActivity(new Intent(NotificationsActivity.this, MyCurrentRequestsActivity.class));
                break;
            case R.id.nav_sign_out:
                firebaseAuth.signOut();
                Toast.makeText(NotificationsActivity.this, "succcessfully signed out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(NotificationsActivity.this, LoginActivity.class));
                break;
            default:
                break;
        }
        return true;
    }
}