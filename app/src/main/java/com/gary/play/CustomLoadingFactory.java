package com.gary.play;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dyhdyh.widget.loading.factory.LoadingFactory;

public class CustomLoadingFactory implements LoadingFactory {
    @Override
    public View onCreateView(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_process_dialog_color, parent, false);
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_process_dialog_color, parent, false);
        // View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_process_dialog_icon, parent, false);

        return view;
    }
}
