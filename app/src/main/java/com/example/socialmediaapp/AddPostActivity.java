package com.example.socialmediaapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.ref.Reference;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    ActionBar actionBar;
    //permission const
    private static final int CAMERA_REQUEST_CODE =100;
    private static final int GALLERY_REQUEST_CODE = 200;

    //image picking constants
    private static final int IMAGE_PICK_CAMERA_CODE =300;
    private static final int IMAGE_PICK_GALLERY_CODE =400;
    int currentRequestCode;


    String[] cameraPermissions;
    String[] storagePermissions;

    //Views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    //user info
    String name, email, uid, dp;

    Uri image_uri = null;
    private ActivityResultLauncher<Intent> cameraLauncher;

    //progress bar
    Dialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");

        //enable nback button in actionBar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        //inti permissions arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init progress bar
        pd = new Dialog(this);
        pd.setContentView(R.layout.activity_add_post);
        pd.setCancelable(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        actionBar.setSubtitle(email);
        //get info of current user
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    name= "" + ds.child("name").getValue();
                    email= "" + ds.child("email").getValue();
                    dp= "" + ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //init cameralauncher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if(currentRequestCode == IMAGE_PICK_CAMERA_CODE) {
                            imageIv.setImageURI(image_uri);
                        } else if (currentRequestCode == IMAGE_PICK_GALLERY_CODE && result.getData() !=null) {
                            image_uri = result.getData().getData();
                            imageIv.setImageURI(image_uri);
                        }
                    }
                }
        );

        //init galleryLauncher


        //init view
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);

        //upload image from phone
        imageIv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //show image pick dialog
                ShowImagePickDialog();
            }
        });
        //upload button listener
        uploadBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String title = titleEt.getText().toString().trim();
                String desciption = descriptionEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this,"Enter Title..."
                            ,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(desciption)){
                    Toast.makeText(AddPostActivity.this,"Enter Description..."
                            ,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(image_uri == null){
                    //post without image
                    uploadData(title, desciption, "noImage");
                }else {
                    uploadData(title,desciption,String.valueOf(image_uri));
                }
            }
        });

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }
    private void showProgressDialog(String message) {
        TextView progressMessage = pd.findViewById(R.id.progressMessage);
        progressMessage.setText(message);
        pd.show();
    }

    private void hideProgressDialog() {
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }

    private void uploadData(String title, String desciption, String uri) {
        showProgressDialog("Publishing post...");
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" +"post" + timeStamp;
        if(!uri.equals("noImage")){
            //post with image
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            reference.putFile(Uri.parse(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uoload to firebase storage
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){
                                //url is received upload post to firebase db
                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put info
                                hashMap.put("uid",uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescription", desciption);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pTime", timeStamp);

                                //path to store post Data
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in this ref
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //added in database
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT ).show();
                                                //reset views
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);
                                                image_uri = null;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // fail adding post
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT ).show();

                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT ).show();

                        }
                    });
        }else{
            //post without image
            //url is received upload post to firebase db
            HashMap<Object, String> hashMap = new HashMap<>();
            //put info
            hashMap.put("uid",uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescription", desciption);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);

            //path to store post Data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put data in this ref
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //added in database
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT ).show();
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_uri = null;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // fail adding post
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT ).show();

                        }
                    });
        }
        new Handler().postDelayed(() -> {
            hideProgressDialog();
        },3000);

    }

    //Check if storage permission is granted or not
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    //check if Camera permission is granted or not
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    //request Storage permission if not granted
    private void requestStoragePermission(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, GALLERY_REQUEST_CODE);
    }
    //request Camera permission if not granted
    private void requestCameraPermission(){
        //request runtime camera permission
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);

    }



    private void ShowImagePickDialog() {
        //options{camera, gallery} to show in dialog
        String[] options ={"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");
        builder.setItems(options, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    //choose camera
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        PickFromCamera();
                    }

                }
                if(which == 1){
                    //choose gallery
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        PickFromGallery();
                    }
                }

            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void PickFromGallery() {
        //pick from gallery
        currentRequestCode = IMAGE_PICK_GALLERY_CODE;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        cameraLauncher.launch(intent);
    }

    private void PickFromCamera() {
        //intent to pick image form camera
        currentRequestCode = IMAGE_PICK_CAMERA_CODE;
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick" );
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr" );
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        cameraLauncher.launch(intent);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method handle to allow or deny permission
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //if both permission is granted
                        PickFromCamera();
                    }else{
                        //if camera or gallery permission were denied
                        Toast.makeText(this,"both Camera and Storage permission are needed...", Toast.LENGTH_SHORT).show();
                    }
                }else{
                }
            }
            break;
            case GALLERY_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){
                        //if storage permission is granted
                        PickFromGallery();
                    }else{
                        //if storage permission were denied
                        Toast.makeText(this,"Storage permission are needed...", Toast.LENGTH_SHORT).show();
                    }
                }else{
                }
            }
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            //user signed in
            email = user.getEmail();
            uid = user.getUid();
        }else{
            //user not signed in -> main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        //menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_post){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}

