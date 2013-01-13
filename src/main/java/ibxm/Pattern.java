
package ibxm;

import ibxm.Note;


public class Pattern {
	public int numRows;
	public byte[][] data;
	public int patNumber;

	public Pattern() {
		// this is a stub needed to make json conversion work.
	}
	
	public Pattern( int numChannels, int numRows, int patNumber ) {
		data = new byte[ numChannels * numRows ][ 5 ];
		this.numRows = numRows;
		this.patNumber = patNumber;
	}
	
	public Pattern (int numRows, byte[][] data, int patNumber) {
		this.numRows = numRows;
		this.data = data;
		this.patNumber = this.patNumber;
	}

	public void setPatNumber (int num) {
		this.patNumber = num;
	}
	public void setNumRows (int num) {
		this.numRows = num;
	}
	public void setData (byte[][] d) {
		this.data = d;
	}

	public Note getNote( int index ) {
		int offset = index;
		Note note = new Note();
		note.key = data[ offset ][0] & 0xFF;
		note.instrument = data[ offset ] [ 1 ] & 0xFF;
		note.volume = data[ offset ][ 2 ] & 0xFF;
		note.effect = data[ offset ][ 3 ] & 0xFF;
		note.param = data[ offset ][ 4 ] & 0xFF;
		return note;
	}
	
	
	public void toStringBuffer( StringBuffer out ) {
		char[] hex = {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int channels = data.length / ( numRows * 5 );
		int data_offset = 0;
		for( int row = 0; row < numRows; row++ ) {
			for( int channel = 0; channel < channels; channel++ ) {
				for( int n = 0; n < 5; n++ ) {
					int b = data[ data_offset ][n];
					if( b == 0 ) {
						out.append( "--" );
					} else {
						out.append( hex[ ( b >> 4 ) & 0xF ] );
						out.append( hex[ b & 0xF ] );
					}
				}
				data_offset++;
				out.append( ' ' );
			}
			out.append( '\n' );
		}
	}
	
	public String toString() {
		return data.toString();
	}
}
