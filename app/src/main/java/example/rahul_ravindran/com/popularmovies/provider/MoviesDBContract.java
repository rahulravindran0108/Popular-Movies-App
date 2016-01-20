package example.rahul_ravindran.com.popularmovies.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rahulravindran on 18/01/16.
 */
public class MoviesDBContract {

    //creates helper to access genre columns
    public interface GenresColumns {
        String GENRE_ID = "genre_id";
        String GENRE_NAME = "genre_name";

    }

    //creates helper to access movies columns
    public interface MoviesColumns {
        String MOVIE_ID = "movie_id";
        String MOVIE_TITLE = "movie_title";
        String MOVIE_OVERVIEW = "movie_overview";
        String MOVIE_POPULARITY = "movie_popularity";
        String MOVIE_GENRE_IDS = "movie_genre_ids";
        String MOVIE_VOTE_COUNT = "movie_vote_count";
        String MOVIE_VOTE_AVERAGE = "movie_vote_average";
        String MOVIE_RELEASE_DATE = "movie_release_date";
        String MOVIE_FAVORED = "movie_favored";
        String MOVIE_POSTER_PATH = "movie_poster_path";
        String MOVIE_BACKDROP_PATH = "movie_backdrop_path";
    }

    public static final String CONTENT_AUTHORITY =
                                    "example.rahul_ravindran.com.popularmovies.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_GENRES = "genres";
    private static final String PATH_MOVIES = "movies";

    /*
        Movies can have many genres
    */
    public static class GenresDBHelper implements BaseColumns, GenresColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                                                appendPath(PATH_GENRES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.popularmovies.genre";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.popularmovies.genre";

        public static final String DEFAULT_SORT = BaseColumns._ID + " DESC";
        //references all genres

        public static Uri buildGenresUri() {
            return CONTENT_URI;
        }

        //references a given genre
        public static Uri buildGenreUri(String genreId) {
            return CONTENT_URI.buildUpon().appendPath(genreId).build();
        }

    }

    public static class MoviesDBHelper implements  BaseColumns, MoviesColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.popularmovies.movie";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.popularmovies.movie";

        public static final String DEFAULT_SORT = BaseColumns._ID + " DESC";

        public static Uri buildMovieUri(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }

        public static Uri buildGenresDirUri(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).appendPath(PATH_GENRES).build();
        }

        public static String getMovieId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    private MoviesDBContract() {
        throw new AssertionError("No instances.");
    }

}
