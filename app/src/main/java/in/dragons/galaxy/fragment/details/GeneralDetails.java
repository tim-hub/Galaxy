package in.dragons.galaxy.fragment.details;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.dragons.galaxy.CategoryManager;
import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.Util;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.model.ImageSource;

public class GeneralDetails extends AbstractHelper {

    public GeneralDetails(DetailsFragment detailsFragment, App app) {
        super(detailsFragment, app);
    }

    @Override
    public void draw() {
        drawAppBadge(app);
        if (app.isInPlayStore()) {
            drawGeneralDetails(app);
            drawDescription(app);
        }
    }

    private void drawAppBadge(App app) {
        ImageView imageView = (ImageView) detailsFragment.getActivity().findViewById(R.id.icon);
        ImageSource imageSource = app.getIconInfo();
        if (null != imageSource.getApplicationInfo()) {
            imageView.setImageDrawable(imageView.getContext().getPackageManager().getApplicationIcon(imageSource.getApplicationInfo()));
        } else {
            Picasso
                    .with(detailsFragment.getActivity())
                    .load(imageSource.getUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imageView);
        }

        setText(R.id.displayName, app.getDisplayName());
        setText(R.id.packageName, R.string.details_developer, app.getDeveloperName());
        drawVersion((TextView) detailsFragment.getActivity().findViewById(R.id.versionString), app);
    }

    private void drawGeneralDetails(App app) {
        detailsFragment.getActivity().findViewById(R.id.app_detail).setVisibility(View.VISIBLE);
        detailsFragment.getActivity().findViewById(R.id.general_card).setVisibility(View.VISIBLE);
        setText(R.id.installs, R.string.details_installs, Util.addDiPrefix(app.getInstalls()));
        if (app.isEarlyAccess()) {
            setText(R.id.rating, R.string.early_access);
        } else {
            setText(R.id.rating, R.string.details_rating, app.getRating().getAverage());
        }

        setText(R.id.updated, R.string.details_updated, app.getUpdated());
        setText(R.id.size, R.string.details_size, Formatter.formatShortFileSize(detailsFragment.getActivity(), app.getSize()));
        setText(R.id.category, R.string.details_category, new CategoryManager(detailsFragment.getActivity()).getCategoryName(app.getCategoryId()));
        setText(R.id.developer, R.string.details_developer, app.getDeveloperName());
        setText(R.id.price, app.getPrice());
        setText(R.id.contains_ads, app.containsAds() ? R.string.details_contains_ads : R.string.details_no_ads);

        drawOfferDetails(app);
        drawChanges(app);

        if (app.getVersionCode() == 0) {
            detailsFragment.getActivity().findViewById(R.id.updated).setVisibility(View.GONE);
            detailsFragment.getActivity().findViewById(R.id.size).setVisibility(View.GONE);
        }
    }

    private void drawChanges(App app) {
        String changes = app.getChanges();
        if (TextUtils.isEmpty(changes)) {
            detailsFragment.getActivity().findViewById(R.id.more_changes_container).setVisibility(View.GONE);
            detailsFragment.getActivity().findViewById(R.id.changes_container).setVisibility(View.GONE);
            return;
        }
        if (app.getInstalledVersionCode() == 0) {
            setText(R.id.changes, Html.fromHtml(changes).toString());
            detailsFragment.getActivity().findViewById(R.id.more_changes_container).setVisibility(View.VISIBLE);
        } else {
            setText(R.id.changes_upper, Html.fromHtml(changes).toString());
            detailsFragment.getActivity().findViewById(R.id.changes_container).setVisibility(View.VISIBLE);
        }
    }

    private void drawOfferDetails(App app) {
        List<String> keyList = new ArrayList<>(app.getOfferDetails().keySet());
        Collections.reverse(keyList);
        for (String key : keyList) {
            addOfferItem(key, app.getOfferDetails().get(key));
        }
    }

    private void addOfferItem(String key, String value) {
        if (null == value) {
            return;
        }
        TextView itemView = new TextView(detailsFragment.getActivity());
        try {
            itemView.setAutoLinkMask(Linkify.ALL);
            itemView.setText(detailsFragment.getString(R.string.two_items, key, Html.fromHtml(value)));
        } catch (RuntimeException e) {
            Log.w(getClass().getSimpleName(), "System WebView missing: " + e.getMessage());
            itemView.setAutoLinkMask(0);
            itemView.setText(detailsFragment.getString(R.string.two_items, key, Html.fromHtml(value)));
        }
        ((LinearLayout) detailsFragment.getActivity().findViewById(R.id.offer_details)).addView(itemView);
    }

    private void drawVersion(TextView textView, App app) {
        String versionName = app.getVersionName();
        if (TextUtils.isEmpty(versionName)) {
            return;
        }
        textView.setText(detailsFragment.getString(R.string.details_versionName, versionName));
        textView.setVisibility(View.VISIBLE);
        if (!app.isInstalled()) {
            return;
        }
        try {
            PackageInfo info = detailsFragment.getActivity().getPackageManager().getPackageInfo(app.getPackageName(), 0);
            String currentVersion = info.versionName;
            if (info.versionCode == app.getVersionCode() || null == currentVersion) {
                return;
            }
            String newVersion = versionName;
            if (currentVersion.equals(newVersion)) {
                currentVersion += " (" + info.versionCode;
                newVersion = app.getVersionCode() + ")";
            }
            textView.setText(detailsFragment.getString(R.string.details_versionName_updatable, currentVersion, newVersion));
            setText(R.id.download, detailsFragment.getString(R.string.details_update));
        } catch (PackageManager.NameNotFoundException e) {
            // We've checked for that already
        }
    }

    private void drawDescription(App app) {
        if (TextUtils.isEmpty(app.getDescription())) {
            detailsFragment.getActivity().findViewById(R.id.description_header).setVisibility(View.GONE);
        } else {
            setText(R.id.description, Html.fromHtml(app.getDescription()).toString());
            initExpandableGroup(R.id.description_header, R.id.description_container);
            detailsFragment.getActivity().findViewById(R.id.more_card).setVisibility(View.VISIBLE);
            if (app.getInstalledVersionCode() == 0 || TextUtils.isEmpty(app.getChanges())) {
                //activity.findViewById(R.id.description_header).performClick();
            }
        }
    }
}