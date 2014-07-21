package d3V;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;

/**
 * Usage
 * java flickoh.ConvertGraph targetuser moviename interestgraph sentiments
 *
 */

public class ConvertGraph{

    String targetUser;
    String movieName;
    String interestGraph;
    String sentimentFile;
    String outputFileName;

    HashMap<String,Node> nodeMap;
    ArrayList<Node> nodes;
    ArrayList<Link> links;
    ArrayList<String> movies;

    int totalTweets = 0;
    int allSentiment = 0;
    float overallSentiment = 0.0f;

//    Example:
//    alsmola
//    Skyfall
//    ./@alsmola/alsmola_core3_graph.gml
//    ./@alsmola/alsmola_core3_statuses_with_sentiment.json

    public static void main(String[] args) throws IOException{

//        if (args[0] == null || args[1] == null || args[2] == null) return;
        if (args[0] == null || args[1] == null) return;

        String outputFileName = "./@"+args[0]+"/"+args[0]+"_"+args[1]+"_d3.json";
//        ConvertGraph convert = new ConvertGraph(args[0], args[1], args[2], args[3], outputFileName);
        String interestGraph = "./@"+args[0]+"/"+args[0]+"_core2_graph.gml";
        String sentimentFile = "./@"+args[0]+"/"+args[0]+"_core2_statuses_with_sentiment.json";
        ConvertGraph convert = new ConvertGraph(args[0], args[1], interestGraph, sentimentFile, outputFileName);
        convert.readFiles();
        convert.calculateAvgSentiment();
        convert.createMovieGraph();
        convert.writeFiles();

    }

    ConvertGraph(String targetUser, String movieName, String interestGraph, String sentimentFile, String outputFileName) throws IOException{

        this.targetUser = targetUser;
        this.movieName = movieName;
        this.interestGraph = interestGraph;
        this.sentimentFile = sentimentFile;
        this.outputFileName = outputFileName;

    }


    // reading input files
    private void readFiles() throws IOException{

        BufferedReader moviebr = new BufferedReader(new FileReader("movielist"));
        String movieline;
        int movieIndex = -1;

        movies = new ArrayList<String>();

        // create arraylist of movie object
        while((movieline = moviebr.readLine()) != null){
            String[] splits = movieline.split("\\t");
            String name = splits[0].trim();
            movies.add(name);
        }

        moviebr.close();


        for(int i=0; i< movies.size(); i++){
            if (movies.get(i).equals(movieName)) movieIndex = i;
        }

        System.out.println("movie name: "+movieName);
        System.out.println("movie index: "+movieIndex);


        // reading interest graph

        BufferedReader br = new BufferedReader(new FileReader(interestGraph));

        String line;
        String id;
        String name;
        String source;
        String target;
        String nextLine;
        String[] splits, nextSplits;

        nodes = new ArrayList<Node>();
        links = new ArrayList<Link>();

        while ((line = br.readLine()) != null) {
            if (line.length() == 0 ) {
                //skip this line
            } else {

                splits = line.trim().split("\\s+");

                if ( splits[0].equals("id")){
                    id = splits[1];
                    nextLine = br.readLine();

                    nextSplits = nextLine.trim().split("\\s+");
                    if ( nextSplits[0].equals("label")){
                        name = nextSplits[1];

                        nodes.add(new Node(id,name));
                    }
                }

                if (splits[0].equals("source")){

                    source = nodes.get(Integer.parseInt(splits[1])).getName();

                    nextLine = br.readLine();
                    nextSplits = nextLine.trim().split("\\s+");
                    if ( nextSplits[0].equals("target")){
                        target = nodes.get(Integer.parseInt(nextSplits[1])).getName();
                        links.add(new Link(source, target));
                    }
                }
            }
        }

        br.close();

        System.out.println("the size of original nodes: "+ nodes.size());
        System.out.println("the size of original links: "+links.size());

        nodeMap = new HashMap<String,Node>();
        Iterator<Node> it = nodes.iterator();
        while(it.hasNext()){
            Node n = it.next();
            nodeMap.put(n.getName(), n);
            //System.out.println("nodeMap: "+n.getName());
        }


        // Reading sentiment file

        BufferedReader sentimentBr = new BufferedReader(new FileReader(sentimentFile));

        int polarity;
        String tweet;
        String user;
        int idx;

        Pattern p = 	Pattern.compile("\"polarity\":\\s*(\\d+)\\s*,\\s*\"name\":\\s*(\"\\w+\"),\\s*\"no\":\\s*(\\d+),\\s*\"text\":\\s*(\"((?!\"[a-zA-Z]*\":).)*\"),\\s*\"meta\":");
        // "polarity":\s*(\d+)\s*,\s*"name":\s*("\w+"),\s*"no":\s*\d+,\s*"text":\s*"(((?!"[a-zA-Z]*":).)*)",\s*"meta":


        Matcher m;
        while ((line = sentimentBr.readLine()) != null) {
            if (line.length() == 0 ) {
                // skips when it matches on ????
            } else {

                //System.out.println("line: " +line);
                m = p.matcher(line);

                if(m.find()){
                    polarity = Integer.parseInt(m.group(1));
                    user = m.group(2);
                    idx = Integer.parseInt(m.group(3));
                    tweet = m.group(4);

                    // only this tweet is related to the specific movie
                    if(idx == movieIndex){
                        System.out.println("related tweet sentiment:" + idx + "- " + tweet);
                        Node n = nodeMap.get(user);
                        if (n != null){
                            n.addSentiment(polarity);
                            n.addTweet(tweet);
                            n.addNoTweets();
                        }
                        else {
                            System.out.println(" error! there is no node for this user - "+ user);
                        }
                    }
                }
            }
        }

        sentimentBr.close();

    }

    private void calculateAvgSentiment(){
        // calculate average sentiment
        // getSentiment: return the sum of sentiment of the user
        // sentiment: 0 - negative 2 - neutral 4 - positive
        // allSentiment: sum of the sentiment of each user

        allSentiment = 0;
        Iterator<Node> it = nodes.iterator();
        while(it.hasNext()){
            Node n = it.next();
            if (n.getNoTweets() != 0){
                n.setAvgSentiment((float)n.getSentiment()/n.getNoTweets());
                allSentiment +=n.getSentiment();
            }
            else {
                // **********************************
                // NOTE: if there is no tweets, the avg sentiment is set to 1 ***
                // **********************************
                n.setAvgSentiment((float)1);
            }
            //System.out.println(new DecimalFormat("#.##").format(n.getAvgSentiment()));
        }

    }

    private void createMovieGraph(){
        // scan all the nodes, and see if a node is talking about this movie;
        // if so, put this node in the firstMap (hashtable)

        HashMap<String, Node> firstMap = new HashMap<String, Node>();
        Iterator<Node> iter = nodes.iterator();

        String quoatedTargetUser = "\""+targetUser+"\"";

        while(iter.hasNext()){
            Node n = iter.next();

            if (n.getName().equals(quoatedTargetUser)) {
                n.setLevel(0);  // himself
                firstMap.put(n.getName(), n);
            }else{
                if(isDirectFriend(n.getName(), quoatedTargetUser)) n.setLevel(1);
                else n.setLevel(2);

                if(n.getNoTweets()>0) firstMap.put(n.getName(),n);
            }

        }

        // Add the direct friends who don't talk about this movie, but have more than one friends talking about this movie
        // scan firstMap to see if a 2nd level node's 1nd level nodes are included
        // if not, put these not-included level 1 friends into secondMap ;

        HashMap<String, Node> secondMap = new HashMap<String, Node>();
        Iterator<Node> iterator = firstMap.values().iterator();
        while(iterator.hasNext()){
            Node node = iterator.next();
            if(node.getLevel() == 2){
                //see if this node's level1 friends are included
                for(int i=0; i<links.size();i++){
                    String friendName = links.get(i).getFriend(node.getName());
                    if (friendName != null){
                        if(firstMap.containsKey(friendName)) continue;
                        else secondMap.put(friendName,nodeMap.get(friendName));
                    }
                }
            }
        }

        // combine firstMap and SecondMap into newNodesMap
        // newNodesMap contains only nodes who are talking about this movie, or
        // level 1 nodes who have connected nodes(i.e. friends) talking about this movie

        HashMap<String, Node> newNodesMap = new HashMap<String,Node>();
        newNodesMap.putAll(firstMap);
        newNodesMap.putAll(secondMap);

        // scan links if the link is connected to the nodes in the newNodesMap
        // add these connected links into newLinks

        ArrayList<Link> newLinks = new ArrayList<Link>();
        for(int i=0; i<links.size(); i++){
            Link link = links.get(i);
            if (newNodesMap.containsKey(link.getSource()) && newNodesMap.containsKey(link.getTarget())) newLinks.add(link);
        }


        // clear nodes, copy newNodesmap into nodes
        // assign new id for nodes

        nodes.clear();
        nodes.addAll(newNodesMap.values());

        // totalTwets: total number of movie tweets
        // allSentiment:  sum of the sentiment of each user
        // overallSentiment = the sum of sentiment of all the friends / totalTweets

        totalTweets = 0;
        for(int i=0;i<nodes.size();i++){
            Node node = nodes.get(i);
            node.setId(Integer.toString(i));
            totalTweets += node.getNoTweets();
        }
        overallSentiment = 0.0f;
        if (totalTweets>0)
            overallSentiment = (float)allSentiment/totalTweets;

        System.out.println(new DecimalFormat("#.##").format(overallSentiment));
        System.out.println("all Sentiment: "+ allSentiment);
        System.out.println("The number of new nodes: "+nodes.size());
        System.out.println("The total number of tweets: "+ totalTweets);

        // clear links, copy newLinks into links

        links.clear();
        links.addAll(newLinks);

        // assign new sourceId & targetId

        System.out.println("The number of new links: "+links.size());

        for(int i=0;i<links.size();i++){
            Link link = links.get(i);
            link.setSourceId(newNodesMap.get(link.getSource()).getId());
            link.setTargetId(newNodesMap.get(link.getTarget()).getId());
        }
    }

    private void writeFiles(){
        //write a file

        write("{\"movie\":\""+movieName+"\",");
        write("\"targetUser\":\""+targetUser+"\",");
        write("\"totalTweets\":\""+totalTweets+"\",");
        write("\"overallSentiment\":\""+new DecimalFormat("#.##").format(overallSentiment)+"\",");
        write("\"nodes\":[");


        for(int i =0; i<nodes.size(); i++){
            Node node = nodes.get(i);
            write("{\"name\":");
            write(node.getName());
            write(",\"sentiment\":");
            //System.out.println(node.getAvgSentiment());
            write(new DecimalFormat("#.##").format(node.getAvgSentiment()));
            write(", ");
            write("\"level\":");
            write(Integer.toString(node.getLevel()));
            write(", ");
            write("\"noTweets\":");
            write(Integer.toString(node.getNoTweets()));
            write(", ");

            if (node.getNoTweets() ==0) {
                write("\"tweets\":[]");
            }else{
                ArrayList<String> tweets = node.getTweets();
                write("\"tweets\":[");
                for(int j=0;j<tweets.size();j++){

                    write("{\"text\":" + tweets.get(j)+"}");
                    if (j == tweets.size()-1) write("]");
                    else write(",");
                }
            }

            write(",\"id\":");
            write(node.getId());
            if (i == nodes.size()-1) write("}]\n");  // last element
            else write("},\n");
        }

        // links
        write("\"links\":[");



        for(int i =0; i<links.size(); i++){
            Link link = links.get(i);
            write("{\"source\":");
            write(link.getSourceId());
            write(",\"target\":");
            write(link.getTargetId());
            if ( i == links.size()-1) write("}]}");  //last element
            else write("},");
        }

        endWrite();

        System.out.println("done!");

    }

    FileWriter fw;

    private void write(String s) {
        try {
            if (fw == null)
                fw = new FileWriter(outputFileName, false);
            fw.write(s);
        } catch (IOException e) {}
    }

    private void endWrite() {
        try {
            fw.close();
        } catch (IOException e) {}
    }

    private boolean isDirectFriend(String user1, String user2){

        for(int i=0;i<links.size();i++){
            if(links.get(i).isFriend(user1, user2)) return true;
        }

        return false;
    }

}


class Node{
    String id;
    String name;
    int sentiment;
    float avgSentiment;
    ArrayList<String> tweets;
    int noTweets;
    int level;

    Node(String id, String name){
        this.id = id;
        this.name = name;
        tweets = new ArrayList<String>();
        sentiment = 0;
        noTweets = 0;
        avgSentiment = 0.0f;
        level = 2;
    }

    String getId(){
        return id;
    }

    void setId(String id){
        this.id = id;
    }

    String getName(){
        return name;
    }

    void setLevel(int level){
        this.level = level;
    }

    int getLevel(){
        return level;
    }

    ArrayList<String> getTweets(){
        return tweets;
    }

    void addTweet(String s){
        tweets.add(s);
    }

    void addSentiment(int sent){
        sentiment += sent;
    }

    int getSentiment(){
        return sentiment;
    }

    void setAvgSentiment(float f){
        avgSentiment = f;
    }

    float getAvgSentiment(){
        return avgSentiment;
    }

    void addNoTweets(){
        noTweets++;
    }

    int getNoTweets(){
        return noTweets;
    }

}

class Link{
    String source;
    String target;
    String sourceId;
    String targetId;

    Link(String source, String target){
        this.source = source;
        this.target = target;
    }

    String getSource(){
        return source;
    }

    String getTarget(){
        return target;
    }

    void setSourceId(String sourceId){
        this.sourceId = sourceId;
    }

    void setTargetId(String targetId){
        this.targetId = targetId;
    }

    String getSourceId(){
        return sourceId;
    }

    String getTargetId(){
        return targetId;
    }

    boolean isFriend(String s, String s1){

        if ((s.equals(source) && s1.equals(target))) return true;
        if ((s.equals(target) && s1.equals(source))) return true;

        return false;
    }

    String getFriend(String s){

        if(s.equals(source)) return target;
        if(s.equals(target)) return source;

        return null;
    }
}
