package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;

public class EventFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_fragment, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        FragmentManager fragmentManager = getChildFragmentManager();

        // Add Tabs
        tabLayout.addTab(tabLayout.newTab().setText("Cá nhân"));
        tabLayout.addTab(tabLayout.newTab().setText("Tổ chức"));

        // Mặc định hiện tab Cá nhân
        fragmentManager.beginTransaction()
                .replace(R.id.tab_container, new PersonalEventFragment())
                .commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment;
                if (tab.getPosition() == 0)
                    selectedFragment = new PersonalEventFragment();
                else
                    selectedFragment = new OrganizationEventFragment();

                fragmentManager.beginTransaction()
                        .replace(R.id.tab_container, selectedFragment)
                        .commit();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }
}
