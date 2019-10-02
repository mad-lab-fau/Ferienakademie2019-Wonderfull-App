package de.ferienakademie.wonderfull;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public interface HeightChangeCallback
{
    void onHeightChanged(ArrayList<Entry> data);
}
