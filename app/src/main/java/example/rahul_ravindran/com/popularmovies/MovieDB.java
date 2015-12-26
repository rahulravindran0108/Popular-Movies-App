package example.rahul_ravindran.com.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahulravindran on 06/12/15.
 */
public class MovieDB implements Parcelable {

    @Expose
    long id;

    @Expose @SerializedName("genre_ids")
    List<Integer> genreIds = new ArrayList<>();

    @Expose @SerializedName("backdrop_path")
    String backdropPath;

    @Expose @SerializedName("original_language")
    String originalLanguage;

    @Expose
    String overview;

    @Expose @SerializedName("original_title")
    String originalTitle;

    @Expose @SerializedName("poster_path")
    String posterPath;

    @Expose
    String title;

    @Expose @SerializedName("vote_average")
    double voteAverage;

    @Expose @SerializedName("vote_count")
    long voteCount;

    @Expose @SerializedName("release_date")
    String releaseDate;

    @Expose
    double popularity;

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long voteCount) {
        this.voteCount = voteCount;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public static final class Response {

        @Expose
        public int page;

        @Expose @SerializedName("total_pages")
        public int totalPages;

        @Expose @SerializedName("total_results")
        public int totalMovies;

        @Expose @SerializedName("results")
        public List<MovieDB> movies = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeList(this.genreIds);
        dest.writeString(this.overview);
        dest.writeString(this.releaseDate);
        dest.writeString(this.posterPath);
        dest.writeString(this.backdropPath);
        dest.writeDouble(this.popularity);
        dest.writeString(this.title);
        dest.writeDouble(this.voteAverage);
        dest.writeLong(this.voteCount);
    }

    protected MovieDB(Parcel in) {
        this.id = in.readLong();
        this.genreIds = new ArrayList<Integer>();
        in.readList(this.genreIds, List.class.getClassLoader());
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.posterPath = in.readString();
        this.backdropPath = in.readString();
        this.popularity = in.readDouble();
        this.title = in.readString();
        this.voteAverage = in.readDouble();
        this.voteCount = in.readLong();
    }

    public static final Creator<MovieDB> CREATOR = new Creator<MovieDB>() {
        public MovieDB createFromParcel(Parcel source) {return new MovieDB(source);}

        public MovieDB[] newArray(int size) {return new MovieDB[size];}
    };
}
