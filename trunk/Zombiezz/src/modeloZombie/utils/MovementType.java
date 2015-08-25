package modeloZombie.utils;

/**
 * <p>
 * <b> RANDOM </b> all movements are valid
 * <p>
 * <b>SEMI_RANDOM </b> (at least) on one direction, the distance shouldn't get
 * bigger
 * <p>
 * <b>INDIRECT </b> shouldn't get further away than the previous distance
 * <p>
 * <b>DIRECT </b> towards the goal in both directions
 * <p>
 * 
 * TODO
 * 
 * @author lorelay
 * 
 */
public enum MovementType {
    RANDOM, SEMI_RANDOM, INDIRECT, DIRECT;

}
