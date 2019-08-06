/* DO NOT EDIT THIS FILE - it is machine generated */
#include <native.h>
/* Header for class VCOLLIDE_VCJavaWidget */

#ifndef _Included_VCOLLIDE_VCJavaWidget
#define _Included_VCOLLIDE_VCJavaWidget

#pragma pack(4)

typedef struct ClassVCOLLIDE_VCJavaWidget {
    int64_t vcPtr;
    int64_t vcColPtr;
    struct HArrayOfInt *collisions;
    long maxNumObjs;
} ClassVCOLLIDE_VCJavaWidget;
HandleTo(VCOLLIDE_VCJavaWidget);

#pragma pack()

#ifdef __cplusplus
extern "C" {
#endif
extern void VCOLLIDE_VCJavaWidget_Open(struct HVCOLLIDE_VCJavaWidget *);
extern void VCOLLIDE_VCJavaWidget_Close(struct HVCOLLIDE_VCJavaWidget *);
extern long VCOLLIDE_VCJavaWidget_NewObject(struct HVCOLLIDE_VCJavaWidget *);
extern void VCOLLIDE_VCJavaWidget_AddTri(struct HVCOLLIDE_VCJavaWidget *,double,double,double,double,double,double,double,double,double);
extern void VCOLLIDE_VCJavaWidget_EndObject(struct HVCOLLIDE_VCJavaWidget *);
extern void VCOLLIDE_VCJavaWidget_DeleteObject(struct HVCOLLIDE_VCJavaWidget *,long);
extern void VCOLLIDE_VCJavaWidget_ActivateObject(struct HVCOLLIDE_VCJavaWidget *,long);
extern void VCOLLIDE_VCJavaWidget_DeactivateObject(struct HVCOLLIDE_VCJavaWidget *,long);
extern void VCOLLIDE_VCJavaWidget_ActivatePair(struct HVCOLLIDE_VCJavaWidget *,long,long);
extern void VCOLLIDE_VCJavaWidget_DeactivatePair(struct HVCOLLIDE_VCJavaWidget *,long,long);
extern void VCOLLIDE_VCJavaWidget_UpdateTrans(struct HVCOLLIDE_VCJavaWidget *,long,double,double,double,double,double,double,double,double,double,double,double,double,double,double,double,double);
extern void VCOLLIDE_VCJavaWidget_Collide(struct HVCOLLIDE_VCJavaWidget *);
extern void VCOLLIDE_VCJavaWidget_Report(struct HVCOLLIDE_VCJavaWidget *,HArrayOfInt *);
#ifdef __cplusplus
}
#endif
#endif