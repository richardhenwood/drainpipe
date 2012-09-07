
package org.neuralyte.drainpipe.resources;

import ibxm.Module;
import ibxm.Pattern;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
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

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.apache.commons.beanutils.PropertyUtils;
import org.neuralyte.drainpipe.Tracker;

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
			         if( "instruments".equals( name ) || 
			        		 "patterns".equals( name ) ||
			        		 "sequence".equals( name) ){
			             return true;
			          }
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
    	tracker.loadModule(new URL(URLDecoder.decode(songName, "ISO-8859-1")));

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

    	JSONArray json = JSONArray.fromObject(seq, jsonConfig);
    	String out = json.toString();
    	return out;
    }
    
    @POST @Path("{songname}/sequence")
    @Produces("application/json")
    public String sequence(@PathParam("songname") String songName, String incomingJson) {
    	//int[] seq = null;
    	//String[] strSeq = null;
    	
    	//Tracker tracker = Tracker.getInstance();
    	//tracker.get_module().global_volume = Integer.parseInt(incomingJson);
    	
    	//incomingJson = incomingJson.substring(1, incomingJson.length() - 1);    	
    	JSONArray json = (JSONArray) JSONSerializer.toJSON( incomingJson ); 
    	tracker.getPlayer().getModule().setSequence(json.toArray());
    	
    	//JSONArray json = JSONArray.toArray(incomingJson);
    	
    	/*strSeq = incomingJson.split(",");
    	seq = new int[strSeq.length];
    	for (int i = 0; i < strSeq.length; i++) {
    		seq[i] = Integer.parseInt(strSeq[i]);
    	}
    	tracker.getPlayer().getModule().setSequence(seq);*/
    	System.out.println("sequence updated: " + Arrays.toString(tracker.getPlayer().getModule().getSequence()));
    	return json.toString();
    	//return "{\"sequence\": " + incomingJson + "}";
    }

    
    @GET @Path("{songname}/patterns")
    @Produces("application/json")
    public String patterns(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	Object[] pat = tracker.getPlayer().getModule().getPatterns();
    	JSONArray json = JSONArray.fromObject(pat, jsonConfig);
    	String out = json.toString();
    	return out;
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
    	Object[] inst = tracker.getPlayer().getModule().getInstruments();
    	//Object[] inst = tracker.get_module().get_instruments();
    	JSONArray json = JSONArray.fromObject(inst, jsonConfig);
    	String out = json.toString();
    	return out;
    }
    
    @GET @Path("{songname}/jsong")
    @Produces("application/json")
    public String jsong(@PathParam("songname") String songName) {

    	//Tracker tracker = Tracker.getInstance();
    	
    	/*
    	Object[] globals = tracker.get_module().get_globals();
    	JSONArray json = JSONArray.fromObject(globals);
    	String globalsJson = json.toString();
    	
    	Object[] inst = tracker.get_module().get_instruments();
    	json = JSONArray.fromObject(inst);
    	String instrumentJson = json.toString();
    	
       	Object[] pat = tracker.get_module().get_patterns();
    	json = JSONArray.fromObject(pat);
    	String patternJson = json.toString();
    	

    	int[] seq = tracker.get_module().get_sequence();
    	String out = "[";
    	for (int i = 0; i < seq.length - 1; i++) {
    		out += seq[i] + "," ;
    	}
    	out += seq[seq.length - 1];
    	
    	String sequenceJson = "{\"sequence\": " + out + "]}";
    	*/
    	return "massive regression: this isn't supported now";
    	/*return "{\"globals\": " + globalsJson + "," +
    			"\"patterns\": " + patternJson + "," +
    			"\"sequence\": " + sequenceJson + "," +
    			"\"instruments\": " + instrumentJson +
    			"}";*/
    }
    
    
}
