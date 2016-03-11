package com.franciscogiraldo.fcog.grability.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.franciscogiraldo.fcog.grability.provider.GrabilityContract;
import com.franciscogiraldo.fcog.grability.ui.adapter.AdaptadorApps;
import com.franciscogiraldo.fcog.grability.R;
import com.franciscogiraldo.fcog.grability.utils.Constantes;
import com.franciscogiraldo.fcog.grability.web.App;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private RecyclerView recyclerView;

    private AdaptadorApps adapter;
    private TextView emptyView;
    private ImageView view_icon;

    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapterSpinner;
    private static List<String> categorias;

    //category chosen on main grid, default All categories
    private int categoria_pos = 0;
    private String categoria;

    SharedPreferences prefs;
    private int actual_view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        App.loadAll(getContentResolver());

        // get category id from main spinner
        if (getIntent().getExtras() != null) {
            Intent intent = getIntent();
            categoria_pos = Integer.parseInt(intent.getStringExtra("categoria_pos"));
        }

        setContentView(R.layout.activity_main);

        setToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.reciclador);
        spinner = (Spinner) findViewById(R.id.apps_spinner);
        emptyView = (TextView) findViewById(R.id.recyclerview_data_empty);
        view_icon = (ImageView) findViewById(R.id.view_icon);

        emptyView.setText(R.string.aviso_no_hay_promo_categoria);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        actual_view = prefs.getInt(Constantes.DEFAULT_VIEW, Constantes.LIST);

        load_adapter(actual_view);

        // set spinner information
        categorias = Arrays.asList((getResources().getStringArray(R.array.categorias)));
        categoria = categorias.get(categoria_pos);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSpinner = ArrayAdapter.createFromResource(this, R.array.categorias, R.layout.categoria_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSpinner.setDropDownViewResource(R.layout.categoria_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapterSpinner);
        // set spinner position
        //int spinnerPosition = adapterSpinner.getPosition(categoria_nombre);
        spinner.setSelection(categoria_pos);
        spinner.setOnItemSelectedListener(this);

        view_icon.setOnClickListener(this);
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_favoritos:
                Intent intent1 = new Intent(getApplicationContext(), FavoritosActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;
            case R.id.action_alerta:
                Intent intent2 = new Intent(getApplicationContext(), AlertasActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;
            case R.id.action_settings:
                Intent intent3 = new Intent(getApplicationContext(), SettingsActivity.class);
                intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent3);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String where = null;

        //si el usuario selecciono categoria con el spinner
        //pero no se por qué siempre entra al onItemSelected del spinner entonces el else es innecesario pero lo dejo por "si las moscas"
        if (args != null  && args.containsKey("posicion_id")){
            int posicion_id = args.getInt("posicion_id");
            String categoria = categorias.get(posicion_id);

            where = (posicion_id == 0) ? null : GrabilityContract.Columnas.CATEGORIA +  "='" + categoria + "'";
        }
        else{
            // Consultar las promociones por categoría
            where = (categoria_pos == 0) ? null : GrabilityContract.Columnas.CATEGORIA +  "='" + categoria + "'";
        }

        return new CursorLoader(this, GrabilityContract.CONTENT_URI1, null, where, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        if (data.getCount() == 0){
            emptyView.setText(R.string.error_descargar_datos);
        }
        else{
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Bundle args = new Bundle();
        args.putInt("posicion_id", position);

        getSupportLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {

        if (actual_view == Constantes.LIST) {
            load_adapter(Constantes.GRID);
            //persist user's view choice
            prefs.edit().putInt(Constantes.DEFAULT_VIEW, Constantes.GRID).apply();
        }
        else{
            load_adapter(Constantes.LIST);
            //persist user's view choice
            prefs.edit().putInt(Constantes.DEFAULT_VIEW, Constantes.LIST).apply();
        }
    }

    private void load_adapter(int view_selected){

        RecyclerView.LayoutManager layoutManager;
        int icon_id;

        adapter = new AdaptadorApps(this, view_selected);

        if (view_selected == Constantes.LIST){
            layoutManager = new LinearLayoutManager(this);
            icon_id = R.drawable.ic_grid;
        }
        else{
            layoutManager = new GridLayoutManager(this, 3);
            icon_id = R.drawable.ic_list;
        }

        actual_view = view_selected;

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(0, null, this);

        // change icon
        view_icon.setImageResource(icon_id);
    }
}