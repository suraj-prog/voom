package com.example.voom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.session.MediaSession;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Stream;
import com.opentok.android.Session;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements com.opentok.android.Session.SessionListener, PublisherKit.PublisherListener
{
private static String API_KEY = "46815554";
private static String SESSION_ID = "2_MX40NjgxNTU1NH5-MTU5MzM0MzgyODcyN34wcXJ0R3VDdy9ibzNyOVJGajRSN2lFZWp-fg";
private static String TOKEN = "T1==cGFydG5lcl9pZD00NjgxNTU1NCZzaWc9NzEyY2YzZGJiNTBiMjk2MjAxZGQwYTE3NzVjOGQ4NDEwMzljZjk1MTpzZXNzaW9uX2lkPTJfTVg0ME5qZ3hOVFUxTkg1LU1UVTVNek0wTXpneU9EY3lOMzR3Y1hKMFIzVkRkeTlpYnpOeU9WSkdhalJTTjJsRlpXcC1mZyZjcmVhdGVfdGltZT0xNTkzMzQzOTYzJm5vbmNlPTAuNzcxNjE5NjIxNjAwMjkwMiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTk1OTM1OTYzJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
private static final int RC_VIDEO_APP_PERM = 124 ;
private ImageView closeVideochatBtn;
private DatabaseReference usersRef;
private String userID = "";
private FrameLayout mPublisherViewController;
private FrameLayout mSubscriberViewController;
private Session mSession;
private Publisher mPublisher;
private Subscriber mSubscriber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        closeVideochatBtn = findViewById(R.id.close_video_chat_button);
        closeVideochatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
              usersRef.addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot)
                  {
                    if(snapshot.child(userID).hasChild("Ringing"))
                  {
                      usersRef.child(userID).child("Ringing").removeValue();
                      if(mPublisher != null)
                      {
                          mPublisher.destroy();
                      }
                      if(mSubscriber != null)
                      {
                          mSubscriber.destroy();
                      }
                      startActivity(new Intent(VideoChatActivity.this,RegisterActivity.class));
                      finish();
                  }
                      if(snapshot.child(userID).hasChild("Calling"))
                      {
                          usersRef.child(userID).child("Calling").removeValue();
                          if(mPublisher != null)
                          {
                              mPublisher.destroy();
                          }
                          if(mSubscriber != null)
                          {
                              mSubscriber.destroy();
                          }
                          startActivity(new Intent(VideoChatActivity.this,RegisterActivity.class));
                          finish();
                      }else
                      {
                          startActivity(new Intent(VideoChatActivity.this,RegisterActivity.class));
                          finish();
                      }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error)
                  {

                  }
              });
            }
        });
        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission()
    {
         String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
         if(EasyPermissions.hasPermissions(this,perms))
         {

                 mPublisherViewController = findViewById(R.id.publisher_container);
                 mSubscriberViewController = findViewById(R.id.subscriber_container);

                 //1. initialize and connect to the session
                mSession = new Session.Builder(this,API_KEY,SESSION_ID).build();
                mSession.setSessionListener(VideoChatActivity.this);
                mSession.connect(TOKEN);

         }else
         {
             EasyPermissions.requestPermissions(this,"Hey this application needs permissions Please Allow",RC_VIDEO_APP_PERM);
         }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
//2. Publishing a Stream to the session
    @Override
    public void onConnected(com.opentok.android.Session session) {
        Log.i(LOG_TAG,"Session Connected");
        mPublisher.swapCamera();
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);
        mPublisherViewController.addView(mPublisher.getView());
        if(mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(com.opentok.android.Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");

    }
//3. Subscribing to the streams
    @Override
    public void onStreamReceived(com.opentok.android.Session session, Stream stream)
    {
         Log.i(LOG_TAG,"Stream Received");
         if(mSubscriber == null)
         {
             mSubscriber = new Subscriber.Builder(this,stream).build();
             mSession.subscribe(mSubscriber);
             mSubscriberViewController.addView(mSubscriber.getView());
         }
    }

    @Override
    public void onStreamDropped(com.opentok.android.Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");
        if(mSubscriber != null)
        {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }

    }

    @Override
    public void onError(com.opentok.android.Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Streaming Error");
    }
}