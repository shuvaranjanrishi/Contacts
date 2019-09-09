package com.example.asus.contacts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import java.util.ArrayList;

import static android.provider.OpenableColumns.DISPLAY_NAME;

public class MainActivity extends AppCompatActivity{

    ListView listView;
    ArrayList<ContactHolder> contactHolderArrayList;
    DBHelper dbHelper;
    ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.contact_app_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        listView = findViewById(R.id.listViewId);

        dbHelper = new DBHelper(getApplicationContext());

        populateListView();

        //list view click listener event to show contact details
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this,ViewContactDetailsActivity.class);

                intent.putExtra("ID",contactHolderArrayList.get(position).id);
                intent.putExtra("IMAGE",contactHolderArrayList.get(position).image);
                intent.putExtra("NAME",contactHolderArrayList.get(position).name);
                intent.putExtra("NUMBER",contactHolderArrayList.get(position).number);

                startActivityForResult(intent,200);
            }
        });

    }

    //go to add new contact activity by clicking fab button
    public void floatingButtonAction(View view){
        startActivityForResult(new Intent(MainActivity.this,AddContactActivity.class),100);
    }

    //add data to listview and refresh
    void populateListView(){
        contactHolderArrayList = dbHelper.getAllData();
        contactAdapter = new ContactAdapter(MainActivity.this,contactHolderArrayList);
        listView.setAdapter(contactAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_layout,menu);

        MenuItem menuItem = menu.findItem(R.id.searchViewId);
        SearchView searchView = (SearchView) menuItem.getActionView();

        //search contact action from all contacts
        searchView.setQueryHint("search from "+contactHolderArrayList.size()+" contacts");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if(item.getItemId() == R.id.sortByNameId){
            dbHelper.setOrderBy(0);
            populateListView();

        }
        else if(item.getItemId() == R.id.sortByNumberId){
            dbHelper.setOrderBy(1);
            populateListView();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 100 && resultCode == RESULT_OK){
            populateListView();
            Toast.makeText(this, "Contact saved Successfully", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == 200 && resultCode == RESULT_OK){
            populateListView();
            Toast.makeText(this, "Contact updated Successfully", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == 200 && resultCode == 300){
            populateListView();
            Toast.makeText(this, "Contact deleted Successfully", Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
