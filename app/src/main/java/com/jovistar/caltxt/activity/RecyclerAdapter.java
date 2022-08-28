package com.jovistar.caltxt.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;
import java.util.Locale;

//import android.widget.Filter.FilterResults;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private static final String TAG = "RecyclerAdapter";

    int finalHeight = Constants.PHOTO_SIZE_STANDARD, finalWidth = Constants.PHOTO_SIZE_STANDARD;
    protected ArrayList<IDTObject> dtoListOriginal;
    protected ArrayList<IDTObject> dtoListFiltered;
    private final SparseBooleanArray selectionList;
    private final CaltxtFragment context;
    String keyCallIcon = "";
    Addressbook addressbook;

    RecyclerAdapter(CaltxtFragment context, ArrayList<IDTObject> items) {
        this.context = context;
        this.dtoListOriginal = items;
        this.dtoListFiltered = items;
        this.selectionList = new SparseBooleanArray(items.size());
        addressbook = Addressbook.getInstance(context.getContext());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dtolist_item, viewGroup, false);

        return new ViewHolder(v, i);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (dtoListFiltered.size() == 0) {
            return;
        }

        IDTObject rowitem = dtoListFiltered.get(position);
        keyCallIcon = "";

//		holder.imageViewHeaderRight.setTag((position));
        holder.imageViewHeaderLeft.setTag((position));//7-dec-14

        // sets row view based on IDTOObject
        holder.textViewHeader.setText(rowitem.getHeader());
//		holder.textViewHeader.setSingleLine();
//		holder.textViewHeader.setTextColor(rowitem.getHeaderFontColor());

        holder.textViewSubject.setTextColor(rowitem.getSubjectFontColor());
        holder.imageViewSubject.setImageResource(rowitem.getSubjectIconResource());

        //uncommented 12112019, highlight only the text, not whole midContent
		holder.textViewBody.setBackgroundResource(rowitem.getBodyBackground());
        holder.textViewBody.setTextColor(rowitem.getBodyFontColor());
        holder.textViewBody.setTag((position));

        // set the backgroud to text associated with Caltxt
        //commented 13112019, highlight only the text, not whole midContent
//        holder.textViewBody2Frame.setBackgroundResource(rowitem.getFooterBackground());
        //uncommented 13112019, highlight only the text, not whole midContent
		holder.textViewBody2.setBackgroundResource(rowitem.getFooterBackground());
        holder.textViewBody2.setTextColor(rowitem.getFooterFontColor());
        holder.textViewBody2.setTag((position));

        // set the backgroud to text associated with Caltxt
        //commented 12112019, highlight only the text, not whole midContent
//        holder.midContent.setBackgroundResource(rowitem.getBodyBackground());

//		holder.bodyTextPhotoFrame.setVisibility(View.GONE);
        holder.bodyTextPhoto.setVisibility(View.GONE);
//		holder.bodyTextPhotoProgressBar.setVisibility(View.GONE);
//		holder.bodyText2PhotoFrame.setVisibility(View.GONE);
        holder.bodyText2Photo.setVisibility(View.GONE);
//		holder.bodyText2PhotoProgressBar.setVisibility(View.GONE);

        if (rowitem.getBody() != null && rowitem.getBody().trim().length() > 0) {
//			holder.imageViewBody.setVisibility(View.VISIBLE);
            holder.textViewBody.setVisibility(View.VISIBLE);
            holder.textViewBody.setText(Html.fromHtml(rowitem.getBody().trim()));
        } else {
/*			if (rowitem.getCName().equals("XCtx")) {
//				holder.textViewBody.setTextColor(Color.GRAY);
				holder.textViewBody.setText(Html.fromHtml("<i>No text received</i>"));
//				holder.textViewBody.setBackgroundResource(R.drawable.caltxtto_undelivered_selector);
			} else {
				holder.textViewBody.setVisibility(View.GONE);
			}*/
            holder.textViewBody.setVisibility(/*View.INVISIBLE*/View.GONE);
//			holder.imageViewBody.setVisibility(View.GONE);
        }
        if (rowitem.getFooter() != null && rowitem.getFooter().trim().length() > 0) {
//			holder.imageViewBody2.setVisibility(View.VISIBLE);
            holder.textViewBody2Frame.setVisibility(View.VISIBLE);
            holder.textViewBody2.setVisibility(View.VISIBLE);
            holder.imageViewBody2Icon.setVisibility(View.VISIBLE);
            holder.textViewBody2.setText(Html.fromHtml(rowitem.getFooter().trim()));
        } else {
            holder.textViewBody2Frame.setVisibility(View.GONE);
            holder.imageViewBody2Icon.setVisibility(View.GONE);
            /*if (rowitem.getCName().equals("XCtx")) {
//				holder.textViewBody2.setTextColor(Color.GRAY);
				holder.textViewBody2.setText(Html.fromHtml("<i>No text sent</i>"));
				holder.textViewBody2.setBackgroundResource(R.drawable.caltxtfrom_undelivered_selector);
			} else {
				holder.textViewBody2.setVisibility(View.GONE);
			}*/
            holder.textViewBody2.setVisibility(View.GONE);
//			holder.imageViewBody2.setVisibility(View.GONE);
        }

//		holder.bodyTextPhotoProgressBar.setVisibility(View.GONE);
//		holder.bodyText2PhotoProgressBar.setVisibility(View.GONE);
        if (rowitem.getCName().equals("XCtx")) {
            XCtx no = (XCtx) rowitem;
//			XCtx no = (XCtx) context.adapter.getItem(Integer.parseInt(holder.imageViewHeaderLeft.getTag().toString()));
            if (no.getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
                if (no.getBody().trim().length() > 0) {
                    holder.textViewHeader.setText(Html.fromHtml(rowitem.getHeader() + " " + no.getBody()));
                    holder.textViewBody.setVisibility(View.GONE);
//				} else if(no.getFooter().trim().length()>0) {
//					holder.textViewHeader.setText(Html.fromHtml(rowitem.getHeader() + ", <i>I</i> " + no.getFooter()));
//					holder.textViewBody2Frame.setVisibility(View.GONE);
                }
            }

            if (no.getBodyPicURL().length() == 0) {
//				holder.bodyTextPhotoFrame.setVisibility(View.GONE);
                holder.bodyTextPhoto.setVisibility(View.GONE);
//				holder.bodyTextPhotoProgressBar.setVisibility(View.GONE);
            } else {
//				holder.bodyTextPhotoFrame.setVisibility(View.VISIBLE);
                holder.bodyTextPhoto.setVisibility(View.VISIBLE);
                holder.bodyTextPhoto.setTag(no.getBodyPicURL());
//				holder.bodyTextPhotoProgressBar.setVisibility(View.VISIBLE);
                ImageLoader.getInstance(context.getContext()).DisplayImage(
                        no.getBodyPicURL(), holder.bodyTextPhoto,
                        200,
                        R.drawable.ic_terrain_white_24dp, false);
            }

            if (no.getFooterPicURL().length() == 0) {
//				holder.bodyText2PhotoFrame.setVisibility(View.GONE);
                holder.bodyText2Photo.setVisibility(View.GONE);
//				holder.bodyText2PhotoProgressBar.setVisibility(View.GONE);
            } else {
                holder.textViewBody2Frame.setVisibility(View.VISIBLE);
                holder.textViewBody2.setVisibility(View.VISIBLE);
                holder.imageViewBody2Icon.setVisibility(View.VISIBLE);
                holder.textViewBody2.setText(Html.fromHtml(rowitem.getFooter().trim()));

//				holder.bodyText2PhotoFrame.setVisibility(View.VISIBLE);
                holder.bodyText2Photo.setVisibility(View.VISIBLE);
//				holder.bodyText2PhotoProgressBar.setVisibility(View.VISIBLE);
                holder.bodyText2Photo.setTag(no.getFooterPicURL());

                ImageLoader.getInstance(context.getContext()).DisplayImage(
                        no.getFooterPicURL(), holder.bodyText2Photo,
                        200,
                        R.drawable.ic_terrain_white_24dp, false);
            }

            if (rowitem.getSubjectIconResource() > 0) {
                holder.imageViewSubject.setVisibility(View.VISIBLE);
            } else {
                holder.imageViewSubject.setVisibility(View.GONE);
            }
            holder.imageViewBody2Icon.setImageResource(rowitem.getFooterIconResource());
            //01JUN17, dont show this icon, it represents wrong message sent status!
            //09-Dec-2018, removed now should show correct message status based on
            //Firebase Published, Publishing, Delivered
            //holder.imageViewBody2Icon.setVisibility(View.GONE);

            holder.textViewHeader.setTextColor(((XCtx) rowitem).getHeaderFontColor(context.getContext()));
            holder.textViewSubject.setText(((XCtx) rowitem).getSubject(context.getContext()));

//			holder.textViewSubject.setText(no.getRecvToDString(context)
//					+ (no.getSubject().length() == 0 ? "" : ", "
//							+ no.getSubject()));
//			if(no.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
            if (Addressbook.isItMe(no.getUsernameCaller())) {
                keyCallIcon = XMob.toFQMN(no.getNumberCallee(), Addressbook.getMyCountryCode());
            } else {
                keyCallIcon = XMob.toFQMN(no.getUsernameCaller(), Addressbook.getMyCountryCode());
            }
            holder.imageViewOverlayLeft.setTag(keyCallIcon);

//			if(no.getCallState()==XCtx.OUT_MESSAGE_ADMIN) {
//				holder.textViewBody.setTag((position));
//				holder.textViewBody2.setTag((position));
//				holder.textViewBody.setText(Html.fromHtml("<b>"+no.getOccupation()+"</b>, " + no.getCaltxt()));
//				holder.textViewBody2.setText(Html.fromHtml("<b>"+no.getOccupation()+"</b>, " + no.getCaltxt()));
//			} else if(no.getCallState()==XCtx.OUT_MESSAGE_TRIGGER) {
//				holder.textViewBody.setTag((position));
//				holder.textViewBody2.setTag((position));
//				holder.textViewBody.setText(Html.fromHtml("<i>"+no.getCaltxt()+"</i>"));
//				holder.textViewBody2.setText(Html.fromHtml("<i>"+no.getCaltxt()+"</i>"));
//			}
            if (!addressbook.isContact(keyCallIcon)) {
                try {
                    Float.parseFloat(no.getHeader()/*.substring(1)*/.trim());
                    holder.imageViewIndicatorRight.setVisibility(View.GONE);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "NumberFormatException : " + no.getHeader()/*.substring(1)*/);
                    /*if has contact name then potential caltxt user(not in contact list); indicate to user
                     * MUST exist in Search list though. All caltxt users who are not in contact list are
                     * added in Search list if they ping or ack ping */
                    if (addressbook.isRegistered(keyCallIcon)) {//have in Search list also
                        holder.imageViewIndicatorRight.setVisibility(View.VISIBLE);
                        holder.imageViewIndicatorRight.setImageResource(R.drawable.ic_new);
                        holder.imageViewIndicatorRight.setTag(null);//to identify its not eligible for onclick
                    } else {
                        holder.imageViewIndicatorRight.setVisibility(View.GONE);
                    }
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

            holder.textViewHeader.setTextColor(rowitem.getHeaderFontColor());
            holder.textViewSubject.setText(rowitem.getSubject());

            holder.imageViewIndicatorRight.setVisibility(View.GONE);
            XMob no = (XMob) rowitem;
            holder.buttonInvite.setTag(no.getUsername());
            // keyCallIcon = no.getHeadline();
            // keyCallIcon = Globals.getContactStatus(no.getUsername());
            keyCallIcon = XMob.toFQMN(no.getUsername(), Addressbook.getMyCountryCode());
            holder.imageViewOverlayLeft.setTag(keyCallIcon);
            holder.imageViewSubject.setVisibility(View.GONE);
//			holder.imageViewBody.setVisibility(View.GONE);
            if (no.getBody().length() > 0) {
                holder.textViewSubject.setText(no.getBody());
            }
            holder.textViewBody.setVisibility(View.GONE);

            if (Blockbook.getInstance(context.getContext()).get(no.getUsername()) != null) {
                holder.buttonInvite.setText(null);
                holder.buttonInvite.setBackgroundResource(R.drawable.ic_remove_circle_outline_black_24dp);
//				holder.buttonInvite.setVisibility(View.VISIBLE);
                //03JUN17, no need to show in contacts
                holder.buttonInvite.setVisibility(View.GONE);
            } else {
//				if(Addressbook.get().isCaltxtContact(no.getUsername())) {
                if (no.isRegistered()) {
                    holder.imageViewIndicatorRight.setTag(no.getUsername());
//					holder.imageViewIndicatorRight.setVisibility(View.VISIBLE);
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
        } else if (rowitem.getCName().equals("XAd")) {
            holder.textViewHeader.setTextColor(rowitem.getHeaderFontColor());
            holder.textViewSubject.setText(rowitem.getSubject());
//			holder.imageViewHeaderRight.setVisibility(View.GONE);
            holder.imageViewSubject.setVisibility(View.GONE);
            holder.imageViewHeaderLeft.setVisibility(View.GONE);
//			rowView.findViewById(R.id.right_content).setVisibility(View.GONE);
            holder.imageViewIndicatorRight.setVisibility(View.GONE);
            holder.buttonInvite.setVisibility(View.GONE);
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
        holder.imageViewHeaderLeft.setBackgroundResource(R.drawable.button_bg_round_lightgreen);
//		}
        XMob mob = addressbook.getRegistered(keyCallIcon);
        if (mob != null && mob.isRegistered()/* && RebootService.getConnection(context.getContext()).isConnected()*/) {
            if (selectionList.get(position)) {
                holder.imageViewHeaderLeft.setImageResource(R.drawable.ic_check_black_24dp);
            } else {
                ImageLoader.getInstance(context.getContext()).DisplayImage(
                        /*rowitem.getIcon()*/keyCallIcon + Constants.IMAGE_FILE_EXTN, holder.imageViewHeaderLeft,
                        finalWidth/*Constants.LIST_IMG_HEIGHT*/, R.drawable.ic_person_white_24dp, true);
            }
            // WHY PING HERE? 23JAN2017
//				RebootService.getConnection(context.getContext()).submitPingContact(Addressbook.getInstance(context.getContext()).getMyProfile().getUsername(),
//						keyCallIcon);
            if (mob.isOffline() == false) {
                holder.imageViewOverlayLeftBorder.setBackgroundResource(R.drawable.circle_white);
                holder.imageViewOverlayLeft.setBackgroundResource(Addressbook.getInstance(context.getContext()).geStatusBackgroundResource(mob));
                holder.imageViewOverlayLeft.setImageResource(Addressbook.getInstance(context.getContext()).getContactStatusIconResource(mob));
//					holder.imageViewOverlayLeft.setBackgroundResource(addressbook.geStatusBackgroundResource(mob));
//					holder.imageViewOverlayLeft.setImageResource(addressbook.getContactStatusIconResource(mob));
            } else {
                holder.imageViewOverlayLeftBorder.setBackgroundResource(android.R.color.transparent);
                holder.imageViewOverlayLeft.setBackgroundResource(android.R.color.transparent);
                holder.imageViewOverlayLeft.setImageResource(0);
            }
        } else {
            holder.imageViewOverlayLeftBorder.setBackgroundResource(android.R.color.transparent);
            holder.imageViewOverlayLeft.setBackgroundResource(android.R.color.transparent);
            holder.imageViewOverlayLeft.setImageResource(0);
            if (selectionList.get(position)) {
                holder.imageViewHeaderLeft.setImageResource(R.drawable.ic_check_black_24dp);
            } else {
                holder.imageViewHeaderLeft.setImageResource(R.drawable.ic_person_white_24dp);
            }
        }
//			holder.imageViewOverlayLeft.setImageDrawable(Addressbook.get().getContactStatusOverlayIconResource(
//					keyCallIcon));
        if (selectionList.get(position)) {
//				holder.imageViewOverlayLeft.setImageResource(R.drawable.ic_done_white_24dp);
				holder.rowView.setBackgroundColor(context.getResources().getColor(R.color.lightgrey50));
//				holder.midContent.setSelected(true);
            holder.rowView.setSelected(true);
//				holder.textViewBody.setSelected(true);
//				holder.textViewBody2.setSelected(true);
//				holder.rowView.setBackgroundResource(R.drawable.call_blue_black_text);
//				holder.textViewBody.setBackgroundResource(R.drawable.call_blue_black_text);
//				holder.textViewBody2.setBackgroundResource(R.drawable.call_blue_black_text);
        } else {
//				holder.midContent.setSelected(false);
            holder.rowView.setSelected(false);
            TypedValue outValue = new TypedValue();
            context.getActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            holder.rowView.setBackgroundResource(outValue.resourceId);
//              holder.textViewBody.setSelected(false);
//				holder.textViewBody2.setSelected(false);
//				holder.rowView.setBackgroundResource(R.drawable.background_listitem);
//				holder.textViewBody.setBackgroundResource(rowitem.getBodyBackground());
//				holder.textViewBody2.setBackgroundResource(rowitem.getFooterBackground());
        }

//			holder.rowView.setBackgroundColor(selectionList.get(position) ? 
//					context.getResources().getColor(R.color.lightgreen_transparent_50pc) : Color.TRANSPARENT);
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
//		holder.rowView.setBackgroundColor(selectionList.get(position) ? 
//				context.getResources().getColor(R.color.lightgrey) : Color.TRANSPARENT);
		/*
		if (rowitem.getCName().equals("XAd")) {
			((Globals) context.getApplication()).getImageLoader().DisplayImage(
					rowitem.getIcon(), holder.imageViewHeaderLeft,
					Constants.LIST_IMG_HEIGHT, R.drawable.faq);
		} else {
			((Globals) context.getApplication()).getImageLoader().DisplayImage(
					rowitem.getIcon(), holder.imageViewHeaderLeft,
					Constants.LIST_IMG_HEIGHT, Constants.icon_social_person);
		}*/
    }

    @Override
    public int getItemCount() {
        return dtoListFiltered.size();
    }

    public IDTObject getItem(int position) {
        return dtoListFiltered.get(position);
    }

    public void clear() {
        dtoListFiltered.clear();
        dtoListOriginal.clear();
    }

    public int getSelectedCount() {
        return selectionList.size();
    }

    public void toggleSelection(int position) {
        selectView(position, !selectionList.get(position));
    }

    private void selectView(int position, boolean value) {
        if (value) {
            selectionList.put(position, value);
            Log.d(TAG, "selectView selected");
        } else {
            selectionList.delete(position);
            Log.d(TAG, "selectView deselected");
        }
        notifyItemChanged(position);// redraw just one item
//		notifyDataSetChanged();// redraws every item in list
    }

    public SparseBooleanArray getSelectedIds() {
        return selectionList;
    }

    public void removeSelection() {
        selectionList.clear();
        notifyDataSetChanged();
    }

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

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton imageViewHeaderLeft;
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
        View midContent;
        //		View bodyTextPhotoFrame;
        AppCompatImageView bodyTextPhoto;
        //		ProgressBar bodyTextPhotoProgressBar;
//		View bodyText2PhotoFrame;
        AppCompatImageView bodyText2Photo;
        //		ProgressBar bodyText2PhotoProgressBar;
//		ImageView imageViewHeaderRight;
//		CheckBox checkbox;
        Button buttonInvite;
        View rowView;
        int position;

        ViewHolder(View rowView, int i) {
            super(rowView);
            this.position = i;
//            this.rowView = rowView;
            this.rowView = rowView.findViewById(R.id.cardView);
//            mTextView = (TextView)rowView.findViewById(R.id.list_item);

            midContent = rowView.findViewById(R.id.body_text1_frame);
            imageViewHeaderLeft = rowView.findViewById(R.id.headerleft_icon);
            /* below few lines to capture imageview h and w */
            ViewTreeObserver vto = imageViewHeaderLeft.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    imageViewHeaderLeft.getViewTreeObserver().removeOnPreDrawListener(this);
                    finalHeight = imageViewHeaderLeft.getMeasuredHeight();
                    finalWidth = imageViewHeaderLeft.getMeasuredWidth();
//			        Log.d(TAG, "onCreate, Height: " + finalHeight + " Width: " + finalWidth);
                    return true;
                }
            });
            imageViewOverlayLeft = rowView.findViewById(R.id.overlay_corner);
            imageViewOverlayLeftBorder = rowView.findViewById(R.id.overlay_border);
            imageViewIndicatorRight = rowView.findViewById(R.id.indicator);
            textViewHeader = rowView.findViewById(R.id.header_text);
            imageViewSubject = rowView.findViewById(R.id.subject_icon);
            textViewSubject = rowView.findViewById(R.id.subject_text);
//			imageViewBody = (ImageView) rowView.findViewById(R.id.body_icon);
            textViewBody = rowView.findViewById(R.id.body_text);
//			imageViewBody2 = (ImageView) rowView.findViewById(R.id.body_icon2);
            textViewBody2 = rowView.findViewById(R.id.body_text2);
            textViewBody2Frame = rowView.findViewById(R.id.body_text2_frame);
            imageViewBody2Icon = rowView.findViewById(R.id.body_text2_icon);
//			imageViewHeaderRight = (ImageView) rowView.findViewById(R.id.headerright_icon);
            buttonInvite = rowView.findViewById(R.id.invite_button);
//			bodyTextPhotoFrame = (View) rowView.findViewById(R.id.body_text_pic_frame);
            bodyTextPhoto = rowView.findViewById(R.id.body_text_pic);
//			bodyTextPhotoProgressBar = (ProgressBar) rowView.findViewById(R.id.body_text_pic_progress_bar);
//			bodyText2PhotoFrame = (View) rowView.findViewById(R.id.body_text2_pic_frame);
            bodyText2Photo = rowView.findViewById(R.id.body_text2_pic);
//			bodyText2PhotoProgressBar = (ProgressBar) rowView.findViewById(R.id.body_text2_pic_progress_bar);
//			checkbox = (CheckBox) rowView.findViewById(R.id.check);
//			checkbox.setVisibility(View.GONE);
			/*if (checkable) {
				checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						IDTObject element = (IDTObject) checkbox.getTag();
						// element.setSelected(buttonView.isChecked());
						selectionList.put(clickposition, buttonView.isChecked());
					}
				});
				checkbox.setTag(getItem(position));
			} else {
				checkbox.setVisibility(View.INVISIBLE);
				// checkbox.setFocusable(true);
			}*/
			/*imageViewHeaderRight.setOnClickListener(new OnClickListener() {
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
						mob.setUsername(XMob.toFQMN(mob.getNumber(), CaltxtApp.getMyCountryCode()));
						if(ri.getCaltxt().trim().length()>0)
							mob.setHeadline("RE: "+ri.getCaltxt());//reply 19-Jan-15
					} else if (rowitem.getCName().equals("XMob")) {
						mob = (XMob) getItem(Integer.parseInt((v.getTag()).toString()));
					}
					caltxtInput.putExtra("IDTOBJECT", mob);
					context.startActivity(caltxtInput);
				}
			});*/
            /*imageViewOverlayLeft*/
            imageViewHeaderLeft.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    int index = Integer.parseInt((imageViewHeaderLeft.getTag()).toString());
                    if (context.adapter.getItem(index).getCName().equals("XCtx")) {
                        context.rowOnClickActionView(context.adapter.getItem(index));
                    }
                }
            });
            bodyText2Photo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pic_url = (String) bodyText2Photo.getTag();
                    Log.d(TAG, "pic_url " + pic_url);
                    Intent activity = new Intent(context.getActivity(), PhotoFullscreen.class);
                    activity.putExtra("URL", pic_url);
                    context.startActivity(activity);
//					Intent intent = new Intent();
//					intent.setAction(Intent.ACTION_VIEW);
//					intent.setDataAndType(Uri.parse(pic_url), "image/*");
//					context.startActivity(intent);
                }
            });
            bodyTextPhoto.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pic_url = (String) bodyTextPhoto.getTag();
                    Log.d(TAG, "pic_url " + pic_url);
                    Intent activity = new Intent(context.getActivity(), PhotoFullscreen.class);
                    activity.putExtra("URL", pic_url);
                    context.startActivity(activity);
//					Intent intent = new Intent();
//					intent.setAction(Intent.ACTION_VIEW);
//					intent.setDataAndType(Uri.parse(pic_url), "image/*");
//					context.startActivity(intent);
                }
            });
            imageViewIndicatorRight.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (imageViewIndicatorRight.getTag() == null)//its new contact indicator
                        return;
                    //its caltxt contact
                    Intent caltxtInput = new Intent(context.getActivity(), CaltxtInputActivity.class);
                    XMob mob = addressbook.getRegistered(imageViewIndicatorRight.getTag().toString());
                    if(mob.getUsername().length()==0 || mob.getUsername().equals(Addressbook.getInstance(context.getContext()).getMyProfile().getUsername())) {
                        // do not open caltxt input screen if this item is this user itself
                        Log.i(TAG, "rowOnClickActionOpen: do not open caltxt input screen if this item is this user itself");
                        return;
                    }
                    caltxtInput.putExtra("IDTOBJECT", mob);
//					caltxtInput.putExtra("VIEW", Constants.INPUT_VIEW);
                    context.startActivity(caltxtInput);
                }
            });
            buttonInvite.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    Log.d(TAG, "buttonInvite : " + buttonInvite.getTag());
                    if (Blockbook.getInstance(context.getContext()).get(buttonInvite.getTag()) != null) {
                        Blockbook.getInstance(context.getContext()).remove((String) buttonInvite.getTag());
                        Connection.get().addAction(Constants.contactUnblockedProperty, buttonInvite.getTag(), null);
//						notifyDataSetInvalidated();
//						notifyDataSetChanged();
                    } else {
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse("sms:+" + buttonInvite.getTag()));
                        sendIntent.putExtra("sms_body",
                                context.getString(R.string.service_sms_invite)
                                /*"Hi! Try new App for smart calling, to know more visit http://Caltxt.com"*/);
                        context.startActivity(sendIntent);
                    }
                }
            });
            rowView.setTag(this);
			/*rowView.setOnClickListener(new OnClickListener() {
			      @Override
			      public void onClick(View v) {
					IDTObject obj = (IDTObject) getItem(
							Integer.parseInt((((ViewHolder) v.getTag()).imageViewHeaderLeft.getTag()).toString()));
					rowOnClickAction(obj);
			      }
			    });*/
            textViewBody.setOnLongClickListener(new OnLongClickListener() {
//			midContent.setOnLongClickListener(new OnLongClickListener() {

                public boolean onLongClick(View v) {
                    int index = Integer.parseInt((textViewBody.getTag()).toString());
                    IDTObject obj = dtoListFiltered.get(index);
                    textViewBody.getTag();

//					IDTObject obj = (IDTObject) getItem(
//							Integer.parseInt((imageViewHeaderLeft.getTag()).toString()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ClipboardManager clipboard = (ClipboardManager) context.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = null;
                        if (obj.getCName().equals("XCtx")) {
                            clip = ClipData.newPlainText(obj.getBody(), obj.getBody());
                        } else if (obj.getCName().equals("XMob")) {
                            clip = ClipData.newPlainText(((XMob) obj).getNumber(), ((XMob) obj).getNumber());
                        }
                        if (clip != null)
                            clipboard.setPrimaryClip(clip);
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (obj.getCName().equals("XCtx")) {
                            clipboard.setText(obj.getBody());
                        } else if (obj.getCName().equals("XMob")) {
                            clipboard.setText(((XMob) obj).getNumber());
                        }
                    }

                    if (obj.getCName().equals("XCtx")) {
                        Notify.toast(v, context.getActivity(), context.getString(R.string.prompt_text_copied), Toast.LENGTH_LONG);
                    } else if (obj.getCName().equals("XMob")) {
                        Notify.toast(v, context.getActivity(), context.getString(R.string.prompt_number_copied), Toast.LENGTH_LONG);
                    }
                    return true;
                }
            });
            textViewBody2.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    int index = Integer.parseInt((textViewBody.getTag()).toString());
                    IDTObject obj = dtoListFiltered.get(index);
//					IDTObject obj = (IDTObject) getItem(
//							Integer.parseInt((imageViewHeaderLeft.getTag()).toString()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ClipboardManager clipboard = (ClipboardManager) context.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(obj.getFooter(), obj.getFooter());
                        clipboard.setPrimaryClip(clip);
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(obj.getFooter());
                    }
                    Notify.toast(v, context.getActivity(), context.getString(R.string.prompt_text_copied), Toast.LENGTH_LONG);
                    return true;
                }
            });
            rowView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    int index = Integer.parseInt((imageViewHeaderLeft.getTag()).toString());
                    if (context.mActionMode == null) {
                        context.rowOnClickActionOpen(context.adapter.getItem(index));
                    } else {
                        context.onListItemSelect(index);
                    }
                }
            });
            rowView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    int index = Integer.parseInt((imageViewHeaderLeft.getTag()).toString());
                    //allow multiple select on log view only
                    if (context.adapter.getItem(index).getCName().equals("XCtx")) {
                        context.onListItemSelect(index);
                        return true;
                    }
                    return true;
                }
            });
        }
    }
}
