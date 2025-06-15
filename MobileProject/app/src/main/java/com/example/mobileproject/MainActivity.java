package com.example.mobileproject;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.RGBLuminanceSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity {

    private TextView resultText;
    private String scannedContent = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resultText = findViewById(R.id.resultText);
        Button uploadButton = findViewById(R.id.uploadButton);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            decodeQRCodeFromUri(imageUri);
                        }
                    } else {
                        Toast.makeText(this, "이미지 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        uploadButton.setOnClickListener(v -> openImagePicker());
        resultText.setOnClickListener(v -> {
            if (scannedContent != null && isValidUrl(scannedContent)) {
                String urlToOpen = prependSchemeIfMissing(scannedContent);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
                    fallback.addCategory(Intent.CATEGORY_BROWSABLE);
                    startActivity(Intent.createChooser(fallback, "브라우저 선택"));
                }
            } else {
                Toast.makeText(this, "유효한 URL이 아닙니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "QR 이미지 선택"));
    }

    private void decodeQRCodeFromUri(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            String decodedText = decodeQRCode(bitmap);
            if (decodedText != null) {
                scannedContent = decodedText;
                resultText.setText("스캔된 내용: " + scannedContent);
            } else {
                scannedContent = null;
                resultText.setText("QR 코드를 인식하지 못했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private String decodeQRCode(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        MultiFormatReader reader = new MultiFormatReader();
        try {
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Result result = reader.decode(binaryBitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        }
    }

    private void loadDownloadImagesAndScan() {
        Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE
        };

        Cursor cursor = getContentResolver().query(
                collection,
                projection,
                null,
                null,
                null
        );

        if (cursor == null) {
            Toast.makeText(this, "다운로드 폴더에 접근할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Uri> imageUris = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));

            if (mimeType != null && mimeType.startsWith("image/")) {
                Uri contentUri = ContentUris.withAppendedId(collection, id);
                imageUris.add(contentUri);
            }
        }
        cursor.close();

        if (imageUris.isEmpty()) {
            Toast.makeText(this, "다운로드 폴더에 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        scanImageListForQRCode(imageUris, 0);
    }

    private void scanImageListForQRCode(List<Uri> uris, int index) {
        if (index >= uris.size()) {
            Toast.makeText(this, "다운로드된 이미지에서 QR 코드를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
            scannedContent = null;
            resultText.setText("QR 코드를 인식하지 못했습니다.");
            return;
        }

        Uri uri = uris.get(index);
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            String decodedText = decodeQRCode(bitmap);
            if (decodedText != null) {
                scannedContent = decodedText;
                resultText.setText("다운로드 폴더 QR 스캔 결과: " + scannedContent);
                Toast.makeText(this, "QR 코드를 인식했습니다!", Toast.LENGTH_SHORT).show();
            } else {
                scanImageListForQRCode(uris, index + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
    }

    private String prependSchemeIfMissing(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return "https://" + url;
    }
}
