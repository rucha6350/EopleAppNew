package rucha.sawant.eopleapp;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Splash extends AppCompatActivity {

    CallbackManager callbackManager;
    ImageView fb, gl;
    int RC_SIGN_IN = 5;
    private static final String EMAIL = "email";
    LoginButton loginButton;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    String newstring;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        fb = findViewById(R.id.fb);
        gl = findViewById(R.id.google_login);
        mAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile",EMAIL));

        if (ContextCompat.checkSelfPermission(Splash.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Splash.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        if(isFacebookLoggedIn() || isGoogleLoggedIn()){
            nextActivity();
        }
        else {
            FacebookSdk.sdkInitialize(getApplicationContext());
            AppEventsLogger.activateApp(this,"196782664575260");
            callbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {

                            final Bundle params = new Bundle();
                            params.putString("fields", "name,email,gender,picture.type(large)");
                            new GraphRequest(AccessToken.getCurrentAccessToken(), "me", params, HttpMethod.GET,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            if (response != null) {
                                                try {
                                                    JSONObject data = response.getJSONObject();
                                                    if (data.has("picture")) {
                                                        String profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                                        String name = data.getString("name");
                                                        String mail = data.getString("email");
                                                        Log.v("THE EMAIL IS:", mail);
                                                        SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.putString("name", name);
                                                        editor.putString("email", mail);
                                                        editor.putString("profilepic", profilePicUrl);
                                                        editor.apply();
                                                        nextActivity();

                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(Splash.this,"EXCEPTION",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    }).executeAsync();


                        }

                        @Override
                        public void onCancel() {
                            // App code
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            // App code
                        }
                    });





        }

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.performClick();
            }
        });

        gl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                Log.d("GOOGLE", "Intry block");
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("GOOGLE", "Google sign in failed", e);
                // ...
            }
        }

    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("GOOGLE", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("GOOGLE", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String name = user.getDisplayName();
                            for (UserInfo profile : user.getProviderData()) {

                                // Get the profile photo's url
                                Uri photoUrl = profile.getPhotoUrl();

                                // Variable holding the original String portion of the url that will be replaced
                                String originalPieceOfUrl = "s96-c/photo.jpg";

                                // Variable holding the new String portion of the url that does the replacing, to improve image quality
                                String newPieceOfUrlToAdd = "s400-c/photo.jpg";
                                String photoPath = photoUrl.toString();

                                // Replace the original part of the Url with the new part
                                newstring = photoPath.replace(originalPieceOfUrl, newPieceOfUrlToAdd);

                                // Check if the Url path is null
                            }// End if

                            Log.v("THE PHOTO URL IS: ", newstring);
                            String mail = user.getEmail();
                            String username = mail;
                            SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("name", name);
                            editor.putString("profilepic", newstring);
                            editor.putString("email", mail);
                            editor.apply();

                            nextActivity();


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("GOOGLE", "signInWithCredential:failure", task.getException());

                        }

                        // ...
                    }
                });
    }

    private boolean isFacebookLoggedIn(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && !accessToken.isExpired();

    }

    private boolean isGoogleLoggedIn(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.v("GOOGLE ch",(currentUser != null )+"");
        return currentUser != null;
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void nextActivity(){
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish();
    }
}
