package com.jenuine.task_a;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.google.android.maps.GeoPoint;

public class MainActivity extends Activity {
	final private int CAPTURE_IMAGE = 1;
	private String imgPath;
	private String selectedImagePath = null;
	GPSTracker tracker;
	private Button upload;
	int serverResponseCode = 0;
	ProgressDialog dialog = null;
	private String upLoadServerUri = "http://jenuin.in/jenu/upload.php";
	// String upLoadServerUri
	// ="http://vikatanereader.ephronsystems.com/test/upload/upload.php";
	private Context context;
	private Bitmap myBitmap;
	private Button capture;
	private Geocoder gCoder;
	protected List<Address> addresses;
	private boolean PROGRESS_OVER;
	private SharedPreferences preferences;
	private ConnectionDetector connectionDetector;
	private static String TEXT = "", ADDRESS = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		capture = (Button) findViewById(R.id.buttonCapture);
		upload = (Button) findViewById(R.id.buttonUploader);
		tracker = new GPSTracker(this);
		gCoder = new Geocoder(this);
		preferences = this.getSharedPreferences("MyPreferences",
				Context.MODE_PRIVATE);

		connectionDetector = new ConnectionDetector(context);

		/*
		 * if (location != null)
		 * System.out.println("Network "+location.getLatitude() + ":" +
		 * location.getLongitude()); else System.out.println("loc null");
		 */
		if (!connectionDetector.isConnectingToInternet()) {
			showMessage("Message");
		} else {

			if (preferences.getString("address", "").trim().length() <= 0) {
			final ProgressDialog progressDialog = new ProgressDialog(context);
			progressDialog.setCancelable(false);
			progressDialog.setMessage("Loading....");
			progressDialog.show();

			Thread mThread = new Thread() {
				@Override
				public void run() {
					try {
						synchronized (this) {

							if (tracker.canGetLocation()) {
								System.out.println(tracker.getLatitude() + " "
										+ tracker.getLongitude());

								/**
								 * * String add=ConvertPointToLocation(new
								 * GeoPoint((int)tracker.getLatitude(),
								 * (int)tracker.getLongitude()));
								 * Toast.makeText(context, add,
								 * Toast.LENGTH_SHORT).show();
								 */

								try {
									addresses = gCoder.getFromLocation(
											tracker.getLatitude(),
											tracker.getLongitude(), 2);

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

							// Wait given period of time or exit on touch
							wait(3000);

						}
					} catch (InterruptedException ex) {
					}

					// finish();

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (addresses != null && addresses.size() > 0) {
								Toast.makeText(
										getApplicationContext(),
										"You are in : "
												+ addresses.get(0)
														.getAddressLine(0)
												+ ","
												+ addresses.get(0)
														.getCountryName(),
										Toast.LENGTH_LONG).show();
								ADDRESS = addresses.get(0).getAddressLine(0)
										+ ","
										+ addresses.get(0).getCountryName();

								SharedPreferences.Editor editor = preferences
										.edit();
								editor.putString("address", ADDRESS);
								editor.commit();

								progressDialog.dismiss();
							} else {
								if (preferences.getString("address", "")
										.length() > 0) {
									Toast.makeText(
											getApplicationContext(),
											"Location Un identified App Quiting",
											Toast.LENGTH_LONG).show();
									progressDialog.dismiss();
									finish();
								} else {
									ADDRESS = preferences.getString("address",
											"");
									Toast.makeText(getApplicationContext(),
											"You are in : " + ADDRESS,
											Toast.LENGTH_LONG).show();
								}

							}
						}
					});
				}
			};
			mThread.start();

		}
		else{
			ADDRESS = preferences.getString("address",
					"");
			System.out.println(ADDRESS);
		}
		}

		capture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (checkGeofence(ADDRESS)) {

					final Intent intent = new Intent(
							MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
					startActivityForResult(intent, CAPTURE_IMAGE);
				} else {
					showMessage();
				}
			}
		});
		upload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (selectedImagePath != null)
					new Uploader().execute(selectedImagePath);

				/*
				 * 
				 * new Thread(new Runnable() { public void run() {
				 * runOnUiThread(new Runnable() { public void run() {
				 * System.out.println("uploading..."); } });
				 * 
				 * try {
				 * 
				 * MyHttpClient.executeMultipartPostFile( upLoadServerUri,
				 * selectedImagePath);
				 * 
				 * 
				 * 
				 * dialog.dismiss(); } catch (Exception e) { // TODO
				 * Auto-generated catch block e.printStackTrace(); }
				 * 
				 * } }).start();
				 */}
		});

	}

	/*
	 * @Override protected void onStart() {
	 * 
	 * if (!connectionDetector.isConnectingToInternet()) {
	 * showMessage("Message"); } // TODO Auto-generated method stub
	 * super.onStart(); }
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	private void showMessage(String string) {
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
				.create();

		// Setting Dialog Title
		alertDialog.setTitle("Taska info");

		// Setting Dialog Message
		alertDialog.setMessage(" Internet not available ");

		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.ic_launcher);

		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	void showMessage() {
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
				.create();

		// Setting Dialog Title
		alertDialog.setTitle("Taska info");

		// Setting Dialog Message
		alertDialog
				.setMessage("Your location is not included in the app ,Please goto settings and add your location and enjoy the app  ");

		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.ic_launcher);

		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(context, SettingsActivity.class);
				startActivity(intent);
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	protected boolean checkGeofence(String location) {
		// TODO Auto-generated method stub

		List<String> list = SettingsActivity.countries;

		Iterator<String> iterator = list.iterator();
		boolean isExists = false;
		while (iterator.hasNext()) {
			String loc = (String) iterator.next();
			if (loc.equals(location)) {
				isExists = true;
			}
		}

		return isExists;

	}

	class Uploader extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			super.onPreExecute();
			dialog = ProgressDialog.show(MainActivity.this, "",
					"Uploading file...", true);
		}

		@Override
		protected JSONObject doInBackground(String... url) {
			// TODO Auto-generated method stub
			Map config = new HashMap();
			config.put("cloud_name", "jenuin-in");
			config.put("api_key", "428779592986786");
			config.put("api_secret", "yw-ZJRC69t7Kli6cLwjHJtAF1hE");
			Cloudinary cloudinary = new Cloudinary(config);
			// Cloudinary cloudinary = new
			// Cloudinary("cloudinary://123456789012345:abcdeghijklmnopqrstuvwxyz12@n07t21i7");
			// String root_sd = Environment.getExternalStorageDirectory()
			// .toString();
			// File file = new File(root_sd + "/DCIM/" + "img.png");
			FileInputStream inputStream;
			try {
				Map options = new HashMap();
				inputStream = new FileInputStream(url[0]);
				JSONObject respose = cloudinary.uploader().upload(inputStream,
						Cloudinary.emptyMap());
				return respose;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			upload.setVisibility(View.GONE);
			capture.setVisibility(View.VISIBLE);
			try {
				System.out.println(result.get("url"));
				if (result != null) {
					String url = result.getString("url");
					System.out.println(url);
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(url));
					startActivity(browserIntent);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}
	}

	// http://jenuin.in/upload.php
	public String ConvertPointToLocation(GeoPoint point) {
		String address = "";
		Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocation(
					point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6,
					1);

			if (addresses.size() > 0) {
				for (int index = 0; index < addresses.get(0)
						.getMaxAddressLineIndex(); index++)
					address += addresses.get(0).getAddressLine(index) + " ";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return address;
	}

	public Uri setImageUri() {
		// Store image in dcim
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh-mm-ss");
		SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
		// String fileName = dateFormat.format(new Date()) + ".jpg"

		TEXT = "TAKEN ON " + dateFormat.format(new Date()) + " TIME ON "
				+ displayFormat.format(new Date()) + " AT " + ADDRESS;

		File file = new File(Environment.getExternalStorageDirectory()
				+ "/DCIM/", "image-" + dateFormat.format(new Date()) + "-"
				+ timeFormat.format(new Date()) + ".png");
		Uri imgUri = Uri.fromFile(file);
		this.imgPath = file.getAbsolutePath();
		return imgUri;
	}

	public String getImagePath() {
		return imgPath;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_CANCELED) {
			if (requestCode == CAPTURE_IMAGE) {

				selectedImagePath = getImagePath();
				if (selectedImagePath == null)
					Toast.makeText(getApplicationContext(), "Please Try Again",
							Toast.LENGTH_SHORT).show();
				else {

					capture.setVisibility(View.GONE);
					upload.setVisibility(View.VISIBLE);
					System.out.println("this is path of img"
							+ selectedImagePath);

					myBitmap = decodeFile(selectedImagePath);

					processImage(selectedImagePath, myBitmap);
					Bitmap myBitmap2 = resize(selectedImagePath);

					watermarkText(myBitmap2, TEXT);
					myBitmap = null;

				}
			} else {
				super.onActivityResult(requestCode, resultCode, data);
			}
		}

	}

	private Bitmap resize(String picturePath) {
		// TODO Auto-generated method stub
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, options);
		int width = options.outWidth;
		int height = options.outHeight;

		// String filepath = Environment.getExternalStorageDirectory() +
		// "/file.png";
		File file = new File(picturePath);
		long length = file.length();
		float len1 = length / 1024;
		String type = options.outMimeType;
		Log.e("FilePath", "FilePath:" + picturePath);
		Log.e("type", "type" + type);
		Log.e("height", "height" + Integer.toString(height));
		Log.e("width", "width" + Integer.toString(width));
		Log.e("length", "length" + Float.toString(len1));
		if (height > 150 && width > 150) {
			float len = (length / 1024) / 1024;
			if (len < 3) {
				Bitmap bm = BitmapFactory.decodeFile(picturePath);
				width = (int) (bm.getWidth() / 1.7);
				height = (int) (bm.getHeight() / 1.7);
				Bitmap new_bm = Bitmap.createScaledBitmap(bm, width, height,
						true);
				try {

//					File file2 = new File(
//							Environment.getExternalStorageDirectory()
//									+ "/DCIM/", "image-" + ".png");

					new_bm.compress(Bitmap.CompressFormat.JPEG, 80,
							new FileOutputStream(getImagePath()));

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				bm = null;
				return new_bm;
			} else {
				Toast.makeText(getApplicationContext(), "image is above 3MB",
						Toast.LENGTH_SHORT).show();

			}

		} else {
			Toast.makeText(getApplicationContext(),
					"Choose 150*150 or above Resolution image",
					Toast.LENGTH_SHORT).show();

		}
		return null;

	}

	private Bitmap decodeFile(String f) {
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 70;

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();

		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;

		float scaleHeight = ((float) newHeight) / height;

		// CREATE A MATRIX FOR THE MANIPULATION

		Matrix matrix = new Matrix();

		// RESIZE THE BIT MAP

		matrix.postScale(scaleWidth, scaleHeight);

		// RECREATE THE NEW BITMAP

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width / 2,
				height / 2, matrix, false);

		return resizedBitmap;

	}

	private Bitmap processImage(String yourimagepath, Bitmap bmpPic) {
		// TODO Auto-generated method stub
		Matrix mat = new Matrix();

		ExifInterface exif = null;
		try {
			exif = new ExifInterface(yourimagepath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String orientstring = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
		int orientation = orientstring != null ? Integer.parseInt(orientstring)
				: ExifInterface.ORIENTATION_NORMAL;
		int rotateangle = 0;
		if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
			rotateangle = 90;
		if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
			rotateangle = 180;
		if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
			rotateangle = 270;

		mat.setRotate(rotateangle, (float) bmpPic.getWidth() / 2,
				(float) bmpPic.getHeight() / 2);

		File f = new File(yourimagepath);
		try {
			bmpPic = BitmapFactory.decodeStream(new FileInputStream(f), null,
					null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Bitmap bmpPic1 = Bitmap.createBitmap(bmpPic, 0, 0, bmpPic.getWidth(),
				bmpPic.getHeight(), mat, true);

		try {
			bmpPic1.compress(Bitmap.CompressFormat.JPEG, 80,
					new FileOutputStream(selectedImagePath));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return bmpPic1;
	}

	Bitmap watermarkText(Bitmap src, String text) {

		int w = src.getWidth();
		int h = src.getHeight();
		System.out.println(w + ":" + h);
		Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(src, 0, 0, null);
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setTextSize(23);
		paint.setAntiAlias(true);
		// paint.setUnderlineText(true);
		canvas.drawText(text, 20, 25, paint);
		try {
			result.compress(Bitmap.CompressFormat.JPEG, 100,
					new FileOutputStream(getImagePath()));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return result;

	}

	public String getAbsolutePath(Uri uri) {
		String[] projection = { MediaColumns.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


}
