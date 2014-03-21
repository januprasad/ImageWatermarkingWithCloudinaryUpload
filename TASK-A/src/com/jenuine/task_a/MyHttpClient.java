package com.jenuine.task_a;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class MyHttpClient {

	/** The time it takes for our client to timeout */
	public static final int HTTP_TIMEOUT = 3 * 1000; // milliseconds

	private static final String serverPath = "http://192.168.2.9:8080/FTP_Server/";

	/** Single instance of our HttpClient */
	private static HttpClient mHttpClient;
	Context context;

	public MyHttpClient(Context context) {
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get our single instance of our HttpClient object.
	 * 
	 * @param i
	 * 
	 * @return an HttpClient object with connection parameters set
	 */

	public static String downloader(int id, String FILE_URL) {

		try {
			URL u = new URL(serverPath + "files/" + id + "/" + FILE_URL);
			InputStream is = u.openStream();

			DataInputStream dis = new DataInputStream(is);

			byte[] buffer = new byte[1024];
			int length;

			File file = new File(FILE_URL);
			Log.i("RootFileList", file.getName());

			FileOutputStream fos = new FileOutputStream(new File(
					Environment.getExternalStorageDirectory() + "/"
							+ makeDirifNotExists() + "/" + file.getName()));
			while ((length = dis.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}

		} catch (MalformedURLException mue) {
			Log.e("SYNC getUpdate", "malformed url error", mue);
		} catch (IOException ioe) {
			Log.e("SYNC getUpdate", "io error", ioe);
		} catch (SecurityException se) {
			Log.e("SYNC getUpdate", "security error", se);
		}
		return "/mnt/sdcard/" + makeDirifNotExists() + "/" + FILE_URL;

	}

	private static String makeDirifNotExists() {
		String dirName = "AndFTP";
		File dir = new File(Environment.getExternalStorageDirectory() + "/"
				+ dirName);

		if (!dir.isDirectory()) {
			dir.mkdirs();
		}

		// TODO Auto-generated method stub
		return dirName;
	}

	private static HttpClient getHttpClient() {

		if (mHttpClient == null) {
			mHttpClient = new DefaultHttpClient();
			final HttpParams params = mHttpClient.getParams().setParameter(
					CoreProtocolPNames.USER_AGENT, "Device");

			HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
			ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
		}
		return mHttpClient;
	}

	/**
	 * Performs an HTTP Post request to the specified url with the specified
	 * parameters.
	 * 
	 * @param urlServlet
	 *            The web address to post the request to
	 * @param postParameters
	 *            The parameters to send via the request
	 * @return The result of the request
	 * @throws Exception
	 */
	public String executeHttpPost(String urlServlet,
			ArrayList<NameValuePair> postParameters) throws Exception {
		BufferedReader in = null;

		try {
			HttpClient client = getHttpClient();
			HttpPost request = new HttpPost(serverPath + urlServlet);
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
					postParameters);

			request.setEntity(formEntity);

			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (Exception e) {
				Toast.makeText(context, "Server Not Found", Toast.LENGTH_SHORT)
						.show();
				e.printStackTrace();
			}

			if (response != null) {
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));

				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();

				String result = sb.toString();
				return result;
			} else {
				return "";
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					return "";
				}
			}
		}
	}

	/**
	 * Performs an HTTP GET request to the specified url.
	 * 
	 * @param url
	 *            The web address to post the request to
	 * @return The result of the request
	 * @throws Exception
	 */
	public static String executeHttpGet(String url) throws Exception {
		BufferedReader in = null;
		try {
			HttpClient client = getHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();

			String result = sb.toString();
			return result;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();

				}
			}
		}
	}

	public static String executeMultipartPostFile(String URL, String file)
			throws Exception {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost( URL);
		Log.i("Logging", "executeMultipartPostFile");

		FileBody bin = new FileBody(new File(file));
/*		StringBody shareFileBody = new StringBody(domain.getSharestatus() + "");
		StringBody encryptFileBody = new StringBody(domain.getEncryptstatus()
				+ "");
		StringBody folderFileBody = new StringBody(domain.getFolder() + "");
*/	


		MultipartEntity reqEntity = new MultipartEntity();

/*		reqEntity.addPart("userId", useridFileBody);
		reqEntity.addPart("shareFile", shareFileBody);
		reqEntity.addPart("encryptFile", encryptFileBody);
		reqEntity.addPart("folderName", folderFileBody);*/
		reqEntity.addPart("bin", bin);
		httppost.setEntity(reqEntity);

		HttpResponse response = httpclient.execute(httppost);
		String responseContent = parseHttpResponse(response);
		Log.i("CONTENT", responseContent);
		return responseContent.trim();
	}

	public static String parseHttpResponse(HttpResponse response) {
		// TODO Auto-generated method stub

		BufferedReader in = null;
		if (response != null) {
			try {
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));

				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();

				String result = sb.toString();
				return result;
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					return "";
				}
			}

		} else {
			return "";
		}

		return null;
	}

	public static String executeMultipartPostImage(String FILEURL, String URL)
			throws Exception {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(serverPath + URL);
		Log.i("Logging", "executeMultipartPostFile");

		FileBody bin = new FileBody(new File(FILEURL));

		/*StringBody useridFileBody = new StringBody(
				LoginDataService.domain.getId() + "");*/

		MultipartEntity reqEntity = new MultipartEntity();

		/*reqEntity.addPart("userId", useridFileBody);*/
		reqEntity.addPart("bin", bin);
		httppost.setEntity(reqEntity);

		HttpResponse response = httpclient.execute(httppost);
		String responseContent = parseHttpResponse(response);
		Log.i("CONTENT", responseContent);
		return responseContent.trim();

	}

}
