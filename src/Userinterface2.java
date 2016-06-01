import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Color;
import java.io.IOException;

/**
 * Swing interface v2.1
 * 
 * This class implements the new GUI for the sudoku solver.
 * 
 * @author Forceflow
 * @version 15/09
 */
public class Userinterface2
{
    private JFrame frame;
    private JPanel sudokupanel;
    private JPanel buttonMAINpanel;
    private JMenuBar menubar;
    private JTextArea statustext;
    private JLabel statusbar;
    private JTextField[][] fields;
    
    private boolean extendedlogging;
    private boolean showstatistics;

    /**
     * Constructor for objects of class Userinterface2
     */
    public Userinterface2()
    {
        buildInterface();
        extendedlogging = true;
        showstatistics = true;
    }
    
    /**
     * This method (re)builds the complete interface
     */
    private void buildInterface()
    {
        // Creating the top level frame
        frame = new JFrame("SudokuSolve 0.2 by Forceflow");
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        // Creating the menu bar & adding the menus
        makeMenuBar();
        frame.setJMenuBar(menubar);
        // Creating the sudokupanel and adding it
        makeSudokuPanel();
        contentPane.add(sudokupanel,BorderLayout.CENTER);
        // Creating the buttonMAINpanel and adding it
        makebuttonMAINpanel();
        contentPane.add(buttonMAINpanel,BorderLayout.SOUTH);
        // Creating the status text area and adding it
        statustext = new JTextArea(9,20);
        statustext.setLineWrap(true);
        statustext.setWrapStyleWord(true);
        statustext.setEditable(false);
        JScrollPane areaScrollPane = new JScrollPane(statustext);
        areaScrollPane.setPreferredSize(new Dimension(500, 230));
        contentPane.add(areaScrollPane,BorderLayout.EAST);
        output("Welcome to SudokuSolve 0.2"+ "\n" + "Enter your sudoku and click solve to start." + "\n");
        
        // Finishing UI
        frame.pack();
        frame.setVisible(true);
    }
   /**
    * This private methods constructs the menu bar
    */
    private void makeMenuBar()
    {
        // Creating menu
        menubar = new JMenuBar();
        // Adding menu items
        JMenu programMenu = new JMenu("Program");
        menubar.add(programMenu);
        JMenu sudokuMenu = new JMenu("Sudoku");
        menubar.add(sudokuMenu);
        JMenu helpMenu = new JMenu("Help");
        menubar.add(helpMenu);
        JMenuItem importitem = new JMenuItem("Import");
        importitem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){importSudoku();}});
        JMenuItem clearsudokuitem = new JMenuItem("Clear");
        clearsudokuitem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){clearSudoku();}});
        sudokuMenu.add(clearsudokuitem);
        sudokuMenu.add(importitem);
    }
   /**
    * This private methods constructs the Sudoku panel
    */
    private void makeSudokuPanel()
    {
        // Creating SudokuPanel
        sudokupanel = new JPanel(new GridLayout(9,9));
        // Adding textfields & making conversion matrix
        fields = new JTextField[10][10];
        int rowcounter = 1; int columncounter = 1;
        for(int i = 0; i<= 80; i++)
        {
            JTextField sudokufield = new JTextField(1);
            sudokufield.setHorizontalAlignment(JTextField.CENTER);
            sudokupanel.add(sudokufield);
            // generate conversion matrix so we know which(otherwise anonymous) field represents which cell of the sudoku
            fields[rowcounter][columncounter] = sudokufield;
            columncounter++; if(columncounter == 10){columncounter = 1;rowcounter++;}
        }
        sudokupanel.setPreferredSize(new Dimension(200,200));
    }
   /**
    * This private methods constructs the lower panel
    */
    private void makebuttonMAINpanel()
    {
        // Creating buttonMAINpanel
        buttonMAINpanel = new JPanel();
        buttonMAINpanel.setLayout(new BorderLayout());
        
        // Creating subpanels
        JPanel buttonpanel2 = new JPanel();
        JPanel buttonpanel3 = new JPanel();
        
        // Creating buttons
        JButton clearButton = new JButton("Clear Sudoku");
        clearButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){clearSudoku();}});
        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){solveSudoku();}});
        JButton clearButton2 = new JButton("Clear Log");
        clearButton2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e){clearLog();}});
        
        // Creating checkboxes
        final JCheckBox extendedlog = new JCheckBox ("Extended log");
        extendedlog.setSelected(true);
        extendedlog.addItemListener(new ItemListener() 
        {public void itemStateChanged(ItemEvent e)
            {Object source = e.getItemSelectable();
             if (e.getStateChange() == ItemEvent.SELECTED) {setExtendedLogging(true);}
             if (e.getStateChange() == ItemEvent.DESELECTED) {setExtendedLogging(false);}
            }});
        final JCheckBox showstatistics = new JCheckBox ("Show statistics");
        showstatistics.setSelected(true);
        showstatistics.addItemListener(new ItemListener() 
        {public void itemStateChanged(ItemEvent e)
            {Object source = e.getItemSelectable();
             if (e.getStateChange() == ItemEvent.SELECTED) {setShowstatistics(true);}
             if (e.getStateChange() == ItemEvent.DESELECTED) {setShowstatistics(false);}
            }});
        
        // Adding panels and items
        buttonpanel3.add(solveButton); 
        buttonpanel3.add(clearButton);
        buttonpanel2.add(extendedlog);
        buttonpanel2.add(showstatistics);
        buttonpanel2.add(clearButton2);
        buttonMAINpanel.add(buttonpanel2, BorderLayout.EAST);
        buttonMAINpanel.add(buttonpanel3, BorderLayout.WEST);
    }
   /**
    * This method is called when the user hits the solve button
    * It checks the sudoku for invalid characters, and invalid sudoku input.
    * After that, it solves the sudoku.
    */
    private void solveSudoku()
    {
        output("\n" + "Started solving sudoku ..." + "\n" + "\n");
        boolean valid = true; // this boolean can stop the process at any time, by flagging false
        // STEP 1: we validate the user input = check it for invalid characters and typos
        valid = validateInput();
        if(!valid) // problem
        { output("Please correct the sudoku and try again." + "\n");}
        else // no problem
        {
            // we generate the sudoku
            Sudoku currentsudoku = new Sudoku(createSudokuArray());
            // STEP 2: we validate the created sudoku for impossibilities = check if user doesn't input bogus sudokus
            valid = validateSudoku(currentsudoku);
            if(!valid) // problem
            { output("Please correct the sudoku and try again." + "\n");}
            else // no problem, let's solve the sudoku
            {
                markCurrent();
                currentsudoku.solve();
                outputSudoku(currentsudoku);
                
                // add solve log and statistics to output area
                if(extendedlogging)
                {output(currentsudoku.getOutputlog() + "\n");}
                if(showstatistics)
                {output(currentsudoku.getStatistics() + "\n");}
                
                // add the finishing info the output area
                if(currentsudoku.getAllFoundFlag()){output("All sudoku values have been found.");noEdits();}
                else{output("Not all sudoku values have been found.");}
                if(currentsudoku.getContainsErrorsFlag())
                {output("ERROR: Solving process created errors. Please send the output log and the exact sudoku to" + "\n" +
                    "sudokusolve.sourceforge.net. Thanks in advance !");}
                
            }

        }
    }
    
    /**
     * This method validates the sudoku itself. Impossible sudokus (containing identical numbers in the same
     * row, box or column) will be rejected by the program, so we don't start the solve process with a bogus
     * sudoku.
     * 
     * It marks the rows, columns or boxes where the problem occurs.
     * 
     * Note that this method only accepts sudokus that already have been generated by the GUI. (avoiding double work)
     * 
     * @ returns True if the sudoku is valid
     */
    private boolean validateSudoku(Sudoku thissudoku)
    {
        boolean valid = thissudoku.validator();
        if(!valid)
        {
            // mark the groups with problems
            int[][] errorcells = thissudoku.getErrorCells();
            int howmany = errorcells.length;
            for(int i = 0; i <= howmany-1; i++)
            {markField(errorcells[i][0], errorcells[i][1]);}
            // output the problems to the log
            String text = thissudoku.getErrorInfo();
            output(text);
        }
        return valid;
    }
            
       
    
    /**
     * This method validates user input (but does NOT check if the sudoku is valid !)
     * Every box should contain 1 number, from 1 to 9. No characters or other ASCII-stuff.
     * Errors are logged to the status area.
     * The method stops when the first error is found.
     * 
     * @ returns Boolean true if the input is validated
     */
    private boolean validateInput()
    {
        unmarkFields();
        boolean valid = true;
        String[][] strings = collectsudokuStrings();
        for (int row = 1;row <=9;row++)
        {
        for (int column = 1; column<=9; column++)
            {
                String currentstring = strings[row][column];
                // character check
                try {Integer.parseInt(currentstring);} 
                    catch (NumberFormatException e) 
                    { 
                       if(!(currentstring.equals("")))
                          {   valid = false;
                              markField(row,column);
                              output("Problem at ("+row+","+column+") : Invalid character (" + currentstring + ")." + "\n");}
                    }
                // length check
                if(currentstring.length() > 1) 
                    {valid = false; 
                     markField(row,column);
                     output("Problem at ("+row+","+column+") : Too many characters(" + currentstring + ")." +"\n");}
                // zero check
                if(currentstring.equals("0"))
                {valid = false; 
                     markField(row,column);
                     output("Problem at ("+row+","+column+") : 0 is not a valid number. Leave field blank for unknown number." +"\n");}    
            }
        }
        return valid;
    }
    
    /**
     * This method is called to output a given sudoku to the sudoku panel (preferably called after it has been solved,
     * but also useful to bring up the earlier sudokus)
     */
    private void outputSudoku(Sudoku thissudoku)
    {
        int[][] sudokumatrix = thissudoku.getSudokuMatrix();
        for(int row = 1; row <=9; row++)
        {
            for(int column = 1; column <=9; column++)
            {
                String text = "" + sudokumatrix[row][column];
                if(!(text.equals("0")))
                {fields[row][column].setText(text);}
                else{fields[row][column].setText("");}
            }
        }
    }
        
    /**
     * This method collects the user input from the sudokupanel
     */
    private String[][] collectsudokuStrings()
    {
        String[][] strings = new String[10][10];
        for (int row=1;row<=9;row++)  
        {
            for (int column=1;column<=9;column++)
            {
                JTextField field = fields[row][column];
                strings[row][column] = field.getText();
            }
        }
        return strings;
    }
    
    /**
     * This method recombines the user input into a (single-lined) Sudoku Array which is passed on to the solve algorithm.
     * Note that this method also includes an error catching part, but only as a last resort: checking should be performed earlier.
     */
    private int[] createSudokuArray()
    {
        String[][] strings = collectsudokuStrings(); // we collect the user input (straight from fields)
        int[] sudokuarray = new int[81]; // this will become the sudokuarray needed to build it
        int innercounter = 0; /// this counter helps positioning the numbers in the 
        for(int row = 1; row <=9; row++)
        {for(int column = 1; column <=9; column++)
                {int currentnumber = 0;
                 try{ currentnumber = Integer.parseInt(strings[row][column]);}
                 catch(NumberFormatException e) {currentnumber = 0;};
                 sudokuarray[innercounter] = currentnumber;
                 innercounter++;}}
        return sudokuarray;
    }
    
    /**
     * This method clears the sudokupanel of markings, user input and highlights.
     */
    private void clearSudoku()
    {
        for (int row = 1;row <=9;row++)
        {for (int column = 1; column<=9; column++)
            {JTextField field = fields[row][column];
             field.setBackground(Color.white);
             field.setText("");
             field.setEditable(true);}}
    }
    
    public void importSudoku()
    {
        Importer thisimporter = new Importer();
        try
        {
            Sudoku thissudoku = thisimporter.importSudoku();
            outputSudoku(thissudoku);
            output(thisimporter.getLog());
        }
        catch(IOException e)
        {
            output("There was an unknown error importing your file.");
        }
    }
    
    /**
     * Marks a certain cell on the sudokupanel
     */
    private void markField(int row, int column)
    {
        JTextField field = fields[row][column];
        field.setBackground(Color.red);
    }
    
    /**
     * Marks the currently filled-in cells
     */
    private void markCurrent()
    {
        for (int row = 1;row <=9;row++)
        {for (int column = 1; column<=9; column++)
            {
                if (!fields[row][column].getText().equals("")){
                fields[row][column].setBackground(Color.lightGray);}
            }
        }
    }
        
    
    /**
     * Unmarks all fields on the sudokupanel
     */
    private void unmarkFields()
    {
        for (int row = 1;row <=9;row++)
        {for (int column = 1; column<=9; column++)
            {JTextField field = fields[row][column];
             field.setBackground(Color.white);}
        }
    }
    
    /**
     * Render the entire sudokupanel un-editable by user
     */
    private void noEdits()
    {
       for (int row = 1;row <=9;row++)
        {for (int column = 1; column<=9; column++)
            {JTextField field = fields[row][column];
             field.setEditable(false);}
        }
    }
    /**
     * Clear the log
     */
    private void clearLog(){statustext.setText("");}
    
    /**
     * Method to output text to the status area
     */
    private void output(String text)
    {
        statustext.append(text);
        statustext.setCaretPosition(statustext.getDocument().getLength());
    }
    
    private void setExtendedLogging(boolean toggle)
    {
        extendedlogging = toggle;
//         System.out.println("Toggle log: " + toggle);
    }
    private void setShowstatistics(boolean toggle)
    {
        showstatistics = toggle;
//         System.out.println("Toggle stat: " + toggle);
    }
    
}