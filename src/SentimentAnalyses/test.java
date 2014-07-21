package SentimentAnalyses;

import au.com.bytecode.opencsv.CSVReader;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    static final long MILLISECOND = 1000;

    public static void main(String[] args) throws IOException {
        //String str="✨Join Gatsby at Prom 2014: \"An Elegant Affair\" #great #mohicavaliers ✨ http://t.co/KK2Uc27sMn";
        //str.replaceAll("[^a-zA-Z0-9\\s]","");
//        CSVReader reader = new CSVReader(new FileReader("test.csv"));
//            String [] nextLine;
//            while ((nextLine = reader.readNext()) != null) {
//            // nextLine[] is an array of values from the line
//            System.out.println(nextLine[0] + nextLine[1] + "etc...");
//            }

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("Em3WTI7jc90HcvKzPkTLQ")
                .setOAuthConsumerSecret("vg4p6rOF32bmffqRR8m0jAUClrxvtGiMB5PrSr3Zsw")
                .setOAuthAccessToken("1681973072-1q0zI0VPjHD3ttNuaBOL94frzCI9sXInxAcDK0w")
                .setOAuthAccessTokenSecret("ZRLkOyjmhHBkU1iNyEVNyIgIBsKrl0DUDKOcOMneYFYEM");
        cb.setJSONStoreEnabled(true);


            TwitterFactory tf = new TwitterFactory(cb.build());
            final Twitter twitter = tf.getInstance();
    //        try {
    //            Status status = twitter.showStatus(Long.parseLong("438899323623837697"));
    //
    //            if (status == null) { //
    //                // don't know if needed - T4J docs are VERY BAD
    //            } else {
    //                System.out.println("@" + status.getUser().getScreenName()
    //                        + " - " + status.getText());
    //            }
    //        } catch (TwitterException e) {
    //            System.err.print("Failed to search tweets: " + e.getMessage());
    //            // e.printStackTrace();
    //            // DON'T KNOW IF THIS IS THROWN WHEN ID IS INVALID
    //        }
            try {
                String core_user = "svlafraniere";
                FileWriter writer=new FileWriter(core_user+"_friends.txt", true);

                long cursor = -1;
                PagableResponseList<User> directFriends = null;

                System.out.println("Listing " + core_user + " friends's ScreenNames.");
                int count = 0;
                getFriends(writer, twitter, cursor, count, core_user);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (TwitterException e) {
                e.printStackTrace();
            }

    }

    public static void getFriends(FileWriter writer, Twitter twitter, long _cursor, int _count, String _username) throws TwitterException, IOException, InterruptedException {
        PagableResponseList<User> idrfriends;
        do {
                idrfriends= twitter.getFriendsList(_username, _cursor);
                for (User _user : idrfriends){
                     writer.write(_username + "\t" + _user.getScreenName() + "\n");
                }
                _count += idrfriends.size();
                System.out.println(_username + ": " + _count);
                if (_count >= 40) break;

                Thread.sleep(120 * MILLISECOND);
            } while ((_cursor = idrfriends.getNextCursor()) != 0);
        }
}