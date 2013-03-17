package map;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import codeanticode.glgraphics.GLConstants;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.*;
import com.hp.hpl.jena.util.FileManager;

import markers.AttendeeMarker;

import org.geonames.*;

public class Map extends PApplet {
	
	private static final long serialVersionUID = -325652256485957908L;
	
	final String inputFileName2011  = "2011_fulltext_.rdf";
	final String inputFileName2012  = "2012_fulltext_.rdf";
	final String inputFileNamejets  = "jets12_fulltext_.rdf";
	
	Model m = ModelFactory.createDefaultModel();
	InputStream in2011 = FileManager.get().open(inputFileName2011 );
	InputStream in2012 = FileManager.get().open(inputFileName2012 );
	InputStream injets = FileManager.get().open(inputFileNamejets );
	
	ArrayList<HashMap<String, String>> nameAffiliation;
	ArrayList<HashMap<String, String>> affiliationLocation;
	ArrayList<HashMap<String, List<String>>> organisationMembers;
	List<ArrayList<String>> attending;
	List<ArrayList<String>> affiliations;
	
	UnfoldingMap currentMap;
	UnfoldingMap map1;
	UnfoldingMap map2;
	List<ArrayList<Marker>> cities;
	
	ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria(); // the object we need for our search

	public void setup() {
		size(800, 600, GLConstants.GLGRAPHICS);
		
		//2011 LAK
		nameAffiliation = new ArrayList<HashMap<String, String>>(2);
		nameAffiliation.add(new HashMap<String, String>());
		nameAffiliation.add(new HashMap<String, String>());
		
		affiliationLocation =  new ArrayList<HashMap<String, String>>(2);
		affiliationLocation.add(new HashMap<String, String>());
		affiliationLocation.add(new HashMap<String, String>());
		
		attending = new ArrayList< ArrayList<String>>(2);
		attending.add(new ArrayList<String>());
		attending.add(new ArrayList<String>());
		
		affiliations = new ArrayList<ArrayList<String>>(2);
		affiliations.add(new ArrayList<String>());
		affiliations.add(new ArrayList<String>());
		
		cities = new ArrayList<ArrayList<Marker>>(2);
		cities.add(new ArrayList<Marker>());
		cities.add(new ArrayList<Marker>()); 
		
		organisationMembers = new ArrayList<HashMap<String, List<String>>>(2);
		organisationMembers.add(new HashMap<String, List<String>>());
		organisationMembers.add(new HashMap<String, List<String>>());
		
		String location2011 = "Banff";
		PImage image = loadImage("marker_green.png");
		cities.get(0).add(getMarker(location2011, "LAK2011_Conference", image,0));
		String location2012 = "Vancouver";
		cities.get(1).add(getMarker(location2012, "LAK2012_Conference", image,1));
	    map1 = new UnfoldingMap(this);
	    map2 = new UnfoldingMap(this);
	    MapUtils.createDefaultEventDispatcher(this, map1, map2);
	 
	    currentMap = map1;
		MapUtils.createDefaultEventDispatcher(this, currentMap);
		processRDF(in2011, 0);
		processRDF(in2012, 1);
		map1.addMarkers(cities.get(0));
		map2.addMarkers(cities.get(1));
		
	}
		
	private void processRDF(InputStream file, int i) {
		m.read(file,null);
		getAttendees(m, i);
		getAffiliations(m,i);
		getCities(m,i);
		lookUpLocations(m,i);
	}
	
	private void lookUpLocations(Model m2, int i) {
		//Get the coordinates of the location of the affilliation
		WebService.setUserName("greetcapsel"); //connect to geoNames 
		Iterator<String> iterOverLocation = affiliationLocation.get(i).keySet().iterator();
		PImage image;
		if(i==0)
			image = loadImage("marker_red.png");
		else
			image = loadImage("marker_blue.png");
		while(iterOverLocation.hasNext()) {
			String affiliation = (String)iterOverLocation.next();
			String locationName = (String)affiliationLocation.get(i).get(affiliation); //the string of the location we want to search in the geonames database
			cities.get(i).add(getMarker(locationName, affiliation, image,i));
		}
		System.out.println("OK!");
	}

	public void getAttendees(Model m, int i) {
		//Read in all attendees
		System.out.println("Reading in attendees...");
		ResIterator iter = m.listResourcesWithProperty(FOAF.based_near);
		if (iter.hasNext()) {
		    while (iter.hasNext()) {
		    	Resource thisElement = iter.nextResource();
		    	String name = thisElement.getRequiredProperty(FOAF.name).getString();
		    	//Where is this person coming from?
		    	String placeURL = thisElement.getPropertyResourceValue(FOAF.based_near).toString();
		    	@SuppressWarnings("unused")
				String place = parse(placeURL);
	    		if(!attending.get(i).contains(name))
	    			attending.get(i).add(name);
		    }
		} 
		else 
		     System.out.println("No attendees were found in the database");
		System.out.println("OK!");
	}
	
	public void getAffiliations(Model m, int i) {
		//Search their affiliation
		System.out.println("Reading in affiliations...");
		ResIterator iter2 = m.listResourcesWithProperty(FOAF.member);
		if (iter2.hasNext()) {
			while (iter2.hasNext()) {
		    	Resource thisElement = iter2.nextResource();
		    	String affiliation = thisElement.getRequiredProperty(FOAF.name).getString();
		    	affiliations.get(i).add(affiliation);
		    	StmtIterator members = thisElement.listProperties(FOAF.member);
		    	List<String> names = new ArrayList<String>();
		    	while(members.hasNext()) {
		    		String urlName = members.nextStatement().getResource().toString();
					String name = parse(urlName);
		    		names.add(name);
		    	}
		    	organisationMembers.get(i).put(affiliation, names);
		    }
		} 
		else {
		     System.out.println("No organisations were found in the database");
		}
		System.out.println("OK!");
	}
	
	public void getCities(Model m, int i) {
		//Create the hashmap for all present affiliations
		//Read in the affiliation locations
		System.out.println("Reading cities...");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("universities.txt"));
			String text = null;
	         // repeat until all lines is read
	         while ((text = reader.readLine()) != null) {
	             String[] parts = text.split("&");
	             String organisation = parts[0];
	             String city = parts[1];
	             //String state = parts[2];
	             //String county = parts[3];
	             if(affiliations.get(i).contains(organisation))
	             {
	            	 if(!affiliationLocation.get(i).containsKey(organisation)){
	            		 affiliationLocation.get(i).put(organisation, city);
	            	 }
	             }
	         }
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("OK!");
	}
		

	public void draw() {
		background(0);
		currentMap.draw();
	}
	
	public String parse(String url) {
    	String[] splitURL = url.split("/");
    	String result =  splitURL[splitURL.length-1];
    	if (result.contains("-")){
    		result = result.replaceAll("-", " ");
    	}
    	return result;
	}
	
	public void mouseMoved() {
	    Marker hitMarker = currentMap.getFirstHitMarker(mouseX, mouseY);
	    if (hitMarker != null) {
	        // Select current marker 
	        hitMarker.setSelected(true);
	    } else {
	        // Deselect all other markers
	        for (Marker marker : currentMap.getMarkers()) {
	            marker.setSelected(false);
	        }
	    }
	}
	
	public void keyPressed() {
	    if (key == '1') {
	        currentMap = map1;
	    } else if (key == '2') {
	        currentMap = map2;
	    }
	}
	
	public Marker getMarker(String location, String affiliation,PImage markerImage,int i)
	{
		AttendeeMarker marker = null;
		WebService.setUserName("greetcapsel"); //connect to geoNames 
		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria(); //the object we need for our search
		searchCriteria.setQ(location); //setup the main search item, the name of the location
		 try {
             	ToponymSearchResult searchResult = WebService.search(searchCriteria); // a toponym search result as returned by the geonames webservice.
             	//System.out.println(locationName + " is located at " + searchResult.getToponyms().get(0).getLatitude() + ", "+ searchResult.getToponyms().get(0).getLongitude());
             	Location coordinates = new Location(searchResult.getToponyms().get(0).getLatitude(), searchResult.getToponyms().get(0).getLongitude());
             	//System.out.println(coordinates.getLat() + ", " + coordinates.getLon());
             	PFont font = loadFont("Helvetica-12.vlw");
             	List<String> members = organisationMembers.get(i).get(affiliation);
             	if (members == null)
             		members = new ArrayList<String>();
             	marker = new AttendeeMarker(coordinates, markerImage, affiliation ,members, font);
             } catch (Exception e) {
            	 e.printStackTrace();
             }
		return marker;
		
	}


}