package com.jovistar.caltxt.network.voice;

/**
 * Created by jovika on 10/10/2017.
 */

public class PlaceCellStrength {
    public int strength = 0;
    public String place;
    public String cellid;

    public PlaceCellStrength(String p, int s, String c) {
        this.strength = s;
        this.cellid = c;
        this.place = p;
    }

    public String toString() {
        return cellid + "@" + place + " has strength " + strength;
    }
}
