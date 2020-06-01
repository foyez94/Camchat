package com.rifcode.camchat.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifcode.camchat.R;
import com.rifcode.camchat.utils.SinchService;
import com.sinch.android.rtc.calling.Call;

public class PlaceActivity extends BaseActivity {

    private ImageView mCallButton;
    private TextView mCallName;
    private DatabaseReference dbUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_video);

        //initializing UI elements
        mCallName = findViewById(R.id.callName);
        mCallButton =  findViewById(R.id.callButton);
        mCallButton.setEnabled(false);
        mCallButton.setOnClickListener(buttonClickListener);

        dbUser = FirebaseDatabase.getInstance().getReference().child("Users");
        dbUser.child(ChatUserActivity.hisID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = String.valueOf(dataSnapshot.child("username").getValue());
                mCallName.setText(username);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


        Button stopButton =  findViewById(R.id.stopButton);
        stopButton.setOnClickListener(buttonClickListener);


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // invoked when the connection with SinchServer is established
    @Override
    protected void onServiceConnected() {
        TextView userName =  findViewById(R.id.loggedInName);
        userName.setText(getSinchServiceInterface().getUserName());
        mCallButton.setEnabled(true);
    }

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }

    //to kill the current session of SinchService
    private void stopButtonClicked() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        finish();
    }

//    public void incrementCounter() {
//        DatabaseReference db = classFirebase.dbrefCountRef().child(ChatActivity.user_id_visit).child("count");
//        db.runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(MutableData mutableData) {
//                if (mutableData.getValue() == null) {
//                    mutableData.setValue(1);
//                } else {
//                    mutableData.setValue((Long) mutableData.getValue() + 1);
//                }
//                return Transaction.success(mutableData);
//            }
//
//            @Override
//            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
//            }
//        });
//    }

    //to place the call to the entered name
    private void callButtonClicked() {
        String userName = mCallName.getText().toString();

        if (userName.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_username_logincalling, Toast.LENGTH_LONG).show();
            return;
        }

        Call call = getSinchServiceInterface().callUserVideo(userName);
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);

        //incrementCounter();
    }


    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.callButton:
                    callButtonClicked();
                    break;

                case R.id.stopButton:
                    stopButtonClicked();
                    break;

            }
        }
    };

}
