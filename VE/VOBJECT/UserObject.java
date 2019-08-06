// UserObject.java -- Keyboard-controlled vobject 
//
// by Dennis Brown for Colab Bus VE Project
// 
// This class provides a way to change aspects of a virtual object
// using keyboard commands. It also opens a window using OpenGL and
// renders the simulation in it.

package ve.vobject;

import java.applet.*;
import java.awt.*;
import java.rmi.*;
import java.util.Vector;
import java.lang.String;
import java.lang.Double;
//import OpenGL.*;
// import VCOLLIDE.*;
import ve.types.*;
import com.hermetica.magician.*;


public class UserObject extends AwareObject
{
   private GraphicsDrawer GD = null;
   private GL GLPIPE = null;
   private GLU GLUPIPE = null;
   private GLComponent glc = null;

   // Keep the light position here.
   private float lightPosition[] = new float[4];


   // pointer to user interface
   private UserInterfaceFrame UI = null;

   // This boolean determines if collisions should be checked.
   protected boolean checkCollisions = true;

   // This is a list of copies of object seen by this object, which is
   // used in the user interface. It must be instantiated and kept
   // updated in this class (the UI has its own class).
   private List copyList = new List(4, false);

   // This is an array of copyEnhancement objects, which tell which
   // enhancements to use on each local copy of a remote object.
   // It is indexed by ID.
   protected CopyEnhancement[] copyEnhancements;



   /////////////////////////////////////////////////////////////////////////
   // CONSTRUCTORS
   /////////////////////////////////////////////////////////////////////////

   // This constructor is for an ORIGINAL virtual object. It
   // instantiates an AwareObject then adds a user interface.
   public UserObject(String serverName, ThreeVec initPosition,
                     ThreeVec initLookat, ThreeVec initVUP,
                     double initFocusRadius, double initNimbusRadius,
                     String initType, PolyObject initGeometry,
                     int windowXpos, int windowYpos, int windowXsize,
                     int windowYsize, boolean reportToConMan)
   throws RemoteException
   {
      super(serverName, initPosition, initLookat, initVUP, initFocusRadius,
            initNimbusRadius, initType, initGeometry, false);

      // Initialize the array of enhancements to copies.
      copyEnhancements = new CopyEnhancement[maxNumObjs];
      for (int i = 0; i < maxNumObjs; i++)
         copyEnhancements[i] = new CopyEnhancement();

      // Initialize the AWT list of copies.
      for (int i = 0; i < maxNumObjs; i++) copyList.add(i + " <empty>", i);

      // Set up user interface.
      UI = new UserInterfaceFrame("Controls for " + initType, this,
                                  windowXpos, windowYpos,
                                  windowXsize, windowYsize,
                                  copyList, copyEnhancements);

      // Try to instantiate an OpenGL widget.
      GD = new GraphicsDrawer(windowXsize, windowYsize, this, initPosition);
      GLPIPE = GD.getGL();
      GLUPIPE = GD.getGLU();
      glc = GD.getComponent();

      // Update the graphics display to clear the screen. Do it twice
      // due to double buffering.
      updateGraphics();
      updateGraphics();

      // Report myself to the connection manager.
      if (reportToConMan == true) reportToConMan(serverName);

      // After id is known, add myself to the copy list.
      copyList.replaceItem(id + " <this obj>", id);
   }


   // This constructor is for a LOCAL COPY OF A REMOTE virtual object.
   public UserObject(int id, ThreeVec position, ThreeVec lookat, ThreeVec VUP,
                     double focusRadius, double nimbusRadius, String type,
                     PolyObject geometry, UserObject localOwner,
                     VObject remoteOwner)
   throws RemoteException
   {
      super(id, position, lookat, VUP, focusRadius, nimbusRadius, type,
            geometry, localOwner, remoteOwner);
   }




   /////////////////////////////////////////////////////////////////////////
   // METHODS TO UPDATE DISPLAY AND COLLIDER
   /////////////////////////////////////////////////////////////////////////


   // updateGraphics draws at each local copy of a remote object.
   // It is executed constantly in another thread.
   public void updateGraphics()
   {
      if (glc == null) return;
      glc.repaint();
   }


   public void updateGraphicsHelper()
   {
      // If the GL PIPE isn't initialized yet, don't use it yet.
      if (GLPIPE == null) return;

      // Clear the screen buffer and transformation matrix.
      GLPIPE.glClear(GLPIPE.GL_COLOR_BUFFER_BIT | GLPIPE.GL_DEPTH_BUFFER_BIT);
      GLPIPE.glLoadIdentity();

      // Set the lookat coordinates to set the transformation matrix correctly.
      GLUPIPE.gluLookAt(position.x, position.y, position.z,
                        lookat.x, lookat.y, lookat.z,
                        VUP.x, VUP.y, VUP.z);

      // Set the light position to be the user's position.
      lightPosition[0] = (float)position.x;
      lightPosition[1] = (float)position.y;
      lightPosition[2] = (float)position.z;
      lightPosition[3] = 1.0f;
      GLPIPE.glLightfv(GLPIPE.GL_LIGHT0, GLPIPE.GL_POSITION, lightPosition); 

      // Draw each local copy of a remote object.
      for (int c = 0; c < maxNumObjs; c++)
      {
         try
         {

            // If there is no copy at this array position, do nothing (duh!).
            if (localCopies[c] == null) continue;

            // Grab the id of the local copy for use below.
            int id = localCopies[c].id;

            // If the "draw" enhancement is turned off for this
            // object, don't draw it.
            if (copyEnhancements[id].draw == false) continue;

            // Grab the geometry of the object to draw.
            PolyObject geo = localCopies[c].geometry;

            // Draw its geometry.
            for (int t = 0; t < geo.numTriangles; t++)
            {
               GLPIPE.glBegin(GLPIPE.GL_POLYGON);

               // If the "colorShift" enhancement is turned off, don't
               // shift colors; otherwise, do.
               if (copyEnhancements[id].colorShift == false)
                  GLPIPE.glColor3d((double)(geo.vertices[geo.triangles[t].vertices[0]].vColor.getRed())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[0]].vColor.getGreen())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[0]].vColor.getBlue())/255.0);
               else
                  GLPIPE.glColor3d((double)(geo.vertices[geo.triangles[t].vertices[0]].vColor.getBlue())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[0]].vColor.getRed())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[0]].vColor.getGreen())/255.0);


               GLPIPE.glNormal3d(geo.triangles[t].normal.x,
                                 geo.triangles[t].normal.y,
                                 geo.triangles[t].normal.z);

               GLPIPE.glVertex3d(geo.vertices[geo.triangles[t].vertices[0]].coords.x,
                                 geo.vertices[geo.triangles[t].vertices[0]].coords.y,
                                 geo.vertices[geo.triangles[t].vertices[0]].coords.z);

               // If the "colorShift" enhancement is turned off, don't
               // shift colors; otherwise, do.
               if (copyEnhancements[id].colorShift == false)
                  GLPIPE.glColor3d((double)(geo.vertices[geo.triangles[t].vertices[1]].vColor.getRed())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[1]].vColor.getGreen())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[1]].vColor.getBlue())/255.0);
               else
                  GLPIPE.glColor3d((double)(geo.vertices[geo.triangles[t].vertices[1]].vColor.getBlue())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[1]].vColor.getRed())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[1]].vColor.getGreen())/255.0);

               GLPIPE.glVertex3d(geo.vertices[geo.triangles[t].vertices[1]].coords.x,
                                 geo.vertices[geo.triangles[t].vertices[1]].coords.y,
                                 geo.vertices[geo.triangles[t].vertices[1]].coords.z);

               // If the "colorShift" enhancement is turned off, don't
               // shift colors; otherwise, do.
               if (copyEnhancements[id].colorShift == false)
                  GLPIPE.glColor3d((double)(geo.vertices[geo.triangles[t].vertices[2]].vColor.getRed())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[2]].vColor.getGreen())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[2]].vColor.getBlue())/255.0);
               else
                  GLPIPE.glColor3d((double)(geo.vertices[geo.triangles[t].vertices[2]].vColor.getBlue())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[2]].vColor.getRed())/255.0,
                                   (double)(geo.vertices[geo.triangles[t].vertices[2]].vColor.getGreen())/255.0);

               GLPIPE.glVertex3d(geo.vertices[geo.triangles[t].vertices[2]].coords.x,
                                 geo.vertices[geo.triangles[t].vertices[2]].coords.y,
                                 geo.vertices[geo.triangles[t].vertices[2]].coords.z);

               GLPIPE.glEnd();
            }
         } catch (Exception e)
         {
            reportErrorAndDie(e);
         }
      }
   }


   // Augment updateCollider so that it updates the display every time
   // the collider gets updated. This is somewhat of a hack but oh well.
   public void updateCollider()
   {      
      super.updateCollider();

      if (copy == false)
         updateGraphics();
      else
         ((UserObject)localOwner).updateGraphics();
   }


   // Augment collide of AwareObject by making sure collision detection
   // is enabled for checking for collisions.
   protected synchronized void collide()
   {
      if (checkCollisions == true) super.collide();
   }



   /////////////////////////////////////////////////////////////////////////
   // METHODS FOR HANDLING USER INTERFACE-RELATED EVENTS
   /////////////////////////////////////////////////////////////////////////


   // Determine if collision detection is on.
   public boolean collisionDetectionOn()
   {
      return checkCollisions;
   }


   // Turn on collision detection...
   public void turnOnCollisionDetection()
   {
      checkCollisions = true;
      System.out.println("Collisions turned on");

      // Now that collisions are on, update the current set of collisions.
      collide();
   }


   // Turn off collision detection...
   public void turnOffCollisionDetection()
   {
      checkCollisions = false;
      System.out.println("Collisions turned off");
   }


   // Activate collisions with object of that id.
   public void activateCollisionsWithObject(int id)
   {
      // Activate.
//    VC.ActivateObject(localCopies[id].VCid);

      // Now update the collider.
      collide();
   }


   // Deactivate collisions with object of that id.
   public void deactivateCollisionsWithObject(int id)
   {
      // Deactivate.
//    VC.DeactivateObject(localCopies[id].VCid);

      // Now update the collider.
      collide();
   }



   /////////////////////////////////////////////////////////////////////////
   // METHODS FOR MAKING AND DESTROYING LOCAL COPIES OF REMOTE OBJECTS
   /////////////////////////////////////////////////////////////////////////


   // Augment makeLocalCopy of AwareObject by adding the new object's
   // name (type) to the copyList.
   public VObject makeLocalCopy(int id, ThreeVec position,
                                ThreeVec lookat, ThreeVec VUP,
                                double focusRadius, double nimbusRadius,
                                String type, PolyObject geometry,
                                VObject remoteOwner)
   throws RemoteException
   {
      UserObject localCopy = null;

      System.out.println("Making a local copy of " + id);


      // Create the new vobject instance.
      try
      {
         localCopy = new UserObject(id, position, lookat, VUP,
                                    focusRadius, nimbusRadius, type, geometry,
                                    this, remoteOwner);
      } catch (Exception e)
      {
         reportErrorAndDie(e);
      }

      // Add it to the localCopies array.
      localCopies[id] = localCopy;

      // Add it to the copy list for the user interface.
      copyList.replaceItem(id + " " + type, id);

      // Update the collider.
      updateCollider();

      return localCopy;
   }


   // Augment destroyCopy of AwareObject by removing the dead object's
   // name (type) from the copyList.
   public void destroyCopy(int id) throws RemoteException
   {
      super.destroyCopy(id);
      copyList.replaceItem(id + " <empty>", id);

      copyEnhancements[id].resetValues();
   }
}




// CopyEnhancement is a simple class that holds information about the
// enhancements made to each local copy of a remote object.
class CopyEnhancement
{
   // Detect collisions with this object?
   public boolean collisionDetect = true;

   // Should this object have shifted colors?
   public boolean colorShift = false;

   // Should we draw the object?
   public boolean draw = true;

   // Reset the values to the defaults.
   public void resetValues()
   {
      collisionDetect = true;
      colorShift = false;
      draw = true;
   }
}




// This is a frame for the user interface to the virtual object.
class UserInterfaceFrame extends Frame
{
   // This is a pointer to the user object which instantiates this frame.
   UserObject userObj = null;

   // This is the id of the object selected for enhancing.
   int objectSelected = -1;

   // This is the array of enhancements to copies of objects. It's
   // initialized by the UserObject constructor.
   CopyEnhancement[] copyEnhancements;

   // label and button for collisions
   Label allObjCollisionsLabel;
   Button allObjCollisionsButton;

   // labels, buttons, and text fields for changing focus and nimbus
   Label focusLabel;
   TextField focusTextField;
   Button focusButton;
   Label nimbusLabel;
   TextField nimbusTextField;
   Button nimbusButton;

   // list of object copies
   List copyList;
   Label objectSelectedLabel;

   // label and button for single object collisions
   Label singleObjCollisionsLabel;
   Button singleObjCollisionsButton;

   // label and button for single object colorShifting
   Label singleObjColorShiftLabel;
   Button singleObjColorShiftButton;

   // label and button for single object drawing
   Label singleObjDrawLabel;
   Button singleObjDrawButton;


   // Set up applet frame...
   public UserInterfaceFrame(String title, UserObject userObj,
                             int rendererXpos, int rendererYpos,
                             int rendererXsize, int rendererYsize,
                             List copyList, CopyEnhancement[] copyEnhancements)
   {
      super(title);

      // Set some values for use below...
      this.userObj = userObj;
      this.copyList = copyList;
      this.copyEnhancements = copyEnhancements;

      // Set a new layout for this component.
      GridBagLayout gridbag = new GridBagLayout();

      // Set up the menu bar.
      MenuBar menubar = new MenuBar();
      Menu file = new Menu("File");
      menubar.add(file);
      file.add("Quit");
      this.setMenuBar(menubar);

      // Create a panel to hold components.
      Panel panel1 = new Panel();
      panel1.setLayout(gridbag);

      // Create button for collisions with all other vobjects.
      allObjCollisionsLabel = new Label("Collisions with all objects: ON");
      allObjCollisionsButton = new Button("Click to turn OFF");
      constrain(panel1, allObjCollisionsLabel,  0, 0, 1, 1);
      constrain(panel1, allObjCollisionsButton, 1, 0, 1, 1);

      // Create a text field for entering a new focus (press "Set" when OK).
      focusLabel = new Label("Focus radius: ");
      focusTextField = new
                       TextField(String.valueOf(userObj.getFocusRadius()), 12);
      focusButton = new Button("Set");
      constrain(panel1, focusLabel, 0, 1, 1, 1);
      constrain(panel1, focusTextField, 1, 1, 1, 1);
      constrain(panel1, focusButton, 2, 1, 1, 1);

      // Create a text field for entering a new nimbus (press "Set" when OK).
      nimbusLabel = new Label("Nimbus radius: ");
      nimbusTextField = new
                        TextField(String.valueOf(userObj.getNimbusRadius()), 12);
      nimbusButton = new Button("Set");
      constrain(panel1, nimbusLabel, 0, 2, 1, 1);
      constrain(panel1, nimbusTextField, 1, 2, 1, 1);
      constrain(panel1, nimbusButton, 2, 2, 1, 1);

      // Put list of copies and associated buttons into panel.
      constrain(panel1, new Label("Remote Objects:"), 0, 3, 1, 1, 10, 0, 0, 0);
      constrain(panel1, copyList, 0, 4, 1, 4, GridBagConstraints.VERTICAL,
                GridBagConstraints.NORTHWEST, 0.0, 1.0, 0, 0, 0, 0);

      // Use a label to say which object has been selected.
      objectSelectedLabel = new Label("Object selected: none");
      constrain(panel1, objectSelectedLabel, 1, 3, 1, 1, 10, 0, 0, 0);

      // Use a label and button to show and change whether or not
      // collisions with this object are enabled.
      singleObjCollisionsLabel = new Label("Collisions: ON");
      constrain(panel1, singleObjCollisionsLabel, 1, 4, 1, 1);
      singleObjCollisionsButton = new Button("Click to turn OFF");
      constrain(panel1, singleObjCollisionsButton, 2, 4, 1, 1);

      // Use a label and button to show and change whether or not
      // this object colorShifts.
      singleObjColorShiftLabel = new Label("Color shifting: ON");
      constrain(panel1, singleObjColorShiftLabel, 1, 5, 1, 1);
      singleObjColorShiftButton = new Button("Click to turn ON");
      constrain(panel1, singleObjColorShiftButton, 2, 5, 1, 1);

      // Use a label and button to show and change whether or not
      // this object is drawn.
      singleObjDrawLabel = new Label("Drawing: ON");
      constrain(panel1, singleObjDrawLabel, 1, 6, 1, 1);
      singleObjDrawButton = new Button("Click to turn OFF");
      constrain(panel1, singleObjDrawButton, 2, 6, 1, 1);

      // Put panel1 into the frame.
      this.setLayout(gridbag);
      constrain(this, panel1, 0, 0, 1, 1, GridBagConstraints.VERTICAL,
                GridBagConstraints.NORTHWEST, 0.0, 1.0, 10, 10, 5, 5);

      // Set location and size of frame.
      this.setLocation(rendererXpos, rendererYpos + rendererYsize + 25);
      this.pack();
      this.show();
   }


   // This function was taken from "Java in a Nutshell" example 5-4.
   // It places components within a container.
   public void constrain(Container container, Component component,
                         int grid_x, int grid_y,
                         int grid_width, int grid_height,
                         int fill, int anchor,
                         double weight_x, double weight_y,
                         int top, int left, int bottom, int right)
   {
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = grid_x; c.gridy = grid_y;
      c.gridwidth = grid_width; c.gridheight = grid_height;
      c.fill = fill; c.anchor = anchor;
      c.weightx = weight_x; c.weighty = weight_y;
      if (top+bottom+left+right > 0)
         c.insets = new Insets(top, left, bottom, right);

      ((GridBagLayout)container.getLayout()).setConstraints(component, c);
      container.add(component);
   }

   // This function was taken from "Java in a Nutshell" example 5-4.
   // It calls constrain with some default values.
   public void constrain(Container container, Component component,
                         int grid_x, int grid_y,
                         int grid_width, int grid_height)
   {
      constrain(container, component,
                grid_x, grid_y, grid_width, grid_height,
                GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
                0.0, 0.0,
                0, 0, 0, 0);
   }

   // This function was taken from "Java in a Nutshell" example 5-4.
   // It calls constrain with some default values.
   public void constrain(Container container, Component component,
                         int grid_x, int grid_y,
                         int grid_width, int grid_height,
                         int top, int left, int bottom, int right)
   {
      constrain(container, component,
                grid_x, grid_y, grid_width, grid_height,
                GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
                0.0, 0.0,
                top, left, bottom, right);
   }

   // Handle the GUI events.
   public boolean handleEvent(Event e)
   {
      try
      {
         switch (e.id)
         {
         case Event.ACTION_EVENT:
            {
               // Handle pull-down menu events.
               if (e.target instanceof MenuItem)
               {
                  // Quit shuts everything down.
                  if (((String)e.arg).equals("Quit"))
                  {
                     userObj.removeFromConMan();
                     System.exit(0);
                  }
               }

               // Handle the all-object collisions button.
               else if (e.target == allObjCollisionsButton)
               {
                  if (userObj.collisionDetectionOn() == true)
                  {
                     userObj.turnOffCollisionDetection();
                     allObjCollisionsLabel.
                     setText("Collisions with all objects: OFF");
                     allObjCollisionsButton.setLabel("Click to turn ON");
                  } else if (userObj.collisionDetectionOn() == false)
                  {
                     userObj.turnOnCollisionDetection();
                     allObjCollisionsLabel.
                     setText("Collisions with all objects: ON");
                     allObjCollisionsButton.setLabel("Click to turn OFF");
                  }
                  return true;
               }

               // Handle single object collisions button.
               else if (e.target == singleObjCollisionsButton)
               {
                  if (objectSelected == -1) return true;
                  if (copyEnhancements[objectSelected].collisionDetect == true)
                  {
                     singleObjCollisionsLabel.setText("Collisions: OFF");
                     singleObjCollisionsButton.setLabel("Click to turn ON");
                     copyEnhancements[objectSelected].collisionDetect = false;
                     userObj.deactivateCollisionsWithObject(objectSelected);
                  } else if (copyEnhancements[objectSelected].collisionDetect == false)
                  {
                     singleObjCollisionsLabel.setText("Collisions: ON");
                     singleObjCollisionsButton.setLabel("Click to turn OFF");
                     copyEnhancements[objectSelected].collisionDetect = true;
                     userObj.activateCollisionsWithObject(objectSelected);
                  }
                  return true;
               }

               // Handle single object colorShift button.
               else if (e.target == singleObjColorShiftButton)
               {
                  if (objectSelected == -1) return true;
                  if (copyEnhancements[objectSelected].colorShift == true)
                  {
                     singleObjColorShiftLabel.setText("Color shifting: OFF");
                     singleObjColorShiftButton.setLabel("Click to turn ON");
                     copyEnhancements[objectSelected].colorShift = false;
                     userObj.updateGraphics();
                  } else if (copyEnhancements[objectSelected].colorShift == false)
                  {
                     singleObjColorShiftLabel.setText("Color shifting: ON");
                     singleObjColorShiftButton.setLabel("Click to turn OFF");
                     copyEnhancements[objectSelected].colorShift = true;
                     userObj.updateGraphics();
                  }
                  return true;
               }

               // Handle single object draw button.
               else if (e.target == singleObjDrawButton)
               {
                  if (objectSelected == -1) return true;
                  if (copyEnhancements[objectSelected].draw == true)
                  {
                     singleObjDrawLabel.setText("Drawing: OFF");
                     singleObjDrawButton.setLabel("Click to turn ON");
                     copyEnhancements[objectSelected].draw = false;
                     userObj.updateGraphics();
                  } else if (copyEnhancements[objectSelected].draw == false)
                  {
                     singleObjDrawLabel.setText("Drawing: ON");
                     singleObjDrawButton.setLabel("Click to turn OFF");
                     copyEnhancements[objectSelected].draw = true;
                     userObj.updateGraphics();
                  }
                  return true;
               }

               // Handle the focus button.
               else if (e.target == focusButton)
               {
                  try
                  {
                     double newFocusRadius =
                     (Double.valueOf(focusTextField.getText())).doubleValue();
                     userObj.setFocusRadius(newFocusRadius);
                  } catch (Exception ex)
                  {
                     reportErrorAndDie(ex);
                  }
                  return true;
               }

               // Handle the nimbus button.
               else if (e.target == nimbusButton)
               {
                  try
                  {
                     double newNimbusRadius =
                     (Double.valueOf(nimbusTextField.getText())).doubleValue();
                     userObj.setNimbusRadius(newNimbusRadius);
                  } catch (Exception ex)
                  {
                     reportErrorAndDie(ex);
                  }
                  return true;
               }
            }

            // Handle selections on the vobject copy list.
         case Event.LIST_SELECT:
            {
               objectSelected = copyList.getSelectedIndex();

               // Can't select self for these enhancements.
               if (objectSelected == userObj.getId())
               {
                  objectSelected = -1;
                  objectSelectedLabel.setText("Object selected: none");
                  return true;
               }

               // Once a new object is selected, reflect its enhancement
               // parameters in the widgets.
               objectSelectedLabel.setText("Object selected: " + objectSelected);
               if (copyEnhancements[objectSelected].collisionDetect == true)
               {
                  singleObjCollisionsLabel.setText("Collisions: ON");
                  singleObjCollisionsButton.setLabel("Click to turn OFF");
               } else if (copyEnhancements[objectSelected].collisionDetect == false)
               {
                  singleObjCollisionsLabel.setText("Collisions: OFF");
                  singleObjCollisionsButton.setLabel("Click to turn ON");
               }
               if (copyEnhancements[objectSelected].colorShift == true)
               {
                  singleObjColorShiftLabel.setText("Color shifting: ON");
                  singleObjColorShiftButton.setLabel("Click to turn OFF");
               } else if (copyEnhancements[objectSelected].colorShift == false)
               {
                  singleObjColorShiftLabel.setText("Color shifting: OFF");
                  singleObjColorShiftButton.setLabel("Click to turn ON");
               }
               if (copyEnhancements[objectSelected].draw == true)
               {
                  singleObjDrawLabel.setText("Drawing: ON");
                  singleObjDrawButton.setLabel("Click to turn OFF");
               } else if (copyEnhancements[objectSelected].draw == false)
               {
                  singleObjDrawLabel.setText("Drawing: OFF");
                  singleObjDrawButton.setLabel("Click to turn ON");
               }
               return true;
            }


            // Handle key presses.
         case Event.KEY_PRESS:
            {
               // Pressing 'E' increases pitch (rotate around x axis).
               if (((char)e.key == 'e') || ((char)e.key == 'E'))
               {
                  userObj.changePitch(-10.0);
                  return true;
               }

               // Pressing 'C' decreases pitch (rotate around x axis).
               if (((char)e.key == 'c') || ((char)e.key == 'C'))
               {
                  userObj.changePitch(10.0);
                  return true;
               }

               // Pressing 'S' rolls ccw (rotate around z axis).
               if (((char)e.key == 's') || ((char)e.key == 'S'))
               {
                  userObj.changeRoll(10.0);
                  return true;
               }

               // Pressing 'F' rolls cw (rotate around z axis).
               if (((char)e.key == 'f') || ((char)e.key == 'F'))
               {
                  userObj.changeRoll(-10.0);
                  return true;
               }

               // Return false so other key presses are passed to the applet.
               return false;
            }

            // Handle more key presses.
         case Event.KEY_ACTION:
            {
               // Pressing UP moves forward along DOV.
               if (e.key == Event.UP)
               {
                  userObj.moveAlongDOV(1.0);
                  return true;
               }

               // Pressing DOWN moves backward along DOV.
               if (e.key == Event.DOWN)
               {
                  userObj.moveAlongDOV(-1.0);
                  return true;
               }

               // Pressing RIGHT rotates cw around y axis.
               if (e.key == Event.RIGHT)
               {
                  userObj.changeYaw(10.0);
                  return true;
               }

               // Pressing LEFT rotates ccw around y axis.
               if (e.key == Event.LEFT)
               {
                  userObj.changeYaw(-10.0);
                  return true;
               }

               // Return false so other key presses are passed to the applet.
               return false;
            }
         }
      } catch (Exception ex)
      {
         reportErrorAndDie(ex);
      }

      return false;
   }

   // reportErrorAndDie is a lazy way of dealing with fatal errors.
   protected static void reportErrorAndDie(Exception e)
   {
      System.err.println("UserInterface exception: " + e.getMessage());
      e.printStackTrace();
   }
}


class GraphicsDrawer extends Frame implements GLComponentListener
{
   // Use native methods from OpenGL for graphics.
   private GL GLPIPE = null;
   private GLU GLUPIPE = null;

   // UserObject which owns this GraphicsDrawer
   UserObject UserObj = null;

   ThreeVec initPosition = null;

   GLComponent glc = null;

   int windowXsize;
   int windowYsize;

   // Keep the light position here.
   private float lightPosition[] = new float[4];


   public GraphicsDrawer(int windowXsize, int windowYsize, UserObject UserObj, ThreeVec initPosition)
   {
      GLPIPE = new CoreGL();
      GLUPIPE = new CoreGLU();
      this.UserObj = UserObj;
      this.initPosition = initPosition;
      this.windowXsize = windowXsize;
      this.windowYsize = windowYsize;
      setLayout(new BorderLayout());
      glc = GLComponentFactory.createGLComponent(windowXsize, windowYsize);
      add("Center", glc);
      pack();
      show();

      // Set some basic properties of the OpenGL state.

      GLCapabilities cap = glc.getContext().getCapabilities();
      cap.setDepthBits(12);
      cap.setColourBits(24);
      cap.setPixelType(GLCapabilities.RGBA);
      cap.setDoubleBuffered(GLCapabilities.DOUBLEBUFFER);
      glc.addGLComponentListener(this);
      glc.initialize();  
   }

   public void initialize(GLDrawable component)
   {
      GLPIPE.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
      GLPIPE.glMatrixMode(GLPIPE.GL_PROJECTION);
      GLPIPE.glLoadIdentity();
      GLPIPE.glFrustum(-1.0, 1.0, -1.0, 1.0, 1.5, 1000.0);
      GLPIPE.glMatrixMode(GLPIPE.GL_MODELVIEW);
      GLPIPE.glViewport(component, 0, 0, windowXsize, windowYsize);
      GLPIPE.glEnable(GLPIPE.GL_DEPTH_TEST);
      GLPIPE.glDepthFunc(GLPIPE.GL_LESS);
      GLPIPE.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

      // Set up lighting.
      lightPosition[0] = (float)initPosition.x;
      lightPosition[1] = (float)initPosition.y;
      lightPosition[2] = (float)initPosition.z;
      lightPosition[3] = 1.0f;
      GLPIPE.glLightfv(GLPIPE.GL_LIGHT0, GLPIPE.GL_POSITION, lightPosition); 
      GLPIPE.glEnable(GLPIPE.GL_COLOR_MATERIAL);
      GLPIPE.glEnable(GLPIPE.GL_LIGHTING);
      GLPIPE.glEnable(GLPIPE.GL_LIGHT0);  
   }

   public void reshape(GLDrawable component, int width, int height)   
   {
      UserObj.updateGraphicsHelper();
   }

   public void display(GLDrawable component)
   {
      UserObj.updateGraphicsHelper();
   }

   public GL getGL()
   {
      return GLPIPE;
   }

   public GLU getGLU()
   {
      return GLUPIPE;
   }

   public GLComponent getComponent()
   {
      return glc;
   }
}



