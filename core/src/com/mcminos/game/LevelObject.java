package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by ulno on 17.08.15.
 *
 * Actual objects of a Level. Walls, Main, Doors, Ghosts and pills are
 * created here. the corresponding graphics are in Graphics
 */
public class LevelObject implements  Comparable<LevelObject>, KryoSerializable {

    public final static int maxzIndex=10000;
    private int x; // windowVPixelXPos-Position in level blocks * virtualBlockResolution
    private int y; // windowVPixelYPos-Position in level blocks * virtualBlockResolution
    private LevelBlock levelBlock = null; // currently associated LevelBlock
    private Graphics gfx; // actual Graphics for the object
    private int zIndex = maxzIndex; // by default it is too high
    private Mover mover = null; // backlink
    private Level level = null;
    private int holeLevel;
    public static final int maxHoleLevel = 4; // maximum open
    private int animDelta = 0; // how much offset for the animation
    private Types type;
    private DoorTypes doorType = DoorTypes.None;
    private int initOneWayType = -1;

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output,x);
        kryo.writeObject(output,y);
        if(gfx != null)
            kryo.writeObject(output,gfx.getName());
        else
            kryo.writeObject(output,"");
        if(mover != null) {
            kryo.writeClass(output, mover.getClass());
            kryo.writeObject(output, mover);
        } else {
            kryo.writeClass(output, null);
        }
        kryo.writeObject(output, levelBlock);
        kryo.writeObject(output, zIndex);
        kryo.writeObject(output, holeLevel);
        kryo.writeObject(output, animDelta);
        kryo.writeObject(output, type);
        kryo.writeObject(output, doorType);
        kryo.writeObject(output, levelBlock.getOneWayType());
    }

    @Override
    public void read(Kryo kryo, Input input) {
        x = kryo.readObject(input,Integer.class);
        y = kryo.readObject(input,Integer.class);
        gfx = Graphics.getByName( kryo.readObject(input,String.class));
        Registration c = kryo.readClass(input);
        if( c == null) {
            mover = null;
        } else if (c.getType() == RockMover.class) {
            mover = kryo.readObject(input, RockMover.class);
        } else if (c.getType() == GhostMover.class) {
            mover = kryo.readObject(input, GhostMover.class);
        } else if (c.getType() == McMinosMover.class) {
            mover = kryo.readObject(input, McMinosMover.class);
        } else { // should not happen
            Gdx.app.log("LevelObject kryo read","Got wrong mover reading file.");
            mover = kryo.readObject(input, Mover.class);
        }
        levelBlock = kryo.readObjectOrNull(input, LevelBlock.class);
        zIndex = kryo.readObject(input, Integer.class);
        holeLevel = kryo.readObject(input, Integer.class);
        animDelta = kryo.readObject(input, Integer.class);
        type = kryo.readObject(input, Types.class);
        doorType = kryo.readObject(input, DoorTypes.class);
        initOneWayType = kryo.readObject(input, Integer.class);
    }

    /**
     * make sure levelblock is initialized by coordinates
     * @param game
     */
    public void initAfterKryoLoad( Game game ) {
        Level level = game.getLevel();
        this.level = level;
        if(levelBlock != null) {
            int lbx = levelBlock.getX();
            int lby = levelBlock.getY();
            levelBlock = null; // so far not correctly initialized, therefore set this to 0
            setLevelBlock( level.get(lbx,lby) ); // TODO: check if this is important for other levelblocks
        }
        if(mover != null)
            mover.initAfterKryoLoad(game,this);
    }

    public int getInitOneWayType() {
        return initOneWayType;
    }

    public enum Types {Unspecified, Power1, Power2, Power3,
        IndestructableWall, InvisibleWall, Rockme, Live, Letter,
        Skull, Bomb, Dynamite, Rock, Pill, Castle, McMinos, Wall, Background, Key, Umbrella,
        DoorClosed, DoorOpened, SpeedUpField, SpeedDownField, WarpHole, KillAllField, OneWay,
        Chocolate, LandMine, LandMineActive, LandMineExplosion, BombFused, DynamiteExplosion,
        BombExplosion, DestroyedWall, Ghost1, Ghost2, Ghost3, Ghost4, KillAllPill, Exit, Bonus1, Bonus2, Bonus3, Whisky, Mirror, Poison, Medicine, SkullField, Destination, DynamiteFused, McMinosDying, McMinosFalling, McMinosWinning, Hole};
    public enum DoorTypes {None, HorizontalOpened,HorizontalClosed, VerticalOpened,VerticalClosed};

    private void construct(LevelBlock levelBlock, int zIndex, Types type) {
        level = levelBlock.getLevel();
        // does not exist here playwindow = game.getPlayWindow();
        x = levelBlock.getX() << PlayWindow.virtualBlockResolutionExponent;
        y = levelBlock.getY() << PlayWindow.virtualBlockResolutionExponent;
        this.zIndex = zIndex;
        this.type = type;
        setLevelBlock(levelBlock);
        level.addToAllLevelObjects(this);
    }

    /**
     * This is called when re-constructed from kryo-save
     */
    LevelObject() {

    }

    /**
     *all.
     * @param x in block coordinates
     * @param y in block coordinates (movable objects can have fraction as coordinate)
     * @param zIndex need to know zIndex to allow correct drawing order later
     */
    LevelObject(Level level, int x, int y, int zIndex, Types type) {
        LevelBlock lb = level.get(x,y);
        construct(lb, zIndex, type);
    }

    public LevelObject(LevelBlock levelBlock, Graphics graphics, Types type) {
        construct(levelBlock, graphics.getzIndex(), type);
        setGfx(graphics);
    }

    /*LevelObject(int windowVPixelXPos, int windowVPixelYPos) {
        LevelObject(windowVPixelXPos,windowVPixelYPos,maxzIndex);
    }
*/
    public void setGfx(Graphics gfx) {
        this.gfx = gfx;
    }

    public void draw(PlayWindow playwindow) {
        if(gfx != null) // castle parts can be null or invisible things
            gfx.draw(playwindow,level,x,y,animDelta);
    }

    public void drawMini(PlayWindow playwindow, SpriteBatch batch) {
        if(gfx != null) // castle parts can be null or invisible things
            gfx.drawMini(playwindow,level,batch,x,y,animDelta);
    }

    @Override
    public int compareTo(LevelObject lo) {
        return  zIndex - lo.zIndex;
    }

    public LevelBlock moveTo(int vpx, int vpy, LevelBlock headingTo ) {
        LevelBlock from = levelBlock;
        // check and eventually fix coordinates
        // if(Game.getScrollX()) { always allow
        if (vpx < 0) vpx += level.getWidth() << PlayWindow.virtualBlockResolutionExponent;
        if (vpx >= level.getWidth() << PlayWindow.virtualBlockResolutionExponent)
            vpx -= level.getWidth() << PlayWindow.virtualBlockResolutionExponent;
        //}
        //if(Game.getScrollY()) {
        if (vpy < 0) vpy += level.getHeight() << PlayWindow.virtualBlockResolutionExponent;
        if (vpy >= level.getHeight() << PlayWindow.virtualBlockResolutionExponent)
            vpy -= level.getHeight() << PlayWindow.virtualBlockResolutionExponent;
        //}

        // needs to be updated to check for collisions via associations
        if (from != headingTo) {
            if(from != null)
                from.remove(this);
            /* deals already with rockmes // check if rock and update rockme counters
            if (type == LevelObject.Types.Rock) {
                // Check, if we are on a rockme
                if (from.isRockme()) level.increaseRockmes();
            }
            // happens in setLevelBlock: headingTo.putMoveable(this);
            */
        }
        setLevelBlock( headingTo ); // todo: might be not totally correct for destination
        setXY(vpx,vpy);
        return levelBlock;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * assign a matching LevelBlock based on the current coordinates
     */
    public void assignLevelBlock(PlayWindow playwindow) {
        setLevelBlock( level.getLevelBlockFromVPixel(x, y) );
    }

    public boolean hasGfx() {
        return gfx != null;
    }

    public void setMover(Mover mover) {
        this.mover = mover;
    }

    public Mover getMover() {
        return mover;
    }

    /**
     * make sure to remove yourself from list
     * also cleans itself from associated levelblock
     */
    public void dispose() {
        if(mover!=null) {
            mover.remove(this);
        }
        if(levelBlock != null ) { // has levelBlock
            levelBlock.remove(this);
            levelBlock = null;
        }
        level.removeFromAllLevelObjects(this);
    }

    public boolean isIndestructable() {
        return type == Types.IndestructableWall;
    }

    public boolean isInvisible() {
        return type == Types.InvisibleWall;
    }

    public boolean isRockme() {
        return type == Types.Rockme;
    }

    public void setHoleLevel(int holeLevel) {
        this.holeLevel = holeLevel;
        // set gfx
        Graphics[] holes =
                new Graphics[]{Entities.holes_0, Entities.holes_1,
                        Entities.holes_2, Entities.holes_3, Entities.holes_4};
        this.setGfx(holes[holeLevel]);
    }

    public boolean increaseHole(Audio audio) {
        holeLevel ++;
        if(holeLevel > maxHoleLevel) {
            holeLevel = maxHoleLevel;
            return false;
        }
        // it got bigger
        audio.soundPlay("holegrow");
        setHoleLevel(holeLevel);
        return true;
    }

    public int getVX() {
        return x;
    }
    public int getVY() {
        return y;
    }

    public void setDoorType(DoorTypes doorType) {
        this.doorType = doorType;
    }

    public DoorTypes getDoorType() {
        return doorType;
    }

    public Types getType() {
        return type;
    }

    public LevelBlock getLevelBlock() {
        return levelBlock;
    }

    void animationStartNow(Game game) {
        int len = gfx.getAnimationFramesLength();

        animDelta = len - (int)(game.getTimerFrame() % (long)len); // TODO: check if we need to use Timer or Animationframe here
    }

    void animationStartRandom(Game game) {
        animDelta = game.random(gfx.getAnimationFramesLength());
    }

    public boolean fullOnBlock() {
        return (getVX() % PlayWindow.virtualBlockResolution  == 0) && (getVY() % PlayWindow.virtualBlockResolution == 0);
    }

    public boolean holeIsMax() {
        return holeLevel == maxHoleLevel;
    }

    public void setOneWayGfx(int i) {
        LevelObject lo = this;
        switch (i) {
            case 0:
                lo.setGfx(Entities.arrows_static_up);
                break;
            case 1:
                lo.setGfx(Entities.arrows_static_right);
                break;
            case 2:
                lo.setGfx(Entities.arrows_static_down);
                break;
            case 3:
                lo.setGfx(Entities.arrows_static_left);
                break;
            case 4:
                lo.setGfx(Entities.arrows_rotatable_up);
                break;
            case 5:
                lo.setGfx(Entities.arrows_rotatable_right);
                break;
            case 6:
                lo.setGfx(Entities.arrows_rotatable_down);
                break;
            case 7:
                lo.setGfx(Entities.arrows_rotatable_left);
                break;
        }

    }

    public void setLevelBlock(LevelBlock newBlock) {
        if(newBlock != levelBlock) { // new levelBlock
            if (levelBlock != null) { // it has one assigned, so it needs to be removed
                levelBlock.remove(this);
            }
            // now we know that it has nothing assigned
            levelBlock = newBlock;
            if(newBlock != null) {
                levelBlock.add(this);
            }
        }
    }

    public int getGhostNr() {
        if(type.ordinal() >= Types.Ghost1.ordinal() && type.ordinal() <= Types.Ghost4.ordinal() ) {
            return type.ordinal() - Types.Ghost1.ordinal();
        }
        return -1;
    }

    public int getzIndex() {
        return zIndex;
    }

}
