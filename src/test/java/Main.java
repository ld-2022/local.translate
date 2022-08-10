import com.ld.analyzer.CoderAnalyzer;
import com.ld.utils.TokenStreamUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Main {

    private static List<String> humpParticiple(String str) {

        List<String> result = new ArrayList<>();
        List<String> words = new ArrayList<>();
        if (str.contains(" ")){
            words.addAll(List.of(str.split(" ")));
        }else if (str.contains("_")){
            words.addAll(List.of(str.split("_")));
        }
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            int length = word.length();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                String s = String.valueOf(word.charAt(j));
                if (j == 0) {
                    sb.append(s);
                    continue;
                }
                if (s.matches("[A-Z]") && !String.valueOf(word.charAt(j-1)).matches("[A-Z]")) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            result.addAll(List.of(sb.toString().toLowerCase(Locale.ROOT).split(" ")));
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(TokenStreamUtils.toList(new CoderAnalyzer().tokenStream("xxx","String")));
//        BufferedReader reader = Files.newBufferedReader(Path.of("/Users/zxw/Desktop/work/rookie/src/main/resources/dic/stardict.csv"));
//        CSVParser csvParser = CSVFormat.DEFAULT.parse(reader);
//        int j = 0;
//        for (CSVRecord record : csvParser) {
//            j++;
//            System.out.println(record.get(0)+" -- "+record.get(3));
//            if (j > 10){
//                break;
//            }
//        }
//        csvParser.close();
//        reader.close();
    }
}
