// Vertex.java -- Vertex class
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class implements a vertex with four coordinates and a color.

package ve.types;

import java.awt.Color;
import java.io.Serializable;
import ve.types.ThreeVec;

public class Vertex implements Serializable
{
  public ThreeVec coords;               // x, y, z coordinate of vertex
  public Color vColor;                  // color of the vertex

  // Make a new vertex given three points and three color values.
  public Vertex(double x, double y, double z, 
		float r, float g, float b)
  {
    coords = new ThreeVec(x, y, z);
    vColor = new Color(r, g, b);
  }

  // Make a new vertex given three points and a color object.
  public Vertex(double x, double y, double z, Color rgb)
  {
    coords = new ThreeVec(x, y, z);
    vColor = rgb;
  }

  // Update x, y, and z.
  public synchronized void updatexyz(double x, double y, double z)
  {
    coords.updatexyz(x, y, z);
  }
  
  // Make a copy of a vertex.
  public Vertex(Vertex copyMe)
  {
    coords = new ThreeVec(copyMe.coords);
    vColor = new Color(copyMe.vColor.getRGB());
  }
}




