package net.shoppingcart.project;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatDialog {
//    List<String> locations;
    @SuppressLint("DefaultLocale")
    public PaymentActivity(@NonNull Context context, @NonNull Double value){
        super(context, resolveDialogTheme(context));

        setTitle("Payment");
        setContentView(R.layout.dialog_payment);

        findViewById(R.id.close).setOnClickListener(v -> dismiss());
        TextView amount = findViewById(R.id.textView9);
        amount.setText(String.format("%.2f", value));
    }

    private static int resolveDialogTheme(@NonNull Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.alertDialogTheme, outValue, true);
        return outValue.resourceId;
    }
}
