package com.rifcode.camchat.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifcode.camchat.R;
import com.rifcode.camchat.utils.SinchService;
import com.sinch.android.rtc.SinchError;

public class loginVideoChatActivity extends BaseActivity implements SinchService.StartFailedListener {


    private Button mLoginButton;
    private TextView mLoginName;
    private ProgressDialog mSpinner;
    private DatabaseReference dbUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_video_chat);

        dbUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        //asking for permissions here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA
                    , android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.READ_PHONE_STATE},100);
        }

        //initializing UI elements
        mLoginName = findViewById(R.id.loginName);
        mLoginButton =  findViewById(R.id.loginButton);
        mLoginButton.setEnabled(false);

        dbUsers.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLoginName.setText(String.valueOf(dataSnapshot.child("username").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClicked();
            }
        });
    }



    //this method is invoked when the connection is established with the SinchService
    @Override
    protected void onServiceConnected() {
        mLoginButton.setEnabled(true);
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    protected void onPause() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        super.onPause();

    }


    @Override
    public void onStartFailed(SinchError error) {
       // Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();



    }

    //Invoked when just after the service is connected with Sinch
    @Override
    public void onStarted() {
        openPlaceCallActivity();
    }

    //Login is Clicked to manually to connect to the Sinch Service
    private void loginClicked() {
        String userName = mLoginName.getText().toString();

        if (userName.isEmpty()) {
            //Toast.makeText(this, "Please enter a username", Toast.LENGTH_LONG).show();
            return;
        }

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(userName);
            showSpinner();
        } else {
            openPlaceCallActivity();
        }
    }


    //Once the connection is made to the Sinch Service, It takes you to the next activity where you enter the name of the user to whom the call is to be placed
    private void openPlaceCallActivity() {
        Intent mainActivity = new Intent(this, PlaceActivity.class);
        startActivity(mainActivity);
    }

    private void showSpinner() {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle(getString(R.string.loggin_in_call));
        mSpinner.setMessage(getString(R.string.please_wait_connect));
        mSpinner.show();
    }
}
