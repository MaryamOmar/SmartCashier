package net.shoppingcart.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.shoppingcart.project.adapters.CartItemList;
import net.shoppingcart.project.models.CartItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private static List<CartItem> cartItems;
    private String uid;
    private FirebaseAuth auth;
    private ListView lv;
    private Button btnCheckout;
    private TextView total;
    private Double value=0.00;

    public static void deleteCartItem(CartItem cartItem){
        cartItems.remove(cartItem);
        Log.d("Updated CartItems", Arrays.toString(cartItems.toArray()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            uid = user.getUid();
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        setContentView(R.layout.activity_cart);

        getSupportActionBar().setTitle("My cart");
        cartItems = new ArrayList<>();

        btnCheckout = findViewById(R.id.btncheckout);
        lv = findViewById(R.id.listview);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CartItem item = cartItems.get(position);
                //Toast.makeText(getApplicationContext(), ""+position, Toast.LENGTH_LONG).show();
                DeleteActivity deleteActivity = new DeleteActivity(CartActivity.this, item);
                deleteActivity.show();
            }
        });
        total = findViewById(R.id.textView8);

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePrice();

                PaymentActivity dialog = new PaymentActivity(CartActivity.this, value, uid);
                dialog.show();
            }
        });

        //tempItems();
    }

    void tempItems(){
        for(int i=0; i<5; i++){
            CartItem item = new CartItem(""+i, "name:"+i,1.00, i,"");
            cartItems.add(item);
        }

        CartItemList adapter = new CartItemList(CartActivity.this, cartItems);
        lv.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //fetchCartItems();
        //tempItems();
        calculatePrice();
    }

    @SuppressLint("DefaultLocale")
    void calculatePrice(){
        value = 0.00;
        for(CartItem item: cartItems){
            Log.d("price", String.format("%f %f %d ",item.getItemPrice()*item.getItemQty(), item.getItemPrice(), item.getItemQty()));
            value += item.getItemPrice()*item.getItemQty();
        }

        total.setText(String.format("%.2f", value));

    }


    void fetchCartItems(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("cart_list").child(uid);
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartItems.clear();
                Log.d("TK->TEST", dataSnapshot.toString());
                Toast.makeText(getApplicationContext(), dataSnapshot.toString(), Toast.LENGTH_LONG).show();

                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    CartItem item = dataSnapshot1.getValue(CartItem.class);
                    cartItems.add(item);
                }

                CartItemList adapter = new CartItemList(CartActivity.this, cartItems);
                lv.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
