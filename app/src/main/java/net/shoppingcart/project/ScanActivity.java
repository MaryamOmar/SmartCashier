package net.shoppingcart.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.budiyev.android.codescanner.CodeScanner;

import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import net.shoppingcart.project.models.CartItem;
import net.shoppingcart.project.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScanActivity extends AppCompatActivity {
    private static final int RC_PERMISSION = 10;
    private CodeScanner mCodeScanner ;
    private boolean mPermissionGranted;
    private List<Product> productsList;
    private DatabaseReference databaseReference;

    private String uid;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        getSupportActionBar().setTitle("Scan products");
        productsList = new ArrayList<>();

        fetchProducts();
//        firebaseAuth = FirebaseAuth.getInstance();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null) {
            uid = user.getUid();
        }else {
            startActivity(new Intent(ScanActivity.this, LoginActivity.class));
            finish();
        }

        mCodeScanner = new CodeScanner(getApplicationContext(), findViewById(R.id.scanner));

        mCodeScanner.setDecodeCallback(result -> ScanActivity.this.runOnUiThread(() -> {

            Product resultProduct = lookupProduct(result.getText());

            if(resultProduct == null){
                ScanResultsActivity dialog = new ScanResultsActivity(ScanActivity.this, null, result, false);
                dialog.setOnDismissListener(d -> mCodeScanner.startPreview());
                dialog.show();
            }else {
                ScanResultsActivity dialog = new ScanResultsActivity(ScanActivity.this, resultProduct, result, true);
                dialog.setOnDismissListener(d -> mCodeScanner.startPreview());
                dialog.show();
            }


        }));

        mCodeScanner.setErrorCallback(error -> runOnUiThread(
                () -> Toast.makeText(getApplicationContext(), getString(R.string.scanner_error, error), Toast.LENGTH_LONG).show()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission( Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = false;
                requestPermissions(new String[] {Manifest.permission.CAMERA}, RC_PERMISSION);
            } else {
                mPermissionGranted = true;
            }
        } else {
            mPermissionGranted = true;
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = true;
                mCodeScanner.startPreview();
            } else {
                mPermissionGranted = false;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //firebaseAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPermissionGranted) {
            mCodeScanner.startPreview();
        }
        //firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
       // firebaseAuth.removeAuthStateListener(mAuthListener);
    }

    private void fetchProducts(){
        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        databaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            productsList.clear();
            Log.d("TK->PRODUCTS", dataSnapshot.toString());

            for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                Product product = dataSnapshot1.getValue(Product.class);
                if(Objects.requireNonNull(product).getAvailable()>0){
                    productsList.add(product);
                }

                //Toast.makeText(getApplicationContext(), dataSnapshot1.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    });

    }

    private Product lookupProduct(String result) {
        for (final Product product : productsList) {
            // Access properties of person, usage of getter methods would be good
            if (product.getId().equals(result)) {
                // Found matching person
                return product;
            }
        }

        // Traversed whole list but did not find a matching person
        return null;
    }



}