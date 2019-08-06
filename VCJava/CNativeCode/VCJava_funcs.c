/*
 * This mammoth C file takes care of all the native implementation for the
 * bulk of VCOLLIDE commands [it is based on the OpenGL C file which
 * is not my original work -- Dennis Brown]
 */

/* 
 * need to include the JAVA internal header files for macros and function
 * prototypes required to maipulated JAVA data structures and functions
 *
 * StubPreamble.h includes the structure and macro definitions neede to
 * convert JAVA data structures into C data structures.
 *
 */

#include "StubPreamble.h"

/*
 * the next thing to include are special headers that were created by
 * JAVAH.  They include the C structure definitions for the JAVA classes
 */
#include "VCOLLIDE_VCJavaWidget.h"

/*
 * next put any C UNIX specific header files that are necessary to implement
 * this native code
 */			
#include <VCol.h>


/* These are the C functions which correspond to the "native" members
   of VCJavaWidget. They basically call the VCOLLIDE functions from
   the saved VCOLLIDE instance "vc." The more complicated functions
   are commented. */

void VCOLLIDE_VCJavaWidget_Open(struct HVCOLLIDE_VCJavaWidget *javaObj)
{
  /* This is a pointer to the VCOLLIDE instance on the C side. */
  void *vc;

  /* This is a pointer to the VC Report (of collisions) on the C side. */
  void *vcCollisions;

  /* Maximum number of objects, stored on Java side. */
  int maxNumObjs = unhand(javaObj)->maxNumObjs;;
  
  /* Initialize vc and save it on the Java side. */
  vc = vcOpen(); 
  unhand(javaObj)->vcPtr = (long)vc;

  /* Initialize vcCollisions and save it on the Java side. */
  vcCollisions = (void *) malloc(maxNumObjs * maxNumObjs *
				 sizeof(VCReportType) );
  unhand(javaObj)->vcColPtr = (long)vcCollisions;
}

void VCOLLIDE_VCJavaWidget_Close(struct HVCOLLIDE_VCJavaWidget *javaObj)
{
  void *vc = (void *) unhand(javaObj)->vcPtr; 
  vcClose(vc);
}
     
long VCOLLIDE_VCJavaWidget_NewObject(struct HVCOLLIDE_VCJavaWidget *javaObj)
{
  void *vc = (void *) unhand(javaObj)->vcPtr;
  int id;
  vcNewObject(vc, &id);
  return id;
}

void VCOLLIDE_VCJavaWidget_AddTri(struct HVCOLLIDE_VCJavaWidget *javaObj,
				  double v11, double v12, double v13,
				  double v21, double v22, double v23,
				  double v31, double v32, double v33)
{
  /* The triangle is passed in as 3 3D points (as 9 doubles) instead of
     three arrays. It's easier this way since there are no array-based
     vertices on the Java side. */

  void *vc = (void *) unhand(javaObj)->vcPtr;
  double v1[3], v2[3], v3[3];

  v1[0] = v11;
  v1[1] = v12;
  v1[2] = v13;
  v2[0] = v21;
  v2[1] = v22;
  v2[2] = v23;
  v3[0] = v31;
  v3[1] = v32;
  v3[2] = v33;
  vcAddTri(vc, v1, v2, v3);
}

void VCOLLIDE_VCJavaWidget_EndObject(struct HVCOLLIDE_VCJavaWidget *javaObj)
{
  void *vc = (void *) unhand(javaObj)->vcPtr;
  vcEndObject(vc);
}

void VCOLLIDE_VCJavaWidget_DeleteObject(struct HVCOLLIDE_VCJavaWidget *javaObj,
					long id)
{
  void *vc = (void *) unhand(javaObj)->vcPtr;
  vcDeleteObject(vc, (int)id);
}

void VCOLLIDE_VCJavaWidget_ActivateObject(struct HVCOLLIDE_VCJavaWidget
					  *javaObj, long id)
{
  void *vc = (void *) unhand(javaObj)->vcPtr;
  vcActivateObject(vc, (int)id);
}

void VCOLLIDE_VCJavaWidget_DeactivateObject(struct HVCOLLIDE_VCJavaWidget
					    *javaObj, long id)
{
  void *vc = (void *) unhand(javaObj)->vcPtr;
  vcDeactivateObject(vc, (int)id);
}

void VCOLLIDE_VCJavaWidget_ActivatePair(struct HVCOLLIDE_VCJavaWidget
					*javaObj, long id1, long id2)
{
  void *vc = (void *) unhand(javaObj)->vcPtr;
  vcActivatePair(vc, (int)id1, (int)id2);
}

void VCOLLIDE_VCJavaWidget_DeactivatePair(struct HVCOLLIDE_VCJavaWidget
					  *javaObj, long id1, long id2)
{
  void *vc = (void *) unhand(javaObj)->vcPtr;
  vcDeactivatePair(vc, (int)id1, (int)id2);
}

void VCOLLIDE_VCJavaWidget_UpdateTrans(struct HVCOLLIDE_VCJavaWidget *javaObj,
				       long id, 
				       double a11, double a12,
				         double a13, double a14,
				       double a21, double a22,
				         double a23, double a24,
				       double a31, double a32,
				         double a33, double a34,
				       double a41, double a42,
				         double a43, double a44)
{
  /* The transformation is passed in as 16 doubles rather than a 4x4
     array. It's easier this way since the information is not array-based
     on the Java side. */

  void *vc = (void *) unhand(javaObj)->vcPtr;
  double A[4][4];

  A[0][0] = a11;
  A[0][1] = a12;
  A[0][2] = a13;
  A[0][3] = a14;
  A[1][0] = a21;
  A[1][1] = a22;
  A[1][2] = a23;
  A[1][3] = a24;
  A[2][0] = a31;
  A[2][1] = a32;
  A[2][2] = a33;
  A[2][3] = a34;
  A[3][0] = a41;
  A[3][1] = a42;
  A[3][2] = a43;
  A[3][3] = a44;
  vcUpdateTrans(vc, id, A);
}

void VCOLLIDE_VCJavaWidget_Collide(struct HVCOLLIDE_VCJavaWidget *javaObj)
{
  void *vc = (void *) unhand(javaObj)->vcPtr;
  vcCollide(vc);
}

/* Report does a lot of work since it is converting the VCOLLIDE
   report data (an array of special structures) into a simple array
   of integers to give back to the Java program. */
void VCOLLIDE_VCJavaWidget_Report(struct HVCOLLIDE_VCJavaWidget *javaObj,
				  HArrayOfInt *JAVAcollisions)
{
  /* vc (the pointer to the VCOLLIDE instance) is stored on the Java
     side and will be retrieved. */
  void *vc = (void *)unhand(javaObj)->vcPtr;

  /* maxNumObjs is stored on the Java side and will be retrieved. */
  int maxNumObjs = unhand(javaObj)->maxNumObjs;

  /* vcCollisions is stored on the Java side and will be retrieved. 
     It is an array of structures defined by VCOLLIDE. */
  VCReportType *vcCollisions = (VCReportType *)unhand(javaObj)->vcColPtr;

  /* javaCollisions is stored on the Java side and will be retrieved. 
     It is an array of integers; the ids of colliding objects are
     listed pairwise in the array. It is needed since I don't want to
     attempt to parse the structured vcCollisions array on the Java
     side. */
  int *javaCollisions = (int *)unhand(JAVAcollisions)->body;
  
  /* number of colliding pairs will be determined by VCOLLIDE. */
  int no_of_colliding_pairs;
 
  /* i and j are generic counters. */
  int i, j;

  /* Report collisions into vcCollisions array. */
  no_of_colliding_pairs = vcReport(vc, maxNumObjs*maxNumObjs, vcCollisions);

  /* Clear out javaCollisions array. */
  for (i = 0; i < maxNumObjs * maxNumObjs; i++)
    javaCollisions[i] = -1;

  /* Copy collisions from vcCollisions array into javaCollisions array. */
  j = 0;
  for (i = 0; i < no_of_colliding_pairs; i++)
    {
      javaCollisions[j] = vcCollisions[i].id1;
      javaCollisions[j+1] = vcCollisions[i].id2;
      j += 2;
    }
}






