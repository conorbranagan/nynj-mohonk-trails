package edu.newpaltz.nynjmohonk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Map is an object which is mapped to a particular row in the Map table
 */
public class Map implements Parcelable {
	private int imageLoadState;
	private int id;
	private double min_longitude, max_latitude, lon_per_pixel, lat_per_pixel;
	private String name, ekey, fname, url;
	private Context myContext;
	private Bitmap bm;
	private byte[] cipherFile, clearFile, b;
	private ByteArrayOutputStream baos;
	private File f;
	private FileInputStream fis = null;
	private TEA file; 
	
	/**
	 * Returns a map object with the given applcaition context
	 */
	public Map(Context c) {
		imageLoadState = 0;
		myContext = c;
	}
	
	/**
	 * Sets a value of this instance based on the column number in the SQLite database.
	 * Probably not the best method of doing this because as the database schema changes, so does
	 * this method. 
	 * @param column The column number of the table
	 * @param val A String value to assign to the selected value
	 */
	public void setVal(int column, String val) {
		switch(column) {
			case 0: this.id = Integer.parseInt(val); break;
			case 1: this.name = val; break;
			case 2: this.ekey = val; break;
			case 3: this.fname = val; break;
			case 4: this.url = val; break;
			default: break;
		}
	}
	
	
	/**
	 * Sets a value based on the column number. Overloaded method, uses a double value in this case. See other
	 * method for more comments
	 * @param column The column number of the table
	 * @param val The double value to assign
	 */
	public void setVal(int column, double val) {
		switch(column) {
			case 5: this.min_longitude = val; break;
			case 6: this.max_latitude = val; break;
			case 7: this.lat_per_pixel = val; break;
			case 8: this.lon_per_pixel = val; break;
			default: break;
		}
	}
	
	/**
	 * @return The unique id of the row for this map
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return The name/title of the map
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The encryption key for the map file
	 */
	public String getEkey() {
		return ekey;
	}

	/**
	 * @return The filename of the map when it's stored on the phone
	 */
	public String getFname() {
		return fname;
	}

	/**
	 * @return The URL of the map image where it will be downloaded from
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return The minimum longitude covered by the map image (from the world file)
	 */
	public double getMinLongitude() { 
		return min_longitude;
	}
	
	/**
	 * @return The maximum latitude covered by the map image (from the world file)
	 */
	public double getMaxLatitude() {
		return max_latitude;
	}
	
	// Maximum longitude and minimum latitude are calculated later on
	
	/**
	 * @return The latitude amount per pixel in the image (from the world file)
	 */
	public double getLatPerPixel() {
		return lat_per_pixel;
	}
	
	/**
	 * @return The longitude amount per pixel in the image (from the world file)
	 */
	public double getLonPerPixel() {
		return lon_per_pixel;
	}
	
	public int getImageLoadState() { 
		return imageLoadState;
	}
	
	/**
	 * Generate filename from the image URL
	 */
	public String getFilename() {
		return url.substring(url.lastIndexOf('/') + 1);
	}

	/**
	 * Unused method that is required by Parcable type
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * A parcel is a faster Serializable type, so as such we are writing out are data into a format
	 * that can be moved around quickly
	 */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(name);
		out.writeString(ekey);
		out.writeString(fname);
		out.writeString(url);
		out.writeDouble(min_longitude);
		out.writeDouble(max_latitude);
		out.writeDouble(lat_per_pixel);
		out.writeDouble(lon_per_pixel);
	}
	
	/**
	 * Download image, if needed, from the URL of this instance into our data/data folder and
	 * then encrypt the image file
	 */
	public void loadImage() {
		Bitmap loadedImage = null;
		try {
			// File exists, set image load state as loaded
			myContext.openFileInput(getFilename());
			imageLoadState = 1; // image is done loading!
			return;
		} catch (FileNotFoundException e) {
			imageLoadState = 3;
			// File does not exist, attempt to load from URL
			URL myURL = null;
			try {
				myURL = new URL(url);
			} catch (MalformedURLException me) {
				imageLoadState = 2; // error loading/downloading image
			}
			
			HttpGet httpRequest = null;
			
			// Download the image from the URL given
			try {
				httpRequest = new HttpGet(myURL.toURI());
			} catch (URISyntaxException exp) {
				imageLoadState = 2;
			}
			
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = (HttpResponse)httpClient.execute(httpRequest);
				HttpEntity entity = response.getEntity();
				BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
				InputStream instream = bufHttpEntity.getContent();
				loadedImage = BitmapFactory.decodeStream(instream);
				baos = new ByteArrayOutputStream();
	        	loadedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
	        	b = baos.toByteArray();
				encryptImage(b);
				instream.close();
			} catch (Exception exp) {
				Log.d("DEBUG", "Error loading image 1");
				Log.d("DEBUG", exp.getMessage());
			}			
			
			// Write the file out to disk as a bitmap (compressed as a JPEG file)
			try {
				FileOutputStream f = myContext.openFileOutput(getFilename(), Context.MODE_WORLD_READABLE);
				loadedImage.compress(CompressFormat.JPEG, 90, f);
				f.flush();
				f.close();
				//encryptImage();
			} catch (Exception exp) {
				Log.d("DEBUG", exp.toString());
				Log.d("DEBUG", "Error loading image 2");
				imageLoadState = 2;
				return;
			}
			
		}
		imageLoadState = 1; // 1 = image fully downloaded and loaded
	}
	
	/**
	 * Encrypt the bitmap file before writing it out to disk
	 */
	private void encryptImage(byte[] b) {
		file = new TEA("This is the passphrase".getBytes());
        
        cipherFile = file.encrypt(b);
	}
	
	/**
	 * Decrypt the image so that it can be read in as a bitmap
	 */
	private void decryptImage() {
		clearFile = file.decrypt(cipherFile);
	}
	
	/**
	 * Required by Parceable type. An inner class that creates a parcable from the Map object
	 */
	public static final Parcelable.Creator<Map> CREATOR = new Parcelable.Creator<Map>() {

		@Override
		public Map createFromParcel(Parcel source) {
			return new Map(source);
		}

		@Override
		public Map[] newArray(int size) {
			return new Map[size];
		}
		
	};
	
	/**
	 * A private constructor used in the Parcable inner class to create a map from a Parcable object
	 */
	private Map(Parcel in) {
		id = in.readInt();
		name = in.readString();
		ekey = in.readString();
		fname = in.readString();
		url = in.readString();
		min_longitude = in.readDouble();
		max_latitude = in.readDouble();
		lat_per_pixel = in.readDouble();
		lon_per_pixel = in.readDouble();
	}
	
}
