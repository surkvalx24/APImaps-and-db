package com.example.mapsapppp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button btnMyLocation;
    private Button btnAdd;
    private Button btnShow;
    private Button btnShowAll;
    private Button btnDeleteAll;
    private TextView geoInfo;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String location;
    private Double lang, longt;
    // для поиска
    private SearchView searchView;
    // База данных
    public DbHelper db;
    Object id;
    int Clicked = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = getApplicationContext() ;

        final TextView geoInfo = findViewById(R.id. geo_info);
        Button btnMyLocation = findViewById(R.id. btnShowgeo);
        final Button btnAdd = findViewById(R.id. btnAdd);
        Button btnShow = findViewById(R.id. btnShow);
        Button btnShowAll = findViewById(R.id. btnShowAll);
        Button btnDeleteAll = findViewById(R.id. btnDeleteAll);
        db = new DbHelper(this);
        btnAdd.setEnabled(false);
        // иницилизируем карты
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // проверка включена ли геолокация на устройстве
        if(isLocationEnabled(context) == false) {
            geoInfo.setText("Гелокация не включена! Не могу определить ваше местоположение");
            geoInfo.setTextColor(getResources().getColor(R.color.colorError));
        } else {
            geoInfo.setText("Всё ок");
            geoInfo.setTextColor(getResources().getColor(R.color.colorOk));
        }
        // определение местоположения
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        MarkerOptions a = new MarkerOptions().position(new LatLng(50,6));
 //       Marker m = mMap.addMarker(a);
 //       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(50,6)));
        // иницилизируем поиск
        final SearchView searchView = findViewById(R.id. search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                btnAdd.setEnabled(true);
                location = searchView.getQuery().toString();
                List<Address> addresslist = null;
                if(location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addresslist = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addresslist.get(0);
                    lang = address.getLatitude();
                    longt = address.getLongitude();
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                }
                return false;
            }
            // ----------------------

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // действия по кнопкам
        btnShowAll.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Cursor cursor = db.GetData();
               while(cursor.moveToNext()) {
                   LatLng latLng = new LatLng(cursor.getDouble(2), cursor.getDouble(3));
                   mMap.addMarker(new MarkerOptions().position(latLng).title(cursor.getString(1)));
               }
           }
         });
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
            }
        });

        btnMyLocation.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if(isLocationEnabled(MainActivity.this) == false) {
                    geoInfo.setText("Гелокация не включена! Не могу определить ваше местоположение");
                    geoInfo.setTextColor(getResources().getColor(R.color.colorError));
                    Toast.makeText(getApplicationContext(), "Включите геолокацию!", Toast.LENGTH_LONG).show();
                } else {
                    geoInfo.setText("Всё ок");
                    geoInfo.setTextColor(getResources().getColor(R.color.colorOk));
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                            // location
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            if(location!=null) {
                                                Double lat = location.getLatitude();
                                                Double longt = location.getLatitude();
                                                Toast.makeText(getApplicationContext(), lat + " - " + longt, Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), " - ", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                        }
                    }
                }
            }
        }) ;
        btnAdd.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ok_no = db.Add(location,lang,longt);
                if(ok_no == true)
                    Toast.makeText(getApplicationContext(), "Данные добавлены в базу", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Ошибка, данные не добавлены в базу", Toast.LENGTH_LONG).show();
            }
        });
        btnShow.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = db.GetData();
                if(cursor.getCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Нет данных в бд", Toast.LENGTH_LONG).show();
                    return;
                }
                StringBuffer stringBuffer = new StringBuffer();
                LongSparseArray IDs = new LongSparseArray(); // массив id
                int count = cursor.getCount();
                String[] Data = new String[count];
                int i = 0;
                while(cursor.moveToNext()) {
                    stringBuffer.append(cursor.getString(1) + "\n");
                    stringBuffer.append(cursor.getString(2) + "\n");
                    stringBuffer.append(cursor.getString(3) + "\n" + "\n");
                    Data[i] = "Введённый адрес: "+ cursor.getString(1) + "Широта: "+cursor.getString(2)+" Долгота: "
                            +cursor.getString(3);
                    IDs.put(i,cursor.getString(0)); // закидываем ИД к ИД в listview
                    i++;
                }
                //Toast.makeText(getApplicationContext(), Data.get(1).toString(), Toast.LENGTH_LONG).show();
                    ShowData("Data: ", Data, IDs);
            }
        });
    }
    public void ShowData(String title, String[] Data, final LongSparseArray IDs) {

        int checkedItem = -1; Clicked = 0;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
  //      builder.setMessage(Message);
        builder.setSingleChoiceItems( Data, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i < IDs.size(); i++) {
                            if(IDs.keyAt(i) == which) {
                                id = IDs.valueAt(i);
                                Toast.makeText(getApplicationContext(), id.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                        Clicked = 1;
                    }
                });
        builder.setPositiveButton("Показать на карте", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Clicked == 0) {
                            Toast.makeText(getApplicationContext(), "Выберите маркер!", Toast.LENGTH_LONG).show();
                        } else {
                            Cursor cursor = db.GetData();
                            while (cursor.moveToNext()) {
                                if (cursor.getString(0).equals(id.toString())) {
                                    String Address1 = cursor.getString(1);
                                    Double lang1 = cursor.getDouble(2);
                                    Double longt1 = cursor.getDouble(3);
                                    LatLng latLng = new LatLng(lang1, longt1);
                                    mMap.addMarker(new MarkerOptions().position(latLng).title(Address1));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                    Toast.makeText(getApplicationContext(), "Address: " + Address1.toString() + "Lang: " + lang1.toString() + " Longt: " + longt1.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
        builder.setNegativeButton("Редактировать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                 //       final Context context = getApplicationContext() ;
                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View edittextView = li.inflate(R.layout.edittext, null);
                        AlertDialog.Builder builder_edit = new AlertDialog.Builder(MainActivity.this);
                        builder_edit.setView(edittextView);
                        final EditText etAddress = (EditText) edittextView.findViewById(R.id.etAddress);
                        final EditText etLan = (EditText) edittextView.findViewById(R.id.etLan);
                        final EditText etLongt = (EditText) edittextView.findViewById(R.id.etLongt);
                        builder_edit.setCancelable(true);
                        builder_edit.setTitle("Редактирование маркета");
                        builder_edit.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog_edit, int which) {
                                boolean ok_no = db.Edit(id.toString(), etAddress.getText().toString(),etLan.getText().toString(),etLongt.getText().toString());
                                if(ok_no == true)
                                    Toast.makeText(getApplicationContext(), "Отредактированно!", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Ошибка редактирования!", Toast.LENGTH_LONG).show();
                            }
                        });
                        Cursor cursor_edit = db.GetData();
                        while (cursor_edit.moveToNext()) {
                            if (cursor_edit.getString(0).equals(id.toString())) {
                                etAddress.setText(cursor_edit.getString(1));
                                etLan.setText(cursor_edit.getString(2));
                                etLongt.setText(cursor_edit.getString(3));
                            }
                        }
                        AlertDialog dialog_edit = builder_edit.create();
                        dialog_edit.show();
                    }
                });
        builder.setNeutralButton("Удалить маркер", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor cursor = db.GetData();
                        while (cursor.moveToNext()) {
                            if (cursor.getString(0).equals(id.toString())) {
                                Integer ok_no = db.Delete(cursor.getString(0));
                                if(ok_no > 0)
                                    Toast.makeText(getApplicationContext(), "Маркер " + cursor.getString(0) + " удалён!", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Маркера не существует", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // добавляем изначальную позицию карт

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        MarkerOptions a = new MarkerOptions().position(sydney);
//        Marker m = mMap.addMarker(a);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                mMap.setMyLocationEnabled(true);
//            }
//        }
//        else {
//            mMap.setMyLocationEnabled(true);
//        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


}
