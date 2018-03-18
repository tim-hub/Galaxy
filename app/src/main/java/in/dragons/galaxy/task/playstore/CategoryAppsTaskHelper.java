package in.dragons.galaxy.task.playstore;

import com.github.yeriomin.playstoreapi.CategoryAppsIterator;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;

import java.io.IOException;

import in.dragons.galaxy.AppListIterator;
import in.dragons.galaxy.PlayStoreApiAuthenticator;

public class CategoryAppsTaskHelper extends EndlessScrollTaskHelper implements CloneableTask {

    private String categoryId;

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public CategoryAppsTaskHelper(AppListIterator iterator) {
        super(iterator);
    }

    @Override
    public CloneableTask clone() {
        CategoryAppsTaskHelper task = new CategoryAppsTaskHelper(iterator);
        task.setFilter(filter);
        task.setCategoryId(categoryId);
        task.setErrorView(errorView);
        task.setContext(context);
        task.setProgressIndicator(progressIndicator);
        return task;
    }

    @Override
    protected AppListIterator initIterator() throws IOException {
        return new AppListIterator(new CategoryAppsIterator(
                new PlayStoreApiAuthenticator(context).getApi(),
                categoryId,
                GooglePlayAPI.SUBCATEGORY.MOVERS_SHAKERS
        ));
    }
}
