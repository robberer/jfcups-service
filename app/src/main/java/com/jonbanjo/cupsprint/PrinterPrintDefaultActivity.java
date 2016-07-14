package com.jonbanjo.cupsprint;

/*
JfCupsPrintService
Copyright (C) 2014 Jon Freeman

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;

import com.jonbanjo.cupsprintservice.R;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class PrinterPrintDefaultActivity extends Activity {

	ListView printersListView;
	ArrayList<String> printersArray;
	Uri jobUri;
	String mimeType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final PrintQueueIniHandler ini = new PrintQueueIniHandler(getBaseContext());
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		/*
        Print SMS - @robert
		 */
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (Intent.ACTION_SEND.equals(action) && extraText != null && extraText.equals("printSMS")) {
            jobUri = Uri.parse("file:///storage/emulated/legacy/last-sms.txt");
            mimeType = "text/plain";
        }
        /*
		Print SMS - @robert - END
		 */

		if (Intent.ACTION_SEND.equals(action) && type != null) {
		        jobUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
				mimeType = intent.getType();
		}
		if (jobUri == null){
			String toast = "No printable document found";
            Toast.makeText(PrinterPrintDefaultActivity.this, toast, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		printersArray = ini.getPrintQueueConfigs();
		if (printersArray.size() == 0){
			 Intent noprinterintent = new Intent(this, PrinterMainActivity.class);
		     startActivity(noprinterintent);
			 finish();
			 return;
		}
		String printer = ini.getDefaultPrinter();
		if (!printer.equals("")){
			doPrintJob(printer);
			finish();
			return;
		}
		if (printersArray.size() ==1){
			String printername = printersArray.get(0);
			ini.setDefaultPrinter(printername);
			doPrintJob(printername);
			finish();
			return;
		}
		setContentView(R.layout.activity_printer_print_default);
		printersListView=(ListView) findViewById(R.id.printersPrintDefaultView);
		registerForContextMenu(printersListView);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, printersArray);
			printersListView.setAdapter(aa);
		printersListView.setClickable(true);
		printersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if (jobUri == null)
					return;
				String printer = (String) printersListView.getItemAtPosition(position);
				ini.setDefaultPrinter(printer);
				doPrintJob(printer);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.about:
	    		Intent intent = new Intent(this, AboutActivity.class);
	    		intent.putExtra("printer", "");
	    		startActivity(intent);
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == 500){
			finish();
		}
	}
	
	private void doPrintJob(String printer){
		Intent sendIntent = new Intent(this, PrintJobActivity.class);
		sendIntent.putExtra("type", "static");
		sendIntent.putExtra("printer", printer);
		sendIntent.putExtra("mimeType", mimeType);
		sendIntent.setData(jobUri);
		this.startActivityForResult(sendIntent, 500);
		}
	

}
