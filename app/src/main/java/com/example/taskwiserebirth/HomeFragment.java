package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    //TODO: replace homefragment with live2d fragment later
    ImageButton collapseButton;
    View bottomNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        collapseButton = view.findViewById(R.id.fullscreen_button);
        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView); // Assuming bottom navigation view is defined in the activity layout

        collapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomNavigationView.getVisibility() == View.VISIBLE) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }
}
