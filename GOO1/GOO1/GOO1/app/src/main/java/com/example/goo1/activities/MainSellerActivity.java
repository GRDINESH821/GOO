package com.example.goo1.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.goo1.adapters.AdapterProductSeller;
import com.example.goo1.Constants;
import com.example.goo1.models.ModelProduct;
import com.example.goo1.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameTv,shopNameTv,emailTv,tabProductsTv,tabOrdersTv,filteredProductsTv;
    private EditText searchProductEt;
    private ImageButton logoutBtn,editProfileBtn,addProductBtn,filterProductBtn;
    private ImageView profileIv;
    private RelativeLayout productsRl,ordersRl;
    private RecyclerView productsRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTv=findViewById(R.id.nameTv);
        shopNameTv=findViewById(R.id.shopNameTv);
        emailTv=findViewById(R.id.emailTv);
        tabProductsTv=findViewById(R.id.tabProductsTv);
        tabOrdersTv=findViewById(R.id.tabOrdersTv);
        filteredProductsTv=findViewById(R.id.filteredProductsTv);
        searchProductEt=findViewById(R.id.searchProductEt);
        logoutBtn=findViewById(R.id.logoutBtn);
        editProfileBtn=findViewById(R.id.editProfileBtn);
        addProductBtn =findViewById(R.id.addProductBtn);
        filterProductBtn =findViewById(R.id.filterProductBtn);
        profileIv=findViewById(R.id.profileIv);

        productsRl=findViewById(R.id.productsRl);
        ordersRl=findViewById(R.id.ordersRl);
        productsRv=findViewById(R.id.productsRv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        showProductsUI();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            try{
                adapterProductSeller.getFilter().filter(s);

            }
            catch (Exception e){
                e.printStackTrace();
            }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make offline
                //sign out
                // go to login activity
                makeMeOffline();

            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit profile activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
            }
        });
        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit add product activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));

            }
        });
        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load products
                showProductsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load orders
                showOrdersUI();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String selected =Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if(selected.equals("All")){
                                    loadAllProducts();
                                }
                                else{
                                    loadFilteredProducts(selected);
                                }
                            }
                        }).show();


            }
        });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        productList.clear();
                        for(DataSnapshot ds:dataSnapshot.getChildren()){

                            String productCategory=""+ds.child("productCategory").getValue();
                            if(selected.equals(productCategory)){
                                ModelProduct modelProduct =ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        adapterProductSeller= new AdapterProductSeller(MainSellerActivity.this,productList);
                        productsRv.setAdapter(adapterProductSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadAllProducts() {

        productList = new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        productList.clear();
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelProduct modelProduct =ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        adapterProductSeller= new AdapterProductSeller(MainSellerActivity.this,productList);
                        productsRv.setAdapter(adapterProductSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showProductsUI() {
        //show products ui and hide ordersui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);
        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI() {
        //show orders ui and hide ordersui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }



    private void makeMeOffline() {
        //after logging in,make user online
        progressDialog.setMessage("Logging out");
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("online","false");
        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed updating
                Toast.makeText(MainSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        }
        else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()) {
                            //get data from db
                            String name = "" + ds.child("name").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String email = "" + ds.child("email").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();

                            //set data to ui
                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);

                            try{
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                            }
                            catch(Exception e){
                                profileIv.setImageResource(R.drawable.ic_store_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}