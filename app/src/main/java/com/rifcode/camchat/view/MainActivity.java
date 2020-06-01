package com.rifcode.camchat.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.rifcode.camchat.utils.DialogUtils;
import com.rifcode.camchat.R;
import com.rifcode.camchat.models.Users;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static int GALLERY_PICK=10;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private StorageReference mStorageImage;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private TextView btnRate,btnNoThanks,tvusername,tvCountry,tvAgerofile,tvsexprofile;
    private View mViewInflate;
    private String sex="Male";
    private DatabaseReference dbusers;
    private RecyclerView rcvRandom;
    private FirebaseRecyclerAdapter<Users,userssViewHolder> randomRecyclerAdapte;
    private LinearLayoutManager mLayoutManager;
//    private InterstitialAd interstitialAd;
//    private AdView adView;
    private String TAG="Mainactivity";
    private CircleImageView imgvMyphoto;
    private NavigationView navigationView;
    private ImageView changePhoto;
    int index = 0;
    private TextView tvphone;
    private AdView mAdView;
    private View mViewInflatedialogUploadImage;
//    private Button btnFun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorageImage = FirebaseStorage.getInstance().getReference();
        widgets();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarmain);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);


        drawerLayout=findViewById(R.id.drawer);
//        btnFun=findViewById(R.id.btnFun);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle;
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {

            MobileAds.initialize(this);
            mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

//            // Initialize the Audience Network SDK
//            AudienceNetworkAds.initialize(this);
//
//            adView = new AdView(this, getString(R.string.banner1), AdSize.BANNER_HEIGHT_50);
//
//            // Find the Ad Container
//            LinearLayout adContainer = findViewById(R.id.banner_main);
//
//            // Add the ad view to your activity layout
//            adContainer.addView(adView);
//
//            // Request an ad
//            adView.loadAd();


            dbusers = FirebaseDatabase.getInstance().getReference().child("Users");

            Query aa = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("number").limitToFirst(100000);
            firebaseProductsRecyclerview(aa);

            dbusers.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String image = String.valueOf(dataSnapshot.child("image").getValue());
                    String ctr = String.valueOf(dataSnapshot.child("country").getValue());
                    String user = String.valueOf(dataSnapshot.child("username").getValue());
                    String rateapp = String.valueOf(dataSnapshot.child("rateApp").getValue());
                    String sex = String.valueOf(dataSnapshot.child("sex").getValue());
                    String age = String.valueOf(dataSnapshot.child("age").getValue());
                    String phone = String.valueOf(dataSnapshot.child("phone").getValue());

                    tvphone.setText(phone);
                    tvsexprofile.setText(sex);
                    tvAgerofile.setText(age);
                    tvCountry.setText(ctr);
                    tvusername.setText(user);
                    if (!image.equals("default"))
                        setImage(image);

                    if(rateapp.equals("false")|| !dataSnapshot.hasChild("rateApp"))
                        dialogRateThisApp();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

//        btnFun.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getAllkeys();
//            }
//        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermission())
                    requestPermission();
                else
                {
                    dialogUploadImage();
                }
            }
        });


    }

    private void dialogUploadImage(){

        mViewInflatedialogUploadImage= getLayoutInflater().inflate(R.layout.dialog_warning_uploadimage,null);
        final CheckBox cb = mViewInflatedialogUploadImage.findViewById(R.id.cbCheckImageUpload);
        Button btnupload = mViewInflatedialogUploadImage.findViewById(R.id.btnUpload);
        final AlertDialog.Builder alertDialogBuilder = DialogUtils.CustomAlertDialog(mViewInflatedialogUploadImage,this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cb.isChecked()){
                    /// open galery :
                    alertDialog.dismiss();
                    Intent galleryIntent = new Intent();
                    galleryIntent.setType("image/*");
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(galleryIntent,getString(R.string.select_galory)),GALLERY_PICK);

                }else{
                    Toast.makeText(MainActivity.this, getString(R.string.risk_deletaccount), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void setImage(String thumb_image){
        Picasso.get().load(thumb_image).placeholder(R.drawable.default_user).into(imgvMyphoto);
    }

    private void widgets() {
        navigationView = findViewById(R.id.nv);
        rcvRandom= findViewById(R.id.rclViewUsers);
        rcvRandom.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false);
        rcvRandom.setLayoutManager(mLayoutManager);

        tvCountry = navigationView.getHeaderView(0).findViewById(R.id.tvCountry);
        tvusername = navigationView.getHeaderView(0).findViewById(R.id.tvusername);
        imgvMyphoto = navigationView.getHeaderView(0).findViewById(R.id.imgvMyphoto);
        tvAgerofile = navigationView.getHeaderView(0).findViewById(R.id.tvageprofile);
        tvphone = navigationView.getHeaderView(0).findViewById(R.id.tvphone);
        tvsexprofile = navigationView.getHeaderView(0).findViewById(R.id.tvsexprofile);
        changePhoto = navigationView.getHeaderView(0).findViewById(R.id.changePhoto);
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListner) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, cancelListner)
                .create()
                .show();
    }


    private  boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    private void dialogRateThisApp(){

        mViewInflate = getLayoutInflater().inflate(R.layout.dialog_rateapp,null);
        btnRate = mViewInflate.findViewById(R.id.btnRateNow);
        btnNoThanks = mViewInflate.findViewById(R.id.btnNoThanks);
        final AlertDialog.Builder alertDialogBuilder = DialogUtils.CustomAlertDialog(mViewInflate,MainActivity.this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMarket();
                if(mAuth.getUid()!=null)
                    dbusers.child(mAuth.getUid()).child("rateApp").setValue("true");
                alertDialog.dismiss();
            }
        });

        btnNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

    }


    private void getAllkeys(){


        dbusers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String key = child.getKey();
                    fun(key);
                }
                Toast.makeText(MainActivity.this, "Finish", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }//onCancelled
        });

    }

    private void fun(String key) {
        dbusers.child(key).child("number").setValue(-1*System.currentTimeMillis());
    }


    private void firebaseProductsRecyclerview(final Query aa){

        randomRecyclerAdapte =
                new FirebaseRecyclerAdapter<Users,
                        userssViewHolder>(

                        Users.class,
                        R.layout.layout_item_user_match,
                        userssViewHolder.class,
                        aa

                ) {
                    @Override
                    protected void populateViewHolder(final userssViewHolder viewHolder, Users model, final int position) {

                        final String product_id = getRef(position).getKey();
//                        viewHolder.adContainer.removeAllViews();

//                        if(position==5 || position==1 || position==10 || position==20 || position==30 || position==40 || position==50 || position==60 ||
//                                position==70 || position==80 || position==90 || position==100 || position==110 || position==120
//                                || position==330 || position==130 || position==140 || position==150 || position==160 || position==170
//                                || position==240 || position==220 || position==210 || position==200 || position==190 || position==180
//                                || position==250 || position==260 || position==270 || position==280|| position==290 || position==300
//                                || position==320 || position==340 || position==350 || position==310 || position==360 || position==370
//                                || position==380 || position==390 || position==400 || position==410
//                        ){
//                            viewHolder.adContainer.setVisibility(View.VISIBLE);
//                            viewHolder.getAdsRectangle(MainActivity.this);
//
//                        }else{
//                            viewHolder.adContainer.setVisibility(View.GONE);
//                            //viewHolder.getAdsRectangle(MainActivity.this);
//
//                        }
//

                        dbusers.child(product_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild("sex") && dataSnapshot.hasChild("username") && dataSnapshot.hasChild("image") && dataSnapshot.hasChild("phone")) {
                                    String sexxx = dataSnapshot.child("sex").getValue().toString();
                                    String age = dataSnapshot.child("age").getValue().toString();
                                    String country = dataSnapshot.child("country").getValue().toString();
                                    String phone = dataSnapshot.child("phone").getValue().toString();

                                    viewHolder.tvphoneUser.setText(phone);
                                    viewHolder.tvusername.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                                    viewHolder.tvLiveInUserMatch.setText(country);
                                    String image = String.valueOf(dataSnapshot.child("image").getValue());
                                    imageUserFromDBviewHolder(image, sexxx, viewHolder);
                                    viewHolder.tvageuser.setText(age);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });

                        viewHolder.imgvLovemsg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(!product_id.equals(mAuth.getUid())) {
                                    Intent intmessage = new Intent(MainActivity.this, ChatUserActivity.class);
                                    intmessage.putExtra("userIDvisited", product_id);
                                    startActivity(intmessage);
                                }
                            }
                        });

                        viewHolder.imgvVideoMssage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(!product_id.equals(mAuth.getUid())) {
                                    Intent intmessage = new Intent(MainActivity.this, ChatUserActivity.class);
                                    intmessage.putExtra("userIDvisited", product_id);
                                    startActivity(intmessage);
                                }
                            }
                        });

                        viewHolder.imgvMessagemsg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(!product_id.equals(mAuth.getUid())) {
                                    Intent intmessage = new Intent(MainActivity.this, ChatUserActivity.class);
                                    intmessage.putExtra("userIDvisited", product_id);
                                    startActivity(intmessage);
                                }
                            }
                        });


                    }

                };
        rcvRandom.setAdapter(randomRecyclerAdapte);
//        randomRecyclerAdapte.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//
//                int testimonycount = randomRecyclerAdapte.getItemCount();
//                int lastVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
//
//                if (lastVisiblePosition == -1 ||
//                        (positionStart >= (testimonycount - 1) &&
//                                lastVisiblePosition == (positionStart - 1))) {
//                    rcvRandom.scrollToPosition(positionStart);
//                }
//            }
//        });
    }

    private void imageUserFromDBviewHolder(final String image, String sexxxx,final userssViewHolder holder){


        if(!image.equals("default")) {

            //// Offline Capabilities: networkPolicy(NetworkPolicy.OFFLINE)
            Picasso.get().load(image).into(holder.imgvUser, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(image).into(holder.imgvUser);
                }
            });

        }else
         {

            //// Offline Capabilities: networkPolicy(NetworkPolicy.OFFLINE)
            Picasso.get().load(R.drawable.default_user).into(holder.imgvUser, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(R.drawable.default_user).into(holder.imgvUser);
                }
            });

        }


    }

//    private void showAdWithDelay() {
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                // Check if interstitialAd has been loaded successfully
//                if(interstitialAd == null || !interstitialAd.isAdLoaded()) {
//                    return;
//                }
//                // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
//                if(interstitialAd.isAdInvalidated()) {
//                    return;
//                }
//                // Show the ad
//                interstitialAd.show();
//            }
//        }, 1000 * 60 * 5);
//        interstitialAd.loadAd();
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (interstitialAd != null) {
//            interstitialAd.destroy();
//        }
//        super.onDestroy();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        interstitialAd = new InterstitialAd(this, getString(R.string.interstitial));
//        showAdWithDelay();
//    }

    public static class userssViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView imgvUser;
        TextView tvusername,tvageuser,tvLiveInUserMatch,tvphoneUser;
        ImageView imgvLovemsg,imgvMessagemsg,imgvVideoMssage;

//        AdView adViewItem;
//        LinearLayout adContainer;
        public userssViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tvusername = mView.findViewById(R.id.tvUserNameMatch2);
            tvphoneUser = mView.findViewById(R.id.tvPhoneUser);
            imgvLovemsg = mView.findViewById(R.id.imgvLoveMessage);
            imgvMessagemsg = mView.findViewById(R.id.imgvMessageMessage);
            imgvVideoMssage = mView.findViewById(R.id.imgvVideoMssage);
            imgvUser = mView.findViewById(R.id.imgvUserMatch);
            tvageuser = mView.findViewById(R.id.tvAgeMatchuser);
            tvLiveInUserMatch = mView.findViewById(R.id.tvLiveInUserMatch);
            // Find the Ad Container
//            adContainer = mView.findViewById(R.id.banner_item);
        }

        public void getAdsRectangle(Context mCont){

//            adViewItem = new AdView(mCont, mCont.getString(R.string.rectangle), AdSize.RECTANGLE_HEIGHT_250);
//            // Add the ad view to your activity layout
//            adContainer.addView(adViewItem);
//
//            // Request an ad
//            adViewItem.loadAd();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            loginActActivity();
        }
//        interstitialAd = new InterstitialAd(this, getString(R.string.interstitial));
//        showAdWithDelay();
    }

    public void loginActActivity(){
        // if user used com first time :
        Intent loginActIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginActIntent);

        finish();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.ic_matching: {
                startActivity(new Intent(MainActivity.this, MatchingActivity.class));
                overridePendingTransition(R.anim.enter_right_to_left,R.anim.exit_right_to_left);
                break;
            }
            case R.id.ic_home: {

                break;
            }
            case R.id.ic_messaging: {
                startActivity(new Intent(MainActivity.this, MessagingActivity.class));
                overridePendingTransition(R.anim.enter_right_to_left,R.anim.exit_right_to_left);
                break;
            }

            case R.id.ic_pp: {
                startActivity(new Intent(MainActivity.this, PolicyActivity.class));
                overridePendingTransition(R.anim.enter_right_to_left,R.anim.exit_right_to_left);
                break;
            }
            case R.id.ic_rating: {
                launchMarket();
                break;
            }
            case R.id.ic_shareapp: {
                onClickShareApp();
                break;
            }
            case R.id.ic_logout: {
                FirebaseAuth.getInstance().signOut();
                loginActActivity();
                break;
            }

        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            //Toast.makeText(this, " unable to find market com", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickShareApp(){
        Intent sharePst = new Intent(Intent.ACTION_SEND);
        sharePst.setType("text/plain");
        sharePst.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_friend)+" "+"https://play.google.com/store/apps/details?id="+getPackageName());

        startActivity(Intent.createChooser(sharePst,getString(R.string.choose_app_share)));
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else
        {
            super.onBackPressed();
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.perm_grat), Toast.LENGTH_SHORT).show();
                    dialogUploadImage();
//                    Intent galleryIntent = new Intent();
//                    galleryIntent.setType("image/*");
//                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                    startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.select_galory)), GALLERY_PICK);

                } else {
                    Toast.makeText(this, R.string.per_dene, Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED )
                        {
                            showMessageOKCancel(getString(R.string.youneed),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermission();
                                        }
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            Toast.makeText(MainActivity.this, getString(R.string.accept_permission), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)

                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                final Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                // set id user to name image .jpg
                String idUserForImage = mAuth.getUid();


                //// Bitmap Upload image//////////////////////////////
                Bitmap thumb_bitmap = new Compressor(MainActivity.this)
                        .setMaxHeight(160)
                        .setMaxWidth(160)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                //////////////////////// end upload //////////////////////////////////////

                final StorageReference filePath = mStorageImage.child("profile_image").child(idUserForImage+".jpg");

                Uri file = Uri.fromFile(new File(thumb_filePath.getAbsolutePath()));
                UploadTask uploadTask = filePath.putFile(file);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (downloadUri != null) {

                                String photoStringLink = downloadUri.toString(); //YOU WILL GET THE DOWNLOAD URL HERE !!!!
                                Map updateHash_map = new HashMap<>();
                                updateHash_map.put("image",photoStringLink);
                                dbusers.child(mAuth.getUid()).updateChildren(updateHash_map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(MainActivity.this, R.string.imagseccess, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            }

                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                //Log.d(TAG, "onActivityResult: "+error);
            }
        }


    }


}
