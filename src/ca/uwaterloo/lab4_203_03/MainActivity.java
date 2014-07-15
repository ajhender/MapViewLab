package ca.uwaterloo.lab4_203_03;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.lab4_203_03.MainActivity.PlaceholderFragment.GPSCoordinator;
import mapper.InterceptPoint;
import mapper.MapLoader;
import mapper.MapView;
import mapper.NavigationalMap;
import mapper.PositionListener;
import mapper.VectorUtils;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	//Initializing class wide variables
	static Button clearBtn;
	static MapView mapView;
	static PositionListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Initializing mapView with the dimensions of the map
		mapView = new MapView(getApplicationContext(), 400, 300 ,20, 20);
		
		//Registering the Context Menu for mapView
		registerForContextMenu(mapView);
		
			
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//Creating the Context Menu for mapView
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		mapView.onCreateContextMenu(menu, v, menuInfo);
	}
	
	//Adding an item to the Context Menu
	@Override
	public boolean onContextItemSelected(MenuItem item){
		return super.onContextItemSelected(item) || mapView.onContextItemSelected(item);
	}
	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements PositionListener {

		public PlaceholderFragment() {
		}
 
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			//Initializing the view where widgets will be displayed
			LinearLayout lmain = (LinearLayout) rootView.findViewById(R.id.mainLayout);
			lmain.setOrientation(LinearLayout.VERTICAL);
			
			mapView.addListener(this);
			
			

			//Initializing button to clear step count
			clearBtn = new Button(rootView.getContext());	
			clearBtn.setText("Clear");
			clearBtn.setTextSize(16);
			
			//Creating onClickListener to reset the step count, N/S displacement, and E/W displacement when the button is clicked
			clearBtn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					OrientationEventListener.steps = 0;
					OrientationEventListener.xCoord = 0;
					OrientationEventListener.yCoord = 0;
				}		

			});
			
			//Initializing the variable dir with the directory pointing to the .SVG file
			final File dir = new File("/sdcard/Android/data/ca.uwaterloo.Lab3_203_03/files");
			 
			//Loading the map from the directory and adding it to the mainlayout
			NavigationalMap map = MapLoader.loadMap( dir, "Lab-room-peninsula.svg");
			mapView.setMap(map);
			lmain.addView(mapView);
			
			//Creating a textview to display a title for the data and adding it to mainlayout 
			TextView tA = new TextView(rootView.getContext());
			tA.setText("\nDead Reckoning---------");
			lmain.addView(tA);

			//Creating a textview to display the data from the Orientation class 
			TextView tvA = new TextView(rootView.getContext());
			lmain.addView(tvA);	
			
			TextView tvC = new TextView(rootView.getContext());
			lmain.addView(tvC);	

			//Adding button to the mainlayout
			lmain.addView(clearBtn);

			//Creating a sensormanager object	
			SensorManager sensorManager = (SensorManager)
					rootView.getContext().getSystemService(SENSOR_SERVICE);

			//Creating the variables needed for each required sensor
			Sensor linAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			Sensor magFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			

			//Creating a sensoreventlistener for the required sensors
			OrientationEventListener orientationField = new OrientationEventListener(tvA);
			sensorManager.registerListener(orientationField, linAccelSensor, SensorManager.SENSOR_DELAY_FASTEST);
			sensorManager.registerListener(orientationField, magFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
			sensorManager.registerListener(orientationField, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);

			GPSCoordinator directions = new GPSCoordinator(tvC);
			//lmain.addView(OrientationEventListener.myCompass);
	
			return rootView;

		}
		
		//The Orientation class that will calculate steps and displacement
		static class OrientationEventListener implements SensorEventListener { 
			//Declaring the variables required in the class
			TextView output;
			private float[] accelVal = new float[3];
			private float[] smoothAccelVal = new float[3];
			private float[] magVal = new float[3];	
			private float[] smoothMagVal = new float [3];
			private float[] orientationVal = new float[3];
			private float[] RMatrix;
			private float[] IMatrix;
			private float smoothedAccelX = 0;
			private float smoothedAccelY = 0;
			private float smoothedAccelZ = 0;
			private float azimuth;
			private List<PointF> drawPath;
			private static PointF userPath;
			private float state = 0;
			private double heading = 0;
			private GPSCoordinator update;
			final float stepDistance = 1;
			static float steps = 0;
			static float xCoord;
			static float yCoord;

			//Constructor of Orientation class
			public OrientationEventListener(TextView outputView){
				output = outputView;
			}

			public void onAccuracyChanged(Sensor s, int i) {}

			//Method that will calculate steps and displacement based on when the sensor changes
			public void onSensorChanged(SensorEvent se){

			//Switch statement determining when each sensor changes
			switch (se.sensor.getType()){
				
				//Case for when the Linear Acceleration sensor changes
				case Sensor.TYPE_LINEAR_ACCELERATION:
					final float x = se.values[0];
					final float y = se.values[1];
					final float z = se.values[2];
					
			
					//Low Pass Filter smoothing the noise from the raw sensor data
					smoothedAccelX += (x - smoothedAccelX)/5;
					smoothedAccelY += (y - smoothedAccelY)/5;
					smoothedAccelZ += (z - smoothedAccelZ)/5;
					
					
					//Finite State Machine calculating when a step occurs
					if((smoothedAccelY > 0.7) && (state ==0) && Math.abs(smoothedAccelX) < 2) {
						state = 1;
					}

					else if((smoothedAccelZ > 1) && (state == 1)) {
						state = 2;

					}
					else if((state == 2) && (smoothedAccelZ < -0.5)){
						state = 3;

					}
					//Final state of the FSM that calculates steps and the displacement values
					else if(state == 3){
						steps++;
						
						xCoord =  (float) (mapView.getUserPoint().x + Math.sin(azimuth));
						yCoord =  (float) (mapView.getUserPoint().y - Math.cos(azimuth));
						
						mapView.setUserPoint(xCoord, yCoord);
						userPath = new PointF(xCoord, yCoord);
						//GPSCoordinator setPath = new GPSCoordinator(userPath);
						//mapView.setUserPath(drawPath);
						update.showDirection();
		
						state = 0;

					}

					break;

				//Case for when the Magnetic Field sensor changes
				case Sensor.TYPE_MAGNETIC_FIELD:
					//Array of magnetic field values
					magVal[0] = se.values[0];
					magVal[1] = se.values[1];
					magVal[2] = se.values[2];
					
					//Array of smoothed magnetic field values
					smoothMagVal[0] += (magVal[0] - smoothMagVal[0])/30;
					smoothMagVal[1] += (magVal[1] - smoothMagVal[1])/30;
					smoothMagVal[2] += (magVal[2] - smoothMagVal[2])/30;
					
					break;
				
				//Case for when the Accelerometer sensor changes
				case Sensor.TYPE_ACCELEROMETER:
					//Array of accelerometer values
					accelVal[0] = se.values[0];
					accelVal[1] = se.values[1];
					accelVal[2] = se.values[2];
					
					//Array of smoothed accelerometer values
					smoothAccelVal[0] += (accelVal[0] - smoothAccelVal[0])/30;
					smoothAccelVal[1] += (accelVal[1] - smoothAccelVal[1])/30;
					smoothAccelVal[2] += (accelVal[2] - smoothAccelVal[2])/30;
					
					break;
				}

			//Determining the Orientation of the device------------
				//Making sure the phone is receiving necessary data for orientation
				if(smoothAccelVal != null && smoothMagVal != null){
					RMatrix = new float[9];
					IMatrix = new float[9];

					SensorManager.getRotationMatrix(RMatrix, IMatrix, smoothAccelVal, smoothMagVal);// Retrieve RMatrix, necessary for the getOrientation method
					SensorManager.getOrientation(RMatrix, orientationVal);// Get the current orientation of the device
			
				}
				
				azimuth = orientationVal[0] + (float) Math.toRadians(21);
				
				//Converting the orientation values into user friendly values between 0 & 2Pi
				heading = (double)Math.round(azimuth * 100) / 100;
				if (heading < 0){
				heading = heading + 2 * (Math.PI);
				}
				
				//Outputting the final values in the proper format
				output.setText(String.format("Step Counter------- \n Steps = %f\n  \n Orientation------- \n North/South(y) = %f \n East/West(x) = %f \n Heading = %f \n X= %f \n Y= %f ",
						+												steps, xCoord, yCoord, heading, mapView.getUserPoint().x, mapView.getUserPoint().y));
				
			}
			

		}
		
		public static class GPSCoordinator {

			private static TextView output;
			private static int state = 0;
			static float distance;
			static float angle;
			static float rads;
			
			static PointF pA = new PointF((float) 3.5, (float) 9.25);
			static PointF pB = new PointF((float) 7.15, (float) 9.15);
			static PointF pC = new PointF((float) 11.4, (float) 9.25);
			static PointF pD = new PointF((float) 15.8, (float) 9.35);

			static PointF dA = new PointF((float) 4.5, (float) 9.25);
			static PointF dB = new PointF((float) 8.5, (float) 9.15);
			static PointF dC = new PointF((float) 12.7, (float) 9.25);
			static PointF dD = new PointF((float) 14.6, (float) 9.35);
			
			public GPSCoordinator(TextView outputView) {
				output = outputView;
			}

			public GPSCoordinator() {
				// TODO Auto-generated constructor stub
			}

			public static List<PointF> calculatePathOne() {

				List<PointF> pathOne = new ArrayList<PointF>();

				pathOne.add(mapView.getUserPoint());
				pathOne.add(pA);
				pathOne.add(pB);
				pathOne.add(pC);
				pathOne.add(pD);
				pathOne.add(mapView.getDestinationPoint());

				return pathOne;

			}

			public List<PointF> calculatePathTwo() {

				List<PointF> pathTwo = new ArrayList<PointF>();

				pathTwo.add(mapView.getOriginPoint());
				pathTwo.add(pB);
				pathTwo.add(pC);
				pathTwo.add(pD);
				pathTwo.add(mapView.getDestinationPoint());

				return pathTwo;

			}

			public List<PointF> calculatePathThree() {

				List<PointF> pathThree = new ArrayList<PointF>();

				pathThree.add(mapView.getOriginPoint());
				pathThree.add(pC);
				pathThree.add(pD);
				pathThree.add(mapView.getDestinationPoint());

				return pathThree;

			}

			public List<PointF> calculatePathFour() {

				List<PointF> pathFour = new ArrayList<PointF>();

				pathFour.add(mapView.getOriginPoint());
				pathFour.add(pD);
				pathFour.add(mapView.getDestinationPoint());

				return pathFour;

			}

			public List<PointF> calculatePathFive() {

				List<PointF> pathFive = new ArrayList<PointF>();

				pathFive.add(mapView.getOriginPoint());
				pathFive.add(mapView.getDestinationPoint());

				return pathFive;

			}
			
			public List<PointF> calculatePathSix() {

				List<PointF> pathSix = new ArrayList<PointF>();

				pathSix.add(mapView.getOriginPoint());
				pathSix.add(pA);
				pathSix.add(pB);
				pathSix.add(pC);
				pathSix.add(mapView.getDestinationPoint());

				return pathSix;

			}
			
			public List<PointF> calculatePathSeven() {

				List<PointF> pathSeven = new ArrayList<PointF>();

				pathSeven.add(mapView.getOriginPoint());
				pathSeven.add(pA);
				pathSeven.add(pB);
				pathSeven.add(mapView.getDestinationPoint());

				return pathSeven;

			}
			
			public List<PointF> calculatePathEight() {

				List<PointF> pathEight = new ArrayList<PointF>();

				pathEight.add(mapView.getOriginPoint());
				pathEight.add(pB);
				pathEight.add(pC);
				pathEight.add(mapView.getDestinationPoint());

				return pathEight;

			}
			
			public List<PointF> calculatePathNine() {

				List<PointF> pathNine = new ArrayList<PointF>();

				pathNine.add(mapView.getDestinationPoint());
				pathNine.add(pB);
				pathNine.add(pC);
				pathNine.add(mapView.getOriginPoint());

				return pathNine;

			}
			
			public List<PointF> calculatePathTen() {

				List<PointF> pathTen = new ArrayList<PointF>();

				pathTen.add(mapView.getDestinationPoint());
				pathTen.add(pA);
				pathTen.add(pB);
				pathTen.add(mapView.getOriginPoint());

				return pathTen;

			}
			
			public List<PointF> calculatePathEleven() {

				List<PointF> pathEleven = new ArrayList<PointF>();

				pathEleven.add(mapView.getDestinationPoint());
				pathEleven.add(pA);
				pathEleven.add(pB);
				pathEleven.add(pC);
				pathEleven.add(mapView.getOriginPoint());

				return pathEleven;

			}
			
			public List<PointF> calculatePathTwelve() {

				List<PointF> pathTwelve = new ArrayList<PointF>();

				pathTwelve.add(mapView.getDestinationPoint());
				pathTwelve.add(pD);
				pathTwelve.add(mapView.getOriginPoint());

				return pathTwelve;

			}
			
			public List<PointF> calculatePathThirteen() {

				List<PointF> pathThirteen = new ArrayList<PointF>();

				pathThirteen.add(mapView.getDestinationPoint());
				pathThirteen.add(pC);
				pathThirteen.add(pD);
				pathThirteen.add(mapView.getOriginPoint());

				return pathThirteen;

			}
			
			public List<PointF> calculatePathFourteen() {

				List<PointF> pathFourteen = new ArrayList<PointF>();

				pathFourteen.add(mapView.getDestinationPoint());
				pathFourteen.add(pB);
				pathFourteen.add(pC);
				pathFourteen.add(pD);
				pathFourteen.add(mapView.getOriginPoint());

				return pathFourteen;

			}
			
			public static List<PointF> calculatePathFifteen() {

				List<PointF> pathFifteen = new ArrayList<PointF>();

				pathFifteen.add(mapView.getDestinationPoint());
				pathFifteen.add(pA);
				pathFifteen.add(pB);
				pathFifteen.add(pC);
				pathFifteen.add(pD);
				pathFifteen.add(mapView.getOriginPoint());

				return pathFifteen;

			}
			
			public List<PointF> calculatePathSixteen() {

				List<PointF> pathSixteen = new ArrayList<PointF>();

				pathSixteen.add(mapView.getOriginPoint());
				pathSixteen.add(pB);
				pathSixteen.add(mapView.getDestinationPoint());

				return pathSixteen;

			}
			
			public List<PointF> calculatePathSeventeen() {

				List<PointF> pathSeventeen = new ArrayList<PointF>();

				pathSeventeen.add(mapView.getOriginPoint());
				pathSeventeen.add(pC);
				pathSeventeen.add(mapView.getDestinationPoint());

				return pathSeventeen;

			}


			
			public void showDirection() {
				
				
				if( mapView.getOriginPoint().x < dA.x && mapView.getDestinationPoint().x > dD.x){
					 mapView.setUserPath(calculatePathOne());
					 calcDirectionFour(pA, pB, pC, pD); 
				 }
				 else if(mapView.getOriginPoint().x < dB.x && mapView.getOriginPoint().x > dA.x && mapView.getDestinationPoint().x > dD.x){
					 mapView.setUserPath(calculatePathTwo());
					 calcDirectionThree(pB, pC, pD);
				 }
				 else if(mapView.getOriginPoint().x < dC.x && mapView.getOriginPoint().x > dB.x && mapView.getDestinationPoint().x > dD.x){
					 mapView.setUserPath(calculatePathThree());
					 calcDirectionTwo(pC, pD);
				 }
				 else if(mapView.getOriginPoint().x < dD.x && mapView.getOriginPoint().x > dC.x && mapView.getDestinationPoint().x > dD.x){
					 mapView.setUserPath(calculatePathFour());
					 calcDirectionOne(pD);					 
				 }
				 else if((mapView.getOriginPoint().x > dD.x && mapView.getDestinationPoint().x > dD.x) 
						 || (mapView.getOriginPoint().x < dA.x && mapView.getDestinationPoint().x < dA.x)){
					 mapView.setUserPath(calculatePathFive());
					 calcDirectionZero();
				 }
				 else if( mapView.getOriginPoint().x < dA.x && mapView.getDestinationPoint().x < dC.x && mapView.getDestinationPoint().x > dB.x){
					 mapView.setUserPath(calculatePathSix());
					 calcDirectionThree(pA, pB, pC);
				 }
				 else if( mapView.getOriginPoint().x < dA.x && mapView.getDestinationPoint().x < dB.x && mapView.getDestinationPoint().x > dA.x){
					 mapView.setUserPath(calculatePathSeven());	
					 calcDirectionTwo(pA, pB);
				 }
				 else if(mapView.getOriginPoint().x > dA.x && mapView.getOriginPoint().x <dB.x && mapView.getDestinationPoint().x < dC.x && mapView.getDestinationPoint().x > dB.x){
					 mapView.setUserPath(calculatePathEight());	
					 calcDirectionTwo(pB, pC);
				 }
				 else if(mapView.getDestinationPoint().x > dA.x && mapView.getDestinationPoint().x <dB.x && mapView.getOriginPoint().x < dC.x && mapView.getOriginPoint().x > dB.x){
					 mapView.setUserPath(calculatePathNine());
					 calcDirectionTwo(pC, pB);
				 }
				 else if( mapView.getDestinationPoint().x < dA.x && mapView.getOriginPoint().x < dB.x && mapView.getOriginPoint().x > dA.x){
					 mapView.setUserPath(calculatePathTen());	
					 calcDirectionTwo(pB, pA);
				 }
				 else if( mapView.getDestinationPoint().x < dA.x && mapView.getOriginPoint().x < dC.x && mapView.getOriginPoint().x > dB.x){
					 mapView.setUserPath(calculatePathEleven());	
					 calcDirectionThree(pC, pB, pA);
				 }
				 else if(mapView.getDestinationPoint().x < dD.x && mapView.getDestinationPoint().x > dC.x && mapView.getOriginPoint().x > dD.x){
					 mapView.setUserPath(calculatePathTwelve());
					 calcDirectionOne(pD);	
				 }
				 else if(mapView.getDestinationPoint().x < dC.x && mapView.getDestinationPoint().x > dB.x && mapView.getOriginPoint().x > dD.x){
					 mapView.setUserPath(calculatePathThirteen());
					 calcDirectionTwo(pD, pC);
				 }
				 else if(mapView.getDestinationPoint().x < dB.x && mapView.getDestinationPoint().x > dA.x && mapView.getOriginPoint().x > dD.x){
					 mapView.setUserPath(calculatePathFourteen());
					 calcDirectionThree(pD, pC, pB);
				 }
				 else if( mapView.getDestinationPoint().x < dA.x && mapView.getOriginPoint().x > dD.x){
					 mapView.setUserPath(calculatePathFifteen());
					 calcDirectionFour(pD, pC, pB, pA);
				 }
				 else if( mapView.getDestinationPoint().x > dA.x && mapView.getDestinationPoint().x < dB.x && mapView.getOriginPoint().x > dA.x && mapView.getOriginPoint().x < dB.x){
					 mapView.setUserPath(calculatePathSixteen());
					 calcDirectionOne(pB);	
				 }
				 else if( mapView.getDestinationPoint().x > dB.x && mapView.getDestinationPoint().x < dC.x && mapView.getOriginPoint().x > dB.x && mapView.getOriginPoint().x < dC.x){
					 mapView.setUserPath(calculatePathSeventeen());
					 calcDirectionOne(pC);	
				 }
				
			}

			private static void calcDirectionFour(PointF pA, PointF pB, PointF pC, PointF pD) {
				
				if(state == 0){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pA);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), mapView.getOriginPoint(), pA);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pA.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pA.y)){
						state = 1;
					}
					
				}
				else if(state == 1){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pB);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pA, pB);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pB.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pB.y)){
						state = 2;
					}
					
				}
				else if(state == 2){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pC);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pB, pC);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pC.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pC.y)){
						state = 3;
					}
					
				}
				
				else if(state == 3){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pD);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pC, pD);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pD.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pD.y)){
						state = 4;
					}
					
				}
				
				else if(state == 4){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), mapView.getDestinationPoint());
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pD, mapView.getDestinationPoint());
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}			
					
					
				}
				
				
			}
			
			private static void calcDirectionThree(PointF pA, PointF pB, PointF pC) {
				
				if(state == 0){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pA);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), mapView.getOriginPoint(), pA);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pA.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pA.y)){
						state = 1;
					}
					
				}
				else if(state == 1){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pB);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pA, pB);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pB.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pB.y)){
						state = 2;
					}
					
				}
				else if(state == 2){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pC);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pB, pC);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pC.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pC.y)){
						state = 3;
					}
					
				}
				
				else if(state == 3){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), mapView.getDestinationPoint());
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pC, mapView.getDestinationPoint());
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}			
					
					
				}
				
				
			}
			
			private static void calcDirectionTwo(PointF pA, PointF pB) {
				
				if(state == 0){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pA);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), mapView.getOriginPoint(), pA);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pA.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pA.y)){
						state = 1;
					}
					
				}
				else if(state == 1){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pB);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pA, pB);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pB.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pB.y)){
						state = 2;
					}
					
				}				
				else if(state == 2){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), mapView.getDestinationPoint());
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pB, mapView.getDestinationPoint());
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}			
					
					
				}
				
				
			}
			
			private static void calcDirectionOne(PointF pA) {
				
				if(state == 0){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), pA);
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), mapView.getOriginPoint(), pA);
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}					
					
					if(Math.round((float) mapView.getUserPoint().x) == Math.round((float)pA.x) && Math.round((float) mapView.getUserPoint().y) == Math.round((float)pA.y)){
						state = 1;
					}
					
				}
						
				else if(state == 1){
					
					distance = VectorUtils.distance(mapView.getUserPoint(), mapView.getDestinationPoint());
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), pA, mapView.getDestinationPoint());
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}			
					
					
				}
				
				
			}
			
			private static void calcDirectionZero() {
				
					
					distance = VectorUtils.distance(mapView.getUserPoint(), mapView.getDestinationPoint());
					rads = VectorUtils.angleBetween(mapView.getUserPoint(), mapView.getOriginPoint(), mapView.getDestinationPoint());
					
					angle = (float)Math.round(rads * 100) / 100;
					if (angle < 0){
					angle = (float) (angle + 2 * (Math.PI));
					}
				
			}
			
			
		}

		@Override
		public void originChanged(MapView source, PointF loc) {
			source.setUserPoint(loc);
			
			if(!(mapView.getDestinationPoint() == null)){
			GPSCoordinator display = new GPSCoordinator();
			display.showDirection();
			}
			
		}

		@Override
		public void destinationChanged(MapView source, PointF dest) {
			
			GPSCoordinator display = new GPSCoordinator();
			display.showDirection();
			
			
			
		}
	
		
		

	}
	
	
}
