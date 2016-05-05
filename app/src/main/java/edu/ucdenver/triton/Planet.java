package edu.ucdenver.triton;

import java.util.Calendar;
import java.util.GregorianCalendar;
import android.graphics.PointF;
import android.util.Log;

public class Planet {

    PointF sun;
    String planetName;
    double scaleFactor;
    PointF currentLocation;
    double semiMajorAxis;   //a
    double semiMinorAxis;   //b
    double distanceScaleFactor;
    double eccentricity;
    double period;
    Calendar currentDate;

    //Perturbation constants for Jupiter, Saturn, Uranus, Neptune, & Pluto
    static final double b[] = {-0.00012452, 0.00025899, 0.00058331, -0.00041348, -0.01262724};
    static final double c[] = {0.06064060, -0.13434469, -0.97731848, 0.68346318};
    static final double s[] = {-0.35635438, 0.87320147, 0.17689245, -0.10162547};
    static final double f[] = {38.35125000, 38.35125000, 7.67025000, 7.67025000};

    //Physics variables
    private static final String planetNameArray[] = {"Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"};
    private static final double rMinArray[] = {0.307, 0.718, 0.983, 1.382, 4.951, 9.024, 18.33, 29.81, 29.656};   //AU
    private static final double rMaxArray[] = {0.467, 0.728, 1.017, 1.666, 5.455, 10.086, 20.11, 30.33, 49.319};   //AU
    private static final double eccentricityArray[] = {0.206, 0.007, 0.017, 0.093, 0.048, 0.056, 0.047, 0.009, 0.248, 0.617, 0.529, 0.967};   //Dimensonless
    private static final double periodArray[] = {0.241, 0.615, 1.0, 1.881, 11.86, 29.46, 84.01, 164.8, 248.5, 3.61, 2.76, 75.32}; //Years
    private static final double thetaArray[] = {5.1, 1.4, 1.2, 1.6, 1.2, 4.4, 1.2, 2.0, 5.6, 3.1, 3.1, 3.1};
    private static final float orientDegArray[] = {0f, 0.0f, 0f, 100f, 0f, 0f, 0f, 0f, 200f, 70f, -45f, 115f};

    final double precision = 10E-6;

    //Calculated
    //double d;   //Day 0.0 occurs at 2000 Jan 0.0 UT //Now passed in by SolarSystemView
    double N; //longitude of the ascending node
    double i; //inclination to the ecliptic (plane of the Earth's orbit)
    double w; //Longitude of perihelion
    double W; //Argument of perihelion
    double a; //semi-major axis
    double e; //eccentricity (0=circle, 0-1=ellipse, 1=parabola)
    double eDeg;
    double deltaE;
    double M; //mean anomaly (0 at perihelion; increases uniformly with time)
    double deltaM;
    double ML;
    double x0, y0, x, y;
    float scale;
    double solSize;

    public Planet(int ID, PointF sunP, float scale, double sSize) {
        this.scale = scale;
        planetName = planetNameArray[ID];
        sun = new PointF(sunP.x, sunP.y);
        period = periodArray[ID] * 365.25;    //Convert to days
        scaleFactor = scale + sSize*2;
        semiMajorAxis = (rMinArray[ID] + rMaxArray[ID]) / 2;
        Log.i("Constructor", "Sun Size: " + solSize);
        semiMinorAxis = semiMajorAxis * Math.sqrt(1 - Math.pow(eccentricity, 2));
        distanceScaleFactor = scaleFactor * semiMajorAxis * (1 - Math.pow(eccentricity, 2));
        this.eccentricity = eccentricityArray[ID];
        currentLocation = new PointF(0.0f,0.0f);
        currentDate = GregorianCalendar.getInstance();

    }

    //Ecliptic latitude/longitude based on time
    public void calculatePosition(double d) {
        //computeEffectiveDateTime();
        //Degrees
        double E, E0, E1, xv, yv, v, r;
        switch(this.planetName) {
            case "Mercury":
                N = 48.3313 + 3.24587E-5 * d;
                i = 7.0047 + 5.00E-8 * d;
                w = 29.1241 + 1.01444E-5 * d;
                a = 0.387098;
                e = 0.205635 + 5.59E-10 * d;
                M = 168.6562 + 4.0923344368 * d;
                break;
            case "Venus":
                N =  76.6799 + 2.46590E-5 * d;
                i = 3.3946 + 2.75E-8 * d;
                w =  54.8910 + 1.38374E-5 * d;
                a = 0.723330;
                e = 0.006773 - 1.302E-9 * d;
                M =  48.0052 + 1.6021302244 * d;
                break;
            case "Earth":
                N = 0.0;
                i = 0.0;
                w = 282.9404 + 4.70935E-5 * d;
                a = 1.000000;  //AU
                e = 0.016709 - 1.151E-9 * d;
                M = 356.0470 + 0.9856002585 * d;
                break;
            case "Mars":
                N =  49.5574 + 2.11081E-5 * d;
                i = 1.8497 - 1.78E-8 * d;
                w = 286.5016 + 2.92961E-5 * d;
                a = 1.523688;
                e = 0.093405 + 2.516E-9 * d;
                M =  18.6021 + 0.5240207766 * d;
                break;
            case "Jupiter":
                N = 100.4542 + 2.76854E-5 * d;
                i = 1.3030 - 1.557E-7 * d;
                w = 273.8777 + 1.64505E-5 * d;
                a = 5.20256;
                e = 0.048498 + 4.469E-9 * d;
                M =  19.8950 + 0.0830853001 * d;
                break;
            case "Saturn":
                N = 113.6634 + 2.38980E-5 * d;
                i = 2.4886 - 1.081E-7 * d;
                w = 339.3939 + 2.97661E-5 * d;
                a = 9.55475;
                e = 0.055546 - 9.499E-9 * d;
                M = 316.9670 + 0.0334442282 * d;
                break;
            case "Uranus":
                N =  74.0005 + 1.3978E-5 * d;
                i = 0.7733 + 1.9E-8 * d;
                w = 96.6612 + 3.0565E-5 * d;
                a = 19.18171 - 1.55E-8 * d;
                e = 0.047318 + 7.45E-9 * d;
                M = 142.5905 + 0.011725806 * d;
                break;
            case "Neptune":
                N = 131.7806 + 3.0173E-5 * d;
                i = 1.7700 - 2.55E-7 * d;
                w = 272.8461 - 6.027E-6 * d;
                a = 30.05826 + 3.313E-8 * d;
                e = 0.008606 + 2.15E-9 * d;
                M = 260.2471 + 0.005995147 * d;
                break;
            default:
                throw new RuntimeException("Invalid planet name!");
        }
        while (M > 360.0) {
            M = M - 360;
        }
        while (M < 0.0) {
            M = M + 360;
        }
        E = M + e*(180 / Math.PI) * Math.sin(Math.toRadians(M)) * (1.0 + e * Math.cos(Math.toRadians(M)));
        if(e > 0.06) {
            E0 = E;
            E1 = 0.0;
            for (int i = 0; i < 50; ++i) {
                E1 = E0 - ( E0 - e*(180/Math.PI) * Math.sin(Math.toRadians(E0)) - M) / ( 1 - e * Math.cos(Math.toRadians(E0)));
            }
            E = E1;
        }

        xv = a * (Math.cos(Math.toRadians(E)) - e); //r * Math.cos(v)
        yv =  a * (Math.sqrt(1.0 - e*e) * Math.sin(Math.toRadians(E))); //r * sin(v);
        v = Math.atan2(yv, xv);
        r = Math.sqrt(xv*xv + yv*yv)*scale;
        if (planetName.equals("Earth")) {
            currentLocation.x = (float) -(r * (Math.cos(Math.toRadians(N)) * Math.cos(v + Math.toRadians(w)) - Math.sin(Math.toRadians(N)) * Math.sin(v + Math.toRadians(w)) * Math.cos(Math.toRadians(i))));
            currentLocation.y = (float) -(r * (Math.sin(Math.toRadians(N)) * Math.cos(v + Math.toRadians(w)) + Math.cos(Math.toRadians(N)) * Math.sin(v + Math.toRadians(w)) * Math.cos(Math.toRadians(i))));
        }
        else {
            currentLocation.x = (float) (r * (Math.cos(Math.toRadians(N)) * Math.cos(v + Math.toRadians(w)) - Math.sin(Math.toRadians(N)) * Math.sin(v + Math.toRadians(w)) * Math.cos(Math.toRadians(i))));
            currentLocation.y = (float) (r * (Math.sin(Math.toRadians(N)) * Math.cos(v + Math.toRadians(w)) + Math.cos(Math.toRadians(N)) * Math.sin(v + Math.toRadians(w)) * Math.cos(Math.toRadians(i))));
        }
        currentLocation.x = sun.x + (float) (currentLocation.x * distanceScaleFactor);
        currentLocation.y = sun.y - (float) (currentLocation.y * distanceScaleFactor);
    }
    public PointF getCurrentLocation() {
        return currentLocation;
    }

    public String  getName() {
        return planetName;
    }

    public double getPeriod() {
        return period;
    }

    public void setScale(float s) {
        scale = s;
    }

    public double getScale(){
        return scale;
    }
}