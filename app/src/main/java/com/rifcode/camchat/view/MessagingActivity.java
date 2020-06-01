package com.rifcode.camchat.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

//import com.facebook.ads.AdSize;
//import com.facebook.ads.AdView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rifcode.camchat.R;
import com.rifcode.camchat.models.Messaging;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagingActivity extends AppCompatActivity {

    private DatabaseReference userdatabseRef;
    private DatabaseReference messagingdatabseRef;
    private RecyclerView rcvUsersMessages;
    private FirebaseRecyclerAdapter<Messaging,messagingViewHolder> messagingRecyclerAdapte;
    private String current_user_id;
    private FirebaseAuth mAuth;
    private LinearLayoutManager mLayoutManager;
//    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);


//        adView = new AdView(this, getString(R.string.banner2), AdSize.BANNER_HEIGHT_50);
//
//        // Find the Ad Container
//        LinearLayout adContainer =  findViewById(R.id.banner_messaging);
//
//        // Add the ad view to your activity layout
//        adContainer.addView(adView);
//
//        // Request an ad
//        adView.loadAd();

        widgets();
        // auth :
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        userdatabseRef= FirebaseDatabase.getInstance().getReference().child("Users");
        messagingdatabseRef= FirebaseDatabase.getInstance().getReference().child("Messaging").child(current_user_id);

        getSupportActionBar().setTitle(getString(R.string.messaging));

        firebaseRecyclerview();

    }

    private void widgets() {
        rcvUsersMessages = findViewById(R.id.rcvMessaging);
        rcvUsersMessages.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(MessagingActivity.this);
        mLayoutManager.setReverseLayout(true); /// reverse ///
        mLayoutManager.setStackFromEnd(true);
        rcvUsersMessages.setLayoutManager(mLayoutManager);


    }

    private void firebaseRecyclerview(){

        Query qury = messagingdatabseRef.orderByChild("TimeAgo");
        messagingRecyclerAdapte = new FirebaseRecyclerAdapter<Messaging, messagingViewHolder>(
                Messaging.class,
                R.layout.layout_user_messages,
                messagingViewHolder.class,
                qury
        ) {
            @Override
            protected void populateViewHolder(final messagingViewHolder viewHolder, Messaging model, final int position) {

                final String list_user_id = getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent chatin = new Intent(MessagingActivity.this, ChatUserActivity.class);
                        chatin.putExtra("userIDvisited",list_user_id);
                        startActivity(chatin);
                    }
                });


                userdatabseRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = String.valueOf(dataSnapshot.child("username").getValue());
                        String image = String.valueOf(dataSnapshot.child("image").getValue());

                        viewHolder.setusername(userName);
                        viewHolder.setUserImage(image,MessagingActivity.this);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        };
        rcvUsersMessages.setAdapter(messagingRecyclerAdapte);
    }

    public static class messagingViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView tvusername;
        CircleImageView civUsersMessaging;

        public messagingViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            tvusername =  mView.findViewById(R.id.tvUsernameMessaging);
            civUsersMessaging = mView.findViewById(R.id.imgProfileUsername);


        }



        public void setusername(String username){

            tvusername.setText(username);
        }

        public void setUserImage(String thumb_image, Context ctx){

            Picasso.get().load(thumb_image)
                    .placeholder(R.drawable.default_user).into(civUsersMessaging);

        }


    }


}
