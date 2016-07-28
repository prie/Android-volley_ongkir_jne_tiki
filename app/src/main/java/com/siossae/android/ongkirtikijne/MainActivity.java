package com.siossae.android.ongkirtikijne;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.siossae.android.ongkirtikijne.adapter.OngkirAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView ongkirList;
    private RadioGroup rServisGroup;
    private RadioButton rServisButton;
    private AutoCompleteTextView actKotaAsal, actKotaTuju;
    private Spinner spnrBerat;
    private Button btnCekOngkir;
    private TextView resultHeader;
    OngkirAdapter ongkirAdapter;
    ArrayList<String> namaLayanan, tarifLayanan, estimasiTiba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ongkirList = (ListView) findViewById(R.id.ongkir_listview);

        rServisGroup = (RadioGroup) findViewById(R.id.radio_group_servis);
        actKotaAsal = (AutoCompleteTextView) findViewById(R.id.ac_kota_asal);
        actKotaTuju = (AutoCompleteTextView) findViewById(R.id.ac_kota_tuju);
        spnrBerat = (Spinner) findViewById(R.id.berats_spinner);
        resultHeader = (TextView) findViewById(R.id.result_header);

        btnCekOngkir = (Button) findViewById(R.id.btn_proses);

        namaLayanan = new ArrayList<String>();
        tarifLayanan = new ArrayList<String>();
        estimasiTiba = new ArrayList<String>();

        radioService();

        // Mengisi AutoCompleteTextView menggunakan ArrayAdapter
        ArrayAdapter<String> kotaAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, CITY);
        actKotaAsal.setAdapter(kotaAdapter);
        actKotaTuju.setAdapter(kotaAdapter);

        // Mengisi Spinner dengan item2 yang definisikan di strings.xml menggunakan ArrayAdapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.berats_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrBerat.setAdapter(adapter);

        ongkirAdapter = new OngkirAdapter(this, namaLayanan, tarifLayanan, estimasiTiba);
        ongkirList.setAdapter(ongkirAdapter);

        btnCekOngkir.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == btnCekOngkir && rServisButton != null) {

            namaLayanan.clear();
            tarifLayanan.clear();
            estimasiTiba.clear();

            String service = rServisButton.getText().toString().toLowerCase();
            String kota_asal = actKotaAsal.getText().toString().toLowerCase();
            String kota_tuju = actKotaTuju.getText().toString().toLowerCase();
            String berat_barang = String.valueOf(spnrBerat.getSelectedItem());

            try {
                getDataOngkir(service, kota_asal, kota_tuju, berat_barang);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Koneksi Error!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Please choose the Servis!", Toast.LENGTH_SHORT).show();
        }
    }

    public void radioService() {
        int radioServiceSelectedId = rServisGroup.getCheckedRadioButtonId();
        rServisButton = (RadioButton) findViewById(radioServiceSelectedId);

        rServisGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_jne:
                        rServisButton = (RadioButton) findViewById(R.id.radio_jne);
                        break;
                    case R.id.radio_tiki:
                        rServisButton = (RadioButton) findViewById(R.id.radio_tiki);
                        break;
                }
            }
        });
    }

    private static final String[] CITY = new String[] {
            "Surabaya", "Semarang", "Bandung", "Jakarta", "Yogyakarta"
    };

    public void getDataOngkir(String servis, String kotaAsal, String kotaTuju, String beratBarang) {

        String url = "http://ibacor.com/api/ongkir?service=" + servis + "&dari=" + kotaAsal + "&ke=" + kotaTuju + "&berat=" + beratBarang;

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //Log.e("DATA", response.toString());
                try {
                    JSONObject object = new JSONObject(response.toString());
                    String statusRender = object.getString("status");
                    //Log.e("DATA", errorMessage);

                    if (statusRender.equals("error")) {
                        ongkirAdapter.clear();
                        resultHeader.setText("");

                        String errorMessage = object.getString("message");
                        //Log.e("DATA", errorMessage);
                        Toast.makeText(MainActivity.this, "ERROR!!! " + errorMessage, Toast.LENGTH_SHORT).show();
                    } else if (statusRender.equals("success")){
                        JSONArray ongkir = object.getJSONArray("ongkos");

                        //for (int i = 0;i < ongkir.length();i++) {  ==> untuk render semua isi array
                        for (int i = 0;i < 3;i++) {               // ==> unutk render 3 isi array yg pertama
                            JSONObject arr_obj = ongkir.getJSONObject(i);
                            String layanan = arr_obj.getString("layanan");
                            String tarif = arr_obj.getString("tarif");
                            String estimasi = arr_obj.getString("etd");

                            namaLayanan.add(layanan);
                            tarifLayanan.add(convertCurr(tarif));
                            estimasiTiba.add(estimasi.replaceAll("Days", "hari"));
                        }
                        resultHeader.setText("Data ongkir");
                    }
                    ongkirAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("DATA", error.toString());
                //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Convert String "17,000" to "Rp 17.000,00"
    private String convertCurr(String nominal) {
        int money = Integer.parseInt(nominal.replaceAll(",", ""));

        DecimalFormat df = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setMonetaryDecimalSeparator(',');
        dfs.setGroupingSeparator('.');
        dfs.setCurrencySymbol("Rp ");
        df.setDecimalFormatSymbols(dfs);

        return df.format(money);
    }
}
