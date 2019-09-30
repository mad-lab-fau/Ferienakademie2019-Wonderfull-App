package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class HikeMainscreen extends AppCompatActivity {

    public static final int ENERGY_LEVEL = 22;
    public static final int DRINK_STATUS = 70;
    public static final boolean GROUP = true;
    public static final int GROUP_STATUS = 0; //0=together, 1=someone is missing, 2=group lost

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(GROUP){
            setContentView(R.layout.activity_hike_mainscreen_with_group);
            ImageView group = (ImageView) findViewById(R.id.imageView3);
            TextView group_text = (TextView) findViewById(R.id.textView7);

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

        } else {
            setContentView(R.layout.activity_hike_mainscreen_without_group);
        }

        ImageView battery = (ImageView) findViewById(R.id.imageView);
        ImageView drop = (ImageView) findViewById(R.id.imageView2);
        TextView energy_text = (TextView) findViewById(R.id.textView3);
        TextView drink_text = (TextView) findViewById(R.id.textView4);
        TextView break_text = (TextView) findViewById(R.id.breaktext);
        energy_text.setText(ENERGY_LEVEL+"%");
        break_text.setVisibility(View.INVISIBLE);

        if(ENERGY_LEVEL >=90){
            battery.setImageResource(R.drawable.battery10);
        } else{
            if(ENERGY_LEVEL >=80){
                battery.setImageResource(R.drawable.battery9);
            } else {
                if (ENERGY_LEVEL >= 70) {
                    battery.setImageResource(R.drawable.battery8);
                } else {
                    if (ENERGY_LEVEL >= 60) {
                        battery.setImageResource(R.drawable.battery7);
                    } else {
                        if (ENERGY_LEVEL >= 50) {
                            battery.setImageResource(R.drawable.battery6);
                        } else {
                            if (ENERGY_LEVEL >= 40) {
                                battery.setImageResource(R.drawable.battery5);
                            } else {
                                if (ENERGY_LEVEL >= 30) {
                                    battery.setImageResource(R.drawable.battery4);
                                } else {
                                    if (ENERGY_LEVEL >= 20) {
                                        battery.setImageResource(R.drawable.battery3);
                                        break_text.setVisibility(View.VISIBLE);
                                    } else {
                                        if (ENERGY_LEVEL >= 10) {
                                            battery.setImageResource(R.drawable.battery2);
                                            break_text.setVisibility(View.VISIBLE);
                                        } else {
                                                battery.setImageResource(R.drawable.battery1);
                                                break_text.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        if(DRINK_STATUS >=90){
            drop.setImageResource(R.drawable.drop10);
        }  else {
            if (DRINK_STATUS >= 80) {
                drop.setImageResource(R.drawable.drop9);
            } else {
                if (DRINK_STATUS >= 70) {
                    drop.setImageResource(R.drawable.drop8);
                } else {
                    if (DRINK_STATUS >= 60) {
                        drop.setImageResource(R.drawable.drop7);
                    } else {
                        if (DRINK_STATUS >= 50) {
                            drop.setImageResource(R.drawable.drop6);
                        } else {
                            if (DRINK_STATUS >= 40) {
                                drop.setImageResource(R.drawable.drop5);
                            } else {
                                if (DRINK_STATUS >= 30) {
                                    drop.setImageResource(R.drawable.drop4);
                                } else {
                                    if (DRINK_STATUS >= 20) {
                                        drop.setImageResource(R.drawable.drop3);
                                    } else {
                                        if (DRINK_STATUS >= 10) {
                                            drop.setImageResource(R.drawable.drop2);
                                        } else {
                                            if (DRINK_STATUS > 0) {
                                                drop.setImageResource(R.drawable.drop1);
                                            } else {
                                                drop.setImageResource(R.drawable.drop);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

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
