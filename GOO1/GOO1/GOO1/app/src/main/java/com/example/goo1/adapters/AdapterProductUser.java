package com.example.goo1.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goo1.FilterProductUser;
import com.example.goo1.R;
import com.example.goo1.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser>implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList,filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList=productsList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);

        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {

        ModelProduct modelProduct=productsList.get(position);
        //get data
        String discountNote =modelProduct.getDiscountNote();
        String discountAvailable =modelProduct.getDiscountAvailable();
        String discountPrice =modelProduct.getDiscountPrice();
        String productCategory =modelProduct.getProductCategory();
        String productDescription =modelProduct.getProductDescription();
        String originalPrice =modelProduct.getOriginalPrice();
        String productQuantity =modelProduct.getProductQuantity();
        String productTitle =modelProduct.getProductTitle();
        String timestamp =modelProduct.getTimestamp();
        String productIcon =modelProduct.getProductIcon();
        String productId =modelProduct.getProductId();

        //set data
        holder.titleTv.setText(productTitle);
        holder.descriptionTv.setText(productDescription);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("Rs."+discountPrice);
        holder.originalPriceTv.setText("Rs."+originalPrice);

        if(discountAvailable.equals("true")){
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        else{
            // product is on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);

        }
        try{
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIv);
        }
        catch(Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add product to cart
                showQuantityDialog(modelProduct);

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }
    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 1;

    private void showQuantityDialog(ModelProduct modelProduct) {
        // inflate lyaout for dialog
        View  view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        //init layout views
        ImageView productIv = view.findViewById(R.id.productIv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv =view.findViewById(R.id.priceDiscountedTv);
        TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBt = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);
        //get data from model
        String productId = modelProduct.getProductId();
        String title =modelProduct.getProductTitle();
        String productQuantity =modelProduct.getProductQuantity();
        String description =modelProduct.getProductDescription();
        String discountNote =modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();

        String price;
        if(modelProduct.getDiscountAvailable().equals("true")){
            //product has discount
            price = modelProduct.getDiscountPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        else{
            //product don't have discount
            discountedNoteTv.setVisibility(View.GONE);
            priceDiscountedTv.setVisibility(View.GONE);
            price =modelProduct.getOriginalPrice();

        }
        cost = Double.parseDouble(price.replaceAll("Rs.",""));
        finalCost =Double.parseDouble(price.replaceAll("Rs.",""));
        quantity =1;
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        //set data
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_gray).into(productIv);
        }
        catch (Exception e){
            productIv.setImageResource(R.drawable.ic_cart_gray);
        }
        titleTv.setText(""+title);
        pQuantityTv.setText(""+productQuantity);
        descriptionTv.setText(""+description);
        discountedNoteTv.setText(""+discountNote);
        originalPriceTv.setText("Rs."+modelProduct.getOriginalPrice());
        priceDiscountedTv.setText("Rs."+modelProduct.getDiscountPrice());
        finalPriceTv.setText("Rs."+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();
        //increase quqntity
        incrementBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity = quantity+1;
                finalPriceTv.setText("Rs."+finalCost);
                quantityTv.setText(""+quantity);
            }
        });
        //   decrease quantity
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost - cost;
                quantity = quantity-1;
                finalPriceTv.setText("Rs."+finalCost);
                quantityTv.setText(""+quantity);
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalprice = finalPriceTv.getText().toString().trim().replace("Rs.","");
                String quantity = quantityTv.getText().toString().trim();

                //add to db(SQLite)
                addToCart(productId,title,priceEach,totalprice,quantity);
                dialog.dismiss();
            }
        });
    }

    private int itemId = 1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();
        Boolean b = easyDB.addData("Item_Id",itemId)
                .addData("Item_PID",productId)
                .addData("Item_Name",title)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();
        Toast.makeText(context,"Added to cart", Toast.LENGTH_SHORT).show();

    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter=new FilterProductUser(this,filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{

        private ImageView productIconIv;
        private TextView discountedNoteTv,titleTv,descriptionTv,addToCartTv,discountedPriceTv,originalPriceTv;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            productIconIv=itemView.findViewById(R.id.productIconIv);
            discountedNoteTv=itemView.findViewById(R.id.discountedNoteTv);
            titleTv=itemView.findViewById(R.id.titleTv);
            descriptionTv=itemView.findViewById(R.id.descriptionTv);
            addToCartTv=itemView.findViewById(R.id.addToCartTv);
            discountedPriceTv=itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv=itemView.findViewById(R.id.originalPriceTv);







        }
    }
}
