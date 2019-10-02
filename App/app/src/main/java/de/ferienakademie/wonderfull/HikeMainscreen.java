package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class HikeMainscreen extends AppCompatActivity {

    public int ENERGY_LEVEL = 34;
    public int DRINK_STATUS = 10;
    public static final boolean GROUP = true;
    public int GROUP_STATUS = 0; //0=together, 1=someone is missing, 2=group lost
    public int factor = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(GROUP){
            setContentView(R.layout.activity_hike_mainscreen_with_group);
            ImageView group = (ImageView) findViewById(R.id.imageView3);
            TextView group_text = (TextView) findViewById(R.id.textView7);

            switchGroupIcon(group,group_text);

            group.setClickable(true);
            group.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GROUP_STATUS++;
                    GROUP_STATUS = GROUP_STATUS%3;
                    switchGroupIcon(group,group_text);
                }
            });

            factor = 4;

        } else {
            setContentView(R.layout.activity_hike_mainscreen_without_group);
            factor = 6;
        }

        ImageView energy = (ImageView) findViewById(R.id.imageView);
        View energy_bar = (View) findViewById(R.id.view);
        ImageView drop = (ImageView) findViewById(R.id.imageView2);
        View drink_bar = (View) findViewById(R.id.view2);

        TextView energy_text = (TextView) findViewById(R.id.textView3);
        TextView drink_text = (TextView) findViewById(R.id.textView4);
        TextView break_text = (TextView) findViewById(R.id.breaktext);
        TextView energy_headline = (TextView) findViewById(R.id.textView);
        TextView drink_headline = (TextView) findViewById(R.id.textView2);


        View.OnClickListener openEnergy = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HikeMainscreen.this, EnergyDetails.class);
                intent.putExtra("ENERGY_LEVEL", ""+ENERGY_LEVEL);
                startActivity(intent);
            };
        };

        View.OnClickListener openDrink = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HikeMainscreen.this, hydration_details.class);
                intent.putExtra("DRINK_STATUS", ""+DRINK_STATUS);
                startActivity(intent);
            };
        };

        energy_text.setClickable(true);
        energy_text.setOnClickListener(openEnergy);
        energy_headline.setClickable(true);
        energy_headline.setOnClickListener(openEnergy);

        drink_text.setClickable(true);
        drink_text.setOnClickListener(openDrink);
        drink_headline.setClickable(true);
        drink_headline.setOnClickListener(openDrink);

        drop.setClickable(true);
        drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DRINK_STATUS+=33;
                DRINK_STATUS = DRINK_STATUS%100;
                switchDrinkStatus(drink_bar, drop,drink_text);
            }
        });

        energy.setClickable(true);
        energy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ENERGY_LEVEL+=33;
                ENERGY_LEVEL = ENERGY_LEVEL%100;
                switchEnergyLevel(energy_bar,energy,energy_text,break_text);
            }
        });

        switchEnergyLevel(energy_bar, energy, energy_text, break_text);

        switchDrinkStatus(drink_bar, drop, drink_text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hike_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_emergency):
                Intent mainIntent = new Intent(this, EmergencyActivity.class);
                startActivity(mainIntent);
                return true;
            case (R.id.menu_profile):
                Intent profilIntent = new Intent(this, profile.class);
                startActivity(profilIntent);
                return true;
            case R.id.menu_plots:
                startActivity(new Intent(this, Graphen.class));
                return true;
            default:
                return false;
        }
    }

    public void switchEnergyLevel(View energy_bar, ImageView energy, TextView energy_text, TextView break_text){
        energy_text.setText(ENERGY_LEVEL+"%");
        break_text.setVisibility(View.INVISIBLE);

        int new_height = factor * ENERGY_LEVEL;

        if(new_height==0){
            energy_bar.getLayoutParams().height = 1;
        } else {
            energy_bar.getLayoutParams().height = new_height;
        }

        if(ENERGY_LEVEL >=66){
            energy_bar.setBackgroundColor(getResources().getColor(R.color.green));
        } else{
            if(ENERGY_LEVEL >=33){
                energy_bar.setBackgroundColor(getResources().getColor(R.color.orange));
            } else {
                energy_bar.setBackgroundColor(getResources().getColor(R.color.red));
                break_text.setVisibility(View.VISIBLE);
            }
        }
    }

    public void switchGroupIcon(ImageView group, TextView group_text){
        switch (GROUP_STATUS){
            case 0:
                group.setImageResource(R.drawable.group_ok);
                group_text.setText(R.string.group_good);
                break;
            case 1:
                group.setImageResource(R.drawable.someone_lost);
                group_text.setText(R.string.group_missing);
                break;
            case 2:
                group.setImageResource(R.drawable.group_lost);
                group_text.setText(R.string.group_lost);
                break;
            default:
                setContentView(R.layout.activity_hike_mainscreen_without_group);
        }
    }

    public void switchDrinkStatus(View drink_bar, ImageView drop, TextView drink_text){

        int new_height = factor * DRINK_STATUS;

        if(new_height == 0){
            drink_bar.getLayoutParams().height = 1;
        } else {
            drink_bar.getLayoutParams().height = new_height;
        }

        drink_bar.setBackgroundColor(getResources().getColor(R.color.blue));

        if(DRINK_STATUS >=66){
            drink_text.setText(R.string.drink_perfect);
        }else{
            if(DRINK_STATUS >=33){
                drink_text.setText(R.string.drink_good);
            }else{
                drink_text.setText(R.string.drink_bad);
            }
        }
    }
}
