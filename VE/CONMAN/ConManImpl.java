// ConManImpl.java -- Connection Manager Implementation class
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class keeps track Virtual Object positions and notifies
// the objects when they need to establish or break communications.

package ve.conman;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import ve.vobject.*;
import ve.types.*;
import java.util.Vector;

public class ConManImpl extends UnicastRemoteObject implements ConMan
{
  // This is the initial size of a vector.
  private static final int vectorSize = 30;

  // This is the size with which to increment a full vector.
  private static final int vectorSizeIncrement = 30;

  // The remoteVObjects vector contains remoteVObjects which
  // contain basic information about each object in the world.
  private Vector remoteVObjects = new Vector(vectorSize, vectorSizeIncrement);

  // This is the number of objects in the world. We can't just use
  // remoteVObject.size() since some entries in that vector may become null.
  private int numObjs = 0;

  
  // The constructor just starts a new thread to watch for collisions
  // between the spherical auras of the objects.
  public ConManImpl() throws RemoteException
  {
    super();

    // Start thread to detect aura collisions.
    AuraWatcher AuraWatch = new AuraWatcher(this);
  }


  // register receives information about an object, creates a local
  // remoteVObjectInfo object for it, and puts it in the vector.
  // This function is called via RMI by the object and returns that
  // object's world ID.
  public int register(VObject remoteVObject, ThreeVec position,
		      double focusRadius, double nimbusRadius)
    throws RemoteException
  {
    // The number of objects always grows. It includes dead objects.
    numObjs++;
    int id = numObjs - 1;

    // Here we're making an assumption about how addElement works.
    // If it works as expected, then this object will be inserted at
    // position "id."
    remoteVObjects.addElement(new remoteVObjectInfo(remoteVObject,
						    id, position,
						    focusRadius,
						    nimbusRadius));

    System.out.print("New object: id = " + id);
    System.out.print(" pos = " + position.x + " " +
		       position.y + " " + position.z);
    System.out.print(" focus = " + focusRadius);
    System.out.println(" nimbus = " + nimbusRadius);

    return(id);
  }


  // unregister removes the object with id = "id" from the world.
  // This function is called via RMI by the object to be removed.
  public void unregister(int id) throws RemoteException
  {
    System.out.println("Object " + id + " is leaving the simulation.");

    // Get a pointer to the object's information.
    remoteVObjectInfo killme = null;
    try {
      killme = (remoteVObjectInfo)remoteVObjects.elementAt(id);
      remoteVObjects.setElementAt(null, id);
    } catch (Exception e) { reportErrorAndDie(e); }

    // Look at each object's information. If this object is copied by
    // any of the others, tell them to destroy the copy. 
    for (int i = 0; i < numObjs; i++)
      {
	remoteVObjectInfo currObj = null;
	try {
	  currObj = (remoteVObjectInfo)remoteVObjects.elementAt(i);
	  if (currObj == null) continue;

	  // If the dead object is in the focus of the current object...
	  if (currObj.objectInFocus(killme) == true)
	    {
	      System.out.println("Removing copy of dead object " + id +
				 " from object " + i);
	      // ... remove this object from the focus list,
	      currObj.removeObjectFromFocusList(killme);

	      // tell the current object to destroy its copy, and
	      currObj.remoteVObject.destroyCopy(id);

	      // tell the current object to quit sending updates to
	      // any copies it might have in the dead object.
	      currObj.remoteVObject.forgetRemoteCopiesIn(id);
	    }
	} catch (Exception e) { reportErrorAndDie(e); }
      }
  }


  // updateObjectPosition updates the position of an object in its
  // information.
  // This function is called via RMI by the object whose position changed.
  public void updateObjectPosition(int id, ThreeVec position)
    throws RemoteException
  {
    try {
      remoteVObjectInfo updateMe =
	(remoteVObjectInfo)remoteVObjects.elementAt(id);
      updateMe.updatePosition(position);
    } catch (Exception e) { reportErrorAndDie(e); } 
  }


  // updateFocusRadius updates the focus radius of an object in its
  // information object.
  // This function is called via RMI by the object whose focus radius changed.
  public void updateFocusRadius(int id, double focusRadius)
    throws RemoteException
  {
    try {
      remoteVObjectInfo updateMe =
	(remoteVObjectInfo)remoteVObjects.elementAt(id);
      updateMe.updateFocusRadius(focusRadius);
    } catch (Exception e) { reportErrorAndDie(e); } 
  }


  // updateNimbusRadius updates the nimbus radius of an object in its
  // information object.
  // This function is called via RMI by the object whose nimbus radius changed.
  public void updateNimbusRadius(int id, double nimbusRadius)
    throws RemoteException
  {
    try {
      remoteVObjectInfo updateMe =
	(remoteVObjectInfo)remoteVObjects.elementAt(id);
      updateMe.updateNimbusRadius(nimbusRadius);
    } catch (Exception e) { reportErrorAndDie(e); } 
  }


  // detectAuraCollisions cycles through all objects' information and
  // detects collisions between each object's aura and every other
  // object's aura. If one object's focus intersects another's nimbus,
  // the first object receives a local copy of the second.
  public void detectAuraCollisions()
  {
    int i, j;                         // just some counters

    remoteVObjectInfo obj1 = null;    // pointer to the first object
    remoteVObjectInfo obj2 = null;    // pointer to the second object
    ThreeVec pos1 = null;             // first object's position
    ThreeVec pos2 = null;             // second object's position
    double distance = 0.0;            // distance between objects

    // For each object that isn't null...
    for (i = 0; i < numObjs; i++)
      {
	try {
	  obj1 = (remoteVObjectInfo)remoteVObjects.elementAt(i);
	} catch (Exception e) { reportErrorAndDie(e); }

	if (obj1 == null) continue;

	// ... look at every other object that isn't the same object or null.
	for (j = 0; j < numObjs; j++)
	  if (i != j)
	    {
	      try {
		obj2 = (remoteVObjectInfo)remoteVObjects.elementAt(j);
	      } catch (Exception e) { reportErrorAndDie(e); }
	      
	      if (obj2 == null) continue;
	      
	      // Find distance between object centers.
	      pos1 = obj1.getPosition();
	      pos2 = obj2.getPosition();
	      distance = (pos1.subtract(pos2)).length();
	      
	      // If obj2 is in obj1's focus range but not copied by
	      // obj1, tell obj1 to get obj2.
	      if (distance <= (obj1.getFocusRadius() +
			       obj2.getNimbusRadius()) &&
		  (obj1.objectInFocus(obj2) == false))
		{
		  System.out.print("Adding copy of " + obj2.id +
				     " to " + obj1.id + ": ");
		  System.out.println("(" + distance + " apart)");

		  // Add obj2 to obj1's local list of copied objects.
		  obj1.addObjectToFocusList(obj2);

		  // Tell obj2 to ask obj1 to make a copy of obj2.
		  try {
		    obj2.remoteVObject.requestRemoteCopy(obj1.remoteVObject);
		  } catch (Exception e) { reportErrorAndDie(e); }
		}
	      
	      // If obj2 isn't in obj1's focus range but IS copied by
	      // obj1, tell obj1 to get rid of obj2.
	      else if (distance > (obj1.getFocusRadius() +
				   obj2.getNimbusRadius()) &&
		       (obj1.objectInFocus(obj2) == true))
		{
		  System.out.println("Removing copy of object " + obj2.id +
				     " from object " + obj1.id);
		  System.out.println("    " + distance + " apart");

		  // Remove obj2 from obj1's local list of copied objects.
		  obj1.removeObjectFromFocusList(obj2);

		  // Tell obj1 to destroy its copy of obj2.
		  try {
		    obj1.remoteVObject.destroyCopy(obj2.id);
		  } catch (Exception e) { reportErrorAndDie(e); }
		}
	    }
      }
  }
  

  // main sets a security manager, makes a new instance of this class,
  // and binds a well-known name to that instance so that objects can
  // find it.
  public static void main(String args[])
  {
    System.setSecurityManager(new RMISecurityManager());

    String port = "1099";  // Default rmiregistry port
    
    if (args.length == 1)
      {
	port = args[0];
      }
    else if (args.length > 1)
      {
	System.err.println("One argument expected: port number.");
	System.exit(0);
      }

    try {
      System.out.println("Hello1");
      ConManImpl ins = new ConManImpl();
      System.out.println("Hello2");
      InetAddress add = InetAddress.getLocalHost();
      System.out.println("Hello3");
      Naming.rebind("//"+add.getHostName()+":"+port+"/ConMan", ins);
      System.out.println("Hello4");
    } catch (Exception e) { reportErrorAndDie(e); }
    
    System.out.println("ConMan bound in registry");
  }


  // reportErrorAndDie is a lazy way of dealing with fatal errors.
  private static void reportErrorAndDie(Exception e)
  {
    System.err.println("ConMan exception: " + e.getMessage());
    e.printStackTrace();
  }
}




// This class holds information about each object in the world which
// is needed by the Connection Manager to do its job.
class remoteVObjectInfo
{
  VObject remoteVObject;      // remote reference to the actual object
  int id;                     // a copy of its id
  ThreeVec position;          // a copy of its position
  double focusRadius;         // a copy of its focus radius
  double nimbusRadius;        // a copy of its nimbus radius

  // This is the initial size of a vector.
  private static final int vectorSize = 30;

  // This is the size with which to increment a full vector.
  private static final int vectorSizeIncrement = 30;

  // This vector holds references to other objects within this
  // object's focus (i.e., of which objects does it have copies).
  Vector objectsInFocus = null;

  
  // The constructor is straightforward.
  public remoteVObjectInfo(VObject remoteVObject, int id, ThreeVec position,
			   double focusRadius, double nimbusRadius)
  {
    this.remoteVObject = remoteVObject;
    this.id = id;
    this.position = position;
    this.focusRadius = focusRadius;
    this.nimbusRadius = nimbusRadius;

    objectsInFocus = new Vector(vectorSize, vectorSizeIncrement);
  }


  // updatePosition changes the position within this object's information.
  public synchronized void updatePosition(ThreeVec position)
  {
    this.position.x = position.x;
    this.position.y = position.y;
    this.position.z = position.z;
  }

  
  // updateFocusRadius changes the focus radius within this object's
  // information.
  public synchronized void updateFocusRadius(double focusRadius)
  {
    this.focusRadius = focusRadius;
  }

  
  // updateNimbusRadius changes the nimbus radius within this object's
  // information.
  public synchronized void updateNimbusRadius(double nimbusRadius)
  {
    this.nimbusRadius = nimbusRadius;
  }

  
  // getPosition() returns this object's position.
  public synchronized ThreeVec getPosition()
  {
    return position;
  }

  
  // getFocusRadius() returns this object's focus radius.
  public synchronized double getFocusRadius()
  {
    return focusRadius;
  }

  
  // getNimbusRadius() returns this object's nimbus radius.
  public synchronized double getNimbusRadius()
  {
    return nimbusRadius;
  }

  
  // objectInFocus returns TRUE if the object referenced by obj is in
  // the focus of this object.
  public boolean objectInFocus(remoteVObjectInfo obj)
  {
    if (objectsInFocus.indexOf(obj) != -1)
      return true;
    else
      return false;
  }


  // addObjectToFocusList adds the reference to obj to this object's
  // focus list.
  public void addObjectToFocusList(remoteVObjectInfo obj)
  {
    objectsInFocus.addElement(obj);
  }


  // removeObjectFromFocusList removes the reference to obj from this
  // object's focus list.
  public void removeObjectFromFocusList(remoteVObjectInfo obj)
  {
    objectsInFocus.removeElement(obj);
  }
}




// The AuraWatcher is a class which runs the "detectAuraCollisions"
// method of the ConManImpl class in a thread.
class AuraWatcher extends Thread
{
  ConManImpl conMan;

  public AuraWatcher(ConManImpl conMan)
  {
    super("AuraWatcher");
    this.conMan  = conMan;
    start();
  }

  public void run()
  {
    while(true)
      {
        yield();
        conMan.detectAuraCollisions();
      }
  }
}







