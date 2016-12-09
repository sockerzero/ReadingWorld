package com.dnd.readingworld.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.dnd.readingworld.Model.ContentWord;
import com.dnd.readingworld.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus on 11/27/2016.
 */

public class MyListWord_ItemAdapter extends BaseAdapter implements Filterable {

    Context myContext;
    int myLayout;
    List<ContentWord> arayContent;
    List<ContentWord> arayContentafterFilter;

    public MyListWord_ItemAdapter(Context myContext, int myLayout, List<ContentWord> arayContent) {
        this.myContext = myContext;
        this.myLayout = myLayout;
        this.arayContentafterFilter = arayContent;
        this.arayContent = arayContent;
    }


    @Override
    public int getCount() {
        return arayContent.size();
    }

    @Override
    public ContentWord getItem(int position) {
        return arayContent.get(position);
    }

    public List<ContentWord> getList1()
    {
        return arayContentafterFilter;
    }
    public List<ContentWord> getList()
    {
        return arayContent;
    }

    public void removeItemInarayContentAfterFilter(int position) {
        arayContentafterFilter.remove(position);
        notifyDataSetChanged();
    }
    public void removeItemInarayContent(int position)
    {
        arayContent.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class viewHolder{
        TextView txtWord, txtType;
        ImageView imageView;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowview = convertView;
        viewHolder holder = new viewHolder();
        if(rowview == null)
        {
            rowview = inflater.inflate(myLayout,null);
            holder.txtWord = (TextView) rowview.findViewById(R.id.word);
            holder.txtType = (TextView) rowview.findViewById(R.id.wordType);
            holder.imageView = (ImageView) rowview.findViewById(R.id.imgView);
            rowview.setTag(holder);
        }else{
            holder = (viewHolder) rowview.getTag();
        }

        holder.txtWord.setText(arayContent.get(position).getWordContent());
        holder.txtType.setText(arayContent.get(position).getWordType());

        Picasso.with(myContext).load(arayContent.get(position).getLinkImage()).into(holder.imageView);

        return rowview;
    }

    private ItemFilter mFilter = new ItemFilter();

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<ContentWord> list = arayContentafterFilter;

            int count = list.size();
            final ArrayList<ContentWord> nlist = new ArrayList<ContentWord>(count);

            ContentWord filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.getWordContent().toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arayContent = (ArrayList<ContentWord>) results.values;
            notifyDataSetChanged();
        }

    }
}

