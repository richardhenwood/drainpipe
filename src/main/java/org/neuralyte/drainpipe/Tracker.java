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
package org.neuralyte.drainpipe;

//import ibxm.IBXM;
import ibxm.Instrument;
import ibxm.Module;
import ibxm.Player;

//import java.io.File;
//import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.*;

import javax.sound.sampled.LineUnavailableException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.stream.JsonReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

//import javax.sound.sampled.LineUnavailableException;

public class Tracker implements Runnable {

	final int SAMPLE_RATE = 44100;
//	public static ibxm.Player player;
	private URL songFile = null;
	private int interpolation, duration, samplePos;
    //private IBXM ibxm;
	private Player player;
	
   // private JList instrumentList;

	//private String outfile = null;


	// Private constructor prevents instantiation from other classes
	private Tracker() {
		//System.out.println("this shouldn't be caled.");
		/*try {
			player = new ibxm.Player();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder { 
		public static final Tracker INSTANCE = new Tracker();
	}

	public static Tracker getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void play () {
		player.play();
	}

	public void playPattern (int patNo) {
		player.playPattern(patNo);
	}

	
	public String getSongName () throws IOException {
		String path = "Unknown!";
		path = this.songFile.getPath();
		return path;
	}

	public void loadModule( URL modfile, JsonConfig jsonConfig ) throws IOException {
		this.songFile = modfile;
		System.out.println("loading mod: " + songFile);		
		
		URLConnection con = modfile.openConnection();
		con.connect();
		System.out.println("content length: " + con.getContentLength());
		System.out.println("content type: " + con.getContentType());
		
		
		InputStream in = con.getInputStream();
		DataInputStream modStream = null;
		
		if (con.getContentType().equals("application/zip")) {
			System.out.println("Zip file found. Loading the first thing I can find.");
			File tmpzip = File.createTempFile("modarchive", ".zip");
			FileOutputStream fos = new FileOutputStream(tmpzip);
			DataInputStream zipStream = new DataInputStream(in);
			byte b = zipStream.readByte();
			try {
				while (true) {
					fos.write(b);
					b = zipStream.readByte();
				}
			}
			catch (EOFException ex) {
				System.out.println("Zip now local:" + tmpzip.getAbsolutePath());
			}
			ZipFile zipmod = new ZipFile(tmpzip);
			System.out.println("entry: " + zipmod.entries().nextElement().getName());
			in = zipmod.getInputStream(zipmod.getEntry(zipmod.entries().nextElement().getName()));	
			//moduleData = new 
		}
		else if (con.getContentType().equals("text/plain; charset=utf-8")) {
			System.out.println("looks like we've got a jsong from couch db.");
			String incomingJson = "";
			String temp;
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			while ((temp = br.readLine()) != null) {
				incomingJson += temp;
			}
			Gson gson = new Gson();
			Module mod = gson.fromJson(incomingJson, Module.class);
			player = new Player(SAMPLE_RATE, mod);

			this.duration = player.calculateSongDuration();
			this.samplePos = 0;
			System.out.println(player.toString());
			return;
		}
		
		//byte[] moduleData = new byte[con.getContentLength()];
		//else {
			modStream = new DataInputStream(in);
		//}
			
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		// lets only allow mods of < 10 MB to load.
		byte[] moduleData = new byte[10485760];// IOUtils.toByteArray(in);// modStream.readByte();
		int nRead;
		while ((nRead = in.read(moduleData, 0, moduleData.length)) != -1) {
			  buffer.write(moduleData, 0, nRead);
		}
		buffer.flush();
		
		moduleData = buffer.toByteArray();
		//modStream.readFully(moduleData);
		
		in.close();
		
        Module module = new Module( moduleData );
       // try {
			player = new Player(SAMPLE_RATE);
			//player.setInterpolation(interpolation);
			player.loadModule(module);
        //ibxm = new IBXM( module, SAMPLE_RATE );
        //ibxm.setInterpolation( interpolation );
			duration = player.calculateSongDuration();
			samplePos = 0;
        /*seekSlider.setMinimum( 0 );
        seekSlider.setMaximum( duration );
        seekSlider.setValue( 0 );
        songLabel.setText( module.songName.trim() );*/
  /*      Vector<String> vector = new Vector<String>();
        Instrument[] instruments = module.instruments;
        for( int idx = 0, len = instruments.length; idx < len; idx++ ) {
                String name = instruments[ idx ].name;
                if( name.trim().length() > 0 )
                        vector.add( String.format( "%03d: %s", idx, name ) );
        }
        instrumentList.setListData( vector );
*/
		
		
//		player.setModule( ibxm.Player.loadModule( urlfs ) );
		//urlfs.close();
        
		/*} catch (LineUnavailableException e) {
			System.out.println("can't start player.");
			e.printStackTrace();
		}*/
	}
	
	public Player getPlayer() {
		return player;
	}
	
    public synchronized void stop() {
    	player.stop();
  /*      playing = false;
        try {
                if( playThread != null ) playThread.join();
        } catch( InterruptedException e ) {
        }*/
 /*       updateTimer.stop();
        playButton.setText( "Play" );*/
}

private synchronized void seek( int pos ) {
        samplePos = player.seek( pos );
}

/*
private synchronized void setInterpolation( int interpolation ) {
        this.interpolation = interpolation;
        if( player != null ) player.setInterpolation( interpolation );
}*/

	public void restart() {
		this.seek(0);
		//player.restart();
		// TODO Auto-generated method stub
		
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
	}

class PlayerInstanceCreator implements InstanceCreator<Player> {	
	public Player createInstance(Type arg0) {
		return new Player(44100);
	}
}
