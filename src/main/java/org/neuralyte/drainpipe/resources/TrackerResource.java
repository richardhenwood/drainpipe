/*
    Project Drainpipe: A RESTful sound tracker
    Copyright (C) 2012  Richard Henwood rjhenwod@yahoo.co.uk

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neuralyte.drainpipe.resources;

import ibxm.Instrument;
import ibxm.Module;
import ibxm.Pattern;
import ibxm.Sample;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

//import net.sf.json.JSON;
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;
//import net.sf.json.JSONSerializer;
//import net.sf.json.JsonConfig;
//import net.sf.json.util.PropertyFilter;

import org.apache.commons.beanutils.PropertyUtils;
import org.neuralyte.drainpipe.Tracker;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@Path("/drainpipe")
public class TrackerResource {

	@Context ServletConfig sc; 
	String uiFileHTML = null;
	//private JsonConfig jsonConfig = null;
	private static Tracker tracker = null;
	private static String loadedModURL = null; // used to avoid unnecessary mod reloads.

	   
	public TrackerResource() {
		/*if (this.jsonConfig == null) {
			System.out.println("initializing tracker");
	
			this.jsonConfig = new JsonConfig();
			this.jsonConfig.setIgnorePublicFields(false);
			this.jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
				public boolean apply( Object source, String name, Object value ) {
			          return false;
				}
				//public void setProperty(Object o, String string, Object o1) {
			    public void setProperty(Object source, String name, Object value) {
			        try {
			            this.setProperty(source, name, value);
			        } catch (Exception ex) {
			        	System.out.println("converting json: " + source + " " + name + " " + value);
			            //ignore
			        }
			    }
			});
		}*/
	}

    @GET 
    @Produces("text/html")
	public String getPoorMansStatic() {
    	String staticRoot = sc.getInitParameter("org.neuralyte.drainpipe.staticRoot");
    	System.out.println("context = " + staticRoot);
    	
    	uiFileHTML = "";
    	//@TODO fix this with a dev flag:
    	//if (uiFileHTML == null) {
    	// this is what should be tested in with a 'dev' flag:
    	// i.e. when in dev mode, don't cache the staticRoot file.
    	// I can't fix that now, it's too complicated.
    	if (true) {
	        try
	        {
	        	String uiFile = staticRoot;
	        	FileInputStream f = new FileInputStream( uiFile );
	        	DataInputStream in = new DataInputStream(f);
	        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        	String strLine = "";
	        	while ((strLine = br.readLine()) != null)   {
	        		uiFileHTML += strLine + "\n";
	        	}
	        	in.close();
	        }
	        catch (Exception e)
	        {
	                System.err.println ("Error writing to file");
	        }
    	}
    	return uiFileHTML;

    	//return "poor mans static";
    }
    
    @GET @Path("{songname}/new")
    @Produces("text/plain")
    public String newSong(@PathParam("songname") String songName) throws URISyntaxException {
    	tracker = Tracker.getInstance();
    	tracker.newModule();
    	/*try {
    		// TODO: a new modules really should be available as a constructor.
			//tracker.loadModule(new URL("http://neuralyte.org/~ig0r/new.xm"));
    		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    	
    	//return Response.created(new URI(songName));
    	//return this.savecopy(songName);
    	return("{saveLocation: 'NOT_SAVED'}");
    }
    
    @GET @Path("{songname}")
    @Produces("text/plain")
    public String getJsongRoot(@PathParam("songname") String songName) throws IOException {
    	System.out.println("get:");
    	System.out.println("'"+songName+"'");
    	
    	tracker = Tracker.getInstance();
    	if ((loadedModURL == null || !songName.endsWith(loadedModURL)) 
    			&& !songName.equals("NOT_SAVED")) {
    		tracker.loadModule(new URL(URLDecoder.decode(songName, "ISO-8859-1")));
    		loadedModURL = songName;
    	}
    	else {
    		System.out.println("Tune already loaded, or a new tune, ignoring load request.");
    	}

    	Module mod = tracker.getPlayer().getModule();
    	
    	Gson gson = new Gson();
    	String json = gson.toJson(mod);
    	return json;
    }
    
    @GET @Path("{songname}/play")
    @Produces("text/plain")
    public String playJsong(@PathParam("songname") String songName) throws IOException {
    	tracker = Tracker.getInstance();
    	if (tracker == null) {
    		this.getJsongRoot(songName);
    	}
    	tracker.play();
    	
    	return "playing.";
    }
    
    @GET @Path("{songname}/status")
    @Produces("text/plain")
    public String getstatus(@PathParam("songname") String songName) {
    	int speed = tracker.getPlayer().getSpeed();
    	int ticklen = tracker.getPlayer().getTickLen();
    	int pattern = tracker.getPlayer().getCurrentPattern();
    	int row = tracker.getPlayer().getCurrentRow();
    	return "{'speed': " + speed
    			+ ", 'ticklen': " + ticklen
    			+ ", 'pattern': " + pattern 
    			+ ", 'row': " + row 
    			+ "}"; 
    }
    
    @GET @Path("{songname}/restart")
    @Produces("text/plain")
    public String restartJsong(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	tracker.restart();
    	return "restarted.";
    }
    
    @GET @Path("{songname}/stop")
    @Produces("text/plain")
    public String stopJsong(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	tracker.stop();
    	return "stopped.";
    }
    
    
    @GET @Path("{songname}/global_volume")
    @Produces("application/json")
    public String global_volume(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	int vol = tracker.getPlayer().getModule().defaultGVol;
    	
    	return "{\"global_volume\": " + vol + "}";
    }
    
    @POST @Path("{songname}/global_volume")
    @Produces("application/json")
    public String global_volume(@PathParam("songname") String songName, String incomingJson) {
       	Tracker tracker = Tracker.getInstance();

    	//Tracker tracker = Tracker.getInstance();
    	tracker.getPlayer().getModule().defaultGVol = Integer.parseInt(incomingJson);
    	
    	//System.out.println("incomingJson: " + incomingJson);
    	return "{\"global_volume\": " + incomingJson + "}";
    }

    @GET @Path("{songname}/sequence")
    @Produces("application/json")
    public String sequence(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	int[] seq = tracker.getPlayer().getModule().getSequence();
   	
    	Gson gson = new Gson();
    	String json = gson.toJson(seq);
    	return json;
    }
    
    @POST @Path("{songname}/sequence")
    @Produces("application/json")
    public String sequence(@PathParam("songname") String songName, String incomingJson) {
       	Tracker tracker = Tracker.getInstance();
       	
    	//incomingJson = incomingJson.substring(1, incomingJson.length() - 1);    	
    	//JSONArray json = (JSONArray) JSONSerializer.toJSON( incomingJson ); 
    	Gson gson = new Gson();
    	int[] seq = gson.fromJson(incomingJson, int[].class);
    	tracker.getPlayer().setSequence(seq);
    	
    	//JSONArray json = JSONArray.toArray(incomingJson);
    	
    	/*strSeq = incomingJson.split(",");
    	seq = new int[strSeq.length];
    	for (int i = 0; i < strSeq.length; i++) {
    		seq[i] = Integer.parseInt(strSeq[i]);
    	}
    	tracker.getPlayer().getModule().setSequence(seq);*/
    	System.out.println("sequence updated: " + Arrays.toString(tracker.getPlayer().getModule().getSequence()));
    	return incomingJson.toString();
    	//return "{\"sequence\": " + incomingJson + "}";
    }

    
    @GET @Path("{songname}/patterns")
    @Produces("application/json")
    public String patterns(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	ArrayList<Pattern> pats = tracker.getPlayer().getModule().getPatterns();
    	Gson gson = new Gson();
    	String json = gson.toJson(pats);
    	
    	//JSONArray json = JSONArray.fromObject(pat, jsonConfig);
    	//String out = json.toString();
    	return json;
    }

    @GET @Path("{songname}/pattern/{patNo}/play")
    @Produces("application/json")
    public String patternPlay(@PathParam("songname") String songName, @PathParam("patNo") int patNo) throws IOException {

    	if (tracker == null) {
    		this.getJsongRoot(songName);
    	}
    	if (!tracker.isPlaying()) {
    		tracker.play();
    	}
    	tracker.playPattern(patNo);
    	tracker.getPlayer().getSpeed();
    	return "playing pattern: " + patNo;
    	
    	/*
    	    	
    	JSONObject json = (JSONObject) JSONObject.fromObject( incomingJson, jsonConfig ); 
    	Pattern pat = (Pattern)JSONObject.toBean(json, Pattern.class);
    	tracker.getPlayer().getModule().setPattern(pat);
    	
    	System.out.println("pattern updated: " + tracker.getPlayer().getModule().getPattern(patNo).toString());
    	return json.toString();*/
    }  	
    
    @POST @Path("{songname}/pattern/{patNo}")
    @Produces("application/json")
    public String pattern(@PathParam("songname") String songName, @PathParam("patNo") int patNo, String incomingJson) {
       	Tracker tracker = Tracker.getInstance();
    	    	
    	Gson gson = new Gson();
    	Pattern pat = gson.fromJson(incomingJson, Pattern.class);
    	tracker.getPlayer().getModule().setPattern(pat);
    	
    	System.out.println("pattern updated: " + tracker.getPlayer().getModule().getPattern(patNo).toString());
    	return pat.toString();
    }
    
    @POST @Path("{songname}/instrument/{instNo}")
    @Produces("application/json")
    public String instrument(@PathParam("songname") String songName, @PathParam("instNo") int instNo, String incomingJson) {
    	Tracker tracker = Tracker.getInstance();
    	
    	Gson gson = new Gson();
    	Instrument inst = gson.fromJson(incomingJson, Instrument.class);
    	tracker.getPlayer().getModule().setInstrument(inst, instNo);
    	
    	System.out.println("instrument updated: " + tracker.getPlayer().getModule().getInstrument(instNo).toString());
    	return inst.toString();
    }
    
    @POST @Path("{songname}/instrument/{instNo}/sample/{sampNo}")
    @Produces("application/json")
    public String sample(@PathParam("songname") String songName, 
    		@PathParam("instNo") int instNo, 
    		@PathParam("sampNo") int sampNo, 
    		String incomingJson) {
    	Tracker tracker = Tracker.getInstance();
    	
    	Gson gson = new Gson();
    	Sample sampDat = gson.fromJson(incomingJson, Sample.class);
    	tracker.getPlayer().getModule().setSample(instNo, sampNo, sampDat);
    	
    	System.out.println("sample updated: " + tracker.getPlayer().getModule().getInstrument(instNo).toString());
    	return sampDat.toString();
    }
    
    @POST @Path("{songname}/patterns")
    @Produces("application/json")
    public String patterns(@PathParam("songname") String songName, String incomingJson) {
    	//int[] seq = null;
    	//String[] strSeq = null;
    	//Tracker tracker = Tracker.getInstance();
       	Tracker tracker = Tracker.getInstance();
    	
    	Gson gson = new Gson();
    	ArrayList<Pattern> pats = gson.fromJson(incomingJson, ArrayList.class);
    	tracker.getPlayer().getModule().setPatterns(pats);
    	
    	return "{\"sequence\": \"done\"}";
    }
    
    @GET @Path("{songname}/instruments")
    @Produces("application/json")
    public String instruments(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	ArrayList<Instrument> insts = tracker.getPlayer().getModule().getInstruments();
    	//Object[] inst = tracker.get_module().get_instruments();
    	//JSONArray json = JSONArray.fromObject(inst, jsonConfig);
    	//String out = json.toString();
    	
    	Gson gson = new Gson();
    	String json = gson.toJson(insts);
    	return json;
    }
    
    @GET @Path("{songname}/jsong")
    @Produces("application/json")
    public String jsong(@PathParam("songname") String songName) {

    	Module module = tracker.getPlayer().getModule();
    	Gson gson = new Gson();
    	String json = gson.toJson(module);
    	
    	return json;

//    	return "massive regression: this isn't supported now";
    }
    
    @GET @Path("{songname}/save")
    @Produces("application/json")
    public String save(@PathParam("songname") String songName) {
    	Tracker tracker = Tracker.getInstance();

    	Module module = tracker.getPlayer().getModule();
    	Gson gson = new Gson();
    	String json = gson.toJson(module);
    	String saveLocation = null;
		System.out.println("doing overwriting save. rev: "+ module.getRevision());
		OutputStream os = null;
    	try {
    		URL url = new URL(URLDecoder.decode(songName, "ISO-8859-1") + "/" + module.getDocId());
    		System.out.println("url: " + url.toString());
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setDoOutput(true);
    		conn.setRequestMethod("PUT");
    		conn.setRequestProperty("Content-Type", "application/json");
    		conn.setConnectTimeout(1000);
    		conn.setReadTimeout(1000);
    		System.setProperty("http.keepAlive", "false");
    		conn.setRequestProperty("Connection", "close");
    		
    		os = conn.getOutputStream();
    		os.write(json.toString().getBytes());
    		
    		if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
    			throw new RuntimeException("Failed : HTTP error code : "
    				+ conn.getResponseCode() + conn.getResponseMessage());
    		}
    		InputStreamReader in = new InputStreamReader(conn.getInputStream());
    		BufferedReader br = new BufferedReader(in);
     
    		String output;
    		System.out.println("Output from Server .... \n");
    		while ((output = br.readLine()) != null) {
    			CouchResponse cb = gson.fromJson(output, CouchResponse.class);
    			System.out.println("got rev: " + cb.rev);
    			module.setRevision(cb.rev);
    		}
    		saveLocation = conn.getHeaderField("Location");
    		System.out.println("response:"+saveLocation);
    		
    		conn.disconnect();
    	} catch (MalformedURLException e) {

    		e.printStackTrace();

    	} catch (IOException e) {

    		e.printStackTrace();

    	}
    	finally {
            if (os != null) try { os.close(); } catch (IOException ignore) {}
        }

    	return("{saveLocation: '"+saveLocation
    			+"', '_id': '"+module.getDocId()
    			+"', '_rev': '"+module.getRevision()
    			+"'}");
    }
    
    @GET @Path("{songname}/savecopy/{docStore}")
    @Produces("application/json")
    public String savecopy(@PathParam("songname") String songName, @PathParam("docStore") String docStore) {
       	Tracker tracker = Tracker.getInstance();

    	String saveLocation = null;
    	try {
    		Gson gson = new Gson();
        	String json = gson.toJson(tracker.getPlayer().getModule());
    		URL url = new URL(URLDecoder.decode(docStore, "ISO-8859-1"));
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setDoOutput(true);
    		conn.setRequestMethod("POST");
    		conn.setRequestProperty("Content-Type", "application/json");
    		OutputStream os = conn.getOutputStream();
    		os.write(json.toString().getBytes());
    		os.flush();
     
    		if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
    			throw new RuntimeException("Failed : HTTP error code : "
    				+ conn.getResponseCode() + conn.getResponseMessage());
    		}
    		InputStreamReader in = new InputStreamReader(conn.getInputStream());
    		BufferedReader br = new BufferedReader(in);
     
    		System.out.println("Output from Server .... \n");
    		
    		String output;
    		while ((output = br.readLine()) != null) {
    			CouchResponse cb = gson.fromJson(output, CouchResponse.class);
    			System.out.println("got rev: " + cb.rev);
    			tracker.getPlayer().getModule().setDocId(cb.id);
    			tracker.getPlayer().getModule().setRevision(cb.rev);
    		}
    		br.close();
    		saveLocation = conn.getHeaderField("Location");
    		System.out.println("response:"+saveLocation);
    		
    		conn.disconnect();
    	} catch (MalformedURLException e) {

    		e.printStackTrace();

    	} catch (IOException e) {

    		e.printStackTrace();

    	}
    	return("{saveLocation: '"+saveLocation
    			+"', '_id': '"+tracker.getPlayer().getModule().getDocId()
    			+"', '_rev': '"+tracker.getPlayer().getModule().getRevision()
    			+"'}");
    }
    
    // This method 
    @POST @Path("{songname}/jsong")
    @Produces("application/json")
    public String jsong(@PathParam("songname") String songName, String incomingJson) {       	
    	Tracker tracker = Tracker.getInstance();
    	
    	Gson gson = new Gson();
    	Module mod = gson.fromJson(incomingJson, Module.class);
    	tracker.getPlayer().setModule(mod);
     	System.out.println("new mod loaded from json");
    	return "{save:'complete'}";
    }
}

class CouchResponse {
	public String ok;
	public String id;
	public String rev;
}
