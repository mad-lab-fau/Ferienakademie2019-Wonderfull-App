package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import de.ferienakademie.wonderfull.ProfileWrapper;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    private ProfileWrapper profileDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Button button = findViewById(R.id.uploadProfileImage);
        button.setOnClickListener(this);

        profileDB = new ProfileWrapper(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {      // wird fuer alle buttons ausgefuehrt, daher switch
            case R.id.uploadProfileImage:
                // button action fuer upload profile image ...
                break;
        }
    }


}
