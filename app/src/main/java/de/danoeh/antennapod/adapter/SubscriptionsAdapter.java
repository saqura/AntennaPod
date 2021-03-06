package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.lang.ref.WeakReference;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.glide.ApGlideSettings;
import de.danoeh.antennapod.fragment.AddFeedFragment;
import de.danoeh.antennapod.fragment.ItemlistFragment;
import jp.shts.android.library.TriangleLabelView;

/**
 * Adapter for subscriptions
 */
public class SubscriptionsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    /** placeholder object that indicates item should be added */
    public static final Object ADD_ITEM_OBJ = new Object();

    /** the position in the view that holds the add item */
    private static final int ADD_POSITION = 0;

    private NavListAdapter.ItemAccess itemAccess;

    private final WeakReference<MainActivity> mainActivityRef;

    public SubscriptionsAdapter(MainActivity mainActivity, NavListAdapter.ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
        this.mainActivityRef = new WeakReference<>(mainActivity);
    }

    public void setItemAccess(NavListAdapter.ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
    }

    private int getAdjustedPosition(int origPosition) {
        return origPosition - 1;
    }

    @Override
    public int getCount() {
        return 1 + itemAccess.getCount();
    }

    @Override
    public Object getItem(int position) {
        if (position == ADD_POSITION) {
            return ADD_ITEM_OBJ;
        }
        return itemAccess.getItem(getAdjustedPosition(position));
    }

    @Override
    public long getItemId(int position) {
        if (position == ADD_POSITION) {
            return 0;
        }
        return itemAccess.getItem(getAdjustedPosition(position)).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            holder = new Holder();

            LayoutInflater layoutInflater =
                    (LayoutInflater) mainActivityRef.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.subscription_item, parent, false);
            holder.feedTitle = (TextView) convertView.findViewById(R.id.txtvTitle);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imgvCover);
            holder.count = (TriangleLabelView) convertView.findViewById(R.id.triangleCountView);


            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        if (position == ADD_POSITION) {
            holder.feedTitle.setText(R.string.add_feed_label);
            holder.count.setVisibility(View.INVISIBLE);
            Glide.with(mainActivityRef.get())
                    .load(R.drawable.ic_add_grey_600_48dp)
                    .dontAnimate()
                    .into(holder.imageView);
            return convertView;
        }

        final Feed feed = (Feed) getItem(position);
        if (feed == null) return null;

        holder.feedTitle.setText(feed.getTitle());
        holder.count.setVisibility(View.VISIBLE);
        holder.count.setPrimaryText(String.valueOf(itemAccess.getFeedCounter(feed.getId())));
        Glide.with(mainActivityRef.get())
                .load(feed.getImageUri())
                .placeholder(R.color.light_gray)
                .error(R.color.light_gray)
                .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                .fitCenter()
                .dontAnimate()
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.feedTitle.setVisibility(View.INVISIBLE);
                        return false;
                    }
                })
                .into(holder.imageView);

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == ADD_POSITION) {
            mainActivityRef.get().loadChildFragment(new AddFeedFragment());
        } else {
            Fragment fragment = ItemlistFragment.newInstance(getItemId(position));
            mainActivityRef.get().loadChildFragment(fragment);
        }
    }

    static class Holder {
        public TextView feedTitle;
        public ImageView imageView;
        public TriangleLabelView count;
    }
}
