/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package SentimentAnalyses;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class PrintSampleStream {
    /**
     * Main entry of this application.
     *
     * @param args
     */
    public DBCollection collection;
    public Mongo mongo;
    public int count = 1;

    public void LinkMongodb() throws Exception {

		/*
		 * Link Mongodb
		 * build a data named FourS2
		 * build a collection named Foursquare
		 *
		 */
        mongo = new Mongo("localhost", 27017);
        DB db = mongo.getDB("tstream");

        //collection = db.createCollection("movie", db);
        collection = db.getCollection("Flight MH370");
//        collection.drop();
        System.out.println("Link Mongodb!");
    }


    public static void main(String[] args) throws TwitterException {

        final PrintSampleStream pr = new PrintSampleStream();

        try {
            pr.LinkMongodb();
        }  catch (Exception e) {
            e.printStackTrace();
        }

        ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("Em3WTI7jc90HcvKzPkTLQ")
                    .setOAuthConsumerSecret("vg4p6rOF32bmffqRR8m0jAUClrxvtGiMB5PrSr3Zsw")
                    .setOAuthAccessToken("1681973072-1q0zI0VPjHD3ttNuaBOL94frzCI9sXInxAcDK0w")
                    .setOAuthAccessTokenSecret("ZRLkOyjmhHBkU1iNyEVNyIgIBsKrl0DUDKOcOMneYFYEM");
            cb.setJSONStoreEnabled(true);

        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        TwitterStream twitterStream = tf.getInstance();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                //System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                //System.out.println(status);
                String str = DataObjectFactory.getRawJSON(status);
                try {
                    //JSONObject nnstr = new JSONObject(newstr);
                    DBObject dbObject =(DBObject)JSON.parse(str);
//                    System.out.println(dbObject);
                    pr.collection.insert(dbObject);
                    //System.out.println(dbObject);
                    pr.count++;
                    if (pr.count%1000==0) System.out.println(pr.count);
                    if(pr.count>100000) {
                        pr.mongo.close();
                        System.exit(0);
                    }
                }  catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);

        String[] trackArray;
        String[] Track = {"Malaysia Airlines", "Flight MH370", "Boeing-777", "Kuala Lumpur", "Bei jing" };
        //trackArray[0] = "Obama";
        //trackArray[1] = "Romney";

        FilterQuery filter = new FilterQuery();
        filter.track(Track);
        String[] lang = {"en"};
        filter.language(lang);
        twitterStream.filter(filter);
        //pr.mongo.close();
    }

}
