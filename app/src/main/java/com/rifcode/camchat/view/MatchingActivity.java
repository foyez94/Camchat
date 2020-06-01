package com.rifcode.camchat.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.facebook.ads.AdSize;
//import com.facebook.ads.AdView;
//import com.facebook.ads.AudienceNetworkAds;
//import com.facebook.ads.InterstitialAd;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rifcode.camchat.R;
import com.rifcode.camchat.models.Users;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatchingActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private TextView btnRate,btnNoThanks;
    private View mViewInflate;
    private String sex="Male";
    private Button btnStart;
    private DatabaseReference dbRandom,dbreqst;
    private TextView tvlookingfor,tvageuser;
    private DatabaseReference dbusers;
    private ArrayList<String> userList=new ArrayList<>();
    private Users user;
    private RecyclerView rcvRandom;
    private FirebaseRecyclerAdapter<Users,userssViewHolder> randomRecyclerAdapte;
    private LinearLayout lnDefault;
    private LinearLayoutManager mLayoutManager;
//    private InterstitialAd interstitialAd;
    int index  = 0;
    private DatabaseReference dbRandomMale,dbRandomFimale;
//    private AdView adView;
    private String TAG="activity_matching";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {

//            // Initialize the Audience Network SDK
//            AudienceNetworkAds.initialize(this);
//
//            adView = new AdView(this, getString(R.string.banner1), AdSize.BANNER_HEIGHT_50);
//
//            // Find the Ad Container
//            LinearLayout adContainer = findViewById(R.id.banner_matching);
//
//            // Add the ad view to your activity layout
//            adContainer.addView(adView);
//
//            // Request an ad
//            adView.loadAd();

            getSupportActionBar().setTitle(getString(R.string.match));

            dbRandom = FirebaseDatabase.getInstance().getReference().child("Random");

            dbusers = FirebaseDatabase.getInstance().getReference().child("Users");

            dbusers.child(String.valueOf(mAuth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild("sex") || dataSnapshot.hasChild("country")) {

                        String sexUser = String.valueOf(dataSnapshot.child("sex").getValue());
                        String country = String.valueOf(dataSnapshot.child("country").getValue());

                        dbRandomMale = FirebaseDatabase.getInstance().getReference().child("Random").child("Male").child(country);
                        dbRandomFimale = FirebaseDatabase.getInstance().getReference().child("Random").child("Female").child(country);

                        if (sexUser.equals("Male")) {
                            Query aa= dbRandomFimale.orderByChild("number");
                            firebaseProductsRecyclerview(aa);
                        }
                        else {
                            Query aa= dbRandomMale.orderByChild("number");
                            firebaseProductsRecyclerview(aa);
                        }
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });




        }
        wedgets();


    }


    private void firebaseProductsRecyclerview(Query aa){

        randomRecyclerAdapte =
                new FirebaseRecyclerAdapter<Users,
                        userssViewHolder>(

                        Users.class,
                        R.layout.layour_item_user,
                        MatchingActivity.userssViewHolder.class,
                        aa

                ) {
                    @Override
                    protected void populateViewHolder(final MatchingActivity.userssViewHolder viewHolder, Users model, final int position) {

                        final String product_id = getRef(position).getKey();
                        assert product_id != null;

                        dbusers.child(product_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild("username") && dataSnapshot.hasChild("image")) {

                                    viewHolder.tvusername.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                                    String image = String.valueOf(dataSnapshot.child("image").getValue());
                                    imageUserFromDBviewHolder(image, viewHolder);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });

                        viewHolder.imgvUser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(!product_id.equals(mAuth.getUid())) {
                                    Intent intmessage = new Intent(MatchingActivity.this, ChatUserActivity.class);
                                    intmessage.putExtra("userIDvisited", product_id);
                                    startActivity(intmessage);
                                }
                            }
                        });

                        viewHolder.imgvvideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(!product_id.equals(mAuth.getUid())) {
                                    Intent intmessage = new Intent(MatchingActivity.this, ChatUserActivity.class);
                                    intmessage.putExtra("userIDvisited", product_id);
                                    startActivity(intmessage);
                                }
                            }
                        });


                    }

                };
        rcvRandom.setAdapter(randomRecyclerAdapte);
        randomRecyclerAdapte.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int testimonycount = randomRecyclerAdapte.getItemCount();
                int lastVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (testimonycount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    rcvRandom.scrollToPosition(positionStart);
                }
            }
        });
    }

    public static class userssViewHolder extends RecyclerView.ViewHolder{

        View mView;
        CircleImageView imgvUser;
        ImageView imgvvideo;
        TextView tvusername;

        public userssViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tvusername = mView.findViewById(R.id.tvUsernameSearch);
            imgvUser = mView.findViewById(R.id.imgvUserSearch);
            imgvvideo = mView.findViewById(R.id.imgvVideoSearch);
            // Find the Ad Container
        }



    }



    private void imageUserFromDBviewHolder(final String image,final userssViewHolder holder){


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

        }else{

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

    @Override
    protected void onResume() {
        super.onResume();

    }


    private void wedgets() {

        rcvRandom= findViewById(R.id.rclViewUsersmatching);
        rcvRandom.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(MatchingActivity.this,2
                ,LinearLayoutManager.VERTICAL,true);
        rcvRandom.setLayoutManager(mLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            loginActActivity();
        }


    }

    public void loginActActivity(){
        // if user used com first time :
        Intent loginActIntent = new Intent(MatchingActivity.this, LoginActivity.class);
        startActivity(loginActIntent);

        finish();
    }


}
