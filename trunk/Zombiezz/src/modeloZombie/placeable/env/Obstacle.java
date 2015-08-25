package modeloZombie.placeable.env;

import java.awt.Color;

import modeloZombie.utils.ObstacleType;

/**
 * extends EnvironmentalElement
 * 
 * <p>
 * <b>MUD</b> (0.28, 2, false, false, true, true) orange
 * <p>
 * <b>LAKE</b> (0.35, 4,true, false, false, true) blue
 * <p>
 * <b>WALL</b> (0.5, 3, false, false, true, false) gray
 * <p>
 * <b>NOTHING</b> black
 * <p>
 * {@link ObstacleType}
 * 
 * @author Cristina
 * 
 */
public class Obstacle extends EnvironmentalElement {
    private final ObstacleType _type;
    private final static Color BROWN = new Color(139, 69, 19);

    private double _decreaseSpeed;
    private int _visibilityRange;
    private boolean _zombieLifeTaker;
    private boolean _survivorLifeTaker;

    /**
     * <b>initObs</b> instantiates obstacle characteristics:
     * <p>
     * _decreaseSpeed, _visibilityRange, _survivorLifeTaker, _zombieLifeTaker
     * 
     * @param
     */
    private void initObs(double decreaseSpeed_, int visibilityRange_, boolean survivorLifeTaker_,
            boolean zombieLifeTaker_) {
        _decreaseSpeed = decreaseSpeed_;
        _visibilityRange = visibilityRange_;
        _survivorLifeTaker = survivorLifeTaker_;
        _zombieLifeTaker = zombieLifeTaker_;
    }

    /**
     * <b>Obstacle</b> constructor - initializes obstacle types with their own
     * characteristics:
     * <p>
     * COLOR, _decreaseSpeed, _visibilityRange, _survivorLifeTaker,
     * _zombieLifeTaker, _type
     * 
     * @param type_
     */
    public Obstacle(ObstacleType type_) {
        super(getColor(type_));
        if (ObstacleType.LAKE == type_) {
            initObs(0.5, 4, true, false);
        } else if (ObstacleType.MUD == type_) {
            initObs(0.75, 2, false, false);
        } else if (ObstacleType.WALL == type_) {
            initObs(0.02, 3, false, false);
        }
        _type = type_;

    }

    /**
     * 
     * <b>getObstacleType</b> returns the type of obstacle it is
     * 
     * @params
     * @return ObstacleType <code>_type</code>
     *         <p>
     *         Cristina 13 Apr 2011
     */
    public ObstacleType getType() {
        return _type;
    }

    /**
     * 
     * <b>getDecreaseSpeed</b> returns the speed an agent might have while on an
     * obstacle
     * 
     * @params
     * @return int <code>_decreaseSpeed</code>
     *         <p>
     *         Cristina 13 Apr 2011
     */
    public double getDecreaseSpeed() {
        return _decreaseSpeed;
    }

    /**
     * 
     * <b>getVisibilityRange</b> returns the distance from which an agent can
     * see the obstacle
     * 
     * @params
     * @return int <code>_visibilityRange</code>
     *         <p>
     *         lorelay Apr 14, 2011
     */
    public int getVisibilityRange() {
        return _visibilityRange;
    }

    /**
     * <b>isSurvivorLifeTaker</b> returns if it kills a survivor
     * 
     * @params
     * @return boolean <code>_survivorLifeTaker</code>
     *         <p>
     *         Cristina 13 Apr 2011
     */
    public final boolean isSurvivorLifeTaker() {
        return _survivorLifeTaker;
    }

    /**
     * <b>isZombieLifeTaker</b> returns if it kills a zombie
     * 
     * @params
     * @return boolean <code>_zombieLifeTaker</code>
     *         <p>
     *         Cristina 13 Apr 2011
     */
    public final boolean isZombieLifeTaker() {
        return _zombieLifeTaker;
    }

    /**
     * <b>getColor</b> returns the color for each type of obstacle
     * 
     * @param type_
     *            ObstacleType
     * @return Color
     *         <p>
     *         Cristina 12 June 2011
     */
    public final static Color getColor(ObstacleType type_) {
        if (ObstacleType.LAKE == type_) {
            return Color.BLUE;
        } else if (ObstacleType.MUD == type_) {
            return BROWN;
        } else if (ObstacleType.WALL == type_) {
            return Color.GRAY;
        } else {
            return Color.BLACK;
        }

    }

    /*
     * 
     * (non-Javadoc) toString Gets the string name of each type of obstacle
     * 
     * @params
     * 
     * @return String Cristina 13 Apr 2011
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String str;
        if (ObstacleType.MUD == _type) {
            str = "Mud";
        } else if (ObstacleType.LAKE == _type) {
            str = "Lake";
        } else {
            str = "Wall";
        }
        return str;
    }

}
