package com.example.goo1.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goo1.FilterProduct;
import com.example.goo1.R;
import com.example.goo1.activities.EditProductActivity;
import com.example.goo1.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList,filterList;
    private FilterProduct filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList=productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);

        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
    ModelProduct modelProduct=productList.get(position);
        String id =modelProduct.getProductId();
        String uid =modelProduct.getUid();
        String discountNote =modelProduct.getDiscountNote();
        String discountAvailable =modelProduct.getDiscountAvailable();
        String discountPrice =modelProduct.getDiscountPrice();
        String productCategory =modelProduct.getProductCategory();
        String productDescription =modelProduct.getProductDescription();
        String icon =modelProduct.getProductIcon();
        String quantity =modelProduct.getProductQuantity();
        String title =modelProduct.getProductTitle();
        String timestamp =modelProduct.getTimestamp();
        String originalPrice =modelProduct.getOriginalPrice();


        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("Rs."+discountPrice);
        holder.originalPriceTv.setText("Rs."+originalPrice);
        if(discountAvailable.equals("true")){
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        else{
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);

        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIv);
        }
        catch(Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //handle item clicks, show item details(in bottom sheet)
                detailsBottomSheet(modelProduct);//here the model contains detaits of clicked product

            }
        });


    }
    private void detailsBottomSheet(ModelProduct modelProduct){
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view of bottomsheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        //set  view of bottom sheet
        bottomSheetDialog.setContentView(view);


        //init  views of bottom sheet
        ImageButton backBtn =view.findViewById(R.id.backBtn);
        ImageButton deleteBtn =view.findViewById(R.id.deleteBtn);
        ImageButton editBtn =view.findViewById(R.id.editBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView tittleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);

        //get data
        String id =modelProduct.getProductId();
        String uid =modelProduct.getUid();
        String discountAvailable =modelProduct.getDiscountAvailable();
        String discountNote =modelProduct.getDiscountNote();
        String discountPrice =modelProduct.getDiscountPrice();
        String productCategory =modelProduct.getProductCategory();
        String productDescription =modelProduct.getProductDescription();
        String icon =modelProduct.getProductIcon();
        String quantity =modelProduct.getProductQuantity();
        String title =modelProduct.getProductTitle();
        String timestamp =modelProduct.getTimestamp();
        String originalPrice =modelProduct.getOriginalPrice();

        // set data
        tittleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountNoteTv.setText(discountNote);
        discountedPriceTv.setText("Rs."+discountPrice);
        originalPriceTv.setText("Rs."+originalPrice);
        if(discountAvailable.equals("true")){
            // product is on discount
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);// add strike through an orginal price

        }else{
            // no discount
            discountedPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);

        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(productIconIv);
        }
        catch(Exception e){
            productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        // show dialog
        bottomSheetDialog.show();
        //editclick
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //opens edit product activity, pass id of product
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);

            }
        });
        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                // show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you ssure you want to delete product "+title+"?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // delete
                                deleteProduct(id);//id is the product id
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });
        //back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void deleteProduct(String id) {
        //delete product
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Product deleted
                        Toast.makeText(context,"Product deleted..",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterProduct(this,filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{

        private ImageView productIconIv;
        private TextView discountedNoteTv,titleTv,quantityTv,discountedPriceTv,originalPriceTv;
        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
            productIconIv = itemView.findViewById(R.id.productIconIv);

        }
    }
}
