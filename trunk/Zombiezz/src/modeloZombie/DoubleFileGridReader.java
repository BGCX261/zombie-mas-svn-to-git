// a class to read habitat data
// the assumption is that the file starts with 3 lines
// to be ignored (makes the file more readable for humans)
// 
// then each line in the remainder of the file consists
// of an x co-ordinate, a y co-ordinate, and a data point
// (a double).  The biggest x and y co-ordinates come first;
// that way the size of the space can be immediately determined
package modeloZombie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class DoubleFileGridReader {
    private BufferedReader theReader;
    private boolean eof;
    private int nextX, nextY;
    private double nextData;

    public void close() {
        try {
            theReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public DoubleFileGridReader(String fileName) {
        try {
            theReader = new BufferedReader(new FileReader(fileName));
            // skip the first 3 lines, since that's the format of data files
            theReader.readLine();
            theReader.readLine();
            theReader.readLine();
        } catch (IOException exp) {
            System.err.println("Error:  input file not found");
            wrongFormat();
        }
    }

    public boolean nextData() {
        if (eof)
            return false;
        String nextLine = null;
        try {
            nextLine = theReader.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (nextLine == null) {
            eof = true;
            return false;
        } else
            try {
                StringTokenizer izer = new StringTokenizer(nextLine);
                if (!izer.hasMoreTokens())
                    wrongFormat();
                nextX = Integer.parseInt(izer.nextToken());
                if (!izer.hasMoreTokens())
                    wrongFormat();
                nextY = Integer.parseInt(izer.nextToken());
                if (!izer.hasMoreTokens())
                    wrongFormat();
                nextData = Double.parseDouble(izer.nextToken());
                return true;
            } catch (NumberFormatException exp) {
                wrongFormat();
            }
        return false;
    }

    public int getX() {
        return nextX;
    }

    public int getY() {
        return nextY;
    }

    public double getData() {
        return nextData;
    }

    public void wrongFormat() {
        throw new RuntimeException("Error:  input file in wrong format");
    }
}
