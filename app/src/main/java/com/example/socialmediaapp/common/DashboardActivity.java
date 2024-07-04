package com.example.socialmediaapp.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.socialmediaapp.Chat.ChatListFragment;
import com.example.socialmediaapp.Notification.Token;
import com.example.socialmediaapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

@SuppressWarnings("deprecation")
public class DashboardActivity extends AppCompatActivity {
    //firebase authen
    FirebaseAuth firebaseAuth;
    GoogleSignInClient mGoogleSignInClient;

    //init views
    //TextView mProfileTv;
    ActionBar actionBar;
    String mUId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //actionbar and titile
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //init components
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        firebaseAuth = FirebaseAuth.getInstance();

        //mProfileTv = findViewById(R.id.);

        //Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //home on start
        actionBar.setTitle("Home");
        HomeFragment fragment= new HomeFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content,fragment, "");
        ft.commit();

        checkUserStatus();

        //update token


//        updateToken(FirebaseMessaging.getInstance().getToken().toString());

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
              if (menuItem.getItemId() == R.id.nav_home){
                  //home fragment transaction
                  actionBar.setTitle("Home");
                  HomeFragment fragment= new HomeFragment();
                  FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                  ft.replace(R.id.content,fragment, "");
                  ft.commit();

                  return true;
              }
              if(menuItem.getItemId() == R.id.nav_profile){
                  //profile fragment transaction
                  actionBar.setTitle("Profile");
                  ProfileFragment fragment1= new ProfileFragment();
                  FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                  ft1.replace(R.id.content,fragment1, "");
                  ft1.commit();
                  return true;
              }
              if(menuItem.getItemId() == R.id.nav_users){
                  //users fragment transaction
                  actionBar.setTitle("Users");
                  UsersFragment fragment2= new UsersFragment();
                  FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                  ft2.replace(R.id.content,fragment2, "");
                  ft2.commit();
                  return true;
              }
              if (menuItem.getItemId() == R.id.nav_chat){
                  //home fragment transaction
                  actionBar.setTitle("Chats");
                  ChatListFragment fragment3= new ChatListFragment();
                  FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                  ft3.replace(R.id.content,fragment3, "");
                  ft3.commit();

                  return true;
              }
                  return false;
            }
        });

    }



    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token data = new Token(token);
        ref.child(mUId).setValue(data);
    }


    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!= null){
            //user signed in stay
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            //get user id
            mUId = user.getUid();
            //save uid of currently signed in user in shared preference
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("CURRENT_USER_ID", mUId);
            editor.apply();

            //update token
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if(task.isSuccessful()){
                        String token = task.getResult();
                        updateToken(token);
                        Log.d("TOKEN3", token);
                    }
                }
            });
        }else {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

}