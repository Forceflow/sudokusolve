import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Math;

/**
 * This is the class representing the Sudoku.
 * @author Forceflow
 * @version 21/07/2006
 */
public class Sudoku
{
    /**
     * Fields
     */
    
    // the sudoku itself
    private Element[][] Elements;
    
    // the next items contain the current sudoku validation info
    private boolean allfound;
    private boolean containserrors; // of course, this should remain false all the time :)
    
    // These variables can be used in GUI as additional info (when running into errors)
    private String log; // all solve info
    private String statistics; // all solve statistics
    private int[][] errorcells; // the cells which contain errors
    private String errors; // textual info about these errors
    
    /**
     * Constructor for class Sudoku
     * @param numberarray The array containing the sudoku numbers, from left to right, top to bottom
     */
    
    public Sudoku(int[] numberarray)
    {
        boolean debugmode = true;
        log = "";
        statistics = "";
        // building the sudoku matrix
        Elements= new Element[9][9] ;
        int counter = 0 ;
        for (int rij = 0; rij <= 8; rij++)
        {
            for (int kolom = 0; kolom <= 8; kolom++)
            {
                /**
                 * Get the right number from the numberarray and create an Element at the right position in the
                 * two-dimensional sudoku array, using the int retrieved from the array as argument.
                 */
                int whichblock = whichBlock(rij,kolom);
                Elements[rij][kolom] = new Element(numberarray[counter],whichblock,rij+1,kolom+1);
                counter++;
            }
        }
    }
    
    /**
     * The solving algorithm
     */
    
    public void solve()
    {
    long starttime = System.currentTimeMillis();    
    boolean debugmode = true;
    boolean ietsgedaan = true;
    int newvaluesfound = 0; // statistics value
    int statistics_hiddensingles = 0;
    int statistics_lockedcandidates1 = 0;
    int statistics_lockedcandidates2 = 0;
    int statistics_nakedpairs = 0;
    int statistics_nakedtriples = 0;
    int statistics_hiddenpairs = 0;
    int runs = 0;
    while((ietsgedaan) && (!quickvalidator()) )
    {
        // Is our algorithm still running, and how many steps did it take to get here ?
        ietsgedaan = false;
        runs++;
        if((debugmode) && (runs > 1)){log = log + "\n" + "\n" + "### Starting run " + runs +" ..."+ "\n";}
        if((debugmode) && (runs == 1)){log = log + "### Starting run " + runs +" ..."+ "\n";}
        
        /**
         * BASIC TECHNIQUE: 'Singles'
         * We run over all elements. When they are labeled
         * found, we can safely scrap the possibilities
         * in the element's row, column and block.
         */
        
        for (int rij = 0; rij <= 8; rij++)
        {
            for (int kolom = 0; kolom <= 8; kolom++)
            {
                // Pick the element
                Element currentelement = Elements[rij][kolom];
                // We only start scrapping if the element is labeled found, but hasn't been scrapped yet.
                if ((currentelement.giveFound()) && (!currentelement.giveScrapped()))
                {
                    ietsgedaan = true;
                    
                    // Scrapping in rows and columns
                    int number = currentelement.giveNumber();
                    for(int i=0; i<kolom;i++){Element scrap_in = Elements[rij][i];
                        scrap_in.scrapnumber(number);}
                    for(int i=kolom+1;i<=8;i++){Element scrap_in = Elements[rij][i];
                        scrap_in.scrapnumber(number);}
                    for(int i=0; i<rij;i++){Element scrap_in = Elements[i][kolom];
                        scrap_in.scrapnumber(number);}
                    for(int i=rij+1;i<=8;i++){Element scrap_in = Elements[i][kolom];
                        scrap_in.scrapnumber(number);}
                    // Scrapping in blocks, We check which block the element is in
                    int whichblock = currentelement.giveBlock();
                    // We collect the block elements and start scrapping
                    Element[] block = collectGroupElements(0, whichblock);
                    for(int A=1; A <= 9; A++)
                    {
                        Element currentelement2 = block[A];
                        if(!currentelement2.giveFound())
                        {currentelement2.scrapnumber(number);}
                    }
                    currentelement.setScrapped(true);
                    if(debugmode){log = log + "\n" + "Scrapped value " + number + " in row " + currentelement.giveRow() +
                        ", column " + currentelement.giveColumn() + " and block " + currentelement.giveBlock() + ".";}
                }
            }
        }
        
        /**
         * BASIC TECHNIQUE: Hidden Singles (Block)
         * Very frequently, there is only one candidate for a given row, column or box, but it is hidden among other candidates.
         * ADVANCED TECHNIQUE: Locked Candidates 1 (always Block)
         * Sometimes a candidate within a box is restricted to one row or column. Since one of these cells must contain that 
         * specific candidate, the candidate can safely be excluded from the remaining cells in that row or column outside of the box.
         */

        for(int blockteller = 1; blockteller<10; blockteller++) // giant loop : for every block
        {
            // we collect the block elements
            Element[] blockelements = collectGroupElements(0, blockteller);
            // we generate the possibility matrix
            int[][] numberposib = generateNumberPosib(blockelements);
            // Hidden Singles Code
            for(int teller3=1; teller3 < 10; teller3++)
            {
                // We look for values with exactly one possibility
                if(numberposib[0][teller3] == 1)
                {
                    // We look for the corresponding index to find the block element
                    boolean indexfound = false;
                    int elementnumber = 99;
                    while(!indexfound)
                    {
                        for(int teller4=1; teller4<10;teller4++)
                        {
                            if(numberposib[teller4][teller3] == 1)
                            {elementnumber = teller4;
                             indexfound = true;}
                        }
                    }
                    // By using the index, we ask for the actual element
                    Element blockelement = blockelements[elementnumber];
                    // We start scrapping
                    if(!blockelement.giveFound() && (blockelement.isPossibility(teller3)))
                    {
                        blockelement.scrapallexceptone(teller3);
                        ietsgedaan = true;
                        statistics_hiddensingles++;
                        if(debugmode)
                        {
                            log = log + "\n" + "Marked (and thus found) value " + teller3 +" (in row " + blockelement.giveRow() + ", column "
                            + blockelement.giveColumn() + ") as a Hidden Single (Blocks).";
                        }
                    }
                }      
            }// end of Hidden Singles loop
            
            // Locked Candidates 1 code
            
            // We iterate over 1->9
            for(int teller3=0; teller3 < 10; teller3++)
            {
                // We look for values with more than one possibility
                if(numberposib[0][teller3] > 1)
                {
                    Element[] collectedelements = new Element[numberposib[0][teller3]];
                    // Collect the elements containing the possibility of this value
                    int teller6 = 0;
                    for (int teller5=1; teller5<10; teller5++)
                    {
                        if(numberposib[teller5][teller3] == 1)
                        {
                            collectedelements[teller6] = blockelements[teller5];
                            teller6++;
                        }
                    }
                    
                    // Now we collect all the row and column numbers of these elements
                    
                    int[] rownumbers = new int[numberposib[0][teller3]];
                    int[] colnumbers = new int[numberposib[0][teller3]];
                    int teller8 = 0; 
                    for(int teller7 = 0; teller7 <= collectedelements.length-1; teller7++)
                    {
                        Element thiselement = collectedelements[teller7];
                        rownumbers[teller8] = thiselement.giveRow();
                        colnumbers[teller8] = thiselement.giveColumn();
                        teller8++;
                    }
                    
                    // Now we check whether or not these numbers are all the same
                    // Note: it's never possible for rowOK AND columnOK to be true.
                    boolean rowOK = true;
                    boolean columnOK = true;
                    int teller9 =1;
                    int rownumber = rownumbers[0];
                    int colnumber = colnumbers[0];
                    while((rowOK) && (teller9 <= collectedelements.length-1))
                    {if(rownumbers[teller9] != rownumber){rowOK=false;}teller9++;}
                    teller9=1;
                    while((columnOK) && (teller9 <= collectedelements.length-1))
                    {if(colnumbers[teller9] != colnumber){columnOK=false;}teller9++;}
                    
                    // Now we start scrapping in the other blocks which contain the same row or column.
                    // OPTIMISATION: Could this be done more efficiently ? 2 loops over the entire sudoku are slow :p
                    if(rowOK)
                    {
                        Element[] rowselements = collectGroupElements(1, rownumber);
                        statistics_lockedcandidates1++;
                        if(debugmode){log = log + "\n" + "Scrapped value " + teller3 + " in row " + rownumber 
                                                + " outside block " + blockteller + ". (Locked Candidates 1)";}
                        for(int A = 1; A<=9;A++)
                        {
                        
                            Element thiselem = rowselements[A];
                            if((thiselem.giveBlock() != blockteller) && (thiselem.isPossibility(teller3)) && (!thiselem.giveFound()))
                                        {
                                            thiselem.scrapnumber(teller3);
                                            ietsgedaan = true;
                                        }
                        }
                     }
                     if(columnOK)
                     {
                        Element[] columnselements = collectGroupElements(2, colnumber);
                        statistics_lockedcandidates1++;
                        if(debugmode){log = log + "\n" +"Scrapped value " + teller3 + " in column " + colnumber 
                                                + " outside block " + blockteller + ". (Locked Candidates 1)";}
                         for(int A = 1; A<=9;A++)
                         {
                                
                               Element thiselem = columnselements[A];
                               if((thiselem.giveBlock() != blockteller) && (thiselem.isPossibility(teller3)) && (!thiselem.giveFound()))
                                        {
                                            thiselem.scrapnumber(teller3);
                                            ietsgedaan = true;
                                        }
                         }
                     }
                } // End of if-statement about number of possibilities (>1)
            } // End of loop over all values (loop within giant loop)
       } // End of Hidden Candidates (for Blocks) AND Locked Candidates 1
       
        /**
        * BASIC TECHNIQUE: Hidden Singles (Columns AND Rows)
        * ADVANCED TECHNIQUE: Locked Candidates Segment2 (Columns AND Rows)
        */
       
       for(int uberteller = 0; uberteller <=1; uberteller++) 
       // this uberteller is the key to code re-use. The switch 0 performs the operation for Rows, 1 for columns
       {
       for(int groupteller = 1; groupteller <= 9; groupteller++)
        {
            Element[] groupelements = null; 
            if(uberteller==0)// uberteller = 0, so we fetch the row elements
            {groupelements = collectGroupElements(1, groupteller);}
            if(uberteller==1)// uberteller = 1, so we fetch the column elements
            {groupelements = collectGroupElements(2, groupteller);}
            // Create a two-dimensional array containing the possibility counts
            int[][] numberposib = generateNumberPosib(groupelements);
            
            // Find the one-possibility values and mark them
            for(int teller3=1; teller3 < 10; teller3++)
            {
                if(numberposib[0][teller3] == 1) // if the value has only one possibility in the group ...
                {
                    boolean indexfound = false;
                    int elementnumber = 99;
                    while(!indexfound) // in which element ?
                    {
                        for(int teller4=1; teller4<10;teller4++)
                        {
                            if(numberposib[teller4][teller3] == 1)
                            {
                                elementnumber = teller4;
                                indexfound = true;
                            }
                        }
                    }
                    // By using the index, we ask for the actual element
                    Element groupelement = groupelements[elementnumber];
                    // and start scrapping
                    if(!groupelement.giveFound() && (groupelement.isPossibility(teller3)))
                    {
                        groupelement.scrapallexceptone(teller3);
                        ietsgedaan = true;
                        statistics_hiddensingles++;
                        if((debugmode) && (uberteller==0))
                        {
                            log = log + "\n" +"Marked (and thus found) value " + teller3 +" (in row " + groupelement.giveRow() + ", column "
                            + groupelement.giveColumn() + ") as a Hidden Single (Rows).";
                        }
                        if((debugmode) && (uberteller==1))
                        {
                            log = log + "\n" +"Marked (and thus found) value " + teller3 +" (in row " + groupelement.giveRow() + ", column "
                            + groupelement.giveColumn() + ") as a Hidden Single (Columns).";
                        }
                    }
                }      
            }
            // Locked Candidates 2 code
            for(int teller3=1; teller3 < 10; teller3++)
            {
                if(numberposib[0][teller3] > 1)
                {
                    // Collect the group elements (2 or more) with a possibility on this value (teller3, 1->9)
                    
                    Element[] collectedgroupelements = new Element[numberposib[0][teller3]];
                    int teller4 = 0; // the counter to advance in the new array
                    for(int teller5 = 0; teller5 <= 9; teller5++)
                    {
                        if(numberposib[teller5][teller3] == 1)
                        {
                            // The element is retrieved from the groupelements array and added to the collection
                            collectedgroupelements[teller4] = groupelements[teller5];
                            teller4++;
                        }
                    }
                    
                    // We should now have an array containing all the group elements with the possibility on that value
                    // Now we collect the blocknumber of these elements
                    
                    int[] blocknumbers = new int[collectedgroupelements.length];
                    teller4 = 0; // we reuse the counter from above
                    for(int teller7 = 0; teller7 <= collectedgroupelements.length-1; teller7++)
                    {
                        Element thiselement = collectedgroupelements[teller7];
                        blocknumbers[teller4] = thiselement.giveBlock();
                        teller4++;
                    }
                    
                    // Now we check whether or not these blocknumbers are the same
                    
                    int blocknumber = blocknumbers[0];
                    boolean blockOK = true;
                    teller4 = 1; // we reuse the counter from above
                    while((blockOK) && (teller4 <= blocknumbers.length-1))
                    {
                        if(blocknumbers[teller4] != blocknumber)
                        {
                            blockOK = false;
                        }
                        teller4++;
                    }
                    
                    // If this is the case, we can safely exclude this candidate (= teller3) from the OTHER cells in the box
                    // (so the elements where scrapping occurs) SHOULD be in the same box, but NOT in the same group
                    
                    if(blockOK)
                    {
                        if((debugmode) && (uberteller == 0))
                        {
                            log = log + "\n" +"Scrapped value " + teller3 + " from cells in block " + blocknumber 
                            + " that don't belong to row " + groupteller +". (Locked Candidates 2)";
                        }
                        if((debugmode) && (uberteller == 1))
                        {
                            log = log + "\n" +"Scrapped value " + teller3 + " from cells in block " + blocknumber 
                            + " that don't belong to column " + groupteller +". (Locked Candidates 2)";
                        }
                        statistics_lockedcandidates2++;
                        Element[] inblock = collectGroupElements(0,blocknumber); // we're sure to only have the elements from the block
                        for(int A=1; A <= 9; A++)
                        {
                             Element thiselem = inblock[A];
                             if(uberteller==0)
                             {
                                 // if thiselem is not in the same row, still has the possibility to be scrapped and hasn't been found yet
                                 if((thiselem.giveRow() != groupteller) && (thiselem.isPossibility(teller3)) && (!thiselem.giveFound()))
                                        {
                                            thiselem.scrapnumber(teller3);
                                            ietsgedaan = true;
                                        }
                             }
                             if(uberteller==1)
                             {
                                 // if thiselem is not in the same column, still has the possibility to be scrapped and hasn't been found yet
                                 if((thiselem.giveColumn() != groupteller) && (thiselem.isPossibility(teller3)) && (!thiselem.giveFound()))
                                        {
                                            thiselem.scrapnumber(teller3);
                                            ietsgedaan = true;
                                        }
                             }
                         }
                    }// closing if(blockOK)
                } // closing if statement that possibilities > 1    
            } // closing Locked Candidates 2 for Columns AND Rows
        }// End of Loop for Hidden Candidates/Locked Candidates 2 for Columns AND Rows
    }//Uberteller end
        
        /**
         * ADVANCED TECHNIQUE: NAKED PAIRS (for blocks,columns and rows) 16/07 0:06
         * 
         * If two cells in a group contain an identical pair of candidates and 
         * only those two candidates, then no other cells in that group 
         * could be those values.
         * 
         */
        for(int uberteller = 0; uberteller<=2;uberteller++)
        {
            for(int groupcounter = 1; groupcounter < 10; groupcounter++) // Iterating over all group numbers, baby
            {
            Element[] groupelements = null;
            if(uberteller == 0){groupelements = collectGroupElements(0,groupcounter);} // we're in block mode
            if(uberteller == 1){groupelements = collectGroupElements(1, groupcounter);} // we're in row mode
            if(uberteller == 2){groupelements = collectGroupElements(2, groupcounter);} // we're in column mode
            // Now it's time to start comparing the possibility arrays by using a double loop
            for(int counter1=1; counter1 <= 9; counter1++)
            {
                for(int counter2=1; counter2 <= 9; counter2++)
                {
                    // explanation of if tests
                    // the arrays should be the same, should contain only 2 possibilities, should both come from elements which haven't been found yet
                    // and should (of course) not come from the same element.
                    boolean condition_one = (compareArrays(groupelements[counter1].givePossibilities(),groupelements[counter2].givePossibilities()));
                    boolean condition_two = (groupelements[counter1].numberofPossibilities() == 2);
                    boolean condition_three = (!groupelements[counter1].giveFound());
                    boolean condition_four = (!groupelements[counter2].giveFound());
                    boolean condition_five = (groupelements[counter1] != groupelements[counter2]);
                    boolean arewealone = true;
                    
                    // This check might be useless: is it ever possible that more than 3 possibility arrays contain only 2 candidates ? 
                    // I don't think so, but I'm not sure, that's why this check is in
                    for(int counter9=1; counter9 <= 9;counter9++)
                        {
                            if((condition_one) && (condition_two) && (condition_three) && (condition_four) && (condition_five))
                            {
                                int[] posarray1 = groupelements[counter1].givePossibilities();
                                if( (compareArrays(posarray1,groupelements[counter9].givePossibilities())) && (groupelements[counter9] != groupelements[counter1])
                                && (groupelements[counter9] != groupelements[counter2]) && (!groupelements[counter9].giveFound()))
                                {
                                    arewealone = false;
                                }
                            }
                        }
                    // end of useless test, probably
                    
                    if( (condition_one) && (condition_two) && (condition_three) && (condition_four) && (condition_five) && (arewealone))
                    {
                        // Just a thought: am I allowed to change the groupelements properties whilst I'm still looping over them ?
                        
                        // We assign the array to a local variable to improve code readability and avoid ridicously long commands
                        int[] possibilityarray1 = groupelements[counter1].givePossibilities();
                        int[] commonvalues = new int[groupelements[counter1].numberofPossibilities()];
                        int counter4 = 0;
                        // First we collect the common values by looping over one of the two arrays
                        for(int counter3 = 1; counter3 <= 9; counter3++)
                        {
                            if (possibilityarray1[counter3] == 1)
                            {
                                commonvalues[counter4] = counter3;
                                counter4++;
                            }
                        }
                        // Now we have an int[] array containing all the values from the Naked Pairs
                        // We can scrap these values from the remaining elements in the group, but not from
                        // the naked pair itself. In other words: time for action.
                        boolean ietsnugedaan = false; // helper boolean for debug text
                        for(int counter5 = 1; counter5<= 9; counter5++) // we loop over all the block elements
                        {
                            boolean condition1 = (groupelements[counter5] != groupelements[counter1]);
                            boolean condition2 = (groupelements[counter5] != groupelements[counter2]);
                            // the first two checks make sure the element is not one of the naked pair
                            boolean condition3 = (!groupelements[counter5].giveFound());
                            // the third check verifies that we're not changing anything on a found element (which would be useless)
                            boolean condition4 = (groupelements[counter5].containsPossibilities(commonvalues));
                            // the fourth check verifies that the common values are still present in the element (otherwise we're
                            // scrapping zeros and the 'ietsgedaan' flag toggles to true -> infinite loop)
                            if( (condition1) && (condition2)
                                && (condition3) && (condition4))
                            {
                                groupelements[counter5].scrapnumbers(commonvalues);
                                ietsgedaan = true;
                                ietsnugedaan = true;
                                statistics_nakedpairs++;
                            }
                        }
                        if((debugmode) && (ietsnugedaan))
                        {
                            // collect the scrapped values in a string
                            String values = "";
                            for(int littlecounter=0; littlecounter <= commonvalues.length-1;littlecounter++)
                            {
                                values = values + commonvalues[littlecounter] +", ";
                            }
                            String output = "Scrapped values " + values + " in all elements in ";
                            if(uberteller==0){output = output+"block ";}
                            if(uberteller==1){output = output+"row ";}
                            if(uberteller==2){output = output+"column ";}
                            output = output + groupcounter + ", except in the elements located at (" +
                            groupelements[counter1].giveRow() +","+groupelements[counter1].giveColumn() +
                            ") and (" + groupelements[counter2].giveRow() +","+groupelements[counter2].giveColumn() +
                            "). (Naked Pairs)";
                            log = log + "\n" +output;
                        }
                    }// End of If-check on compare arrays
                }
            }
        } // End of Naked Pairs inner loop
    }// End of Naked Pairs uber-loop
    
    /**
     * EXPERT TECHNIQUE: Naked Triples (for Groups, Rows, Columns) (18/07 22:01)
     * 
     * A Naked Triple is slightly more complicated because it does not always imply three numbers each in three cells.
     * Any group of three cells in the same unit that contain IN TOTAL three candidates is a Naked Triple. Each cell 
     * can have two or three numbers, as long as in combination all three cells have only three numbers. 
     * When this happens, the three candidates can be removed from all other cells in the same unit. 
     */
    for(int uberteller = 0; uberteller<=2;uberteller++) // yet again the uberteller to apply the technique for blocks, rows and columns
        {
            for(int groupcounter = 1; groupcounter < 10; groupcounter++) // Iterating over all group numbers, baby
            {
            Element[] groupelements = null;
            if(uberteller == 0) // we're in block mode
            {groupelements = collectGroupElements(0,groupcounter);}
            if(uberteller == 1) // we're in row mode
            {groupelements = collectGroupElements(1, groupcounter);}
            if(uberteller ==2) // we're in column mode
            {groupelements = collectGroupElements(2, groupcounter);
            }
            // now we collect the elements containing three or less possibilities
            ArrayList collectedelements2 = new ArrayList();
            for(int A=1; A<=9; A++)
            {
                Element thiselement = groupelements[A];
                if((!thiselement.giveFound()) && (thiselement.numberofPossibilities() <= 3))
                {collectedelements2.add(thiselement);}
            }
            
            if(!(collectedelements2.size() < 3)) // if we don't even have three elements, we can give up the search now
            {
                // we stored them in an ArrayList because we don't know how many there would be
                Element[] collectedelements = new Element[collectedelements2.size()];
                // we'd rather have them in an array, though
                collectedelements2.toArray(collectedelements);
                // now we collect all the possibility arrays, and find the possibilities that can occur
                int[] posarray = new int[10];
                for(int A=0; A<=collectedelements.length-1; A++) // we iterate over all elements
                {
                    Element gotelement = collectedelements[A];
                    int[] currentposarray = gotelement.givePossibilities();
                    for(int B=0; B<=currentposarray.length-1; B++) // we iterate over all their possibilities
                    {
                        if(currentposarray[B] == 1) // if there is a possibility
                            {posarray[B] = 1;}
                    }
                }
                // now we have to transform this array into an array containing *only* the possibility values
                int howmany = 0;
                // how big should this array be ?
                for(int A=0; A<=9; A++){if(posarray[A] == 1){howmany++;}}
                if(!(howmany < 3)) // we can stop this when we don't even have 3 possibilities
                {
                    int[] possiblevalues = new int[howmany];int innerteller = 0;
                    for(int A=0; A <= 9; A++)
                    {
                        if(posarray[A] == 1)
                        {possiblevalues[innerteller] = A;innerteller++;}
                    }
                    // we have an array with the possibilities which could form a possible naked triple
                    // now we're going to generate a matrix in which the rows represent a three-digit combination
                    int[][] numbercombinations = generate3Combinations(possiblevalues);
                    // now we need all 3-element combinations we can form with our elements (located in the collectedelements array)
                    // We will use the same helper method as for the normal values, but now we'll let it generate index numbers
                    // how many elements do we have, and let's fill an array containing numbers
                    int[] indexnumbers = new int[collectedelements.length];
                    for(int teller = 0; teller<=indexnumbers.length-1;teller++)
                    {indexnumbers[teller] = teller;}
                    // now we generate the matrix containing all the combinations of index numbers
                    int[][] indexcombinations = generate3Combinations(indexnumbers);
                    // now it's (finally) time to start sorting out which combinations contain all the values and start scrapping
                    for (int indexcombnumber = 0; indexcombnumber <= howmanyCombinations(collectedelements.length,3)-1; indexcombnumber++)
                    {
                        for(int valuecombnumber = 0; valuecombnumber <= howmanyCombinations(possiblevalues.length,3)-1; valuecombnumber++)
                        {
                            int[] foundarray = {0,0,0};// the three elements must contain the values at least once
                            boolean valid = true;
                            for (int elementcounter = 0; elementcounter <= 2; elementcounter++)
                            {
                                if(valid) 
                                // if one of the elements renders the combination invalid (by containing any 
                                // other number than those allowed, for example), we can stop this iteration :-)
                                {
                                        Element currentelement = collectedelements[indexcombinations[indexcombnumber][elementcounter]];
                                        // first we check whether or not this element contains one of the numbers
                                        boolean somethingfoundhere = false; // if this flag stays false, the combination is invalid
                                        if(currentelement.isPossibility(numbercombinations[valuecombnumber][0]))
                                        {foundarray[0] = 1; somethingfoundhere = true;}
                                        if(currentelement.isPossibility(numbercombinations[valuecombnumber][1]))
                                        {foundarray[1] = 1; somethingfoundhere = true;}
                                        if(currentelement.isPossibility(numbercombinations[valuecombnumber][2]))
                                        {foundarray[2] = 1; somethingfoundhere = true;}
                                        valid = somethingfoundhere; // if nothing is found, the combination is invalid
                                        // now we check whether or not this element contains any other numbers than those allowed
                                        int[] temparray = {numbercombinations[valuecombnumber][0],
                                            numbercombinations[valuecombnumber][1],numbercombinations[valuecombnumber][2]};
                                        if(currentelement.containsotherPossibilities(temparray))
                                            {valid = false;}
                                 }// end of validation checker
                             }// end of check-loop
                             // after this loop, we'll do something if the sum = 3 and it's still all valid
                             if( (foundarray[0] + foundarray[1] + foundarray[2] == 3) && (valid))
                             {
                                 // we collect the information we need
                                 Element element1 = collectedelements[indexcombinations[indexcombnumber][0]];
                                 Element element2 = collectedelements[indexcombinations[indexcombnumber][1]];
                                 Element element3 = collectedelements[indexcombinations[indexcombnumber][2]];
                                 int[] temparray = {numbercombinations[valuecombnumber][0],
                                 numbercombinations[valuecombnumber][1],numbercombinations[valuecombnumber][2]};
                                 // this is a helper boolean for debug info
                                 boolean ietsnugedaan = false;
                                 // we loop over all group elements
                                 for (int finalgroupcounter=1;finalgroupcounter <=9;finalgroupcounter++)
                                 {
                                     Element currentgroupelement = groupelements[finalgroupcounter];
                                     boolean check1 = !currentgroupelement.giveFound(); // no found flag in element
                                     boolean check2 = currentgroupelement != element1; // the element 
                                     boolean check3 = currentgroupelement != element2; //shouldn't be a part
                                     boolean check4 = currentgroupelement != element3; // of the naked triple
                                     boolean check5 = currentgroupelement.containsPossibilities(temparray); 
                                     // and it should hàve something to get scrapped
                                     if((check1) && (check2) && (check3) && (check4) && (check5)) // finally, at last, belissimo, time for action.
                                     {
                                         currentgroupelement.scrapnumbers(temparray);
                                         statistics_nakedtriples++;
                                         ietsgedaan = true;
                                         ietsnugedaan = true;
                                     }// closing scrapping stuff
                                 }// closing looping over group elements
                                 
                                 if((debugmode) && (ietsnugedaan))
                                 {
                                     String output = "Scrapped values " + temparray[0] + ", " + temparray[1] +" and " + temparray[2] + " in ";
                                     if(uberteller==0){output = output+"block ";}
                                     if(uberteller==1){output = output+"row ";}
                                     if(uberteller==2){output = output+"column ";}
                                     output = output + groupcounter + " except in the elements ";
                                     if(uberteller==1){ output = output + element1.giveColumn() + "," + element2.giveColumn() + " and " +
                                     element3.giveColumn();}
                                     if(uberteller==2){ output = output + element1.giveRow() + "," + element2.giveRow() + " and " +
                                     element3.giveRow();}
                                     if(uberteller==0){ output = output + "located at (" + element1.giveRow() +","+element1.giveColumn()+"),("+
                                     element2.giveRow()+","+element2.giveColumn()+") and ("+element3.giveRow()+","+element3.giveColumn()+
                                     ")";}
                                     output = output + ". (Naked Triples)";
                                     log = log + "\n" +output;
                                  }//closing debugmode info
                              }//closing piece that adds the spice to the scrapping action
                            }//end of looping over combinations (values)
                        }//end of looping over combinations (indexes);
                }// end of If-loop (abort if possibilities < 3)
              }// end of If-loop (abort if elements < 3)
            }// end of groupcounter
        }// end of groupselector (uberteller) , thus end of naked triples
        
        /**
         * EXPERT TECHNIQUE: Hidden pairs
         * If two cells in a group contain a pair of candidates (hidden amongst other candidates) that are not found 
         * in any other cells in that group, then other candidates in those two cells can be excluded safely.
         */
        for(int uberteller = 0; uberteller<=2;uberteller++) // yet again the uberteller to apply the technique for blocks, rows and columns
        {
            for(int groupcounter = 1; groupcounter < 10; groupcounter++) // Iterating over all group numbers, baby
            {
            Element[] groupelements = null;
            if(uberteller == 0) // we're in block mode
            {groupelements = collectGroupElements(0,groupcounter);}
            if(uberteller == 1) // we're in row mode
            {groupelements = collectGroupElements(1, groupcounter);}
            if(uberteller ==2) // we're in column mode
            {groupelements = collectGroupElements(2, groupcounter);
            }// okay, we've got the group elements
            int[][] numberposib = generateNumberPosib(groupelements);
            // now we have to collect the possibilities which appear in the possibility matrix of exactly two elements
            // first we calculate how many of these values there will be
            int counter = 0;
            for(int teller3=1; teller3 < 10; teller3++)
            {if(numberposib[0][teller3] == 2){counter++;}}
            if(counter >= 2) // if we don't even have 2 values, this method won't solve anything
            {
                int[] values = new int[counter];// now we'd like to have a nice array containing the numbers, 
                Element[][] corr_elements = new Element[2][counter];//and another matrix (2 x n) containing the 2 elements concerned
                int innerteller = 0; // switches between array columns
                int helpcounter1 = 0; // switches between matrix columns
                for(int teller3=1; teller3 < 10; teller3++) // we loop over the group elements
                {if(numberposib[0][teller3] == 2)
                    {values[innerteller] = teller3; // add the value to the numberarray
                      int helpcounter2 = 0; // switches between matrix rows
                      for(int teller4=1; teller4 < 10; teller4++)
                      { if((numberposib[teller4][teller3] == 1) && (!groupelements[teller4].giveFound()))
                          { corr_elements[helpcounter2][helpcounter1] = groupelements[teller4];
                            helpcounter2++; }
                      }
                      innerteller++;helpcounter1++;
                    }
                } // after this loop, we should have our array with numbers, and our matrix containing the corresponding elements
                // now we have to form all possible combinations of 2 values, and then check if they both concern the same elements
                int[][] combinations = generate2Combinations(values);
                // due to general stupidity, we now also need some mapping index 
                // to find out where the values are located in the values array, since the generate2Combinations method doesn't work in any
                // particular order, and I'm not planning on letting it leak internal info about the array itself. 
                // Loose lips sink ships, so we better do this.
                int[] indexofvalues = new int[10];
                for(int i = 0; i <= values.length-1; i++)
                { indexofvalues[values[i]] = i;}
                for(int teller3=0; teller3 <= howmanyCombinations(values.length,2)-1; teller3++) // we iterate over all combinations
                {
                    // we fetch everything required to determine whether or not these form Hidden Pairs
                    int number1 = combinations[teller3][0]; int number2 = combinations[teller3][1];
                    int[] scrappers = {number1,number2};
                    Element n1_1 = corr_elements[0][indexofvalues[number1]]; 
                    Element n1_2 = corr_elements[1][indexofvalues[number1]];
                    Element n2_1 = corr_elements[0][indexofvalues[number2]]; 
                    Element n2_2 = corr_elements[1][indexofvalues[number2]];
                    if( ((n1_1 == n2_1) || (n1_1 == n2_2)) &&
                        ((n1_2 == n2_1) || (n1_2 == n2_2)) && (!n1_1.giveFound()) && (!n1_2.giveFound())
                        && ((n1_1.containsotherPossibilities(scrappers)) || (n1_2.containsotherPossibilities(scrappers))))
                        {
                            n1_1.scrapallexcept(scrappers);
                            n1_2.scrapallexcept(scrappers);
                            ietsgedaan = true;
                            statistics_hiddenpairs++;
                            String output = "";
                            output = output + "Scrapped values " + number1 + ", " + number2 + " in ";
                            if(uberteller==0){output = output+"block ";}
                            if(uberteller==1){output = output+"row ";}
                            if(uberteller==2){output = output+"column ";}
                            output = output + groupcounter + ", except in the elements located at ";
                            if(uberteller==1){ output = output + n1_1.giveColumn() + " and " + n1_2.giveColumn();}
                            if(uberteller==2){ output = output + n1_1.giveRow() + " and " + n1_2.giveRow();}
                            if(uberteller==0){ output = output + "(" + n1_1.giveRow() +","+n1_1.giveColumn()+") and ("+
                            n1_2.giveRow()+","+n1_2.giveColumn() + ")";}
                            output = output + ". (Hidden Pairs)";
                            log = log + "\n" + output;
                        }
                 }
            }// end of if test: (#values found has to be >= 2)
            }// end of loop over groups
        }// end of uberteller loop, thus end of Hidden pairs
            
 
        /**
         * The Elements which are found are labeled likewise, so they
         * can be checked in the next run.
         * 
         * !!! THIS IS THE CLEANING UP PROCESS. THIS LOOP SHOULD BE THE LAST, ALWAYS !!!
         * 
         * No other loop should be allowed to set the found flag of elements
         * to true, this loop only can decide whether or not an element is found. Period.
         * Screw with this, and you'll bugger up the algorithm.
         */
        
        for (int rij = 0; rij <= 8; rij++)
        {
            for (int kolom = 0; kolom <= 8; kolom++)
            {
               Element currentelement = Elements[rij][kolom];
               int[] numberarray = currentelement.givePossibilities();
               int sum = 0;
               int indexlocation = 99;
               for(int i=0; i<=numberarray.length-1; i++)
               {
                   sum = sum + numberarray[i];
                   if(numberarray[i] == 1)
                   {
                       indexlocation = i;
                   }  
               }
               if((sum == 1) && (!currentelement.giveFound()))
               {
                   currentelement.setNumber(indexlocation);
                   currentelement.setFound(true);
                   newvaluesfound++;
                   if(debugmode){log = log + "\n" +"Found value " + indexlocation 
                       + " at row " + currentelement.giveRow() +", column " + currentelement.giveColumn() +".";}
               }
            }
        }// closing the label/validation loop
      }// closing the while(ietsgedaan & notallfoundyet) loop -> quite important, this ends the program
    // Log statistics
    long totaltime = System.currentTimeMillis() - starttime;
    statistics = statistics + "\n" + "\n" +  "### Solve statistics" + "\n" + "\n" + "Solving process ended after " + runs + " runs.";
    statistics = statistics + "\n" + "Total processing time was " + totaltime + " milliseconds.";
    statistics = statistics + "\n" + newvaluesfound + " new values were found.";
    statistics = statistics + "\n" + "The Hidden Singles technique was applicated "+ statistics_hiddensingles +" times." + "\n" +
    "The Locked Candidates 1 technique was applicated " + statistics_lockedcandidates1 + " times." + "\n" +
    "The Locked Candidates 2 technique was applicated " + statistics_lockedcandidates2 + " times." + "\n" +
    "The Naked Pairs technique was applicated " + statistics_nakedpairs + " times." + "\n" +
    "The Naked Triples technique was applicated " + statistics_nakedtriples + " times." + "\n" +
    "The Hidden Pairs technique was applicated " + statistics_hiddenpairs + " times." + "\n";
    validator(); // add possible error info to fields
    }// closing the solve method
    
    /**
     * This method prints the sudoku in its current state
     * to the command line.
     */
    
    public String show(int option)
    {
        String sudoku = "#########################" + "\n" + "#"; 
        for (int rij = 0; rij <= 8; rij++)
        {
            for (int kolom = 0; kolom <= 8; kolom++)
            {
                String nummerke = " ";
                if(Elements[rij][kolom].giveNumber() != 0)
                     {nummerke = "" + Elements[rij][kolom].giveNumber();}
                if((kolom ==2) || (kolom == 5) || (kolom == 8))
                     { sudoku = sudoku + " " + nummerke + " #";}
                     else{ sudoku = sudoku + " " + nummerke;}
             }
             sudoku = sudoku + "\n";
             if((rij ==2) || (rij == 5)) {sudoku = sudoku + "#########################" + "\n" + "#";}
             else if(rij !=8){sudoku = sudoku + "#";}
             if(rij==8){sudoku = sudoku + "#########################";}
      }
      return sudoku;
    }
    
    /**
     * This is the quick validation method: it checks whether or not all the elements of the sudoku have the 'Found' flag.
     * It's called 'quick' because it assumes the solver algoritm doesn't make create any errors, neither does it the assume
     * the user to input bogus sudokus (oh how I wish !).
     * 
     * This validation method raises the AllFound flag.
     * 
     * @return True if all elements have the 'Found' flag.
     */
    
    public boolean quickvalidator()
    {
        boolean correct = false;
            boolean found = true;
            int row = 0;int column = 0;
            // Instead of the regular double for-loop, we use a while loop here, to stop the check when some element 
            // doesn't have the 'found' flag. This is supposed to be a quick check, so every bit helps.
            while((found) && (row <= 8))
            {
                Element currentelement = Elements[row][column];
                if(!currentelement.giveFound())
                {found = false;}
                column++;
                if(column > 8)
                {column = 0;row++;}
            }
        correct = found;
        allfound = correct;
        return correct;
    }
    /**
     * This is the validator: it checks the sudoku for errors. (double numbers in row/block/column).
     * If it encounters any errors, it saves the errorinfo into a string which is accessible by using
     * the getErrorInfo().
     * 
     * This validation method raises the ContainsErrors flag.
     * 
     * @return true If the sudoku doesn't contain any errors.
     */
    // this needs optimizing. note to self: OPTIMIZING, not breaking the dozen of loops this thing runs in.
    public boolean validator()
    {
            boolean OK = true;
            errors = "The sudoku you entered seems to be invalid: " + "\n";
            int[][] errorareas = new int[3][10]; // this matrix will tell us where the problem areas are
            for(int uberteller=0; uberteller<=2; uberteller++)
            {
                for(int groupcounter = 1; groupcounter <=9; groupcounter++)
                {
                   //collecting group elements
                   Element[] groupelements = null;
                   if(uberteller == 0){groupelements = collectGroupElements(0,groupcounter);}// we're in block mode
                   if(uberteller == 1){groupelements = collectGroupElements(1, groupcounter);}// we're in row mode
                   if(uberteller == 2){groupelements = collectGroupElements(2, groupcounter);}// we're in column mode
                   int[] numbersfound = new int[10]; numbersfound[0]=1;
                   for(int elementselector = 1; elementselector <=9; elementselector++)
                   {
                         Element currentelement = groupelements[elementselector];
                         if(currentelement.giveFound())
                            {
                                for(int numberselector = 1; numberselector<=9;numberselector++)
                                    { 
                                        if((currentelement.giveNumber() == numberselector) && (numbersfound[numberselector] == 1))
                                        {// we found a double !
                                            // generating the error message & adding error areas.
                                            String errormessage = "Found double number (" + numberselector+ ") in ";
                                            if(uberteller==0){errormessage = errormessage+"block ";
                                            errorareas[0][groupcounter] = 1;}
                                            if(uberteller==1){errormessage = errormessage+"row ";
                                            errorareas[1][groupcounter] = 1;}
                                            if(uberteller==2){errormessage = errormessage+"column ";
                                            errorareas[2][groupcounter] = 1;}
                                            errormessage = errormessage + groupcounter + ".";
                                            errors = errors + errormessage + "\n";
                                            // raising the flag
                                            OK = false;
                                        }
                                        if(currentelement.giveNumber() == numberselector)
                                          {numbersfound[numberselector]=1;}
                                     }
                            }
                     }
                 }
            }
        containserrors = !OK;
        
        if (OK) // if after a second or third check with this method, 
                //the sudoku seems to be valid, the errorinfo can be removed
        {errorcells = null; errors = null;}
        
        if (!OK) // if errors have been found, the problem groups have to be listed (so they can be marked by the GUI)
        {
            // first we determine how many error groups we have
            int numberoferrorgroups = 0;
            for(int i = 0; i<=2; i++){
                for(int i2 = 1; i2<=9; i2++){
                    if(errorareas[i][i2] == 1)
                    {numberoferrorgroups++;}}}
           // now we create a matrix which can contain all the error coordinates
           errorcells = new int[9*numberoferrorgroups][2];
           // now we fill this matrix
           int innercounter = 0;
           for(int i = 0; i<=2; i++){
                for(int i2 = 1; i2<=9; i2++){
                     if(errorareas[i][i2] == 1)
                     {
                         Element[] groupelements = collectGroupElements(i,i2);
                         for(int i3 = 1; i3 <=9 ; i3++)
                         { Element currentelement = groupelements[i3];
                           errorcells[innercounter][0] = currentelement.giveRow();
                           errorcells[innercounter][1] = currentelement.giveColumn();
                           innercounter++;
                        }
                    }
                }
            }              
        }
        return OK;   
    }// end of validator
     
    /**
     * This method generates the matrix representation of the sudoku
     * 
     * @return int[][] The matrix containing the sudoku values
     * 
     */
    
    public int[][] getSudokuMatrix()
    {
        int[][] sudokumatrix = new int [10][10];
        {for(int row = 0;row <=8; row++){
        for(int column = 0;column <=8;column++)
        {sudokumatrix[row+1][column+1] = Elements[row][column].giveNumber();}}}
        return sudokumatrix;
    }
    
    /**
     * HELPER METHOD
     * Private method to find out in which block
     * the element at the given position is (1->9)
     * Since I haven't found a way to do this the easy
     * way, it's still hard-coded.
     */
    private int whichBlock(int rij, int kolom)
    {
        int number = 0;
        if((rij >= 0) && (rij<=2) && (kolom>=0) && (kolom<=2)){number = 1;}
        if((rij >= 0) && (rij<=2) && (kolom>=3) && (kolom<=5)){number = 2;}
        if((rij >= 0) && (rij<=2) && (kolom>=6) && (kolom<=8)){number = 3;}
        if((rij >= 3) && (rij<=5) && (kolom>=0) && (kolom<=2)){number = 4;}
        if((rij >= 3) && (rij<=5) && (kolom>=3) && (kolom<=5)){number = 5;}
        if((rij >= 3) && (rij<=5) && (kolom>=6) && (kolom<=8)){number = 6;}
        if((rij >= 6) && (rij<=8) && (kolom>=0) && (kolom<=2)){number = 7;}
        if((rij >= 6) && (rij<=8) && (kolom>=3) && (kolom<=5)){number = 8;}
        if((rij >= 6) && (rij<=8) && (kolom>=6) && (kolom<=8)){number = 9;}
        return number;
    }
    
    /**
     * HELPER METHOD
     * Private methods to generate an array containing the group elements requested
     * option = 0 for block, 1 for row, 2 for column
     */
    
    private Element[] collectGroupElements(int option, int groupnumber)
    {
            int teller = 1;
            Element[] groupelements = new Element[10];
            for (int rij = 0; rij <= 8; rij++)
            {
                for (int kolom = 0; kolom <= 8; kolom++)
                {Element currentelement = Elements[rij][kolom];
                   if(option == 0){if(currentelement.giveBlock() == groupnumber){
                        groupelements[teller] = currentelement;teller++;}}
                   if(option == 1){if(currentelement.giveRow() == groupnumber){
                       groupelements[teller] = currentelement;teller++;}}
                   if(option == 2){if(currentelement.giveColumn() == groupnumber){
                       groupelements[teller] = currentelement;teller++;}}}}
            return groupelements;
     }
     
     /**
      * HELPER METHOD
      * 
      * Private method to calculate numberpossibility-matrix (needed in Hidden Candidates techniques)
      * for a group of 9 elements. This is a matrix representing the possibilities within a group.
      * The first row contains the amount of times the possibility occurs.
      * The other rows contain the position of the possibilities. 
      * (for example: if (4,6) = 1, element 4 contains the possibility 6)
      * 
      */
     
     private int[][] generateNumberPosib(Element[] groupelements)
     {
         int[][] numberposib = new int[10][10];
            for(int whichnumber=1; whichnumber < 10; whichnumber++)
            {
                    for(int teller2=1;teller2 <= 9;teller2++)
                    {
                        Element currentgroupelement = groupelements[teller2];
                        // The isfound check is introduced because the arrays of found elements
                        // possibly contain errors generated by other operations: they should be neglected in the result.
                        if((currentgroupelement.isPossibility(whichnumber)) && (!currentgroupelement.giveFound()))
                        {
                            numberposib[0][whichnumber] = numberposib[0][whichnumber] + 1;
                            numberposib[teller2][whichnumber] = 1;
                        }
                    }
            }
            return numberposib;
      }
      
     /**
      * HELPER METHOD
      * Private method to compare (same length) integer arrays
      */
     
     private boolean compareArrays(int[] array1, int[] array2)
     {
         boolean thesame = true;
         int teller1 = 0;
         while((thesame) && (teller1 <= array1.length-1))
         {if(array1[teller1] != array2[teller1]){thesame = false;}teller1++;}
         return thesame;
     }
     
     /**
      * HELPER METHOD
      * Faculty
      */
     
     private int fac(int number)
     {
         int resultaat = number;
         if(number != 0){
         for(int teller = number; teller >=2; teller--)
         {resultaat = resultaat*(teller-1);}}
         return resultaat;
     }
     
     /**
      * HELPER METHOD
      * Given an array of values and a group number Y, this method calculates how many Y-digit combinations can be formed
      * with the given value array.
      */
     
     private int howmanyCombinations(int howmany, int fromhowmany)
     {
         int numberofcombinations = 0;
         if(howmany == fromhowmany){numberofcombinations = 1;}
         else{numberofcombinations = fac(howmany) / ( fac(howmany - fromhowmany) * fac(fromhowmany) );}
         return numberofcombinations;
     }
     
     /**
      * HELPER METHOD
      * Given an array of values, this generates a matrix containing all the THREE-digit combinations possible.
      * Value order is unimportant, and double combinations (example: [ 2 3 5 ] and [ 3 2 5 ]) are not allowed.
      */
     
     private int[][] generate3Combinations(int[] numbers)
     {
         int numberofcombinations = howmanyCombinations(numbers.length,3);
         // now we create an array with the right size
         int[][] combinations = new int[numberofcombinations][3];
         if(numberofcombinations == 1)
         {combinations[0][0] = numbers[0];
          combinations[0][1] = numbers[1];
          combinations[0][2] = numbers[2];}
         else{
         int selector1 = 0;
         int selector2 = 1;
         int combinationteller = 0;
         // inner loop
         while (selector1 != numbers.length-2) // as long as selector 1 hasn't reached the end (last number -1)
         {
                while(selector2 != numbers.length-1) // as long as selector 2 hasn't reached the end (last number)
                {
                    for (int movingselector = selector2+1; movingselector <= numbers.length-1; movingselector++) 
                    // the moving selector stops when it has reached the last number
                        {combinations[combinationteller][0] = numbers[selector1];
                        combinations[combinationteller][1] = numbers[selector2];
                        combinations[combinationteller][2] = numbers[movingselector];
                        combinationteller++;}
                    selector2++;
                }
         selector1++;
         selector2 = selector1+1;
         }
        }
        return combinations;
    }
    
    /**
      * HELPER METHOD
      * Given an array of values, this generates a matrix containing all the TWO-digit combinations possible.
      * Value order is unimportant, and double combinations (example: [ 2 3 ] and [ 3 2 ]) are not allowed.
      */
     
    private int[][] generate2Combinations(int[] numbers)
    {
        int numberofcombinations = howmanyCombinations(numbers.length,2);
        // now we create an matrix in which each row contains a possible combination
        int[][] combinations = new int[numberofcombinations][2];
        if(numberofcombinations == 2)
         {combinations[0][0] = numbers[0];
          combinations[0][1] = numbers[1];}
        else{
        int selector = 0;
        int combinationteller = 0;
        while(selector != numbers.length-1)
        {
            for(int movingselector = selector+1; movingselector <= numbers.length-1; movingselector++)
                {combinations[combinationteller][0] = numbers[selector];
                combinations[combinationteller][1] = numbers[movingselector];
                combinationteller++;}
            selector++;
        }
        }
        return combinations;
    }
    
   /**
     * Accessor methods
     */
    
    /**
     * This method returns the output log, which contains all actions performed on the sudoku.
     * 
     * @return log The output log
     */
    public String getOutputlog(){return log;}
    /**
     * This method returns the All Found flag, which is true if all the elements of the sudoku have
     * been found. This method should be called when the user is sure the quickvalidator already has
     * been used.
     * 
     * @return boolean AllFoundFlag
     */
    public boolean getAllFoundFlag(){return allfound;}
    /**
     * This method returns the Contains errors flag, which is true if the sudoku contains errors.
     * This method should be called when the user is sure the validator method (which is heavy on performance)
     * is already called, there's no need to call it multiple times, to ensure thread-safety.
     * 
     * @return boolean ContainsErrorsFlag
     */
    public boolean getContainsErrorsFlag(){return containserrors;}
    /**
     * This method returns the Error Info.
     * 
     * @return String ErrorInfo
     */
    public String getErrorInfo(){return errors;}
    /**
     * This method returns the coordinates of the error cells. 
     * 
     * @return int[][] Error Cells
     */
    public int[][] getErrorCells(){return errorcells;}
    public String getStatistics(){return statistics;}
}