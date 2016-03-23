package com.franciscogiraldo.fcog.grability.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.franciscogiraldo.fcog.grability.R;
import com.franciscogiraldo.fcog.grability.ui.fragment.AppsFragment;


public class MainActivity extends AppCompatActivity {

    private final String APPSFRAGMENT_TAG = "AFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AppsFragment(), APPSFRAGMENT_TAG)
                    .commit();
        }
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


}