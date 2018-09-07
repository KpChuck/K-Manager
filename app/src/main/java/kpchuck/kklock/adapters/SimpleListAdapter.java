package kpchuck.kklock.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;

import gr.escsoft.michaelprimez.revealedittext.tools.UITools;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.ISpinnerSelectedView;
import kpchuck.kklock.R;

/**
 * Created by michael on 1/8/17.
 */

public class SimpleListAdapter extends BaseAdapter implements Filterable, ISpinnerSelectedView {

    private Context mContext;
    private ArrayList<String> mBackupStrings;
    private ArrayList<String> mStrings;
    private StringFilter mStringFilter;

    public SimpleListAdapter(Context context, ArrayList<String> strings) {
        mContext = context;
        mStrings = strings;
        mBackupStrings = strings;
        mStringFilter = new StringFilter(mContext.getResources().getString(R.string.otherRomsBeta));
    }

    @Override
    public int getCount() {
        return mStrings == null ? 0 : mStrings.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (mStrings != null && position > 0)
            return mStrings.get(position - 1);
        else
            return mContext.getResources().getString(R.string.chooseRom);
    }

    @Override
    public long getItemId(int position) {
        if (mStrings == null && position > 0)
            return mStrings.get(position).hashCode();
        else
            return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (position == 0) {
            view = getNoSelectionView();
        } else {
            view = View.inflate(mContext, R.layout.view_list_item, null);
            ImageView letters = view.findViewById(R.id.ImgVw_Letters);
            TextView dispalyName = view.findViewById(R.id.TxtVw_DisplayName);
            letters.setImageDrawable(getTextDrawable(mStrings.get(position-1)));
            dispalyName.setText(mStrings.get(position-1));

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.secondaryBackgroundColor, typedValue, true);
            @ColorInt int color = typedValue.data;
            view.setBackgroundColor(color);
        }
        return view;
    }

    @Override
    public View getSelectedView(int position) {
        View view = null;
        if (position == 0) {
            view = getNoSelectionView();
        } else {
            view = View.inflate(mContext, R.layout.view_list_item, null);
            ImageView letters = view.findViewById(R.id.ImgVw_Letters);
            TextView dispalyName = view.findViewById(R.id.TxtVw_DisplayName);
            letters.setImageDrawable(getTextDrawable(mStrings.get(position-1)));
            dispalyName.setText(mStrings.get(position-1));

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.secondaryBackgroundColor, typedValue, true);
            @ColorInt int color = typedValue.data;
            view.setBackgroundColor(color);
        }
        return view;
    }

    @Override
    public View getNoSelectionView() {
        View view = View.inflate(mContext, R.layout.view_list_no_selection_item, null);
        return view;
    }

    private TextDrawable getTextDrawable(String displayName) {
        TextDrawable drawable = null;
        if (!TextUtils.isEmpty(displayName)) {
            int color2 = ColorGenerator.MATERIAL.getColor(displayName);
            drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(UITools.dpToPx(mContext, 32))
                    .height(UITools.dpToPx(mContext, 32))
                    .textColor(Color.WHITE)
                    .toUpperCase()
                    .endConfig()
                    .round()
                    .build(displayName.substring(0, 1), color2);
        } else {
            drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(UITools.dpToPx(mContext, 32))
                    .height(UITools.dpToPx(mContext, 32))
                    .endConfig()
                    .round()
                    .build("?", Color.GRAY);
        }
        return drawable;
    }

    @Override
    public Filter getFilter() {
        return mStringFilter;
    }

    public class StringFilter extends Filter {

        private String otherRoms;

        public StringFilter(String otherRoms){
            this.otherRoms = otherRoms;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults filterResults = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = mBackupStrings.size();
                filterResults.values = mBackupStrings;
                return filterResults;
            }
            final ArrayList<String> filterStrings = new ArrayList<>();
            for (String text : mBackupStrings) {
                if (text.toLowerCase().contains(constraint.toString().toLowerCase()) || text.equals(otherRoms)) {
                    filterStrings.add(text);

                }
            }
            filterResults.count = filterStrings.size();
            filterResults.values = filterStrings;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mStrings = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    }

}