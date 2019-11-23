package com.inertia.phyzmo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.List;

public class ObjectSelectionAdapter extends RecyclerView.Adapter<ObjectSelectionAdapter.ViewHolder> {

    private List<ObjectChoiceModel> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private DisplayDataActivity mActivity;

    // data is passed into the constructor
    ObjectSelectionAdapter(Context context, List<ObjectChoiceModel> data, DisplayDataActivity activity) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mActivity = activity;
        updateSelectButton();
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.object_choice_selection, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.objectName.setText(mData.get(position).getName());
        holder.objectEnabled.setChecked(mData.get(position).isEnabled());

        //System.out.println("In Binding for " + mData.get(position).getName() + " at position " + position + ", setting checked to " + mData.get(position).isEnabled());

        holder.objectEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomCheckBox checkbox = (CustomCheckBox) v;
                checkbox.setChecked(!checkbox.isChecked(), true);
                mData.get(position).setEnabled(checkbox.isChecked());
                updateSelectButton();
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public String getSelectedItemsAsString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < mData.size(); ++i) {
            if (mData.get(i).isEnabled()) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                String itemName = Utils.escapeStringUrl(mData.get(i).getName().toLowerCase());
                sb.append(itemName);
            }
        }
        return sb.toString();
    }

    public String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < mData.size(); ++i) {
            if (mData.get(i).isEnabled()) {
                if (foundOne) {
                    sb.append(",");
                }
                foundOne = true;
                String itemName = Utils.escapeStringUrl(mData.get(i).getName().toLowerCase());
                sb.append("'" + itemName + "'");
            }
        }
        return sb.toString();
    }

    public ArrayList<String> getSelectedItems() {
        ArrayList<String> selectedItems = new ArrayList<>();
        for (ObjectChoiceModel object: mData) {
            if (object.isEnabled()) {
                selectedItems.add(object.getName().toLowerCase());
            }
        }
        return selectedItems;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView objectName;
        CustomCheckBox objectEnabled;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            objectName = itemView.findViewById(R.id.objectName);
            objectEnabled = itemView.findViewById(R.id.objectEnabledCheckbox);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    ObjectChoiceModel getItem(int id) {
        return mData.get(id);
    }

    public void setData(ArrayList<ObjectChoiceModel> d){
        this.mData = d;
//        System.out.println("List of Objects Updated: ");
//        for (ObjectChoiceModel o: mData) {
//            System.out.println("\t-> " + o.getName() + ", " + o.isEnabled());
//        }
        this.notifyDataSetChanged();
        updateSelectButton();
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void updateSelectButton() {
        Button saveObjects = mActivity.findViewById(R.id.saveObjectsChosen);
        EditText distanceInput = mActivity.findViewById(R.id.distanceInput);
        CustomImageView imageCanvas = mActivity.findViewById(R.id.distanceCanvas);

        saveObjects.setEnabled(false);
        boolean oneItemSelected = false;
        for (ObjectChoiceModel object: mData) {
            if (object.isEnabled()) {
                oneItemSelected = true;
            }
        }
        boolean distanceInputted = false;
        try {
            if (Double.valueOf(distanceInput.getText().toString()) > 0) {
                distanceInputted = true;
            }
        } catch (Exception e) {
            System.out.println("Tryed to cast an empty string to double");
        }
        saveObjects.setEnabled(oneItemSelected && distanceInputted && imageCanvas.hasValidLine());
    }
}