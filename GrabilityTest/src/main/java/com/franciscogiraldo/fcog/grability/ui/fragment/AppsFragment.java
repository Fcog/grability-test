package com.franciscogiraldo.fcog.grability.ui.fragment;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.franciscogiraldo.fcog.grability.R;
import com.franciscogiraldo.fcog.grability.ui.adapter.AdaptadorApps;
import com.franciscogiraldo.fcog.grability.utils.Constantes;

/**
 * Created by fcog on 3/18/16.
 */
public class AppsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener, View.OnClickListener{

    private RecyclerView recyclerView;

    private AdaptadorApps adapter;
    private TextView emptyView;
    private ImageView view_icon;

/*    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapterSpinner;
    private static List<String> categorias;*/

    //category chosen on main grid, default All categories
    private int categoria_pos = 0;
    private String categoria;

    SharedPreferences prefs;
    private int actual_view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_apps, container, false);

/*        // get category id from main spinner
        if (getIntent().getExtras() != null) {
            Intent intent = getIntent();
            categoria_pos = Integer.parseInt(intent.getStringExtra("categoria_pos"));
        }*/

        recyclerView = (RecyclerView) rootView.findViewById(R.id.reciclador);
//        spinner = (Spinner) rootView.findViewById(R.id.apps_spinner);
        emptyView = (TextView) rootView.findViewById(R.id.recyclerview_data_empty);
        view_icon = (ImageView) rootView.findViewById(R.id.view_icon);

        emptyView.setText(R.string.aviso_no_hay_promo_categoria);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        actual_view = prefs.getInt(Constantes.DEFAULT_VIEW, Constantes.LIST);

        load_adapter(actual_view);

/*        // set spinner information
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
        spinner.setOnItemSelectedListener(this);*/

        view_icon.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        App2.loadAll(getActivity().getContentResolver());

/*        String where = null;

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

        return new CursorLoader(this, GrabilityContract.CONTENT_URI1, null, where, null, null);*/
        return new CursorLoader(getActivity(), GrabilityContract.CONTENT_URI1, null, null, null, null);
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

        getActivity().getSupportLoaderManager().restartLoader(0, args, this);
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

        adapter = new AdaptadorApps(getActivity(), view_selected);

        if (view_selected == Constantes.LIST){
            layoutManager = new LinearLayoutManager(getActivity());
            icon_id = R.drawable.ic_grid;
        }
        else{
            layoutManager = new GridLayoutManager(getActivity(), 3);
            icon_id = R.drawable.ic_list;
        }

        actual_view = view_selected;

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        getActivity().getSupportLoaderManager().initLoader(0, null, this);

        // change icon
        view_icon.setImageResource(icon_id);
    }
}
