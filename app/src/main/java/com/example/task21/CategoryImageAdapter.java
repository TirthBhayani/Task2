package com.example.task21;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class CategoryImageAdapter extends RecyclerView.Adapter<CategoryImageAdapter.ImageViewHolder> {

    private List<String> imagesBase64;

    public CategoryImageAdapter(List<String> imagesBase64) {
        this.imagesBase64 = imagesBase64;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String base64 = imagesBase64.get(position);
        try {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageResource(android.R.color.darker_gray);
        }
        holder.imageView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), FullScreenImageActivity.class);
            intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_BASE64, base64);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imagesBase64 != null ? imagesBase64.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
