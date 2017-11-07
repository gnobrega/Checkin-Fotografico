package br.com.plux.checkinfotografico;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import br.com.plux.checkinfotografico.bean.LocationBean;
import layout.Checkin;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private View mProgress;
    private View mContainerMain;
    private static Integer lastMenuSelected = null;
    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.MAIN_ACTIVITY = this;
        App.context = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Integer menuIndex = 0;
        if( MainActivity.lastMenuSelected != null ) {
            menuIndex = MainActivity.lastMenuSelected;
            String fragmentClass = getFragmentByMenu(menuIndex);
            loadFragment(fragmentClass);
        }
        navigationView.getMenu().getItem(menuIndex).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(menuIndex));

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    @Override
    public void onBackPressed() {

        //Voltar
        Checkin checkinFrament = (Checkin)Fragment.instantiate(MainActivity.this, "layout.Checkin");
        checkinFrament.onBack(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }

        //Preencha o cabeçalho com os dados do usuário
        TextView mUserName = (TextView) findViewById(R.id.textUserName);
        TextView mUserEmail = (TextView) findViewById(R.id.textUserEmail);
        SharedPreferences sharedpreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        if (sharedpreferences != null && mUserName != null) {
            String strName = sharedpreferences.getString("name", "");
            mUserName.setText(strName);
            mUserEmail.setText(sharedpreferences.getString("email", ""));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public Integer getMenuIndex(Integer id) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        for( int i = 0; i < navigationView.getMenu().size(); i ++ ) {
            if( id == navigationView.getMenu().getItem(i).getItemId() ) {
                return i;
            }
        }
        return 0;
    }

    public String getFragmentByMenu(Integer menuIndex) {
        String fragmentClass = "";
        switch( menuIndex ) {
            case 0:
                fragmentClass = "layout.Location";
                break;
            case 1:
                fragmentClass = "layout.Timeline";
                break;
            case 2:
                fragmentClass = "layout.Checkin";
                break;
            case 3:
                fragmentClass = "layout.Sync";
                break;
        }
        return fragmentClass;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //Recupera o id do menu clicado
        int id = item.getItemId();
        String fragmentClass = "";
        MainActivity.lastMenuSelected = getMenuIndex(id);
        SharedPreferences sharedpreferences;

        //Identifica o menu selecionado
        switch (id) {
            case R.id.nav_camera:
                fragmentClass = "layout.Location";
                break;
            case R.id.nav_sync:
                fragmentClass = "layout.Sync";
                break;
            case R.id.nav_map:

                //Verifica se foi selecionado o ponto
                sharedpreferences = getSharedPreferences("location", Context.MODE_PRIVATE);
                if( sharedpreferences == null || sharedpreferences.getInt("id", 0) == 0 ) {
                    Toast.makeText(this, "Selecione um ponto", Toast.LENGTH_SHORT).show();
                    fragmentClass = "layout.Location";
                } else {
                    Integer locationId = sharedpreferences.getInt("id", 0);
                    if( locationId == 0 ) {
                        Toast.makeText(this, "Ponto inválido", Toast.LENGTH_SHORT).show();
                    } else {
                        DataBase db = new DataBase(getApplicationContext());
                        LocationBean locationBean = db.getLocation(locationId);
                        String location = locationBean.getName();
                        String latitude = locationBean.getLatitude();
                        String longitude = locationBean.getLongitude();
                        if( latitude.equals("") || longitude.equals("") ) {
                            Util.toast(getApplicationContext(), "Coordenadas inválidas");
                        } else {
                            //Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?label=" + Uri.encode(location));
                            Uri gmmIntentUri = Uri.parse("geo:0?q=" + latitude + "," + longitude + "(" + Uri.encode(location) + ")");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    }

                }
                break;
            case R.id.nav_logoff:
                logoff();
                toLoginActivity();
                break;
            case R.id.nav_checkin:

                //Verifica se foi selecionado o ponto
                sharedpreferences = getSharedPreferences("location", Context.MODE_PRIVATE);
                if( sharedpreferences == null || sharedpreferences.getInt("id", 0) == 0 ) {
                    Toast.makeText(this, "Selecione um ponto", Toast.LENGTH_SHORT).show();
                    fragmentClass = "layout.Location";
                } else {
                    fragmentClass = "layout.Checkin";
                }
                break;
            case R.id.nav_timeline:
                fragmentClass = "layout.Timeline";
                break;
            default:

        }

        //Carrega a tela referente ao item clicado
        if (!fragmentClass.equals("")) {
            loadFragment(fragmentClass);
        }
        return true;
    }

    public void loadFragment(String fragmentClass) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.flContent, Fragment.instantiate(MainActivity.this, fragmentClass));
        tx.commit();

        //Oculta o menu
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * Retorna para a tela de login
     */
    public void toLoginActivity() {
        Intent intentMain = new Intent(MainActivity.this, LoginActivity.class);
        MainActivity.this.startActivity(intentMain);
    }

    public void logoff() {
        SharedPreferences sharedpreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedpreferences.edit();
        e.clear();
        e.commit();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://br.com.plux.checkinfotografico/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://br.com.plux.checkinfotografico/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
