// ThreeVec.java -- Vector class
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class implements a 3-vector and supporting methods.

package ve.types;

import java.lang.Math;
import java.io.Serializable;

public class ThreeVec implements Serializable
{
  public double x;
  public double y;
  public double z;

  // Make a new vector given x, y, z.
  public ThreeVec(double x, double y, double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  // Change a vector given x, y, z.
  public synchronized void updatexyz(double x, double y, double z)
  {
    this.x = y;
    this.y = y;
    this.z = z;
  }
  
  // Make a new vector from an old vector.
  public ThreeVec(ThreeVec copyMe)
  {
    x = copyMe.x;
    y = copyMe.y;
    z = copyMe.z;
  }

  // Return the length of the vector.
  public synchronized double length()
  {
    return java.lang.Math.sqrt((x*x) + (y*y) + (z*z));
  }

  // Normalize the vector.
  public synchronized void normalize()
  {
    double length = java.lang.Math.sqrt((x*x) + (y*y) + (z*z));
    x /= length;
    y /= length;
    z /= length;
  }

  // Return the difference between this vector and another.
  public synchronized ThreeVec subtract(ThreeVec B)
  {
    return new ThreeVec(x-B.x, y-B.y, z-B.z);
  }

  // Return the sum of this vector and another.
  public synchronized ThreeVec add(ThreeVec B)
  {
    return new ThreeVec(x+B.x, y+B.y, z+B.z);
  }

  // Return the cross product of this vector and another.
  public synchronized ThreeVec cross(ThreeVec B)
  {
    return new ThreeVec(y*B.z-z*B.y, z*B.x-x*B.z, x*B.y-y*B.x);
  }

  // Return the dot product of this vector and another.
  public synchronized double dot(ThreeVec B)
  {
    return (x*B.x + y*B.y + z*B.z);
  }

  public synchronized void printThreeVec()
  {
    System.out.println("<"+x+" "+y+" "+z+">");
  }
}







