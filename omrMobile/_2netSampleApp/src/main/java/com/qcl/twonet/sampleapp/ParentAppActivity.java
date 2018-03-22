/**
 * Copyright (C) 2013-2014 Qualcomm Life, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Qualcomm
 * Life, Inc.
 * <p>
 * The following sample code illustrates various aspects of the 2net Mobile SDK.
 * <p>
 * The sample code herein is provided for your convenience, and has not been
 * tested or designed to work on any particular system configuration. It is
 * provided AS IS and your use of this sample code, whether as provided or with
 * any modification, is at your own risk. Neither Qualcomm Life, Inc. nor any
 * affiliate takes any liability nor responsibility with respect to the sample
 * code, and disclaims all warranties, express and implied, including without
 * limitation warranties on merchantability, fitness for a specified purpose,
 * and against infringement.
 */

package com.qcl.twonet.sampleapp;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.qcl.twonet.sampleapp.AlertDialogFragment.AlertDialogListener;
import com.qcl.twonet.sampleapp.EnterAuthCodeDialog.EnterAuthCodeDialogListener;
import com.qcl.vh.content.VirtualHubProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Main activity of the 2net Mobile Application. This Activity displays a list of configured sensors (medical
 * devices) and their latest status (last reading time and values, etc).
 */
public class ParentAppActivity extends FragmentActivity implements EnterAuthCodeDialogListener, AlertDialogListener {
    final static String TAG = "SampleParentApp +++";
    final static String LAST_READING_PREFERENCES_NAME = "last_reading";
    final static int REQUEST_SHOW_EULA = 100;
    public static final String ACTION_AUTH_RESULT_RECEIVED = "com.qcl.vh.action.AUTH_RESULT_RECEIVED";
    public static final String EXTRA_AUTH_RESULT = "com.qcl.vh.extra.AUTH_STATUS";

    public String authname = null, authpass = null;
    public static String wearpass;

    List<DataHolder> mListItems = new ArrayList<ParentAppActivity.DataHolder>();
    Handler mHandler = new Handler();
    // Instantiate the RequestQueue.
    RequestQueue queue;

    BluetoothAdapter mBtAdapter;

    private int mAuthResult = 0;

    private boolean mResumed;

    private boolean mShowAuthErrorDialogOnResume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set main layout for this Activity
        setContentView(R.layout.parent_app_activity);

        Log.d(TAG, "onCreate");
        Log.d(TAG, "intent had : " + getIntent().getStringExtra("authpwd")); //rj? if called from watch, we r passing n the PWD here
        if (getIntent().getStringExtra("authpwd") != null) {
            wearpass = getIntent().getStringExtra("authpwd");
        } else {
            wearpass= "Ilohcbe2107";
        }

        new UntrustSSLCerts().run();         //rj? note this should never be used outside of testing/debug since it trusts all certs

        // Look up the ListView and set its adapter
        ListView lv = (ListView) findViewById(R.id.device_list);
        lv.setAdapter(mAdapter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Start AsyncTask to load devices
        new LoadDevices().execute();

        // Register content observer to watch for device list changes
        final Uri URI_DEVICES = VirtualHubProvider.getDevicesUri(this);
        getContentResolver().registerContentObserver(URI_DEVICES, true, mContentObserver);

        // Register listener of SharedPreferences changes
        getSharedPreferences(LAST_READING_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(mPrefChanged);

        // Register Authentication Status listener
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_AUTH_RESULT_RECEIVED);
        registerReceiver(mAuthResultReceiver, filter);

        // Start periodic view updates
        mHandler.post(mUpdateViews);

        // Show Auth Code Entry dialog if VHID is not set
        String vhId = Utility.getVirtualHubId(this);
        if (vhId == null && savedInstanceState == null) {
            // Display the dialog
            showEnterAuthCodeDialog();
        }

        // Display EULA screen if it hasn't been accepted yet
        if (!Utility.hasUserAcceptedEula(this)) {
            //rj? startActivityForResult(new Intent(getApplicationContext(), EulaActivity.class), REQUEST_SHOW_EULA);
        }

        // Certificate is expired, need to reauthenticate.
        if (Utility.isCertExpired(this)) {
            showEnterAuthCodeDialog();
        }

        // queue = Volley.newRequestQueue(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mResumed = false;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mResumed = true;
        if (mShowAuthErrorDialogOnResume) {
            mShowAuthErrorDialogOnResume = false;
            showAuthErrorDialog();
        }
        // cancel any notifications we may have created while the app wasn't visible
        NotificationManager nm = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    /**
     * Observer of changes to the Medical device list or their attributes
     */
    final ContentObserver mContentObserver = new ContentObserver(mHandler) {
        public void onChange(boolean selfChange) {
            // Reload device list from the content provider
            new LoadDevices().execute();
        }
    };

    // Displays the Auth Code entry dialog
    private void showEnterAuthCodeDialog() {
        EnterAuthCodeDialog dialog;
        Fragment authCodeFragment = getSupportFragmentManager().findFragmentByTag("auth_code");
        if (null == authCodeFragment) {
            dialog = new EnterAuthCodeDialog();
        } else {
            // in some error cases (Airplane mode ON), auth result callback comes quickly even before, "auth_code"
            // fragment has been removed, in that case re-use it.
            dialog = (EnterAuthCodeDialog) authCodeFragment;
        }

        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "auth_code");
    }

    // This runs every 1000 ms to help keep the timestamps current
    final Runnable mUpdateViews = new Runnable() {
        @Override
        public void run() {
            mAdapter.notifyDataSetChanged();
            if (!isFinishing()) {
                mHandler.postDelayed(mUpdateViews, 1000L);
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        // Unregister listeners and observers when the Activity is destroyed
        getSharedPreferences(LAST_READING_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(mPrefChanged);
        getContentResolver().unregisterContentObserver(mContentObserver);

        // unregister broadcast receiver
        unregisterReceiver(mAuthResultReceiver);

    }

    final SharedPreferences.OnSharedPreferenceChangeListener mPrefChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            new LoadDevices().execute();
        }
    };

    /**
     * A View representing a Medical Device shown as a list item.
     */
    class DeviceView extends RelativeLayout {

        ImageView mIcon;
        ImageView mIconLed;
        TextView mTitle;
        TextView mSubtitleReading;
        TextView mSubtitleTime;
        TextView mStatusLine;
        int mLedState = 0;

        public DeviceView(Context context) {
            super(context);
            // Inflate the layout for this view
            LayoutInflater.from(context).inflate(R.layout.device_item, this);
            // Initialize all visual elements
            mIcon = (ImageView) findViewById(R.id.icon);
            mIconLed = (ImageView) findViewById(R.id.icon_led);
            mTitle = (TextView) findViewById(R.id.title);
            mSubtitleReading = (TextView) findViewById(R.id.subtitle_reading);
            mSubtitleTime = (TextView) findViewById(R.id.subtitle_time);
            mStatusLine = (TextView) findViewById(R.id.status_line);
            // Start LED animation
            AnimationDrawable ad = (AnimationDrawable) mIconLed.getDrawable();
            ad.start();
        }

    }

    /**
     * Adapter for the ListView that controls View creation and recycling.
     */
    final BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mListItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return (long) position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceView v = (DeviceView) convertView;
            if (null == v) {
                v = new DeviceView(getApplicationContext());
            }
            DataHolder dh = mListItems.get(position);
            v.mIcon.setImageResource(nameToResId(dh.name));
            v.mTitle.setText(dh.name);
            long now = System.currentTimeMillis();
            CharSequence subtitle = (0L == dh.lastReadingDate ? getString(R.string.never) : "Received "
                    + DateUtils.getRelativeTimeSpanString(dh.lastReadingDate, now,
                    DateUtils.MINUTE_IN_MILLIS));
            if (now - dh.lastReadingDate < 60000L) {
                subtitle = getString(R.string.just_now);
                v.mIconLed.setVisibility(View.VISIBLE);
            } else {
                v.mIconLed.setVisibility(View.GONE);
            }
            if (dh.lastReading != null && dh.lastReading.length() > 0) {
                v.mSubtitleReading.setVisibility(View.VISIBLE);
                v.mSubtitleReading.setText(dh.lastReading);
                if (dh.lastReadingDeviceTime > 0L) {
                    v.mSubtitleReading.append("\n"
                            + DateUtils.formatDateTime(getApplicationContext(), dh.lastReadingDeviceTime,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));
                }
            } else {
                v.mSubtitleReading.setVisibility(View.GONE);
            }
            v.mSubtitleTime.setText(subtitle);
            if (dh.active) {
                if (dh.lastReadingDate > 0L) {
                    v.mStatusLine.setVisibility(View.GONE); // hide status line when active with readings
                } else {
                    v.mStatusLine.setVisibility(View.VISIBLE);
                    v.mStatusLine.setText(R.string.active);
                }
            } else {
                v.mStatusLine.setVisibility(View.VISIBLE);
                v.mStatusLine.setText(R.string.inactive);
            }
            return v;
        }

    };

    /**
     * Maps device name to a corresponding icon resource
     */
    static int nameToResId(String name) {
        name = name.toLowerCase(Locale.US);
        if (name.contains("nonin")) {
            return R.drawable.ic_heart;
        } else if (name.contains("pressure") || (name.contains("bp") && name.contains("monitor"))) {
            return R.drawable.ic_bloodpressure;
        } else if (name.contains("2-in-1")) {
            return R.drawable.ic_blood_and_bp;
        } else if (name.contains("gluco")) {
            return R.drawable.ic_blood;
        } else if (name.contains("scale")) {
            return R.drawable.ic_scale;
        } else if (name.contains("asthma")) {
            return R.drawable.ic_inhaler;
        } else if (name.contains("thermo")) {
            return R.drawable.ic_thermometer;
        }
        return R.drawable.ic_heart;
    }

    /**
     * Simple value object to pass data to the ListView adapter.
     */
    static class DataHolder {
        String name;
        String address;
        boolean active;
        long lastReadingDeviceTime;
        long lastReadingDate;
        String lastReading;
    }

    /**
     * Comparator that allows us to sort devices in the list by date of the last reading. Devices with most recent
     * readings are promoted to the top.
     */
    final static Comparator<DataHolder> DATE_COMPARATOR = new Comparator<DataHolder>() {
        @Override
        public int compare(DataHolder d1, DataHolder d2) {
            return Long.valueOf(d2.lastReadingDate).compareTo(Long.valueOf(d1.lastReadingDate));
        }
    };

    /**
     * An {@link AsyncTask} implementation that allows us to load and update the list of devices and attributes in the
     * background.
     */
    class LoadDevices extends AsyncTask<String, String, Collection<DataHolder>> {
        String deviceinfo;
        @Override
        protected Collection<DataHolder> doInBackground(String... params) {
            final Context context = getApplicationContext();
            ContentAccess dm = new ContentAccess(context);
            Set<MedicalDevice> all = dm.findAll();

            BluetoothAdapter adapter = mBtAdapter;
            Set<BluetoothDevice> bondedDevices = null != adapter ? adapter.getBondedDevices()
                    : new HashSet<BluetoothDevice>();
            HashSet<String> bondedAddresses = new HashSet<String>();
            for (BluetoothDevice d : bondedDevices) {
                bondedAddresses.add(d.getAddress());
            }

            ArrayList<DataHolder> list = new ArrayList<ParentAppActivity.DataHolder>();
            SharedPreferences sp = context.getSharedPreferences(
                    ParentAppActivity.LAST_READING_PREFERENCES_NAME, Context.MODE_PRIVATE);
            for (MedicalDevice md : all) {
                DataHolder holder = new DataHolder();
                holder.address = md.getMacAddress();
                holder.name = getName(md.getModel(), md.getType());

                 deviceinfo = String.format("Retrieved model %s type %s name %s sn %s", md.getModel(), md.getType(), holder.name, md.getSerialNumber());
                Log.v(TAG, "Collection call:" + deviceinfo);

                holder.lastReadingDate = sp.getLong(
                        Utility.keyForAddress("lastWhen", holder.address), 0L);
                holder.lastReading = sp.getString(Utility.keyForAddress("lastReading", holder.address), "");
                holder.lastReadingDeviceTime = sp.getLong(Utility.keyForAddress("lastReadingTime", holder.address), 0L);

                boolean stickyMaster = (md.isMaster() && md.getChannelMode() == MedicalDevice.CHANNEL_MODE_STICKY);
                boolean bonded = bondedAddresses.contains(md.getMacAddress());

                holder.active = (stickyMaster && bonded && md.getChannel() > 0) || (!stickyMaster && bonded)
                        || (null != adapter && !adapter.isEnabled() && md.getLastSeenTime() > 0);

                // Exception for Entra devices that unpair when idle
                if (!holder.active && holder.lastReadingDate > 0L && holder.name != null
                        && holder.name.toLowerCase(Locale.US).contains("entra")) {
                    holder.active = true;
                }

                list.add(holder);
            }
            Collections.sort(list, DATE_COMPARATOR);
            return list;
        }

        // Maps device model and type to user friendly text for display
        private String getName(String model, String type) {
            if (model == null) return "unknown " + type;

            model = model.toLowerCase(Locale.US);
            if (model.contains("nonin")) {
                return "Nonin " + type;
            }
            if (model.contains("fora")) {
                return "Fora " + type;
            }
            if (model.contains("asthmapolis")) {
                return "Asthmapolis " + type;
            }
            if (model.contains("entra")) {
                return "Entra " + type;
            }
            if (model.contains("choicemmed")) {
                return "ChoiceMMed " + type;
            }
            if (model.contains("omron")) {
                return "Omron " + type;
            }
            if (model.contains("a&d") || model.contains("and")) {
                return "A&D " + type;
            }
            if (model.contains("idt")) {
                return "IDT " + type;
            }
            if (model.contains("continua")) {
                return "Continua " + type;
            }
            if (model.contains("polymap")) {
                return "Polymap " + type;
            }
            if (model.contains("wgn") || model.contains("walgreens")) {
                return "Walgreens " + type;
            }
            if (model.contains("nipro")) {
                return "Nipro " + type;
            }
            if (model.contains("roche")) {
                return "Roche " + type;
            }
            if (model.contains("jnj")) {
                return "J&J " + type;
            }
            return "unknown " + type;
        }

        protected void onPostExecute(Collection<DataHolder> result) {
            mListItems.clear();
            mListItems.addAll(result);
            mAdapter.notifyDataSetChanged();

            Toast.makeText(getApplicationContext(), deviceinfo, Toast.LENGTH_LONG).show(); //rj?

        }

    }

    private final BroadcastReceiver mAuthResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mAuthResult = intent.getIntExtra(EXTRA_AUTH_RESULT, 1);
            Log.d(TAG, "Received auth callback with result " + mAuthResult);
            if (mAuthResult == 0) {
                Utility.setCertExpired(context, false); //rj? use something similar to log out
            }
            handleAuthResult();
        }

    };


    /**
     * Callback from {@link EnterAuthCodeDialog} received when the user enters and submits a new Auth Code value
     */

    @Override
    public void onEnterAuthCodeDialogOk(final String authname, final String authpass) {
        this.authname = authname;
        try {
            AuthParser authParser = new AuthParser();
            authParser.execute(authname, authpass);
        } catch (Exception e) {
            Log.v(TAG, "        exc thr:" + e.getMessage());
        }

        ProgressDialogFragment pdf = new ProgressDialogFragment();
        pdf.setCancelable(false);
        pdf.show(getSupportFragmentManager(), "progress_bar");

    }

    private class AuthParser extends AsyncTask<String, Void, String> {
    //rj? replaced volley calls with this class

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            Log.v("++++", "                     authcode async");

            HttpGet httpGet = new HttpGet(
                    "https://openidm.aeet.fridam.aeet-forgerock.com/openidm/endpoint/twonetcommdata?_queryFilter=userName%20eq%20'" + params[0] + "'");
            httpGet.setHeader("X-OpenIDM-Username", params[0]); //authname
            httpGet.setHeader("X-OpenIDM-Password", params[1]); //authpwd

            String payload;
            JSONObject jPayload, jCode = null;
            String authCode = null;

            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();
                payload = EntityUtils.toString(entity);

                jPayload = new JSONObject(payload);
                Object jResult = jPayload.get("result");
                JSONArray jsonArray = new JSONArray(jResult.toString());
                JSONObject obj = jsonArray.getJSONObject(0);

                authCode = obj.getString("authCode2Net");
                Log.v(TAG, "       got " + authCode);

            } catch (Exception e) {
                Log.v(TAG, "       error in doInBG " + e.getMessage());
                return e.getLocalizedMessage();
            }

            try {
                Utility.authenticate(ParentAppActivity.this, getString(R.string.application_id), authCode);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "true"; // rj this never gets checked, btw
        }

        protected void onPostExecute(String results) {
            Log.v(TAG, "authParser.onPostRslt got: " + results);
        }
    }

    public static class UntrustSSLCerts { //rj? this is only to get past excp thrown about cert
        protected static final String TAG = "NukeSSLCerts +++";

        public static void run() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                                return myTrustedAnchors;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
            } catch (Exception e) {
                Log.v(TAG, "        trustAllCerts E");
            }
        }
    }


    /**
     * A callback from {@link EnterAuthCodeDialog} received when the user cancels the dialog.
     */
    @Override
    public void onEnterAuthCodeDialogCancel() {
        // We cannot proceed without the Auth Code, so just finish the Activity
        finish();
    }

    public void logoutClick(View someview) { //rj? UI <> have a logout btn, so we added one
        Log.v(TAG, "        LOGGING OUT");
        Utility.setCertExpired(getApplicationContext(), true);
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            ((ActivityManager)getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
        } else {
            // use old hacky way, which can be removed
            // once minSdkVersion goes above 19 in a few years.
        }


        finish();
    }

    /**
     * This Progress dialog is shown when an Auth Code entered by the user is being validated.
     */
    public static class ProgressDialogFragment extends DialogFragment {

        static final String TAG = ParentAppActivity.TAG + ".ProgressDialogFragement";

        private boolean mDismissOnResume;

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setTitle(getString(R.string.authenticating_title));
            dialog.setMessage(getString(R.string.authenticating_message));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mDismissOnResume) {
                super.dismiss();
            }
        }

        @Override
        public void dismiss() {
            if (isResumed()) {
                super.dismiss();
            } else {
                // The dialog is not in the foreground, postpone until it's resumed
                mDismissOnResume = true;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (REQUEST_SHOW_EULA == requestCode) {
            if (RESULT_OK == resultCode) {
                Utility.setUserAcceptedEula(getApplicationContext());
            } else {
                finish();
            }
        }
    }

    private void dismissAuthProgressDialog() {
        ProgressDialogFragment pdf = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(
                "progress_bar");
        if (pdf != null) { // if dialog is still visible (user has not navigated away), dismiss it
            pdf.dismiss();
        } else {
            Log.w(TAG, "Progress dialog fragment not found!");
        }
    }

    private void handleAuthResult() {
        dismissAuthProgressDialog();
        if (mAuthResult != 0) {
            if (mResumed) {
                showAuthErrorDialog();
            } else {
                // Re-display Auth Error dialog on Activity resume
                mShowAuthErrorDialogOnResume = true;
            }
        }
    }

    void showAuthErrorDialog() {
        // Create the fragment and show it as a dialog.
        DialogFragment newFragment = AlertDialogFragment.newInstance(R.string.auth_err_dialog_title,
                R.string.auth_err_dialog_text);
        newFragment.show(getSupportFragmentManager(), "alert_dialog");
    }

    @Override
    public void onAuthErrorDialogOk() {
        showEnterAuthCodeDialog();
    }

    @Override
    public void onAuthErrorDialogCancel() {
        // We cannot proceed without the Auth Code, so just finish the Activity
        finish();
    }
}

//        try {
//            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://openidm.aeet.fridam.aeet-forgerock.com/openidm/endpoint/twonetcommdata", null, new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject s) {
//                    ///handle response from service
//                    try {
//                        Log.v(TAG, "        respon recv!!" );
//                        String authCode = s.getString("authCode");
//                        Utility.authenticate(ParentAppActivity.this, getString(R.string.application_id), authCode);
//                    } catch (JSONException e) {
//                        Log.v(TAG, " onResp:" + e.getMessage());
//                        finish();
//                    }
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError e) {
//                    //handle error response
//                    Log.v(TAG, " onErr!:" + e.getMessage());
//                    finish();
//                }
//            }) {
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Log.v(TAG, " map");
//                    Map<String, String> params = new HashMap<String, String>();
//                    return params;
//                }
//
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    Log.v(TAG, " map:" );
//                    Map<String, String> headers = new HashMap<String, String>();
//                    String credentials = authName + ":" + authPass;
//                    String auth = "Basic "
//                            + Base64.encodeToString(credentials.getBytes(),
//                            Base64.NO_WRAP);
//                    headers.put("Authorization", auth);
//                    return headers;
//                }
//            };
//            queue.add(request);
//        } catch (Exception e) {
//            Log.v(TAG, "        exc thr:" + e.getMessage());
//        }
