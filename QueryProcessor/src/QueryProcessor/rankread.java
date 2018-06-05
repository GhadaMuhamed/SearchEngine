package QueryProcessor;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class rankread extends TimerTask {
    public static List <Double> pagerank=new ArrayList<Double>();

    public rankread( List<Double> pagerank){

        this.pagerank=pagerank;
    }
    @Override
    public void run() {
        System.out.println("ssdsdss");
        read_pagerank();
    }
    public static void read_pagerank() {
        pagerank.clear();
        FileReader fileReader;
        File file = new File("G:\\out.txt");
        try {
            fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String s;
            try {
                s = br.readLine();
                while (s != null) {
                    pagerank.add(Double.parseDouble(s));
                    s = br.readLine();

                }
            } catch (IOException e) {
            }
            try {
                fileReader.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(pagerank.size());
    }
}
