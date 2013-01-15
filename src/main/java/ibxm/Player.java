// Copyright mutart ???
// Modifications copyright 2012,2013, Richard Henwood Lisenced under AGPL3


package ibxm;

import java.io.*;

import javax.sound.sampled.*;

public class Player {
	private Thread play_thread;
	private static IBXM ibxm;
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
		Player.ibxm = new IBXM(mod, sampleRate);
		try {
			audioLine = AudioSystem.getSourceDataLine( new AudioFormat( this.SAMPLERATE, 16, 2, true, true ) );
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadModule(Module mod) {
		Player.ibxm = new IBXM(mod, this.SAMPLERATE);
	}
	
	public Module getModule () {
		return Player.ibxm.getModule();
	}
	
	public int seek (int i) {
		return Player.ibxm.seek(i);
	}
	
	/**
		Open the audio device and begin playback.
		If a module is already playing it will be restarted.
	*/
	public void play() {
		Player.ibxm.setLoopPattern(false);
		stop();
		play_thread = new Thread( new AudioDriver(SAMPLERATE) );
		play_thread.start();
		//running = true;
	}
	public void playPattern(int patNo) {
		Player.ibxm.setLoopPattern(true);
		Player.ibxm.setLoopPatternNo(patNo);
		this.restart();
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
		Player.ibxm.seek(0);
		//Player.AudioDriver.
		System.out.println("restarted");
	}
	
	public void _setInterpolation(int interpolation) {
		Player.ibxm.setInterpolation(interpolation);
	}

	public int calculateSongDuration() {
		return Player.ibxm.calculateSongDuration();
	}

	public void setSequence(int[] pats) {
		if (pats.length < Player.ibxm.getSeqpos())
			Player.ibxm.setSequencePos(pats.length - 1);
		this.getModule().setSequence(pats);
	}

	public void setModule(Module mod) {
		Player.ibxm.setModule(mod, SAMPLERATE);
		// TODO Auto-generated method stub
		
	}

	public int getSpeed() {
		return Player.ibxm.getSpeed();
		// TODO Auto-generated method stub
		
	}
	public int getTickLen() {
	 	return Player.ibxm.getTickLen();
	}

	public int getCurrentPattern() {
		return Player.ibxm.getCurrentPattern();
	}	
	
	public int getCurrentRow() {
		return Player.ibxm.getCurrentRow();
	}
	
	private class AudioDriver implements Runnable {
		private int sampleRate;
		
		public AudioDriver(int sampleRate) {
			this.sampleRate = sampleRate;
		}

		public void run() {
			if( running ) return;
			try {
                int[] mixBuf = new int[ Player.ibxm.getMixBufferLength() ];
                byte[] outBuf = new byte[ mixBuf.length * 2 ];
				audioLine.open();
				audioLine.start();
				running = true;
				while( running ) {
                    int count = Player.ibxm.getAudio( mixBuf );
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

}