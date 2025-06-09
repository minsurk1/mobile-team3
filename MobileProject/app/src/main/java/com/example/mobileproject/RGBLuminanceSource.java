package com.example.mobileproject;

import com.google.zxing.LuminanceSource;

public class RGBLuminanceSource extends LuminanceSource {

    private final byte[] luminances;

    public RGBLuminanceSource(int width, int height, int[] pixels) {
        super(width, height);
        luminances = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;
// 밝기 계산 (Y값, Y = 0.299R + 0.587G + 0.114B 근사)
            luminances[i] = (byte) ((r + g + g + b) >> 2);
        }
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        System.arraycopy(luminances, y * width, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        return luminances;
    }
}