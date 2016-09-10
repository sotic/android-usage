package com.me.app.edittextdemo.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.me.app.edittextdemo.R;


public class MainActivity extends AppCompatActivity {

    private EditText et_paste_disabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_paste_disabled = (EditText) findViewById(R.id.et_paste_disabled);
        et_paste_disabled.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
