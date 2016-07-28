package com.siossae.android.ongkirtikijne.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.siossae.android.ongkirtikijne.R;

import java.util.ArrayList;

/**
 * Created by pri on 22/07/16.
 */
public class OngkirAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<String> layanan, ongkir, estimasi;

    public OngkirAdapter(Context context, ArrayList<String> layanan, ArrayList<String> ongkir, ArrayList<String> estimasi) {
        super(context, R.layout.ongkir_list, layanan);
        this.context = context;
        this.layanan = layanan;
        this.ongkir = ongkir;
        this.estimasi = estimasi;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        view = null;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.ongkir_list, null);
        }

        TextView txtLayanan = (TextView) view.findViewById(R.id.layanan);
        TextView txtTarif = (TextView) view.findViewById(R.id.tarif);
        TextView txtEstimasi = (TextView) view.findViewById(R.id.estimasi);

        txtLayanan.setText(layanan.get(position));
        txtTarif.setText(ongkir.get(position));
        txtEstimasi.setText(estimasi.get(position));

        return (view);
    }
}
