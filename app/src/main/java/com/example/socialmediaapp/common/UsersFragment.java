package com.example.socialmediaapp.common;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapters.AdapterUser;
import com.example.socialmediaapp.common.MainActivity;
import com.example.socialmediaapp.models.ModelUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class UsersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterUser adapterUser;
    List<ModelUser> users;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient mGoogleSignInClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        //init firebase
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this.getActivity(), gso);
        firebaseAuth = FirebaseAuth.getInstance();

        //init recycler view
        recyclerView = view.findViewById(R.id.usersRCV);
        //set recycler view properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //init users list
        users = new ArrayList<>();
        //add users to list
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        //get current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database "users" contain users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all users
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelUser modelUser = dataSnapshot.getValue(ModelUser.class);
                    if (!modelUser.getUid().equals(firebaseUser.getUid())){
                        users.add(modelUser);
                    }

                    //adapter
                    adapterUser = new AdapterUser(getActivity(), users);
                    //set adpater to recycler view
                    recyclerView.setAdapter(adapterUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    /*inflate options menu*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        //serach listerner
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //if search query is not empty
                if(!query.isEmpty()){
                    searchUser(query);
                }else {
                    //if search query is empty
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //if search query is not empty
                if(!newText.isEmpty()){
                    searchUser(newText);
                }else {
                    //if search query is empty
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchUser(String query) {
        //get current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database "users" contain users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all users
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelUser modelUser = dataSnapshot.getValue(ModelUser.class);
                    //get all searched users except current signed user
                    if (!modelUser.getUid().equals(firebaseUser.getUid())){
                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase())
                        || modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            users.add(modelUser);
                        }
                    }

                    //adapter
                    adapterUser = new AdapterUser(getActivity(), users);
                    //refresh adapter
                    adapterUser.notifyDataSetChanged();

                    //set adpater to recycler view
                    recyclerView.setAdapter(adapterUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //handle item clicks


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);// show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            mGoogleSignInClient.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!= null){
            //user signed in stay
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        }else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}