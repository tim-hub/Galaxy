package in.dragons.galaxy;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AboutFragment extends Fragment {

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.app_abt_inc, container, false);

        drawVersion();
        drawActions();
        drawDevCard(R.string.dev1_imgURL, (ImageView) v.findViewById(R.id.dev1_avatar));
        drawDevCard(R.string.dev2_imgURL, (ImageView) v.findViewById(R.id.dev2_avatar));
        drawList(getResources().getStringArray(R.array.contributors), ((TextView) v.findViewById(R.id.contributors)));
        drawList(getResources().getStringArray(R.array.opensource), ((TextView) v.findViewById(R.id.opensource)));

        return v;
    }

    private void drawVersion() {
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView) v.findViewById(R.id.app_version)).setText(packageInfo.versionName + "." + packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void drawActions() {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        ((TextView) v.findViewById(R.id.github)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                browserIntent.setData(Uri.parse(getResources().getString(R.string.linkGit)));
                startActivity(browserIntent);
            }
        });
        ((TextView) v.findViewById(R.id.xda)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                browserIntent.setData(Uri.parse(getResources().getString(R.string.linkXDA)));
                startActivity(browserIntent);
            }
        });
        ((TextView) v.findViewById(R.id.telegram)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                browserIntent.setData(Uri.parse(getResources().getString(R.string.linkTelegram)));
                startActivity(browserIntent);
            }
        });
    }

    private void drawDevCard(int URL, ImageView imageView) {
        Picasso.with(this.getActivity())
                .load(getResources().getString(URL))
                .placeholder(R.drawable.ic_user_placeholder)
                .transform(new CircleTransform())
                .into(imageView);
    }

    private void drawList(String[] List, TextView tv) {
        StringBuilder builder = new StringBuilder();
        for (String s : List) {
            builder.append("◉  ");
            builder.append(s);
            builder.append("\n");
        }
        (tv).setText(builder.toString().trim());
    }
}