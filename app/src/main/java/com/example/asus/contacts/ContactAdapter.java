package com.example.asus.contacts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactAdapter extends BaseAdapter implements Filterable {

    private Context context;
    ArrayList<ContactHolder> contactHolderArrayList;

    CustomFilter customFilter;
    ArrayList<ContactHolder> filterList;

    public ContactAdapter(Context context, ArrayList<ContactHolder> contactHolderArrayList) {
        this.context = context;
        this.contactHolderArrayList = contactHolderArrayList;
        this.filterList = contactHolderArrayList;
    }

    @Override
    public int getCount() {
        return contactHolderArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactHolderArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class MyViewHolder{

        ImageView myImage;
        TextView myName;
        TextView myNumber;
        TextView callTextView;

        MyViewHolder(View v){

             myImage = v.findViewById(R.id.customImageViewId);
             myName = v.findViewById(R.id.nameTextViewId);
             myNumber = v.findViewById(R.id.numberTextViewId);
             callTextView = v.findViewById(R.id.id);
        }
    }

    //check permission of calling
    private boolean checkCallingPermission(){
        String permission = "android.permission.CALL_PHONE";
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        MyViewHolder myViewHolder = null;
        View view = convertView;

        if(view == null){

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view =layoutInflater.inflate(R.layout.custom_item_layout,null);

            myViewHolder = new MyViewHolder(view);
            view.setTag(myViewHolder);

        }else{
            myViewHolder = (MyViewHolder) view.getTag();
        }



        ContactHolder contactHolder = contactHolderArrayList.get(position);

        byte[] byteImage = contactHolder.image;
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);

        myViewHolder.myImage.setImageBitmap(bitmap);
        myViewHolder.myName.setText(contactHolder.name);
        myViewHolder.myNumber.setText(contactHolder.number);

        Animation animation = AnimationUtils.loadAnimation(context,R.anim.slide_left);
        view.startAnimation(animation);


        //calling button action
        myViewHolder.callTextView.setClickable(true);
        myViewHolder.callTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkCallingPermission()){
                    context.startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+contactHolderArrayList.get(position).number)));
                }
            }
        });
        return view;
    }



    @Override
    public Filter getFilter() {

        if(customFilter == null){
            customFilter = new CustomFilter();
        }
        return customFilter;
    }

    class CustomFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults filterResults = new FilterResults();

            if(constraint != null && constraint.length() > 0){
                //constraint to upper
                constraint = constraint.toString().toUpperCase();
                ArrayList<ContactHolder> filters = new ArrayList<ContactHolder>();

                //get specific item
                for(int i=0; i<filterList.size(); i++){
                    if(filterList.get(i).name.toUpperCase().contains(constraint)){
                        ContactHolder ch = new ContactHolder(filterList.get(i).image, filterList.get(i).name,filterList.get(i).number);
                        filters.add(ch);
                    }
                }
                filterResults.count = filters.size();
                filterResults.values = filters;

            }
            else {

                filterResults.count = filterList.size();
                filterResults.values = filterList;

            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            contactHolderArrayList = (ArrayList<ContactHolder>) results.values;
            notifyDataSetChanged();

        }
    }
}
