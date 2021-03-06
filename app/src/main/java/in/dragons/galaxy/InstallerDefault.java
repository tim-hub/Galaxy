package in.dragons.galaxy;

import android.content.Context;
import android.util.Log;

import in.dragons.galaxy.model.App;

public class InstallerDefault extends InstallerAbstract {

    public InstallerDefault(Context context) {
        super(context);
    }

    @Override
    public boolean verify(App app) {
        if (background) {
            Log.i(getClass().getSimpleName(), "Background installation is not supported by default installer");
            return false;
        }
        return super.verify(app);
    }

    @Override
    protected void install(App app) {
        InstallationState.setSuccess(app.getPackageName());
        context.startActivity(
                InstallerAbstract.getOpenApkIntent(
                        context,
                        Paths.getApkPath(context, app.getPackageName(), app.getVersionCode())
                )
        );
    }
}
