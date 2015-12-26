package example.rahul_ravindran.com.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahulravindran on 25/12/15.
 */
public class MovieReview implements Parcelable {

    @Expose
    long id;

    @Expose
    String author;

    @Expose
    String content;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    //response code
    public static final class Response {

        @Expose
        public int id;

        @Expose
        public int page;

        @Expose @SerializedName("total_pages")
        public int totalPages;

        @Expose @SerializedName("total_results")
        public int totalResults;

        @Expose @SerializedName("results")
        public List<MovieReview> reviews = new ArrayList<>();
    }
    protected MovieReview(Parcel in) {
        this.id = in.readLong();
        this.author = in.readString();
        this.content = in.readString();
    }

    public static final Creator<MovieReview> CREATOR = new Creator<MovieReview>() {
        @Override
        public MovieReview createFromParcel(Parcel in) {
            return new MovieReview(in);
        }

        @Override
        public MovieReview[] newArray(int size) {
            return new MovieReview[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.author);
        dest.writeString(this.content);
    }


}
