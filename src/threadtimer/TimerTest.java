package threadtimer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: gcavalli
 * Date: Aug 4, 2009
 * Time: 7:05:42 PM
 */
public class TimerTest
{
    private String str = "";
    private TimerTask task = new TimerTask(){
        public void run(){
            if( str.equals("") ){
                System.out.println( "you input nothing. exit..." );
                System.exit( 0 ); // HMMM... How to stop a thread that is not responding???
            }
        }
    };

    public void getInput()
            throws Exception
    {
        Timer timer = new Timer();
        timer.schedule( task, TIMEOUT * 1000 );

        System.out.println( "Input a string within '" + TIMEOUT + "' seconds: " );
        BufferedReader in = new BufferedReader(
                new InputStreamReader( System.in ) );
        str = in.readLine();

        timer.cancel();
        System.out.println( "you have entered: "+ str );
    }

    public static void main( String[] args )
    {
        try{
            (new TimerTest()).getInput();
        }
        catch( Exception e ){
            System.out.println( e );
        }
        System.out.println( "main exit..." );
    }

    private static final int TIMEOUT = 3;
}
