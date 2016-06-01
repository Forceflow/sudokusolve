import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.File;

public class Saver
{
	/**
	 * Write text to a file.
	 * 
     * @param text Text to write.
	 * @param fileName Name of file to write to.
	 * @throws IOException on error.
	 */
   static public void writeText(String text, String fileName){
   try{
       BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), "US-ASCII"));
   out.write(text);
   out.flush();
   out.close();}
   catch(Exception e)
   {
    }
}
}
