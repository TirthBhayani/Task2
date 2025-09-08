package com.example.task21;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class add_category extends AppCompatActivity {

    private static final int PICK_IMAGES = 100;
    private List<Uri> imageUris = new ArrayList<>();
    private EditText edtCategoryName;
    private Button btnSelectImages, btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        edtCategoryName = findViewById(R.id.edtCategoryName);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnUpload = findViewById(R.id.btnUpload);

        btnSelectImages.setOnClickListener(v -> selectImagesFromGallery());
        btnUpload.setOnClickListener(v -> uploadCategory());
    }

    private void selectImagesFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }
            Toast.makeText(this, imageUris.size() + " Images Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadCategory() {
        String categoryName = edtCategoryName.getText().toString().trim();
        if (categoryName.isEmpty() || imageUris.isEmpty()) {
            Toast.makeText(this, "Enter name & select images", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> newImagesBase64 = new ArrayList<>();
        for (Uri uri : imageUris) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] bytes = baos.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                newImagesBase64.add(base64);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("categories");

        // Check if category exists
        categoriesRef.orderByChild("name").equalTo(categoryName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Category exists → append images
                            for (DataSnapshot catSnap : snapshot.getChildren()) {
                                List<String> existingImages = catSnap.child("imagesBase64")
                                        .getValue(new GenericTypeIndicator<List<String>>() {});
                                if (existingImages == null) existingImages = new ArrayList<>();
                                existingImages.addAll(newImagesBase64);

                                categoriesRef.child(catSnap.getKey())
                                        .child("imagesBase64")
                                        .setValue(existingImages)
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(add_category.this, "Images added to existing category", Toast.LENGTH_SHORT).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(add_category.this, "Failed to add images", Toast.LENGTH_SHORT).show()
                                        );
                            }
                        } else {
                            // New category
                            String key = categoriesRef.push().getKey();
                            Category category = new Category(categoryName, newImagesBase64);
                            categoriesRef.child(key).setValue(category)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(add_category.this, "Category uploaded", Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(add_category.this, "Upload failed", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(add_category.this, "Firebase Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
