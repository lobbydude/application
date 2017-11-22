package com.drnds.titlelogy.activity.vendor.orderqueueactivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.drnds.titlelogy.R;
import com.drnds.titlelogy.activity.client.gridactivity.EditGridViewActivity;
import com.drnds.titlelogy.activity.client.gridactivity.GridUploadActivity;
import com.drnds.titlelogy.util.AppController;
import com.drnds.titlelogy.util.Config;
import com.drnds.titlelogy.util.Logger;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.drnds.titlelogy.adapter.client.RecyclergridviewAdapter.GRID;
import static com.drnds.titlelogy.adapter.vendor.VendorRecyclerOrderQueueAdapter.VENDORORDER;

public class VendorUploadActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText inputDescript;
    Button upload, submit;
    Spinner spinner;
    TextInputLayout descriptionlayout;
    private ArrayList<String> document;
    private ArrayList<String> documentIds;
    private ProgressDialog pDialog;

    String Vendor_User_Id,Order_Id,Vendor_Id,subId,docId,documents,orderNum,Descript,clientId;
    Uri audioFileUri;
    Boolean flag = false;
    TextView textView;
    SharedPreferences sp,pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_upload);
        toolbar = (Toolbar) findViewById(R.id.toolbar_vendorupload);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Upload Document");

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        if (toolbar != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinner=(Spinner)findViewById(R.id.document_spinner);
        sp = getApplicationContext().getSharedPreferences(
                "VendorLoginActivity", 0);
        pref = getApplicationContext().getSharedPreferences(
                VENDORORDER, 0);
        Vendor_User_Id = sp.getString("Vendor_User_Id","");

        Order_Id = pref.getString("Order_Id","");
        Vendor_Id=sp.getString("Vendor_Id","");

        Logger.getInstance().Log("oid"+Order_Id);
        subId=pref.getString("Sub_Client_Id","");
        orderNum=pref.getString("Order_Number","");
        clientId=pref.getString("Clinet_Id","");
        Logger.getInstance().Log("onum"+orderNum);
        pDialog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
        pDialog.setCancelable(false);
        document = new ArrayList<String>();
        documentIds = new ArrayList<>();
        inputDescript=(EditText)findViewById(R.id.input_discription_order_vendor);
        descriptionlayout=(TextInputLayout)findViewById(R.id.input_layout_description_vendor_order);

        Logger.getInstance().Log("descript"+Descript);
        checkInternetConnection();

        upload = (Button) findViewById(R.id.button_vendorupload);
        textView=(TextView)findViewById(R.id.uploadtext);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                return;
            }
        }

        UploadDocument();
        getDocument();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                docId = documentIds.get(position);
                documents = document.get(position);

                //String State_Name = states.get(position);





            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }


        });
    }








    private void getDocument(){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, Config.DOCUMENT_URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {

                    JSONArray jsonArray = response.getJSONArray("Client_master");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject details = jsonArray.getJSONObject(i);
                        document.add(details.getString("Document_Type"));
                        documentIds.add(details.getString("Document_Type_Id"));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                spinner.setAdapter(new ArrayAdapter<String>(VendorUploadActivity.this, android.R.layout.simple_spinner_dropdown_item, document));

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });


        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }
    private boolean validateDescription() {
        if (inputDescript.getText().toString().trim().isEmpty()) {
            descriptionlayout.setError(getString(R.string.err_msg_description));
            requestFocus(inputDescript);
            return false;
        } else {
            descriptionlayout.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
            TastyToast.makeText( this,"Check Internet Connection",TastyToast.LENGTH_SHORT, TastyToast.INFO);
            return false;
        }
        return false;
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    public void UploadDocument(){
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                checkInternetConnection();   // checking internet connection
                if (!validateDescription())
                {

                    return;
                }

                else{
                new MaterialFilePicker()
                        .withActivity(VendorUploadActivity.this)
                        .withRequestCode(10)
                        .start();

            }}
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            UploadDocument();
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
        }
    }

    ProgressDialog progress;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        if(requestCode == 10 && resultCode == RESULT_OK){
            Descript = inputDescript.getText().toString().trim();
            progress = new ProgressDialog(VendorUploadActivity.this);
            progress.setTitle("Uploading");
            progress.setMessage("Please wait...");
            progress.show();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    File f  = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
                    String content_type  = getMimeType(f.getPath());

                    String file_path = f.getAbsolutePath();
                    OkHttpClient client = new OkHttpClient();
                    RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                    RequestBody request_body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type",content_type)
                            . addFormDataPart("Order_Id",Order_Id)
                            . addFormDataPart("Document_From","2")
                            . addFormDataPart("Sub_Client_Id",subId)
                            . addFormDataPart("Document_Type_Id",docId)
                            . addFormDataPart("User_Id",Vendor_User_Id)
                            . addFormDataPart("Order_Number",orderNum)
                            . addFormDataPart("Description",Descript)


                            . addFormDataPart("Client_Id",clientId)

                            .addFormDataPart("uploaded_file",file_path.substring(file_path.lastIndexOf("/")+1), file_body)

                            .build();



                    System.out.println("erqh"+request_body);

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url("https://titlelogy.com/Final_Api/api/DocumentUpload/Uplodfile")
                            .post(request_body)
                            .build();
                    System.out.println(content_type+",,"+file_path+",,"+client+",,"+file_body+",,"+request_body+",,"+request);
                    try {
                        okhttp3.Response response = client.newCall(request).execute();
                        // System.out.println("response"+response.body().string());



                        if(!response.isSuccessful()){

                            progress.dismiss();
                            throw new IOException("Error : "+response);
                        }else{
                            Intent intent=new Intent(VendorUploadActivity.this,EditOrderVendorActivity.class);
                            startActivity(intent);
                            finish();
                            flag = true;
                            progress.dismiss();
                        }





                    } catch (IOException e) {
                        progress.dismiss();
                        e.printStackTrace();
                    }


                }
            });

            t.start();


            if (flag){
                Toast.makeText(getApplicationContext(),"some thing went wrong", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"Uploaded successfull", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

}
