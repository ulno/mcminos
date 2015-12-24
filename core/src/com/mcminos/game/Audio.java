package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by ulno on 05.10.15.
 */
public class Audio {
    public final static String[] soundNames = new String[]{"aaahhh",
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
            "zisch"
    };

    public final static String[] musicNames = new String[]{
            "Clumsy-Monster-Bash_remixed",
            "Creaky-Country-Fair",
            "Cyber-Streets",
            "Disco-Ants-Go-Clubbin",
            "Ghoulish-Fun",
            "Off-to-Another-Heist",
            "Runaway-Technology",
            "Star-Light",
            "The-Hairy-Monsters-Dance-a-Thon",
            "The-Toy-Factory",
            "The-Triumph-of-Technology_v001",
            "Urban-Sci-Fi-Heroes"
    };

    public final static String title = "McMinos-Title";
    public final static String screenLoop = "Chamber-of-Jewels";

    boolean sound = true;
    boolean music = true;

    Music musicPlayed = null;

    enum MusicType {None,Fixed,Random};
    MusicType currentMusicType = MusicType.None;
    String currentMusic = "";

    private Random randomGenerator = new Random();

    public HashMap<String, com.badlogic.gdx.audio.Sound> soundList = new HashMap<>();

    public Audio() {
    }

    public void soundPlay(String s) {
        if (sound && s != null && s != "")
            soundList.get(s).play(1.0f);
        // TODO: add volume
    }

    public void addSound(String s, Sound sound) {
        soundList.put(s, sound);
    }

    public void toggleSound() {
        setSound(!sound);
    }

    public void toggleMusic() {
        setMusic(!music);
    }

    public boolean getSound() {
        return sound;
    }

    public boolean getMusic() {
        return music;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
        if(sound == false) {
            for(com.badlogic.gdx.audio.Sound s:soundList.values()) {
                s.stop();
            }
        }
    }

    public void setMusic(boolean music) {
        this.music = music;
        if(music) {
            if(musicPlayed != null) {
                musicPlayed.play();
            }
        } else {
            if(musicPlayed != null)
                musicPlayed.stop();
            // musicStop(); resets too much
        }
    }

    public void musicFixed(int select) {
        String musicFile;

        musicStop();

        switch(select) {
            case 1:
                musicFile = screenLoop;
                break;
            default:
                musicFile = title;
                break;
        }

        musicFile = "music/fixed/" + musicFile + ".mp3";

        musicPlayed = Gdx.audio.newMusic(Gdx.files.internal(musicFile));
        musicPlayed.setLooping(true); // loop cleaner - so far only possible when volume reduced and built up again
        if(music) musicPlayed.play();
    }

    private void musicStop() {
        if(musicPlayed != null) {
            musicPlayed.stop();
            musicPlayed.dispose();
            currentMusic = "";
            currentMusicType = MusicType.None;
        }
    }

    public void musicRandom() {
        if(currentMusicType != MusicType.Random ) {
            musicStop();
            currentMusicType = MusicType.Random;
            selectAndPlayNext();
        }
    }

    private void selectAndPlayNext() {
        musicStop();

        int index = randomGenerator.nextInt(musicNames.length);
        String newMusic = musicNames[index];
        if(newMusic.equals(currentMusic)) {
            newMusic = musicNames[(index+1)%musicNames.length];
        }

        String musicFile = "music/random/" + newMusic + ".mp3";

        musicPlayed = Gdx.audio.newMusic(Gdx.files.internal(musicFile));
        musicPlayed.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                selectAndPlayNext();
            }
        });
        if(music) musicPlayed.play();
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