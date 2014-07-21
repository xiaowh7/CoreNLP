import java.io.*;
import java.util.*;

import au.com.bytecode.opencsv.CSVReader;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.process.Tokenizer.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

class ParserDemo {

    public static void main(String[] args) throws IOException {
//        Tokenizer
        LexicalizedParser lp = LexicalizedParser
                .loadModel("edu/stanford/nlp/models/lexparser/englishRNN.ser.gz");

//        String outfile = "dependency_train.txt";
//        String outfile = "dependency_test.txt";
        String outfile = "Sentistrength_dependency_without_neutral.txt";
//        String outfile = "Sentistrength_dependency.txt";
//        String outfile = "Apoorv_dependency_train.txt";
//        String outfile = "IT_dependency.txt";
        BufferedWriter fw = new BufferedWriter(new FileWriter(outfile));
//        String Filename = "final_train-full-B_Input.txt";
//        String Filename = "final_test-gold-B_Input.txt";
//        String Filename = "final_sms-test-gold-B_Input.txt";
//        String Filename = "test_final.txt";
//        String Filename = "final.csv";
        String Filename = "withoutNeutral.csv";
        BufferedReader reader = new BufferedReader(new FileReader(Filename));

        String nextLine;
        int cnt = 0;

        while ((nextLine = reader.readLine()) != null){
            cnt++;
            if (cnt%100==0) System.out.println(cnt);
//            for (int i=0;i<nextLine.length;i++){
//                System.out.println(nextLine[i]);
//            }
            String[] data = nextLine.split("\t");
            String[] sent1;
            List<String> sentList = new ArrayList<String>();
            String text = data[1];
            sent1 = text.split(" ");
            for(int i=0; i<sent1.length; i++){
                if (sent1[i].startsWith("@") || sent1[i].startsWith("http://") || sent1[i].startsWith("https://")) continue;
                sentList.add(sent1[i]);
            }
            String[] sent = sentList.toArray(new String[sentList.size()]);
//            String[] sent = text.split(" ");
//            for (int i=0; i<sent.length; i++){
//                if (i>0) System.out.print(" ");
//                System.out.print(sent[i]);
//            }
//            System.out.println();
//        String[] sent = { "The", "human", "rights", "report", "poses", "a", "substantial", "challenge", "to",
//        "the", "US", "interpretation", "of", "good", "and", "evil", "."};
            List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
            Tree parse = lp.apply(rawWords);
//            parse.pennPrint();
//            System.out.println();

            TreebankLanguagePack tlp = lp.getOp().langpack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);

            //Stanford dependencies in the CoNLL format
//            System.out.println(GrammaticalStructure.dependenciesToString(gs,
//                    gs.typedDependencies(), parse, true, false));

            Collection<TypedDependency> tdl = gs.allTypedDependencies();
//            System.out.println(tdl);
//            System.out.println();
            Object[] ans = tdl.toArray();
            for (int i=0;i<ans.length;i++){
                fw.write(ans[i].toString());
                fw.write("\t");
            }
            fw.write("\n");
//            System.out.println(parse.taggedYield());
//            System.out.println();


        }
        reader.close();
        fw.close();
    }

}