

/**
 * The test class Tester.
 *
 * @author  (your name)
 * @version (a version number or a date)
 */
public class Tester extends junit.framework.TestCase
{
    /**
     * Default constructor for test class Tester
     */
    public Tester()
    {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    protected void setUp()
    {      
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown()
    {
    }
    public void testEasysudoku()
    {
        int[] testsudoku = {9,0,0,7,0,0,0,0,0,
                            0,5,0,0,0,0,4,0,0,
                            7,4,0,0,5,8,0,0,0,
                            0,0,3,0,0,0,5,1,0,
                            5,0,0,8,0,4,0,0,3,
                            1,9,6,0,0,0,0,0,0,
                            0,0,0,0,0,0,3,0,0,
                            0,1,0,5,0,0,9,0,8,
                            0,7,9,0,3,6,1,0,0};
        Sudoku easysuduku = new Sudoku(testsudoku);
        easysuduku.solve();
//         if(easysuduku.validator(1))
//         {
//             System.out.println("Validated");
//         }
        
    }
    public void testAveragesudoku()
    {
        int[] testsudoku = {2,0,8,0,0,1,0,0,6,
                            0,0,0,0,3,0,0,9,0,
                            0,0,0,0,0,0,1,0,8,
                            1,3,0,0,4,8,0,0,0,
                            0,2,7,5,0,0,0,4,0,
                            0,0,0,1,0,0,0,0,0,
                            0,0,0,0,0,0,0,5,0,
                            3,0,6,0,0,7,0,0,0,
                            0,7,0,4,0,0,0,2,0};
        Sudoku easysuduku = new Sudoku(testsudoku);
        easysuduku.solve();
    }
     public void testInvalidsudoku()
    {
        int[] testsudoku = {2,0,8,5,0,1,0,0,6,
                            0,0,0,0,3,0,0,9,0,
                            0,0,0,0,0,0,1,0,8,
                            1,3,0,0,4,8,0,0,0,
                            0,2,7,5,0,0,0,4,0,
                            0,0,0,1,0,0,0,0,0,
                            0,0,0,0,0,0,0,5,0,
                            3,0,6,0,0,7,0,0,0,
                            0,7,0,4,0,0,0,2,0};
        Sudoku easysuduku = new Sudoku(testsudoku);
        easysuduku.solve();
        
    }
     public void testDifficultsudoku()
    {
        int[] testsudoku = {0,0,0,0,4,0,0,3,0,
                            9,8,0,6,0,1,0,0,0,
                            0,0,0,0,0,0,2,0,0,
                            0,0,0,0,0,0,0,0,1,
                            0,0,4,0,5,0,7,0,0,
                            6,0,0,0,0,0,0,0,0,
                            0,0,5,0,0,0,0,0,0,
                            0,0,0,9,0,8,0,7,6,
                            0,0,0,0,3,0,0,0,0};
        Sudoku easysuduku = new Sudoku(testsudoku);
        easysuduku.solve();
        
    }
   
    public void testDifficultSudoku2()
    {
        int[] testsudoku = {0,1,0,0,4,0,2,9,0,
                            0,0,5,0,9,0,4,0,0,
                            0,0,8,0,0,0,0,0,0,
                            7,0,0,0,0,0,0,0,0,
                            0,5,0,0,0,0,0,4,9,
                            0,4,0,0,6,0,8,0,0,
                            5,0,0,0,8,0,1,0,3,
                            1,8,0,0,5,2,0,0,0,
                            0,7,0,3,0,0,0,0,0};
        Sudoku easysuduku = new Sudoku(testsudoku);
        easysuduku.solve();
        
    }
    public void testDifficultSudoku3()
    {
        int[] testsudoku = {0,1,0,0,9,0,0,0,4,
                            0,0,5,0,3,0,0,0,0,
                            0,3,0,2,0,0,7,0,8,
                            3,0,0,4,0,0,8,0,0,
                            0,0,7,0,0,0,1,0,0,
                            0,0,8,0,0,3,0,0,5,
                            7,0,3,0,0,2,0,9,0,
                            0,0,0,0,7,0,4,0,0,
                            9,0,0,0,1,0,0,7,0};
        Sudoku easysuduku = new Sudoku(testsudoku);
        easysuduku.solve();
        
    }
    public void testFillsudoku()
    {
        int[] testsudoku = {1,6,3,4,5,0,0,8,9,1,0,3,2,0,6,7,0,9,2,0,1,7,0,3,7,8,9,1,0,3,4,0,6,7,8,9,1,0,3,4,5,3,0,8,9,0,2,3,4,0,6,7,8,9,0,2,3,4,5,0,0,0,9,1,2,3,4,5,2,7,8,3,1,2,3,5,5,6,7,8,9};
        new Sudoku(testsudoku);
    }
    public void testElementscrapping()
    {
        Element testelement = new Element(0,3,0,8);
        int[] lijstje2 = {0,2,7,4};
        testelement.scrapallexcept(lijstje2);
        int[] lijstje = testelement.givePossibilities();
        assertEquals(1, lijstje[2]);
        assertEquals(1, lijstje[4]);
        assertEquals(1, lijstje[7]);
        assertEquals(0, lijstje[0]);
    }
}


