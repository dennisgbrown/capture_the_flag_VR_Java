// PolyObject.java -- PolyObject class
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class implements a polygonal object as a bunch of triangles.

package ve.types;

import java.awt.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import ve.types.Vertex;
import ve.types.Triangle;
import ve.types.ThreeVec;

public class PolyObject implements Serializable
{
  public int numVertices;       // number of vertices             
  public int numTriangles;      // number of triangles
  public Vertex vertices[];     // set of vertices used in object
  public Triangle triangles[];  // Triangles as triples of the above vertices
  public Vertex origVertices[]; // vertices asusming object is at origin

  
  // Make a new, empty PolyObject.
  public PolyObject()
  {
    numVertices = 0;
    numTriangles = 0;
  }


  // Read in object geometry from an AC3D triangle file.
  public PolyObject(String AC3DFile)
  {
    Vector inputVertices = new Vector();
    File inputFile = null;
    InputStream fileInStream = null;
    Reader fileReader = null;
    StreamTokenizer dataStream = null;
    
    // Try to open input file.
    try
      {
	inputFile = new File(AC3DFile);
	fileInStream = new FileInputStream(inputFile);
	fileReader = new BufferedReader(new InputStreamReader(fileInStream));
        dataStream = new StreamTokenizer(fileReader);
      }
    catch(NullPointerException e)
      {
	System.out.println("Error opening file " + AC3DFile + ".");
	return;
      }
    catch(java.io.IOException e)
      {
	System.out.println("IO Exception.");
	return;
      }

    // Try to read in tokens from stream.
    int typeRead;
    try 
      {
        typeRead = dataStream.nextToken();
      }
    catch(java.io.IOException e)
      {
        System.out.println("IO Exception.");
        return;
      }

    // Read in the triangles--very straightforward stuff.
    while(typeRead != dataStream.TT_EOF)
      {
	double vertices[][] = new double[3][3];
	int colorInt;
	
	numTriangles++;
	for (int i = 0; i < 3; i++)
	  {
	    for (int j = 0; j < 3; j++)
	      {
		try
		  {
		    vertices[i][j] = dataStream.nval;
		    typeRead = dataStream.nextToken();
		  }
		catch(java.io.IOException e)
		  {
		    System.out.println("IO Exception.");
		    return;
		  }
	      }
	  }
	
	try 
          {
            typeRead = dataStream.nextToken();
	    colorInt = java.lang.Integer.parseInt(dataStream.sval.substring(1),
	    					  16);
            typeRead = dataStream.nextToken();
          }
        catch(java.io.IOException e)
          {
            System.out.println("IO Exception.");
            return;
          }
	catch(NumberFormatException e2)
	  {
	    System.out.println("Can't parse number.");
	    colorInt = 453422;
	  }
	
	for (int i = 0; i < 3; i++)
	  inputVertices.addElement(new Vertex(vertices[i][0], vertices[i][1],
					      vertices[i][2],
					      new Color(colorInt)));
      }
    
    try
      {
        fileInStream.close();
      }

    catch(java.io.IOException e)
      {
        System.out.println("IO Exception.");
        return;
      }
    
    // Put data into correct format.
    numVertices = numTriangles * 3;
    origVertices = new Vertex[numVertices];
    inputVertices.copyInto(origVertices);
    vertices = new Vertex[numVertices];
    for (int j = 0; j < numVertices; j++)
      vertices[j] = new Vertex(origVertices[j]);
    triangles = new Triangle[numTriangles];
    for (int i = 0; i < numTriangles; i++)
      {
	ThreeVec normal =
	  (vertices[i*3+2].coords.subtract(vertices[i*3].coords)).cross
	  (vertices[i*3+1].coords.subtract(vertices[i*3].coords));
	normal.normalize();
	triangles[i] = new Triangle(i*3, i*3+1, i*3+2, normal);
      }
  }
  
  
  // Copy a PolyObject.
  public PolyObject(PolyObject copyMe)
  {
    numVertices = copyMe.numVertices;
    numTriangles = copyMe.numTriangles;

    vertices = new Vertex[numVertices];
    origVertices = new Vertex[numVertices];
    triangles = new Triangle[numTriangles];

    for (int i = 0; i < numVertices; i++)
      vertices[i] = new Vertex(copyMe.vertices[i]);

    for (int i = 0; i < numVertices; i++)
      origVertices[i] = new Vertex(copyMe.origVertices[i]);

    for (int i = 0; i < numTriangles; i++)
      triangles[i] = new Triangle(copyMe.triangles[i]);
  }


  // Update the normal for each triangle.
  public void updateNormals()
  {
    for (int i = 0; i < numTriangles; i++)
      {
	triangles[i].normal = 
	  (vertices[i*3+2].coords.subtract(vertices[i*3].coords)).cross(
	   vertices[i*3+1].coords.subtract(vertices[i*3].coords));
	triangles[i].normal.normalize();
      }
  }
  

  // Print out the vertices for debugging.
  public void printVertices()
  {
    System.out.println("polyobject");
    for (int i = 0; i < numVertices; i++)
      System.out.println(vertices[i].coords.x + " " +
			 vertices[i].coords.y + " " +
			 vertices[i].coords.z);
  }
}

  





