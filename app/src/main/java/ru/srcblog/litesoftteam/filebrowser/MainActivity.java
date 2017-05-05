package ru.srcblog.litesoftteam.filebrowser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    MainActivity main;
    final int ANSWER_BROWSER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = this;
        Button b1 = (Button) findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(main,jFileBrowser.class);
                i.putExtra("mode", jFileBrowser.MODE_SAVE);
                //i.putExtra("filter","rc");
                startActivityForResult(i,ANSWER_BROWSER);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ANSWER_BROWSER) {
                Toast.makeText(main, "Selected file: " + data.getStringExtra("file"), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
