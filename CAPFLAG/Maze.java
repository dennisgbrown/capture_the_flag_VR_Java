// Maze.java -- Capture-the-flag maze object
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class reads in a maze file and makes a Maze object, a GoodPath
// object, and a BadPath object from the info in the file.

package capflag;

import java.net.InetAddress;
import java.rmi.*;
import java.lang.Double;
import java.io.*;
import ve.vobject.BaseObject;
import ve.types.*;


public class Maze extends BaseObject
{
  // The constructor just calls the super constructor with the right arguments.
  public Maze(String serverName, ThreeVec initPosition,
	      ThreeVec initLookat, ThreeVec initVUP,
	      String initType, PolyObject initGeometry)
    throws RemoteException
  {
    super(serverName, initPosition, initLookat, initVUP, -1000000,
	  100000, initType, initGeometry, true);
  }


  // Main sets up the values for this vobject and finds out the name
  // of the Connection Manager server. Then it creates new
  // Maze, goodPath, and badPath objects.
  public static void main(String args[])
  {
    // position of the vobject
    double positionx = 5.0;
    double positiony = 0.0;
    double positionz = 20.0;

    // lookat values for the vobject
    double lookatx = 5.0;
    double lookaty = 0.0;
    double lookatz = -10.0;

    // VUP values for the vobject
    double VUPx = 0.0;
    double VUPy = 1.0;
    double VUPz = 0.0;

    // default maze file name
    String mazeFile = "capflag/mazes/7x7.mz";
    
    // name of conman server to use
    String serverName = "";

    
    // Parse the arguments.
    try {
      int argnum = 0;
      while (argnum < args.length)
	{
	  if (args[argnum].equals("-serverName"))
	    {
	      serverName = args[argnum+1];
	      argnum += 2;
	      continue;
	    }
	  if (args[argnum].equals("-mazeFile"))
	    {
	      mazeFile = args[argnum+1];
	      argnum += 2;
	      continue;
	    }
	  if (args[argnum].equals("-position"))
	    {
	      positionx = (Double.valueOf(args[argnum+1])).doubleValue();
	      positiony = (Double.valueOf(args[argnum+2])).doubleValue();
	      positionz = (Double.valueOf(args[argnum+3])).doubleValue();
	      argnum += 4;
	      continue;
	    }
	  if (args[argnum].equals("-lookat"))
	    {
	      lookatx = (Double.valueOf(args[argnum+1])).doubleValue();
	      lookaty = (Double.valueOf(args[argnum+2])).doubleValue();
	      lookatz = (Double.valueOf(args[argnum+3])).doubleValue();
	      argnum += 4;
	      continue;
	    }
	  if (args[argnum].equals("-VUP"))
	    {
	      VUPx = (Double.valueOf(args[argnum+1])).doubleValue();
	      VUPy = (Double.valueOf(args[argnum+2])).doubleValue();
	      VUPz = (Double.valueOf(args[argnum+3])).doubleValue();
	      argnum += 4;
	      continue;
	    }
	  System.out.println("Can't handle this argument: " + args[argnum]);
	  argnum++;
	}

      // If no server name given, assume local host on port 1099.
      if (serverName.equals(""))
	{
	  System.err.println("No host:port given, using local host on 1099.");
	  try {
	    InetAddress add = InetAddress.getLocalHost();
	    serverName = add.getHostName() + ":1099";
	  } catch (Exception e) { reportErrorAndDie(e); }
	}

      // Set up values using given info and create a new Player object.
      ThreeVec position = new ThreeVec(positionx, positiony, positionz);
      ThreeVec lookat = new ThreeVec(lookatx, lookaty, lookatz);
      ThreeVec VUP = new ThreeVec(VUPx, VUPy, VUPz);

      // Set up a mazeFileReader to read in the maze file.
      mazeFileReader fileReader = new mazeFileReader(mazeFile);

      // Create a geometry from the maze file info.
      PolyObject mazeGeom = fileReader.makeMazeWalls();

      // Create a new Maze instance.
      Maze instanceOfMe = new Maze(serverName, position, lookat, VUP,
				   "Maze", mazeGeom);

      // Create a new GoodPath instance.
      PolyObject goodPathGeom = fileReader.makeGoodPath();
      Maze goodPath = new Maze(serverName, position, lookat, VUP,
			       "Good Path", goodPathGeom);

      // Create a new BadPath instance.
      PolyObject badPathGeom = fileReader.makeBadPath();
      Maze badPath = new Maze(serverName, position, lookat, VUP,
			      "Bad Path", badPathGeom);

      // Create a new Ceiling instance.
      PolyObject ceilingGeom = fileReader.makeCeiling();
      Maze ceiling = new Maze(serverName, position, lookat, VUP,
			      "Ceiling", ceilingGeom);
      
    } catch (Exception e) { reportErrorAndDie(e); }
  }
}

  
class mazeFileReader
{
  int sizeX = 0;                // X size in pieces/bricks/units/whatever
  int sizeY = 0;                // Y size
  char mazeData[][] = null;     // type of each piece
  int walls = 0;                // number of wall blocks
  int goodPaths = 0;            // number of good path tiles
  int badPaths = 0;             // number of bad path tiles
  char wallChar = 'B';          // character which represents a wall
  char goodPathChar = 'p';      // character which represents the good path
  char badPathChar = 'x';       // character which represents the bad path
  double xScale = 5.0;          // how big each wall is in each direction
  double yScale = 5.0;
  double zScale = 5.0;
  PolyObject mazeWalls = null;  // maze geometry
  PolyObject goodPath = null;   // good path geometry
  PolyObject badPath = null;    // bad path geometry
  PolyObject ceiling = null;    // ceiling geometry


  // Read in the mazefile data into an array. This function isn't pretty.
  public mazeFileReader(String mazeFile)
  {
    File dataFile;
    FileInputStream fileInStream;
    StreamTokenizer dataStream;
    
    try {
      dataFile = new File(mazeFile);
      fileInStream = new FileInputStream(dataFile);
      dataStream = new StreamTokenizer(fileInStream);

      int typeRead;
    
      // Try to read in tokens from stream.
      typeRead = dataStream.nextToken();

      // Read in the maze data from the file.
      while(typeRead != dataStream.TT_EOF)
	{
	  if ((dataStream.sval != null) && (dataStream.sval.equals("sizeX")))
	    {
	      typeRead = dataStream.nextToken();
	      sizeX = (int)dataStream.nval;
	    }
	  
	  if ((dataStream.sval != null) && (dataStream.sval.equals("sizeY")))
	    {
	      typeRead = dataStream.nextToken();
	      sizeY = (int)dataStream.nval;
	      mazeData = new char[sizeY][];
	    }
	  
	  if ((dataStream.sval != null) && (dataStream.sval.equals("begin")))
	    {
	      for (int i = 0; i < sizeY; i++)
		{
		  typeRead = dataStream.nextToken();
		  mazeData[i] = new char[sizeX];
		  for (int j = 0; j < sizeX; j++)
		    {
		      mazeData[i][j] = dataStream.sval.charAt(j);
		      if (mazeData[i][j] == wallChar) walls++;
		      if (mazeData[i][j] == goodPathChar) goodPaths++;
		      if (mazeData[i][j] == badPathChar) badPaths++;
		    }
		}
	    }

	  typeRead = dataStream.nextToken();
	}
    } catch (Exception e) { reportErrorAndDie(e); }
  }


  // Use the maze file data to make a PolyObject representing the maze walls.
  PolyObject makeMazeWalls()
  {
    if (mazeWalls != null) return mazeWalls;
    
    // Make a new, empty PolyObject.
    mazeWalls = new PolyObject();

    // Set up the numbers of vertices and triangles, then the arrays.
    mazeWalls.numVertices = walls * 12 * 3;
    mazeWalls.numTriangles = walls * 12;
    mazeWalls.vertices = new Vertex[mazeWalls.numVertices];
    mazeWalls.triangles = new Triangle[mazeWalls.numTriangles];

    // Create cubes for each wall piece.
    int vertNum = 0;
    int triNum = 0;
    ThreeVec normal;
    for (int i = 0; i < sizeY; i++)
      for (int j = 0; j < sizeX; j++)
	if (mazeData[i][j] == wallChar)
	  {
	    // Create each vertex for the cube.
	    Vertex a = new Vertex((double)j * xScale, 0.0,
				  (double)i * zScale,
				  0.0f, 1.0f, 1.0f);
	    Vertex b = new Vertex((double)(j+1) * xScale, 0.0,
				  (double)i * zScale,
				  0.0f, 1.0f, 1.0f);
	    Vertex c = new Vertex((double)j * xScale, 0.0,
				  (double)(i+1) * zScale,
				  0.0f, 1.0f, 1.0f);
	    Vertex d = new Vertex((double)(j+1) * xScale, 0.0,
				  (double)(i+1) * zScale,
				  0.0f, 1.0f, 1.0f);
	    Vertex e = new Vertex((double)j * xScale, yScale,
				  (double)i * zScale,
				  0.0f, 1.0f, 1.0f);
	    Vertex f = new Vertex((double)(j+1) * xScale, yScale,
				  (double)i * zScale,
				  0.0f, 1.0f, 1.0f);
	    Vertex g = new Vertex((double)j * xScale, yScale,
				  (double)(i+1) * zScale,
				  0.0f, 1.0f, 1.0f);
	    Vertex h = new Vertex((double)(j+1) * xScale, yScale,
				  (double)(i+1) * zScale,
				  0.0f, 1.0f, 1.0f);

	    // Make 12 triangles for the 6 faces of the cube.
	    mazeWalls.vertices[vertNum] = g;
	    mazeWalls.vertices[vertNum+1] = c;
	    mazeWalls.vertices[vertNum+2] = d;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = h;
	    mazeWalls.vertices[vertNum+1] = g;
	    mazeWalls.vertices[vertNum+2] = d;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = h;
	    mazeWalls.vertices[vertNum+1] = d;
	    mazeWalls.vertices[vertNum+2] = b;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = f;
	    mazeWalls.vertices[vertNum+1] = h;
	    mazeWalls.vertices[vertNum+2] = b;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = e;
	    mazeWalls.vertices[vertNum+1] = g;
	    mazeWalls.vertices[vertNum+2] = h;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = f;
	    mazeWalls.vertices[vertNum+1] = e;
	    mazeWalls.vertices[vertNum+2] = h;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = e;
	    mazeWalls.vertices[vertNum+1] = f;
	    mazeWalls.vertices[vertNum+2] = b;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = a;
	    mazeWalls.vertices[vertNum+1] = e;
	    mazeWalls.vertices[vertNum+2] = b;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = e;
	    mazeWalls.vertices[vertNum+1] = a;
	    mazeWalls.vertices[vertNum+2] = g;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = a;
	    mazeWalls.vertices[vertNum+1] = c;
	    mazeWalls.vertices[vertNum+2] = g;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = c;
	    mazeWalls.vertices[vertNum+1] = d;
	    mazeWalls.vertices[vertNum+2] = a;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    mazeWalls.vertices[vertNum] = d;
	    mazeWalls.vertices[vertNum+1] = b;
	    mazeWalls.vertices[vertNum+2] = a;
	    normal =
	      (mazeWalls.vertices[vertNum+2].coords.
	       subtract(mazeWalls.vertices[vertNum].coords)).
	      cross(mazeWalls.vertices[vertNum+1].coords.
		    subtract(mazeWalls.vertices[vertNum].coords));
	    normal.normalize();
	    mazeWalls.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;
	  }

    // Make backup copy of vertices within the PolyObject.
    mazeWalls.origVertices = new Vertex[mazeWalls.numVertices];
    for (int i = 0; i < mazeWalls.numVertices; i++)
      mazeWalls.origVertices[i] = new Vertex(mazeWalls.vertices[i]);

    return mazeWalls;
  }


  // Use the maze file data to make a PolyObject representing a path.
  PolyObject makePath(char pathChar1, char pathChar2, int numTiles,
		      double height, float red, float green, float blue)
  {
    // Make a new, empty PolyObject.
    PolyObject path = new PolyObject();

    // Set up the numbers of vertices and triangles.
    path.numVertices = numTiles * 2 * 3;
    path.numTriangles = numTiles * 2;
    path.vertices = new Vertex[path.numVertices];
    path.triangles = new Triangle[path.numTriangles];

    // Create rectangles for each path tile.
    int vertNum = 0;
    int triNum = 0;
    ThreeVec normal;
    for (int i = 0; i < sizeY; i++)
      for (int j = 0; j < sizeX; j++)
	if ((mazeData[i][j] == pathChar1) ||
	    (mazeData[i][j] == pathChar2))
	  {
	    // Make a vertex for each corner.
	    Vertex a = new Vertex((double)j * xScale, height,
				  (double)i * zScale, red, green, blue);
	    Vertex b = new Vertex((double)(j+1) * xScale, height,
				  (double)i * zScale, red, green, blue);
	    Vertex c = new Vertex((double)j * xScale, height,
				  (double)(i+1) * zScale, red, green, blue);
	    Vertex d = new Vertex((double)(j+1) * xScale, height,
				  (double)(i+1) * zScale, red, green, blue);

	    // Make two triangles with those four vertices.
	    path.vertices[vertNum] = a;
	    path.vertices[vertNum+1] = d;
	    path.vertices[vertNum+2] = b;
	    normal =
	      (path.vertices[vertNum+2].coords.
	       subtract(path.vertices[vertNum].coords)).
	      cross(path.vertices[vertNum+1].coords.
		    subtract(path.vertices[vertNum].coords));
	    normal.normalize();
	    path.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;

	    path.vertices[vertNum] = a;
	    path.vertices[vertNum+1] = c;
	    path.vertices[vertNum+2] = d;
	    normal =
	      (path.vertices[vertNum+2].coords.
	       subtract(path.vertices[vertNum].coords)).
	      cross(path.vertices[vertNum+1].coords.
		    subtract(path.vertices[vertNum].coords));
	    normal.normalize();
	    path.triangles[triNum] =
	      new Triangle(vertNum,vertNum+1,vertNum+2,normal);
	    vertNum += 3;
	    triNum++;
	  }
    
    // Make backup copy of vertices within the PolyObject.
    path.origVertices = new Vertex[path.numVertices];
    for (int i = 0; i < path.numVertices; i++)
      path.origVertices[i] = new Vertex(path.vertices[i]);
    
    return path;
  } 


  // Use the maze file data to make a PolyObject representing the good path.
  PolyObject makeGoodPath()
  {
    if (goodPath != null) return goodPath;

    return makePath(goodPathChar, goodPathChar, goodPaths, 0.0,
		    1.0f, 0.0f, 0.0f);
  }

  
  // Use the maze file data to make a PolyObject representing the bad path.
  PolyObject makeBadPath()
  {
    if (badPath != null) return badPath;

    return makePath(badPathChar, badPathChar, badPaths, 0.0,
		    1.0f, 0.0f, 0.0f);
  }


  // Use the maze file data to make a PolyObject representing the ceiling.
  PolyObject makeCeiling()
  {
    if (ceiling != null) return ceiling;

    return makePath(goodPathChar, badPathChar, goodPaths + badPaths,
		    yScale, 0.0f, 1.0f, 1.0f);
  }

  
  // reportErrorAndDie is a lazy way of dealing with fatal errors.
  protected static void reportErrorAndDie(Exception e)
  {
    System.err.println("MazeFileReader exception: " + e.getMessage());
    e.printStackTrace();
  }
}






