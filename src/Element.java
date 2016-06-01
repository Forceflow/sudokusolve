/**
 * This is the class representing one element of the Sudoku.
 * By eliminating the several possibilities for an Element, the
 * correct value can be found.
 * @author Forceflow
 * @version 21/07/2006
 */
public class Element
{
    // The found and scrapped booleans are included to avoid double work in the solving process
    private boolean found;
    private boolean scrapped;
    
    private int[] possibilities;
    private int number;
    
    // The block row and column variables are the basic properties of the element, its place on the grid
    private int block;
    private int row;
    private int column;
    
    /**
     * Constructor for class Element. This initialises the Element, and immediately marks the element as found
     * if the given number is different from zero.
     * 
     * @param number The number of this element (if it is already known). If this element is unknown, this should be 0.
     * @param block The block this element belongs to
     * @param row The row this element belongs to
     * @param column The column this element belongs to
     */
    
    public Element(int number, int block, int row, int column)
    {
        
        /**
         * Every element has an array of possibilities
         * which are scraped out during the solving, until
         * only one remains.
         */
         
        possibilities = new int[10];
        this.block = block;
        this.row = row;
        this.column = column;
        
        /**
         * For comfort's sake, we take a size 10 array, so
         * the indexes go from 0 to 9. We fill the array with 1's
         * on every other position than 0. (because zeros can never
         * occur in sudoku's, and all other possibilities are plausible
         * in the beginning)
         */
         
        for (int i=1; i <= 9; i++)
        {
            possibilities[i] = 1;
        }
        
        //When the user supplies any other number than 0 as an argument
        //this element is already known:
         
        if(number!=0)
        {
            found = true;
            scrapallexceptone(number);
            this.number = number;
        }
    } // End of constructor
    
    /**
     * These are helper methods for scrapping indices
     */
      
    /**
     * Scrap a number from the possibility list.
     * @param thisindex The number to scrap.
     */
    public void scrapnumber(int thisindex)
    {possibilities[thisindex] = 0;}
    
    /**
     * Scrap multiple numbers from the possibility list.
     * @param indices The numbers to scrap.
     */
    public void scrapnumbers(int[] indices)
    {
        for (int i=0; i <= indices.length-1; i++)
        {
            if(indices[i] != 0){possibilities[indices[i]] = 0;}
        }
    }
    
    /**
     * Scrap all numbers except for one.
     * @param thisindex The number to keep.
     */
    public void scrapallexceptone(int thisindex)
    {
        for (int i=1; i <= 9; i++)
        {possibilities[i] = 0;}
        possibilities[thisindex] = 1;
    }
    /**
     * Scrap all numbers except for these.
     * @param indices The list of numbers to keep.
     */
    public void scrapallexcept(int[] indices)
    {
        for (int i=1; i <= 9; i++)
        {possibilities[i] = 0;}
        for (int i=0; i <= indices.length-1; i++)
        {if(indices[i] != 0){possibilities[indices[i]] = 1;}}
    }
    
    /**
     * Accessor method for checking possibilities
     * @param number The number to be checked for a possibility
     * @return True if the number is still a possibility
     */
    
    public boolean isPossibility(int number)
    {
        boolean yesorno = false;
        if(possibilities[number] == 1){yesorno = true;}
        return yesorno;
    }

    /**
     * Accessor method to check whether or not the 
     * element contains any (!!!) of the given possibilities.
     * @param numbers Array containing the numbers to be checked
     * @return True if any of these numbers is found
     */
    
    public boolean containsPossibilities(int[] numbers)
    {
        boolean yesorno = false;
        for(int teller = 0 ; teller <= numbers.length-1 ; teller++)
        {if(isPossibility(numbers[teller])){yesorno = true;}}
        return yesorno;
    }
    /** 
     *  Acessor method to check whether or not the element contains
     *  any other elements than those provided 
     *  (it is not checked whether or not the elements provided are in the possibility array)
     *  @param numbers Array containing the numbers to be checked
     *  @return True if any number other than those provided is found
     */
    public boolean containsotherPossibilities(int[] numbers)
    {
        boolean othernumberfound = false;
        for (int i = 1; i <= 9; i++)
        {
            if(!othernumberfound)
            {
                
                if(possibilities[i] == 1)
                {
                    boolean foundinlist = false;
                    for(int i2 = 0;i2 <= numbers.length-1; i2++)
                    {
                        if(i == numbers[i2])
                        {foundinlist = true;}
                    }
                    othernumberfound = !foundinlist;    
                }
                
            }
        }
        return othernumberfound;
     }
                
    
    /**
     * Accessor method for checking the number of possibilities still open
     * @return The number of possibilities still open
     */
    
    public int numberofPossibilities()
    {
        int totaal = 0;
        for(int teller=1;teller<=9; teller++)
        {if(possibilities[teller] == 1){totaal++;}}
        return totaal;
    }
    
    /**
     * Returns the possibility array
     * @return The possibility array
     */
    public int[] givePossibilities(){return possibilities;}
    /**
     * Returns the Element's number (0 if it hasn't been found yet)
     * @return The Element's number
     */
    public int giveNumber(){return number;}
    /**
     * Returns the Found flag: this determines whether or not the element's number has been found.
     * @return The Found flag
     */
    public boolean giveFound(){return found;}
    /**
     * Returns the Scrapped flag: this determines whether or not the scrapping technique was already used on this element.
     * @return The Scrapped flag
     */
    public boolean giveScrapped(){return scrapped;}
    /**
     * Returns number of the block this element belongs to
     * @return The block number
     */
    public int giveBlock(){return block;}
    /**
     * Returns number of the row this element belongs to
     * @return The row number
     */
    public int giveRow(){return row;}
    /**
     * Returns number of the Column this element belongs to
     * @return The column number
     */
    public int giveColumn(){return column;}
    /**
     * Sets the Found flag to the given state
     * @param yesorno The boolean to set
     */
    public void setFound(boolean yesorno){found = yesorno;}
    /**
     * Sets the Scrapped flag to the given state
     * @param yesorno The boolean to set
     */
    public void setScrapped(boolean yesorno){scrapped = yesorno;}
    /**
     * Sets the Element's number to the given number
     * @param yesorno The number to set it to
     */
    public void setNumber(int number){this.number = number;}
}