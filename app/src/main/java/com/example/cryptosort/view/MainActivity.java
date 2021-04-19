package com.example.cryptosort.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cryptosort.adaptor.RecyclerViewAdapter;
import com.example.cryptosort.compare.CryptoSortComparator;
import com.example.cryptosort.compare.NameComparator;
import com.example.cryptosort.compare.NameDescComparator;
import com.example.cryptosort.compare.PriceComparator;
import com.example.cryptosort.model.CryptoModel;
import com.example.cryptosort.R;
import com.example.cryptosort.service.CryptoAPI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.FavDB;
import database.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ArrayList<CryptoModel> cryptoModels;
    private String BASE_URL ="https://api.nomics.com/v1/";

    Retrofit retrofit;
    RecyclerView recyclerView;
    SearchView searchView;
    RecyclerViewAdapter recyclerViewAdapter;
    CompositeDisposable compositeDisposable;
    Button priceSortButton;
    Button nameSortButton;
    Button favSortButton;
    Button logoutButton;
    TextView userTextView;

    private FavDB favDB;

    private FirebaseUser user;
    private DatabaseReference reference;

    private String userID;

    private Boolean firstStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //https://api.nomics.com/v1/prices?key=00c2edef8b460ceee95bfa55aefeac31

        recyclerView  = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        priceSortButton = findViewById(R.id.priceSortBtn);
        nameSortButton = findViewById(R.id.nameSortBtn);
        favSortButton = findViewById(R.id.favSortBtn);
        logoutButton = findViewById(R.id.logoutBtn);
        userTextView = findViewById(R.id.userTextView);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        if (firstStart == true){

            favDB = new FavDB(MainActivity.this);

            reference.child(userID + "/" + "currency").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()){
                        for (DataSnapshot data : task.getResult().getChildren()){
                            favDB.insertIntoTheDatabase(data.getKey(),"1");
                            reference.child(userID + "/" + "currency").removeValue();
                        }
                    } else{
                        Toast.makeText(MainActivity.this, "Failed To Get Fav", Toast.LENGTH_LONG).show();
                    }
                }
            });
            firstStart = false;
        }

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if (userProfile != null){
                    String email = userProfile.email.split("@")[0];
                    userTextView.setText("Welcome, " + email + " !");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Something Wrong Happened!", Toast.LENGTH_LONG).show();
            }
        });

        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create()).build();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = favDB.getReadableDatabase();
                Cursor cursor = favDB.select_all_fav_list();

                try {
                    while (cursor.moveToNext()){
                        String id = cursor.getString(cursor.getColumnIndex(FavDB.KEY_ID));

                        Map<String, Object> idUpdate = new HashMap<>();
                        idUpdate.put(id, 1);

                        reference.child(userID + "/" + "currency").updateChildren(idUpdate);
                    }
                }finally {
                    if (cursor != null && cursor.isClosed())
                        cursor.close();
                    db.close();
                }
                favDB.clearTable();
                firstStart = true;
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        loadData();
    }

    private void loadData(){
        CryptoAPI cryptoAPI = retrofit.create(CryptoAPI.class);

        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(cryptoAPI.getData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse));
    }

    private void handleResponse(List<CryptoModel> cryptoModelList){

        cryptoModels = new ArrayList<>(cryptoModelList);

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerViewAdapter = new RecyclerViewAdapter(cryptoModels, MainActivity.this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    int clickSort = 0;
    ArrayList<CryptoModel> cryptoModelForAsc;

    public void priceSortButton(View view){
        cryptoModelForAsc = new ArrayList<CryptoModel>(cryptoModels);

        if (clickSort == 0) {
            Collections.sort(cryptoModelForAsc, new CryptoSortComparator(new PriceComparator()));

            priceSortButton.animate().rotation(180).setInterpolator(new AccelerateDecelerateInterpolator());
            recyclerViewAdapter = new RecyclerViewAdapter(cryptoModelForAsc, MainActivity.this);
            recyclerViewAdapter.notifyDataSetChanged();

            clickSort++;
        }
        else if (clickSort == 1){
            priceSortButton.animate().rotation(0).setInterpolator(new AccelerateDecelerateInterpolator());
            recyclerViewAdapter = new RecyclerViewAdapter(cryptoModels, MainActivity.this);
            recyclerViewAdapter.notifyDataSetChanged();

            clickSort = 0;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();

    }

    int clickNameSort = 0;
    ArrayList<CryptoModel> cryptoModelForName;

    public void nameSortButton(View view){
        cryptoModelForName = new ArrayList<CryptoModel>(cryptoModels);

        if (clickNameSort == 0) {
            Collections.sort(cryptoModelForName, new CryptoSortComparator(new NameComparator()));

            nameSortButton.animate().rotationY(180).setInterpolator(new AccelerateDecelerateInterpolator());
            recyclerViewAdapter = new RecyclerViewAdapter(cryptoModelForName, MainActivity.this);
            recyclerViewAdapter.notifyDataSetChanged();

            clickNameSort++;
        }
        else if (clickNameSort == 1){
            nameSortButton.animate().rotationY(0).setInterpolator(new AccelerateDecelerateInterpolator());
            Collections.sort(cryptoModelForName, new CryptoSortComparator(new NameDescComparator()));
            recyclerViewAdapter = new RecyclerViewAdapter(cryptoModelForName, MainActivity.this);
            recyclerViewAdapter.notifyDataSetChanged();

            clickNameSort = 0;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    int clickFavSort = 0;
    public void favSortButton(View view){
        if (clickFavSort == 0) {
            favSortButton.setBackgroundResource(R.drawable.ic_baseline_favorite_red_24);
            recyclerViewAdapter.showFavList(clickFavSort);
            recyclerViewAdapter.notifyDataSetChanged();

            clickFavSort++;
        }
        else if (clickFavSort == 1){
            favSortButton.setBackgroundResource(R.drawable.ic_baseline_favorite_shadow_24);
            loadData();
            clickFavSort = 0;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}