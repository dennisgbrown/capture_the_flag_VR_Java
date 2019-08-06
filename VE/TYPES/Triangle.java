// Triangle.java -- Triangle class
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class implements a triangle object.

package ve.types;

import ve.types.ThreeVec;
import java.io.Serializable;

public class Triangle implements Serializable
{
  public int vertices[];    // This is an array of indices into the
			    // vertex array of the PolyObject with
			    // which this triangle is associated
  public ThreeVec normal;   // normal vector

  // Create a new triangle given the indices of three vertices and normal.
  public Triangle(int zero, int one, int two, ThreeVec normal)
  {
    vertices = new int[3];
    vertices[0] = zero;
    vertices[1] = one;
    vertices[2] = two;
    this.normal = normal;
  }
  
  // Create a copy of a triangle.
  public Triangle(Triangle copyMe)
  {
    for(int i = 0; i < 3; i++)
      vertices[i] = copyMe.vertices[i];
    normal = new ThreeVec(copyMe.normal);
  }
}



