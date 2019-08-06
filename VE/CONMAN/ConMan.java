// ConMan.java -- Connection Manager remote interface
//
// by Dennis Brown for Colab Bus VE Project
// 
// This is the remote interface for the Connection Manager class.

package ve.conman;

import java.rmi.*;
import ve.vobject.VObject;
import ve.types.*;

public interface ConMan extends Remote
{
  // register receives information about an object, creates a local
  // remoteVObjectInfo object for it, and puts it in the vector.
  // This function is called via RMI by the object and returns that
  // object's world ID.
  public int register(VObject remoteVObject, ThreeVec position,
		      double focusRadius, double nimbusRadius)
    throws RemoteException;

  // unregister removes the object with id = "id" from the world.
  // This function is called via RMI by the object to be removed.
  public void unregister(int id) throws RemoteException;
  
  // updateObjectPosition updates the position of an object in its
  // information object.
  // This function is called via RMI by the object whose position changed.
  public void updateObjectPosition(int id, ThreeVec position)
    throws RemoteException;

  // updateFocusRadius updates the focus radius of an object in its
  // information object.
  // This function is called via RMI by the object whose focus radius changed.
  public void updateFocusRadius(int id, double focusRadius)
    throws RemoteException;

  // updateNimbusRadius updates the nimbus radius of an object in its
  // information object.
  // This function is called via RMI by the object whose nimbus radius changed.
  public void updateNimbusRadius(int id, double nimbusRadius)
    throws RemoteException;
}








