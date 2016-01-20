package example.rahul_ravindran.com.popularmovies.provider;

import android.app.Application;
import android.content.ContentResolver;

import com.squareup.sqlbrite.BriteContentResolver;
import com.squareup.sqlbrite.SqlBrite;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

/**
 * Created by rahulravindran on 18/01/16.
 */
@Module
public class MoviesProviderModule {

    Application mApplication;

    public MoviesProviderModule(Application app) {
        this.mApplication = app;
    }

    @Provides
    @Singleton SqlBrite provideSqlBrite() {
        return SqlBrite.create(new SqlBrite.Logger() {
            @Override
            public void log(String message) {
                Timber.tag("Database").v(message);
            }
        });
    }

    @Provides
    @Singleton ContentResolver provideContentResolver() {
        return this.mApplication.getContentResolver();
    }

    @Provides
    @Singleton
    BriteContentResolver provideBrideContentResolver(SqlBrite sqlBrite, ContentResolver contentResolver) {
        return sqlBrite.wrapContentProvider(contentResolver);
    }
}
