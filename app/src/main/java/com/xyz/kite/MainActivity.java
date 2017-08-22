package com.xyz.kite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xyz.kites.Kite;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.kite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Kite.Builder(MainActivity.this)
                        .setLayoutRes(R.layout.kite)
                        .setDuration(2000)
                        .setWindowAnimations(R.style.AnimFromTop)
                        .setCallback(new Kite.Builder.Callback() {
                            @Override
                            public void onCreateView(View view) {
                                TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
                                tvContent.setText("onCreateView");
                            }

                            @Override
                            public void onDragDown(View view) {
                                Toast.makeText(MainActivity.this, "Drag down", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build()
                        .show();
            }
        });
    }
}
