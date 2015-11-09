package com.mcminos.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Collections;

/**
 * Created by ulno on 17.08.15.
 *
 * Actual objects of a Level. Walls, Main, Doors, Ghosts and pills are
 * created here. the corresponding graphics are in Graphics
 */
public class LevelObject implements  Comparable<LevelObject> {

    public final static int maxzIndex=10000;
    private int x; // windowVPixelXPos-Position in level blocks * virtualBlockResolution
    private int y; // windowVPixelYPos-Position in level blocks * virtualBlockResolution
    private Graphics gfx; // actual Graphics for the object
    private int zIndex = maxzIndex; // by default it is too high
    private Mover mover = null; // backlink
    private Level level = null;
    private LevelBlock levelBlock = null; // currently associated LevelBlock
    private int holeLevel;
    public static final int maxHoleLevel = 4; // maximum open
    private int animDelta = 0; // how much offset for the animation
    private Types type;
    private DoorTypes doorType = DoorTypes.None;
    private Game game;
    private Audio audio;

    public enum Types {Unspecified, Power1, Power2, Power3,
        IndestructableWall, InvisibleWall, Rockme, Live, Letter,
        Skull, Bomb, Dynamite, Rock, Pill, Castle, McMinos, Wall, Background, Key, Umbrella,
        DoorClosed, DoorOpened, SpeedUpField, SpeedDownField, WarpHole, KillAllField, OneWay,
        Chocolate, LandMine, LandMineActive, LandMineExplosion, BombFused, DynamiteExplosion,
        BombExplosion, DestroyedWall, Ghost1, Ghost2, Ghost3, Ghost4, KillAllPill, Exit, Bonus1, Bonus2, Bonus3, Whisky, Mirror, Poison, Medicine, SkullField, Hole};
    public enum DoorTypes {None, HorizontalOpened,HorizontalClosed, VerticalOpened,VerticalClosed};

    private void construct(LevelBlock levelBlock, int zIndex, Types type) {
        level = levelBlock.getLevel();
        this.levelBlock = levelBlock;
        game = level.getGame();
        audio = game.getAudio();
        // does not exist here playwindow = game.getPlayWindow();
        x = levelBlock.getX() << PlayWindow.virtualBlockResolutionExponent;
        y = levelBlock.getY() << PlayWindow.virtualBlockResolutionExponent;
        this.zIndex = zIndex;
        this.type = type;
        level.addToAllLevelObjects(this);
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
            gfx.draw(playwindow,x,y,animDelta);
    }

    public void drawMini(PlayWindow playwindow, SpriteBatch batch) {
        if(gfx != null) // castle parts can be null or invisible things
            gfx.drawMini(playwindow,batch,x,y,animDelta);
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

//        LevelBlock to = game.getLevelBlockFromVPixel(vpx, vpy);
        // needs to be updated to check for collisions via associations
        if( from != headingTo ) {
            //if (from != null) { should not happen when properly intialized
                from.removeMovable(this);
                // check if rock and update rockme counters
                if (type == LevelObject.Types.Rock) {
                    // Check, if we are on a rockme
                    if (from.isRockme()) level.increaseRockmes();
                }
            //}
            headingTo.putMoveable(this);
        }
        levelBlock = headingTo; // todo: might be not totally correct for destination
        setXY(vpx,vpy);
        return levelBlock;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * assign a matching LevelBlock based on th ecurrent coordinates
     */
    public void assignLevelBlock() {
        levelBlock = game.getLevelBlockFromVPixel(x, y);
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

    // make sure to remove yourself from list
    public void dispose() {
        level.removeFromAllLevelObjects(this);
        // TODO: think if we also have to remove from other things in level-block
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

    public boolean increaseHole() {
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

    void animationStartNow() {
        int len = gfx.getAnimationFramesLength();

        animDelta = len - (int)(game.getGameFrame() % (long)len);
    }

    void animationStartRandom() {
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

    public void setLevelBlock(LevelBlock levelBlock) {
        this.levelBlock = levelBlock;
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
