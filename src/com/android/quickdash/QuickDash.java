package com.android.quickdash;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Power;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.IWindowManager;
import android.view.Surface;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.internal.telephony.Phone;

public class QuickDash extends Activity implements OnDismissListener, OnClickListener {

    /* Buttons */
	private ImageButton mWifiButton;
    private ImageButton mDataButton;
    private ImageButton mSoundButton;
    private ImageButton mRotationButton;
    private ImageButton mAirplaneButton;
    private ImageButton mLteButton;
    private ImageButton mTetherButton;
    private ImageButton mBluetoothButton;
    private ImageButton mGpsButton;
    private ImageButton mBrightnessButton;
    private ImageButton mTorchButton;
    private ImageButton mSyncButton;

    /* Button Descriptions */
    private TextView mWifiDesc;
    private TextView mDataDesc;
    private TextView mSoundDesc;
    private TextView mRotationDesc;
    private TextView mAirplaneDesc;
    private TextView mLteDesc;
    private TextView mTetherDesc;
    private TextView mBluetoothDesc;
    private TextView mGpsDesc;
    private TextView mBrightnessDesc;
    private TextView mTorchDesc;
    private TextView mSyncDesc;

    int mWifiApEnabled;
    int mRingerMode;
    int mNetworkMode;
    int mBrightnessLevel;
    boolean mRotationEnabled;
    boolean mAirplaneEnabled;
    boolean mAutoBrightEnabled;
    boolean syncValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		LayoutInflater factory = LayoutInflater.from(this);
		final View mView = factory.inflate(
				R.layout.settings, null);
		AlertDialog alert = new AlertDialog.Builder(this).setView(mView).create();
		alert.setOnDismissListener(this);

        registerClickListener(mView);
        updateButtonState();

		alert.show();
	}

    private void registerClickListener(View mView) {

        /* Wifi toggler */
        mWifiButton = (ImageButton) mView.findViewById(R.id.wifi_button);
        mWifiDesc = (TextView) mView.findViewById(R.id.wifi_desc);
        mWifiButton.setOnClickListener(this);

        /* Mobile data toggler */
        mDataButton = (ImageButton) mView.findViewById(R.id.data_button);
        mDataDesc = (TextView) mView.findViewById(R.id.data_desc);
        mDataButton.setOnClickListener(this);

        /* Sound toggler */
        mSoundButton = (ImageButton) mView.findViewById(R.id.sound_button);
        mSoundDesc = (TextView) mView.findViewById(R.id.sound_desc);
        mSoundButton.setOnClickListener(this);

        /* Auto rotate toggler */
        mRotationButton = (ImageButton) mView.findViewById(R.id.rotation_button);
        mRotationDesc = (TextView) mView.findViewById(R.id.rotation_desc);
        mRotationButton.setOnClickListener(this);

        /* Airplane mode toggler */
        mAirplaneButton = (ImageButton) mView.findViewById(R.id.airplane_button);
        mAirplaneDesc = (TextView) mView.findViewById(R.id.airplane_desc);
        mAirplaneButton.setOnClickListener(this);

        /* Lte Toggler */
        mLteButton = (ImageButton) mView.findViewById(R.id.lte_button);
        mLteDesc = (TextView) mView.findViewById(R.id.lte_desc);
        mLteButton.setOnClickListener(this);

        /* Tether Toggler */
        mTetherButton = (ImageButton) mView.findViewById(R.id.tether_button);
        mTetherDesc = (TextView) mView.findViewById(R.id.tether_desc);
        mTetherButton.setOnClickListener(this);

        /* Bluetooth Toggler */
        mBluetoothButton = (ImageButton) mView.findViewById(R.id.bluetooth_button);
        mBluetoothDesc = (TextView) mView.findViewById(R.id.bluetooth_desc);
        mBluetoothButton.setOnClickListener(this);

        /* Gps Toggler */
        mGpsButton = (ImageButton) mView.findViewById(R.id.gps_button);
        mGpsDesc = (TextView) mView.findViewById(R.id.gps_desc);
        mGpsButton.setOnClickListener(this);

        /* Brightness Toggler */
        mBrightnessButton = (ImageButton) mView.findViewById(R.id.brightness_button);
        mBrightnessDesc = (TextView) mView.findViewById(R.id.brightness_desc);
        mBrightnessButton.setOnClickListener(this);

        /* Torch Toggler */
        mTorchButton = (ImageButton) mView.findViewById(R.id.torch_button);
        mTorchDesc = (TextView) mView.findViewById(R.id.torch_desc);
        mTorchButton.setOnClickListener(this);

        /* Sync Toggler */
        mSyncButton = (ImageButton) mView.findViewById(R.id.sync_button);
        mSyncDesc = (TextView) mView.findViewById(R.id.sync_desc);
        mSyncButton.setOnClickListener(this);
    }

    private void updateButtonState() {
        try {

            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            IPowerManager mPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));

            /* set images based on values */
            int wifiState = wifiManager.getWifiState();
            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                case WifiManager.WIFI_STATE_ENABLING:
                    mWifiButton.setImageResource(R.drawable.wifi_on);
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                case WifiManager.WIFI_STATE_DISABLED:
                    mWifiButton.setImageResource(R.drawable.wifi_off);
                    break;
            }

            mRotationEnabled = (Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
            mRotationButton.setImageResource(mRotationEnabled ?
                    R.drawable.auto_rotate_on : R.drawable.auto_rotate_off);

            mWifiApEnabled = wifiManager.getWifiApState();
            switch (mWifiApEnabled) {
                case WifiManager.WIFI_AP_STATE_ENABLED:
                case WifiManager.WIFI_AP_STATE_ENABLING:
                    mTetherButton.setImageResource(R.drawable.tether_on);
                    break;
                case WifiManager.WIFI_AP_STATE_DISABLED:
                case WifiManager.WIFI_AP_STATE_DISABLING:
                    mTetherButton.setImageResource(R.drawable.tether_off);
                    break;
            }

            mDataButton.setImageResource(cm.getMobileDataEnabled() ?
                    R.drawable.data_on : R.drawable.data_off);

            mRingerMode = am.getRingerMode();
            if (mRingerMode == AudioManager.RINGER_MODE_SILENT) {
                mSoundButton.setImageResource(R.drawable.sound_off);
            } else if (mRingerMode == AudioManager.RINGER_MODE_VIBRATE) {
                mSoundButton.setImageResource(R.drawable.vibrate_on);
            } else if (mRingerMode == AudioManager.RINGER_MODE_NORMAL) {
                mSoundButton.setImageResource(R.drawable.sound_on);
            }

            mAirplaneEnabled = (Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) == 1);
            mAirplaneButton.setImageResource(mAirplaneEnabled ?
                    R.drawable.airplane_on : R.drawable.airplane_off);

            mNetworkMode = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.PREFERRED_NETWORK_MODE);
            mLteButton.setImageResource(mNetworkMode ==
                      Phone.NT_MODE_GLOBAL ?
                    R.drawable.lte_on : R.drawable.lte_off);

            mBluetoothButton.setImageResource(mBluetoothAdapter.isEnabled() ?
                    R.drawable.bt_on : R.drawable.bt_off);

            mGpsButton.setImageResource(mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER) ?
                    R.drawable.gps_on : R.drawable.gps_off);

            mBrightnessLevel = Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            mAutoBrightEnabled = (Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 0) == 1);
            if (mAutoBrightEnabled) {
                mBrightnessButton.setImageResource(
                        R.drawable.brightness_auto);
            } else if (mBrightnessLevel == 255) {
                mBrightnessButton.setImageResource(
                        R.drawable.brightness_full);
            } else {
                mBrightnessButton.setImageResource(
                        R.drawable.brightness_half);
            }

            syncValue = ContentResolver.getMasterSyncAutomatically();
            mSyncButton.setImageResource(syncValue ?
                    R.drawable.sync_on : R.drawable.sync_off);

            /* Not implemented yet */
            mTorchButton.setImageResource(R.drawable.torch_off);

        } catch (SettingNotFoundException b) {
            // Don't need logging yet.
        }
    }
	
    @Override
    public void onResume() {
        super.onResume();
     }
 
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v == mWifiButton) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            boolean wifiEnabled = wifiManager.isWifiEnabled();
            Toast.makeText(this, !wifiEnabled ? "Enabling WiFi" : "Disabling WiFi", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(!wifiEnabled);
            updateButtonState();
        } else if (v == mTetherButton) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            boolean wifiApEnabled = wifiManager.isWifiApEnabled();
            boolean wifiEnabled = wifiManager.isWifiEnabled();
            if (!wifiApEnabled) {
                if (wifiEnabled) {
                    wifiManager.setWifiEnabled(false);
                }
                wifiManager.setWifiApEnabled(null, true);
                Intent intent = new Intent(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
                intent.putExtra("state", true);
                sendBroadcast(intent);
                mTetherButton.setImageResource(R.drawable.tether_on);
                Toast.makeText(this, "Enabling WiFi AP mode", Toast.LENGTH_SHORT).show();
            } else {
                wifiManager.setWifiApEnabled(null, false);
                Intent intent = new Intent(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
                intent.putExtra("state", false);
                sendBroadcast(intent);
                Toast.makeText(this, "Disabling WiFi AP mode", Toast.LENGTH_SHORT).show();
                mTetherButton.setImageResource(R.drawable.tether_off);
            }     
        } else if (v == mDataButton) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService
                    (CONNECTIVITY_SERVICE);

            boolean dataEnabled = cm.getMobileDataEnabled();
            cm.setMobileDataEnabled(!dataEnabled);
            if (!dataEnabled) {
                mDataButton.setImageResource(R.drawable.data_on);
                Toast.makeText(this, "Enabling mobile data", Toast.LENGTH_SHORT).show();
            } else {
                mDataButton.setImageResource(R.drawable.data_off);
                Toast.makeText(this, "Disabling mobile data", Toast.LENGTH_SHORT).show();
            }
        } else if (v == mSoundButton) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            int mode = am.getRingerMode();
            if (mode == AudioManager.RINGER_MODE_SILENT) {
                Toast.makeText(this, "Changing Ringer Mode: Vibrate", Toast.LENGTH_SHORT).show();
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else if (mode == AudioManager.RINGER_MODE_VIBRATE) {
                Toast.makeText(this, "Changing Ringer Mode: Normal", Toast.LENGTH_SHORT).show();
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else {
                Toast.makeText(this, "Changing Ringer Mode: Silent", Toast.LENGTH_SHORT).show();
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
            updateButtonState();        
        } else if (v == mRotationButton) {
            boolean autorotate = (Settings.System.getInt(getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 0) != 0);
            Toast.makeText(this, !autorotate ? "Enabling auto-rotation" : "Disabling auto-rotation", Toast.LENGTH_SHORT).show();
            Settings.System.putInt(getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, autorotate ?  0 : 1);
            updateButtonState();
        } else if (v == mAirplaneButton) {
            boolean airplaneEnabled = (Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1);
            Toast.makeText(this, !airplaneEnabled ? "Enabling airplane mode" : "Disabling airplane mode", Toast.LENGTH_SHORT).show();
            Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, airplaneEnabled ? 0 : 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", !airplaneEnabled);
            sendBroadcast(intent);
            updateButtonState();
        } else if (v == mLteButton) {
            try {
                int network = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.PREFERRED_NETWORK_MODE);
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                if (network == Phone.NT_MODE_GLOBAL) {
                    Toast.makeText(this, "Disabling LTE", Toast.LENGTH_SHORT).show();
                    tm.toggleLTE(false);
                } else if (network == Phone.NT_MODE_CDMA) {
                    Toast.makeText(this, "Enabling LTE", Toast.LENGTH_SHORT).show();
                    tm.toggleLTE(true);
                }
            } catch (SettingNotFoundException e) {
            } 
            updateButtonState();
        } else if (v == mBluetoothButton) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                Toast.makeText(this, "Enabling Bluetooth", Toast.LENGTH_SHORT).show();
                mBluetoothButton.setImageResource(R.drawable.bt_on);
            } else {
                mBluetoothAdapter.disable();
                Toast.makeText(this, "Disabling Bluetooth", Toast.LENGTH_SHORT).show();
                mBluetoothButton.setImageResource(R.drawable.bt_off);
            }
        } else if (v == mGpsButton) {
            LocationManager mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "Enabling GPS", Toast.LENGTH_SHORT).show();
                Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
            } else {
                Toast.makeText(this, "Disabling GPS", Toast.LENGTH_SHORT).show();
                Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, false);
            }
            updateButtonState();
        } else if (v == mBrightnessButton) {
            try {
                IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                int newBrightnessLevel = 10;
                int brightnessMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
                int brightnessLevel = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                if (brightnessMode == 1) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 10);
                    Toast.makeText(this, "New brightness level: 10", Toast.LENGTH_SHORT).show();
                    newBrightnessLevel = 10;
                } else {
                    if (brightnessLevel == 10) {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 50);
                        Toast.makeText(this, "New brightness level: 50", Toast.LENGTH_SHORT).show();
                        newBrightnessLevel = 50;
                    } else if (brightnessLevel == 50) {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 100);
                        Toast.makeText(this, "New brightness level: 100", Toast.LENGTH_SHORT).show();
                        newBrightnessLevel = 100;
                    } else if (brightnessLevel == 100) {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 150);
                        Toast.makeText(this, "New brightness level: 150", Toast.LENGTH_SHORT).show();
                        newBrightnessLevel = 150;
                    } else if (brightnessLevel == 150) {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 200);
                        Toast.makeText(this, "New brightness level: 200", Toast.LENGTH_SHORT).show();
                        newBrightnessLevel = 200;
                    } else if (brightnessLevel == 200) {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
                        Toast.makeText(this, "New brightness level: MAX", Toast.LENGTH_SHORT).show();
                        newBrightnessLevel = 255;
                    } else if (brightnessLevel == 255) {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
                        Toast.makeText(this, "New brightness level: AUTO", Toast.LENGTH_SHORT).show();
                        newBrightnessLevel = 10;
                    } else {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 10);
                        Toast.makeText(this, "New brightness level: 10", Toast.LENGTH_SHORT).show();
                        newBrightnessLevel = 10;
                    }
                }
                try {
                    power.setBacklightBrightness(newBrightnessLevel);
                } catch (RemoteException r) {
                }
            } catch (SettingNotFoundException e) {
            }
            updateButtonState();
        } else if (v == mTorchButton) {
            // not implemented yet
            updateButtonState();
        } else if (v == mSyncButton) {
            boolean syncMode = ContentResolver.getMasterSyncAutomatically();
            if (syncMode) {
                Toast.makeText(this, "Disabling Sync", Toast.LENGTH_SHORT).show();
                ContentResolver.setMasterSyncAutomatically(false);
            } else {
                Toast.makeText(this, "Enabling Sync", Toast.LENGTH_SHORT).show();
                ContentResolver.setMasterSyncAutomatically(true);
            }
            updateButtonState();
        }               
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}
