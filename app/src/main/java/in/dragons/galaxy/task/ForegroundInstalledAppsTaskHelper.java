package in.dragons.galaxy.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import in.dragons.galaxy.GalaxyActivity;
import in.dragons.galaxy.InstalledAppsFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.fragment.FilterMenu;
import in.dragons.galaxy.model.App;

public class ForegroundInstalledAppsTaskHelper extends InstalledAppsTask {

    private InstalledAppsFragment installedAppsFragment;

    public ForegroundInstalledAppsTaskHelper(InstalledAppsFragment installedAppsFragment) {
        this.installedAppsFragment = installedAppsFragment;
        setContext(this.installedAppsFragment.getActivity().getApplicationContext());
        setProgressIndicator(this.installedAppsFragment.getActivity().findViewById(R.id.progress));
        setIncludeSystemApps(new FilterMenu((GalaxyActivity) this.installedAppsFragment.getActivity()).getFilterPreferences().isSystemApps());
    }

    @Override
    protected void onPostExecute(Map<String, App> result) {
        super.onPostExecute(result);
        installedAppsFragment.clearApps();
        List<App> installedApps = new ArrayList<>(result.values());
        Collections.sort(installedApps);
        installedAppsFragment.addApps(installedApps);
    }
}