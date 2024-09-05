package com.example.goo1.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.goo1.Constants;
import com.example.goo1.R;
import com.example.goo1.adapters.AdapterCartItem;
import com.example.goo1.adapters.AdapterProductUser;
import com.example.goo1.models.ModelCartIteam;
import com.example.goo1.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    private ImageView shopIv;
    private TextView shopNameTv,phoneTv,emailTv,openCloseTv,deliveryFeeTv,addressTv,filteredProductsTv;
    private ImageButton callBtn,mapBtn,cartBtn,backBtn,filterProductBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;

    private String shopUid;
    private String myLatitude,myLongitude,myPhone;
    private String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongitude;
    public String deliveryFee;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;
    //cart
    private ArrayList<ModelCartIteam> cartIteamList;
    private AdapterCartItem adapterCartItem;
    // progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopIv=findViewById(R.id.shopIv);
        shopNameTv=findViewById(R.id.shopNameTv);
        phoneTv=findViewById(R.id.phoneTv);
        emailTv=findViewById(R.id.emailTv);
        openCloseTv=findViewById(R.id.openCloseTv);
        deliveryFeeTv=findViewById(R.id.deliveryFeeTv);
        addressTv=findViewById(R.id.addressTv);
        filteredProductsTv=findViewById(R.id.filteredProductsTv);
        callBtn=findViewById(R.id.callBtn);
        mapBtn=findViewById(R.id.mapBtn);
        cartBtn=findViewById(R.id.cartBtn);
        backBtn=findViewById(R.id.backBtn);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        searchProductEt=findViewById(R.id.searchProductEt);
        productsRv=findViewById(R.id.productsRv);
        // init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading details");
        progressDialog.setCanceledOnTouchOutside(false);

        shopUid =getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        //each shop have its own products so there should be different cart for different shops
        //so we need to delete cart data whenever user open this activity

        deleteCartData();
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    adapterProductUser.getFilter().filter(s);

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show items in cart
                showCartDialog();

            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String selected =Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if(selected.equals("All")){
                                    loadShopProducts();
                                }
                                else{
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        }).show();
            }
        });

    }

    private void deleteCartData() {
        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();
        easyDB.deleteAllDataFromTable();//deletes all records from cart
    }

    public double allTotalPrice = 0.00;
    //needs to access these views in adapter so making public
    public TextView sTotalTv,dFeeTv,allTotalPriceTv;
    private void showCartDialog() {
        //init list
        cartIteamList = new ArrayList<>();

        // inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        // init views
        TextView shopNameTv = view.findViewById(R.id .shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
         sTotalTv = view.findViewById(R.id.sTotalTv);
         dFeeTv = view.findViewById(R.id.dFeeTv);
         allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);
        shopNameTv.setText(shopName);
        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();
        //get all records from db
        Cursor res  = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);
            allTotalPrice = allTotalPrice+ Double.parseDouble(cost);
            ModelCartIteam modelCartIteam = new ModelCartIteam(""+id,""+pId,""+name,""+price,""+cost,""+quantity);
            cartIteamList.add(modelCartIteam);

        }
        //set adapter
        adapterCartItem = new AdapterCartItem(this,cartIteamList);
        //set to recycler view
        cartItemsRv.setAdapter(adapterCartItem);
        dFeeTv.setText("Rs."+deliveryFee);
        sTotalTv.setText("Rs."+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("Rs."+(allTotalPrice+Double.parseDouble(deliveryFee.replace("Rs.",""))));
        AlertDialog dialog = builder.create();
        dialog.show();

        // reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });
        //cheout
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // first validate delivery address
                if(myLatitude.equals("")|| myLatitude.equals("null")||myLongitude.equals("null")||myLongitude.equals("")){
                    Toast.makeText(ShopDetailsActivity.this,"Please enter your address in your profile before placing order",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(myPhone.equals("")||myPhone.equals("null")){
                    Toast.makeText(ShopDetailsActivity.this,"Please enter your mobile in your profile before placing order",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(cartIteamList.size() == 0){
                    Toast.makeText(ShopDetailsActivity.this,"Your Cart is empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();

            }
        });

    }

    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Placing your order");
        progressDialog.show();

        //for order id and order time
        String timestamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTv.getText().toString().trim().replace("Rs.","");
        // setup order data
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","Inprogress");
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    //added basic order info now adding order items
                        for(int i=0;i<cartIteamList.size();i++)
                        {
                            String pId = cartIteamList.get(i).getpId();
                            String id = cartIteamList.get(i).getId();
                            String cost = cartIteamList.get(i).getCost();
                            String price = cartIteamList.get(i).getPrice();
                            String name = cartIteamList.get(i).getName();
                            String quantity = cartIteamList.get(i).getQuantity();
                            HashMap<String,String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this,"Order Placed",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this,"Placing failed "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMap() {
        String address="https://maps.google.com/maps?saddr"+myLatitude+","+myLongitude+"&daddr"+shopLatitude+","+shopLongitude;
        Intent intent =new Intent(Intent.ACTION_VIEW,Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                             myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadShopDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name =""+dataSnapshot.child("name").getValue();
                        shopName =""+dataSnapshot.child("shopName").getValue();
                        shopEmail =""+dataSnapshot.child("email").getValue();
                        shopPhone =""+dataSnapshot.child("phone").getValue();
                        shopLatitude =""+dataSnapshot.child("latitude").getValue();
                        shopAddress =""+dataSnapshot.child("address").getValue();
                        shopLongitude =""+dataSnapshot.child("longitude").getValue();
                        deliveryFee =""+dataSnapshot.child("delivery fee").getValue();
                        String profileImage =""+dataSnapshot.child("profileImage").getValue();
                        String shopOpen =""+dataSnapshot.child("shopOpen").getValue();


                        shopNameTv.setText(shopName);
                        emailTv.setText(shopEmail);
                        deliveryFeeTv.setText("Delivery Fee Rs.:"+deliveryFee);
                        addressTv.setText(shopAddress);
                        phoneTv.setText(shopPhone);
                        if(shopOpen.equals("true")){
                            openCloseTv.setText("Open");
                        }
                        else{
                            openCloseTv.setText("Closed");

                        }
                        try{
                            Picasso.get().load(profileImage).into(shopIv);
                        }catch(Exception e)
                        {

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadShopProducts() {
        productsList= new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        productsList.clear();
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelProduct modelProduct =ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        adapterProductUser =new AdapterProductUser(ShopDetailsActivity.this,productsList);
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


}