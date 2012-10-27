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
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

//import net.sf.json.JSON;
//import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
//import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.apache.commons.beanutils.PropertyUtils;
import org.neuralyte.drainpipe.Tracker;

import com.google.gson.Gson;

@Path("/drainpipe")
public class TrackerResource {

	@Context ServletConfig sc; 
	String uiFileHTML = null;
	private JsonConfig jsonConfig = null;
	private static Tracker tracker = null;

	   
	public TrackerResource() {
		if (this.jsonConfig == null) {
			System.out.println("initializing tracker");
	
			this.jsonConfig = new JsonConfig();
			this.jsonConfig.setIgnorePublicFields(false);
			this.jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
				public boolean apply( Object source, String name, Object value ) {
			         /*if( "instruments".equals( name ) || 
			        		 "patterns".equals( name ) ||
			        		 "sequence".equals( name) ){
			             return true;
			          }*/
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
		}
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
    
    @GET @Path("{songname}")
    @Produces("text/plain")
    public String getSongRoot(@PathParam("songname") String songName) throws IOException {
    	System.out.println("get:");
    	System.out.println(songName);
    	
    	tracker = Tracker.getInstance();
    	tracker.loadModule(new URL(URLDecoder.decode(songName, "ISO-8859-1")), jsonConfig);

    	Module mod = tracker.getPlayer().getModule();
    	JSONObject json = JSONObject.fromObject(mod, jsonConfig);
    	String out = json.toString();
    	return out;
        //return "Hello world: " + songName;
    }
    
    @GET @Path("{songname}/play")
    @Produces("text/plain")
    public String playmod(@PathParam("songname") String songName) throws IOException {
    	if (tracker == null) {
    		this.getSongRoot(songName);
    	}
    	tracker.play();
    	return "playing.";
    }
    
    @GET @Path("{songname}/restart")
    @Produces("text/plain")
    public String restartmod(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	tracker.restart();
    	return "restarted.";
    }
    
    @GET @Path("{songname}/stop")
    @Produces("text/plain")
    public String stopmod(@PathParam("songname") String songName) {

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
    	
    	//incomingJson = incomingJson.substring(1, incomingJson.length() - 1);    	
    	//JSONArray json = (JSONArray) JSONSerializer.toJSON( incomingJson ); 
    	Gson gson = new Gson();
    	int[] seq = gson.fromJson(incomingJson, int[].class);
    	tracker.getPlayer().getModule().setSequence(seq);
    	
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
    	Pattern[] pats = tracker.getPlayer().getModule().getPatterns();
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
    		this.getSongRoot(songName);
    	}
    	tracker.playPattern(patNo);
    	return "playing.";
    	
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
    	    	
    	JSONObject json = (JSONObject) JSONObject.fromObject( incomingJson, jsonConfig ); 
    	Pattern pat = (Pattern)JSONObject.toBean(json, Pattern.class);
    	tracker.getPlayer().getModule().setPattern(pat);
    	
    	System.out.println("pattern updated: " + tracker.getPlayer().getModule().getPattern(patNo).toString());
    	return json.toString();
    }
    	
    @POST @Path("{songname}/patterns")
    @Produces("application/json")
    public String patterns(@PathParam("songname") String songName, String incomingJson) {
    	int[] seq = null;
    	String[] strSeq = null;
    	//Tracker tracker = Tracker.getInstance();
    	
    	JSONObject jsonObject = JSONObject.fromObject( incomingJson );
    	Object bean = JSONObject.toBean( jsonObject );
    	List patterns = null;
    	try {
			patterns = (List)PropertyUtils.getProperty( bean, "patterns" );
			tracker.getPlayer().getModule().setPatterns((Pattern[]) patterns.toArray());
			//System.out.println(patterns.toArray().toString());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	return "{\"sequence\": \"done\"}";
    }
    
    @GET @Path("{songname}/instruments")
    @Produces("application/json")
    public String instruments(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	Instrument[] insts = tracker.getPlayer().getModule().getInstruments();
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

    	Object module = tracker.getPlayer().getModule();
    	
    	JSONObject json = JSONObject.fromObject(module, jsonConfig);
    	String out = json.toString();
    	return out;

//    	return "massive regression: this isn't supported now";
    }
    
    @GET @Path("{songname}/save")
    @Produces("application/json")
    public String save(@PathParam("songname") String songName) {
    	//Object module = tracker.getPlayer().getModule();
    	Module module = tracker.getPlayer().getModule();
    	Gson gson = new Gson();
    	String json = gson.toJson(module);
    	try {
    		URL url = new URL("http://drainpipe.iriscouch.com/mods");
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
     
    		BufferedReader br = new BufferedReader(new InputStreamReader(
    				(conn.getInputStream())));
     
    		String output;
    		System.out.println("Output from Server .... \n");
    		while ((output = br.readLine()) != null) {
    			System.out.println(output);
    		}
    		
    		conn.disconnect();
    	} catch (MalformedURLException e) {

    		e.printStackTrace();

    	} catch (IOException e) {

    		e.printStackTrace();

    	}
    	return("output");
    }
    
    @POST @Path("{songname}/save")
    @Produces("application/json")
    public String save(@PathParam("songname") String songName, String incomingJson) {
    	return "not suppported yet";
    }
}
