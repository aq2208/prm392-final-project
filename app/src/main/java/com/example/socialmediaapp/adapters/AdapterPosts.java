package com.example.socialmediaapp.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.Post.AddPostActivity;
import com.example.socialmediaapp.Post.PostDetailActivity;
import com.example.socialmediaapp.Post.PostLikeActvity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.common.ThereProfileActivity;
import com.example.socialmediaapp.models.ModelPost;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{

    Context context;
    List<ModelPost> postsList;

    String myUid;
    private DatabaseReference likesRef;
    private DatabaseReference commentsRef;
    private DatabaseReference postsRef;

    boolean mProcessLike = false;

    public AdapterPosts(Context context, List<ModelPost> postsList) {
        this.context = context;
        this.postsList = postsList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference("Likes");
        //commentsRef = FirebaseDatabase.getInstance().getReference("Comments");
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_posts.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String uid = postsList.get(position).getUid();
        String uEmail = postsList.get(position).getuEmail();
        String uDp = postsList.get(position).getuDp();
        String uName = postsList.get(position).getuName();
        String pId = postsList.get(position).getpId();
        String pImage = postsList.get(position).getpImage();
        String pTimeStamp = postsList.get(position).getpTime();
        String pTitle = postsList.get(position).getpTitle();
        String pDescription = postsList.get(position).getpDescription();
        String pLikes = postsList.get(position).getpLikes();//number of likes
        String pComments = postsList.get(position).getpComments();//number of comments

        //convert timestamp to dd/mm/yyyy hh:mm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = (String) android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa",calendar);

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLikes + " Likes");
        holder.pCommentsTv.setText(pComments + " Comments");
        //set like for each post
        setLikes(holder,  pId);
        

        //set user dp
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
        }catch (Exception e){

        }

        //set postImg
        if(pImage.equals("noImage")){
            holder.pImageIv.setVisibility(View.GONE);
        }else{
            holder.pImageIv.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(pImage).into(holder.pImageIv);
            }catch (Exception e){

            }
        }

        //handle btn click

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn,uid,myUid, pId,pImage);
            }

        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get total number of likes for the posts, who like it
                int pLikes = Integer.parseInt(postsList.get(holder.getAdapterPosition()).getpLikes());
                mProcessLike = true;
                //getid of the post clicked
                String postIde = postsList.get(holder.getAdapterPosition()).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLike){
                            if(snapshot.child(postIde).hasChild(myUid)){
                                //already liked, remove like
                                postsRef.child(postIde).child("pLikes").setValue(String.valueOf(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }else{
                                //not liked
                                postsRef.child(postIde).child("pLikes").setValue(String.valueOf(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start post detail activity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId); //get detail according to post id
                context.startActivity(intent);
            }

        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //get image from image view
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.pImageIv.getDrawable();
                if(bitmapDrawable == null){
                    //post doesn't have image
                    shareTextOnly(pTitle,pDescription);
                }else{
                    //post with image
                    Bitmap b = bitmapDrawable.getBitmap();
                    shareWithImage(b,pTitle,pDescription);
                }
            }

        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click to go to user profile with uid
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

        holder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostLikeActvity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);

            }
        });
    }

    private void shareWithImage(Bitmap b, String pTitle, String pDescription) {

        String shareBody = pTitle +"\n" + pDescription;
        //save image in cache and get save image uri
        Uri uri = saveImageToShare(b);

        //share Intent
        Intent sItent = new Intent(Intent.ACTION_SEND);
        sItent.setType("image/png");
        sItent.putExtra(Intent.EXTRA_STREAM,uri);
        sItent.putExtra(Intent.EXTRA_TEXT,shareBody);
        context.startActivity(Intent.createChooser(sItent,"Share Via"));


    }

    private Uri saveImageToShare(Bitmap b) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try{
            imageFolder.mkdirs(); // create if not exist
            File file = new File(imageFolder,"shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG,100,stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context,"com.example.socialmediaapp.fileprovider",file);

        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody = pTitle + "\n" + pDescription;

        //share Intent
        Intent sItent = new Intent(Intent.ACTION_SEND);
        sItent.setType("text/plain");
        sItent.putExtra(Intent.EXTRA_TEXT,shareBody);
        context.startActivity(Intent.createChooser(sItent,"Share Via"));
    }

    private void setLikes(MyHolder holder, String pId) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(pId).hasChild(myUid)){
                    //liked this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0,0);
                    holder.likeBtn.setTag("Liked");
                }else{
                    //haven't like this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbup,0,0,0);
                    holder.likeBtn.setTag("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);
        if(uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0 , 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1 , 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"View Detail");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if(id == 0){
                    //delete clicked
                    deletePost(pId,pImage);
                }
                if(id == 1){
                    //edit clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);
                }
                if(id == 2){
                    //start post detail activity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId); //get detail according to post id
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void deletePost(String pId, String pImage) {
        if (pImage.equals("noImage")){
            deletePostWithoutImage(pId);
        }else{
            deletePostWithImage(pId,pImage);
        }
    }

    private void deletePostWithImage(String pId, String pImage) {
        //Progress Dialog
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //image deleted. delete post
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        pd.show();
    }

    private void deletePostWithoutImage(String pId) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView uPictureIv, pImageIv;
        TextView uNameTv,pTimeTv,pTitleTv , pDescriptionTv,pLikesTv,pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;
        LinearLayout profileLayout;
        public MyHolder(View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikeTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
}
