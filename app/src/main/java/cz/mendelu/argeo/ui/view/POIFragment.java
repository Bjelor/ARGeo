package cz.mendelu.argeo.ui.view;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import cz.mendelu.argeo.R;

/**
 * Created by Bjelis on 9.5.2017.
 */

public class POIFragment extends Fragment {

    private ImageView mImageView;

    private TextView mTitleView;

    private TextView mDescriptionView;

    private LinearLayout mContainer;

    private String title;
    private String description;
    private Image image;

    private View.OnClickListener mListener;

    public static POIFragment newInstance(Image image, String title, String description){
        POIFragment frag = new POIFragment();
        frag.setTitle(title);
        frag.setDescription(description);
        frag.setImage(image);
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
        mTitleView.setText(title);

        mContainer = (LinearLayout) view.findViewById(R.id.poi_container);

        if(mListener != null)
            mContainer.setOnClickListener(mListener);

        mDescriptionView = (TextView) view.findViewById(R.id.poi_description);
        mDescriptionView.setText(description);


        mImageView = (ImageView) view.findViewById(R.id.poi_image);
        Glide.with(this)
                .load(image)
                .into(mImageView);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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

    public void setImage(Image image) {
        this.image = image;
    }

    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
        if(mContainer != null)
            mContainer.setOnClickListener(listener);
    }
}
