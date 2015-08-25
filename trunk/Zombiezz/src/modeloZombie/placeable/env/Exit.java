/**
 * 
 */
package modeloZombie.placeable.env;

import java.awt.Color;

/**
 * extends EnvironmentalElement
 * <p>
 * COLOR = <b>white</b>
 * 
 * @author Cristina
 * 
 */
public class Exit extends EnvironmentalElement {
    public Exit() {
        super(Color.white);
    }

    @Override
    public String toString() {
        return "Exit";
    }
}
