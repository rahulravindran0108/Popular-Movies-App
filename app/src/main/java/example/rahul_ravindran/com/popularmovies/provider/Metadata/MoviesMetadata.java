package example.rahul_ravindran.com.popularmovies.provider.Metadata;

import android.content.ContentValues;
import android.database.Cursor;

import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.helpers.DbHelpers;
import example.rahul_ravindran.com.popularmovies.provider.MoviesDBContract;
import rx.functions.Func1;


public interface MoviesMetadata {

    String [] PROJECTION = {
            MoviesDBContract.MoviesDBHelper._ID,
            MoviesDBContract.MoviesDBHelper.MOVIE_ID,
            MoviesDBContract.MoviesDBHelper.MOVIE_TITLE,
            MoviesDBContract.MoviesDBHelper.MOVIE_OVERVIEW,
            MoviesDBContract.MoviesDBHelper.MOVIE_GENRE_IDS,
            MoviesDBContract.MoviesDBHelper.MOVIE_POSTER_PATH,
            MoviesDBContract.MoviesDBHelper.MOVIE_POPULARITY,
            MoviesDBContract.MoviesDBHelper.MOVIE_BACKDROP_PATH,
            MoviesDBContract.MoviesDBHelper.MOVIE_RELEASE_DATE,
            MoviesDBContract.MoviesDBHelper.MOVIE_FAVORED,
            MoviesDBContract.MoviesDBHelper.MOVIE_VOTE_AVERAGE,
            MoviesDBContract.MoviesDBHelper.MOVIE_VOTE_COUNT
    };

    Func1<SqlBrite.Query, List<MovieDB>> PROJECTION_MAP = new Func1<SqlBrite.Query, List<MovieDB>>() {
        @Override
        public List<MovieDB> call(SqlBrite.Query query) {

            Cursor cursor = query.run();
            try {

                List<MovieDB> values = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {


                    values.add(new MovieDB().setId(DbHelpers.getLong(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_ID))
                            .setTitle(DbHelpers.getString(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_TITLE))
                            .setOverview(DbHelpers.getString(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_OVERVIEW))
                            .putGenreIdsList(DbHelpers.getString(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_GENRE_IDS))
                            .setPosterPath(DbHelpers.getString(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_POSTER_PATH))
                            .setBackdropPath(DbHelpers.getString(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_BACKDROP_PATH))
                            .setFavored(DbHelpers.getBoolean(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_FAVORED))
                            .setPopularity(DbHelpers.getDouble(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_POPULARITY))
                            .setVoteCount(DbHelpers.getInt(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_VOTE_COUNT))
                            .setVoteAverage(DbHelpers.getDouble(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_VOTE_AVERAGE))
                            .setReleaseDate(DbHelpers.getString(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_RELEASE_DATE)));
                }
                return values;
            } finally {
                //cursor.close();
            }

        }
    };

    String[] ID_PROJECTION = {
            MoviesDBContract.MoviesDBHelper.MOVIE_ID
    };

    Func1<SqlBrite.Query, Set<Long>> ID_PROJECTION_MAP = new Func1<SqlBrite.Query, Set<Long>>() {
        @Override
        public Set<Long> call(SqlBrite.Query query) {
            Cursor cursor = query.run();
            try {
                Set<Long> idSet = new HashSet<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    idSet.add(DbHelpers.getLong(cursor, MoviesDBContract.MoviesDBHelper.MOVIE_ID));
                }
                return idSet;
            } finally {
                cursor.close();
            }
        }
    };

    final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_ID, id);
            return this;
        }

        public Builder title(String title) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_TITLE, title);
            return this;
        }

        public Builder overview(String overview) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_OVERVIEW, overview);
            return this;
        }

        public Builder genreIds(String genreIds) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_GENRE_IDS, genreIds);
            return this;
        }

        public Builder backdropPath(String backdropPath) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_BACKDROP_PATH, backdropPath);
            return this;
        }

        public Builder posterPath(String posterPath) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_POSTER_PATH, posterPath);
            return this;
        }

        public Builder voteCount(long voteCount) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_VOTE_COUNT, voteCount);
            return this;
        }

        public Builder voteAverage(double voteCount) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_VOTE_AVERAGE, voteCount);
            return this;
        }

        public Builder popularity(double popularity) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_POPULARITY, popularity);
            return this;
        }

        public Builder favored(boolean favored) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_FAVORED, favored);
            return this;
        }

        public Builder releaseDate(String date) {
            values.put(MoviesDBContract.MoviesDBHelper.MOVIE_RELEASE_DATE, date);
            return this;
        }

        public Builder movie(MovieDB movie) {
            return id(movie.getId())
                    .title(movie.getTitle())
                    .overview(movie.getOverview())
                    .genreIds(movie.makeGenreIdsList())
                    .backdropPath(movie.getBackdropPath())
                    .posterPath(movie.getPosterPath())
                    .popularity(movie.getPopularity())
                    .voteCount(movie.getVoteCount())
                    .voteAverage(movie.getVoteAverage())
                    .voteAverage(movie.getVoteAverage())
                    .releaseDate(movie.getReleaseDate())
                    .favored(movie.isFavored());
        }

        public ContentValues build() {
            return values;
        }
    }

}
