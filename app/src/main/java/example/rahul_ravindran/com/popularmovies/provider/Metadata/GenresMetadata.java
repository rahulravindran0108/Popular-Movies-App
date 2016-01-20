package example.rahul_ravindran.com.popularmovies.provider.Metadata;

import android.content.ContentValues;
import android.database.Cursor;

import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import example.rahul_ravindran.com.popularmovies.helpers.DbHelpers;
import example.rahul_ravindran.com.popularmovies.model.Genres;
import example.rahul_ravindran.com.popularmovies.provider.MoviesDBContract;
import rx.functions.Func1;

/**
 * Created by rahulravindran on 18/01/16.
 */
public interface GenresMetadata {

    //possible projections for genres
    String[] PROJECTION = {
            MoviesDBContract.GenresDBHelper._ID,
            MoviesDBContract.GenresDBHelper.GENRE_ID,
            MoviesDBContract.GenresDBHelper.GENRE_NAME
    };

    Func1<SqlBrite.Query, Map<Integer, Genres>> PROJECTION_MAP =
            new Func1<SqlBrite.Query, Map<Integer, Genres>>() {
                @Override
                public Map<Integer, Genres> call(SqlBrite.Query query) {
                    Cursor cursor = query.run();
                    try {
                        Map<Integer, Genres> values = new HashMap<>(cursor.getCount());

                        while (cursor.moveToNext()) {
                            int id = DbHelpers.getInt(cursor, MoviesDBContract.GenresColumns.GENRE_ID);
                            String name = DbHelpers.getString(cursor, MoviesDBContract.GenresColumns.GENRE_NAME);
                            values.put(id, new Genres(id, name));
                        }
                        return values;
                    } finally {
                        cursor.close();
                    }
                }
            };

    Func1<SqlBrite.Query, List<Genres>> PROJECTION_LIST = new Func1<SqlBrite.Query, List<Genres>>() {
        @Override
        public List<Genres> call(SqlBrite.Query query) {
            Cursor cursor = query.run();
            try {
                List<Genres> values = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    values.add(new Genres((int)
                            (DbHelpers.getLong(cursor, MoviesDBContract.GenresDBHelper.GENRE_ID)),
                            DbHelpers.getString(cursor,MoviesDBContract.GenresDBHelper.GENRE_NAME )));
                }
                return values;
            }
            finally {
                cursor.close();
            }
        }
    };

    final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(int id) {

            values.put(MoviesDBContract.GenresColumns.GENRE_ID, id);
            return this;
        }

        public Builder name(String name) {
            values.put(MoviesDBContract.GenresColumns.GENRE_NAME, name);
            return this;
        }

        public ContentValues build() {
            return values;
        }
    }
}
