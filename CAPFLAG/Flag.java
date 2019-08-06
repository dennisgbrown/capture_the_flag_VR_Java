// Flag.java -- Capture-the-flag flag object
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class makes a flag object.

package capflag;

import java.net.InetAddress;
import java.rmi.*;
import java.lang.Double;
import ve.vobject.BaseObject;
import ve.types.*;


public class Flag extends BaseObject
{
  // The constructor just calls the super constructor with the right arguments.
  public Flag(String serverName, ThreeVec initPosition,
	      ThreeVec initLookat, ThreeVec initVUP, double initNimbusRadius,
	      String initType, PolyObject initGeometry)
    throws RemoteException
  {
    super(serverName, initPosition, initLookat, initVUP, -1000000.0,
	  initNimbusRadius, initType, initGeometry, true);
  }


  // Main sets up the values for this vobject and finds out the name
  // of the Connection Manager server. Then it creates a new Flag object.
  public static void main(String args[])
  {
    // position of the vobject
    double positionx = 2.5;
    double positiony = 2.0;
    double positionz = 12.5;

    // lookat values for the vobject
    double lookatx = 5.0;
    double lookaty = 2.0;
    double lookatz = 12.5;

    // VUP values for the vobject
    double VUPx = 0.0;
    double VUPy = 1.0;
    double VUPz = 0.0;

    // default nimbus radius (remain hidden until player really close)
    double nimbusRadius = 5.0;
    
    // default geometry file name
    String geomFile = "capflag/models/flag.tri";

    // default object name (as seen by other vobjects)
    String objName = "fake flag";

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
	  if (args[argnum].equals("-geomFile"))
	    {
	      geomFile = args[argnum+1];
	      argnum += 2;
	      continue;
	    }
	  if (args[argnum].equals("-objName"))
	    {
	      objName = args[argnum+1];
	      argnum += 2;
	      continue;
	    }
	  if (args[argnum].equals("-nimbusRadius"))
	    {
	      nimbusRadius = (Double.valueOf(args[argnum+1])).doubleValue();
	      argnum += 2;
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
      PolyObject flagGeom = new PolyObject(geomFile);
      
      // Create a new Flag instance.
      Flag instanceOfMe = new Flag(serverName, position, lookat, VUP,
				   nimbusRadius, objName, flagGeom);

    } catch (Exception e) { reportErrorAndDie(e); }
  }
}

  


