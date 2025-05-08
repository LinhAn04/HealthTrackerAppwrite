package hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.R;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.fragment.CloudFragment;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.fragment.EventFragment;
import hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud.fragment.HealthFragment;

public class OverviewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overview_activity);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        // Gán trang mặc định (HealthFragment) khi ứng dụng vừa mở
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HealthFragment())
                    .commit();
        }
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int id = item.getItemId();
                if (id == R.id.nav_health) {
                    selectedFragment = new HealthFragment();
                }
                else if (id == R.id.nav_event) {
                    selectedFragment = new EventFragment();
                }
                else if (id == R.id.nav_file) {
                    selectedFragment = new CloudFragment();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            };
}
