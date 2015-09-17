package com.mcminos.game;

/**
 * Created by ulno on 30.08.15.
 *
 * Some personal utility functions (like binary logarithm)
 */
public class Util {
    public static int log2binary( int bits )
    {
        if( bits == 0 )
            return 0; // or throw exception
        return 31 - Integer.numberOfLeadingZeros( bits );
    }

    public static int shiftLeftLogical( int number, int shift) {
        if(number > 0)
            return shift > 0 ? number << shift : number >> - shift;
        else
            return shift > 0 ? -((-number) << shift) : -((-number) >> - shift);
    }
}
