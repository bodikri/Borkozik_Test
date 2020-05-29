/*
 * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
 * Copyright (C) 2010-2012  Andrey Novikov <http://andreynovikov.info/>
 *
 * This file is part of Androzic application.
 *
 * Androzic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Androzic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Androzic.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.borkozic.util;

public class Geo
{
	public static double distance(double lat1, double lon1, double lat2, double lon2)
	{
		// WGS-84 ellipsoid
		double a = 6378137;
		double b = 6356752.314245;
		double f = 1/298.257223563;
		double L = Math.toRadians(lon2-lon1);
		double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1);
		double cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2);
		double cosU2 = Math.cos(U2);
  
		double lambda = L, lambdaP, iterLimit = 100;
		double sigma, cosSqAlpha, sinSigma, cosSigma, cos2SigmaM;
		
		do
		{
			double sinLambda = Math.sin(lambda);
			double cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
			if (sinSigma==0) return 0;  // co-incident points
			cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha*sinAlpha;
			try
			{
				cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
			}
			catch (ArithmeticException e)
			{
				cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0
			}
			double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1-C) * f * sinAlpha * (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
		} 
		while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

		if (iterLimit==0) return -1;  // formula failed to converge

		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
		double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
		double s = b*A*(sigma-deltaSigma);
  
		return s;
	}
	/*
 * Calculate distance between two points in latitude and longitude
  * Uses Haversine method as its base.
 * lat1, lon1 Start point lat2, lon2 End point
 * @returns Distance in Meters
 */
	public static double distanceBorko(double lat1, double lat2, double lon1, double lon2)
	{// WGS-84 ellipsoid
		final int R = 6378137; // Radius of the earth in meters

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return  R * c ; // in meters

	}
	/*
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 *
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
	 * el2 End altitude in meters
	 * @returns Distance in Meters
	 */
	public static double distanceBorko(double lat1, double lat2, double lon1, double lon2, double el1, double el2)
	{// WGS-84 ellipsoid
		final int R = 6378137; // Radius of the earth in meters

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c ; // in meters

		double height = el1 - el2;

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(distance);
	}


	public static double distance(double lat1, double lon1, double lat2, double lon2, double el1, double el2)
	{
		// WGS-84 ellipsoid
		double a = 6378137;
		double b = 6356752.314245;
		double f = 1/298.257223563;
		double L = Math.toRadians(lon2-lon1);
		double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1);
		double cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2);
		double cosU2 = Math.cos(U2);

		double lambda = L, lambdaP, iterLimit = 100;
		double sigma, cosSqAlpha, sinSigma, cosSigma, cos2SigmaM;

		do
		{
			double sinLambda = Math.sin(lambda);
			double cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
			if (sinSigma==0) return 0;  // co-incident points
			cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha*sinAlpha;
			try
			{
				cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
			}
			catch (ArithmeticException e)
			{
				cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0
			}
			double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1-C) * f * sinAlpha * (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
		}
		while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

		if (iterLimit==0) return -1;  // formula failed to converge

		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
		double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
		double distance = b*A*(sigma-deltaSigma);
		double height = el1 - el2;
		//android.util.Log.d("Geo-Distance:", "H= " + height);
		if (height==0) return distance;
		double slopedistance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(slopedistance); //in meters
	}
	/*
* Calculate SlopeAngle on Route between two points in latitude and longitude taking
* into account height difference.
* lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
* el2 End altitude in meters
* This angle helps to calculate Below/Above Glide path
* @returns Slope Angle in radians
*/
	public static double SlopeAngle(double lat1, double lon1, double lat2, double lon2, double el1, double el2)
	{
		// WGS-84 ellipsoid
		double a = 6378137;
		double b = 6356752.314245;
		double f = 1/298.257223563;
		double L = Math.toRadians(lon2-lon1);
		double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1);
		double cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2);
		double cosU2 = Math.cos(U2);

		double lambda = L, lambdaP, iterLimit = 100;
		double sigma, cosSqAlpha, sinSigma, cosSigma, cos2SigmaM;

		do
		{
			double sinLambda = Math.sin(lambda);
			double cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
			if (sinSigma==0) return 0;  // co-incident points
			cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha*sinAlpha;
			try
			{
				cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
			}
			catch (ArithmeticException e)
			{
				cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0
			}
			double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1-C) * f * sinAlpha * (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
		}
		while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

		if (iterLimit==0) return -1;  // formula failed to converge

		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
		double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
		double distance = b*A*(sigma-deltaSigma);
		double height = el1 - el2;
		return Math.atan2(height, distance); // in radians;
	}
	/*
 * Calculate below/above GlidePath on Route between two points in latitude and longitude taking
 * into account height difference.  Uses Haversine method as its base.
 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
 * el2 End altitude in meters
 * lat3, lon3, el3 - current position
 * @returns below/above GlidePath in Meters
 */
	public static double belowAboveGlidePath(double lat1, double lon1, double lat2, double lon2, double el1, double el2, double SlopeAngle)
	{
		// WGS-84 ellipsoid
		double a = 6378137;
		double b = 6356752.314245;
		double f = 1/298.257223563;
		double L = Math.toRadians(lon2-lon1);
		double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1);
		double cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2);
		double cosU2 = Math.cos(U2);

		double lambda = L, lambdaP, iterLimit = 100;
		double sigma, cosSqAlpha, sinSigma, cosSigma, cos2SigmaM;

		do
		{
			double sinLambda = Math.sin(lambda);
			double cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
			if (sinSigma==0) return 0;  // co-incident points
			cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha*sinAlpha;
			try
			{
				cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
			}
			catch (ArithmeticException e)
			{
				cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0
			}
			double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1-C) * f * sinAlpha * (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
		}
		while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

		if (iterLimit==0) return -1;  // formula failed to converge

		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
		double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
		double distance = b*A*(sigma-deltaSigma);
		double neededElev = Math.tan(SlopeAngle)*distance + el2; // must be at this elevation
		return el1-neededElev;
	}
	/*
     * Calculate below/above GlidePath on Route between two points in latitude and longitude taking
     * into account height difference.  Uses Haversine method as its base.
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * lat3, lon3, el3 - current position
     * @returns below/above GlidePath in Meters
     */
	public static double needToBe(double lat1, double lon1, double lat2, double lon2, double el2, double SlopeAngle)
	{
		// WGS-84 ellipsoid
		double a = 6378137;
		double b = 6356752.314245;
		double f = 1/298.257223563;
		double L = Math.toRadians(lon2-lon1);
		double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1);
		double cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2);
		double cosU2 = Math.cos(U2);

		double lambda = L, lambdaP, iterLimit = 100;
		double sigma, cosSqAlpha, sinSigma, cosSigma, cos2SigmaM;

		do
		{
			double sinLambda = Math.sin(lambda);
			double cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
			if (sinSigma==0) return 0;  // co-incident points
			cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha*sinAlpha;
			try
			{
				cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
			}
			catch (ArithmeticException e)
			{
				cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0
			}
			double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1-C) * f * sinAlpha * (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
		}
		while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

		if (iterLimit==0) return -1;  // formula failed to converge

		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
		double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
		double distance = b*A*(sigma-deltaSigma);
		return Math.tan(SlopeAngle)* distance+el2;// must be at this elevation
	}

	public static double belowAboveGlidePath(double lat1, double lon1, double el1, double lat2, double lon2, double el2, double lat3, double lon3, double el3)
	{
		// WGS-84 ellipsoid
		final int R = 6378137; // Radius of the earth in meters

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double latDistanceCP = Math.toRadians(lat2 - lat3);
		double lonDistanceCP = Math.toRadians(lon2 - lon3);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		double aCP = Math.sin(latDistanceCP / 2) * Math.sin(latDistanceCP / 2) + Math.cos(Math.toRadians(lat3)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistanceCP / 2) * Math.sin(lonDistanceCP / 2);
		double cCP = 2 * Math.atan2(Math.sqrt(aCP), Math.sqrt(1 - aCP));

		double distance = R * c ; // in meters
		double distanceCP = R * cCP ; // in meters

		double height = el1 - el2; // in meters
		double heightCP = el3 - el2; // in meters

		//double slopedistance = Math.sqrt(Math.pow(distance, 2) + Math.pow(height, 2));

		double slopeAngle = Math.atan2(height, distance); // in radians
		double neededElev = Math.cos(slopeAngle)* distanceCP; // must be at this elevation

		return heightCP-neededElev;
	}
	/*
	 * Calculate bearing between line on two points in latitude and longitude taking
	 * @returns bearing in degree
	 */
	public static double bearing(double lat1, double lon1, double lat2, double lon2)
	{
		double deltaLong = Math.toRadians(lon2 - lon1);

		double rlat1 = Math.toRadians(lat1);
		double rlat2 = Math.toRadians(lat2);

		double y = Math.sin(deltaLong) * Math.cos(rlat2);
		double x = Math.cos(rlat1) * Math.sin(rlat2) - Math.sin(rlat1) * Math.cos(rlat2) * Math.cos(deltaLong);
		double result = Math.toDegrees(Math.atan2(y, x));
		return (result + 360.0) % 360.0;
	}
	/*
	 * Calculate bearing between line on two points in latitude and longitude taking
	 * @returns bearing in radians
	 */
	public static double bearingRad(double lat1, double lon1, double lat2, double lon2)
	{
		double deltaLong = Math.toRadians(lon2 - lon1);

		double rlat1 = Math.toRadians(lat1);
		double rlat2 = Math.toRadians(lat2);

		double y = Math.sin(deltaLong) * Math.cos(rlat2);
		double x = Math.cos(rlat1) * Math.sin(rlat2) - Math.sin(rlat1) * Math.cos(rlat2) * Math.cos(deltaLong);
		double result = Math.atan2(y, x);
		return result ;
	}
	/*
	 * Calculate specific distance on 3 points in latitude and longitude taking
	 * lat1, lon1 Start point; lat2, lon2 middle point; lat2, lon2 Last point
	 * @returns hipotenuseDistance in Meters
	 */
	public static double calculateHipDist(double lat1, double lon1, double lat2, double lon2, double lat3, double lon3, double displacement)
	{
		double bearing1 = bearingRad(lat1,lon1,lat2,lon2);
		double bearing2 = bearingRad(lat2,lon2,lat3,lon3);

		double angleBetwin2Lines = Math.PI - bearing1 -bearing2;
		double hipotenuseDistance = displacement/Math.sin(angleBetwin2Lines/2);

		return hipotenuseDistance;
	}
	
	public static double[] projection(double lat, double lon, double distance, double bearing)
	{
		// WGS-84 ellipsoid
		double a = 6378137;
		double b = 6356752.3142;
		double f = 1/298.257223563;  
		
		double s = distance;
		double alpha1 = Math.toRadians(bearing);
		double sinAlpha1 = Math.sin(alpha1);
		double cosAlpha1 = Math.cos(alpha1);
		  
		double tanU1 = (1-f) * Math.tan(Math.toRadians(lat));
		double cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1)), sinU1 = tanU1*cosU1;
		double sigma1 = Math.atan2(tanU1, cosAlpha1);
		double sinAlpha = cosU1 * sinAlpha1;
		double cosSqAlpha = 1 - sinAlpha*sinAlpha;
		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
		  
		double sigma = s / (b*A), sigmaP = 2*Math.PI;

		double sinSigma, cosSigma, cos2SigmaM, iterLimit = 100;
		
		do
		{
			cos2SigmaM = Math.cos(2*sigma1 + sigma);
			sinSigma = Math.sin(sigma);
			cosSigma = Math.cos(sigma);
			double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
		      B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
		    sigmaP = sigma;
		    sigma = s / (b*A) + deltaSigma;
		}
		while (Math.abs(sigma-sigmaP) > 1e-12 && --iterLimit>0);

		double tmp = sinU1*sinSigma - cosU1*cosSigma*cosAlpha1;
		double lat2 = Math.atan2(sinU1*cosSigma + cosU1*sinSigma*cosAlpha1, 
				(1-f)*Math.sqrt(sinAlpha*sinAlpha + tmp*tmp));
		double lambda = Math.atan2(sinSigma*sinAlpha1, cosU1*cosSigma - sinU1*sinSigma*cosAlpha1);
		double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
		double L = lambda - (1-C) * f * sinAlpha *
				(sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));

		double[] result = {Math.toDegrees(lat2), lon+Math.toDegrees(L)};
		return result;
	}


	/**
	 * Returns VMG (velocity made good)
	 * @param speed movement speed
	 * @param turn desired turn, in degrees
	 * @return VMG in speed units
	 */
	public static double vmg(double speed, double turn)
	{
		return speed * Math.cos(Math.toRadians(turn));
	}

	/**
	 * Returns XTK (off course) when navigating from point A to point B
	 * @param distance current distance to point B
	 * @param dtk desired track (course from A to B), in degrees
	 * @param bearing direction to B, in degrees
	 * @return XTK in distance units
	 */
	public static double xtk(double distance, double dtk, double bearing)
	{
		double dte = 0, dtesign = +1;
		if (bearing > dtk)
		{
			dte = bearing - dtk;
		}
		else if (bearing < dtk)
		{
			dte = dtk - bearing;
			dtesign = -1;
		}
		if (dte > 180)
		{
			dte = 360 - dte;
			dtesign *= -1;
		}
		// TODO replace with NaN
		if (dte > 90)
			return Double.NEGATIVE_INFINITY;

		return distance * Math.sin(Math.toRadians(dte)) * dtesign;
	}

	// TODO check if it can be used elsewhere
	public static double turn(double deg1, double deg2)
	{
		double deg = 0, degsign = +1;
		if (deg2 > deg1)
		{
			deg = deg2 - deg1;
		}
		else if (deg2 < deg1)
		{
			deg = deg1 - deg2;
			degsign = -1;
		}
		if (deg > 180)
		{
			deg = 360 - deg;
			degsign *= -1;
		}
		return deg * degsign;
	}

}

