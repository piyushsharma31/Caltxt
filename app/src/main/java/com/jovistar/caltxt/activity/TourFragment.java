package com.jovistar.caltxt.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.jovistar.caltxt.R;

public class TourFragment extends Fragment {
    private static final String TAG = "ActivityTourPage";
    public static final String ARG_OBJECT = "object";

    TextView title, subject, footer;
    ImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        int i = args.getInt(ARG_OBJECT);
        View rootView = null;
        rootView = inflater.inflate(
                R.layout.tour_page, container, false);
        title = rootView.findViewById(R.id.tourpagetitle);
        subject = rootView.findViewById(R.id.tourpagesubject);
        footer = rootView.findViewById(R.id.tourpagefooter);
        image = rootView.findViewById(R.id.tourpageimage);
        if (i == 1) {
            title.setText(R.string.tour_title);
            subject.setText(R.string.tour_title_subject);
            footer.setText(R.string.tour_title_footer);
//			subject.setVisibility(View.GONE);
        } else if (i == 2) {
            title.setText(R.string.tour_title_identify);
            subject.setText(R.string.tour_title_identify_subject);
            footer.setText(R.string.tour_title_identify_footer);
            image.setImageResource(R.drawable.identify);
        } else if (i == 3) {
            title.setText(R.string.tour_title_block);
            subject.setText(R.string.tour_title_block_subject);
            footer.setText(R.string.tour_title_block_footer);
            image.setImageResource(R.drawable.callblock);
        } else if (i == 4) {
            title.setText(R.string.tour_title_calling);
            subject.setText(R.string.tour_title_calling_subject);
            footer.setText(R.string.tour_title_calling_footer);
            image.setImageResource(R.drawable.contextcalling);
        } else if (i == 5) {
            title.setText(R.string.tour_title_auto);
            subject.setText(R.string.tour_title_auto_subject);
            footer.setText(R.string.tour_title_auto_footer);
            image.setImageResource(R.drawable.autoresponse);
        } else if (i == 6) {
            title.setText(R.string.tour_title_missedcall);
            subject.setText(R.string.tour_title_missedcall_subject);
            footer.setText(R.string.tour_title_missedcall_footer);
            image.setImageResource(R.drawable.missedcallalert);
        } else if (i == 7) {
            title.setText(R.string.tour_title_triggers);
            subject.setText(R.string.tour_title_triggers_subject);
            footer.setText(R.string.tour_title_triggers_footer);
            image.setImageResource(R.drawable.triggers);
        }
        Log.i(TAG, "onCreateView " + i);
//		((Button) rootView.findViewById(R.id.button1))
//		.setText(Integer.toString(args.getInt(ARG_OBJECT)));
//		((TextView) rootView.findViewById(R.id.text1))
//				.setText(Integer.toString(args.getInt(ARG_OBJECT)));
        return rootView;
    }
}
