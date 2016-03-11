package com.franciscogiraldo.fcog.grability.ui.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.franciscogiraldo.fcog.grability.R;
import com.franciscogiraldo.fcog.grability.ui.activity.AppActivity;
import com.franciscogiraldo.fcog.grability.utils.Constantes;
import com.franciscogiraldo.fcog.grability.utils.VolleySingleton;
import com.franciscogiraldo.fcog.grability.web.App;

/**
 * Created by fcog on 9/11/15.
 */
public class AdaptadorApps extends RecyclerView.Adapter<AdaptadorApps.AppViewHolder> {
    private Cursor cursor;
    private Context context;

    public ContentResolver contentResolver;

    public int view_selected;

    public static class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Campos respectivos de un item
        public NetworkImageView imagen;
        public TextView titulo;
        public TextView precio;
        public ImageView imagen_favorita;
        public ClickListener clickListener;


        public AppViewHolder(View itemView) {
            super(itemView);

            imagen = (NetworkImageView) itemView.findViewById(R.id.imagen);
            titulo = (TextView) itemView.findViewById(R.id.text_titulo);
            precio = (TextView) itemView.findViewById(R.id.text_precio);
            imagen_favorita = (ImageView) itemView.findViewById(R.id.imagen_favorito);

            itemView.setOnClickListener(this);
        }

        public interface ClickListener {
            /**
             * Called when the view is clicked.
             *
             * @param v view that is clicked
             * @param position of the clicked item
             * @param isLongClick true if long click, false otherwise
             */
            public void onClick(View v, int position, boolean isLongClick);

        }

        /* Setter for listener. */
        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {

            // If not long clicked, pass last variable as false.
            clickListener.onClick(v, getPosition(), false);
        }

    }

    public AdaptadorApps(Context context, int view_selected) {
        this.context= context;
        this.view_selected = view_selected;
        contentResolver = context.getContentResolver();
    }

    @Override
    public int getItemCount() {
        if (cursor!=null)
            return cursor.getCount();
        return 0;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v;
        if (view_selected == Constantes.LIST) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_layout, viewGroup, false);
        }
        else{
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_grid_layout, viewGroup, false);
        }
        return new AppViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AppViewHolder viewHolder, int i) {
        cursor.moveToPosition(i);


        int promocion_id = cursor.getInt(Constantes.COLUMNA_APP_ID);
        String titulo = cursor.getString(Constantes.COLUMNA_TITULO);
        String imagen = cursor.getString(Constantes.COLUMNA_IMAGEN);
        int precio = cursor.getInt(Constantes.COLUMNA_PRECIO);
        int favorita = cursor.getInt(Constantes.COLUMNA_FAVORITA);

        viewHolder.titulo.setText(titulo);

        if (precio == 0){
            viewHolder.precio.setText(R.string.precio_gratis);
        }
        else{
            viewHolder.precio.setText(String.valueOf(precio));
        }

        if (favorita == 1) {
            viewHolder.imagen_favorita.setImageResource(R.drawable.ic_active_addfavoritos);
        }
        else{
            viewHolder.imagen_favorita.setImageResource(R.drawable.ic_addfavoritos);
        }

        viewHolder.imagen_favorita.setTag(promocion_id);

        ImageLoader imageLoader = VolleySingleton.getInstance(context).getImageLoader();

        //slide_image.setDefaultImageResId(R.drawable.default_picture);
        //viewHolder.imagen.setErrorImageResId(R.drawable.error);

        viewHolder.imagen.setImageUrl(imagen, imageLoader);
        viewHolder.imagen.setScaleType(ImageView.ScaleType.FIT_XY);


        viewHolder.imagen_favorita.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                int app_id = Integer.parseInt(v.getTag().toString());

                App app = new App(contentResolver, app_id);

                app.AddFavorito(contentResolver);
            }
        });

        viewHolder.setClickListener(new AppViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position, boolean isLongClick) {
                if (!isLongClick){
                    cursor.moveToPosition(position);

                    int app_id = Integer.parseInt(cursor.getString(Constantes.COLUMNA_APP_ID));

                    Intent intent = new Intent(context, AppActivity.class);
                    intent.putExtra("app_id", app_id);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Activity activity = (Activity) context;
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                }
            }
        });

    }

    public void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return cursor;
    }
}