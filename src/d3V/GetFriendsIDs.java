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

package d3V;

import data_collector.TwitterDataCollector;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Lists followers' ids
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class GetFriendsIDs {


    static final long MILLISECOND = 1000;
    /**
     * Usage: java twitter4j.examples.friendsandfollowers.GetFollowersIDs [screen name]
     *
     * @param args message
     */
    public static void main(String[] args) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("Em3WTI7jc90HcvKzPkTLQ")
                .setOAuthConsumerSecret("vg4p6rOF32bmffqRR8m0jAUClrxvtGiMB5PrSr3Zsw")
                .setOAuthAccessToken("1681973072-1q0zI0VPjHD3ttNuaBOL94frzCI9sXInxAcDK0w")
                .setOAuthAccessTokenSecret("ZRLkOyjmhHBkU1iNyEVNyIgIBsKrl0DUDKOcOMneYFYEM");
        cb.setJSONStoreEnabled(true);

        TwitterFactory tf = new TwitterFactory(cb.build());
        try {
            String core_user = "nytimes";
            FileWriter writer=new FileWriter(core_user+"_friends.txt", true);
            Twitter twitter = tf.getInstance();

            long cursor = -1;
            ArrayList<String> directFriends = new ArrayList<String>();
            PagableResponseList<User> tempFriends = null;

            System.out.println("Listing " + core_user + " friends's ScreenNames.");
            int count = 0;
            //boolean first = true;
            //getFriends(writer, twitter, cursor, count, core_user);
            do {
                tempFriends = twitter.getFriendsList(core_user, cursor);

                for (User user : tempFriends){
                    directFriends.add(user.getScreenName());
                    writer.write(core_user + "\t" + user.getScreenName() + "\n");
                }

                count += tempFriends.size();
                System.out.println(core_user + ": " + count);
                if (count >= 100) break;

                Thread.sleep(300 * MILLISECOND);

            } while ((cursor = tempFriends.getNextCursor()) != 0);

            System.out.println("Size of DFs: " + directFriends.size());


//            for (String user : directFriends){
            for (int i=0; i<directFriends.size(); i++){
//                PagableResponseList<User> idrfriends = null;
                long _cursor = -1;
                int _count = 0;
                String _username = directFriends.get(i);
                System.out.println("\nFriends of Direct friend: " + _username + "(" + (i+1) +")");
                try {
                    getFriends(writer, twitter, _cursor, _count, _username);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    System.out.println("Failed to get friends' ids: " + e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


            writer.close();
            System.out.println(count);
            System.exit(0);
        } catch (TwitterException e) {
            e.printStackTrace();
            System.out.println("Failed to get friends' ids: " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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

            Thread.sleep(300 * MILLISECOND);
        } while ((_cursor = idrfriends.getNextCursor()) != 0);
    }


}
