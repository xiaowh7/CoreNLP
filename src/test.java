import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
class test {
    public static void main(String[] args) throws IOException {
//        String Filename = "test.csv";
//        CSVReader reader = new CSVReader(new FileReader(Filename),'\t');
//        String [] nextLine;
//        while ((nextLine = reader.readNext()) != null){
//            String text = nextLine[1];
//            String[] token = text.split(" ");
//            System.out.println(text);
//        }
        LexicalizedParser lp = LexicalizedParser
                .loadModel("edu/stanford/nlp/models/lexparser/englishRNN.ser.gz");

        String[] sent = { "The", "human", "rights", "report", "poses", "a", "substantial", "challenge", "to",
        "the", "US", "interpretation", "of", "good", "and", "evil", "."};
        List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
        Tree parse = lp.apply(rawWords);
        parse.pennPrint();
        System.out.println();

        TreebankLanguagePack tlp = lp.getOp().langpack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);

        //Stanford dependencies in the CoNLL format
        System.out.println(GrammaticalStructure.dependenciesToString(gs,
                gs.typedDependencies(), parse, true, false));

        Collection<TypedDependency> tdl = gs.allTypedDependencies();
        System.out.println(tdl);
        System.out.println();

        System.out.println(parse.taggedYield());
        System.out.println();
    }
}