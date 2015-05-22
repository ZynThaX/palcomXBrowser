package se.lth.cs.palcom.browsergui.views;


import java.awt.*;
import java.util.ArrayList;

/**
 * Created by jonas on 2015-05-21.
 */
public class GraphConnectionsPositioning {
    public String source, target;
    public ArrayList<Point> points;

    public GraphConnectionsPositioning(String source, String target){
        this.source = source;
        this.target = target;
    }

    public boolean addPoint(int x, int y){
        return points.add(new Point(x,y));
    }
}
