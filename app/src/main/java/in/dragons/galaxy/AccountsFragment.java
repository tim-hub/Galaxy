package in.dragons.galaxy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.ImageViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.percolate.caffeine.PhoneUtils;
import com.percolate.caffeine.ViewUtils;
import com.squareup.picasso.Picasso;

public class AccountsFragment extends Fragment {

    private AccountTypeDialogBuilder accountTypeDialogBuilder;
    private SharedPreferences sharedPreferences;
    private String Email;
    private View v;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountTypeDialogBuilder = new AccountTypeDialogBuilder(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.app_acc_inc, container, false);
        getActivity().setTitle(R.string.action_accounts);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Email = sharedPreferences.getString(PlayStoreApiAuthenticator.PREFERENCE_EMAIL, "");

        if (isValidEmail(Email) && isConnected()) {
            drawGoogle();
        } else if (isDummyEmail())
            drawDummy();

        setFab();

        return v;
    }

    public void drawDummy() {
        ViewUtils.findViewById(v, R.id.dummy_container).setVisibility(View.VISIBLE);
        ViewUtils.findViewById(v, R.id.no_dummy).setVisibility(View.GONE);

        ImageViewCompat.setImageTintList(ViewUtils.findViewById(v, R.id.dummy_ind),
                ColorStateList.valueOf((getResources().getColor(R.color.colorRed))));

        TextView dummyEmail = ViewUtils.findViewById(v, R.id.dummy_email);
        dummyEmail.setText(Email);

        setText(R.id.dummy_gsf, R.string.device_gsfID, sharedPreferences.getString(PlayStoreApiAuthenticator.PREFERENCE_GSF_ID, ""));

        Button logout = ViewUtils.findViewById(v, R.id.account_logout);
        logout.setOnClickListener(v -> showLogOutDialog());

        Button switched = ViewUtils.findViewById(v, R.id.account_switch);
        switched.setOnClickListener(v -> accountTypeDialogBuilder.logInWithPredefinedAccount());
    }

    public void drawGoogle() {
        if (Email != "") {
            ViewUtils.findViewById(v, R.id.google_container).setVisibility(View.VISIBLE);
            ViewUtils.findViewById(v, R.id.no_google).setVisibility(View.GONE);

            ImageViewCompat.setImageTintList(ViewUtils.findViewById(v, R.id.google_ind),
                    ColorStateList.valueOf((getResources().getColor(R.color.colorGreen))));

            ViewUtils.setText(v, R.id.google_name, sharedPreferences.getString("GOOGLE_NAME", ""));
            ViewUtils.setText(v, R.id.google_email, Email);

            setText(R.id.google_gsf, R.string.device_gsfID, sharedPreferences.getString(PlayStoreApiAuthenticator.PREFERENCE_GSF_ID, ""));

            Button button = ViewUtils.findViewById(v, R.id.google_logout);
            button.setOnClickListener(v -> showLogOutDialog());

            loadAvatar(sharedPreferences.getString("GOOGLE_URL", ""));
        }
    }

    public void setFab() {
        FloatingActionButton dummyFab = ViewUtils.findViewById(v, R.id.dummy_login);
        dummyFab.setOnClickListener(v -> accountTypeDialogBuilder.logInWithPredefinedAccount());

        FloatingActionButton googleFab = ViewUtils.findViewById(v, R.id.google_login);
        googleFab.setOnClickListener(view -> accountTypeDialogBuilder.showCredentialsDialog());
    }

    public void loadAvatar(String Url) {
        Picasso.with(getActivity())
                .load(Url)
                .placeholder(R.drawable.ic_user_placeholder)
                .transform(new CircleTransform())
                .into(((ImageView) v.findViewById(R.id.google_avatar)));
    }

    protected void setText(int viewId, String text) {
        TextView textView = ViewUtils.findViewById(v, viewId);
        if (null != textView)
            textView.setText(text);
    }

    protected void setText(int viewId, int stringId, Object... text) {
        setText(viewId, this.getString(stringId, text));
    }

    AlertDialog showLogOutDialog() {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.dialog_message_logout)
                .setTitle(R.string.dialog_title_logout)
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    new PlayStoreApiAuthenticator(getActivity().getApplicationContext()).logout();
                    dialogInterface.dismiss();
                    getActivity().finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    protected boolean isConnected() {
        return PhoneUtils.isNetworkAvailable(this.getActivity());
    }

    protected boolean isValidEmail(String Email) {
        return !(Email.isEmpty() || isDummyEmail());
    }

    protected boolean isDummyEmail() {
        return (Email.contains("yalp.store.user"));
    }
}