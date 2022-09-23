package com.jovistar.caltxt.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.bo.XPlc;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;
import java.util.Locale;

public class DTOListAdapter extends ArrayAdapter<IDTObject> implements Filterable {
    private static final String TAG = "DTOListAdapter";

    int finalHeight = Constants.PHOTO_SIZE_STANDARD, finalWidth = Constants.PHOTO_SIZE_STANDARD;
    //	boolean checkable = false;
    String keyCallIcon = "";
    private final Activity context;
    protected ArrayList<IDTObject> dtoListOriginal;
    protected ArrayList<IDTObject> dtoListFiltered;
    private final SparseBooleanArray selectionList;

    public DTOListAdapter(Activity context, ArrayList<IDTObject> items) {
        super(context, R.layout.dtolist_item, items);
        this.context = context;
        this.dtoListOriginal = items;
        this.dtoListFiltered = items;
        this.selectionList = new SparseBooleanArray(items.size());
//		for (int i = 0, l = selectionList.size(); i < l; i++)
//			selectionList.put(i, false);
    }

    // For this helper method, return based on filteredData
    @Override
    public int getCount() {
        return dtoListFiltered.size();
    }

    // This should return a data object, not an int
    @Override
    public IDTObject getItem(int position) {
        return dtoListFiltered.get(position);
    }

    public SparseBooleanArray getSelectedIds() {
        return selectionList;
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

    public int getSelectedCount() {
        return selectionList.size();
    }

    public void toggleSelection(int position) {
        selectView(position, !selectionList.get(position));
    }

    /*
     * @Override public long getItemId(int position) { return position; }
     */
    // static to save the reference to the outer class and to avoid access to
    // any members of the containing class
    static class ViewHolder {
        View imageViewHeaderLeftFrame;
        ImageView imageViewHeaderLeft;
        ImageView imageViewOverlayLeft;
        ImageView imageViewOverlayLeftBorder;
        ImageView imageViewIndicatorRight;
        TextView textViewHeader;
        TextView textViewSubject;
        ImageView imageViewSubject;
        //		ImageView imageViewBody;
        TextView textViewBody;
        //		ImageView imageViewBody2;
        TextView textViewBody2;
        View textViewBody2Frame;
        ImageView imageViewBody2Icon;
        //		ImageView imageViewHeaderRight;
//		CheckBox checkbox;
        Button buttonInvite;
        //		View bodyTextPhotoFrame;
        AppCompatImageView bodyTextPhoto;
        //		ProgressBar bodyTextPhotoProgressBar;
//		View bodyText2PhotoFrame;
        AppCompatImageView bodyText2Photo;
//		ProgressBar bodyText2PhotoProgressBar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder will buffer the assess to the individual fields of the row
        // layout

        keyCallIcon = "";
        final ViewHolder holder;
        // Recycle existing view if passed as parameter
        // This will save memory and time on Android
        // This only works if the base layout for all classes are the same
        View rowView = convertView;
        final IDTObject rowitem = getItem(position);
        final int clickposition = position;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.dtolist_item, null, true);
            holder = new ViewHolder();
            holder.imageViewHeaderLeftFrame = rowView.findViewById(R.id.headerleft_icon_frame);
            holder.imageViewHeaderLeft = rowView.findViewById(R.id.headerleft_icon);
            /* below few lines to capture imageview h and w */
            ViewTreeObserver vto = holder.imageViewHeaderLeft.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    holder.imageViewHeaderLeft.getViewTreeObserver().removeOnPreDrawListener(this);
                    finalHeight = holder.imageViewHeaderLeft.getMeasuredHeight();
                    finalWidth = holder.imageViewHeaderLeft.getMeasuredWidth();
//			        Log.d(TAG, "onCreate, Height: " + finalHeight + " Width: " + finalWidth);
                    return true;
                }
            });
//			holder.bodyTextPhotoFrame = (View) rowView.findViewById(R.id.body_text_pic_frame);
            holder.bodyTextPhoto = rowView.findViewById(R.id.body_text_pic);
//			holder.bodyTextPhotoProgressBar = (ProgressBar) rowView.findViewById(R.id.body_text_pic_progress_bar);
//			holder.bodyText2PhotoFrame = (View) rowView.findViewById(R.id.body_text2_pic_frame);
            holder.bodyText2Photo = rowView.findViewById(R.id.body_text2_pic);
//			holder.bodyText2PhotoProgressBar = (ProgressBar) rowView.findViewById(R.id.body_text2_pic_progress_bar);
            holder.imageViewOverlayLeft = rowView.findViewById(R.id.overlay_corner);
            holder.imageViewOverlayLeftBorder = rowView.findViewById(R.id.overlay_border);
            holder.imageViewIndicatorRight = rowView.findViewById(R.id.indicator);
            holder.textViewHeader = rowView.findViewById(R.id.header_text);
            holder.imageViewSubject = rowView.findViewById(R.id.subject_icon);
            holder.textViewSubject = rowView.findViewById(R.id.subject_text);
//			holder.imageViewBody = (ImageView) rowView.findViewById(R.id.body_icon);
            holder.textViewBody = rowView.findViewById(R.id.body_text);
//			holder.imageViewBody2 = (ImageView) rowView.findViewById(R.id.body_icon2);
            holder.textViewBody2 = rowView.findViewById(R.id.body_text2);
//			holder.imageViewHeaderRight = (ImageView) rowView.findViewById(R.id.headerright_icon);
            holder.buttonInvite = rowView.findViewById(R.id.invite_button);
//			holder.checkbox = (CheckBox) rowView.findViewById(R.id.check);
//			holder.checkbox.setVisibility(View.GONE);
			/*if (checkable) {
				holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						IDTObject element = (IDTObject) holder.checkbox.getTag();
						// element.setSelected(buttonView.isChecked());
						selectionList.put(clickposition, buttonView.isChecked());
					}
				});
				holder.checkbox.setTag(getItem(position));
			} else {
				holder.checkbox.setVisibility(View.INVISIBLE);
				// holder.checkbox.setFocusable(true);
			}*/
			/*holder.imageViewHeaderRight.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent caltxtInput = new Intent(context, ActivityCaltxtInput.class);
					XMob mob = null;
					if (rowitem.getCName().equals("XCtx")) {
						mob = new XMob();
						XCtx ri = (XCtx) getItem(Integer.parseInt((v.getTag()).toString()));
						if (ri.getNumberCaller().equals(Addressbook.getMyProfile().getUsername())) {
							mob.setName(ri.getNameCallee());
							mob.setNumber(ri.getNumberCallee(), Globals.getMyCountryCode());
						} else {
							mob.setName(ri.getNameCaller());
							mob.setNumber(ri.getNumberCaller(), Globals.getMyCountryCode());
						}
						if(ri.getCaltxt().trim().length()>0)
							mob.setHeadline("RE: "+ri.getCaltxt());//reply 19-Jan-15
					} else if (rowitem.getCName().equals("XMob")) {
						mob = (XMob) getItem(Integer.parseInt((v.getTag()).toString()));
					}
					caltxtInput.putExtra("IDTOBJECT", mob);
					context.startActivity(caltxtInput);
				}
			});
			holder.imageViewHeaderLeft.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					IDTObject obj = (IDTObject) getItem(Integer.parseInt(v.getTag().toString()));
					rowOnClickAction(obj);
				}
			});*/
            holder.imageViewIndicatorRight.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (holder.imageViewIndicatorRight.getTag() == null)//its new contact indicator
                        return;
                    //its caltxt contact
                    Intent caltxtInput = new Intent(context, CaltxtInputActivity.class);
                    XMob mob = Addressbook.getInstance(context).getRegistered(holder.imageViewIndicatorRight.getTag().toString());
                    if(mob.getUsername().length()==0 || mob.getUsername().equals(Addressbook.getInstance(context).getMyProfile().getUsername())) {
                        // do not open caltxt input screen if this item is this user itself
                        Log.i(TAG, "rowOnClickActionOpen: do not open caltxt input screen if this item is this user itself");
                        return;
                    }
                    caltxtInput.putExtra("IDTOBJECT", mob);
//					caltxtInput.putExtra("VIEW", Constants.INPUT_VIEW);
                    context.startActivity(caltxtInput);
                }
            });
            holder.buttonInvite.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    if (holder.buttonInvite.getTag() instanceof XPlc) {
                        XPlc plc = (XPlc) holder.buttonInvite.getTag();
                        Log.d(TAG, "buttonInvite plc: " + plc.getCellId());
                        Persistence.getInstance(context).deleteXPLC(plc.getCellId());
//						CallManager.getInstance().cellids.remove(plc.getCellId());
                        dtoListFiltered.remove(plc);
                        notifyDataSetInvalidated();
                    } else if (Blockbook.getInstance(getContext()).get(holder.buttonInvite.getTag()) != null) {
                        Blockbook.getInstance(getContext()).remove((String) holder.buttonInvite.getTag());
                        Connection.get().addAction(Constants.contactUnblockedProperty, holder.buttonInvite.getTag(), null);
//						notifyDataSetInvalidated();
//						notifyDataSetChanged();
                    } else {
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse("sms:+" + holder.buttonInvite.getTag()));
                        sendIntent.putExtra("sms_body",
                                context.getString(R.string.service_sms_invite)
                                /*"Hi! Try new App for smart calling, to know more visit http://Caltxt.com"*/);
                        context.startActivity(sendIntent);
                    }
                }
            });
            rowView.setTag(holder);
			/*rowView.setOnClickListener(new OnClickListener() {
			      @Override
			      public void onClick(View v) {
					IDTObject obj = (IDTObject) getItem(
							Integer.parseInt((((ViewHolder) v.getTag()).imageViewHeaderLeft.getTag()).toString()));
					rowOnClickAction(obj);
			      }
			    });*/
            holder.textViewBody.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    IDTObject obj = getItem(
                            Integer.parseInt((holder.imageViewHeaderLeft.getTag()).toString()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(obj.getFooter(), obj.getFooter());
                        clipboard.setPrimaryClip(clip);
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(obj.getBody());
                    }

                    Notify.toast(v, context, context.getString(R.string.prompt_text_copied), Toast.LENGTH_LONG);
//					return false;
                }
            });
            holder.textViewBody2.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    IDTObject obj = getItem(
                            Integer.parseInt((holder.imageViewHeaderLeft.getTag()).toString()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(obj.getFooter(), obj.getFooter());
                        clipboard.setPrimaryClip(clip);
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(obj.getFooter());
                    }
                    Notify.toast(v, context, context.getString(R.string.prompt_text_copied), Toast.LENGTH_LONG);
//					return false;
                }
            });
        } else {
			/*if (checkable) {
				((ViewHolder) rowView.getTag()).checkbox.setTag(getItem(position));
			}*/
            holder = (ViewHolder) rowView.getTag();
        }

//		holder.bodyTextPhotoFrame.setVisibility(View.GONE);
        holder.bodyTextPhoto.setVisibility(View.GONE);
//		holder.bodyTextPhotoProgressBar.setVisibility(View.GONE);
//		holder.bodyText2PhotoFrame.setVisibility(View.GONE);
        holder.bodyText2Photo.setVisibility(View.GONE);
//		holder.bodyText2PhotoProgressBar.setVisibility(View.GONE);

//		holder.imageViewHeaderRight.setTag((position));
        holder.imageViewHeaderLeft.setTag((position));//7-dec-14
        holder.imageViewOverlayLeftBorder.setBackgroundResource(android.R.color.transparent);

        // sets row view based on IDTOObject
        holder.textViewHeader.setText(rowitem.getHeader());
        holder.textViewHeader.setTextColor(rowitem.getHeaderFontColor());
//		holder.textViewHeader.setSingleLine();

        holder.textViewSubject.setText(rowitem.getSubject());
//		holder.textViewSubject.setTextColor(rowitem.getSubjectFontColor());
        holder.imageViewSubject.setImageResource(rowitem.getSubjectIconResource());

        holder.textViewBody.setBackgroundResource(rowitem.getBodyBackground());
        holder.textViewBody.setTextColor(rowitem.getBodyFontColor());

        holder.textViewBody2Frame = rowView.findViewById(R.id.body_text2_frame);
        holder.imageViewBody2Icon = rowView.findViewById(R.id.body_text2_icon);

        holder.textViewBody2.setBackgroundResource(rowitem.getFooterBackground());
        holder.textViewBody2.setTextColor(rowitem.getFooterFontColor());

        if (rowitem.getBody() != null && rowitem.getBody().trim().length() > 0) {
//			holder.imageViewBody.setVisibility(View.VISIBLE);
            holder.textViewBody.setTag((position));
            holder.textViewBody.setVisibility(View.VISIBLE);
            holder.textViewBody.setText(rowitem.getBody().trim());
        } else {
//			holder.imageViewBody.setVisibility(View.GONE);
            holder.textViewBody.setVisibility(View.GONE);
        }
        if (rowitem.getFooter() != null && rowitem.getFooter().trim().length() > 0) {
//			holder.imageViewBody2.setVisibility(View.VISIBLE);
            holder.textViewBody2Frame.setVisibility(View.VISIBLE);
            holder.imageViewBody2Icon.setVisibility(View.VISIBLE);
            holder.textViewBody2.setVisibility(View.VISIBLE);
            holder.textViewBody2.setText(rowitem.getFooter().trim());
        } else {
            holder.textViewBody2Frame.setVisibility(View.GONE);
            holder.imageViewBody2Icon.setVisibility(View.GONE);
//			holder.imageViewBody2.setVisibility(View.GONE);
            holder.textViewBody2.setVisibility(View.GONE);
        }

        if (rowitem.getCName().equals("XCtx")) {
            holder.imageViewSubject.setVisibility(View.VISIBLE);
            holder.imageViewBody2Icon.setImageResource(rowitem.getFooterIconResource());
            holder.textViewSubject.setText(((XCtx) rowitem).getSubject(getContext()));
            holder.textViewHeader.setTextColor(((XCtx) rowitem).getHeaderFontColor(getContext()));

            XCtx no = (XCtx) rowitem;
//			holder.textViewSubject.setText(no.getRecvToDString(context)
//					+ (no.getSubject().length() == 0 ? "" : ", "
//							+ no.getSubject()));
//			if(no.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
            if (Addressbook.getInstance(context).isItMe(no.getUsernameCaller())) {
                keyCallIcon = no.getNumberCallee();
            } else {
                keyCallIcon = no.getUsernameCaller();
            }

            if (no.getCallState() == XCtx.OUT_MESSAGE_ADMIN) {
                holder.textViewBody.setTag((position));
                holder.textViewBody2.setTag((position));
                holder.textViewBody.setText(Html.fromHtml("<i>" + no.getOccupation() + "</i>, " + no.getCaltxt()));
                holder.textViewBody2.setText(Html.fromHtml("<i>" + no.getOccupation() + "</i>, " + no.getCaltxt()));
            }
            if (no.getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
                holder.textViewBody.setTag((position));
                holder.textViewBody.setText(Html.fromHtml("<i>" + no.getCaltxt() + "</i>"));
                holder.textViewBody2.setTag((position));
                holder.textViewBody2.setText(Html.fromHtml("<i>" + no.getCaltxt() + "</i>"));
            }
            if (!Addressbook.getInstance(context).isContact(keyCallIcon)) {
                try {
                    Float.parseFloat(no.getHeader()/*.substring(1)*/.trim());
                    holder.imageViewIndicatorRight.setVisibility(View.GONE);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "NumberFormatException : " + no.getHeader()/*.substring(1)*/);
                    /*if has contact name then potential caltxt user(not in contact list); indicate to user*/
                    holder.imageViewIndicatorRight.setVisibility(View.VISIBLE);
                    holder.imageViewIndicatorRight.setImageResource(R.drawable.ic_new);
                    holder.imageViewIndicatorRight.setTag(null);//to identify its not eligible for onclick
                }
            } else {
                holder.imageViewIndicatorRight.setVisibility(View.GONE);
            }
			/*if (no.getCallState() == XCtx.OUT_CALL) {//outgoing context call
				holder.imageViewSubject.setImageResource(R.drawable.outcall);
//				keyCallIcon = no.getNumberCallee();
			} else if (no.getCallState() == XCtx.IN_CALL_MISSED) {//incoming call missed
				holder.imageViewSubject.setImageResource(R.drawable.missedcall);
//				keyCallIcon = no.getNumberCaller();
			} else if (no.getCallState() == XCtx.IN_CALL) {//incoming context call
				holder.imageViewSubject.setImageResource(R.drawable.incall);
//				keyCallIcon = no.getNumberCaller();
			} else if (no.getCallState() == XCtx.IN_CALL_REPLY) {//incoming call acknowledged with message
				holder.imageViewSubject.setImageResource(R.drawable.missedcall);
//				keyCallIcon = no.getNumberCallee();
			} else if (no.getCallState() == XCtx.IN_CALL_REPLY_RECEIVED) {//outgoing call acknowledged with message
				holder.imageViewSubject.setImageResource(R.drawable.outcallbusy);
//				keyCallIcon = no.getNumberCallee();
			} else if (no.getCallState() == XCtx.IN_MESSAGE_REPLY
					|| no.getCallState() == XCtx.IN_MESSAGE_RECEIVED
					|| no.getCallState() == XCtx.OUT_MESSAGE) {
				holder.imageViewSubject.setImageResource(R.drawable.time);
//				keyCallIcon = no.getNumberCaller();
			}*/
            // keyCallIcon = Globals.getContactStatus(keyCallIcon);
            // holder.imageViewHeaderRight.setImageResource(android.R.drawable.sym_action_call);
//			if (no.getNumberCallee().equals(Addressbook.getMyProfile().getUsername())) {//receiver is myself
//				if (rowitem.getBody() != null && rowitem.getBody().trim().length() > 0) {
					/*if (no.isDelivered())
						holder.imageViewBody.setImageResource(Constants.icon_msg_sent);
					else
						holder.imageViewBody.setImageResource(Constants.icon_msg_sent_not);*/
//				}
//				if (rowitem.getFooter() != null && rowitem.getFooter().trim().length() > 0) {
            // if(no.isDelivered())
//					holder.imageViewBody2.setImageResource(Constants.icon_msg_received);
            // else
            // holder.imageViewBody2.setImageResource(Constants.icon_msg_received_not);
//				}
//			} else {//sender is myself
				/*if (rowitem.getBody() != null && rowitem.getBody().trim().length() > 0) {
					if(no.getCallPriority()==XCtx.PRIORITY_EMERGENCY) {
						if(!no.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
							holder.textViewBody.setBackgroundResource(R.drawable.calloutto_red_white_text);
							holder.textViewBody.setTextColor(Color.WHITE);
						} else {
							holder.textViewBody.setTextColor(Color.BLACK);
							holder.textViewBody.setBackgroundResource(R.drawable.calloutto_trans_white_black_text);
						}
					} else {
						holder.textViewBody.setTextColor(Color.BLACK);
						holder.textViewBody.setBackgroundResource(R.drawable.calloutto_trans_white_black_text);
					}
				}*/
				/*if (rowitem.getFooter() != null && rowitem.getFooter().trim().length() > 0) {
					if (no.isDelivered()) {
						if(no.getCallPriority()==XCtx.PRIORITY_EMERGENCY) {
							if(no.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
								holder.textViewBody2.setBackgroundResource(R.drawable.calloutfrom_red_white_text);
								holder.textViewBody2.setTextColor(Color.WHITE);
							} else {
								holder.textViewBody2.setBackgroundResource(R.drawable.calloutfrom_trans_lightgreen_black_text);
								holder.textViewBody2.setTextColor(Color.BLACK);
							}
						} else {
							holder.textViewBody2.setBackgroundResource(R.drawable.calloutfrom_trans_lightgreen_black_text);
							holder.textViewBody2.setTextColor(Color.BLACK);
						}
//						holder.imageViewBody2.setImageResource(Constants.icon_msg_sent);
					} else {
						holder.textViewBody2.setBackgroundResource(R.drawable.calloutfrom_transparent);
//						holder.imageViewBody2.setImageResource(Constants.icon_msg_sent_not);
					}
				}*/
//			}

//			((Globals) context.getApplication()).getImageLoader().DisplayImage(
//					rowitem.getIcon(), holder.imageViewHeaderLeft,
//					finalWidth/*Constants.LIST_IMG_HEIGHT*/, Constants.icon_social_person);
            holder.buttonInvite.setVisibility(View.GONE);
        } else if (rowitem.getCName().equals("XMob")) {
            holder.imageViewIndicatorRight.setVisibility(View.GONE);
            XMob no = (XMob) rowitem;
//			holder.textViewSubject.setText((no.getSubject().length() == 0 
//					? (no.getNumber()/*no.getUsername().length()>10?("+"+no.getUsername()):no.getUsername()*/) : no.getSubject()));
            holder.buttonInvite.setTag(no.getUsername());
            // keyCallIcon = no.getHeadline();
            // keyCallIcon = Globals.getContactStatus(no.getUsername());
            keyCallIcon = no.getUsername();
            holder.imageViewSubject.setVisibility(View.GONE);
//			holder.imageViewBody.setVisibility(View.GONE);
            if (no.getBody().length() > 0) {
                holder.textViewSubject.setText(no.getBody());
                holder.textViewBody.setVisibility(View.GONE);
            }

            if (Blockbook.getInstance(getContext()).get(no.getUsername()) != null) {
                holder.buttonInvite.setText(null);
                holder.buttonInvite.setBackgroundResource(R.drawable.ic_remove_circle_outline_black_24dp);
                holder.buttonInvite.setVisibility(View.VISIBLE);
            } else {
//				if(Addressbook.get().isCaltxtContact(no.getUsername())) {
                if (no.isRegistered()) {
                    holder.imageViewIndicatorRight.setTag(no.getUsername());
                    holder.imageViewIndicatorRight.setVisibility(View.VISIBLE);
//					holder.imageViewIndicatorRight.setImageResource(R.xml.caltxt_button);
                    holder.imageViewIndicatorRight.setImageResource(R.drawable.ic_call_white_24dp);
//							 Addressbook.get().getContactStatusIconResource(Addressbook.get().getContact(no.getUsername())));
//					holder.buttonInvite.setText(null);
//					holder.buttonInvite.setBackgroundResource(
//							Addressbook.get().getContactStatusIconResource(Addressbook.get().getContact(no.getUsername())));
//					holder.buttonInvite.setVisibility(View.VISIBLE);
                    holder.buttonInvite.setVisibility(View.GONE);
                } else {
                    holder.buttonInvite.setText(R.string.action_invite_contact);
                    holder.buttonInvite.setBackgroundResource(R.drawable.custom_button_green);

                    holder.buttonInvite.setVisibility(View.VISIBLE);
                }
            }
//			((Globals) context.getApplication()).getImageLoader().DisplayImage(
//					rowitem.getIcon(), holder.imageViewHeaderLeft,
//					finalWidth/*Constants.LIST_IMG_HEIGHT*/, Constants.icon_social_person);
            //03JUN17, no need to show in contacts
            holder.buttonInvite.setVisibility(View.GONE);
        } else if (rowitem.getCName().equals("XPlc")) {
            XPlc plc = (XPlc) rowitem;
            holder.buttonInvite.setTag(plc);
            //			holder.textViewHeader.setVisibility(View.GONE);
//			holder.textViewSubject.setVisibility(View.GONE);
//			holder.textViewBody.setVisibility(View.GONE);
            holder.textViewBody2.setVisibility(View.GONE);
//			holder.imageViewHeaderLeft.setVisibility(View.GONE);
//			holder.imageViewSubject.setVisibility(View.GONE);
//			rowView.findViewById(R.id.right_content).setVisibility(View.GONE);
            holder.imageViewOverlayLeft.setVisibility(View.GONE);
            holder.imageViewIndicatorRight.setVisibility(View.GONE);
//			holder.imageViewSubject.setVisibility(View.GONE);
            holder.buttonInvite.setText(R.string.action_place_forget);
            holder.buttonInvite.setBackgroundResource(R.drawable.custom_button_red);

            //preset Tint few resources
//			Drawable d1 = CaltxtApp.getCustomAppContext().getResources().getDrawable(R.drawable.ic_beenhere_white_24dp);
//			Drawable wrappedDrawable = DrawableCompat.wrap(d1);
//			wrappedDrawable = wrappedDrawable.mutate();
//			DrawableCompat.setTint(wrappedDrawable, CaltxtApp.getCustomAppContext().getResources().getColor(R.color.green));
//			d1.invalidateSelf();

            holder.imageViewHeaderLeft.setImageResource(Addressbook.getInstance(context).getStatusResourceIDByName(plc.getStatus()));
            holder.imageViewHeaderLeft.setBackgroundResource(R.drawable.circle_grey);
//			holder.imageViewHeaderLeft.setImageDrawable(d1);
            holder.buttonInvite.setVisibility(View.VISIBLE);
        } else if (rowitem.getCName().equals("XAd")) {
//			holder.imageViewHeaderRight.setVisibility(View.GONE);
            holder.imageViewOverlayLeft.setVisibility(View.GONE);
            holder.imageViewSubject.setVisibility(View.GONE);
            holder.imageViewHeaderLeft.setVisibility(View.GONE);
            holder.imageViewHeaderLeftFrame.setVisibility(View.GONE);
            rowView.findViewById(R.id.right_content).setVisibility(View.GONE);
            holder.textViewHeader.setSingleLine(false);
			/*((Globals) context.getApplication()).getImageLoader().DisplayImage(
					rowitem.getIcon(), holder.imageViewHeaderLeft,
					Constants.LIST_IMG_HEIGHT, R.drawable.faq);*/
        }

//		if(keyCallIcon.length()>0) {
//			holder.imageViewHeaderRight.setImageResource(Globals.getContactStatusOverlayIconResource(keyCallIcon));
//		if(finalWidth>0)
//		if(Addressbook.get().getRegistered(keyCallIcon)!=null && ((XMob)Addressbook.get().getRegistered(keyCallIcon)).isBlocked()) {
/*		if(Blockbook.get().get(keyCallIcon)!=null) {
			((Globals) context.getApplication()).getImageLoader().DisplayImage(
				rowitem.getIcon(), holder.imageViewHeaderLeft,
				finalWidth, R.drawable.ic_action_unblock);
	        Log.d(TAG, "keyCallIcon blocked:" + keyCallIcon);
		} else {*/
        ImageLoader.getInstance(getContext()).DisplayImage(
                rowitem.getIcon(), holder.imageViewHeaderLeft,
                finalWidth/*Constants.LIST_IMG_HEIGHT*/, R.drawable.ic_person_white_24dp, true);
//		}
        XMob mob = Addressbook.getInstance(context).getRegistered(keyCallIcon);
        if (mob != null && mob.isRegistered() && Connection.get().isConnected()) {
            // WHY PING HERE? 23JAN2017
//				RebootService.getConnection(getContext()).submitPingContact(Addressbook.getInstance(context).getMyProfile().getUsername(),
//						keyCallIcon);
        }
        holder.imageViewOverlayLeft.setBackgroundResource(Addressbook.getInstance(context).geStatusBackgroundResource(mob));
        holder.imageViewOverlayLeft.setImageResource(Addressbook.getInstance(context).getContactStatusIconResource(mob));
//			holder.imageViewOverlayLeft.setImageDrawable(Addressbook.getInstance(context).getContactStatusOverlayIconResource(
//					keyCallIcon));
//			holder.imageViewOverlayLeft.setImageResource(Addressbook.get().getContactStatusOverlayIconResource(keyCallIcon));
//			holder.imageViewOverlayLeft.setImageResource(Constants.icon_social_person);
//		}
		/*
		 * short status = Globals.getContactStatus(keyCallIcon); keyCallIcon =
		 * Globals.getContactHeadline(keyCallIcon);
		 * if(status==Constants.STATUS_ONLINE) {
		 * if(keyCallIcon.equalsIgnoreCase(Constants.STRING_STATUS_DND))
		 * holder.imageViewHeaderRight.setImageResource(Constants.icon_dnd);
		 * else if(keyCallIcon.equalsIgnoreCase(Constants.STRING_STATUS_BUSY))
		 * holder.imageViewHeaderRight.setImageResource(Constants.icon_busy);
		 * else if(keyCallIcon.equalsIgnoreCase(Constants.STRING_STATUS_AWAY))
		 * holder.imageViewHeaderRight.setImageResource(Constants.icon_away);
		 * else
		 * if(keyCallIcon.equalsIgnoreCase(Constants.STRING_STATUS_OFFLINE))
		 * holder.imageViewHeaderRight.setImageResource(Constants.icon_offline);
		 * else
		 * holder.imageViewHeaderRight.setImageResource(Constants.icon_available
		 * ); } else {
		 * holder.imageViewHeaderRight.setImageResource(Constants.icon_offline);
		 * }

		if (checkable) {
			holder.checkbox.setChecked(selectionList.get(position));
		}
		 */
//        rowView.setBackgroundColor(selectionList.get(position) ?
//                context.getResources().getColor(R.color.lightgrey50) : Color.TRANSPARENT);
		/*
		if (rowitem.getCName().equals("XAd")) {
			((Globals) context.getApplication()).getImageLoader().DisplayImage(
					rowitem.getIcon(), holder.imageViewHeaderLeft,
					Constants.LIST_IMG_HEIGHT, R.drawable.faq);
		} else {
			((Globals) context.getApplication()).getImageLoader().DisplayImage(
					rowitem.getIcon(), holder.imageViewHeaderLeft,
					Constants.LIST_IMG_HEIGHT, Constants.icon_social_person);
		}
*/
        // rowHeight = rowView.getHeight();
        return rowView;
    }

    /*
     * private Bitmap getDTOListUISprite(String iconame, int h) { return
     * Icons.getInstance().getSprite(iconame, context, h, h); }
     *
     * // this method returns the image with correct size for this UI // if
     * requested img not found in jar, default is returned protected Bitmap
     * getDTOListUISprite(IDTObject obj, int h) { // icon =
     * getDTOListUISprite(obj.getIcon(),h); return
     * getDTOListUISprite(obj.getIcon(), h); }
     */
    public void markAll(boolean state) {
        // for(int i=dtoList.size()-1;i>=0;i--)
        // this.setSelectedIndex(i, true);
        for (int i = 0, l = /* dtoList.size() */getCount(); i < l; i++) {
            selectionList.put(i, state);
        }
        notifyDataSetChanged();
    }

    /*
        private void rowOnClickAction(IDTObject obj) {
            Intent profile = new Intent(context, Profile.class);
            XMob mob = null;
            if (obj.getCName().equals("XCtx")) {
                String uname=null;
                XMob nmob = new XMob();

                XCtx ri = (XCtx) obj;//(XCtx) getItem(Integer.parseInt((v.getTag()).toString()));
                if (ri.getNumberCaller().equals(Addressbook.getMyProfile().getUsername())) {
                    uname=ri.getNumberCallee();
                    nmob.setName(ri.getNameCallee());
                    nmob.setNumber(ri.getNumberCallee(), Globals.getMyCountryCode());
                } else {
                    uname=ri.getNumberCaller();
                    nmob.setName(ri.getNameCaller());
                    nmob.setNumber(ri.getNumberCaller(), Globals.getMyCountryCode());
                }
                mob = Addressbook.get().getContact(uname);
                if(mob==null) {
                    mob = nmob;
                }
    //			mob.setHeadline(Globals.getContactHeadline(mob.getUsername()));
                profile.putExtra("IDTOBJECT", mob);
                context.startActivity(profile);
            } else if (obj.getCName().equals("XMob")) {
                mob = (XMob) obj;//(XMob) getItem(Integer.parseInt((v.getTag()).toString()));
    //			mob = (XMob)rowitem;
                profile.putExtra("IDTOBJECT", mob);
                context.startActivity(profile);
            }
        }
    */
    public SparseBooleanArray getSelections() {
        return selectionList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();

                // If there's nothing to filter on, return the original data for
                // your list
                if (charSequence == null || charSequence.length() == 0) {
                    results.values = dtoListOriginal;
                    results.count = dtoListOriginal.size();
                } else {
                    ArrayList<IDTObject> filterResultsData = new ArrayList<IDTObject>();
//			        Log.d(TAG, "performFiltering filterResultsData:" + filterResultsData.size());

                    for (IDTObject data : dtoListOriginal) {
                        // In this loop, you'll filter through dtoListOriginal
                        // and
                        // compare each item to charSequence.
                        // If you find a match, add it to your new ArrayList
                        // I'm not sure how you're going to do comparison, so
                        // you'll need to fill out this conditional
//				        Log.d(TAG, "performFiltering data:" + data.toString());
                        if (data./* getHeader() */searchString()
                                .toLowerCase(Locale.getDefault())
                                .contains(charSequence)) {
                            filterResultsData.add(data);
//					        Log.d(TAG, "performFiltering data added:" + data.toString());
                        }
                    }

                    results.values = filterResultsData;
                    results.count = filterResultsData.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence,
                                          FilterResults filterResults) {
                dtoListFiltered = (ArrayList<IDTObject>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
