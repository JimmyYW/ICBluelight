package me.jimmywang.icbluelight;

/**
 * A Fragment that take addtional user information and pass to the next fragment
 * Created by yanmingwang on 12/10/16.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class RequestHelpFragment extends Fragment {
    private submit callback;
    private EditText detailLocation;
    private EditText additionMessage;

    /**
     * Get user info
     */
    public interface submit{
        public void onSubmit(String MS, String DT);
    }

    public RequestHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_studet_help, container, false);
        Button requestHelp = (Button) view.findViewById(R.id.helpNow);
        additionMessage = (EditText) view.findViewById(R.id.editText2);
        detailLocation = (EditText) view.findViewById(R.id.editText);
        requestHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSubmit(additionMessage.getText().toString(),detailLocation.getText().toString());
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * This makes sure that the container activity has implemented
     * the callback interface. If not, it throws an exception
     * @param activity
     * current activity that attach to
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (submit) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemSelectedListener");
        }
    }

}
