package rucha.sawant.eopleapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Transaction_status extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_status);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Transaction_status.this, Main.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
