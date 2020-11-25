package com.example.bookbank.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.bookbank.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanBarCodeReturnBookActivity extends AppCompatActivity implements View.OnClickListener {

    private String ownerID;
    private String borrowerID;
    private Button scanButton;
    private String globalBookID;
    private String globalISBN;
    private boolean borrowerScan;
    private boolean ownerScan;
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode_return_book);

        /** Get book id of the book that clicked in the list view of BorrowerBooksActivity */
        globalBookID = getIntent().getStringExtra("BOOK_ID");
        globalISBN = getIntent().getStringExtra("ISBN_OG").replace("ISBN: ", "");
        borrowerScan = getIntent().getBooleanExtra("BORROWER_SCAN", false);
        ownerScan = getIntent().getBooleanExtra("OWNER_SCAN", false);
        ownerID = getIntent().getStringExtra("OWNER_ID");
        borrowerID = getIntent().getStringExtra("BORROWER_ID");

        scanButton = findViewById(R.id.ScanButton);
        scanButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        scanCode();
    }

    private void scanCode(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setCaptureActivity(CaptureBarCodeActivity.class);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt("Scanning Code");
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String ScanDescription;
        if (result != null){
            if (result.getContents() != null){
                String isbnCode = result.getContents().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                /*--Change Status + Change BorrowerID--*/
                boolean updateSuccess = updateBook(isbnCode);
                if (updateSuccess == true){
                    builder.setTitle("Scanning Result" + ": Success!");
                    builder.setMessage(result.getContents());
                }
                else {
                    builder.setTitle("Scanning Result" + ": Fail!");
                    ScanDescription = "\n Incorrect ISBN / Both borrower and owner must scan";
                    builder.setMessage(result.getContents() + System.lineSeparator() + ScanDescription);
                }
                builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        scanCode();
                    }
                }).setNegativeButton("finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else {
                Toast.makeText(this, "No Results", Toast.LENGTH_LONG).show();
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private boolean updateBook(String isbnCode) {
        db = FirebaseFirestore.getInstance();
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("UID DEBUG", currentUser);

        if (isbnCode.equals(globalISBN)){

            if (currentUser.equals(ownerID)){
                ownerScan = true;
            }
            if (currentUser.equals(borrowerID)){
                borrowerScan = true;
            }
            db.collection("Book").document(globalBookID).update("borrowerScanReturn", borrowerScan);

            /** Update the fields for the document in firestore */
            if (ownerScan == true && borrowerScan == true){
                db.collection("Book").document(globalBookID).update("status", "Available");
                db.collection("Book").document(globalBookID).update("borrowerId", "");
                //finish();
                borrowerScan = false;
                ownerScan = false;
                return true;
            }
        }
        return false;
    }
}