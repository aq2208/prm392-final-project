package com.example.socialmediaapp.Chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapters.AdapterChatLists;
import com.example.socialmediaapp.models.ModelChat;
import com.example.socialmediaapp.models.ModelChatList;
import com.example.socialmediaapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatList> chatlistList;
    List<ModelUser> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatLists adapterChatLists;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        //init components
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.chatlistRCV);
        chatlistList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelChatList modelChatList = dataSnapshot.getValue(ModelChatList.class);
                    chatlistList.add(modelChatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelUser modelUser = dataSnapshot.getValue(ModelUser.class);
                    for(ModelChatList modelChatList : chatlistList) {
                        if (modelUser.getUid().equals(modelChatList.getId()) && modelUser.getUid() != null) {
                            userList.add(modelUser);
                            break;
                        }
                    }
                    //adapter
                    adapterChatLists = new AdapterChatLists(getContext(), userList);
                    recyclerView.setAdapter(adapterChatLists);
                    for(int i = 0; i < userList.size(); i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chats");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelChat chat = dataSnapshot.getValue(ModelChat.class);
                    if(chat == null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if(sender == null || receiver == null) {
                        continue;
                    }
                    if((chat.getReceiver().equals(currentUser.getUid())
                            && chat.getSender().equals(userId))
                            || (chat.getReceiver().equals(userId)
                            && chat.getSender().equals(currentUser.getUid()))){
                        if(chat.getType().equals("image")){
                            theLastMessage = "Sent a photo";
                        }else{
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatLists.setLastMessageMap(userId, theLastMessage);
                adapterChatLists.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}