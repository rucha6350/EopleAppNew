package rucha.sawant.eopleapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import rucha.sawant.eopleapp.Adapter.DataAdapter;
import rucha.sawant.eopleapp.Model.Data;

import static java.security.AccessController.getContext;

public class Main extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<Data> listItems;
    private DataAdapter adapter;
    private RecyclerView recyclerView;
    private Navi_drawer navi_drawer;
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private ImageView profiledash, logout, info, back;
    private CircleImageView profilepic;
    private TextView firstname, lastname, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        profiledash = findViewById(R.id.profile_dash);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        listItems = new ArrayList<>();
        mDrawerLayout = findViewById(R.id.naviga);
        navigationView = findViewById(R.id.nav_view);
        navi_drawer = new Navi_drawer();
        navi_drawer.nav(mDrawerLayout, navigationView);
        SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
        View prolayview = findViewById(R.id.prolay);
        profilepic = prolayview.findViewById(R.id.profile);
        firstname = prolayview.findViewById(R.id.first_name);
        lastname = prolayview.findViewById(R.id.last_name);
        email = prolayview.findViewById(R.id.email);
        logout = prolayview.findViewById(R.id.logout);
        info = prolayview.findViewById(R.id.info);
        back = prolayview.findViewById(R.id.back);
        String name = preferences.getString("name","");
        String namearr[] = name.split(" ");
        firstname.setText(namearr[0]);
        lastname.setText(namearr[1]);
        email.setText(preferences.getString("email",""));
        Log.v("NAME", name);

        Glide.with(this).load(preferences.getString("profilepic","")).into(profilepic);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Main.this);
// ...Irrelevant code for customizing the buttons and title
                LayoutInflater inflater = Main.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.alert, null);
                dialogBuilder.setView(dialogView);

                TextView yes = dialogView.findViewById(R.id.logout_yes);
                TextView no = dialogView.findViewById(R.id.logout_no);

                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

                lp.copyFrom(alertDialog.getWindow().getAttributes());
                lp.width = 300;
                lp.height = 500;
                lp.x=70;
                lp.y=100;
                alertDialog.getWindow().setAttributes(lp);

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(AccessToken.getCurrentAccessToken() == null){
                            FirebaseAuth.getInstance().signOut();
                        }
                        else {
                            LoginManager.getInstance().logOut();
                            AccessToken.setCurrentAccessToken(null);
                        }

                        Intent intent = new Intent(Main.this, Splash.class);
                        startActivity(intent);
                        finish();
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });


            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Main.this, Info.class);
                startActivity(intent);
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(Gravity.END);
            }
        });


        profiledash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerclick(view);
            }
        });

        loadData();
    }

    public void drawerclick(View view){
        mDrawerLayout.openDrawer(Gravity.END);

    }

    private void loadData(){
        listItems.clear();
        db.collection("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Data item = new Data(document.getData().get("cost")+"", document.getData().get("type")+"");
                                listItems.add(item);
                                adapter = new DataAdapter(listItems, getApplicationContext());
                                recyclerView.setAdapter(adapter);

                            }

                        } else {
                            Log.d("CHECK", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}

