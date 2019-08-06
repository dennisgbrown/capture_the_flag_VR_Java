// BaseObject.java -- Virtual Object Implementation class
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class provides member functions to access and change aspects
// of a virtual object which is not aware of other objects.

package ve.vobject;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import ve.conman.*;
import ve.types.*;
import java.util.Vector;


public class BaseObject extends UnicastRemoteObject implements VObject
{
  protected int id = -1;                // global id of the vobject
  protected ThreeVec position = null;   // xyz position in the virtual world
  protected ThreeVec lookat = null;     // xyz location the vobject is facing
  protected ThreeVec VUP = null;        // UP vector of the vobject
  protected PolyObject geometry = null; // geometry of the vobject
  protected double focusRadius = -1.0;  // radius of the vobject's focus sphere
  protected double nimbusRadius = -1.0; // radius of the vobject's nimbus 
  protected String type = "";           // string to describe vobject
  protected boolean copy;            // Is this a local copy of a remote obj?
  VObject remoteOwner = null;      // If a local copy, get pointer to original.

  protected ConMan conMan = null;   // reference to the Connection Manager
 
  protected ThreeVec DOV = null;    // direction of view
  protected ThreeVec u = null;      // u,v,w coordinate system in x,y,z world
  protected ThreeVec v = null;      //    (xyz is world; uvw is the vobject)
  protected ThreeVec w = null;
  protected ThreeVec x = null;      // x,y,z coordinate system in u,v,w world
  protected ThreeVec y = null;
  protected ThreeVec z = null;

  // This is the initial size of a vector.
  protected static final int vectorSize = 30;

  // This is the size with which to increment a full vector.
  protected static final int vectorSizeIncrement = 30;

  // number of remote copies of this object
  protected int numRemoteCopies = 0;

  // vector of remote copies of this object
  protected Vector remoteCopies = null; 
  
  // Arbitrarily set the maximum number of objects allowed in the simulation.
  protected int maxNumObjs = 100; 

  // Use this to make sure no moves happen during the making of a copy.
  private boolean canMove = true;


  
  /////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////////////////////////////////////////////


  // This constructor is for an ORIGINAL virtual object. Its arguments
  // are the name of the server hosting the Connection Manager
  // process, the initial position of the vobject, the point it is
  // facing (lookat), its UP vector, its focus and nimbus sphere
  // radii, and its appearance.
  public BaseObject(String serverName, 
		    ThreeVec position, ThreeVec lookat, ThreeVec VUP,
		    double focusRadius, double nimbusRadius, String type,
		    PolyObject geometry, boolean reportToConMan)
    throws RemoteException
  {
    super();

    // Initialize vobject state.
    this.position = position;
    this.lookat = lookat;
    this.VUP = VUP;
    this.focusRadius = focusRadius;
    this.nimbusRadius = nimbusRadius;
    this.type = type;
    this.geometry = geometry;
    this.copy = false;

    // Set up accessory state (DOV, x, y, z, u, v, w) and geometry.
    resetAxesAndVertices();

    // Set up a new vector to contain references to remote copies
    // of this vobject.
    remoteCopies = new Vector(vectorSize, vectorSizeIncrement);

    // Report myself to the connection manager.
    if (reportToConMan == true) reportToConMan(serverName);
  }

  
  // Try to connect to the Connection Manager and register.
  public void reportToConMan(String serverName)
  {
    try {
      conMan = (ConMan)Naming.lookup("//"+serverName+"/ConMan");
      id = conMan.register(this, position, focusRadius, nimbusRadius);
      System.out.println("Object " + type + " given id " + id);
    } catch (Exception e) { reportErrorAndDie(e); }
  }
  
  
  // This constructor is for a LOCAL COPY OF A REMOTE virtual object.
  // It will never be called by a BaseObject, but it is here to
  // appease the Java constructor-chaining gods.
  public BaseObject(int id, ThreeVec position, ThreeVec lookat, ThreeVec VUP,
		    double focusRadius, double nimbusRadius, String type,
		    PolyObject geometry, VObject remoteOwner)
    throws RemoteException
  {
    // Initialize vobject state.
    this.id = id;
    this.position = position;
    this.lookat = lookat;
    this.VUP = VUP;
    this.focusRadius = focusRadius;
    this.nimbusRadius = nimbusRadius;
    this.type = type;
    this.geometry = geometry;
    this.copy = true;
    this.remoteOwner = remoteOwner;

    // Set up accessory state (DOV, x, y, z, u, v, w) and geometry.
    resetAxesAndVertices();
  }

  

  /////////////////////////////////////////////////////////////////////////
  // METHODS FOR GETTING ANS SETTING PROPERTIES OF A VOBJECT
  /////////////////////////////////////////////////////////////////////////

  
  // Return the vobject's global ID.
  public int getId() throws RemoteException
  {
    return id;
  }


  // Return the vobject's type.
  public String getType()
  {
    return type;
  }


  // Return the vobject's focus radius.
  public double getFocusRadius()
  {
    return focusRadius;
  }


  // Return the vobject's nimbus radius.
  public double getNimbusRadius()
  {
    return nimbusRadius;
  }


  // Set the vobject's focus radius.
  public void setFocusRadius(double focusRadius)
  {
    this.focusRadius = focusRadius;
    try {
      conMan.updateFocusRadius(id, focusRadius);
    } catch (Exception e) { reportErrorAndDie(e); }
  }


  // Set the vobject's focus radius.
  public void setNimbusRadius(double nimbusRadius)
  {
    this.nimbusRadius = nimbusRadius;
    try {
      conMan.updateNimbusRadius(id, nimbusRadius);
    } catch (Exception e) { reportErrorAndDie(e); }
  }


  
  /////////////////////////////////////////////////////////////////////////
  // METHODS FOR MANIPULATING THIS COPY OF THE OBJECT ONLY (IGNORING
  // ITS REMOTE COPIES)
  /////////////////////////////////////////////////////////////////////////

  // This function updates the object's geometry to reflect how it
  // looks in the virtual world. The world uses the xyz coordinate
  // system and the object uses a uvw system based on its direction of
  // view and its UP vector. This function calculates DOV, u, v, w,
  // x, y, and z, then changes the vobject's geometry to what it
  // should be in the xyz coordinate system.
  protected void resetAxesAndVertices()
  {
    // Normalize VUP just in case.
    VUP.normalize();

    // Create DOV.
    DOV = new ThreeVec(lookat.x - position.x,
                       lookat.y - position.y,
                       lookat.z - position.z);

    // Create u, v, and w (uvw axes in xyz coordinate system)
    w = new ThreeVec(DOV);
    w.normalize();
    u = new ThreeVec(DOV);
    u = u.cross(VUP);
    u.normalize();
    v = new ThreeVec(u);
    v = v.cross(w);
    v.normalize();

    // Create x, y, and z (xyz axes in uvw coordinate system)
    x = new ThreeVec(u.x, v.x, w.x);
    y = new ThreeVec(u.y, v.y, w.y);
    z = new ThreeVec(u.z, v.z, w.z);
    x.normalize();
    y.normalize();
    z.normalize();

    // Set working copy of vertices.
    for (int i = 0; i < geometry.numVertices; i++)
      {
        geometry.vertices[i].coords.x =
          geometry.origVertices[i].coords.dot(x) + position.x;
        geometry.vertices[i].coords.y =
          geometry.origVertices[i].coords.dot(y) + position.y;
        geometry.vertices[i].coords.z =
          geometry.origVertices[i].coords.dot(z) + position.z;
      }

    // Update normals.
    geometry.updateNormals();
  }


  // Change the vobject's position.
  // If this is the original vobject, tell ConMan about the change.
  protected void updatePosition(double x, double y, double z)
  {
    position.x = x;
    position.y = y;
    position.z = z;

    if (copy == false)
      {
	try {
	  conMan.updateObjectPosition(id, position);
	} catch (Exception e) { reportErrorAndDie(e); }
      }
  }


  
  /////////////////////////////////////////////////////////////////////////
  // METHODS FOR CHANGING THE ORIGINAL OBJECT'S POSITION AND
  // ORIENTATION AND THAT OF ITS COPIES
  /////////////////////////////////////////////////////////////////////////

  
  // Move the object along its direction of view a number of units.
  // If this is the original object, make sure the copies reflect the change.
  public void moveAlongDOV(double numUnits) throws RemoteException
  {
    // Don't move while this object is being copied...
    if (canMove == false) return;
    
    // Create vector to move along (DOV of length numUnits).
    ThreeVec dist = new ThreeVec(DOV);
    dist.normalize();
    dist.x *= numUnits;
    dist.y *= numUnits;
    dist.z *= numUnits;
    
    // Update new position.
    updatePosition(position.x + dist.x,
		   position.y + dist.y,
		   position.z + dist.z);

    // Update new lookat.
    lookat.x += dist.x;
    lookat.y += dist.y;
    lookat.z += dist.z;

    resetAxesAndVertices();

    if (copy == false)
      {
	for (int c = 0; c < remoteCopies.size(); c++)
	  {
	    try {
	      VObject remoteCopy =
		((RemoteCopyAndOwnerId)remoteCopies.elementAt(c)).remoteCopy;
	      remoteCopy.moveAlongDOV(numUnits);
	    } catch (Exception e) { reportErrorAndDie(e); }
	  }
      }
  }

  
  // Change yaw (rotation around v (VUP if VUP is perpenidcular to
  // DOV) given angle in degrees.
  // If this is the original object, make sure the copies reflect the change.
  public void changeYaw(double angle) throws RemoteException
  {
    // Don't move while this object is being copied...
    if (canMove == false) return;

    double radangle = (angle / 180.0) * java.lang.Math.PI;
    double cost = java.lang.Math.cos(radangle);
    double sint = java.lang.Math.sin(radangle);
    
    // First change lookat:
    // Translate lookat to origin.
    lookat.x -= position.x;
    lookat.y -= position.y;
    lookat.z -= position.z;
    // Rotate to uvw coord sys.
    ThreeVec lookatuvw = new ThreeVec(u.dot(lookat),
                                      v.dot(lookat),
                                      w.dot(lookat));
    // Rotate around v axis.
    ThreeVec lookatuvwrot = new ThreeVec(lookatuvw.x * cost +
                                         lookatuvw.z * sint,
                                         lookatuvw.y,
                                         lookatuvw.z * cost -
                                         lookatuvw.x * sint);
    // Rotate back to xyz coord sys.
    ThreeVec lookatxyz = new ThreeVec(x.dot(lookatuvwrot),
                                      y.dot(lookatuvwrot),
                                      z.dot(lookatuvwrot));
    // Translate back into place.
    lookat.x = lookatxyz.x + position.x;
    lookat.y = lookatxyz.y + position.y;
    lookat.z = lookatxyz.z + position.z;
    
    // Next change VUP.
    // Rotate to uvw coord sys.
    ThreeVec VUPuvw = new ThreeVec(u.dot(VUP),
                                   v.dot(VUP),
                                   w.dot(VUP));
    // Rotate around v axis.
    ThreeVec VUPuvwrot = new ThreeVec(VUPuvw.x * cost +
                                      VUPuvw.z * sint,
                                      VUPuvw.y,
                                      VUPuvw.z * cost -
                                      VUPuvw.x * sint);
    // Rotate back to xyz coord sys.
    VUP.x = x.dot(VUPuvwrot);
    VUP.y = y.dot(VUPuvwrot);
    VUP.z = z.dot(VUPuvwrot);

    resetAxesAndVertices();

    if (copy == false)
      {
	for (int c = 0; c < remoteCopies.size(); c++)
	  {
	    try {
	      VObject remoteCopy =
		((RemoteCopyAndOwnerId)remoteCopies.elementAt(c)).remoteCopy;
	      remoteCopy.changeYaw(angle);
	    } catch (Exception e) { reportErrorAndDie(e); }
	  }
      }
  }

  
  // Change pitch (rotation around u, the cross of DOV and VUP) given
  // angle in degrees.
  // If this is the original object, make sure the copies reflect the change.
  public void changePitch(double angle) throws RemoteException
  {
    // Don't move while this object is being copied...
    if (canMove == false) return;

    double radangle = (angle / 180.0) * java.lang.Math.PI;
    double cost = java.lang.Math.cos(radangle);
    double sint = java.lang.Math.sin(radangle);
    
    // First change lookat:
    // Translate lookat to origin.
    lookat.x -= position.x;
    lookat.y -= position.y;
    lookat.z -= position.z;
    // Rotate to uvw coord sys.
    ThreeVec lookatuvw = new ThreeVec(u.dot(lookat),
                                      v.dot(lookat),
                                      w.dot(lookat));
    // Rotate around u axis.
    ThreeVec lookatuvwrot = new ThreeVec(lookatuvw.x,
                                         lookatuvw.y * cost -
                                         lookatuvw.z * sint,
                                         lookatuvw.y * sint +
                                         lookatuvw.z * cost);
    // Rotate back to xyz coord sys.
    ThreeVec lookatxyz = new ThreeVec(x.dot(lookatuvwrot),
                                      y.dot(lookatuvwrot),
                                      z.dot(lookatuvwrot));
    // Translate back into place.
    lookat.x = lookatxyz.x + position.x;
    lookat.y = lookatxyz.y + position.y;
    lookat.z = lookatxyz.z + position.z;
    
    // Next change VUP.
    // Rotate to uvw coord sys.
    ThreeVec VUPuvw = new ThreeVec(u.dot(VUP),
                                   v.dot(VUP),
                                   w.dot(VUP));
    // Rotate around u axis.
    ThreeVec VUPuvwrot = new ThreeVec(VUPuvw.x,
                                      VUPuvw.y * cost -
                                      VUPuvw.z * sint,
                                      VUPuvw.y * sint +
                                      VUPuvw.z * cost);
    // Rotate back to xyz coord sys.
    VUP.x = x.dot(VUPuvwrot);
    VUP.y = y.dot(VUPuvwrot);
    VUP.z = z.dot(VUPuvwrot);

    resetAxesAndVertices();

    if (copy == false)
      {
	for (int c = 0; c < remoteCopies.size(); c++)
	  {
	    try {
	      VObject remoteCopy =
		((RemoteCopyAndOwnerId)remoteCopies.elementAt(c)).remoteCopy;
	      remoteCopy.changePitch(angle);
	    } catch (Exception e) { reportErrorAndDie(e); }
	  }
      }
  }

  
  // Change roll (rotation around w or DOV) given angle in degrees.
  // If this is the original object, make sure the copies reflect the change.
  public void changeRoll(double angle) throws RemoteException
  {
    // Don't move while this object is being copied...
    if (canMove == false) return;

    double radangle = (angle / 180.0) * java.lang.Math.PI;
    double cost = java.lang.Math.cos(radangle);
    double sint = java.lang.Math.sin(radangle);
    
    // First change lookat:
    // Translate lookat to origin.
    lookat.x -= position.x;
    lookat.y -= position.y;
    lookat.z -= position.z;
    // Rotate to uvw coord sys.
    ThreeVec lookatuvw = new ThreeVec(u.dot(lookat),
                                      v.dot(lookat),
                                      w.dot(lookat));
    // Rotate around w axis.
    ThreeVec lookatuvwrot = new ThreeVec(lookatuvw.x * cost -
                                         lookatuvw.y * sint,
                                         lookatuvw.x * sint +
                                         lookatuvw.y * cost,
                                         lookatuvw.z);
    // Rotate back to xyz coord sys.
    ThreeVec lookatxyz = new ThreeVec(x.dot(lookatuvwrot),
                                      y.dot(lookatuvwrot),
                                      z.dot(lookatuvwrot));
    // Translate back into place.
    lookat.x = lookatxyz.x + position.x;
    lookat.y = lookatxyz.y + position.y;
    lookat.z = lookatxyz.z + position.z;
    
    // Next change VUP.
    // Rotate to uvw coord sys.
    ThreeVec VUPuvw = new ThreeVec(u.dot(VUP),
                                   v.dot(VUP),
                                   w.dot(VUP));
    // Rotate around w axis.
    ThreeVec VUPuvwrot = new ThreeVec(VUPuvw.x *
                                      cost - VUPuvw.y * sint,
                                      VUPuvw.x * sint +
                                      VUPuvw.y * cost,
                                      VUPuvw.z);
    // Rotate back to xyz coord sys.
    VUP.x = x.dot(VUPuvwrot);
    VUP.y = y.dot(VUPuvwrot);
    VUP.z = z.dot(VUPuvwrot);

    resetAxesAndVertices();

    if (copy == false)
      {
	for (int c = 0; c < remoteCopies.size(); c++)
	  {
	    try {
	      VObject remoteCopy =
		((RemoteCopyAndOwnerId)remoteCopies.elementAt(c)).remoteCopy;
	      remoteCopy.changeRoll(angle);
	    } catch (Exception e) { reportErrorAndDie(e); }
	  }
      }
  }

  
  
  /////////////////////////////////////////////////////////////////////////
  // METHODS FOR MAKING AND DESTROYING LOCAL COPIES OF REMOTE OBJECTS
  /////////////////////////////////////////////////////////////////////////


  // This function is called by ConMan and tells this vobject to ask
  // the target vobject make a local copy of this vobject.
  public void requestRemoteCopy(VObject target) throws RemoteException
  {
    // Make sure no changes happen while the object is being copied.
    canMove = false;
    
    try {
      System.out.println("Asking target " + target.getId() +
			 " to make copy of me.");
    
      VObject remoteCopy =
	target.makeLocalCopy(id, position, lookat, VUP, focusRadius,
			     nimbusRadius, type, geometry, this);

      // Now add remoteCopy to the list of remote copies to keep updated.
      remoteCopies.addElement(new RemoteCopyAndOwnerId(remoteCopy,
						       target.getId()));
    } catch (Exception e) { reportErrorAndDie(e); }

    canMove = true;
  }

  
  // Quit sending updates to remote copies of this vobject in the
  // vobject with id =  "id." Called by ConMan when the remote vobject dies.
  public void forgetRemoteCopiesIn(int id) throws RemoteException
  {
    System.out.println("Forgetting remote copy of me in " + id);
    
    for (int i = 0; i < remoteCopies.size(); i++)
      {
	try {
	  int remoteCopyOwnerId =
	    ((RemoteCopyAndOwnerId)remoteCopies.elementAt(i)).ownerId;
	  if (remoteCopyOwnerId == id)
	    {
	      remoteCopies.removeElementAt(i);
	      return;
	    }
	} catch (Exception e) { reportErrorAndDie(e); }
      }
  }


  // Make a local copy of a remote vobject with these parameters. Called by
  // the remote vobject to be copied. Return its reference.
  public VObject makeLocalCopy(int id, ThreeVec position,
			       ThreeVec lookat, ThreeVec VUP,
			       double focusRadius, double nimbusRadius,
			       String type, PolyObject geometry,
			       VObject remoteOwner)
    throws RemoteException
  {
    // The base object doesn't keep copies of other objects, so this
    // just does nothing. This function is just here since it's in
    // the VObject interface.
    System.out.println("Shouldn't be calling this member!");
    return null;
  }

  
  // Remove a local copy of a remote vobject with id = "id" from the
  // local copies vector. Called by ConMan. Used when the remote
  // vobject leaves this vobject's focus or when the remote vobject dies.
  public void destroyCopy(int id) throws RemoteException
  {
    // The base object doesn't keep copies of other objects, so this
    // just does nothing. This function is just here since it's in
    // the VObject interface.
    System.out.println("Shouldn't be calling this member either!");
  }

    
  // Remove myself from the Connection Manager, removing myself from the
  // simulation.
  public void removeFromConMan()
  {
    if (copy == false)
      {
	System.out.println("Unregistering...");
	try {
	  conMan.unregister(id);
	} catch (Exception e) { reportErrorAndDie(e); }
      }
  }


  /////////////////////////////////////////////////////////////////////////
  // OTHER METHODS 
  /////////////////////////////////////////////////////////////////////////

  
  // reportErrorAndDie is a lazy way of dealing with fatal errors.
  protected static void reportErrorAndDie(Exception e)
  {
    System.err.println("BaseObject exception: " + e.getMessage());
    e.printStackTrace();
  }
}


// This class exists only to couple a remote copy of the virtual object with
// the id of the virtual object that owns the copy. A vector of these
// are part of the state of the virtual object.
class RemoteCopyAndOwnerId
{
  protected VObject remoteCopy;
  protected int ownerId;

  public RemoteCopyAndOwnerId(VObject remoteCopy, int ownerId)
  {
    this.remoteCopy = remoteCopy;
    this.ownerId = ownerId;
  }
}


