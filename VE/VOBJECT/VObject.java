// VObject.java -- Virtual Object remote interface
//
// by Dennis Brown for Colab Bus VE Project
// 
// This is the remote interface for the Virtual Object class.

package ve.vobject;

import java.rmi.*;
import ve.types.*;

public interface VObject extends Remote
{
  // Return the vobject's global ID.
  public int getId() throws RemoteException;


  // Move the object along its direction of view a number of units.
  // If this is the original object, make sure the copies reflect the change.
  public void moveAlongDOV(double numUnits) throws RemoteException;

  // Change yaw (rotation around v (VUP if VUP is perpenidcular to
  // DOV) given angle in degrees.
  // If this is the original object, make sure the copies reflect the change.
  public void changeYaw(double angle) throws RemoteException;
  
  // Change pitch (rotation around u, the cross of DOV and VUP) given
  // angle in degrees.
  // If this is the original object, make sure the copies reflect the change.
  public void changePitch(double angle) throws RemoteException;

  // Change roll (rotation around w or DOV) given angle in degrees.
  // If this is the original object, make sure the copies reflect the change.
  public void changeRoll(double angle) throws RemoteException;


  // This function is called by ConMan and tells this vobject to ask
  // the target vobject make a local copy of this vobject.
  public void requestRemoteCopy(VObject target) throws RemoteException;

  // Quit sending updates to remote copies of this vobject in the
  // vobject with id =  "id." Called by ConMan when the remote vobject dies.
  public void forgetRemoteCopiesIn(int id) throws RemoteException;
  
  // Make a local copy of a remote vobject with these parameters. Called by
  // the remote vobject to be copied. Return its reference.
  public VObject makeLocalCopy(int id, ThreeVec position,
			       ThreeVec lookat, ThreeVec VUP,
			       double focusRadius, double nimbusRadius,
			       String type, PolyObject geometry,
			       VObject remoteOwner)
    throws RemoteException;
  
  // Remove a local copy of a remote vobject with id = "id" from the
  // local copies vector. Called by ConMan. Used when the remote
  // vobject leaves this vobject's focus or when the remote vobject dies.
  public void destroyCopy(int id) throws RemoteException;
}










