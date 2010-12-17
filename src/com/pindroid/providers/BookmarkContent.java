/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.pindroid.providers;

import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import com.pindroid.providers.TagContent.Tag;
import com.pindroid.util.DateParser;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class BookmarkContent {

	public static class Bookmark implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				BookmarkContentProvider.AUTHORITY + "/bookmark");
		
		public static final  String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.PinDroid.bookmarks";
		
		public static final String Account = "ACCOUNT";
		public static final String Description = "DESCRIPTION";
		public static final String Url = "URL";
		public static final String Notes = "NOTES";
		public static final String Tags = "TAGS";
		public static final String Hash = "HASH";
		public static final String Meta = "META";
		public static final String Time = "TIME";
		public static final String LastUpdate = "LASTUPDATE";
		
		private int mId = 0;
		private String mAccount = null;
        private String mUrl = null;
        private String mDescription = null;
        private String mNotes = null;
        private String mTags = null;
        private String mHash = null;
        private String mMeta = null;
        private Boolean mPrivate = false;
        private long mTime = 0;
        private long mLastUpdate = 0;

        public int getId(){
        	return mId;
        }
        
        public String getUrl() {
            return mUrl;
        }

        public String getDescription() {
            return mDescription;
        }
        
        public String getNotes(){
        	return mNotes;
        }
        
        public String getTagString(){
        	return mTags;
        }
        
        public ArrayList<Tag> getTags(){
			ArrayList<Tag> result = new ArrayList<Tag>();
			for(String s : this.getTagString().split(" ")) {
				result.add(new Tag(s));
			}
			
			return result;
        }
        
        public String getHash(){
        	return mHash;
        }

        public String getMeta(){
        	return mMeta;
        }
        
        public long getTime(){
        	return mTime;
        }
        
        public long getLastUpdate(){
        	return mLastUpdate;
        }
        
        public boolean getPrivate(){
        	return mPrivate;
        }
        
        public String getAccount(){
        	return mAccount;
        }
        
        public Bookmark() {
        }
        
        public Bookmark(int id) {
        	mId = id;
        }
        
        public Bookmark(String url) {
            mUrl = url;
        }
        
        public Bookmark(String url, String description) {
            mUrl = url;
            mDescription = description;
        }
        
        public Bookmark(String url, String description, String notes) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
        }
        
        public Bookmark(String url, String description, String notes, String tags, String account, long time) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mAccount = account;
            mTime = time;
        }
        
        public Bookmark(String url, String description, String notes, String tags, boolean priv, long time) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mPrivate = priv;
            mTime = time;
        }
        
        public Bookmark(String url, String description, String notes, String tags, String hash, String meta, long time) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mHash = hash;
            mMeta = meta;
            mTime = time;
        }
        
        public Bookmark(int id, String account, String url, String description, String notes, String tags, String hash, String meta, long time) {
            mId = id;
        	mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mHash = hash;
            mMeta = meta;
            mTime = time;
            mAccount = account;
        }
        
        public static ArrayList<Bookmark> valueOf(String userBookmark){
        	SAXReader reader = new SAXReader();
        	InputSource inputSource = new InputSource(new StringReader(userBookmark));
        	Document document = null;
			try {
				document = reader.read(inputSource);
			} catch (DocumentException e1) {
				e1.printStackTrace();
			}   	
        	
            String expression = "/posts/post";
            ArrayList<Bookmark> list = new ArrayList<Bookmark>();
           
        	List<Element> nodes = document.selectNodes(expression);
			
			for(int i = 0; i < nodes.size(); i++){
				String shref = nodes.get(i).attributeValue("href");
				String stitle = nodes.get(i).attributeValue("description");
				String snotes = nodes.get(i).attributeValue("extended");
				String stags = nodes.get(i).attributeValue("tag");
				String shash = nodes.get(i).attributeValue("hash");
				String smeta = nodes.get(i).attributeValue("meta");
				String stime = nodes.get(i).attributeValue("time");
				String surl = nodes.get(i).attributeValue("url");
				
				if(shash == null)
					shash = surl;
				
				Date d = new Date(0);
				if(stime != null && stime != ""){
					try {
						d = DateParser.parse(stime);
					} catch (ParseException e) {
						Log.d("Parse error", stime);
						e.printStackTrace();
					}
				}
				
				list.add(new Bookmark(shref, stitle, snotes, stags, shash, smeta, d.getTime()));

			}
				
			return list;
        }
        
        public static Bookmark valueOf(JSONObject userBookmark) {
            try {
                final String url = userBookmark.getString("u");
                final String description = userBookmark.getString("d");
                final JSONArray tags = userBookmark.getJSONArray("t");
                final String stime = userBookmark.getString("dt");
                
                String notes = "";
                String account = "";
                
                if(userBookmark.has("n")) {
                	notes = userBookmark.getString("n");
                }
                if(userBookmark.has("a")) {
                	account = userBookmark.getString("a");
                }
                
				Date d = new Date(0);
				if(stime != null && stime != ""){
					try {
						d = DateParser.parse(stime);
					} catch (ParseException e) {
						Log.d("Parse error", stime);
						e.printStackTrace();
					}
				}

                return new Bookmark(url, description, notes, tags.join(" ").replace("\"", ""), account, d.getTime());
            } catch (final Exception ex) {
                Log.i("User.Bookmark", "Error parsing JSON user object");
            }
            return null;
        }
	}
}