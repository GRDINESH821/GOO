package com.example.goo1.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goo1.R;
import com.example.goo1.activities.ShopDetailsActivity;
import com.example.goo1.models.ModelCartIteam;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {
    private Context context;
    private ArrayList<ModelCartIteam> cartIteams;

    public AdapterCartItem(Context context, ArrayList<ModelCartIteam> cartIteams) {
        this.context = context;
        this.cartIteams = cartIteams;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cartite.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem,parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        //get data
        ModelCartIteam modelCartIteam = cartIteams.get(position);
        String id = modelCartIteam.getId();
        String getpId =modelCartIteam.getpId();
        String title =modelCartIteam.getName();
        String cost = modelCartIteam.getCost();
        String price =modelCartIteam.getPrice();
        String quantity = modelCartIteam.getQuantity();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText(""+quantity);
        holder.itemPriceEachTv.setText(""+price);

        //handle remove click listner, delete item from cart
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // will create table if it doesnot exist
                EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                        .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                        .doneTableColumn();
                easyDB.deleteRow(1,id);
                Toast.makeText(context,"Removed fromcart..",Toast.LENGTH_SHORT).show();
                //refrest list
                cartIteams.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();
                double tx  = Double.parseDouble(((ShopDetailsActivity)context).allTotalPriceTv.getText().toString().trim().replace("Rs.",""));
                double totalPrice = tx-Double.parseDouble(cost.replace("Rs.",""));
                double deliverFee = Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("Rs.",""));
                double sTotalPrice = Double.parseDouble(String.format("%.2f",totalPrice))-Double.parseDouble(String.format("%.2f",deliverFee));
                ((ShopDetailsActivity)context).allTotalPrice=0.00;
                ((ShopDetailsActivity)context).sTotalTv.setText("Rs."+String.format("%.2f",sTotalPrice));
                ((ShopDetailsActivity)context).allTotalPriceTv.setText("Rs."+String.format("%.2f",Double.parseDouble(String.format("%.2f",totalPrice))));

            }
        });

    }

    @Override
    public int getItemCount() {
        return cartIteams.size();
    }


    //view holder class
    class HolderCartItem extends RecyclerView.ViewHolder{


        //ui views of row_cartitems.xml
        private TextView  itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv,itemRemoveTv;
        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            //init views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);

        }
    }
}
