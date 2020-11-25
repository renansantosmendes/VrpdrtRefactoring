/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VRPDRT;

import Algorithms.Algorithms;
import static Algorithms.Algorithms.IteratedLocalSearch;
import static Algorithms.Algorithms.buildInstaceName;
import static Algorithms.Algorithms.rebuildSolutionForOnlineAlgorithms;
import Algorithms.EvolutionaryAlgorithms;
import static Algorithms.EvolutionaryAlgorithms.getMinMaxForObjectives;
import static Algorithms.EvolutionaryAlgorithms.initializeMaxValues;
import static Algorithms.EvolutionaryAlgorithms.initializeMinValues;
import static Algorithms.EvolutionaryAlgorithms.onMOEAD;
import static Algorithms.EvolutionaryAlgorithms.populationGeneratorForWeights;
import Algorithms.Methods;
import static Algorithms.Methods.readProblemUsingExcelData;
import InstanceReader.ReadDataInExcelFile;
import InstanceReader.ScriptGenerator;
import ProblemRepresentation.Node;
import ProblemRepresentation.ProblemSolution;
import ProblemRepresentation.Request;
import ReductionTechniques.CorrelationType;
import com.google.maps.errors.ApiException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jxl.read.biff.BiffException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author renan
 */
public class VrpdrtPaperSolution {

    static final Long timeWindows = (long) 3;
    static List<Request> requests = new ArrayList<>();
    static List<List<Integer>> listOfAdjacencies = new LinkedList<>();
    static List<List<Long>> distanceBetweenNodes = new LinkedList<>();
    static List<List<Long>> timeBetweenNodes = new LinkedList<>();
    static Set<Integer> Pmais = new HashSet<>();
    static Set<Integer> Pmenos = new HashSet<>();
    static Set<Integer> setOfNodes = new HashSet<>();
    static int numberOfNodes;
    static Map<Integer, List<Request>> requestsWhichBoardsInNode = new HashMap<>();
    static Map<Integer, List<Request>> requestsWhichLeavesInNode = new HashMap<>();
    static List<Integer> loadIndexList = new LinkedList<>();
    static Set<Integer> setOfVehicles = new HashSet<>();
    static List<Request> listOfNonAttendedRequests = new ArrayList<>();
    static List<Request> requestList = new ArrayList<>();

    //-------------------Test--------------------------------
    static Long currentTime;
    static Integer lastNode;

    public static void main(String[] args) throws ApiException, Exception, IOException, BiffException {
        String directionsApiKey = "AIzaSyDnbydYYYtvGROBcUXDiiOaxafkmJ0vyos";
        String filePath = "C:\\Doutorado - Renan\\Excel Instances\\";

        int numberOfRequests = 4;
        int requestTimeWindows = 10;
        final Integer vehicleCapacity = 4;
        String instanceSize = "s";

        int numberOfNodes = 12;
        String nodesData = "bh_n" + numberOfNodes + instanceSize;
        String adjacenciesData = "bh_adj_n" + numberOfNodes + instanceSize;
        String instanceName = buildInstaceName(nodesData, adjacenciesData, numberOfRequests, numberOfNodes,
                requestTimeWindows, instanceSize);
        Integer numberOfVehicles = 250;
        instanceName = "r004n12tw10";
        System.out.println(instanceName);

        Integer populationSize = 100;
    
        List<Double> parameters = new ArrayList<>();
        List<List<Double>> nadirPoint = new ArrayList<>();
       
        if (numberOfRequests >= 250) {
            new ScriptGenerator(instanceName, instanceSize, vehicleCapacity)
                    .generate("30d", "lamho-0");
        } else {
            new ScriptGenerator(instanceName, instanceSize, vehicleCapacity)
                    .generate("7d", "lamho-0");
        }

        numberOfNodes = readProblemUsingExcelData(filePath, instanceName, nodesData, adjacenciesData, requests, distanceBetweenNodes,
                timeBetweenNodes, Pmais, Pmenos, requestsWhichBoardsInNode, requestsWhichLeavesInNode, setOfNodes,
                numberOfNodes, loadIndexList);

        Algorithms.printProblemInformations(requests, numberOfVehicles, vehicleCapacity, instanceName, adjacenciesData, nodesData);
        Methods.initializeFleetOfVehicles(setOfVehicles, numberOfVehicles);

        parameters.add(1.0);
        parameters.add(1.0);
        parameters.add(1.0);
        parameters.add(1.0);
        parameters.add(1.0);
        parameters.add(1.0);
        parameters.add(1.0);
        parameters.add(1.0);

        List<Double> mins = initializeMinValues();
        List<Double> maxs = initializeMaxValues();
        nadirPoint.add(mins);
        nadirPoint.add(maxs);

        int reducedDimension = 2;

        ProblemSolution solution = new ProblemSolution();
        ProblemSolution solutionOptimized = new ProblemSolution();
        solution.setSolution(Algorithms.greedyConstructive(0.4, 0.2, 0.2, 0.2, nadirPoint, 0, requests, requestsWhichBoardsInNode,
                requestsWhichLeavesInNode, numberOfNodes, vehicleCapacity, setOfVehicles, listOfNonAttendedRequests, requestList,
                loadIndexList, timeBetweenNodes, distanceBetweenNodes, timeWindows, currentTime, lastNode));

        System.out.println("Solution");
        System.out.println(solution);

        List<Node> nodes = new ReadDataInExcelFile(filePath, instanceName, nodesData, adjacenciesData).getListOfNodes();

        solution.getStaticMapWithAllRoutes(nodes, "bh_adj_n12s", nodesData);
        
        solutionOptimized.setSolution(Algorithms.IteratedLocalSearch(reducedDimension, nadirPoint, parameters, solution, requests, requestsWhichBoardsInNode, requestsWhichLeavesInNode,
                numberOfNodes, vehicleCapacity, setOfVehicles, listOfNonAttendedRequests, requestList, loadIndexList, timeBetweenNodes, distanceBetweenNodes, timeWindows));
        
        System.out.println("Final solution");
        System.out.println(solutionOptimized);
        solutionOptimized.getStaticMapWithAllRoutes(nodes, "bh_adj_n12s", nodesData);
        
    }
}
