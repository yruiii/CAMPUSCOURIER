package com.example.campuscourier.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.campuscourier.R;
import com.example.campuscourier.admin.AdminAdaptor;
import com.example.campuscourier.shared.Login;
import com.example.campuscourier.shared.Requests_2;
import com.example.campuscourier.shared.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class Admin_Home extends AppCompatActivity {

    private Button logoutButton;
    private RecyclerView recyclerViewAdmin;
    private ArrayList<Requests_2> adminArrayList;
    private AdminAdaptor adminAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        initializeViews();
        setupRecyclerView();
        setupFirebaseHelper();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeViews() {
        recyclerViewAdmin = findViewById(R.id.rvAdmin);
        logoutButton = findViewById(R.id.buttonLogout);
    }

    private void setupRecyclerView() {
        adminArrayList = new ArrayList<>();
        recyclerViewAdmin.setHasFixedSize(true);
        recyclerViewAdmin.setLayoutManager(new LinearLayoutManager(this));
        adminAdapter = new AdminAdaptor(adminArrayList, this);
        recyclerViewAdmin.setAdapter(adminAdapter);
    }

    private void setupFirebaseHelper() {
        FirebaseHelper.getReportAdmin(adminArrayList, adminAdapter);
    }
}
