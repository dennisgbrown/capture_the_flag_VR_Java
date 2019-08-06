/*
 * VCJavaWidget class
 *
 * July 1997--Dennis Brown--Colab Bus VE Project
 *
 * creates a VCOLLIDE "widget" to implement collision detection
 */

/*
 * want this file to be part of the VCOLLIDE package
 */
package VCOLLIDE;

/**
 * VCJavaWidget
 *
 * Define a class VCJavaWidget which depends on native methods to implement
 * collision detection through VCOLLIDE
 */
public class VCJavaWidget
{
  // Pointer to VCOLLIDE instance on C side.
  private long vcPtr = 0;

  // Pointer to VCOLLIDE collisions array on C side.
  private long vcColPtr = 0;

  // Java-side collisions array; created from above array on C side.
  public int collisions[];

  // maximum number of objects (passed into constructor)
  private int maxNumObjs = -1;
  
  // Link with the native VCJava library.  If we cannot link, an exception
  // is thrown.  The name of the library is named "VCJava" at the 
  // Java level, or libVCJava.so at the solaris level.
  // since this is a static intitializer, it is loaded when the class\
  // is loaded.
  static {
    try {
      System.loadLibrary( "VCJava" );
    } catch ( UnsatisfiedLinkError e) {
      System.out.println( "Sorry, can't find the VCJava library");
      System.exit(-1);
    }
  }

  // The constructor creates a VCOLLIDE instance through Open() and
  // creates the Java-side collisions array.
  public VCJavaWidget(int maxNumObjs) throws VCJavaWidgetOpenFailedException
  {
    this.maxNumObjs = maxNumObjs;
    collisions = new int[maxNumObjs * maxNumObjs];
    Open();
  }

  // The following functions are defined in VCJava_funcs.c.
  private native void Open();
  public native void Close();
  public native int NewObject();
  public native void AddTri(double v11, double v12, double v13,
			    double v21, double v22, double v23,
			    double v31, double v32, double v33);
  public native void EndObject();
  public native void DeleteObject(int id); 

  public native void ActivateObject(int id);
  public native void DeactivateObject(int id);
  public native void ActivatePair(int id1, int id2);
  public native void DeactivatePair(int id1, int id2);

  public native void UpdateTrans(int id,
				 double a11,double a12,double a13,double a14,
				 double a21,double a22,double a23,double a24,
				 double a31,double a32,double a33,double a34,
				 double a41,double a42,double a43,double a44);
  public native void Collide(); 
  public native void Report(int collisions[]);
}






