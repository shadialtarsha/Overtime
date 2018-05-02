package com.shadialtarsha.overtime.main_screen_controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.model.Match;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MatchesActivity extends AppCompatActivity implements DatePickerFragment.onDateChangedListener {

    private static final String DIALOG_DATE = "date";

    private static final String HOME_TEAM_NAME = "HomeTeamName";
    private static final String HOME_TEAM_KEY = "HomeTeamKey";
    private static final String DATE_TIME = "DateTime";
    private static final String AWAY_TEAM_NAME = "AwayTeamName";
    private static final String AWAY_TEAM_KEY = "AwayTeamKey";

    private Toolbar mMatchesToolbar;
    private RecyclerView mMatchesRecyclerView;
    private NestedScrollView mEmptyMatchesReScrollView;
    private FloatingActionButton mDatePickerFloatingActionButton;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, MatchesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);
        mMatchesToolbar = (Toolbar) findViewById(R.id.matches_toolbar);
        mMatchesToolbar.setTitle(getString(R.string.matches_activity));
        mDatePickerFloatingActionButton = (FloatingActionButton) findViewById(R.id.match_date_picker_fab);
        mMatchesRecyclerView = (RecyclerView) findViewById(R.id.matches_recycler_view);
        mMatchesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mDatePickerFloatingActionButton.getVisibility() == View.VISIBLE) {
                    mDatePickerFloatingActionButton.hide();
                } else if (dy < 0 && mDatePickerFloatingActionButton.getVisibility() != View.VISIBLE) {
                    mDatePickerFloatingActionButton.show();
                }
            }
        });
        mEmptyMatchesReScrollView = (NestedScrollView) findViewById(R.id.matches_empty_layout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mMatchesRecyclerView.setLayoutManager(linearLayoutManager);
        setSupportActionBar(mMatchesToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Date currentDate = new Date();
        String currentMatchDate = parseDate(currentDate);
        String request = "https://api.fantasydata.net/v3/soccer/scores/json/GamesByDate/" + currentMatchDate;
        new MatchesDownloader(this).execute(request);
        mDatePickerFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(new Date());
                dialog.show(fm, DIALOG_DATE);
            }
        });
    }

    private ArrayList<Match> parseJSON(String json) {
        if (json != null) {
            try {
                ArrayList<Match> matchesList = new ArrayList<>();
                JSONArray matches = new JSONArray(json);
                for (int i = 0; i < matches.length(); i++) {
                    JSONObject object = matches.getJSONObject(i);

                    String homeTeamName = object.getString(HOME_TEAM_NAME);
                    String matchDate = object.getString(DATE_TIME);
                    String awayTeamName = object.getString(AWAY_TEAM_NAME);

                    if (homeTeamName.length() > 15) {
                        homeTeamName = object.getString(HOME_TEAM_KEY);
                    }
                    if (awayTeamName.length() > 15) {
                        awayTeamName = object.getString(AWAY_TEAM_KEY);
                    }
                    Match match = new Match();
                    match.setHomeTeam(homeTeamName);
                    match.setMatchDate(matchDate);
                    match.setAwayTeam(awayTeamName);

                    matchesList.add(match);
                }
                return matchesList;
            } catch (JSONException ex) {
                Log.d(Constants.MATCHES_ACTIVITY, ex.getMessage());
                return null;
            }
        } else {
            Log.e(Constants.MATCHES_ACTIVITY, "No data received from HTTP request");
            return null;
        }
    }

    @Override
    public void dateChanged(Date date) {
        String matchDate = parseDate(date);
        String request = "https://api.fantasydata.net/v3/soccer/scores/json/GamesByDate/" + matchDate;
        new MatchesDownloader(this).execute(request);
    }

    private class MatchesDownloader extends AsyncTask<String, Void, Void> {
        private ProgressDialog dialog;
        ArrayList<Match> matchesList;

        public MatchesDownloader(Activity activity){
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getString(R.string.progress_dialog_message));
        }

        @Override
        protected Void doInBackground(String... params) {
            String result = "";
            try {
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestProperty("Ocp-Apim-Subscription-Key", "a8e1bc36242e43818c8e350e7917e3ae");
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                matchesList = parseJSON(result);
            } catch (Exception ex) {
                Log.d(Constants.MATCHES_ACTIVITY, ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (!matchesList.isEmpty()) {
                mMatchesRecyclerView.setVisibility(View.VISIBLE);
                mEmptyMatchesReScrollView.setVisibility(View.GONE);
                MatchAdapter adapter = new MatchAdapter(matchesList);
                mMatchesRecyclerView.setAdapter(adapter);
            } else {
                mMatchesRecyclerView.setVisibility(View.GONE);
                mEmptyMatchesReScrollView.setVisibility(View.VISIBLE);
            }
        }
    }

    private class MatchHolder extends RecyclerView.ViewHolder {

        private TextView mHomeTeamTextView;
        private TextView mMatchDateTextView;
        private TextView mAwayTeamTextView;

        private void setUpViews(View itemView) {
            mHomeTeamTextView = (TextView) itemView.findViewById(R.id.matches_list_item_home_team);
            mMatchDateTextView = (TextView) itemView.findViewById(R.id.matches_list_item_match_date);
            mAwayTeamTextView = (TextView) itemView.findViewById(R.id.matches_list_item_away_team);
        }

        private MatchHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
        }

        private void bindMatch(Match match) {
            mHomeTeamTextView.setText(match.getHomeTeam());
            mMatchDateTextView.setText(match.getMatchDate());
            mAwayTeamTextView.setText(match.getAwayTeam());
        }
    }

    private class MatchAdapter extends RecyclerView.Adapter<MatchHolder> {

        private ArrayList<Match> mMatches;

        private MatchAdapter(ArrayList<Match> matches) {
            mMatches = matches;
        }

        @Override
        public MatchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.matches_list_item, parent, false);
            return new MatchHolder(view);
        }

        @Override
        public void onBindViewHolder(MatchHolder holder, int position) {
            holder.bindMatch(mMatches.get(position));
        }

        @Override
        public int getItemCount() {
            return mMatches.size();
        }
    }

    private String parseDate(Date matchDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(matchDate);
        return calendar.get(Calendar.YEAR) + "-" +
                calendar.get(Calendar.MONTH) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH);
    }
}
