package com.jovistar.caltxt.activity;

import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.bo.XRul;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;

import java.util.ArrayList;

public class IFTTTListAdapter extends ArrayAdapter<XRul> implements Filterable {
    private static final String TAG = "ActivityIFTTTLstAdapter";

    private final IFTTT context;
    protected ArrayList<XRul> iftttListOriginal;
    private final SparseBooleanArray selectionList;

    public IFTTTListAdapter(IFTTT context, ArrayList<XRul> items) {
        super(context, R.layout.ifttt_list_item, items);
        this.context = context;
        this.iftttListOriginal = items;
        this.selectionList = new SparseBooleanArray(items.size());
    }

    // For this helper method, return based on filteredData
    @Override
    public int getCount() {
        return iftttListOriginal.size();
    }

    // This should return a data object, not an int
    @Override
    public XRul getItem(int position) {
        return iftttListOriginal.get(position);
    }

    public XRul remove(int position) {
        return iftttListOriginal.remove(position);
    }

    public void add(int position, XRul rul) {
        iftttListOriginal.add(position, rul);
    }

    public void refresh() {
        iftttListOriginal.clear();
        ArrayList<XRul> newlist = Persistence.getInstance(context).getAllXRUL();
        iftttListOriginal.addAll(newlist);
    }

    static class ViewHolder {
        TextView ifttt_header_text_IF, ifttt_header_IF_description, ifttt_header_text_THEN, ifttt_header_THEN_description;
        ImageButton ifttt_header_icon_IF, ifttt_header_icon_THEN;
        ImageView ifttt_header_icon_IF_overlay, ifttt_repeat_icon;
        TextView ifttt_mid_text;
        androidx.appcompat.widget.SwitchCompat ifttt_switch;
        //		AppCompatImageButton ifttt_delete_button;
        View rowView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder will buffer the assess to the individual fields of the row
        // layout

        final ViewHolder holder;
        // Recycle existing view if passed as parameter
        // This will save memory and time on Android
        // This only works if the base layout for all classes are the same
        View rowView = convertView;
        final XRul rowitem = getItem(position);

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.ifttt_list_item, null, true);
            holder = new ViewHolder();
            holder.rowView = rowView.findViewById(R.id.iftttCardView);
            holder.ifttt_header_icon_IF = rowView.findViewById(R.id.ifttt_header_icon_IF);
            holder.ifttt_header_icon_THEN = rowView.findViewById(R.id.ifttt_header_icon_THEN);
            holder.ifttt_header_text_IF = rowView.findViewById(R.id.ifttt_header_text_IF);
            holder.ifttt_header_IF_description = rowView.findViewById(R.id.ifttt_header_IF_description);
            holder.ifttt_header_text_THEN = rowView.findViewById(R.id.ifttt_header_text_THEN);
            holder.ifttt_header_THEN_description = rowView.findViewById(R.id.ifttt_header_THEN_description);
            holder.ifttt_mid_text = rowView.findViewById(R.id.ifttt_mid_text);
            holder.ifttt_switch = rowView.findViewById(R.id.ifttt_switch);
            holder.ifttt_repeat_icon = rowView.findViewById(R.id.ifttt_repeat_icon);
            holder.ifttt_header_icon_IF_overlay = rowView.findViewById(R.id.overlay_lefside);

//			Drawable d1 = context.getResources().getDrawable(R.drawable.ic_repeat_one_white_24dp);
//			Drawable wrappedDrawable = DrawableCompat.wrap(d1);
//			wrappedDrawable = wrappedDrawable.mutate();
//			DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(R.color.grey));
//			d1.invalidateSelf();
//			holder.ifttt_repeat_icon.setImageDrawable(d1);
            holder.ifttt_repeat_icon.setImageDrawable(ImageLoader.getInstance(context)
                    .getTint(R.drawable.ic_repeat_one_white_24dp, R.color.grey));

//			holder.ifttt_delete_button = (AppCompatImageButton) rowView.findViewById(R.id.ifttt_delete_button);

//			d1 = context.getResources().getDrawable(R.drawable.ic_delete_black_24dp);
//			wrappedDrawable = DrawableCompat.wrap(d1);
//			wrappedDrawable = wrappedDrawable.mutate();
//			DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(R.color.grey));
//			d1.invalidateSelf();
//			holder.ifttt_delete_button.setImageDrawable(d1);

            rowView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    int index = Integer.parseInt((holder.ifttt_header_icon_IF.getTag()).toString());
                    if (context.mActionMode == null) {
                    } else {
                        context.onListItemSelect(index);
                    }
                }
            });
            rowView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    int index = Integer.parseInt((holder.ifttt_header_icon_IF.getTag()).toString());
                    //allow multiple select on log view only
                    context.onListItemSelect(index);
                    return true;
                }
            });

            holder.ifttt_header_icon_IF.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent it = new Intent(context, IFTTTRuleWizard.class);
                    int position = Integer.parseInt(holder.ifttt_header_icon_IF.getTag().toString());
                    it.putExtra("XRul", getItem(position));
                    it.putExtra("ACTION", "IF");
                    context.editingXRulIndex = position;
                    context.startActivityForResult(it, IFTTT.RULE_EDIT);
                }
            });
            holder.ifttt_header_icon_THEN.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    Intent it = new Intent(context, IFTTTRuleWizard.class);
                    int position = Integer.parseInt(holder.ifttt_header_icon_IF.getTag().toString());
                    it.putExtra("XRul", getItem(position));
                    it.putExtra("ACTION", "THEN");
                    context.editingXRulIndex = position;
                    context.startActivityForResult(it, IFTTT.RULE_EDIT);
                }
            });

			/*holder.ifttt_delete_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					XRul rul = getItem(Integer.parseInt(holder.ifttt_delete_button.getTag().toString()));
					Persistence.getInstance(context).deleteXRul(rul.getPersistenceId());
					iftttListOriginal.remove(Integer.parseInt(holder.ifttt_delete_button.getTag().toString()));
					context.deleteTimedActions(rul);
					notifyDataSetChanged();
				}

			});*/
            holder.ifttt_switch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    XRul rul = getItem(Integer.parseInt(holder.ifttt_switch.getTag().toString()));
                    if (isChecked) {

                        if (rul.isComplete()) {
                            Persistence.getInstance(context).setXRulEnabled(rul.getPersistenceId(), true);
                            rul.setEnabled(true);

                            Log.v(TAG, "onCheckedChanged resetTimedActions " + isChecked + ", " + rul.getEventValue() + ", " + rul.getPersistenceId());
                            IFTTT.resetTimedActions(context, rul);
                        } else {
                            //do no turn on (07-OCT-2016, commented, as it switch is disabled if rule incomplete)
//							holder.ifttt_switch.setChecked(false);
                        }
                    } else {

                        Persistence.getInstance(context).setXRulEnabled(rul.getPersistenceId(), false);
                        rul.setEnabled(false);

//						Log.d(TAG, "onCheckedChanged deleteTimedActions "+isChecked + ", "+rul.getEventValue()+ ", "+rul.getPersistenceId());
                        context.deleteTimedActions(rul);
                    }

                    notifyDataSetChanged();
                }
            });

            rowView.setTag(holder);

        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.ifttt_switch.setTag((position));
//		holder.ifttt_delete_button.setTag(position);
        holder.ifttt_header_icon_IF.setTag((position));

        if (rowitem.getEvent().length() == 0) {
            holder.ifttt_mid_text.setText(Html.fromHtml("<i>Empty trigger</i>"));
        } else {
            holder.ifttt_mid_text.setText(Html.fromHtml(rowitem.toString(getContext())));
        }
//		holder.imageViewHeaderRight.setTag((position));
        if (rowitem.getEventValue().length() == 0) {
        } else {
        }

        if (rowitem.isComplete()) {
            holder.ifttt_switch.setEnabled(true);
        } else {
            holder.ifttt_switch.setEnabled(false);
        }

        holder.ifttt_header_icon_IF_overlay.setVisibility(View.GONE);
        // 05-JAN-19, not need to show text, icon is sufficient
        holder.ifttt_header_IF_description.setVisibility(View.GONE);
        holder.ifttt_header_THEN_description.setVisibility(View.GONE);

        if (rowitem.getEvent().length() == 0) {
            holder.ifttt_header_icon_IF.setImageResource(R.drawable.ic_action_add);
            holder.ifttt_header_IF_description.setText(Html.fromHtml("<i>empty</i>"));
        } else {
            holder.ifttt_header_IF_description.setText(rowitem.getEvent()/*getEventValue()*/);
            if (rowitem.getEventValue().startsWith("at ")) {
				/*holder.ifttt_header_icon_IF.setImageDrawable(
						ImageLoader.getInstance(context).getTint(
								Addressbook.getInstance(context).getStatusResourceIDByName(rowitem.getEventValue())
								, R.color.grey));*/
                holder.ifttt_header_icon_IF.setImageResource(Addressbook.getInstance(context).getStatusResourceIDByName(rowitem.getEventValue()));
                holder.ifttt_header_icon_IF_overlay.setVisibility(View.VISIBLE);
                if (rowitem.getActionWhen() == 1) {
                    holder.ifttt_header_icon_IF_overlay.setImageResource(R.drawable.ic_horiz_arrow_left_white_24dp);
                } else {
                    holder.ifttt_header_icon_IF_overlay.setImageResource(R.drawable.ic_horiz_arrow_right_white_24dp);
                }
                if (rowitem.isEnabled()) {
                    holder.ifttt_header_icon_IF.setBackgroundResource(R.drawable.button_bg_round_lightblue);
                } else {
                    holder.ifttt_header_icon_IF.setBackgroundResource(R.drawable.button_bg_round_grey);
                }
            } else {
                holder.ifttt_header_icon_IF.setImageResource(R.drawable.ic_access_time_white_24dp);
                if (rowitem.isEnabled()) {
                    holder.ifttt_header_icon_IF.setBackgroundResource(R.drawable.button_bg_round_lightblue);
                } else {
                    holder.ifttt_header_icon_IF.setBackgroundResource(R.drawable.button_bg_round_grey);
                }
            }
        }

        if (rowitem.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)) {
            holder.ifttt_header_icon_THEN.setImageResource(R.drawable.ic_message_out_white_24dp);
            if (rowitem.isEnabled()) {
                if (rowitem.isAlwaysAsk()) {
                    holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_green);
					/*holder.ifttt_header_icon_THEN.setImageDrawable(
							ImageLoader.getInstance(context).getTint(
									R.drawable.ic_message_out_white_24dp
									, R.color.green));*/
                } else {
                    holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_red);
					/*holder.ifttt_header_icon_THEN.setImageDrawable(
							ImageLoader.getInstance(context).getTint(
									R.drawable.ic_message_out_white_24dp
									, R.color.red));*/
                }
            } else {
                holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_grey);
            }
            holder.ifttt_header_THEN_description.setText(rowitem.getAction()/*"text"*/);
        } else if (rowitem.getAction().equals(XRul.RULES_ACTION_TYPE_CALL)) {
            holder.ifttt_header_icon_THEN.setImageResource(R.drawable.ic_call_white_24dp);
            if (rowitem.isEnabled()) {
                if (rowitem.isAlwaysAsk()) {
                    holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_green);
					/*holder.ifttt_header_icon_THEN.setImageDrawable(
							ImageLoader.getInstance(context).getTint(
									R.drawable.ic_call_white_24dp
									, R.color.green));*/
                } else {
                    holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_red);
					/*holder.ifttt_header_icon_THEN.setImageDrawable(
							ImageLoader.getInstance(context).getTint(
									R.drawable.ic_call_white_24dp
									, R.color.red));*/
                }
            } else {
                holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_grey);
            }
            holder.ifttt_header_THEN_description.setText(rowitem.getAction()/*"call"*/);
        } else if (rowitem.getAction().equals(XRul.RULES_ACTION_TYPE_MUTE_PHONE)
                || rowitem.getAction().equals(XRul.RULES_ACTION_TYPE_UNMUTE_PHONE)) {
            if (rowitem.getAction().equals(XRul.RULES_ACTION_TYPE_MUTE_PHONE)) {
                holder.ifttt_header_icon_THEN.setImageResource(R.drawable.ic_vibration_white_24dp);
            } else if (rowitem.getAction().equals(XRul.RULES_ACTION_TYPE_UNMUTE_PHONE)) {
                holder.ifttt_header_icon_THEN.setImageResource(R.drawable.ic_volume_up_white_24dp);
            }
            if (rowitem.isEnabled()) {
                if (rowitem.isAlwaysAsk())
                    holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_green);
                else
                    holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_red);
            } else {
                holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_grey);
            }
            holder.ifttt_header_THEN_description.setText(rowitem.getAction()/*"call"*/);
        } else {
            holder.ifttt_header_icon_THEN.setImageResource(R.drawable.ic_action_add);
            holder.ifttt_header_icon_THEN.setBackgroundResource(R.drawable.button_bg_rectangle_grey);
            holder.ifttt_header_THEN_description.setText(Html.fromHtml("<i>empty</i>"));
        }

        if (rowitem.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                holder.ifttt_switch.setChecked(true);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                holder.ifttt_switch.setChecked(false);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                holder.ifttt_switch.setChecked(false);
            }
        }

        if (rowitem.getActionRepeat().equals(XRul.RULES_REPEAT_ONCE)) {
//			Drawable d1 = context.getResources().getDrawable(R.drawable.ic_repeat_one_white_24dp);
//			Drawable wrappedDrawable = DrawableCompat.wrap(d1);
//			wrappedDrawable = wrappedDrawable.mutate();
//			DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(R.color.grey));
//			d1.invalidateSelf();
            holder.ifttt_repeat_icon.setImageDrawable(ImageLoader.getInstance(context).
                    getTint(R.drawable.ic_repeat_one_white_24dp, R.color.grey));
//			holder.ifttt_repeat_icon.setImageDrawable(d1);
        } else {
//			Drawable d1 = context.getResources().getDrawable(R.drawable.ic_repeat_white_24dp);
//			Drawable wrappedDrawable = DrawableCompat.wrap(d1);
//			wrappedDrawable = wrappedDrawable.mutate();
//			DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(R.color.grey));
//			d1.invalidateSelf();
            holder.ifttt_repeat_icon.setImageDrawable(ImageLoader.getInstance(context)
                    .getTint(R.drawable.ic_repeat_white_24dp, R.color.grey));
//			holder.ifttt_repeat_icon.setImageDrawable(d1);
        }

        if (selectionList.get(position)) {
//			holder.imageViewOverlayLeft.setImageResource(R.drawable.ic_done_white_24dp);
//			holder.rowView.setBackgroundColor(context.getResources().getColor(R.color.lightgreen_transparent_50pc));
            holder.rowView.setSelected(true);
        } else {
            holder.rowView.setSelected(false);
//			holder.rowView.setBackgroundResource(R.drawable.background_listitem);
        }
        return rowView;
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

    public SparseBooleanArray getSelectedIds() {
        return selectionList;
    }

    public void removeSelection() {
        selectionList.clear();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectionList.size();
    }

    public int getItemCount() {
        return iftttListOriginal.size();
    }

}
