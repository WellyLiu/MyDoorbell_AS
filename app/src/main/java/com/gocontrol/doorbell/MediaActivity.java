package com.gocontrol.doorbell;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MediaActivity extends Activity implements OnItemClickListener {

	private int index, type;
	private List<String> mFileNameList = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setHomeButtonEnabled(true);
		
		// initialize view
		mAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_expandable_list_item_1, mFileNameList);
		ListView listView = new ListView(this);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
		setContentView(listView);
		
		// get index from previous actvitiy
		Intent intent = getIntent();
		index = intent.getIntExtra("index", 0);
		type = intent.getIntExtra("type", 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// get file list from storage
		String door = "door" + index;
		String type = "pictures";
		if (this.type == 1) type = "clips";
		
		File dir = new File(getExternalFilesDir(null), door + File.separator + type);
		if (dir.exists()) {
			
			// get directory
			mFileNameList.clear();
			mFileNameList.addAll(Arrays.asList(dir.list()));
			
		} else {
			
			// directory is not existing
			dir.mkdirs();
			AppUtils.mediaScan(this, dir);
		}
		
		// update list
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		// get file
		String fileName = mFileNameList.get(position);
		String door = "door" + index;
		String type = "pictures";
		if (this.type == 1) type = "clips";
		File file = new File(getExternalFilesDir(null), door + File.separator + type + File.separator + fileName);
		
		// get mime type
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
			MimeTypeMap.getFileExtensionFromUrl((Uri.fromFile(file).toString())));
		
		// start correspond activity
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), mimeType);
			startActivity(intent);

		} catch (ActivityNotFoundException e) {
			
			// not start activity
			Toast.makeText(this, "open file fail", Toast.LENGTH_SHORT).show();
		}
	}
}
