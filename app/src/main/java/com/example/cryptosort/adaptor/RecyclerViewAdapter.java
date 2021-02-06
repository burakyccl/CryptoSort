package com.example.cryptosort.adaptor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptosort.R;
import com.example.cryptosort.model.CryptoModel;
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;

import java.util.ArrayList;
import java.util.List;

import database.FavDB;

import static com.example.cryptosort.R.drawable.noicon_drawable;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RowHolder>  implements Filterable {
    private ArrayList<CryptoModel> cryptoList;
    private ArrayList<CryptoModel> cryptoListFull;
    private ArrayList<CryptoModel> cryptoFavList;
    private Context mContext;
    private FavDB favDB;

    private String[] colors={"#D5EFF5","#F5CFEB","#C9F5E0","#F5D1B0", "#C6F5BC", "#F5EAB0", "#C9F5E0", "#97DBF6", "#F5E6BC", "#E6D5F5"};
    public RecyclerViewAdapter(ArrayList<CryptoModel> cryptoList, Context context) {
        this.cryptoList = cryptoList;
        cryptoListFull = new ArrayList<CryptoModel>(cryptoList);
        this.mContext = context;

    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        favDB = new FavDB(mContext);
        SharedPreferences prefs = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);
        if (firstStart){
            createTableOnFirstStart();
        }

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_layout,parent,false);

        return new RowHolder(view);
    }

    public class RowHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textPrice;
        TextView textLongName;
        ImageView imageLogo;
        Button buttonFav;
        Button buttonBinance;


        public RowHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(CryptoModel cryptoModel, String[] colors, Integer position, RowHolder holder) {
            itemView.setBackgroundColor(Color.parseColor(colors[position % colors.length]));
            textName = itemView.findViewById(R.id.text_name);
            textPrice = itemView.findViewById(R.id.text_price);
            textLongName = itemView.findViewById(R.id.text_longname);
            imageLogo = itemView.findViewById(R.id.image_logo);
            buttonFav = itemView.findViewById(R.id.button_fav);
            buttonBinance = itemView.findViewById(R.id.button_binance);


            textName.setText(cryptoModel.currency);
            textPrice.setText("$ "+cryptoModel.price);
            textLongName.setText(cryptoModel.name);
            GlideToVectorYou
                    .init()
                    .with(mContext)
                    .setPlaceHolder(noicon_drawable,noicon_drawable)
                    .load(Uri.parse(cryptoModel.logo_url), imageLogo);

            favDB.insertIntoTheDatabase(cryptoModel.getId(),cryptoModel.getFavStatus());

            readCursorData(cryptoList.get(position), holder);
            readFavCursorData(cryptoList.get(position), holder);


            buttonFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    CryptoModel cryptoModel = cryptoList.get(position);
                    if (cryptoModel.getFavStatus().equals("0")){
                        cryptoModel.setFavStatus("1");
                        favDB.add_fav(cryptoModel.getId());
                        buttonFav.setBackgroundResource(R.drawable.ic_baseline_favorite_red_24);
                    } else{
                        cryptoModel.setFavStatus("0");
                        favDB.remove_fav(cryptoModel.getId());
                        buttonFav.setBackgroundResource(R.drawable.ic_baseline_favorite_shadow_24);
                    }
                }
            });
            buttonBinance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("https://www.binance.com/en/trade/"+cryptoModel.getId()+"_BUSD"));
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        holder.bind(cryptoList.get(position),colors,position,holder);
    }

    @Override
    public int getItemCount() {
        return cryptoList.size();
    }


    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CryptoModel> filteredList=new ArrayList<CryptoModel>();
            if(constraint==null || constraint.length()==0){
                filteredList.addAll(cryptoListFull);
            }else{
                String filterPattern=constraint.toString().toLowerCase().trim();
                for(CryptoModel item :cryptoListFull){
                    if(item.currency.toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values=filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            cryptoList.clear();

            cryptoList.addAll((List)results.values);
            System.out.println(results);
            notifyDataSetChanged();
        }
    };

    private void createTableOnFirstStart() {
        favDB.insertEmpty();

        SharedPreferences prefs = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    private void readFavCursorData(CryptoModel cryptoModel, RowHolder holder) {
        cryptoFavList = new ArrayList<CryptoModel>(cryptoList);
        cryptoFavList.clear();
        ArrayList favDbList = new ArrayList();

        SQLiteDatabase db = favDB.getReadableDatabase();
        Cursor cursor = favDB.select_all_fav_list();

        try {
            while (cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(FavDB.KEY_ID));
                if (!favDbList.contains(id)){
                    favDbList.add(id);
                }
            }
        }finally {
            if (cursor != null && cursor.isClosed())
                cursor.close();
            db.close();
        }

        for (CryptoModel cr : cryptoList){
            if (favDbList.contains(cr.getId())){
                cryptoFavList.add(cr);
            }
        }
    }

    public void showFavList(int clickFavSort){
        if (clickFavSort == 0){
            cryptoList.clear();

            cryptoList.addAll(cryptoFavList);

            notifyDataSetChanged();
        }
    }

    private void readCursorData(CryptoModel cryptoModel, RowHolder holder) {
        System.out.println("READCURSER");
        Cursor cursor = favDB.read_fav_status(cryptoModel.getId());
        SQLiteDatabase db = favDB.getReadableDatabase();

        try {
            while (cursor.moveToNext()){
                String item_fav_status = cursor.getString(cursor.getColumnIndex(FavDB.FAVORITE_STATUS));
                cryptoModel.setFavStatus(item_fav_status);

                if (item_fav_status != null && item_fav_status.equals("1")){
                    holder.buttonFav.setBackgroundResource(R.drawable.ic_baseline_favorite_red_24);
                } else if (item_fav_status != null && item_fav_status.equals("0")){
                    holder.buttonFav.setBackgroundResource(R.drawable.ic_baseline_favorite_shadow_24);
                }
            }
        } finally {
            if (cursor != null && cursor.isClosed())
                cursor.close();
            db.close();
        }
    }
}
