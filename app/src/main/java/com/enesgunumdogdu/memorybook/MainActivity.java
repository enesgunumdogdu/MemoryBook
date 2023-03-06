package com.enesgunumdogdu.memorybook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.enesgunumdogdu.memorybook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Memory> memoryArrayList;
    MemoryAdapter memoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        memoryArrayList = new ArrayList<Memory>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memoryAdapter = new MemoryAdapter(memoryArrayList);
        binding.recyclerView.setAdapter(memoryAdapter);

        getData();
    }


    private void getData(){

        try{
            SQLiteDatabase database = this.openOrCreateDatabase("Memories",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM memories",null);
            int nameIx = cursor.getColumnIndex("memoryname");
            int idIx = cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                Memory memory = new Memory(name,id);
                memoryArrayList.add(memory);
            }
            memoryAdapter.notifyDataSetChanged();
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.memory_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.add_memory){
         Intent intent = new Intent(MainActivity.this,MemoryActivity.class);
         intent.putExtra("info","new");
         startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}