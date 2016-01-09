package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Queue;
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

//    public final static String[] musicNames = new String[]{
//            "Clumsy-Monster-Bash_remixed",
//            "Creaky-Country-Fair",
//            "Cyber-Streets",
//            "Disco-Ants-Go-Clubbin",
//            "Ghoulish-Fun",
//            "Off-to-Another-Heist",
//            "Runaway-Technology",
//            "Star-Light",
//            "The-Hairy-Monsters-Dance-a-Thon",
//            "The-Toy-Factory",
//            "The-Triumph-of-Technology_v001",
//            "Urban-Sci-Fi-Heroes"
//    };
    public final static String[] musicNames = new String[]{
            "Cyber-Streets",
            "The-Toy-Factory",
            "The-Triumph-of-Technology_v001",
            "Urban-Sci-Fi-Heroes"
    };

    public final static String creditsLoop = "McMinos-Title";
    public final static String congratsLoop = "Chamber-of-Jewels";
    public final static String titleLoop = "The-Princess-Haunted";

    boolean sound = true;
    boolean music = true;

    Music musicPlayed = null;
    private final static int maxQueueLength = 5;

    enum MusicType {None,Fixed,Random};
    MusicType currentMusicType = MusicType.None;
    String currentMusic = "";

    private Random randomGenerator = new Random();

    class MySound {
        private com.badlogic.gdx.audio.Sound snd;
        private long ids[] = new long[maxQueueLength];
        private int idPtr = 0;

        MySound(Sound snd) {
            this.snd = snd;
            for(int i=maxQueueLength-1; i>=0; i--) {
                ids[i] = -1;
            }
        }

        public void stop() {
            for(int i=maxQueueLength-1; i>=0; i--) {
                long id = ids[i];
                if(id!=-1) {
                    snd.stop(id);
                }
            }
        }

        public void play() {
            // TODO: check why the second silence on Mac OS X is necessary
            long lastId = ids[idPtr];
            if(lastId != -1) {
                snd.stop(lastId);
            }
            ids[idPtr] = snd.play(0.8f); // all a little too loud, so 0.8 instead of 1.0
            idPtr = (idPtr + 1)%maxQueueLength;
        }

        public void dispose() {
            snd.stop();
            snd.dispose();
        }
    }

    public HashMap<String, MySound> soundList = new HashMap<>();

    public Audio() {
    }


    public void scheduleLoads(AssetManager manager) {
        // Sounds
        for (String s : Audio.soundNames) {
            manager.load("sounds/" + s + ".mp3", Sound.class);
        }
        // UIs
        // manager.load(DEFAULT_UISKIN, Skin.class); needs to be pre-loaded
    }

    public void finishLoads(AssetManager manager) {
        // Sounds
        for (String s : Audio.soundNames) {
            Sound sound = manager.get("sounds/" + s + ".mp3");
            addSound(s, sound);
        }
        // UIs
        // Game.skin =  manager.get(DEFAULT_UISKIN); needs to be pre-laoded
    }

    public void soundPlay(String s) {
        if (sound && s != null && s != "") {
            MySound snd = soundList.get(s);
            snd.play();
        }
        // TODO: add volume
    }

    public void addSound(String s, Sound sound) {
        soundList.put(s, new MySound(sound));
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
            for(MySound s:soundList.values()) {
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
            case 0:
                musicFile = titleLoop;
                break;
            case 1:
                musicFile = congratsLoop;
                break;
            case 2:
                musicFile = creditsLoop;
                break;
            default:
                musicFile = creditsLoop;
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

    public void dispose() {
        musicStop();
        for(MySound s:soundList.values()) {
            s.dispose();
        }
    }
}