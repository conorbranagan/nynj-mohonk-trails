package edu.newpaltz.nynjmohonk;

import android.os.Parcel;
import android.os.Parcelable;

public class Map implements Parcelable {
	private int id;
	private double bllat, bllon, trlat, trlon;
	private String name, ekey, fname, url;
	
	public Map() {
		// Something to do here?
	}
	
	public void setVal(int column, String val) {
		switch(column) {
			case 0: this.id = Integer.parseInt(val); break;
			case 1: this.name = val; break;
			case 2: this.ekey = val; break;
			case 3: this.fname = val; break;
			case 4: this.url = val; break;
			case 5: this.bllat = Double.parseDouble(val); break;
			case 6: this.bllon = Double.parseDouble(val); break;
			case 7: this.trlat = Double.parseDouble(val); break;
			case 8: this.trlon = Double.parseDouble(val); break;
		}
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEkey() {
		return ekey;
	}

	public String getFname() {
		return fname;
	}

	public String getUrl() {
		return url;
	}

	public double getBllat() {
		return bllat;
	}

	public double getBllon() {
		return bllon;
	}

	public double getTrlat() {
		return trlat;
	}

	public double getTrlon() {
		return trlon;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(name);
		out.writeString(ekey);
		out.writeString(fname);
		out.writeString(url);
		out.writeDouble(bllat);
		out.writeDouble(bllon);
		out.writeDouble(trlat);
		out.writeDouble(trlon);
	}
	
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
	
	private Map(Parcel in) {
		id = in.readInt();
		name = in.readString();
		ekey = in.readString();
		fname = in.readString();
		url = in.readString();
		bllat = in.readDouble();
		bllon = in.readDouble();
		trlat = in.readDouble();
		trlon = in.readDouble();
	}
	
}
