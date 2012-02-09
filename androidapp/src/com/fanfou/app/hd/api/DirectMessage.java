package com.fanfou.app.hd.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.fanfou.app.hd.App;
import com.fanfou.app.hd.db.Contents.BasicColumns;
import com.fanfou.app.hd.db.Contents.DirectMessageInfo;
import com.fanfou.app.hd.http.NetResponse;
import com.fanfou.app.hd.http.ResponseCode;
import com.fanfou.app.hd.service.Constants;
import com.fanfou.app.hd.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.04.30
 * @version 1.1 2011.05.02
 * @version 1.5 2011.05.20
 * @version 1.6 2011.07.22
 * @version 1.7 2011.10.21
 * @version 1.8 2011.11.04
 * @version 1.9 2011.11.21
 * @version 2.0 2011.11.23
 * @version 2.1 2011.12.01
 * @version 2.2 2011.12.16
 * @version 2.3 2011.12.19
 * @version 3.0 2011.12.21
 * @version 3.1 2011.12.23
 * 
 */
public class DirectMessage implements Storable<DirectMessage> {

	public static final String TAG = DirectMessage.class.getSimpleName();

	private static void log(String message) {
		Log.d(TAG, message);
	}

	public String id;
	public String ownerId;
	public String text;
	public Date createdAt;
	public String senderId;
	public String senderScreenName;
	public String recipientId;
	public String recipientScreenName;

	public String senderProfileImageUrl;
	public String recipientProfileImageUrl;

	public int type;

	public String threadUserId;
	public String threadUserName;
	public boolean isRead;

	public long realId;

	public User sender = null;
	public User recipient = null;

	public DirectMessage() {

	}

	public DirectMessage(Parcel in) {	
		id = in.readString();
		ownerId = in.readString();
		createdAt = new Date(in.readLong());
		type = in.readInt();
		
		senderId=in.readString();
		recipientId=in.readString();
		text=in.readString();
		
		senderScreenName=in.readString();
		recipientScreenName=in.readString();
		senderProfileImageUrl=in.readString();
		recipientProfileImageUrl=in.readString();
		
		threadUserId=in.readString();
		threadUserName=in.readString();
		
		isRead = in.readInt() == 0 ? false : true;
	}

	@Override
	public int compareTo(DirectMessage another) {
		return createdAt.compareTo(another.createdAt);
	}

	public boolean isNull() {
		return StringHelper.isEmpty(id);
	}

	public static ArrayList<DirectMessage> parseMessges(NetResponse r, int type)
			throws ApiException {
		JSONArray a = r.getJSONArray();
		return parseMessges(a, type);
	}

	public static ArrayList<DirectMessage> parseMessges(JSONArray a, int type)
			throws ApiException {
		if (a == null) {
			return null;
		}
		ArrayList<DirectMessage> dms = new ArrayList<DirectMessage>();
		try {
			for (int i = 0; i < a.length(); i++) {
				JSONObject o = a.getJSONObject(i);
				DirectMessage dm = parse(o, type);
				dms.add(dm);
			}
		} catch (JSONException e) {
			throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
					e.getMessage(), e);
		}
		return dms;
	}

	public static ArrayList<DirectMessage> parseConversationList(NetResponse response)
			throws ApiException {
		return parseConversationList(response.getJSONArray());
	}

	public static ArrayList<DirectMessage> parseConversationList(JSONArray a)
			throws ApiException {
		if (a == null) {
			return null;
		}
		ArrayList<DirectMessage> dms = new ArrayList<DirectMessage>();
		try {
			for (int i = 0; i < a.length(); i++) {
				JSONObject io = a.getJSONObject(i);
				JSONObject dmo = io.getJSONObject("dm");
				DirectMessage dm = parse(dmo, Constants.TYPE_DIRECT_MESSAGES_CONVERSTATION_LIST);
				dms.add(dm);
			}
		} catch (JSONException e) {
			throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
					e.getMessage(), e);
		}
		return dms;
	}

	public static List<DirectMessage> parseConversationUser(NetResponse response)
			throws ApiException {
		return parseConversationUser(response.getJSONArray());
	}

	public static List<DirectMessage> parseConversationUser(JSONArray a)
			throws ApiException {
		return parseMessges(a, Constants.TYPE_DIRECT_MESSAGES_CONVERSTATION);
	}

	public static DirectMessage parse(Cursor c) {
		if (c == null) {
			return null;
		}
		DirectMessage dm = new DirectMessage();
		dm.id = Parser.parseString(c, BasicColumns.ID);
		dm.ownerId = Parser.parseString(c, BasicColumns.OWNER_ID);
		dm.text = Parser.parseString(c, DirectMessageInfo.TEXT);
		dm.createdAt = Parser.parseDate(c, BasicColumns.CREATED_AT);
		dm.senderId = Parser.parseString(c, DirectMessageInfo.SENDER_ID);
		dm.senderScreenName = Parser.parseString(c,
				DirectMessageInfo.SENDER_SCREEN_NAME);
		dm.recipientId = Parser.parseString(c, DirectMessageInfo.RECIPIENT_ID);
		dm.recipientScreenName = Parser.parseString(c,
				DirectMessageInfo.RECIPIENT_SCREEN_NAME);
		dm.senderProfileImageUrl = Parser.parseString(c,
				DirectMessageInfo.SENDER_PROFILE_IMAGE_URL);
		dm.recipientProfileImageUrl = Parser.parseString(c,
				DirectMessageInfo.RECIPIENT_PROFILE_IMAGE_URL);

		dm.type = Parser.parseInt(c, BasicColumns.TYPE);

		dm.threadUserId = Parser.parseString(c,
				DirectMessageInfo.THREAD_USER_ID);
		dm.threadUserName = Parser.parseString(c,
				DirectMessageInfo.THREAD_USER_NAME);
		dm.isRead = Parser.parseBoolean(c, DirectMessageInfo.IS_READ);

		if (TextUtils.isEmpty(dm.id)) {
			return null;
		}

		return dm;
	}

	public static DirectMessage parse(NetResponse r, int type)
			throws ApiException {
		JSONObject o = r.getJSONObject();
		return parse(o, type);
	}

	public static DirectMessage parse(JSONObject o, int type)
			throws ApiException {
		if (o == null) {
			return null;
		}
		DirectMessage dm = null;
		try {
			dm = new DirectMessage();
			dm.id = o.getString(BasicColumns.ID);
			dm.realId = Parser.decodeMessageRealId(dm.id);
			dm.text = o.getString(DirectMessageInfo.TEXT);
			dm.createdAt = Parser.date(o.getString(BasicColumns.CREATED_AT));
			dm.senderId = o.getString(DirectMessageInfo.SENDER_ID);
			dm.senderScreenName = o
					.getString(DirectMessageInfo.SENDER_SCREEN_NAME);
			dm.recipientId = o.getString(DirectMessageInfo.RECIPIENT_ID);
			dm.recipientScreenName = o
					.getString(DirectMessageInfo.RECIPIENT_SCREEN_NAME);

			if (o.has("sender")) {
				JSONObject so = o.getJSONObject("sender");
				dm.sender = User.parse(so);
				dm.senderProfileImageUrl = dm.sender.profileImageUrl;
			}
			if (o.has("recipient")) {
				JSONObject so = o.getJSONObject("recipient");
				dm.recipient = User.parse(so);
				dm.recipientProfileImageUrl = dm.recipient.profileImageUrl;
			}

			dm.isRead = false;

			dm.type = type;

			dm.ownerId = App.getUserId();
		} catch (JSONException e) {
			throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
					e.getMessage(), e);
		}
		if (App.DEBUG)
			log("DirectMessage.parse id=" + dm.id);
		return dm;
	}

	@Override
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();

		cv.put(BasicColumns.ID, this.id);
		cv.put(BasicColumns.OWNER_ID, this.ownerId);
		cv.put(BasicColumns.CREATED_AT, this.createdAt.getTime());
		cv.put(BasicColumns.TYPE, this.type);

		cv.put(DirectMessageInfo.SENDER_ID, this.senderId);
		cv.put(DirectMessageInfo.RECIPIENT_ID, this.recipientId);
		cv.put(DirectMessageInfo.TEXT, this.text);

		cv.put(DirectMessageInfo.SENDER_SCREEN_NAME, this.senderScreenName);
		cv.put(DirectMessageInfo.RECIPIENT_SCREEN_NAME,
				this.recipientScreenName);

		cv.put(DirectMessageInfo.SENDER_PROFILE_IMAGE_URL,
				this.senderProfileImageUrl);
		cv.put(DirectMessageInfo.RECIPIENT_PROFILE_IMAGE_URL,
				this.recipientProfileImageUrl);


		cv.put(DirectMessageInfo.THREAD_USER_ID, this.threadUserId);
		cv.put(DirectMessageInfo.THREAD_USER_NAME, this.threadUserName);
		cv.put(DirectMessageInfo.IS_READ, this.isRead);

		return cv;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DirectMessage) {
			DirectMessage dm = (DirectMessage) o;
			if (id.equals(dm.id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "[Message] " + BasicColumns.ID + "=" + this.id + " "
				+ DirectMessageInfo.TEXT + "=" + this.text + " "
				+ BasicColumns.CREATED_AT + "=" + this.createdAt + " "
				+ DirectMessageInfo.SENDER_ID + "=" + this.senderId + " "
				+ DirectMessageInfo.RECIPIENT_ID + "=" + this.recipientId + " ";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(ownerId);
		dest.writeLong(createdAt.getTime());
		dest.writeInt(type);
		
		dest.writeString(senderId);
		dest.writeString(recipientId);
		dest.writeString(text);
		
		dest.writeString(senderScreenName);
		dest.writeString(recipientScreenName);
		dest.writeString(senderProfileImageUrl);
		dest.writeString(recipientProfileImageUrl);
		
		dest.writeString(threadUserId);
		dest.writeString(threadUserName);
		
		dest.writeInt(isRead?1:0);
		
	}

	public static final Parcelable.Creator<DirectMessage> CREATOR = new Parcelable.Creator<DirectMessage>() {

		@Override
		public DirectMessage createFromParcel(Parcel source) {
			return new DirectMessage(source);
		}

		@Override
		public DirectMessage[] newArray(int size) {
			return new DirectMessage[size];
		}
	};

}