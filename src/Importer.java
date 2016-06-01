import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Importer extends JPanel
{
    private String log;
    
    /**
     * Constructor for objects of class Importer
     */
    public Importer()
    {
        super(new BorderLayout());
    }
    
    public Sudoku importSudoku() throws IOException
    {
        JFileChooser chooser = new JFileChooser();
        ImportFilter filter = new ImportFilter();
        filter.addExtension("txt");
        filter.addExtension("rtf");
        filter.setDescription("TXT & RTF files");
        chooser.setFileFilter(filter);
        int[] testsudoku = {0,0,0,0,0,0,0,0,0,
                            0,0,0,0,0,0,0,0,0,
                            0,0,0,0,0,0,0,0,0,
                            0,0,0,0,0,0,0,0,0,
                            0,0,0,0,0,0,0,0,0,
                            0,0,0,0,0,0,0,0,0,
                            0,0,0,0,0,0,0,0,0,
                            0,0,0,0,0,0,0,0,0,
                            0,0,0,0,0,0,0,0,0};
        Sudoku thissudoku = new Sudoku(testsudoku);
        int returnVal = chooser.showOpenDialog(Importer.this);
        if(returnVal == JFileChooser.APPROVE_OPTION) // the user selected a file which was correct
        {
            File thisfile = chooser.getSelectedFile(); 
            int[] sudokuarray = parseFile(thisfile);
            thissudoku = new Sudoku(sudokuarray);
        }
        return thissudoku;
    }
    
    private int[] parseFile(File thisfile) throws IOException
    {
        int[] sudokunumbers = new int[81];
        FileInputStream finputstream = new FileInputStream(thisfile); // first we create a fileReader, capable of reading chars
        InputStreamReader streamreader = new InputStreamReader(finputstream); // then we create a BufferedReader, capable of bufferin
        int innercounter = 0;
        int log_numbers = 0;
        int log_empty = 0;
        boolean finishedreading = false;
        while((innercounter <= 80) && (!finishedreading))
        {
            int readnumber = streamreader.read();
            if ((readnumber != -1) && (readnumber != 32))
            {
                String aChar = new Character((char)readnumber).toString();
                int currentnumber = 0;
                try 
                { 
                    currentnumber = Integer.parseInt(aChar);
                    sudokunumbers[innercounter] = currentnumber;
                    log_numbers++;
                    innercounter++;
                } 
                catch (NumberFormatException e) 
                    {}
            }
            else if (readnumber == 32)
            {
                sudokunumbers[innercounter] = 0;
                log_empty++;
                innercounter++;
            }
            else{finishedreading = true;}
       }
       if(log_numbers == 0)
       {log = "No useful data was found in imported file: " + thisfile.getName() + ".";}
       else
       {log = "Imported sudoku data from "+ thisfile.getName() + ". Please verify and correct if necessary.";}
       return sudokunumbers;
    }
   
    public String getLog()
    { return log; }
}
