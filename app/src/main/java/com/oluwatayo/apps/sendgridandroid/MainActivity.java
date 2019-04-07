package com.oluwatayo.apps.sendgridandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;
import com.sendgrid.SendGridResponseCallback;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements SendGridResponseCallback {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        SendGrid sendGrid = new SendGrid(BuildConfig.SendgridKey); //Replace with your sendgrid api key
        sendGrid.setSendgridResponseCallbacks(this);
        SendGrid.Email email = new SendGrid.Email().addBcc("olu@olu.com")
                .addTo("itkalasado@gmail.com", "tolu ma")
                .setSubject("Test test")
                .setReplyTo("no-reply@gmail.com", "no-reply")
                .setFrom("ade@tmail.com", "tayo")
                .setHtml("<strong>Hi</strong>")
                .setText("Text");

        try {
            sendGrid.send(email);
        } catch (SendGridException e) {
            Toast.makeText(this, "ex: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.d("LOG", e.getLocalizedMessage());
        }

    }

    @Override
    public void onMailSendSuccess(SendGrid.Response response) {
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
        Log.e("LOG", "" + response.getCode());
        textView.setText(String.valueOf(response.getCode()));
    }


    @Override
    public void onMailSendFailed(SendGrid.Response response) {
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
        Log.e("LOG", "" + response.getCode());
        textView.setText(String.valueOf(response.getCode()));
    }
}
