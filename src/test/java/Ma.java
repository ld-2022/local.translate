import com.ld.analyzer.CoderAnalyzer;
import com.ld.utils.TokenStreamUtils;

import java.io.IOException;

public class Ma {
    public static void main(String[] args) throws IOException {
        String str = "1234(123)";
        System.out.println(str.substring(0,str.indexOf("(")));
        TokenStreamUtils.toList(new CoderAnalyzer().tokenStream("","imgo.Save()")).forEach(System.out::println);
    }
}
