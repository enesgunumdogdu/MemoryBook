package com.enesgunumdogdu.memorybook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.enesgunumdogdu.memorybook.databinding.RecyclerRowBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MemoryHolder> {

    ArrayList<Memory> memoryArrayList;
    public MemoryAdapter(ArrayList<Memory> memoryArrayList){
        this.memoryArrayList = memoryArrayList;
    }


    @Override
    public MemoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new  MemoryHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryAdapter.MemoryHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(memoryArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),MemoryActivity.class);
                intent.putExtra("memoryId",memoryArrayList.get(position).id);
                intent.putExtra("info","old");
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memoryArrayList.size();
    }

    public class MemoryHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;


        public MemoryHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
