package com.jenuine.task_a;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import de.ankri.views.Switch;

public class SettingsActivity extends Activity {

	private ListView listView;
	private ArrayAdapter<String> adapter;
	public static List<String> countries;
	private Button buttonAdd;
	private SharedPreferences preferences;
static{
	countries = new ArrayList<String>();
	populateList(countries);
}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		listView = (ListView) findViewById(R.id.listview);
		buttonAdd = (Button) findViewById(R.id.buttonadd);
		
		
		adapter = new ArrayAdapter<String>(this, R.layout.list_item, countries);
		listView.setAdapter(adapter);
		Switch switchA = (Switch) this.findViewById(R.id.switch_b);
		switchA.setTextOff("Disbale");
		switchA.setTextOn("Enable");
		switchA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					listView.setVisibility(View.VISIBLE);
					buttonAdd.setVisibility(View.VISIBLE);

				} else {
					listView.setVisibility(View.GONE);
					buttonAdd.setVisibility(View.GONE);
				}
			}
		});

		buttonAdd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				addCurrentLocation();
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int item,
					long arg3) {
				// TODO Auto-generated method stub
				System.out.println(countries.get(item));
			}
		});

	}

	private static void populateList(List<String> countries) {
		// TODO Auto-generated method stub
		countries.add("Palakkad,India");
		countries.add("Coimbatore,India");
		countries.add("Banglore,India");
		countries.add("Culcutta,India");
	}

	public void addCurrentLocation() {
		preferences = this.getSharedPreferences("MyPreferences",
				Context.MODE_PRIVATE);
		if (preferences.getString("address", "").length() > 0) {
			String location = preferences.getString("address", "");
			boolean isExists = false;
			Iterator<String> iterator = countries.iterator();
			while (iterator.hasNext()) {
				String loc = (String) iterator.next();
				if (loc.equals(location)) {
					isExists = true;
				}
			}
			if (!isExists) {
				countries.add(location);
				listView.invalidateViews();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		return super.onOptionsItemSelected(item);
	}

}
