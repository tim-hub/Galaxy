package in.dragons.galaxy.fragment.details;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import in.dragons.galaxy.CircleTransform;
import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.PlayStoreApiAuthenticator;
import in.dragons.galaxy.R;
import in.dragons.galaxy.ReviewStorageIterator;
import in.dragons.galaxy.UserReviewDialogBuilder;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.playstore.ReviewDeleteTask;
import in.dragons.galaxy.task.playstore.ReviewLoadTask;

public class Review extends AbstractHelper {

    static private int[] averageStarIds = new int[]{R.id.average_stars1, R.id.average_stars2, R.id.average_stars3, R.id.average_stars4, R.id.average_stars5};

    private ReviewStorageIterator iterator;

    public Review(DetailsFragment detailsFragment, App app) {
        super(detailsFragment, app);
        iterator = new ReviewStorageIterator();
        iterator.setPackageName(app.getPackageName());
        iterator.setContext(detailsFragment.getActivity());
    }

    @Override
    public void draw() {
        if (!app.isInPlayStore() || app.isEarlyAccess()) {
            return;
        }

        initExpandableGroup(R.id.reviews_header, R.id.reviews_container, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTask(true).execute();
            }
        });
        detailsFragment.getActivity().findViewById(R.id.reviews_card).setVisibility(View.VISIBLE);
        initReviewListControls();

        setText(R.id.average_rating, R.string.details_rating, app.getRating().getAverage());
        for (int starNum = 1; starNum <= 5; starNum++) {
            setText(averageStarIds[starNum - 1], R.string.details_rating_specific, starNum, app.getRating().getStars(starNum));
        }

        detailsFragment.getActivity().findViewById(R.id.user_review_container).setVisibility(isReviewable(app) ? View.VISIBLE : View.GONE);
        in.dragons.galaxy.model.Review review = app.getUserReview();
        initUserReviewControls(app);
        if (null != review) {
            fillUserReview(review);
        }
    }

    private boolean isReviewable(App app) {
        return app.isInstalled()
                && !app.isTestingProgramOptedIn()
                && !PreferenceManager.getDefaultSharedPreferences(detailsFragment.getActivity()).getBoolean(PlayStoreApiAuthenticator.PREFERENCE_APP_PROVIDED_EMAIL, false)
                ;
    }

    public void fillUserReview(in.dragons.galaxy.model.Review review) {
        clearUserReview();
        app.setUserReview(review);
        ((RatingBar) detailsFragment.getActivity().findViewById(R.id.user_stars)).setRating(review.getRating());
        setTextOrHide(R.id.user_comment, review.getComment());
        setTextOrHide(R.id.user_title, review.getTitle());
        setText(R.id.rate, R.string.details_you_rated_this_app);
        detailsFragment.getActivity().findViewById(R.id.user_review_edit_delete).setVisibility(View.VISIBLE);
        detailsFragment.getActivity().findViewById(R.id.user_review).setVisibility(View.VISIBLE);
    }

    public void clearUserReview() {
        ((RatingBar) detailsFragment.getActivity().findViewById(R.id.user_stars)).setRating(0);
        setText(R.id.user_title, "");
        setText(R.id.user_comment, "");
        setText(R.id.rate, R.string.details_rate_this_app);
        detailsFragment.getActivity().findViewById(R.id.user_review_edit_delete).setVisibility(View.GONE);
        detailsFragment.getActivity().findViewById(R.id.user_review).setVisibility(View.GONE);
    }

    private in.dragons.galaxy.model.Review getUpdatedUserReview(in.dragons.galaxy.model.Review oldReview, int stars) {
        in.dragons.galaxy.model.Review review = new in.dragons.galaxy.model.Review();
        review.setRating(stars);
        if (null != oldReview) {
            review.setComment(oldReview.getComment());
            review.setTitle(oldReview.getTitle());
        }
        return review;
    }

    public void showReviews(List<in.dragons.galaxy.model.Review> reviews) {
        detailsFragment.getActivity().findViewById(R.id.reviews_previous).setVisibility(iterator.hasPrevious() ? View.VISIBLE : View.INVISIBLE);
        detailsFragment.getActivity().findViewById(R.id.reviews_next).setVisibility(iterator.hasNext() ? View.VISIBLE : View.INVISIBLE);
        LinearLayout listView = (LinearLayout) detailsFragment.getActivity().findViewById(R.id.reviews_list);
        listView.removeAllViews();
        for (in.dragons.galaxy.model.Review review : reviews) {
            addReviewToList(review, listView);
        }
    }

    private ReviewLoadTask getTask(boolean next) {
        ReviewLoadTask task = new ReviewLoadTask();
        task.setIterator(iterator);
        task.setFragment(this);
        task.setNext(next);
        task.setContext(detailsFragment.getActivity());
        task.setProgressIndicator(detailsFragment.getActivity().findViewById(R.id.progress));
        return task;
    }

    private void addReviewToList(in.dragons.galaxy.model.Review review, ViewGroup parent) {
        LinearLayout reviewLayout = (LinearLayout) detailsFragment.getActivity().getLayoutInflater().inflate(R.layout.review_list_item, parent, false);
        ((TextView) reviewLayout.findViewById(R.id.author)).setText(review.getUserName());
        ((TextView) reviewLayout.findViewById(R.id.title)).setText(detailsFragment.getString(
                R.string.two_items,
                detailsFragment.getString(R.string.details_rating, (double) review.getRating()),
                review.getTitle()
        ));
        ((TextView) reviewLayout.findViewById(R.id.comment)).setText(review.getComment());
        Picasso
                .with(detailsFragment.getActivity())
                .load(review.getUserPhotoUrl())
                .placeholder(R.drawable.ic_user_placeholder)
                .transform(new CircleTransform())
                .into((ImageView) reviewLayout.findViewById(R.id.avatar));

        parent.addView(reviewLayout);
    }

    private void initReviewListControls() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTask(v.getId() == R.id.reviews_next).execute();
            }
        };
        detailsFragment.getActivity().findViewById(R.id.reviews_previous).setOnClickListener(listener);
        detailsFragment.getActivity().findViewById(R.id.reviews_next).setOnClickListener(listener);
    }

    private void initUserReviewControls(final App app) {
        ((RatingBar) detailsFragment.getActivity().findViewById(R.id.user_stars)).setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                new UserReviewDialogBuilder(detailsFragment.getActivity(), Review.this, app.getPackageName())
                        .show(getUpdatedUserReview(app.getUserReview(), (int) rating));
            }
        });
        detailsFragment.getActivity().findViewById(R.id.user_review_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UserReviewDialogBuilder(detailsFragment.getActivity(), Review.this, app.getPackageName())
                        .show(app.getUserReview());
            }
        });
        detailsFragment.getActivity().findViewById(R.id.user_review_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewDeleteTask task = new ReviewDeleteTask();
                task.setFragment(Review.this);
                task.setContext(v.getContext());
                task.execute(app.getPackageName());
            }
        });
    }

    private void setTextOrHide(int viewId, String text) {
        TextView textView = (TextView) detailsFragment.getActivity().findViewById(viewId);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
}