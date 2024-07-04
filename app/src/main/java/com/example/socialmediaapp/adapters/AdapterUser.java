package com.example.socialmediaapp.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.Chat.ChatActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.common.ThereProfileActivity;
import com.example.socialmediaapp.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context mContext;
    List<ModelUser> userList;

    FirebaseAuth firebaseAuth;
    String myUid;
    //constructor
    public AdapterUser(Context mContext, List<ModelUser> userList) {
        this.mContext = mContext;
        this.userList = userList;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_users.xml))
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_users, parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //bind data
        //get data
        String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();
        //set data
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_img)
                    .into(holder.mAvatarIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_img).into(holder.mAvatarIv);
        }
        holder.blockIv.setImageResource(R.drawable.ic_unblocked_green);
        //check if user is blocked or not
        checkIsBlocked(hisUid, holder, position);

        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show choose dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //profile
                            Intent intent = new Intent(mContext, ThereProfileActivity.class);
                            intent.putExtra("uid", hisUid);
                            mContext.startActivity(intent);
                        }
                        if (which == 1) {
                            //chat
                            imBlockedORNot(hisUid);
                        }
                    }
                });
                builder.create().show();
            }
        });
        //clikc to block/ublock user
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userList.get(holder.getAdapterPosition()).isBlocked()){
                    unblockUser(hisUid);

                }else{
                    blockUser(hisUid);
                    Log.d("TAG", "onClick: "+userList.get(holder.getAdapterPosition()).isBlocked());
                }

            }
        });
    }

    private void imBlockedORNot(String hisUid){
        //check current user is blocked by receiver or not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                Toast.makeText(mContext, "You blocked this user...", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        //if not blocked then start activity
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        intent.putExtra("hisUid", hisUid);
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void checkIsBlocked(String hisUid, MyHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       for(DataSnapshot ds : snapshot.getChildren()){
                           if(ds.exists()){
                               userList.get(position).setBlocked(true);
                               holder.blockIv.setImageResource(R.drawable.ic_blocked_red);
                           }
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUid) {
        //block by adding uid to current user's blocked node

        //put values in a hashmap to put in ddb
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(mContext, "Blocked successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Failed to block user :"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void unblockUser(String hisUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                           if(ds.exists()){
                               ds.getRef().removeValue()
                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void unused) {
                                               Toast.makeText(mContext, "Unblocked successfully...", Toast.LENGTH_SHORT).show();

                                           }
                                       }).addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {
                                               Toast.makeText(mContext, "Failed to unblock user :"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                           }
                                       });
                           }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {
        ImageView mAvatarIv , blockIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
            blockIv = itemView.findViewById(R.id.blockIv);
        }
    }
}
