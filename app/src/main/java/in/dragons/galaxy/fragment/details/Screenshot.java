package in.dragons.galaxy.fragment.details;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;

import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.FullscreenImageActivity;
import in.dragons.galaxy.ImageAdapter;
import in.dragons.galaxy.R;
import in.dragons.galaxy.model.App;

public class Screenshot extends AbstractHelper {

    public Screenshot(DetailsFragment detailsFragment, App app) {
        super(detailsFragment, app);
    }

    @Override
    public void draw() {
        if (app.getScreenshotUrls().size() > 0) {
            drawGallery();
        } else {
            return;
        }
    }

    private void drawGallery() {
        Gallery gallery = ((Gallery) detailsFragment.getActivity().findViewById(R.id.screenshots_gallery));
        int screenWidth = detailsFragment.getActivity().getWindowManager().getDefaultDisplay().getWidth();
        gallery.setAdapter(new ImageAdapter(detailsFragment.getActivity(), app.getScreenshotUrls(), screenWidth));
        gallery.setSpacing(15);
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(detailsFragment.getActivity(), FullscreenImageActivity.class);
                intent.putExtra(FullscreenImageActivity.INTENT_SCREENSHOT_NUMBER, position);
                detailsFragment.getActivity().startActivity(intent);
            }
        });
    }
}