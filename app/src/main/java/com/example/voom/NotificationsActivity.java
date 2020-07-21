package com.example.voom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationsActivity extends AppCompatActivity {
private RecyclerView notifications_list;
private DatabaseReference friendRequestRef, contactRef, userRef;
private FirebaseAuth mAuth;
private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        notifications_list = findViewById(R.id.notifications_list);
        notifications_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
       FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(friendRequestRef.child(currentUserId), Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationsViewHolder holder, int position, @NonNull Contacts model) {
              holder.acceptBtn.setVisibility(View.VISIBLE);
              holder.cancleBtn.setVisibility(View.VISIBLE);
              final String listUserId = getRef(position).getKey();
              DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();
              requestTypeRef.addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                      if(snapshot.exists())
                      {
                          String type = snapshot.getValue().toString();
                          if(type.equals("received"))
                          {
                              holder.cardView.setVisibility(View.VISIBLE);
                              userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot snapshot)
                                  {
                                    if(snapshot.hasChild("image"))
                                    {
                                        final String imageStr = snapshot.child("image").getValue().toString();
                                        Picasso.get().load(imageStr).into(holder.profileImageView);
                                    }
                                     final String nameStr = snapshot.child("name").getValue().toString();
                                     holder.userNameTxt.setText(nameStr);

                                    holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            contactRef.child(currentUserId).child(listUserId)
                                                    .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        contactRef.child(listUserId).child(currentUserId)
                                                                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                friendRequestRef.child(listUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            Toast.makeText(NotificationsActivity.this,"New Contact Saved",Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                      holder.cancleBtn.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              friendRequestRef.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<Void> task) {
                                                      if(task.isSuccessful())
                                                      {
                                                          friendRequestRef.child(listUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                              @Override
                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                  if(task.isSuccessful())
                                                                  {
                                                                      Toast.makeText(NotificationsActivity.this,"Friend Request Cancelled",Toast.LENGTH_SHORT).show();
                                                                  }
                                                              }
                                                          });
                                                      }
                                                  }
                                              });
                                          }
                                      });
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError error)
                                  {

                                  }
                              });

                          }else
                          {
                              holder.cardView.setVisibility(View.GONE);
                          }
                      }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {

                  }
              });
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
                View view = LayoutInflater.from(p.getContext()).inflate(R.layout.find_friend_design,p,false);
                NotificationsViewHolder viewHolder = new NotificationsViewHolder(view);
                return viewHolder;
            }
        };
        notifications_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTxt;
        Button acceptBtn, cancleBtn;
        ImageView profileImageView;
        RelativeLayout cardView;
        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.name_notification);
            acceptBtn = itemView.findViewById(R.id.request_accept_btn);
            cancleBtn = itemView.findViewById(R.id.request_decline_btn);
            profileImageView = itemView.findViewById(R.id.image_notification);
            cardView = itemView.findViewById(R.id.card_view);

        }
    }
   /* private void CancelFriendRequest()
    {

    }*/
   /* private void AcceptFriendRequest()
    {

    }*/

}