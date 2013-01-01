
package ibxm;

import java.io.*;

import javax.sound.sampled.*;

public class Player {
	private Thread play_thread;
	private IBXM ibxm;
	private int SAMPLERATE;
	public int song_duration;
	public int play_position;
	private boolean running;
	private SourceDataLine audioLine;
	
	/**
		Instantiate a new Player.
	*/
	public Player(int sampleRate) {
		this.SAMPLERATE = sampleRate;
		//this.ibxm = null; //new IBXM();
		try {
			audioLine = AudioSystem.getSourceDataLine( new AudioFormat( this.SAMPLERATE, 16, 2, true, true ) );
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Player(int sampleRate, Module mod) {
		this.SAMPLERATE = sampleRate;
		this.ibxm = new IBXM(mod, sampleRate);
		try {
			audioLine = AudioSystem.getSourceDataLine( new AudioFormat( this.SAMPLERATE, 16, 2, true, true ) );
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Player() {
		this(44100);		
	}
	
	public void loadModule(Module mod) {
		this.ibxm = new IBXM(mod, this.SAMPLERATE);
	}

	/**
		Set the Module instance to be played.
	*/
	/*public void _old_set_module( Module m ) {
		if( m != null ) module = m;
		stop();
		//ibxm.set_module( module );
		//song_duration = ibxm.calculate_song_duration();
	}*/
	
	public Module getModule () {
		return this.ibxm.getModule();
	}
	
	/**
		If loop is true, playback will continue indefinitely,
		otherwise the module will play through once and stop.
	*/
	/*public void setLoop( boolean loop ) {
		this.loop = loop;
	}*/
	
	public int seek (int i) {
		return ibxm.seek(i);
	}
	
	/**
		Open the audio device and begin playback.
		If a module is already playing it will be restarted.
	*/
	public void play() {
		ibxm.setLoopPattern(false);
		stop();
		play_thread = new Thread( new AudioDriver(SAMPLERATE) );
		play_thread.start();
	}
	public void playPattern(int patNo) {
		ibxm.setLoopPattern(true);
		ibxm.setLoopPatternNo(patNo);
		this.restart();
		//stop();
		//play_thread = new Thread( new AudioDriver(SAMPLERATE) );
		//play_thread.start();
	}
	
	/**
		Stop playback and close the audio device.
	*/
	public void stop() {
		running = false;
		if( play_thread != null ) {
			try {
				play_thread.join();
			} catch( InterruptedException ie ) {}
		}
	}
	
	public void restart() {
		this.play_position = 0;
		ibxm.seek(0);
		System.out.println("restarted");
	}
	
	private class AudioDriver implements Runnable {
		private int sampleRate;
		private int bitsPerSecond;
		
		public AudioDriver(int sampleRate) {
			this.sampleRate = sampleRate;
			this.bitsPerSecond = sampleRate * 2 * 16;
		}

		public void run() {
			if( running ) return;
			try {
                int[] mixBuf = new int[ ibxm.getMixBufferLength() ];
                byte[] outBuf = new byte[ mixBuf.length * 2 ];
				audioLine.open();
				audioLine.start();
				running = true;
				while( running ) {
                    int count = ibxm.getAudio( mixBuf );
                    int outIdx = 0;
                    for( int mixIdx = 0, mixEnd = count * 2; mixIdx < mixEnd; mixIdx++ ) {
                            int ampl = mixBuf[ mixIdx ];
                            if( ampl > 32767 ) ampl = 32767;
                            if( ampl < -32768 ) ampl = -32768;
                            outBuf[ outIdx++ ] = ( byte ) ( ampl >> 8 );
                            outBuf[ outIdx++ ] = ( byte ) ampl;
                    }
                    audioLine.write( outBuf, 0, outIdx );
				}
				audioLine.drain();
				audioLine.flush();
				audioLine.close();
			} catch( LineUnavailableException lue ) {
				lue.printStackTrace();
			/*} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();*/
			}
		}
	}

	public void _setInterpolation(int interpolation) {
		ibxm.setInterpolation(interpolation);
		
	}

	public int calculateSongDuration() {
		return ibxm.calculateSongDuration();
	}

	public void setSequence(int[] pats) {
		if (pats.length < this.ibxm.getSeqpos())
			this.ibxm.setSequencePos(pats.length - 1);
		this.getModule().setSequence(pats);
	}

	public void setModule(Module mod) {
		this.ibxm.setModule(mod, SAMPLERATE);
		// TODO Auto-generated method stub
		
	}

	public void getSpeed() {
		System.out.println("speed: " + ibxm.getSpeed() + " ticklen: " + ibxm.getTickLen());
		// TODO Auto-generated method stub
		
	}	
}