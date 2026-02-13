package com.example.dresscatalog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dresscatalog.model.Dress;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DressAdapter extends RecyclerView.Adapter<DressAdapter.DressViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Dress dress);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Dress dress);
    }

    private final List<Dress> dresses = new ArrayList<>();
    private Set<Integer> favoriteIds;

    private final OnItemClickListener itemClickListener;
    private final OnFavoriteClickListener favoriteClickListener;

    public DressAdapter(OnItemClickListener itemClickListener,
                        OnFavoriteClickListener favoriteClickListener) {
        this.itemClickListener = itemClickListener;
        this.favoriteClickListener = favoriteClickListener;
    }


    public void submitList(List<Dress> list) {
        dresses.clear();
        if (list != null) dresses.addAll(list);
        notifyDataSetChanged();
    }

    public void setFavoriteIds(Set<Integer> ids) {
        this.favoriteIds = ids;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public DressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dress, parent, false);
        return new DressViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DressViewHolder h, int position) {
        Dress d = dresses.get(position);


        h.tvTitle.setText(safe(d.title));
        h.tvSku.setText("Артикул: " + safe(d.sku));


        String color = safe(d.color);
        String style = join(d.style);

        String meta;
        if (!color.isEmpty() && !style.isEmpty()) meta = color + " • " + style;
        else if (!color.isEmpty()) meta = color;
        else meta = style;

        h.tvMeta.setText(meta);


        h.tvPrice.setText(MoneyUtils.formatSom(d.priceSom));


        String previewUrl = pickPreviewUrl(d);
        loadDressImage(h.img, previewUrl);


        boolean isFav = favoriteIds != null && favoriteIds.contains(d.id);
        h.btnFav.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);


        h.btnFav.setOnClickListener(v -> {
            if (favoriteClickListener != null) favoriteClickListener.onFavoriteClick(d);
        });

        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(d);
        });
    }

    @Override
    public int getItemCount() {
        return dresses.size();
    }


    static class DressViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView tvTitle, tvSku, tvMeta, tvPrice;
        ImageButton btnFav;

        public DressViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSku = itemView.findViewById(R.id.tvSku);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnFav = itemView.findViewById(R.id.btnFav);
        }
    }


    private static String pickPreviewUrl(Dress d) {
        if (d == null) return "";

        if (d.imageUrls != null && !d.imageUrls.isEmpty()) {
            String u0 = safe(d.imageUrls.get(0));
            if (isHttp(u0)) return u0;
        }

        String one = safe(d.imageUrl);
        if (isHttp(one)) return one;

        return "";
    }


    private void loadDressImage(ImageView iv, String imageUrl) {
        String url = safe(imageUrl);

        if (isHttp(url)) {
            Glide.with(iv.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .into(iv);
        } else {
            iv.setImageResource(R.drawable.ic_image_placeholder);
        }
    }


    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isHttp(String s) {
        return s.startsWith("http://") || s.startsWith("https://");
    }

    private static String join(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (s == null) continue;
            s = s.trim();
            if (s.isEmpty()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(s);
        }
        return sb.toString();
    }
}
