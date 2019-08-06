// Player.java -- Capture-the-flag player object
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class provides the necessary values to instantiate a
// UserObject which can let a user join a Capture-the-flag session.

package capflag;

import java.net.InetAddress;
import java.rmi.*;
import java.lang.Double;
import ve.vobject.UserObject;
import ve.types.*;


public class Player extends UserObject
{
  // Keep the following values around so we can bounce back off of
  // solid object we hit.
  double undoMoveAlongDOV = 0.0;
  double undoChangeYaw = 0.0;
  double undoChangePitch = 0.0;
  double undoChangeRoll = 0.0;
  int lastOp = -1;

  
  // The constructor just calls the super constructor with the right arguments.
  public Player(String serverName, ThreeVec initPosition,
		ThreeVec initLookat, ThreeVec initVUP,
		double initFocusRadius, double initNimbusRadius,
		String initType, PolyObject initGeometry)
    throws RemoteException
  {
    super(serverName, initPosition, initLookat, initVUP, initFocusRadius,
	  initNimbusRadius, initType, initGeometry, 0, 0, 500, 500, true);
  }


  // Main sets up the values for this vobject and finds out the name
  // of the Connection Manager server. Then it creates a new
  // Player instance.
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

    // focus and nimbus radii
    double focusRadius = 10.0;
    double nimbusRadius = 10.0;

    // default geometry file name
    String geomFile = "capflag/models/playerred.tri";

    // default object name (as seen by other vobjects)
    String objName = "Player";

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
	  if (args[argnum].equals("-focusRadius"))
	    {
	      focusRadius = (Double.valueOf(args[argnum+1])).doubleValue();
	      argnum += 2;
	      continue;
	    }
	  if (args[argnum].equals("-nimbusRadius"))
	    {
	      nimbusRadius = (Double.valueOf(args[argnum+1])).doubleValue();
	      argnum += 2;
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
      PolyObject geometry = new PolyObject(geomFile);
      
      Player instanceOfMe = new Player(serverName, position, lookat,
				       VUP, focusRadius,
				       nimbusRadius,
				       objName, geometry);
    } catch (Exception e) { reportErrorAndDie(e); }
  }


  // Augment collide of UserObject by making sure we can't walk
  // through walls.
  protected synchronized void collide()
  {
    if (checkCollisions == false) return;

    super.collide();
    for (int i = 0; i < maxNumObjs; i++)
      if ((collisions[i] == true) && (collisionTimes[i] == 0))
	{
	  String objName = localCopies[i].getType();
	  System.out.println("You just hit " + objName);

	  // If we just hit something solid, bounce back the same way
	  // we ran into it (not physically accurate).
	  if ((objName.equals("Maze")) || (objName.equals("Good Path")) ||
	      (objName.equals("Bad Path")) || (objName.equals("Ceiling")))
	    {
	      System.out.println("Just hit a solid object; bouncing back.");
	      try {
		switch(lastOp)
		  {
		  case 0:
		    {
		      moveAlongDOV(undoMoveAlongDOV);
		      break;
		    }
		  case 1:
		    {
		      changeYaw(undoChangeYaw);
		      break;
		    }
		  case 2:
		    {
		      changePitch(undoChangePitch);
		      break;
		    }
		  case 3:
		    {
		      changeRoll(undoChangeRoll);
		      break;
		    }
		  }
	      } catch (Exception e) { reportErrorAndDie(e); }
	    }

	  // If we hit the wrong flag, die.
	  if (objName.equals("Wrong Flag"))
	    {
	      System.out.println("\n\n\n\n\n###############################");
	      System.out.println("BOOM! You're dead! You choose the evil ");
	      System.out.println("exploding flag! Bye bye! ");
	      System.out.println("###############################\n\n\n\n\n");
	      removeFromConMan();
	      System.exit(0);
	    }

	  // If we hit the right flag, still die.
	  if (objName.equals("Right Flag"))
	    {
	      System.out.println("\n\n\n\n\n###############################");
	      System.out.println("YAY! You picked the right flag. You won.");
	      System.out.println("Game over!");
	      System.out.println("###############################\n\n\n\n\n");
	      removeFromConMan();
	      System.exit(0);
	    }
	}
  }


  // Augment moveAlongDOV to remember how to backtrack.
  public void moveAlongDOV(double numUnits) throws RemoteException
  {
    super.moveAlongDOV(numUnits);
    undoMoveAlongDOV = -numUnits;
    lastOp = 0;
  }

  
  // Augment changeYaw to remember how to backtrack.
  public void changeYaw(double angle) throws RemoteException
  {
    super.changeYaw(angle);
    undoChangeYaw = -angle;
    lastOp = 1;
  }

  
  // Augment changePitch to remember how to backtrack.
  public void changePitch(double angle) throws RemoteException
  {
    super.changePitch(angle);
    undoChangePitch = -angle;
    lastOp = 2;
  }

  
  // Augment changeRoll to remember how to backtrack.
  public void changeRoll(double angle) throws RemoteException
  {
    super.changeRoll(angle);
    undoChangeRoll = -angle;
    lastOp = 3;
  }
}






