package d3V;

import java.util.regex.Pattern;

public class Movie{

    private String name;
    private int	count;
    private Pattern pattern;

    Movie(String _name){
        name = _name;
    }

    Movie(String _name, Pattern _pattern, int _count){
        name = _name;
        pattern = _pattern;
        count = _count;
    }

    String getName(){
        return name;
    }

    int getCount(){
        return count;
    }

    void plusCount(){
        count++;
    }

    void setPattern(Pattern _pattern){
        pattern = _pattern;
    }

    Pattern getPattern(){
        return pattern;
    }



}