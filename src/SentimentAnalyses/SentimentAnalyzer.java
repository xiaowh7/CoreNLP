package SentimentAnalyses;

import java.io.*;
import java.util.Properties;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyzer {

    public DBCollection collection;
    public Mongo mongo;
    public DB db;
    public int count = 1;

    public TweetWithSentiment findSentiment(String line) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        int mainSentiment = 0;
        if (line != null && line.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(line);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }

            }
        }
        if (mainSentiment == 2 || mainSentiment > 4 || mainSentiment < 0) {
            return null;
        }
        System.out.println(mainSentiment);
        TweetWithSentiment tweetWithSentiment = new TweetWithSentiment(line, toCss(mainSentiment));
        return tweetWithSentiment;

    }


    public void SntAnly(String Filename) throws IOException {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("encoding", "utf-8");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        int tweetsCount = 0, crtAnsw = 0;
//        File file = new File(Filename);
//        InputStream inputStream = new FileInputStream(file);
        //CSVReader reader = new CSVReader(new FileReader("yourfile.csv"), '\t', '\'', 2);
        CSVReader reader = new CSVReader(new FileReader(Filename),'\t');
        CSVWriter writer = new CSVWriter(new FileWriter("CoreNLP.csv"),'\t', CSVWriter.NO_QUOTE_CHARACTER);
        String [] nextLine;
        String truth, predict;
        String [] result = new String[4];
        while ((nextLine = reader.readNext()) != null) {
            tweetsCount += 1;
            if (tweetsCount%100==0){
                System.out.println(tweetsCount);
            }

            result[0] = nextLine[0];
            result[1] = nextLine[1];

//            if (truth.equals("irrelevant")) truth = "neutral";

            int mainSentiment = 0;
            String text = nextLine[3];
            if (text != null && text.length() > 0) {
                int longest = 0;
                Annotation annotation = pipeline.process(text);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    String partText = sentence.toString();
                    if (partText.length() > longest) {
                        mainSentiment = sentiment;
                        longest = partText.length();
                    }

                }
            }

            predict = "neutral";
//            if (mainSentiment == 2 || mainSentiment > 4 || mainSentiment < 0) {
//                continue;
//            }
            if (mainSentiment > 2) predict = "positive";
            else if (mainSentiment < 2) predict = "negative";
//            if (predict.equals(truth))
//                crtAnsw += 1;
//            System.out.println(text + "\t" + mainSentiment + "\t" + predict + "\t" + truth);
            result[2] = predict;
            result[3] = text;
            writer.writeNext(result);
            nextLine=null;

//            System.out.println(text + "\t" + mainSentiment);
//            TweetWithSentiment tweetWithSentiment = new TweetWithSentiment(text, toCss(mainSentiment));
//            return tweetWithSentiment;
        }
        reader.close();
        writer.close();
//        System.out.println(crtAnsw + "\t" + tweetsCount + "\t" + crtAnsw/tweetsCount);
    }


    private String toCss(int sentiment) {
        switch (sentiment) {
        case 0:
            return "alert alert-danger";
        case 1:
            return "alert alert-danger";
        case 2:
            return "alert alert-warning";
        case 3:
            return "alert alert-success";
        case 4:
            return "alert alert-success";
        default:
            return "";
        }
    }

    public void LinkMongodb() throws Exception {

        mongo = new Mongo("localhost", 27017);
        db = mongo.getDB("tstream");

        //collection = db.createCollection("movie", db);
        //collection = db.getCollection("Kun Ming train station");
        collection = db.getCollection("The Great Gatsby");
//        collection.drop();
        System.out.println("Link Mongodb!");
    }

    public void sentimentAnalyses(DBCollection dbCollection){
        String topic = dbCollection.getName();
        String StnCltName = "Sentiment of " + topic;
        DBCollection StnClt = db.getCollection(StnCltName);
        StnClt.drop();

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        int mainSentiment = 0;

        DBCursor cur = dbCollection.find();
//        System.out.println(cur.count());
        count = 0;
        //find all tweets
        while (cur.hasNext()) {
            count++;
            if (count % 50 == 0) System.out.println(count);

            DBObject dbObject = cur.next();
            String text = (String) dbObject.get("text");
            String tweetID = dbObject.get("id").toString();


            if (text != null && text.length() > 0) {
                int longest = 0;
                Annotation annotation = pipeline.process(text);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    String partText = sentence.toString();
                    if (partText.length() > longest) {
                        mainSentiment = sentiment;
                        longest = partText.length();
                    }

                }
            }
            if (mainSentiment > 4 || mainSentiment < 0) {
                continue;
            }

            BasicDBObject stnDbo = new BasicDBObject();
            stnDbo.put("topic", topic);
            stnDbo.append("tweetID", tweetID);
            stnDbo.append("tweet", text);
            stnDbo.append("sentiment", mainSentiment);
            StnClt.insert(stnDbo);
//            System.out.println(mainSentiment);
//            TweetWithSentiment tweetWithSentiment = new TweetWithSentiment(line, toCss(mainSentiment));
//            return tweetWithSentiment;

        }

//        System.out.println(cur.getCursorId());
//        System.out.println(JSON.serialize(cur));
    }

    public static void main(String[] args) throws Exception {

        SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer();
//        TweetWithSentiment tweetWithSentiment = sentimentAnalyzer
//                .findSentiment("✨Join Gatsby at Prom 2014: \"An Elegant Affair\" #great #mohicavaliers ✨ http://t.co/KK2Uc27sMn");

//        sentimentAnalyzer.LinkMongodb();

//        sentimentAnalyzer.sentimentAnalyses(sentimentAnalyzer.collection);
        sentimentAnalyzer.SntAnly("test-gold-B-processed.csv");
//        sentimentAnalyzer.SntAnly("sampleTweets.csv");
        //System.out.println(tweetWithSentiment);
    }
}