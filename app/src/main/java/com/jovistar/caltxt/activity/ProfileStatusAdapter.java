package com.jovistar.caltxt.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jovistar.caltxt.R;
import com.jovistar.commons.bo.XMob;

public class ProfileStatusAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public ProfileStatusAdapter(Context context, String[] values) {
        super(context, R.layout.profile_status_listitem, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.profile_status_listitem, parent, false);
        TextView textView = rowView.findViewById(R.id.activity_profile_status_label);
        ImageView imageView = rowView.findViewById(R.id.activity_profile_status_icon);
        textView.setText(values[position]);
        textView.setTextColor(Color.GRAY);
        // Change the icon for Windows and iPhone
        String s = values[position];
        if (s.equals(XMob.STRING_STATUS_DND)) {
            imageView.setImageResource(R.drawable.ic_donotdisturb_white_24dp);
        } else if (s.equals(XMob.STRING_STATUS_AVAILABLE)) {
            imageView.setImageResource(R.drawable.ic_available_white_24dp);
        } else if (s.equals(XMob.STRING_STATUS_AWAY)) {
            imageView.setImageResource(R.drawable.ic_away_white_24dp);
        } else if (s.equals(XMob.STRING_STATUS_BUSY)) {
            imageView.setImageResource(R.drawable.ic_busy_white_24dp);
        }

        return rowView;
    }
}
