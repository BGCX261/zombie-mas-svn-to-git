/**
 *     Interfata Predator - Prey 
 */
package Interface;

/**
 * @author lorelay
 * 
 */
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class SimResultsUI extends JFrame {

    enum tableHeader {
        Prey_ID, Move, Prec, Weapon, Status
    };

    JButton BtnBrowse;
    JTextArea resultTextArea;

    public void disposeOfFrame() {
        this.dispose();
    }

    public SimResultsUI(String name, final String simResultTxt) {
        super(name);
        this.setBounds(650, 400, 300, 500);
        this.setResizable(true);
        this.setSize(600, 270);

        resultTextArea = new JTextArea();
        resultTextArea.setBounds(40, 20, 200, 150);
        resultTextArea.setAutoscrolls(true);
        resultTextArea.setEditable(false);

        resultTextArea.setText("Prey_ID\t Move\t Prec\t Weapon \t Status\n" + simResultTxt);
        this.add(resultTextArea);

        this.setVisible(true);
        this.pack();
    }

    /*
     * public static void main(String[] args) { SimResultsUI frame = new
     * SimResultsUI("Predator-Prey Simulaion Results");
     * 
     * }
     */

}