package com.gocontrol.doorbell;

import java.util.Locale;

import org.apache.commons.codec.binary.Hex;

import android.os.Parcel;
import android.os.Parcelable;

public class RingEvent implements Parcelable {

	private String peerId;
	private int sessionCode;
	private long time;
	private int lineId;
	
	private RingEvent(Parcel source) {
		peerId = source.readString();
		sessionCode = source.readInt();
		time = source.readLong();
		lineId = source.readInt();
	}
	
	public RingEvent(String peerId, int sessionCode, long time) {
		if (peerId == null) peerId = "";
		this.peerId = peerId;
		this.sessionCode = sessionCode;
		this.time = time;
		lineId = Integer.MIN_VALUE;
	}
	
	public String getPeerId() {
		return peerId;
	}
	
	public RingEvent setPeerId(String peerId) {
		if (peerId == null) peerId = "";
		this.peerId = peerId;
		return this;
	}
	
	public int getSessionCode() {
		return sessionCode;
	}
	
	public RingEvent setSessionCode(int sessionCode) {
		this.sessionCode = sessionCode;
		return this;
	}
	
	public long getTime() {
		return time;
	}
	
	public RingEvent setTime(long time) {
		this.time = time;
		return this;
	}
	
	public int getLineId() {
		return lineId;
	}
	
	public RingEvent setLineId(int lineId) {
		this.lineId = lineId;
		return this;
	}
	
	public static final Parcelable.Creator<RingEvent> CREATOR = new Creator<RingEvent>() {
		
		@Override
		public RingEvent[] newArray(int size) {
			return new RingEvent[size];
		}
		
		@Override
		public RingEvent createFromParcel(Parcel source) {
			return new RingEvent(source);
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(peerId);
		dest.writeInt(sessionCode);
		dest.writeLong(time);
		dest.writeInt(lineId);
	}
	
	@Override
	public int hashCode() {
		return Integer.valueOf(
			new String(Hex.encodeHex(
				peerId.toLowerCase(Locale.getDefault()).getBytes())));
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof RingEvent) {
			if (((RingEvent) o).getPeerId().equalsIgnoreCase(peerId))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		String msg = "'" + RingEvent.class.getSimpleName() + "' class";
		msg += "\n>> peerId = " + peerId;
		msg += "\n>> sessionCode = " + sessionCode;
		msg += "\n>> time = " + time;
		return msg;
	}
}
