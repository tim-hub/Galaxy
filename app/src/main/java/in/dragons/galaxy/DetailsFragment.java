package in.dragons.galaxy;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import in.dragons.galaxy.fragment.details.AppLists;
import in.dragons.galaxy.fragment.details.BackToPlayStore;
import in.dragons.galaxy.fragment.details.Beta;
import in.dragons.galaxy.fragment.details.DownloadOptions;
import in.dragons.galaxy.fragment.details.DownloadOrInstall;
import in.dragons.galaxy.fragment.details.GeneralDetails;
import in.dragons.galaxy.fragment.details.Permissions;
import in.dragons.galaxy.fragment.details.Review;
import in.dragons.galaxy.fragment.details.Screenshot;
import in.dragons.galaxy.fragment.details.Share;
import in.dragons.galaxy.fragment.details.SystemAppPage;
import in.dragons.galaxy.fragment.details.Video;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.playstore.CloneableTask;
import in.dragons.galaxy.task.playstore.DetailsTask;

public class DetailsFragment extends Fragment {

    static private final String INTENT_PACKAGE_NAME = "INTENT_PACKAGE_NAME";

    protected View v;
    protected DownloadOrInstall downloadOrInstallFragment;

    public static App app;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    static public Intent getDetailsIntent(Context context, String packageName) {
        Intent intent = new Intent(context, DetailsFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(DetailsFragment.INTENT_PACKAGE_NAME, packageName);
        return intent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.details_activity_layout, container, false);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        String packageName = arguments.getString("PackageName");
        GetAndRedrawDetailsTask task = new GetAndRedrawDetailsTask(this);
        task.setPackageName(packageName);
        task.setProgressIndicator(getActivity().findViewById(R.id.progress));
        task.execute();
    }

    @Override
    public void onPause() {
        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        redrawButtons();
        super.onResume();
    }

   /* protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        final String packageName = getIntentPackageName(intent);
        if (TextUtils.isEmpty(packageName)) {
            Log.e(this.getClass().getName(), "No package name provided");
            getActivity().finish();
            return;
        }
        Log.i(getClass().getSimpleName(), "Getting info about " + packageName);

        if (null != DetailsFragment.app) {
            redrawDetails(DetailsFragment.app);
        }

        GetAndRedrawDetailsTask task = new GetAndRedrawDetailsTask(this);
        task.setPackageName(packageName);
        task.setProgressIndicator(getActivity().findViewById(R.id.progress));
        task.execute();
    }*/

    private void redrawButtons() {
        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
            downloadOrInstallFragment.registerReceivers();
            downloadOrInstallFragment.draw();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        new DownloadOptions((GalaxyActivity) this.getActivity(), app).inflate(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return new DownloadOptions((GalaxyActivity) this.getActivity(), app).onContextItemSelected(item);
    }

    private String getIntentPackageName(Intent intent) {
        if (intent.hasExtra(INTENT_PACKAGE_NAME)) {
            return intent.getStringExtra(INTENT_PACKAGE_NAME);
        } else if (intent.getScheme() != null
                && (intent.getScheme().equals("market")
                || intent.getScheme().equals("http")
                || intent.getScheme().equals("https")
        )) {
            return intent.getData().getQueryParameter("id");
        }
        return null;
    }

    public void redrawDetails(App app) {
        new GeneralDetails(this, app).draw();
        new Permissions(this, app).draw();
        new Screenshot(this, app).draw();
        new Review(this, app).draw();
        new AppLists(this, app).draw();
        new BackToPlayStore(this, app).draw();
        new Share(this, app).draw();
        new SystemAppPage(this, app).draw();
        new Video(this, app).draw();
        new Beta(this, app).draw();
        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
        }
        downloadOrInstallFragment = new DownloadOrInstall((GalaxyActivity) this.getActivity(), app);
        redrawButtons();
        new DownloadOptions((GalaxyActivity) this.getActivity(), app).draw();
    }

    static class GetAndRedrawDetailsTask extends DetailsTask implements CloneableTask {

        private DetailsFragment detailsFragment;

        public GetAndRedrawDetailsTask(DetailsFragment detailsFragment) {
            this.detailsFragment = detailsFragment;
            setContext(detailsFragment.getActivity());
        }

        @Override
        public CloneableTask clone() {
            GetAndRedrawDetailsTask task = new GetAndRedrawDetailsTask(detailsFragment);
            task.setErrorView(errorView);
            task.setPackageName(packageName);
            task.setProgressIndicator(progressIndicator);
            return task;
        }

        @Override
        protected void onPostExecute(App app) {
            super.onPostExecute(app);
            if (app != null) {
                DetailsFragment.app = app;
                detailsFragment.redrawDetails(app);
            }
        }
    }
}
