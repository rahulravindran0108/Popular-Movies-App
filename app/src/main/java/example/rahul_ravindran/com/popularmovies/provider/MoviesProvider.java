package example.rahul_ravindran.com.popularmovies.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Arrays;

import example.rahul_ravindran.com.popularmovies.helpers.SelectionBuilder;
import timber.log.Timber;

import static example.rahul_ravindran.com.popularmovies.provider.MoviesDBContract.CONTENT_AUTHORITY;
import static example.rahul_ravindran.com.popularmovies.provider.MoviesDatabaseHelper.Tables;
import example.rahul_ravindran.com.popularmovies.provider.MoviesDBContract;


public class MoviesProvider extends ContentProvider {

    private static final String Tag = MoviesProvider.class.getName();
    private SQLiteOpenHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int GENRES = 100;

    private static final int MOVIES = 200;
    private static final int MOVIES_ID = 201;
    private static final int MOVIES_ID_GENRES = 202;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesDBContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "genres", GENRES);

        matcher.addURI(authority, "movies", MOVIES);
        matcher.addURI(authority, "movies/*", MOVIES_ID);
        matcher.addURI(authority, "movies/*/genres", MOVIES_ID_GENRES);

        return matcher;
    }

    private void deleteDatabase() {
        mOpenHelper.close();
        Context context = getContext();
        MoviesDatabaseHelper.deleteDatabase(context);
        mOpenHelper = new MoviesDatabaseHelper(getContext());
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDatabaseHelper(getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);

        Timber.tag(Tag).v("uri=" + uri + " match=" + match + " proj=" + Arrays.toString(projection) +
                " selection=" + selection + " args=" + Arrays.toString(selectionArgs) + ")");

        final SelectionBuilder builder = buildExpandedSelection(uri, match);

        boolean distinct = !TextUtils.isEmpty(uri.getQueryParameter("distinct"));

        Cursor cursor = builder
                .where(selection, selectionArgs)
                .query(db, distinct, projection, sortOrder, null);

        Context context = getContext();
        if (null != context) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case GENRES: {
                return builder.table(Tables.GENRES);
            }
            case MOVIES: {
                return builder.table(Tables.MOVIES);
            }
            case MOVIES_ID: {
                final String movieId = MoviesDBContract.MoviesDBHelper.getMovieId(uri);
                return builder.table(Tables.MOVIES_JOIN_GENRES)
                        .mapToTable(MoviesDBContract.MoviesDBHelper._ID, Tables.MOVIES)
                        .mapToTable(MoviesDBContract.MoviesDBHelper.MOVIE_ID, Tables.MOVIES)
                        .where(Qualified.MOVIES_MOVIE_ID + "=?", movieId);
            }
            case MOVIES_ID_GENRES: {
                final String movieId = MoviesDBContract.MoviesDBHelper.getMovieId(uri);
                return builder.table(Tables.MOVIES_GENRES_JOIN_GENRES)
                        .mapToTable(MoviesDBContract.GenresDBHelper._ID, Tables.GENRES)
                        .mapToTable(MoviesDBContract.GenresDBHelper.GENRE_ID, Tables.GENRES)
                        .where(Qualified.MOVIES_GENRES_GENRE_ID + "=?", movieId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GENRES: {
                return builder.table(Tables.GENRES);
            }
            case MOVIES: {
                return builder.table(Tables.MOVIES);
            }
            case MOVIES_ID: {
                final String movieId = MoviesDBContract.MoviesDBHelper.getMovieId(uri);
                return builder.table(Tables.MOVIES)
                        .where(MoviesDBContract.MoviesDBHelper.MOVIE_ID + "=?", movieId);
            }
            case MOVIES_ID_GENRES: {
                final String movieId = MoviesDBContract.MoviesDBHelper.getMovieId(uri);
                return builder.table(Tables.MOVIES_GENRES)
                        .where(MoviesDBContract.MoviesDBHelper.MOVIE_ID + "=?", movieId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    private interface Qualified {
        String MOVIES_MOVIE_ID = Tables.MOVIES + "." + MoviesDBContract.MoviesDBHelper.MOVIE_ID;
        String MOVIES_GENRES_GENRE_ID = Tables.MOVIES_GENRES + "." + MoviesDBContract.MoviesDBHelper.MOVIE_ID;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case GENRES:
                return MoviesDBContract.GenresDBHelper.CONTENT_TYPE;
            case MOVIES:
                return MoviesDBContract.MoviesDBHelper.CONTENT_TYPE;
            case MOVIES_ID:
                return MoviesDBContract.MoviesDBHelper.CONTENT_ITEM_TYPE;
            case MOVIES_ID_GENRES:
                return MoviesDBContract.GenresDBHelper.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Timber.tag(Tag).v("insert(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GENRES: {
                db.insertOrThrow(Tables.GENRES, null, values);
                notifyChange(uri);
                return MoviesDBContract.GenresDBHelper.buildGenreUri(values.getAsString(MoviesDBContract.GenresDBHelper.GENRE_ID));
            }
            case MOVIES: {
                db.insertOrThrow(Tables.MOVIES, null, values);
                notifyChange(uri);
                return MoviesDBContract.MoviesDBHelper.buildMovieUri(values.getAsString(MoviesDBContract.MoviesDBHelper.MOVIE_ID));
            }
            case MOVIES_ID_GENRES: {
                db.insertOrThrow(Tables.MOVIES_GENRES, null, values);
                notifyChange(uri);
                return MoviesDBContract.GenresDBHelper.buildGenreUri(values.getAsString(MoviesDBContract.GenresDBHelper.GENRE_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Timber.tag(Tag).v("delete(uri=" + uri + ")");
        if (uri.equals(MoviesDBContract.BASE_CONTENT_URI)) {
            deleteDatabase();
            notifyChange(uri);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Timber.tag(Tag).v("update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        notifyChange(uri);
        return retVal;
    }
}
