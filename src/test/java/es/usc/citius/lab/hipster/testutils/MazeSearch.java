/*
 * Copyright 2013 Centro de Investigación en Tecnoloxías da Información (CITIUS).
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

package es.usc.citius.lab.hipster.testutils;

import static org.junit.Assert.fail;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import es.usc.citius.lab.hipster.node.Node;
import es.usc.citius.lab.hipster.node.informed.CostNode;
import es.usc.citius.lab.hipster.node.informed.HeuristicNode;
import es.usc.citius.lab.hipster.util.DoubleCostEvaluator;
import es.usc.citius.lab.hipster.util.DoubleOperable;
import es.usc.citius.lab.hipster.util.NodeToStateListConverter;
import es.usc.citius.lab.hipster.util.maze.Maze2D;

/**
 * Class to generate sample maps to test different search algorithms.
 *
 * @author Adrián González Sieira
 * @author Pablo Rodríguez Mier
 * @since 26-03-2013
 * @version 1.0
 */
public final class MazeSearch {

    private static String[] testMaze1 = new String[]{
        "        ",
        "    X   ",
        "  @ X O ",
        "    X   ",
        "        ",
        "        "};
    private static String[] testMaze2 = new String[]{
        "XX@XXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        "XX XXXXXXXXXXXXX     XXXXXXXXXXX",
        "XX    XXXXXXXXXX XXX XX     XXXX",
        "XXXXX  XXXXXX    XXX XX XXX XXXX",
        "XXX XX XXXXXX XX XXX XX  XX XXXX",
        "XXX     XXXXX XXXXXX XXXXXX XXXX",
        "XXXXXXX       XXXXXX        XXXX",
        "XXXXXXXXXX XXXXX XXXXXXXXXXXXXXX",
        "XXXXXXXXXX XX    XXXXX      XXXX",
        "XXXXXXXXXX    XXXXXXXX XXXX XXXX",
        "XXXXXXXXXXX XXXXXXXXXX XXXX XXXX",
        "XXXXXXXXXXX            XXXX XXXX",
        "XXXXXXXXXXXXXXXXXXXXXXXX XX XXXX",
        "XXXXXX              XXXX XX XXXX",
        "XXXXXX XXXXXXXXXXXX XX      XXXX",
        "XXXXXX XXO   XXXXXX XXXX XXXXXXX",
        "XXXXXX XXXXX   XXX            XX",
        "XXXXXX XXXXXXX XXXXXXXXXXX XXXXX",
        "XXXXXX XXXXXXX XXXXXXXXXXXXXXXXX",
        "XXXXXX            XXXXXXXXXXXXXX"};
    private static String[] testMaze3 = new String[]{
        "                      O          ",
        "                                 ",
        "                                 ",
        "                                 ",
        "                                 ",
        "                                 ",
        "                                 ",
        "                                 ",
        "                                 ",
        "                                 ",
        "                                 ",
        "           @                     ",
        "                                 "};
    private static String[] testMaze4 = new String[]{
        "                      O          ",
        "                                 ",
        "                                 ",
        "                                 ",
        "                                 ",
        "     XXXXXXXXXXXXXXXXXXXXX       ",
        "     XXXXXXXXXXXXXXXXXXXXX       ",
        "                       XXX       ",
        "                       XXX       ",
        "                       XXX       ",
        "                       XXX       ",
        "           @                     ",
        "                                 "};
    private static String[] testMaze5 = new String[]{
        "                  X   O          ",
        "                  X              ",
        "                  XXXXXXXX       ",
        "       XXXXXXXXXX  XXXXX         ",
        "                X    XXXXXXXXXX  ",
        "     XXXXXX  XXXXXXX  XXXX       ",
        "     XXXXXX XXXXXXX  XXXXX       ",
        "                       XXX       ",
        "                       XXX       ",
        "                       XXX       ",
        "                       XXX       ",
        "           @                     ",
        "                                 "};

    private MazeSearch() {
    }

    public static final class Result {
        
        public static final Result NO_RESULT = new Result(new ArrayList<Point>(), Double.POSITIVE_INFINITY);
        private List<Point> path;
        private Double cost;

        public Result(List<Point> path, Double cost) {
            this.path = path;
            this.cost = cost;
        }

        public Double getCost() {
            return cost;
        }

        public List<Point> getPath() {
            return path;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.path != null ? this.path.hashCode() : 0);
            hash = 83 * hash + (this.cost != null ? this.cost.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Result other = (Result) obj;
            if (this.path != other.path && (this.path == null || !this.path.equals(other.path))) {
                return false;
            }
            if (this.cost != other.cost && (this.cost == null || !this.cost.equals(other.cost))) {
                return false;
            }
            return true;
        }
    }
    
    //public static Result executePrintIteratorSearch(AStar<Point> it, StringMaze maze) throws InterruptedException {
    public static Result executePrintIteratorSearch(Iterator<? extends CostNode<Point, DoubleOperable>> it, Maze2D maze) throws InterruptedException {
    	return executePrintIteratorSearch(it, maze, true);
    }

    public static Result executePrintIteratorSearch(Iterator<? extends CostNode<Point, DoubleOperable>> it, Maze2D maze, boolean exitWhenGoalReached) throws InterruptedException {
        int steps = 0;
        Result r = null;
        Collection<Point> explored = new HashSet<Point>();
        while (it.hasNext()) {
            CostNode<Point, DoubleOperable> currentNode = it.next();
            explored.add(currentNode.transition().to());
            steps++;
            List<Node<Point>> nodePath = currentNode.path();
            List<Point> statePath = new NodeToStateListConverter<Point>().convert(nodePath);
            clearOutput(20);
            //System.out.println(maze.getStringMazeFilled(explored, '.'));
            System.out.println(getMazeStringSolution(maze, explored, statePath));
            Thread.sleep(20);
            if (currentNode.transition().to().equals(maze.getGoalLoc())) {
            	//clearOutput(20);
            	//System.out.println(getMazeStringSolution(maze, explored, statePath));
            	//Thread.sleep(2000);
                Double cost = currentNode.getCost().getValue();//new DoubleCostEvaluator<Point>().evaluate(nodePath, AlgorithmIteratorFromMazeCreator.defaultCostFunction());
                r = new Result(statePath, cost);
                if (exitWhenGoalReached){
                    return r;
                }
            }
        }
        if (r == null){
        	fail("Solution not found after " + steps + " steps.");
        }
        return r;
    }
    
    public static void clearOutput(int newlines){
    	char[] chars= new char[newlines];
        Arrays.fill(chars, '\n');
        System.out.println(chars);
    }
    
    public static String getMazeStringSolution(Maze2D maze, Collection<Point> explored, Collection<Point> path){
    	List<Map<Point,Character>> replacements = new ArrayList<Map<Point,Character>>();
    	Map<Point,Character> replacement = new HashMap<Point, Character>();
    	for(Point p :explored){
    		replacement.put(p, '.');
    	}
    	replacements.add(replacement);
    	replacement = new HashMap<Point, Character>();
    	for(Point p :path){
    		replacement.put(p, '*');
    	}
    	replacements.add(replacement);
    	return maze.getReplacedMazeString(replacements);
    }
    
    //public static Result executeIteratorSearch(AStar<Point> it, StringMaze maze) {

    public static Result executeIteratorSearch(Iterator<? extends CostNode<Point, DoubleOperable>> it, Maze2D maze) {
        int steps = 0;
        while (it.hasNext()) {
            CostNode<Point, DoubleOperable> currentNode = it.next();
            steps++;
            if (currentNode.transition().to().equals(maze.getGoalLoc())) {
                List<Node<Point>> nodePath = currentNode.path();
                Double cost = currentNode.getCost().getValue();//new DoubleCostEvaluator<Point>().evaluate(nodePath, AlgorithmIteratorFromMazeCreator.defaultCostFunction());
                List<Point> statePath = new NodeToStateListConverter<Point>().convert(nodePath);
                return new Result(statePath, cost);
            }
        }
        fail("Solution not found after " + steps + " steps.");
        return null;
    }

    public static Result executeJungSearch(DirectedGraph<Point, JungEdge<Point>> jungGraph, Maze2D maze) {
        DijkstraShortestPath<Point, JungEdge<Point>> dijkstra = new DijkstraShortestPath<Point, JungEdge<Point>>(
                jungGraph, new Transformer<JungEdge<Point>, Double>() {
            public Double transform(JungEdge<Point> input) {
                return input.getCost();
            }
        }, true);
        List<JungEdge<Point>> path = dijkstra.getPath(maze.getInitialLoc(),
                maze.getGoalLoc());
        Double cost = 0.0;
        List<Point> statePath = new ArrayList<Point>();
        if(path.isEmpty()){
            return Result.NO_RESULT;
        }
        for (Iterator<JungEdge<Point>> it = path.iterator(); it.hasNext();) {
            JungEdge<Point> current = it.next();
            statePath.add(current.getSource());
            cost += current.getCost();
        }
        statePath.add(maze.getGoalLoc());
        return new Result(statePath, cost);
    }

    public static String[] getTestMaze1() {
        return testMaze1;
    }

    public static String[] getTestMaze2() {
        return testMaze2;
    }

    public static String[] getTestMaze3() {
        return testMaze3;
    }

    public static String[] getTestMaze4() {
        return testMaze4;
    }

    public static String[] getTestMaze5() {
        return testMaze5;
    }
}
