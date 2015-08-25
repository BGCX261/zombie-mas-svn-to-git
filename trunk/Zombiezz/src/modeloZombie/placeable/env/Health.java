/**
 * 
 */
package modeloZombie.placeable.env;

import java.awt.Color;

/**
 * extends EnvironmentalElement
 * <p>
 * COLOR = <b>cyan</b>
 * 
 * @author Cristina
 * 
 */
public class Health extends EnvironmentalElement {
    protected final int HEALTH_INCR;

    public Health() {
        super(Color.cyan);
        HEALTH_INCR = 2;
    }

    /**
     * 
     * <b>getHealthInc</b> returns the amount of health restored with one unit
     * of health
     * 
     * @params
     * @return Cristina 26 Apr 2011
     */
    public int getHealthInc() {
        return HEALTH_INCR;
    }

}
