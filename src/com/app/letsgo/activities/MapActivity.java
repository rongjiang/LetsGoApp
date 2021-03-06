package com.app.letsgo.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.app.letsgo.R;
import com.app.letsgo.fragments.BaseMapFragment;
import com.app.letsgo.fragments.CardListFragment;
import com.app.letsgo.fragments.LogoutFragment;
import com.app.letsgo.fragments.MapViewFragment;
import com.app.letsgo.fragments.NotificationFragment;
import com.app.letsgo.fragments.SystemSettingFragment;
import com.app.letsgo.models.LocalEvent;
import com.app.letsgo.models.LocalEventParcel;

public class MapActivity extends ActionBarActivity {

	ArrayList<LocalEventParcel> events;
	Menu menu;
	private FragmentNavigationDrawer dlDrawer;

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		setupNavDrawer(savedInstanceState);
		Log.d("debug", "MapActivity.onCreate(): setupNavDrawer. ");
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean value = super.onCreateOptionsMenu(menu);
		setQueryTextListener(menu);
		setupNearbyLocationListener(menu);
		return value;
	}

	/**
	 * Search local events based on the {@code query} string
	 * @param query user entered query string
	 */
	public void setQueryTextListener(Menu menu) {
		Log.d("debug", "MapActivity do search...");
		this.menu = menu;
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setQueryHint("Search events");
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
            	Log.d("debug", "searchEvents(): query = " + query);
            	events = LocalEventParcel.search(mCurrentLocation, MapActivity.this, query);
            	getMapViewFragment().setEvents(events);
            	setNearByLocationInvisible();
            	return true;
			}
			@Override
			public boolean onQueryTextChange(String text) {
				return false;
			}
		});
	}
	
	public void setupNearbyLocationListener(Menu menu) {
		MenuItem searchItem = menu.findItem(R.id.action_search);

		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
			
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				setNearByLocationVisible();
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				setNearByLocationInvisible();
				return true;
			}
		});
	}
	
	private void setNearByLocationVisible() {
		LinearLayout llNearBy = (LinearLayout) findViewById(R.id.llNearBy);
		TextView tvNearBy = (TextView) findViewById(R.id.tvNearByLocation);
		
		tvNearBy.setText(nearByArea);
		llNearBy.setVisibility(View.VISIBLE);		
	}
	
	private void setNearByLocationInvisible() {
		LinearLayout llNearBy = (LinearLayout) findViewById(R.id.llNearBy);
		TextView tvNearBy = (TextView) findViewById(R.id.tvNearByLocation);		
		llNearBy.setVisibility(View.INVISIBLE);	
		Log.d("debug", "MapActivity.setNearByLocaitonInvisible...");
	}
		
	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case BaseMapFragment.CONNECTION_FAILURE_RESOLUTION_REQUEST:
			if(resultCode == Activity.RESULT_OK){
				// mapFragment.connect();
			}
			break;
		case ActionBarActivity.CREATE_EVENT_REQUEST_CODE:
			if(resultCode == Activity.RESULT_OK){
				LocalEvent event = data.getParcelableExtra("event");
				events.add(new LocalEventParcel(event));	//add local event
				getMapViewFragment().addEvent(new LocalEventParcel(event));
				
			}
			break;
		case ActionBarActivity.SETTINGS_REQUEST_CODE:
			if(resultCode == Activity.RESULT_OK){
				//re-run the query
				MenuItem searchItem = menu.findItem(R.id.action_search);
				SearchView sView = (SearchView) searchItem.getActionView();
				String query = (String) sView.getQuery().toString();
				events = LocalEventParcel.search(mCurrentLocation, MapActivity.this, query);
            	getMapViewFragment().setEvents(events);
			}
			break;
		}
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		super.onConnected(bundle);
		// setNearByLocation();
		Log.d("debug", "MapActivity.OnConnected: " + mCurrentLocation);
		getBaseMapFragment().setCurrentLocation(mCurrentLocation);
		events = LocalEventParcel.search(mCurrentLocation, this, null);		
		
	}
	
	void setupNavDrawer(Bundle savedInstanceState) {        
        // for Navigation Drawer, find our drawer view
		dlDrawer = (FragmentNavigationDrawer) findViewById(R.id.drawer_layout);
		// Setup drawer view
		dlDrawer.setupDrawerConfiguration((ListView) findViewById(R.id.lvDrawer), 
                     R.layout.drawer_nav_item, R.id.fragmentContainer);
		
		// Add nav items
		dlDrawer.addNavItem("Local Events", "Let's Go!", MapViewFragment.class);
		// dlDrawer.addNavItem("New Event", "New Event", NewEventFragment.class);
		dlDrawer.addNavItem("Settings", "Settings", SystemSettingFragment.class);
		dlDrawer.addNavItem("Notifications", "Notifications", NotificationFragment.class);
		dlDrawer.addNavItem("Logout", "Log Out", LogoutFragment.class);
		
		// Select default
		if (savedInstanceState == null) {
			dlDrawer.selectDrawerItem(0);	
		}
		
	} 
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		if (dlDrawer.isDrawerOpen()) {
			// menu.findItem(R.id.mi_test).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (dlDrawer.getDrawerToggle().onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		dlDrawer.getDrawerToggle().syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		dlDrawer.getDrawerToggle().onConfigurationChanged(newConfig);
	}
	
	private BaseMapFragment getBaseMapFragment() {
		return getMapViewFragment().getBaseMapFragment();
	}
	
	private CardListFragment getListFragment() {
		return getMapViewFragment().getListFragment();
	}
	
	private MapViewFragment getMapViewFragment() {
		return (MapViewFragment) dlDrawer.getFragment(0);
	}
 
}