package com.example.cardioxyas_v10.graphique;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.cardioxyas_v10.R;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class GraphiqueFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_graphique, container, false);

        return root;
    }
}
