package in.dragons.galaxy;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.ImageViewCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Properties;
import java.util.TimeZone;

public class SpoofFragment extends Fragment {

    private String deviceName;
    private ImageView spoofed;
    private Display mDisplay;
    private View v;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.app_device_inc, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        deviceName = sharedPreferences.getString(PreferenceActivity.PREFERENCE_DEVICE_TO_PRETEND_TO_BE, "");
        spoofed = (ImageView) v.findViewById(R.id.spoofed_indicator);
        mDisplay = (this).getActivity().getWindowManager().getDefaultDisplay();

        if (isSpoofed())
            drawSpoofedDevice();
        else
            drawDevice();

        setFab();

        return v;
    }

    public boolean isSpoofed() {
        return (deviceName.contains("device-"));
    }

    public void drawDevice() {
        ImageViewCompat.setImageTintList(spoofed, ColorStateList.valueOf((getResources().getColor(R.color.colorGreen))));
        setText(R.id.device_model, R.string.device_model, Build.MODEL, Build.DEVICE);
        setText(R.id.device_manufacturer, R.string.device_manufacturer, Build.MANUFACTURER);
        setText(R.id.device_architect, R.string.device_board, Build.BOARD);
        setText(R.id.device_timezone, R.string.device_timezone, (CharSequence) TimeZone.getDefault().getDisplayName());
        setText(R.id.device_resolution, R.string.device_res, mDisplay.getWidth(), mDisplay.getHeight());
        setText(R.id.device_api, R.string.device_api, Build.VERSION.SDK);
        setText(R.id.device_cpu, R.string.device_cpu, Build.CPU_ABI);
    }

    public void drawSpoofedDevice() {
        ImageViewCompat.setImageTintList(spoofed, ColorStateList.valueOf((getResources().getColor(R.color.colorRed))));

        Properties properties = new SpoofDeviceManager(this.getActivity()).getProperties(deviceName);
        String Model = properties.getProperty("UserReadableName");

        setText(R.id.device_model, R.string.device_model, Model.substring(0, Model.indexOf('(')), properties.getProperty("Build.DEVICE"));
        setText(R.id.device_manufacturer, R.string.device_manufacturer, properties.getProperty("Build.MANUFACTURER"));
        setText(R.id.device_architect, R.string.device_board, properties.getProperty("Build.HARDWARE"));
        setText(R.id.device_timezone, R.string.device_timezone, properties.getProperty("TimeZone"));
        setText(R.id.device_resolution, R.string.device_res, properties.getProperty("Screen.Width"), properties.getProperty("Screen.Height"));
        setText(R.id.device_api, R.string.device_api, properties.getProperty("Build.VERSION.SDK_INT"));
        String Platforms = properties.getProperty("Platforms");
        setText(R.id.device_cpu, R.string.device_cpu, Platforms.substring(0, Platforms.indexOf(',')));
    }


    public void setFab() {
        FloatingActionButton changeDevice = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        changeDevice.setVisibility(View.VISIBLE);
        changeDevice.setImageResource(R.drawable.app_dev);
        changeDevice.setOnClickListener(view -> {
            //
        });
    }

    protected void setText(int viewId, String text) {
        TextView textView = (TextView) v.findViewById(viewId);
        if (null != textView)
            textView.setText(text);
    }

    protected void setText(int viewId, int stringId, Object... text) {
        setText(viewId, this.getString(stringId, text));
    }
}
