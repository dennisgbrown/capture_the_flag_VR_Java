// AwareObject.java -- for vobjects which are aware of their surroundings
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class adds awareness of other objects and collision detection
// to the BaseObject.

package ve.vobject;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import ve.conman.*;
import ve.types.*;
import java.util.Vector;
//import VCOLLIDE.*;


public class AwareObject extends BaseObject
{
  // If this is a copy, get pointer to local owner (i.e., the object
  // that created the copy, not the remote owner of which this is a copy)
  protected AwareObject localOwner = null;
  
  // vector of copies of remote objects
  protected AwareObject localCopies[];  

  // This is the VCOLLIDE widget for collision detection.
  //protected VCJavaWidget VC = null;

  // This is the object's id in the collider (assigned by the collider).
  // If a local copy of a remote object, this id is valid for the
  // collider in the owner vobject.
  protected int VCid = -1;

  // This array associates collider IDs with vobject pointers.
  // It is not used in local copies of remote objects.
  protected BaseObject colliderIdToVObj[];

  // This array associates object IDs with how long they've been in
  // collision with this object.
  protected int collisionTimes[];

  // This array associates object IDs with whether or not they're
  // colliding with this object.
  protected boolean collisions[];

  
  
  /////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////////////////////////////////////////////


  // This constructor is for an ORIGINAL virtual object. It
  // instantiates a BaseObject then adds awareness and collision variables.
  public AwareObject(String serverName, ThreeVec initPosition,
		     ThreeVec initLookat, ThreeVec initVUP,
		     double initFocusRadius, double initNimbusRadius,
		     String initType, PolyObject initGeometry,
		     boolean reportToConMan)
    throws RemoteException
  {
    super(serverName, initPosition, initLookat, initVUP, initFocusRadius,
	  initNimbusRadius, initType, initGeometry, false);

    // Set up a new array to contain references to local copies of
    // remote vobjects.
    localCopies = new AwareObject[maxNumObjs];
    for (int i = 0; i < maxNumObjs; i++) localCopies[i] = null;

    // Set up collisionTimes array.
    collisionTimes = new int[maxNumObjs];
    for (int i = 0; i < maxNumObjs; i++) collisionTimes[i] = -1;

    // Set up collisions array.
    collisions = new boolean[maxNumObjs];
    for (int i = 0; i < maxNumObjs; i++) collisions[i] = false;

    // Try to instantiate the collision detector.
//    try {
//      VC = new VCJavaWidget(maxNumObjs);
//    } catch (Exception e) { reportErrorAndDie(e); }

    // Set up the collider ID to object ID arrays.
    colliderIdToVObj = new BaseObject[maxNumObjs];
    for (int i = 0; i < maxNumObjs; i++)
      colliderIdToVObj[i] = null;

    // Add this object's geometry to the collision detector.
    VCid = addGeometryToVC(geometry, this);      

    // Set up accessory state (DOV, x, y, z, u, v, w) and geometry.
    resetAxesAndVertices();

    // Update the collider with the changes caused by the above call.
    updateCollider();

    // Report myself to the connection manager.
    if (reportToConMan == true) reportToConMan(serverName);
  }


  // This constructor is for a LOCAL COPY OF A REMOTE virtual object.
  public AwareObject(int id, ThreeVec position, ThreeVec lookat, ThreeVec VUP,
		    double focusRadius, double nimbusRadius, String type,
		    PolyObject geometry, AwareObject localOwner,
		    VObject remoteOwner)
    throws RemoteException
  {
    super(id, position, lookat, VUP, focusRadius, nimbusRadius, type,
	  geometry, remoteOwner);

    // Set the local owner of this copy. 
    this.localOwner = localOwner;
    
    // Add this object's geometry to the collision detector.
    VCid = localOwner.addGeometryToVC(geometry, this); 
    
    // Update the collider with the changes caused by resetAxesAndVertices.
    updateCollider();
  }


  
  /////////////////////////////////////////////////////////////////////////
  // METHODS FOR CHANGING THE ORIGINAL OBJECT'S POSITION AND
  // ORIENTATION AND THAT OF ITS COPIES
  /////////////////////////////////////////////////////////////////////////

  
  // Augment moveAlongDOV to update the collider.
  public void moveAlongDOV(double numUnits) throws RemoteException
  {
    super.moveAlongDOV(numUnits);
    updateCollider();
  }

  
  // Augment changeYaw to update the collider.
  public void changeYaw(double angle) throws RemoteException
  {
    super.changeYaw(angle);
    updateCollider();
  }

  
  // Augment changePitch to update the collider.
  public void changePitch(double angle) throws RemoteException
  {
    super.changePitch(angle);
    updateCollider();
  }

  
  // Augment changeRoll to update the collider.
  public void changeRoll(double angle) throws RemoteException
  {
    super.changeRoll(angle);
    updateCollider();
  }


  
  /////////////////////////////////////////////////////////////////////////
  // METHODS FOR MANIPULATING THIS COPY OF THE OBJECT ONLY (IGNORING
  // ITS REMOTE COPIES)
  /////////////////////////////////////////////////////////////////////////

  
  // Update a vobject's transformation in the collider.
  public void updateCollider()
  {
    return;
//    if (copy == false)
//      {
//	VC.UpdateTrans(VCid,
//		       x.x, x.y, x.z, position.x,
//		       y.x, y.y, y.z, position.y,
//		       z.x, z.y, z.z, position.z,
//		       0.0, 0.0, 0.0, 1.0);
//	collide();
//      }
//    else
//      {
//	localOwner.VC.UpdateTrans(VCid,
//				  x.x, x.y, x.z, position.x,
//				  y.x, y.y, y.z, position.y,
//				  z.x, z.y, z.z, position.z,
//				  0.0, 0.0, 0.0, 1.0);
//	localOwner.collide();
//      }
  }

  
  
  /////////////////////////////////////////////////////////////////////////
  // METHODS FOR MAKING AND DESTROYING LOCAL COPIES OF REMOTE OBJECTS
  /////////////////////////////////////////////////////////////////////////

  
  // Make a local copy of a remote vobject with these parameters. Called by
  // the remote vobject to be copied. Return its reference.
  public VObject makeLocalCopy(int id, ThreeVec position,
			       ThreeVec lookat, ThreeVec VUP,
			       double focusRadius, double nimbusRadius,
			       String type, PolyObject geometry,
			       VObject remoteOwner)
    throws RemoteException
  {
    AwareObject localCopy = null;

    System.out.println("Making a local copy of " + id);

    // Create the new vobject instance.
    try {
      localCopy = new AwareObject(id, position, lookat, VUP,
				  focusRadius, nimbusRadius, type, geometry,
				  this, remoteOwner);
    } catch (Exception e) { reportErrorAndDie(e); }

    // Add it to the localCopies array.
    localCopies[id] = localCopy;

    // Update the collider.
    updateCollider();
    
    return localCopy;
  }


  // Remove a local copy of a remote vobject with id = "id" from the
  // local copies vector. Called by ConMan. Used when the remote
  // vobject leaves this vobject's focus or when the remote vobject dies.
  public void destroyCopy(int id) throws RemoteException
  {
    System.out.println("Removing local copy of " + id);
    
    // Take vobject out of the collider.
//    VC.DeleteObject(localCopies[id].VCid);
//    colliderIdToVObj[localCopies[id].VCid] = null;

    // Take it out of the localCopies array.
    localCopies[id] = null;

    // Update the collider.
    updateCollider();
  }

  
  // Add an object's geometry to the collision detector and
  // collision-detector-id-to-object array. Return that id.
  public synchronized int addGeometryToVC(PolyObject geo, BaseObject obj)
  {
    // Start a new object in the collider.
//    int VCid = VC.NewObject();
    
    // Define the new object with the unaltered geometry of the object.
//    for (int t = 0; t < geo.numTriangles; t++)
//      VC.AddTri(geo.origVertices[geo.triangles[t].vertices[0]].coords.x,
//		geo.origVertices[geo.triangles[t].vertices[0]].coords.y,
//		geo.origVertices[geo.triangles[t].vertices[0]].coords.z,
//		geo.origVertices[geo.triangles[t].vertices[1]].coords.x,
//		geo.origVertices[geo.triangles[t].vertices[1]].coords.y,
//		geo.origVertices[geo.triangles[t].vertices[1]].coords.z,
//		geo.origVertices[geo.triangles[t].vertices[2]].coords.x,
//		geo.origVertices[geo.triangles[t].vertices[2]].coords.y,
//		geo.origVertices[geo.triangles[t].vertices[2]].coords.z); 
//    VC.EndObject();

    int VCid = 0;

    // Add this object to the collider id array.
    colliderIdToVObj[VCid] = obj;

    return VCid;
  }


  
  /////////////////////////////////////////////////////////////////////////
  // METHODS RUNNING IN ANOTHER THREAD
  /////////////////////////////////////////////////////////////////////////

  
  // This member is run constantly in another thread. It checks for
  // collisions between objects.
  protected synchronized void collide()
  {   
    // First, make VC find collisions and put the info into the
    // collisions array.
//    VC.Collide();
//    VC.Report(VC.collisions);
    
    // generic counter
    int i = 0;

    // Set collisions with all other objects to false for now.
    for (i = 0; i < maxNumObjs; i++) collisions[i] = false;

    // For each object colliding with this one, set its collisions[]
    // value to true and increment its collisionTimes entry (capping
    // off at 10000).
    i = 0;
//    while (VC.collisions[i] != -1)
//      {
//	try {
	  // Remember VC.collisions's format is pairwise listing of
	  // colliding object ids (i.e., [i] and [i+1] are colliding),
	  // terminated by -1. So we must check which pair of the
	  // collision is this object and which is the other, if the
	  // pair involves this object at all.
//	  if (VC.collisions[i] == VCid)
//	    {
//	      int objId = colliderIdToVObj[VC.collisions[i+1]].getId();
//	      if (collisionTimes[objId] < 10000) collisionTimes[objId]++;
//	      collisions[objId] = true;
//	    }
//	  else if (VC.collisions[i+1] == VCid)
//	    {
//	      int objId = colliderIdToVObj[VC.collisions[i]].getId();
//	      if (collisionTimes[objId] < 10000) collisionTimes[objId]++;
//	      collisions[objId] = true;
//	    }
//	} catch (Exception e) { reportErrorAndDie(e); }
//        i += 2;
//      }

    // For each object we're no longer hitting, reset its time.
    for (i = 0; i < maxNumObjs; i++)
      if (collisions[i] == false)
	collisionTimes[i] = -1;
  }
}



