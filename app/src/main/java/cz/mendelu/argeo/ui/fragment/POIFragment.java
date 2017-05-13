package cz.mendelu.argeo.ui.fragment;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Locale;

import cz.mendelu.argeo.R;
import cz.mendelu.argeo.util.ARLog;

/**
 * Created by Bjelis on 9.5.2017.
 */

public class POIFragment extends Fragment {

    private ImageView mImageView;
    private TextView mTitleView;
    private TextView mDistanceView;
    private TextView mDescriptionView;
    private RelativeLayout mContainer;

    private String title;
    private String description;
    private float distance;
    private String imageUrl;

    private View.OnClickListener mListener;

    public static POIFragment newInstance(String title, String description, String image){
        POIFragment frag = new POIFragment();
        frag.setTitle(title);
        frag.setDescription(description);
        frag.setImageUrl(image);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_poi, container, false);

        mTitleView = (TextView) view.findViewById(R.id.poi_title);
        setTitle(title);

        mDistanceView = (TextView) view.findViewById(R.id.poi_distance);
        setDistance(distance);

        mDescriptionView = (TextView) view.findViewById(R.id.poi_description);
        setDescription(description);

        mContainer = (RelativeLayout) view.findViewById(R.id.poi_container);

        if(mListener != null)
            mContainer.setOnClickListener(mListener);

        mImageView = (ImageView) view.findViewById(R.id.poi_image);
        setImageUrl(imageUrl);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARLog.d("clicked!!!!!");
            }
        });

    }

    public void setTitle(String title) {
        this.title = title;
        if(mTitleView != null)
            mTitleView.setText(title);
    }

    public void setDescription(String description) {
        this.description = description;
        if(mDescriptionView != null)
            mDescriptionView.setText(description);
    }

    public void setDistance(float distance) {
        this.distance = distance;
        if(mDistanceView != null)
            mDistanceView.setText(distanceToString(distance));
    }

    private String distanceToString(float distance){
        String text;
        if(distance >= 1000.0f){
            distance = distance / 1000.0f;
            text = String.format(Locale.getDefault(), "%.1f km", distance);
        } else {
            text = String.format(Locale.getDefault(), "%.0f m", distance);
        }
        return text;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        if(mImageView != null && imageUrl != null)
            Glide.with(this)
                    .load(imageUrl)
                    .into(mImageView);
    }

    public void setOnClickListener(View.OnClickListener listener){
//        mListener = listener;
//        if(mContainer != null)
//            mContainer.setOnClickListener(listener);
    }
}
