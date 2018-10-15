package com.hellotrace;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.blyang.R.id.mystart;

public class GUIuse extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guiuse);

        Button button;
        button = (Button)findViewById(R.id.mystart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GUIuse.this,MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
