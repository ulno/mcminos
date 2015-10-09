package com.mcminos.game;

import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;

/**
 * Created by ulno on 05.10.15.
 */
public class Audio {
    public static String[] soundNames = new String[]{"aaahhh",
            "antidot",
            "applaus",
            "beep",
            "blub",
            "bulb",
            "error",
            "ethanole",
            "explosio",
            "fade2",
            "fade3",
            "fade",
            "falling",
            "ghosts",
            "gotyou",
            "hihat",
            "holegrow",
            "killall",
            "knurps",
            "life",
            "moverock",
            "orchestr",
            "panflute",
            "poison",
            "power2",
            "power",
            "quietsch",
            "rums",
            "rumble",
            "skullkill",
            "slowdown",
            "speedup",
            "splash",
            "tick",
            "tools",
            "trommeln",
            "treasure",
            "wind",
            "zisch"};

    public HashMap<String,com.badlogic.gdx.audio.Sound> soundList = new HashMap<>();

    public void soundPlay(String s) {
        if(s != null && s != "")
            soundList.get(s).play(1.0f);
    }

    public void addSound(String s, Sound sound) {
        soundList.put(s, sound);
    }
}

/*

Sound fr nchsten Level
        void snd_levelend( void )
        {
                play_sound(APPLAUS, 255, 800 );
        }

        Sound fr Gegengift
        void snd_antidot( void )
        {
        play_sound( TOOLS, 1, 30 );
        }


        Sound fr letter
        void snd_letter( void )
        {
        play_sound( TOOLS, 1, 30 );
        }

        // replaced by rumble
        Sound fr fallenden Stein bzw. aufbrechenden Boden
        void snd_rockfall( void )
        {
        play_sound( SPLASH, 3, 200 );
        }

        Sound fr warpin
        void snd_warpin( void )
        {
        play_sound( BLUB, 2, 200 );
        }

        Sound fr warpout
        void snd_warpout( void )
        {
        play_sound( BULB, 2, 200 );
        }

        Sound fr fallenden McMinos
        void snd_falling( void )
        {
        play_sound( FALLING, 2, 300 );
        }

        Uhrticken
        void snd_tick( void )
        {
        play_sound( TICK, 0, 0 );
        }

        Beep
        void snd_beep( void )
        {
        play_sound( BEEP, 2, 30 );}
        */