package com.mrcornman.otp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mrcornman.otp.R;
import com.mrcornman.otp.activities.MainActivity;
import com.mrcornman.otp.adapters.ClientMatchAdapter;
import com.mrcornman.otp.models.MatchItem;
import com.mrcornman.otp.utils.DatabaseHelper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ClientListFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private String sectionNumber;

    private ClientListInteractionListener clientListInteractionListener;

    private ClientMatchAdapter clientMatchAdapter;
    private List<MatchItem> matchItems;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ClientListFragment newInstance(int sectionNumber) {
        ClientListFragment fragment = new ClientListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ClientListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // init views
        View rootView = inflater.inflate(R.layout.fragment_client_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.client_list);

        // set up list view input
        // set up on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String otherId = view.getTag().toString();
                DatabaseHelper.getUserById(otherId, new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if(parseUser != null) {
                            String recipientId = parseUser.getObjectId();
                            clientListInteractionListener.onRequestOpenConversation(recipientId);
                        }
                    }
                });
            }
        });

        matchItems = new ArrayList<>();
        clientMatchAdapter = new ClientMatchAdapter(getActivity().getApplicationContext(), matchItems);
        listView.setAdapter(clientMatchAdapter);

        // fill list up
        DatabaseHelper.findTopMatches(20, new FindCallback<MatchItem>() {
            @Override
            public void done(List<MatchItem> list, ParseException e) {
                for(MatchItem match : list) {
                    clientMatchAdapter.addMatch(match);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            sectionNumber = getArguments().getString(ARG_SECTION_NUMBER);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

        try {
            clientListInteractionListener = (ClientListInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface ClientListInteractionListener {
        void onRequestOpenConversation(String recipientId);
    }
}