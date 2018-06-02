package es.ava.aruco;

import java.io.File;
import java.util.StringTokenizer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Size;

import es.ava.aruco.exceptions.CPException;

/**
 * Camera parameters needed to 3d rendering. They will be loaded from a file
 * in xml format generated by the OpenCV's calibration algorithm.
 * The parameters used are the camera matrix and the distorsion coefficient matrix.
 * @author Rafael Ortega
 *
 */
public class CameraParameters {

	// cameraMatrix will be of the form
	// | Fx 0  Cx |
	// | 0  Fy Cy |
	// | 0  0   1 |
	private Mat cameraMatrix;
	private MatOfDouble distorsionMatrix;
	private Size camSize;
	
	public CameraParameters(){
		cameraMatrix = new Mat(3,3,CvType.CV_32FC1);
		distorsionMatrix = new MatOfDouble();
	}
	
    /**Indicates whether this object is valid
     */
    public boolean isValid(){
    	if(cameraMatrix != null)
    		return cameraMatrix.rows()!=0 && cameraMatrix.cols()!=0  && 
    		distorsionMatrix.total() > 0;
    	else
    		return false;
    }
	
	public Mat getCameraMatrix(){
		return cameraMatrix;
	}

	public void setCameraMatrix(){
		this.cameraMatrix = new Mat( 3, 3, CvType.CV_32FC1 );
        int row = 0, col = 0;
        this.cameraMatrix.put(row ,col, 1.2519588293098975e+03, 0., 6.6684948780852471e+02, 0., 1.2519588293098975e+03 ,3.6298123112613683e+02 ,0., 0., 1.);
		//this.cameraMatrix.put(row ,col, 1.01e+03, 0., 994., 0., 573. ,370. ,0., 0., 1.);
		/*this.cameraMatrix.put(row ,col, 687.3947280788832 , 0.0 , 660.0309353676239 ,
				0.0 , 2787.2100498863965 , 360.99900846632625 ,
				0.0 , 0.0 , 1.0);*/
	}

    public void setDistCoeff(){
        //double[] coeffArray = {1.3569117181595716e-01 ,-8.2513063822554633e-01, 0. ,0.,1.6412101575010554e+00};
		double[] coeffArray = { 0. , 0., 0. ,0., 0.};
		//double[] coeffArray = { -0.53 , 2.95, 0.0257 ,-0.0795, -4.4};
		//double[] coeffArray = {-1.9763657253595714 , 5.228824955085057 , -0.051838638813433376 , 0.28558541853493635 , 93.07489571832211};
        distorsionMatrix.fromArray(coeffArray);
    }
	
	public MatOfDouble getDistCoeff(){
		return distorsionMatrix;
	}
	
	public void resize(Size size) throws CPException{
	    if (!isValid()) 
	    	throw new CPException("invalid object CameraParameters::resize");
	    if (size == camSize)
	    	return;
	    //now, read the camera size
	    //resize the camera parameters to fit this image size
	    float AxFactor= (float)(size.width)/ (float)(camSize.width);
	    float AyFactor= (float)(size.height)/ (float)(camSize.height);
		float[] current = new float[9];
	    cameraMatrix.get(0, 0, current);
		float[] buff = {current[0]*AxFactor, current[1],          current[2]*AxFactor,
				        current[3],          current[4]*AyFactor, current[5],
				        current[6],          current[7],          current[8]};
		cameraMatrix.put(0, 0, buff);
	}
	
	public void readFromXML(String filepath){		
		File file = new File(filepath);

		Configuration conf;
		try {
			conf = new XMLConfiguration(file);
			Configuration cameraConf = conf.subset("camera_matrix");
			String data = new String();
			data = cameraConf.getString("data");
			StringTokenizer st = new StringTokenizer(data);
			double[] array = new double[9];
			int i = 0;
			while(st.hasMoreElements()){
				array[i] = Double.valueOf(st.nextToken());
				i++;
			}
			cameraMatrix.put(0, 0, array[0], array[1], array[2],
								   array[3], array[4], array[5],
								   array[6], array[7], array[8]);
			// parse the distorsion matrix
			Configuration distortionConf = conf.subset("distortion_coefficients");
			String coeffData = new String();
			coeffData = distortionConf.getString("data");
			StringTokenizer std = new StringTokenizer(coeffData);
			double[] coeffArray = new double[5];
			i = 0;
			while(std.hasMoreElements()){
				coeffArray[i] = Double.valueOf(std.nextToken());
				i++;
			}
			distorsionMatrix.fromArray(coeffArray);	
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
