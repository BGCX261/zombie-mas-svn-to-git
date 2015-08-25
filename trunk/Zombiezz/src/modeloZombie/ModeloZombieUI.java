package modeloZombie;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import modeloZombie.placeable.env.EnvironmentalElement;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

public class ModeloZombieUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;
    private ObjectGridPortrayal2D agentRepresentation;
    public ObjectGridPortrayal2D envRepresentation;
    private Controller theController;

    public static void main(String[] args) {
	ModeloZombieUI sim = null;
	sim = new ModeloZombieUI();

	Console console = new Console(sim);
	console.setVisible(true);
    }

    public ModeloZombieUI() {
	super(new ModeloZombie(System.currentTimeMillis()));
    }

    @Override
    public void init(Controller controller_) {
	super.init(controller_);
	theController = controller_;
	setupDisplay();
    }

    /**
     * 
     * <b>setupDisplay</b> TODO purpose
     * 
     * @param
     * @return Cristina 26 Apr 2011
     */
    private void setupDisplay() {
	final ModeloZombie theModel = (ModeloZombie) state;
	//
	int xSpaceSize = theModel.getEnvironmentSizeX();
	int ySpaceSize = theModel.getEnvironmentSizeY();

	display = new Display2D(6 * xSpaceSize, 6 * ySpaceSize, this, 1);
	displayFrame = display.createFrame();
	theController.registerFrame(displayFrame);
	// register the frame so it appears in the "Display" list
	displayFrame.setVisible(true);

	// attach the portrayals
	// agents
	agentRepresentation = new ObjectGridPortrayal2D() {
	    @Override
	    public Portrayal getDefaultPortrayal() {
		return new OvalPortrayal2D(Color.magenta, 1.0) {
		    @Override
		    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			if (object instanceof Agent) {
			    paint = ((Agent) object).getColor();
			    super.draw(object, graphics, info);
			}
		    }
		};
	    }
	};

	// environment
	envRepresentation = new ObjectGridPortrayal2D() {
	    @Override
	    public Portrayal getDefaultPortrayal() {
		return new RectanglePortrayal2D(Color.cyan) {
		    @Override
		    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			if (object instanceof EnvironmentalElement) {
			    EnvironmentalElement envElement = (EnvironmentalElement) object;

			    paint = envElement.getColor();
			    super.draw(object, graphics, info);

			}
		    }
		};
	    }
	};

	display.attach(envRepresentation, "Environment");
	display.attach(agentRepresentation, "Agents");
	display.setBackdrop(Color.black);
    }

    @Override
    public void start() {
	super.start();
	// setupDisplay();
	agentRepresentation.setField(((ModeloZombie) state).getAgentMap());
	envRepresentation.setField(((ModeloZombie) state).getEnvironmentMap());
	display.reset();
	display.repaint();

    }

    /**
     * 
     */
    @Override
    public void quit() {
	super.quit();

	if (displayFrame != null)
	    displayFrame.dispose();
	displayFrame = null;
	display = null;
    }

    /*
     * public void reset(){ super.reset(state);
     * 
     * start(); }
     */

    @Override
    public Object getSimulationInspectedObject() {
	return state;
    }

}
