package in.dragons.galaxy.fragment.details;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.model.App;

public abstract class AbstractHelper {

    protected DetailsFragment detailsFragment;
    protected App app;

    abstract public void draw();

    public AbstractHelper(DetailsFragment detailsFragment, App app) {
        this.detailsFragment = detailsFragment;
        this.app = app;
    }

    protected void setText(int viewId, String text) {
        TextView textView = (TextView) detailsFragment.getActivity().findViewById(viewId);
        if (null != textView)
            textView.setText(text);
    }

    protected void setText(int viewId, int stringId, Object... text) {
        setText(viewId, detailsFragment.getActivity().getString(stringId, text));
    }

    protected void initExpandableGroup(int viewIdHeader, int viewIdContainer, final View.OnClickListener l) {
        TextView viewHeader = (TextView)detailsFragment.getActivity().findViewById(viewIdHeader);
        viewHeader.setVisibility(View.VISIBLE);
        final LinearLayout viewContainer = (LinearLayout) detailsFragment.getActivity().findViewById(viewIdContainer);
        viewHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = viewContainer.getVisibility() == View.VISIBLE;
                if (isExpanded) {
                    viewContainer.setVisibility(View.GONE);
                    ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more, 0);
                } else {
                    if (null != l) {
                        l.onClick(v);
                    }
                    viewContainer.setVisibility(View.VISIBLE);
                    ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0);
                }
            }
        });
    }

    protected void initExpandableGroup(int viewIdHeader, int viewIdContainer) {
        initExpandableGroup(viewIdHeader, viewIdContainer, null);
    }
}