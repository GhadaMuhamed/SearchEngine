import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class Master extends Thread{
    indexer indexerObj;
    Master(indexer ind){
        indexerObj = ind;
    }
    public void run(){
        try {
            indexerObj.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
