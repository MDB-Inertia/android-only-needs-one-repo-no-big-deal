package com.inertia.phyzmo.datadisplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.inertia.phyzmo.R;
import com.inertia.phyzmo.datadisplay.models.ObjectChoiceModel;
import com.inertia.phyzmo.datadisplay.views.DistanceSelectionView;
import com.inertia.phyzmo.utils.StringUtils;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.List;

public class ObjectSelectionAdapter extends RecyclerView.Adapter<ObjectSelectionAdapter.ViewHolder> {

    private List<ObjectChoiceModel> mData;
    private LayoutInflater mInflater;
    private DisplayDataActivity mActivity;

    ObjectSelectionAdapter(Context context, List<ObjectChoiceModel> data, DisplayDataActivity activity) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mActivity = activity;
        updateSelectButton();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.object_choice_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.objectName.setText(mData.get(position).getName());
        holder.objectEnabled.setChecked(mData.get(position).isEnabled());

        holder.objectEnabled.setOnClickListener(v -> {
            CustomCheckBox checkbox = (CustomCheckBox) v;
            checkbox.setChecked(!checkbox.isChecked(), true);
            mData.get(position).setEnabled(checkbox.isChecked());
            updateSelectButton();
        });
    }

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
                String itemName = StringUtils.escapeStringUrl(mData.get(i).getName().toLowerCase());
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
                String itemName = StringUtils.escapeStringUrl(mData.get(i).getName().toLowerCase());
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView objectName;
        CustomCheckBox objectEnabled;

        ViewHolder(View itemView) {
            super(itemView);

            objectName = itemView.findViewById(R.id.objectName);
            objectEnabled = itemView.findViewById(R.id.objectEnabledCheckbox);
        }
    }

    ObjectChoiceModel getItem(int id) {
        return mData.get(id);
    }

    public void setData(ArrayList<ObjectChoiceModel> d){
        this.mData = d;
        this.notifyDataSetChanged();
        updateSelectButton();
    }

    public void updateSelectButton() {
        Button saveObjects = mActivity.findViewById(R.id.saveObjectsChosen);
        EditText distanceInput = mActivity.findViewById(R.id.distanceInput);
        DistanceSelectionView imageCanvas = mActivity.findViewById(R.id.distanceCanvas);

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
            System.out.println("Tried to cast an empty string to double");
        }
        saveObjects.setEnabled(oneItemSelected && distanceInputted && imageCanvas.hasValidLine());
    }
}