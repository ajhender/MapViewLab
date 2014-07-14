package ca.uwaterloo.lab4_203_03;

import java.util.ArrayList;
import java.util.List;

import mapper.InterceptPoint;
import mapper.NavigationalMap;
import android.graphics.PointF;

public class GPSCoordinator {
	 
	 private int state = 0;
	 private float minX;
	 NavigationalMap wallfinder;
	
	/* 
	 PointF pA = new PointF((float)3.5,(float)9.25);
	 PointF pB = new PointF((float)7.15,(float)9.15);
	 PointF pC = new PointF((float)11.4,(float)9.25);
	 PointF pD = new PointF((float)15.8,(float)9.35);
	 
	 PointF dA = new PointF((float)4.5,(float)9.25);
	 PointF dB = new PointF((float)8.5,(float)9.15);
	 PointF dC = new PointF((float)12.7,(float)9.25);
	 PointF dD = new PointF((float)14.6,(float)9.35);
	 
	 public List<PointF> calculatePathOne(){
	 
	 List<PointF> pathOne = new ArrayList<PointF>();
	 
	 pathOne.add(new PointF(mapView.getOriginPoint()));
	 pathOne.add(pA);
	 pathOne.add(pB);
	 pathOne.add(pC);
	 pathOne.add(pD);
	 pathOne.add(new PointF(mapView.getDestinationPoint()));
	 
	 return pathOne;
	 
	 }
	 
	 public List<PointF> calculatePathTwo(){
	 
	 List<PointF> pathTwo = new ArrayList<PointF>();
	 
	 pathTwo.add(new PointF(mapView.getOriginPoint()));
	 pathTwo.add(pB);
	 pathTwo.add(pC);
	 pathTwo.add(pD);
	 pathTwo.add(new PointF(mapView.getDestinationPoint()));
	 
	 return pathTwo;
	 
	 }
	 
	 public List<PointF> calculatePathThree(){
		 
	 List<PointF> pathThree = new ArrayList<PointF>();
	 
	 pathThree.add(new PointF(mapView.getOriginPoint()));
	 pathThree.add(pC);
	 pathThree.add(pD);
	 pathThree.add(new PointF(mapView.getDestinationPoint()));
	 
	 return pathThree;
	 
	 }
	 
	 public List<PointF> calculatePathFour(){
	 
	 List<PointF> pathFour = new ArrayList<PointF>();
	 
	 pathFour.add(new PointF(mapView.getOriginPoint()));
	 pathFour.add(pD);
	 pathFour.add(new PointF(mapView.getDestinationPoint()));
	 
	 return pathFour;
	 
	 }
	 
	 public List<PointF> calculatePathFive(){
		 
	 List<PointF> pathFive = new ArrayList<PointF>();
	 
	 pathFive.add(new PointF(mapView.getOriginPoint()));
	 pathFive.add(new PointF(mapView.getDestinationPoint()));
	 
	 return pathFive;
	 
	 }
	 
	 
	// List<InterceptPoint> walls = wallfinder.calculateIntersections(mapView.getOriginPoint(),mapView.getDestinationPoint());
	 //minX = walls.Min(p = p.X);
/*
	 if(walls[0] < dA){
		 
		 mapView.setUserPath(pathOne);
		 
		 if(state == 0){
			 //Display heading to pA
			 
			 if(/*reached general area of pA ){
				 state = 1;						 
			 }
		 }
		 else if(state == 1){
			 //Display heading to pB
			 
			 if(/*reached general area of pB ){
				 state = 2;						 
			 }
			 
		 }
		 else if(state == 2){
			 //Display heading to pC
			 
			 if(/*reached general area of pC ){
				 state = 3;						 
			 }
			 
		 }
		 else if(state == 3){
			 //Display heading to pD
			 
			 if(/*reached general area of pD ){
				 state = 4;						 
			 }
			 
		 }
		 else if(state ==4){
			 //Display heading to Destination
			 
			 if(/*reached general area of Destination ){
				 //Display toast for reaching destination						 
			 }
			 
		 }
		 
	 }
	 else if(walls[0] < dB && walls[0] > dA){
		 
		 mapView.setUserPath(pathTwo);
		 
		 if(state == 0){
			 //Display heading to pB
			 
			 if(/*reached general area of pB ){
				 state = 1;						 
			 }
		 }
		 else if(state == 1){
			 //Display heading to pC
			 
			 if(/*reached general area of pC ){
				 state = 2;						 
			 }
			 
		 }
		 else if(state == 2){
			 //Display heading to pD
			 
			 if(/*reached general area of pD ){
				 state = 3;						 
			 }
			 
		 }
		 else if(state == 3){
			 //Display heading to Destination
			 
			 if(/*reached general area of Destination ){
				 //Display toast for reaching destination						 
			 }
			 
		 }
		 
	 }
	 else if(walls[0] < dC && walls[0] > dB){
		 
		 mapView.setUserPath(pathThree);
		 
		 if(state == 0){
			 //Display heading to pC
			 
			 if(/*reached general area of pC ){
				 state = 1;						 
			 }
		 }
		 else if(state == 1){
			 //Display heading to pD
			 
			 if(/*reached general area of pD ){
				 state = 2;						 
			 }
			 
		 }

		 else if(state == 2){
			 //Display heading to Destination
			 
			 if(/*reached general area of Destination ){
				 //Display toast for reaching destination						 
			 }
			 
		 }
	 
	 }
	 else if(walls[0] < dD && walls[0] > dC){
		 
		 mapView.setUserPath(pathFour);
		 
		 if(state == 0){
			 //Display heading to pD
			 
			 if(/*reached general area of pD ){
				 state = 1;						 
			 }
		 }

		 else if(state == 1){
			 //Display heading to Destination
			 
			 if(/*reached general area of Destination ){
				 //Display toast for reaching destination						 
			 }
			 
		 }
		 
	 }
	 else{//Direct route
		 
		 mapView.setUserPath(pathFive);
		 
		 //Display heading to Destination
		 
		 if(/*reached general area of Destination ){
			 //Display toast for reaching destination						 
		 }
		 
	 }
	 */
	 

}
