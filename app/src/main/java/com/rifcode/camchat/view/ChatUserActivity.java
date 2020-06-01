package com.rifcode.camchat.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.facebook.ads.AdSize;
//import com.facebook.ads.AdView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rifcode.camchat.models.Chat;
import com.rifcode.camchat.utils.DialogUtils;
import com.rifcode.camchat.R;

public class ChatUserActivity extends AppCompatActivity {

    private String userID;
    public static String hisID;
    private FirebaseUser user;
    private RecyclerView rcvMessages;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference dbrefHischat,dbrefMyChat;
    private DatabaseReference dbrefUsers;
    private ImageView imgvSend;
    private EditText edtMessage;
    private FirebaseRecyclerAdapter<Chat,chatViewHolder> chatRecyclerAdapte;
    private DatabaseReference dbMessagingHis,dbMessagingMy;
//    private AdView adView;
    private DatabaseReference dbrefnotifi;
    private View mViewInflate;
    private DatabaseReference dbReportAbuseOfContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dbReportAbuseOfContent = FirebaseDatabase.getInstance().getReference().child("ReportAbuse");

//        adView = new AdView(this, getString(R.string.banner2), AdSize.BANNER_HEIGHT_50);
//
//        // Find the Ad Container
//        LinearLayout adContainer =  findViewById(R.id.banner_chat);
//
//        // Add the ad view to your activity layout
//        adContainer.addView(adView);
//
//        // Request an ad
//        adView.loadAd();

        widgets();
        rcvMessages = findViewById(R.id.rclViewMessages);
        rcvMessages.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(ChatUserActivity.this);
        rcvMessages.setLayoutManager(mLayoutManager);

        dbrefnotifi = FirebaseDatabase.getInstance().getReference().child("Notifications");

        hisID = getIntent().getStringExtra("userIDvisited");



        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();



        dbrefMyChat = FirebaseDatabase.getInstance().getReference().child("Chat").child(userID).child(hisID);
        dbrefHischat = FirebaseDatabase.getInstance().getReference().child("Chat").child(hisID).child(userID);
        dbMessagingMy = FirebaseDatabase.getInstance().getReference().child("Messaging").child(userID).child(hisID);
        dbMessagingHis = FirebaseDatabase.getInstance().getReference().child("Messaging").child(hisID).child(userID);
        dbrefUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        dbrefUsers.child(hisID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = String.valueOf(dataSnapshot.child("username").getValue());
                getSupportActionBar().setTitle(username);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        onClick();
    }

    private void onClick() {
        imgvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = edtMessage.getText().toString();

                if (!txt.isEmpty())
                    sendMessage(txt);

            }
        });




    }

    private void widgets() {

        edtMessage = findViewById(R.id.txtSendMessage);
        imgvSend = findViewById(R.id.imgBtnSend);

    }

    private void sendMessage(String message){

        DatabaseReference dbrefMyChatsend = dbrefMyChat.push();
        DatabaseReference dbrefHIschattsend = dbrefHischat.push();
        //String pushMessage = String.valueOf(dbrefMyChatsend.getKey());

        // Toast.makeText(this, pushMessage, Toast.LENGTH_SHORT).show();


        // send to my messages
        dbrefMyChatsend.child("from").setValue(userID);
        dbrefMyChatsend.child("message").setValue(message);

        dbrefHIschattsend.child("from").setValue(userID);
        dbrefHIschattsend.child("message").setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                edtMessage.setText("");
            }
        });

        rcvMessages.getAdapter().notifyDataSetChanged();
        rcvMessages.smoothScrollToPosition(rcvMessages.getAdapter().getItemCount());

        chatRecyclerAdapte.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = chatRecyclerAdapte.getItemCount();
                int lastVisiblePosition =
                        mLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) &&
                        lastVisiblePosition == (positionStart - 1))) {
                    rcvMessages.scrollToPosition(positionStart);
                }
            }
        });

        dbMessagingHis.child("TimeAgo").setValue(ServerValue.TIMESTAMP);
        dbMessagingMy.child("TimeAgo").setValue(ServerValue.TIMESTAMP);


        // for notification messages
//        DatabaseReference notifRef = dbrefnotifi.child(hisID).push();
//        notifRef.child("From").setValue(userID);

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerview();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat,menu);
        return true;
    }

    /// selected items menu:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int check = item.getItemId();
        switch(check) {

            case R.id.ic_videocall:
                onCallVideo();
                break;
            case R.id.ic_love:
                sendMessage(getString(R.string.i_love_you));
                break;

            case R.id.ic_abuse:
                dialogRateThisApp();
                break;

            default:
        }
        return super.onOptionsItemSelected(item);

    }

    private void dialogRateThisApp(){

        mViewInflate = getLayoutInflater().inflate(R.layout.dialog_abusecontent,null);
        TextView btnSentReport = mViewInflate.findViewById(R.id.btnSentReport);
        TextView btnCancel = mViewInflate.findViewById(R.id.btnCancel);
        final AlertDialog.Builder alertDialogBuilder = DialogUtils.CustomAlertDialog(mViewInflate,ChatUserActivity.this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        btnSentReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbReportAbuseOfContent.child(userID).child(hisID).setValue("Abusive");
                alertDialog.dismiss();
                Toast.makeText(ChatUserActivity.this, getString(R.string.send_succes), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

    }



    private void onCallVideo() {

        Intent callingVideoinetnt = new Intent(ChatUserActivity.this, loginVideoChatActivity.class);
        startActivity(callingVideoinetnt);

    }

    private void firebaseRecyclerview(){

        chatRecyclerAdapte = new FirebaseRecyclerAdapter<Chat, chatViewHolder>(

                Chat.class
                ,R.layout.pack_msg_chat
                ,ChatUserActivity.chatViewHolder.class
                ,dbrefMyChat

        ) {
            @Override
            protected void populateViewHolder(final chatViewHolder viewHolder, Chat model, int position) {

                final String list_msg_id = getRef(position).getKey();

                //Toast.makeText(ChatActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                dbrefMyChat.child(list_msg_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String msg = String.valueOf(dataSnapshot.child("message").getValue());
                        final String from = String.valueOf(dataSnapshot.child("from").getValue());
                        viewHolder.setTextMyMessage(msg);
                        dbrefUsers.child(from).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String username = String.valueOf(dataSnapshot.child("username").getValue());
                                viewHolder.setTexttvnamUser(username);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };

        rcvMessages.setAdapter(chatRecyclerAdapte);
        rcvMessages.getAdapter().notifyDataSetChanged();
        rcvMessages.getLayoutManager().scrollToPosition(rcvMessages.getAdapter().getItemCount());
        chatRecyclerAdapte.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int friendlyMessageCount = chatRecyclerAdapte.getItemCount();
                int lastVisiblePosition =
                        mLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) &&
                        lastVisiblePosition == (positionStart - 1))) {
                    rcvMessages.scrollToPosition(positionStart);
                }

            }
        });
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView tvMessage;
        TextView tvusername ;
        public chatViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            tvMessage =  mView.findViewById(R.id.txtMyMessage);
            tvusername = mView.findViewById(R.id.tvnameUser);
        }

        public void setTextMyMessage(String myMessage){
            tvMessage.setText(myMessage);
        }

        public void setTexttvnamUser(String myMessage){

            tvusername.setText(myMessage);
        }

    }


}
