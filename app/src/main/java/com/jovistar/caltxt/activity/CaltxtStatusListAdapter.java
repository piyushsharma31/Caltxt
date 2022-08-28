package com.jovistar.caltxt.activity;

import android.app.Activity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.phone.Addressbook;

import java.util.Iterator;
import java.util.List;

public class CaltxtStatusListAdapter extends ArrayAdapter<CaltxtStatus> {
    private static final String TAG = "CaltxtStatusListAdapter";

    private final SparseBooleanArray selectionList;
    private final List<CaltxtStatus> list;
    private final IFTTTRuleWizard context;

    public CaltxtStatusListAdapter(Activity context, List<CaltxtStatus> list) {
        super(context, R.layout.profile_status_listitem, list);
        this.context = (IFTTTRuleWizard) context;
        this.list = list;
        this.selectionList = new SparseBooleanArray(list.size());
    }

    @Override
    public int getCount() {
        return list.size();
    }

    // This should return a data object, not an int
    @Override
    public CaltxtStatus getItem(int position) {
        return list.get(position);
    }

    int getItemPosition(String name) {
        int i = 0;
        for (Iterator<CaltxtStatus> it = list.iterator(); it.hasNext(); ) {
            final CaltxtStatus sts = it.next();
//			if(sts.getStatusName().equals(name)) {
            if (sts.getStatusCode().equals(name)) {
                break;
            }
            i++;
        }
        return i;
    }

    public void toggleSelection(int position) {
        selectView(position, !selectionList.get(position));
    }

    private void selectView(int position, boolean value) {
        if (value)
            selectionList.put(position, value);
        else
            selectionList.delete(position);
        notifyDataSetChanged();
    }

    public void removeSelection() {
        selectionList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder {
        protected TextView name;
        protected TextView code;
        protected ImageButton flag;
        //        protected View divider;
        int position;
        View rowView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.profile_status_listitem, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.rowView = view.findViewById(R.id.listItemView);
            viewHolder.name = view.findViewById(R.id.activity_profile_status_label);
            viewHolder.code = view.findViewById(R.id.activity_profile_status_subject);
            viewHolder.flag = view.findViewById(R.id.activity_profile_status_icon);
//            viewHolder.divider = (View) view.findViewById(R.id.divider);
            viewHolder.position = position;
            view.setTag(viewHolder);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    ViewHolder holder = (ViewHolder) arg0.getTag();
                    CaltxtStatus c = list.get(holder.position);
                    if (c.getStatusName().equals("Automatic Response")) {

                    } else if (holder.position == Addressbook.PLACE_INDEX_IN_STATUS) {

                    } else {

                    }
                    context.setListValue(c);
//		            context.finish();
                }
            });
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(list.get(position).getStatusName());
        holder.code.setText(list.get(position).getStatusCode());
        holder.flag.setImageResource(list.get(position).getResourceID());
        holder.position = position;
        if (selectionList.get(position)) {
            holder.rowView.setSelected(true);
//			view.setBackgroundResource(R.color.lightgreen_transparent_50pc);
        } else {
            holder.rowView.setSelected(false);
//        	view.setBackgroundResource(android.R.color.transparent);
        }
        /*if(list.get(position).getStatusName().equals(Addressbook.getMyProfile().getPlace())) {
            holder.flag.setBackgroundResource(Addressbook.get().geStatusBackgroundResource(Addressbook.getMyProfile()));
        } else {
        	if(list.get(position).getStatusName().equals("Automatic Response")
        			&& Addressbook.getMyProfile().isAutoResponding()) {
        		holder.flag.setBackgroundResource(R.drawable.circle_busy);
        	} else {
        		holder.flag.setBackgroundResource(R.drawable.circle_grey);
        	}
        }*/

//        if(position==3) {
//        	holder.divider.setVisibility(View.VISIBLE);
//        } else {
//        	holder.divider.setVisibility(View.GONE);
//        }
        return view;
    }
/*
    public CaltxtStatusListAdapter(Context context, List<String> items, List<Integer> images) {
		super(context, android.R.layout.select_dialog_item, items);
		this.images = images;
	}

	public CaltxtStatusListAdapter(Context context, String[] items, Integer[] images) {
		super(context, android.R.layout.select_dialog_item, items);
		this.images = Arrays.asList(images);
	}

	public CaltxtStatusListAdapter(Context context, int items, int images) {
		super(context, android.R.layout.select_dialog_item, context
				.getResources().getStringArray(items));

		final TypedArray imgs = context.getResources().obtainTypedArray(images);
		this.images = new ArrayList<Integer>() {
			{
				for (int i = 0; i < imgs.length(); i++) {
					add(imgs.getResourceId(i, -1));
				}
			}
		};

		// recycle the array
		imgs.recycle();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView textView = (TextView) view.findViewById(android.R.id.text1);

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//			textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
//					images.get(position), 0, 0, 0);
//		} else {
			textView.setCompoundDrawablesWithIntrinsicBounds(
					images.get(position), 0, 0, 0);
//		}
		textView.setCompoundDrawablePadding((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources()
						.getDisplayMetrics()));
		return view;
	}*/
}
