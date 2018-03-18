package in.dragons.galaxy.fragment.details;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.model.App;

public class Share extends AbstractHelper {

    static private String PLAYSTORE_LINK_PREFIX = "https://play.google.com/store/apps/details?id=";

    public Share(DetailsFragment detailsFragment, App app) {
        super(detailsFragment, app);
    }

    @Override
    public void draw() {
        ImageView share = (ImageView) detailsFragment.getActivity().findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, app.getDisplayName());
                i.putExtra(Intent.EXTRA_TEXT, PLAYSTORE_LINK_PREFIX + app.getPackageName());
                detailsFragment.getActivity().startActivity(Intent.createChooser(i, detailsFragment.getActivity().getString(R.string.details_share)));
            }
        });
    }
}