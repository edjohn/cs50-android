package edu.harvard.cs50.fiftygram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;

import java.io.FileDescriptor;
import java.io.IOException;

import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import jp.wasabeef.glide.transformations.gpu.ContrastFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ToonFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback
{
    private ImageView imageView;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        imageView = findViewById(R.id.image_view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void choosePhoto(View view)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null)
        {
            try
            {
                Uri uri = data.getData();
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                imageView.setImageBitmap(image);
            }
            catch (IOException e)
            {
                Log.e("cs50", "Image not found", e);
            }

        }
    }

    public void apply(Transformation<Bitmap> filter)
    {
        Glide
                .with(this)
                .load(image)
                .apply(RequestOptions.bitmapTransform(filter))
                .into(imageView);
    }

    public void applySepia(View view)
    {
        apply(new SepiaFilterTransformation());
    }

    public void applyToon(View view)
    {
        apply(new ToonFilterTransformation());
    }

    public void applySketch(View view)
    {
        apply(new SketchFilterTransformation());
    }

    public void applyGrayscale(View view)
    {
        apply(new GrayscaleTransformation());
    }

    public void applyVignette(View view)
    {
        apply(new VignetteFilterTransformation());
    }

    public void applyContrast(View view)
    {
        apply(new ContrastFilterTransformation());
    }

    public void savePhoto(View view)
    {
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmapImage = draw.getBitmap();
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmapImage, "image", "desc");
    }
}