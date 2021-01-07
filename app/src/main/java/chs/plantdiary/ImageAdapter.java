package chs.plantdiary;

import android.content.Context;
import android.media.Image;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Plants> mPlants; //lista cu toate entry-urile de plante din baza de date
    private OnItemClickListener mListener;

    public ImageAdapter(Context context, List<Plants> plants){
        mContext = context;
        mPlants = plants;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Plants plantCurrent = mPlants.get(position);
        holder.textViewName.setText(plantCurrent.getPlantName());
        //picasso face treaba mai usoara cand punem o poza in imageView
        Picasso.with(mContext)
                .load(plantCurrent.getImageUrl())
                .fit()
                .centerInside()
                //.centerCrop()
                .into(holder.imageView);
    }

    @Override
    //suma a cate entry-uri sunt in baza de date
    public int getItemCount() {
        return mPlants.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {
        public TextView textViewName;
        public ImageView imageView;

        public ImageViewHolder(View itemView){
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null){
                int position = getAdapterPosition(); //we get the position of the clicked item
                if(position != RecyclerView.NO_POSITION){
                    mListener.onItemClick(position);
                }
            }
        }

        //meniu cu optiunile de edit plant si delete plant
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Choose an option");
            MenuItem doWhatever = menu.add(Menu.NONE, 1, 1, "Edit plant");
            MenuItem delete = menu.add(Menu.NONE, 2, 2, "Delete plant");

            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        // cand tinem apasat pe ecran, apare meniul: redirectionat in functie de operatiunea aleasa
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener != null){
                int position = getAdapterPosition(); //we get the position of the clicked item
                if(position != RecyclerView.NO_POSITION){
                    switch(item.getItemId()){
                        case 1:
                            mListener.onEditPlantClick(position);
                            return true;
                        case 2:
                            mListener.onDeletePlantClick(position);
                            return true;
                    }
                }
            }

            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditPlantClick(int position);
        void onDeletePlantClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }
}
