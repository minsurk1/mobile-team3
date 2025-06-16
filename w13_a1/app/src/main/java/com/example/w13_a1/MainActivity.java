package com.example.w13_a1;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity extends AppCompatActivity {
    ArrayList<String> songList = new ArrayList<>();
    ListView listView;
    MediaPlayer mp = null;
    String curSong = null;
    String songPath;
    int curIndex = 0;
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
        // Android 11+ : MANAGE_EXTERNAL_STORAGE 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
            Toast.makeText(this, "파일 접근 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
            return;
        }
        // 권한 요청 (Android 10 이하용)
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        songPath = Environment.getExternalStorageDirectory().getPath() + "/";
        FileFilter filter = file -> !file.isDirectory() && file.getName().endsWith(".mp3");
        File[] files = new File(songPath).listFiles(filter);
        if (files != null) {
            for (File file : files) {
                songList.add(file.getName());
            }
        }
        listView = findViewById(R.id.listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, songList);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
        if (!songList.isEmpty()) {
            listView.setItemChecked(0, true);
            curSong = songList.get(0);
            curIndex = 0;
        }
        listView.setOnItemClickListener((AdapterView<?> arg0, View arg1, int position, long id) -> {
            curIndex = position;
            curSong = songList.get(position);
        });
        // 버튼 설정
        Button btnPlay = findViewById(R.id.button3);
        Button btnStop = findViewById(R.id.button);
        Button btnPrev = findViewById(R.id.button4);
        Button btnNext = findViewById(R.id.button2);
        btnPlay.setOnClickListener(this::play);
        btnStop.setOnClickListener(this::stop);
        btnPrev.setOnClickListener(this::prev);
        btnNext.setOnClickListener(this::next);
    }
    public void play(View v) {
        if (curSong == null) {
            Toast.makeText(this, "노래를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (mp != null) {
                mp.release();
            }
            mp = new MediaPlayer();
            mp.setDataSource(songPath + curSong);
            mp.prepare();
            mp.start();
            Toast.makeText(this, "재생: " + curSong, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("MP3", "재생 실패", e);
        }
    }
    public void stop(View v) {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
            Toast.makeText(this, "정지", Toast.LENGTH_SHORT).show();
        }
    }
    public void prev(View v) {
        if (songList.isEmpty()) return;
        curIndex = (curIndex - 1 + songList.size()) % songList.size();
        curSong = songList.get(curIndex);
        listView.setItemChecked(curIndex, true);
        play(v);
    }
    public void next(View v) {
        if (songList.isEmpty()) return;
        curIndex = (curIndex + 1) % songList.size();
        curSong = songList.get(curIndex);
        listView.setItemChecked(curIndex, true);
        play(v);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.release();
        }
    }
}