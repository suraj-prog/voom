package com.example.voom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navView;
    RecyclerView myContactsList;
    ImageView findPeopleBtn;
    private DatabaseReference  contactRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String userName="", profileImage="", calledBy="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        navView = findViewById(R.id.nav_view);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        findPeopleBtn = findViewById(R.id.find_people_btn);
        myContactsList = findViewById(R.id.contact_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inP = new Intent(MainActivity.this,FindPeopleActivity.class);
                startActivity(inP);
            }
        });
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    Intent in = new Intent(MainActivity.this,MainActivity.class);
                    startActivity(in);
                    break;
                case R.id.navigation_settings:
                    Intent inte = new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(inte);
                    break;
                case R.id.navigation_notifications:
                    Intent inx = new Intent(MainActivity.this,NotificationsActivity.class);
                    startActivity(inx);
                    break;
                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent inc = new Intent(MainActivity.this,RegisterActivity.class);
                    startActivity(inc);
                    finish();
                    break;
            }
            return true;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        checkForRecievingCall();
        validateUser();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactRef.child(currentUserId),Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, MainViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, MainViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MainViewHolder holder, int position, @NonNull Contacts model)
            {
                final String listUserId = getRef(position).getKey();
                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                          if(snapshot.exists())
                          {
                                 userName = snapshot.child("name").getValue().toString();
                                 profileImage = snapshot.child("image").getValue().toString();
                                 holder.userNameTxt.setText(userName);
                                 Picasso.get().load(profileImage).into(holder.profileImageView);
                          }
                          holder.callBtn.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v)
                              {
                                  Intent  ce = new Intent (MainActivity.this, CallingActivity.class);
                                  ce.putExtra("visit_user_id",listUserId);
                                  startActivity(ce);
                              }
                          });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                MainViewHolder viewHolder = new MainViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkForRecievingCall()
    {
        userRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.hasChild("ringing"))
                {
                    calledBy = snapshot.child("ringing").getValue().toString();
                    Intent  ce = new Intent (MainActivity.this, CallingActivity.class);
                    ce.putExtra("visit_user_id",calledBy);
                    startActivity(ce);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTxt;
        Button callBtn;
        ImageView profileImageView;
        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.name_contact);
            callBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);

        }
    }
    private void validateUser()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    Intent  se = new Intent (MainActivity.this, SettingsActivity.class);
                    startActivity(se);
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}