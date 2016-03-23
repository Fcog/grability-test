package com.franciscogiraldo.fcog.grability.ui.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.franciscogiraldo.fcog.grability.R;
import com.franciscogiraldo.fcog.grability.db.App;
import com.franciscogiraldo.fcog.grability.db.AppContentProvider;
import com.franciscogiraldo.fcog.grability.db.AppDao;
import com.franciscogiraldo.fcog.grability.db.DaoMaster;
import com.franciscogiraldo.fcog.grability.db.DaoSession;
import com.franciscogiraldo.fcog.grability.utils.VolleySingleton;
import com.franciscogiraldo.fcog.grability.web.App2;

/**
 * Created by fcog on 9/15/15.
 */
public class AppActivity extends AppCompatActivity {

    App app;
    int app_id;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private AppDao appDao;

    private ShareActionProvider mShareActionProvider;

    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        Intent intent = getIntent();

        app_id = intent.getIntExtra("app_id", 0);

        setContentView(R.layout.activity_app);

        setToolbar();

        NetworkImageView imagen = (NetworkImageView) findViewById(R.id.imagen);
        TextView text_titulo = (TextView) findViewById(R.id.text_titulo);
        TextView text_descripcion = (TextView) findViewById(R.id.text_descripcion);
        TextView text_precio = (TextView) findViewById(R.id.text_precio);
        TextView text_enlace = (TextView) findViewById(R.id.text_enlace);

        ImageLoader mImageLoader = VolleySingleton.getInstance(getBaseContext()).getImageLoader();

        //cargar informacion de la app
        app = new App(getContentResolver(), app_id);

        imagen.setImageUrl(app.imagen, mImageLoader);

        text_titulo.setText(app.titulo);
        text_descripcion.setText(app.descripcion);

        String linkText = "<a href='" + app.enlace + "'>Visita la pagina web de la app.</a>";
        text_enlace.setText(Html.fromHtml(linkText));
        text_enlace.setMovementMethod(LinkMovementMethod.getInstance());

        if (app.precio == 0){
            text_precio.setText(R.string.precio_gratis);
        }
        else{
            text_precio.setText(String.valueOf(app.precio));
        }
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        getMenuInflater().inflate(R.menu.menu_app, menu);

        MenuItem favorito_item = menu.findItem(R.id.action_favoritos);
        MenuItem share_item = menu.findItem(R.id.action_share);

        Log.i("FAVORITO", "APP FAVORITA " + app.favorita);

        if (app.favorita == 1) {
            favorito_item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_active_addfavoritos));
        }
        else{
            favorito_item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_addfavoritos));
        }


        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(share_item);

        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, app.titulo + " - " + app.descripcion + " - Encuentra mas información en " + app.enlace)
                .setType("text/plain");
        setShareIntent(shareIntent);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_favoritos:

                Log.i("FAVORITO ID", "Programando favorito del app: " + app_id);
                App app = new App();
                app.AddFavorito(getContentResolver());

                MenuItem favorito_item = menu.findItem(R.id.action_favoritos);

                if (app.favorita == 1) {
                    app.favorita = 0;
                    favorito_item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_addfavoritos));
                }
                else{
                    app.favorita = 1;
                    favorito_item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_active_addfavoritos));
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    public void AddFavorito(ContentResolver resolver){

        // Consultar registros locales actuales de puntos
        Uri existingUriPunto = AppContentProvider.CONTENT_URI.buildUpon().appendPath(String.valueOf(this.app_id)).build();

        ContentValues values = new ContentValues();

        if (this.getFavorite() == 0){
            values.put(this.getFavorite(), 1);
        }
        else{
            values.put(GrabilityContract.Columnas.FAVORITA, 0);
        }

        resolver.update(existingUriPunto, values, null, null);
    }
}
