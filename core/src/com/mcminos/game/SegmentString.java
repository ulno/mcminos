package com.mcminos.game;

import com.badlogic.gdx.utils.StringBuilder;

/**
 * An in-place String implementation to avoid unnecessary Garbage-Collector runs in games
 * Created by ulno on 11.11.15.
 */
public class SegmentString implements CharSequence {
    protected StringBuilder sb;
    protected int start, len;
    private int counter;

    SegmentString( String initStr ) {
        sb = new StringBuilder( initStr );
        start = 0;
        len = sb.length();
    }

    SegmentString( SegmentString parent, int start, int len ) {
        // TODO: throw exception when index wrong
        this.sb = parent.sb;
        this.start = parent.start + start;
        this.len = len;
    }

    public SegmentString(int capacity) {
        sb = new StringBuilder(capacity);
        // fill buffer
        for( counter = 0; counter < capacity; counter ++)
            sb.append(' ');
        len = sb.length();
    }

    SegmentString sub( int start, int len ) {
        return new SegmentString( this, start, len );
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public char charAt(int i) {
        // TODO: check, if we need to throw an exception here when out of bounds
        return sb.charAt(start+i);
    }

    @Override
    public CharSequence subSequence(int i, int i1) {
        i1 += start;
        if( i1 > start + len) return null; // TODO: check, if we need to throw an exception here
        return sb.subSequence(start+i,start+i1);
    }

    private static char[] numberTranslator = {'0','1','2','3','4','5','6','7','8','9'};
    /**
     * @param position Where to start
     * @param input input number
     * @param fillLength align with leading zeros to thi slength
     * @return
     *
     * taken from: http://stackoverflow.com/questions/473282/how-can-i-pad-an-integers-with-zeros-on-the-left
     */
    public void writeInteger( int input, int position, int fillLength ) {
        int processed = 0;

        if (input < 0) {
            sb.setCharAt(position, '-');
            input = -input;
            processed++;
        }

        // move to end
        position += fillLength - 1;

        while (processed < fillLength) {
            sb.setCharAt(start+position, numberTranslator[input % 10]);
            input /= 10;
            processed++;
            position--;
        }
    }

    public boolean equals(SegmentString str) {
        if( len != str.length() ) return false;
        for( counter = 0; counter < len; counter ++ ) {
            if( charAt(counter) != str.charAt(counter) ) return false;
        }
        return true;
    }


    /**
     * Write and pad input into whole object
     * @param input
     */
    public void writeInteger( int input ) {
        writeInteger( input, 0, len );
    }

    public void writeChar(int pos, char c) {
        sb.setCharAt(start+pos,c);
    }

    public StringBuilder getStringBuilder() {
        return sb;
    }
}
