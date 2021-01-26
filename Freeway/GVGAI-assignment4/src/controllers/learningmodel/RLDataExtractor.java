/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.learningmodel;

import tools.*;
import core.game.Observation;
import core.game.StateObservation;

import java.io.FileWriter;
import java.lang.FdLibm.Pow;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;

import ontology.Types;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author yuy
 */
public class RLDataExtractor {
    public FileWriter filewriter;
    public static Instances s_datasetHeader = datasetHeader();
    
    public RLDataExtractor(String filename) throws Exception{
        
        filewriter = new FileWriter(filename+".arff");
        filewriter.write(s_datasetHeader.toString());
        /*
                // ARFF File header
        filewriter.write("@RELATION AliensData\n");
        // Each row denotes the feature attribute
        // In this demo, the features have four dimensions.
        filewriter.write("@ATTRIBUTE gameScore  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarSpeed  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarHealthPoints NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarType NUMERIC\n");
        // objects
        for(int y=0; y<14; y++)
            for(int x=0; x<32; x++)
                filewriter.write("@ATTRIBUTE object_at_position_x=" + x + "_y=" + y + " NUMERIC\n");
        // The last row of the ARFF header stands for the classes
        filewriter.write("@ATTRIBUTE Class {0,1,2}\n");
        // The data will recorded in the following.
        filewriter.write("@Data\n");*/
        
    }
    
    public static Instance makeInstance(double[] features, int action, double reward){
        features[872] = action;
        features[873] = reward;
        Instance ins = new Instance(1, features);
        ins.setDataset(s_datasetHeader);
        return ins;
    }
    
    public static double[] featureExtract(StateObservation obs){
        
        double[] feature = new double[874];  // 868 + 4 + 1(action) + 1(Q)
        
        // 448 locations
        int[][] map = new int[28][31];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if( obs.getImmovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getImmovablePositions()) allobj.addAll(l);
        if( obs.getMovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getMovablePositions()) allobj.addAll(l);
        if( obs.getNPCPositions()!=null )
            for(ArrayList<Observation> l : obs.getNPCPositions()) allobj.addAll(l);
        
        for(Observation o : allobj){
            Vector2d p = o.position;
            int x = (int)(p.x/28); //squre size is 20 for pacman
            int y= (int)(p.y/28);  //size is 28 for FreeWay
            map[x][y] = o.itype;
        }
        for(int y=0; y<31; y++)
            for(int x=0; x<28; x++)
                feature[y*28+x] = map[x][y];
        
        // 4 states
        feature[868] = obs.getGameTick();
        feature[869] = obs.getAvatarSpeed();
        feature[870] = obs.getAvatarHealthPoints();
        feature[871] = obs.getAvatarType();

        feature[872] = obs.getGameScore();

        //get portalsposition
        int portal_x = 0;
        int portal_y = 0;
        for(int y=0; y<31; y++)
        for(int x=0; x<28; x++){
        feature[y * 28 + x] = map[x][y];
        if(map[x][y] == 4){
        portal_x = x;
        portal_y = y;
        }
        }
        //get avatar position
        Vector2d avatar_position = obs.getAvatarPosition();
        int avatar_x = (int)(avatar_position.x/28);
        int avatar_y = (int)(avatar_position.y/28);
        //get the distance in y
        double distance_avater_portal = Math.abs(avatar_y-portal_y);
        //add the feature
        feature[873] = distance_avater_portal;

        
        return feature;
    }
    

    /*public static double nearestDir(StateObservation obs, ArrayList<Observation> list){
        double mindir = 100000;
        Vector2d obs_2d = obs.getAvatarPosition();
        for (Observation state : list){
            double dist = state.position.dist(obs_2d);
            if (dist < mindir){
                mindir = dist;
            }
        }
        return mindir;
    }

    public static double[] featureExtract(StateObservation obs) {
        double[] feature = new double[s_datasetHeader.numAttributes()];
        if (obs.getImmovablePositions() != null) {
            for (ArrayList<Observation> l : obs.getImmovablePositions()) {
                if (l.size() == 0)
                    continue;
                // pellets
                else if (l.get(0).itype == 4) {
                    feature[0] = nearestDir(obs, l);
                }
                // fruit
                else if (l.get(0).itype == 3) {
                    feature[1] = nearestDir(obs, l);
                }
            }
        }
        // 4 ghosts
        ArrayList<Observation> allGhost = new ArrayList<>();
        if (obs.getPortalsPositions() != null) {
            for (ArrayList<Observation> l : obs.getPortalsPositions()) {
                allGhost.addAll(l);
            }
        }
        feature[2] = nearestDir(obs, allGhost);
        // isNearGhost
        //feature[5] = isNearGhost(obs, allGhost) ? 1 : 0;
        // 4 powerpills
        if (obs.getResourcesPositions() != null) {
            for (ArrayList<Observation> l : obs.getResourcesPositions()) {
                feature[3] = nearestDir(obs, l);
            }
        }
        // 4 ghost direct
        Vector2d avatarPos = obs.getAvatarPosition();
        int i = 4;
        for (Observation o : allGhost) {
            double delta_x = o.position.x - avatarPos.x;
            double delta_y = o.position.y - avatarPos.y;
            double dist = o.position.dist(avatarPos);
            feature[i] = Math.acos(delta_x / dist) + (delta_y < 0 ? Math.PI : 0);
            i++;
        }
    
        return feature;
    }*/


    public static Instances datasetHeader(){
        
        if (s_datasetHeader!=null)
            return s_datasetHeader;
        
        FastVector attInfo = new FastVector();
        // 448 locations
        for(int y=0; y<28; y++){
            for(int x=0; x<31; x++){
                Attribute att = new Attribute("object_at_position_x=" + x + "_y=" + y);
                attInfo.addElement(att);
            }
        }
        Attribute att = new Attribute("GameTick" ); attInfo.addElement(att);
        att = new Attribute("AvatarSpeed" ); attInfo.addElement(att);
        att = new Attribute("AvatarHealthPoints" ); attInfo.addElement(att);
        att = new Attribute("AvatarType" ); attInfo.addElement(att);
        //action
        FastVector actions = new FastVector();
        actions.addElement("0");
        actions.addElement("1");
        actions.addElement("2");
        actions.addElement("3");
        att = new Attribute("actions", actions);        
        attInfo.addElement(att);
        // Q value
        att = new Attribute("Qvalue");
        attInfo.addElement(att);
        
        Instances instances = new Instances("PacmanQdata", attInfo, 0);
        instances.setClassIndex( instances.numAttributes() - 1);
        
        return instances;
    }
    
}
