package com.inertia.phyzmo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MultipleSelectionSpinner extends AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener {

    String[] items = null;
    boolean[] mSelection = null;
    Context context;
    Activity activity;

    String fileName = "";

    ArrayAdapter<String> simpleAdapter;

    public MultipleSelectionSpinner(Context context, Activity a, String fileName) {
        super(context);

        this.context = context;
        this.fileName = fileName;
        this.activity = a;
        simpleAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        super.setAdapter(simpleAdapter);
        this.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    public MultipleSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        simpleAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simpleAdapter);
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (mSelection != null && which < mSelection.length) {
            mSelection[which] = isChecked;
            simpleAdapter.clear();
            if (buildSelectedItemString().length() > 0) {
                simpleAdapter.add(buildSelectedItemString());
            } else {
                simpleAdapter.add("Tap to select");
            }
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds");
        }
    }

    @Override
    public boolean performClick() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(items, mSelection, this);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                System.out.println("Analyzing just the following: " + getSelectedItemsAsString());
                activity.findViewById(R.id.chooseItems).setVisibility(INVISIBLE);
                activity.findViewById(R.id.statusText).setVisibility(VISIBLE);
                TextView t = activity.findViewById(R.id.statusText);
                t.setText("Analyzing Objects");
                String selectedList = buildSelectedItemString().replace(" ", "%20");
                String executedURL = "https://us-central1-phyzmo.cloudfunctions.net/data-computation?objectsDataUri=https://storage.googleapis.com/phyzmo-videos/" + fileName.replace(".mp4", ".json") + "&obj_descriptions=[" + selectedList + "]&ref_list=[[0.121,0.215],[0.9645,0.446],0.60]";
                System.out.println(executedURL);
                new FirebaseUtils.DataComputationRequest(activity).execute(executedURL);
            }
        });
        builder.show();
        return true;
    }

    public void setActivity(Activity a) {
        this.activity = a;
    }

    public void setFilename(String fn) {
        this.fileName = fn;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(String[] items) {
        items = items;
        mSelection = new boolean[items.length];
        simpleAdapter.clear();
        simpleAdapter.add(items[0]);
        Arrays.fill(mSelection, false);
    }

    public void setItems(List<String> newItems) {
        items = newItems.toArray(new String[newItems.size()]);
        mSelection = new boolean[items.length];
        simpleAdapter.clear();
        simpleAdapter.add("Select from detected objects");
        Arrays.fill(mSelection, false);
    }

    public void setSelection(String[] selection) {
        for (String cell : selection) {
            for (int j = 0; j < items.length; ++j) {
                if (items[j].equals(cell)) {
                    mSelection[j] = true;
                }
            }
        }
    }

    public void setSelection(List<String> selection) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        for (String sel : selection) {
            for (int j = 0; j < items.length; ++j) {
                if (items[j].equals(sel)) {
                    mSelection[j] = true;
                }
            }
        }
        simpleAdapter.clear();
        simpleAdapter.add(buildSelectedItemString());
    }

    public void setSelection(int index) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        if (index >= 0 && index < mSelection.length) {
            mSelection[index] = true;
        } else {
            throw new IllegalArgumentException("Index " + index
                    + " is out of bounds.");
        }
        simpleAdapter.clear();
        simpleAdapter.add(buildSelectedItemString());
    }

    public void setSelection(int[] selectedIndicies) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        for (int index : selectedIndicies) {
            if (index >= 0 && index < mSelection.length) {
                mSelection[index] = true;
            } else {
                throw new IllegalArgumentException("Index " + index
                        + " is out of bounds.");
            }
        }
        simpleAdapter.clear();
        simpleAdapter.add(buildSelectedItemString());
    }

    public List<String> getSelectedStrings() {
        List<String> selection = new LinkedList<String>();
        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                selection.add(items[i]);
            }
        }
        return selection;
    }

    public List<Integer> getSelectedIndicies() {
        List<Integer> selection = new LinkedList<Integer>();
        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                selection.add(i);
            }
        }
        return selection;
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {

                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append("'" + items[i] + "'");
            }
        }

        return sb.toString();
    }

    public String getSelectedItemsAsString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(items[i]);
            }
        }
        return sb.toString();
    }
}