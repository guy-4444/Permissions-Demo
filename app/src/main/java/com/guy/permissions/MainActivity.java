package com.guy.permissions;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button main_BTN_ask;
    private Button main_BTN_readContacts;
    private TextView main_LBL_contacts;
    
    final String MY_PERMISSION = Manifest.permission.READ_CONTACTS;
    final int READ_CONTACTS_REQUEST_CODE = 3456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_BTN_ask = findViewById(R.id.main_BTN_ask);
        main_BTN_readContacts = findViewById(R.id.main_BTN_readContacts);
        main_LBL_contacts = findViewById(R.id.main_LBL_contacts);

        main_BTN_ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askContacts();
            }
        });

        main_BTN_readContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readContacts();
            }
        });

        main_LBL_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDataText();
            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission granted to read your External storage", Toast.LENGTH_SHORT).show();
                    readContacts();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void refreshDataText() {
        boolean b1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        boolean b2 = ContextCompat.checkSelfPermission(this, MY_PERMISSION) == PackageManager.PERMISSION_GRANTED;
        boolean b3 = !ActivityCompat.shouldShowRequestPermissionRationale(this, MY_PERMISSION);

        main_LBL_contacts.setText("Above Android 6.0:  " + b1 + "\nPermission Granted:  " + b2 + "\ndon't ask again selected:  " + b3);
    }

    private void readContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, MY_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, MY_PERMISSION)) {
                    askContacts();
                } else {
                    new AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Permission missing")
                            .setMessage("Manually grant permission")
                            .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
                return;
            }
        }


        // TODO: 16/03/2020 check contact permission
        String data = "";
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        data += "\n" + name + ": " + phoneNo;
                        Log.i("ptttC", "Name: " + name);
                        Log.i("ptttC", "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }

        main_LBL_contacts.setText(data);
    }

    private void askContacts() {
        Log.d("pttt", "askContacts");
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{MY_PERMISSION}, READ_CONTACTS_REQUEST_CODE);
    }
}
