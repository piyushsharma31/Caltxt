package com.jovistar.caltxt.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.service.WifiScanReceiver;
import com.jovistar.commons.bo.XMob;
import com.jovistar.commons.util.Logr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CaltxtStatusRecyclerAdapter extends RecyclerView.Adapter<CaltxtStatusRecyclerAdapter.ViewHolder> {
    private static final String TAG = "CaltxtStatusRecyclerAdapter";

    private List<CaltxtStatus> list;
    private Activity mContext;

    public CaltxtStatusRecyclerAdapter(Context context, List<CaltxtStatus> feedItemList) {
        this.list = feedItemList;
        this.mContext = (Activity) context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = null;
        if (list.size() > 5) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_status_griditem, viewGroup,
                    false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_status_listitem, viewGroup,
                    false);
        }

        return new ViewHolder(v, i);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        CaltxtStatus item = list.get(i);

        holder.icon.setImageResource(item.getResourceID());
        holder.label.setText(item.getStatusCode());
        holder.icon.setTag(i);
        holder.rowView.setTag(i);
        if (list.get(i).getStatusCode().equals(Addressbook.getInstance(mContext).getMyProfile().getPlace())) {
            holder.icon.setBackgroundResource(Addressbook.getInstance(mContext).geStatusBackgroundResource(Addressbook.getInstance(mContext).getMyProfile()));
        } else if (list.get(i).getStatusCode().equals(Addressbook.getInstance(mContext).getMyProfile().getHeadline())) {
            holder.icon.setBackgroundResource(Addressbook.getInstance(mContext).geStatusBackgroundResource(Addressbook.getInstance(mContext).getMyProfile()));
        } else {
            if (list.get(i).getStatusCode().equals("Automatic Response")
                    && Addressbook.getInstance(mContext).getMyProfile().isAutoResponding()) {
                holder.icon.setBackgroundResource(Addressbook.getInstance(mContext).geStatusBackgroundResource(Addressbook.getInstance(mContext).getMyProfile()));
            } else {
                holder.icon.setBackgroundResource(R.drawable.circle_grey);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != list ? list.size() : 0);
    }

    public void refreshList() {
        list.clear();
        list.addAll(Addressbook.getInstance(mContext).getPlaceList());
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // Declaration of Views

        ImageButton icon;
        TextView label;
        TextView subject;
        View rowView;
        int position;

        public ViewHolder(View view, int i) {
            super(view);

            this.rowView = view.findViewById(R.id.activity_profile_status);
            this.icon = view.findViewById(R.id.activity_profile_status_icon);
            this.label = view.findViewById(R.id.activity_profile_status_label);
            this.subject = view.findViewById(R.id.activity_profile_status_subject);
//			this.label.setVisibility(View.GONE);
            this.subject.setVisibility(View.GONE);

            this.rowView.setOnClickListener(this);
            this.icon.setOnClickListener(this);
            this.rowView.setOnLongClickListener(this);
            this.icon.setOnLongClickListener(this);

            // Find Views
        }

        @Override
        public void onClick(final View view) {
            // Onclick of row
            final int index = Integer.parseInt((view.getTag()).toString());
//        	ViewHolder holder = (ViewHolder) view.getTag();
            final CaltxtStatus c = list.get(index);

            //cancel DND status, auto response (if set)
            Notify.notify_caltxt_dnd_cancel(mContext);

            if (c.getStatusCode().equals("Automatic Response")) {
                Intent it = new Intent(mContext, QuickResponseEdit.class);
                mContext.startActivity(it);
                mContext.finish();
            } else if (c.getStatusCode().startsWith("In loo")) {
                Intent it = new Intent(mContext, Places.class);
                mContext.startActivity(it);
                mContext.finish();
            } else {
                //reset auto response if set
                XQrp qrp = Persistence.getInstance(mContext).getQuickAutoResponse();
                if ((qrp != null && qrp.getAutoResponseEndTime() > Calendar.getInstance().getTimeInMillis())) {
                    QuickResponseEdit.resetAutoResponse(mContext);
                    Logr.d(TAG, "resetAutoResponse");
                }
                if (c.getStatusCode().equals(XMob.STRING_STATUS_DND)) {
                    Notify.notify_caltxt_call_dnd_enabled(mContext, Calendar.getInstance().getTimeInMillis());
                } else {
                    //reset DND.
                    // commented 10-DEC-16, its auto reset when other status (available, busy..) is set
                }

//        		Addressbook.get().setPlaceInStatusList(c);
/*        		if(c.getStatusCode().startsWith("at ")) {
                    Addressbook.getInstance(mContext).changeMyStatus(Addressbook.getInstance(mContext).getMyProfile().getHeadline(),
							c.getStatusCode());
				} else if(c.getStatusCode().equals("Forget")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CaltxtAlertDialogTheme);
					builder.setPositiveButton(R.string.prompt_confirm_yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {

									Addressbook.getInstance(mContext).changeMyStatus(Addressbook.getInstance(mContext).getMyProfile().getHeadline(),
											c.getStatusCode());
									mContext.finish();

								}
							});
					builder.setNegativeButton(R.string.prompt_confirm_no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
									mContext.finish();
								}
							});
					String [] additionalArgs = {Addressbook.getInstance(mContext).getMyProfile().getPlace()};
					String actionTaken = mContext.getString(R.string.prompt_caltxt_forget_place, (Object[]) additionalArgs);
					builder.setMessage(actionTaken).setTitle(R.string.prompt_caltxt_forget_place_title);
					AlertDialog dialog = builder.create();
					dialog.show();
        		} else {
        			Addressbook.getInstance(mContext).changeMyStatus(c.getStatusCode(),
        					Addressbook.getInstance(mContext).getMyProfile().getPlace());
        		}
*/
                if (c.getStatusCode().startsWith("at ")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CaltxtAlertDialogTheme);
                    builder.setPositiveButton(R.string.prompt_confirm_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    Addressbook.getInstance(mContext).changeMyStatus(Addressbook.getInstance(mContext).getMyProfile().getHeadline(),
                                            c.getStatusCode());

                                    final WifiScanReceiver wifiSR = new WifiScanReceiver();

                                    //scan neighbouring cells at this location and tag this place to them
                                    wifiSR.setAction(c.getStatusCode());
                                    //wifiSR.startCellScan(mContext.getApplicationContext());
//									Persistence.getInstance(mContext).insertXPLC(c.getStatusCode(), CallManager.getInstance().getUniqueCellLocation(mContext), XPlc.NETWORK_TYPE_CELL);

                                    //scan wifi at this location and tag this place to them
//        			ConnectivityBroadcastReceiver.scanWiFiAccessPoints(mContext,c.getStatusCode());
//									mContext.startService(new Intent(mContext, WifiScanService.class).putExtra("place", c.getStatusCode()));
//									wifiSR.setAction(c.getStatusCode());
                                    wifiSR.startWifiAndCellScan(mContext.getApplicationContext());

                                    //if tagging place for first time, tell user what it does!
                                    if (Persistence.getInstance(mContext).getCountXPLC() == 1) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CaltxtAlertDialogTheme);
                                        builder.setPositiveButton(R.string.close,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        mContext.finish();

                                                    }
                                                });
                                        String[] additionalArgs = {c.getStatusCode()};
                                        String actionTaken = mContext.getString(R.string.prompt_caltxt_place_remember_msg, (Object[]) additionalArgs);
                                        builder.setMessage(actionTaken);//.setTitle(R.string.prompt_caltxt_place_remember);
                                        AlertDialog dlg = builder.create();
                                        dlg.show();
                                    } else {
                                        mContext.finish();
                                    }
					/*if(Settings.remember_places.equals(Settings.PLACES_REMEMBER_ALWAYS)) {

						Persistence.getInstance(mContext).insertXPLC(c.getStatusCode(), CallHandler.get().getUniqueCellLocation());
	                    mContext.finish();

        			} else if(Settings.remember_places.equals(Settings.PLACES_REMEMBER_ALWAYS_ASK)) {
            			AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CaltxtAlertDialogTheme);
            			builder.setPositiveButton(R.string.prompt_confirm_yes,
            					new DialogInterface.OnClickListener() {
            						public void onClick(DialogInterface dialog, int id) {

            							Persistence.getInstance(mContext).insertXPLC(c.getStatusCode(), CallHandler.get().getUniqueCellLocation());
            		                    mContext.finish();

            						}
            					});
            			builder.setNegativeButton(R.string.prompt_confirm_no,
            					new DialogInterface.OnClickListener() {
            						public void onClick(DialogInterface dialog, int id) {
            							dialog.cancel();
            		                    mContext.finish();
            						}
            					});
            			String [] additionalArgs = {c.getStatusCode()};
            			String actionTaken = mContext.getString(R.string.prompt_caltxt_place_remember_msg, (Object[]) additionalArgs);
            			builder.setMessage(actionTaken).setTitle(R.string.prompt_caltxt_place_remember);
            			AlertDialog dialog = builder.create();
            			dialog.show();
        			} else if(Settings.remember_places.equals(Settings.PLACES_REMEMBER_NEVER)) {

                        mContext.finish();
        			}
        		} else {
                    mContext.finish();*/

                                }
                            });
                    builder.setNegativeButton(R.string.prompt_confirm_no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    if (c.getStatusCode().equals(Addressbook.getInstance(mContext).getMyProfile().getPlace())) {
                                        Notify.toast(view, mContext, mContext.getString(R.string.prompt_caltxt_forget_place_suggest), 3000);
                                    }
//									Notify.toast(this, this, "", 3000);
//									mContext.finish();

                                }
                            });
                    String[] additionalArgs = {c.getStatusCode()};
                    String actionTaken = mContext.getString(R.string.prompt_caltxt_tag_place_change_description, (Object[]) additionalArgs);
                    String actionTitle = mContext.getString(R.string.prompt_caltxt_tag_place_change, (Object[]) additionalArgs);
                    builder.setMessage(actionTaken).setTitle(actionTitle);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else if (c.getStatusCode().equals("Add")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CaltxtAlertDialogTheme);
                    builder.setTitle("Add place");

                    // Set up the input
                    final EditTextCaltxt input = new EditTextCaltxt(mContext);
                    input.setHint("Mom's House, Dany's Place, etc.");
                    input.setSingleLine(true);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton(R.string.action_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (input.getText().toString().length() == 0) {
                                Notify.toast(view, mContext, "Place name cannot be blank", Toast.LENGTH_LONG);
                                return;
                            }
                            Addressbook.getInstance(mContext).addPlace(input.getText().toString());
                            refreshList();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                } else if (c.getStatusCode().equals("Forget")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CaltxtAlertDialogTheme);
                    builder.setPositiveButton(R.string.prompt_confirm_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    Addressbook.getInstance(mContext).changeMyStatus(Addressbook.getInstance(mContext).getMyProfile().getHeadline(),
                                            c.getStatusCode());
                                    mContext.finish();

                                }
                            });
                    builder.setNegativeButton(R.string.prompt_confirm_no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    mContext.finish();
                                }
                            });
                    String[] additionalArgs = {"<b>" + Addressbook.getInstance(mContext).getMyProfile().getPlace() + "</b>"};
                    String actionTaken = mContext.getString(R.string.prompt_caltxt_forget_place, (Object[]) additionalArgs);
                    builder.setMessage(Html.fromHtml(actionTaken)).setTitle(R.string.prompt_caltxt_forget_place_title);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Addressbook.getInstance(mContext).changeMyStatus(c.getStatusCode(),
                            Addressbook.getInstance(mContext).getMyProfile().getPlace());
                    mContext.finish();
                }

                // addAction moved to changeMyStatus
//				RebootService.getConnection(mContext).addAction(Constants.myStatusChangeProperty, null, c.getStatusCode());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int index = Integer.parseInt((view.getTag()).toString());
            final CaltxtStatus c = list.get(index);

            ArrayList<String> list = Persistence.getInstance(mContext).getAllPlaces();
            boolean isStandardPlace = true;

            for(int i=0; i<list.size(); i++) {
                if(list.get(i).equals(c.getStatusName())) {
                    isStandardPlace = false;
                }
            }

            if (c.getStatusCode().startsWith("at ")) {
                if(isStandardPlace) {
                    Intent it = new Intent(mContext, Places.class);
                    mContext.startActivity(it);
//	                mContext.finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CaltxtAlertDialogTheme);
                    builder.setMessage("Remove place \""+c.getStatusName()+"\" ?");

                    // Set up the buttons
                    builder.setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Addressbook.getInstance(mContext).removePlace(c.getStatusName());
                            refreshList();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            return false;
        }
    }
}
