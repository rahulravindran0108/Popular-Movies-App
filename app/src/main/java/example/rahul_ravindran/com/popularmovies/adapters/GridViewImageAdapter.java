package example.rahul_ravindran.com.popularmovies.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.ui.ResizableImageView;

/**
 * Created by rahulravindran on 15/12/15.
 */
public class GridViewImageAdapter extends BaseAdapter {

    private Context mContext;
    private List<MovieDB> movies;
    private String baseImageUrl = "http://image.tmdb.org/t/p/w185/";

    public GridViewImageAdapter(Context c, List<MovieDB> movies) {
        this.mContext = c;
        this.movies = movies;
    }

    @Override
    public int getCount() {

        return this.movies.size();
    }

    @Override
    public Object getItem(int position) {
        return this.movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.movies.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ResizableImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes

            imageView = new ResizableImageView(this.mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(-1, -1));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setAdjustViewBounds(true);


        } else {
            imageView = (ResizableImageView) convertView;
        }

        Log.i("popMovies","Image url is:"+baseImageUrl+this.movies.get(position).getBackdropPath());

        //load from piccasso
        Picasso.with(this.mContext).load(baseImageUrl +
                this.movies.get(position).getBackdropPath()).into(imageView);
        return imageView;
    }
}
