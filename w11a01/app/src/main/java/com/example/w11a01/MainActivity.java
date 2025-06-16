package com.example.w11a01;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

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
        String report = "";
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
        report += "전체 센서 수" + sensors.size() + "\n";
        int i = 0 ;
        for(Sensor s : sensors){
            report += " " + i++ + "name: " + s.getName() + "\nPower : "
                    + s.getPower() + "\nres: " + s.getResolution()
                    + "\nRange: " + s.getMaximumRange() + "\n\n";
        }
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(report);
    }
}