package com.example.map_kms;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    LocationListener locationListener;
    TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        status = (TextView) findViewById(R.id.status);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
// 위도, 경도값을 추출, 여기에서 활용할 코드 작성
                double lat = location.getLatitude(); // 위도
                double lng = location.getLongitude(); // 경도
                Log.i("AAA","위도 : "+ lat);
                Log.i("AAA","경도 : "+ lng);
                Log.i("AAA"," ");
                status.setText("위도; " + lat + "\n경도:" + lng + "\n고도:" + location.getAltitude());
            }};
        if(ActivityCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, -1, locationListener);
    }
    @Override // 권한 관련. GPS 처리하는 코드(재사용 코드)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100) { // 허용하지 않았으면, 다시 허용하라는 알러트 띄운다.
            if(ActivityCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED){
                finish();
                return;
            }
// 허용했으면, GPS 정보 가져오는 코드 넣는다.
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 3000, -1, locationListener);
        }
    }
}