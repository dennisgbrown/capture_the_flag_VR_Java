Dennis Brown
MS Thesis Project
April 1998


------------------------------------------------------
Instructions for using this virtual environment system
------------------------------------------------------


----------
Background
----------

First read the implementation part of the thesis in thesis/thesis.doc (MS
Word 97 format) for background info, if you need to. 

You can find HTML versions of the paper and of the slide-show for the
defense at http://www.cs.unc.edu/~brownde/pvt/cb/index.html 
(see the section "Various Descriptions of My Project").

It also helps to understand how OpenGL and the VCOLLIDE packages work (see
the usual manuals for OpenGL; VCOLLIDE docs are on-line at 
http://www.cs.unc.edu/~geom/V_COLLIDE/).

The individual source files are thoroughly commented, and reading them,
along with reading the relavant parts of the thesis, and playing with the
sample application, you should get a good idea how my system works.


-----
Files
-----

This is what's in each directory:

  ve: the virtual environment Java package  
  ve/conman: the Connection Manager class
  ve/vobject: the Virtual Object classes
  ve/types: some classes used in ConMan and VObject

  capflag: the capture-the-flag Java package
  capflag/models: the *.tri models used in capture-the-flag
  capflag/mazes: the maze files used in capture-the-flag

  OpenGL: the OpenGL-for-Java Java package

  OpenGL4Java: all the stuff necessary to build the OpenGL package
  OpenGL4Java/OpenGL: the Java side of the OpenGL4Java package
  OpenGL4Java/CNativeCode: the C side of the OpenGL4Java package
  OpenGL4Java/CClassHeaders: stuff generated during the build process
  OpenGL4Java/CClassStubs: stuff generated during the build process

  VCOLLIDE: the VCOLLIDE-for-Java Java package

  VCJava: all the stuff necessary to build the VCOLLIDE package
  VCJava/VCOLLIDE: the Java side of the VCJava package
  VCJava/CNativeCode: the C side of the VCJava package
  VCJava/CClassHeaders: stuff generated during the build process
  VCJava/CClassStubs: stuff generated during the build process

  models: a few miscellaneous *.tri models
  
  thesis: stuff related to my thesis paper and defense talk

Within each directory, each source file has internal docs that should
explain what it does and how to use it. Really!


-------------------
Building Everything
-------------------

(1) Paths

First, you need the OpenGL4Java and VCJava paths added to your CLASSPATH
and LD_LIBRARY_PATH environment variables. If you always start the program
from the top-level directory (i.e., the one containing the *.sh scripts)
then this should work for you:

  CLASSPATH=.:OpenGL4Java:VCJava
  LD_LIBRARY_PATH=.:OpenGL4Java:VCJava

Next, you need to make sure you're using Java 1.1. If you're running on
tbone or redbud, the default version is 1.0 which won't work. 1.1 is
installed here: /afs/cs.unc.edu/home/brownde/RAspace/CosmoCode/usr/java/bin
so you should add that to your PATH environment variable.


(2)

From the top project directory, type "make all".

This builds several things:

  - the ve package (ve/conman, ve/vobject, ve/types)  
      * type "make vepackage" to just build this package
      * type "make veclean" to delete all the .class files

  - the capflag package
      * type "make flagpackage" to just build this package
      * type "make flagclean" to delete all the .class files

  - the OpenGL package
  - the VCOLLIDE package
      * type "make nmistuff" to just build these two packages
      * type "make nmiclean" to clean them out for rebuilding

Examining the makefile should give more detail as to how things are put
together.


-------------------------
Starting Capture-the-Flag
-------------------------

(1) Paths

First, you need the OpenGL4Java and VCJava paths added to your CLASSPATH
and LD_LIBRARY_PATH environment variables. If you always start the program
from the top-level directory (i.e., the one containing the *.sh scripts)
then this should work for you:

  CLASSPATH=.:OpenGL4Java:VCJava
  LD_LIBRARY_PATH=.:OpenGL4Java:VCJava

Next, you need to make sure you're using Java 1.1. If you're running on
tbone or redbud, the default version is 1.0 which won't work. 1.1 is
installed here: /afs/cs.unc.edu/home/brownde/RAspace/CosmoCode/usr/java/bin
so you should add that to your PATH environment variable.


(2) RMI Registry

The RMI Registry needs to be started next. Pick a port number the
Connection Manager and virtual objects will use. The default is 1099 if
you don't specify one. For example, if you chose port 8989, you'd type:
  % rmiregistry 8989 &

Since the rmiregistry is a background process, don't forget to kill it
when you're done working on the virtual environment. You only have to run
it once for each work session (i.e., you can restart ConMan many times
but you only have to start rmiregistry before the first time), but make
sure to kill it before you log out or else it will just sit there and tie
up that port.


(3) Connection Manager

The Connection Manager needs to be started on the same machine and from
the same x-terminal (for unknown reasons). It defaults to port 1099, and
you can specify a different port from the command line. Use the
"conman.sh" script to start it.
  % conman.sh 8989 

After it starts, it will say "ConMan bound in registry" and nothing more.
If it gives errors, kill it and the rmiregistry process, pick another
port, and try again. Sometimes it says it can't find a stub class or some
nonsense, and I never figured out why, because just trying it again
usually fixes it.


(4) Maze

Start a maze process. You can use the maze13.sh or maze7.sh scripts (for
size 13 and size 7 mazes, respectively). You should supply the
machine name and port number to the script. If not, it assumes
you're using this machine on port 1099 for the Connection Manager. 
You can't specify just a machine name or just a port--you must always
supply machine:port in that format. Example:
  % maze7.sh redbud:8989

If you look at maze7.sh, you will see that the Maze object has a lot of
options. They are:
  -serverName: machine:port to use (like above)
  -position: three doubles giving the x, y, and z coordinates of this
             object in the virtual world
  -lookat: a point the object is looking at. This determines which
           way it is facing in the virtual world. Give it three doubles
           for the x, y, and z coordinates of this point.
  -VUP: the object's UP vector; usually 0.0 1.0 0.0
  -mazeFile: the maze file to load. The filename should be relative to the
             directory from which you start the program, OR be an absolute
             filename.


(5) Player

Start a player process. You can use player17.sh (player 1 in the 7x7
maze), player217.sh (player 2 in the 7x7 maze), player113.sh (player 1 in
the 13x13 maze), or player213.sh (player 2 in the 13x13 maze).
You should supply the machine name and port number to the script. If not,
it assumes you're using this machine on port 1099 for the Connection
Manager. You can't specify just a machine name or just a port--you must
always supply machine:port in that format. Example:
  % player17.sh redbud:8989

If you look at player17.sh, you will see that the Player object has a lot of
options. They are:
  -serverName: machine:port to use (like above)
  -position: three doubles giving the x, y, and z coordinates of this
             object in the virtual world
  -lookat: a point the object is looking at. This determines which
           way it is facing in the virtual world. Give it three doubles
           for the x, y, and z coordinates of this point.
  -VUP: the object's UP vector; usually 0.0 1.0 0.0
  -focusRadius: the initial radius of the object's focus
  -nimbusRadius: the initial radius of the object's nimbus
  -geomFile: the .tri file giving this object's geometry. The filename
             should be relative to the directory from which you start the
             program, OR be an absolute filename.
  -objName: a string giving the name this object will have in the VE

To have a second player, start player27.sh or player213.sh (depending on
which maze size you're using) on the same machine or a different machine.

You can also start a "stealth" player, who views the maze from above,
using stealth7.sh or stealth13.sh.


(6) Flags

Start some flag processes. Use flags7.sh if using the 7x7 maze or
flags13.sh if using the 13x13 maze. Example:
  % flags7.sh redbud:8989

If you look at flags7.sh, you will see it starts two flags,
one good and one bad. flags13.sh starts four flags, one is
which is good. The Flag object has a lot of options. They are:
  -serverName: machine:port to use (like above)
  -position: three doubles giving the x, y, and z coordinates of this
             object in the virtual world
  -lookat: a point the object is looking at. This determines which
           way it is facing in the virtual world. Give it three doubles
           for the x, y, and z coordinates of this point.
  -VUP: the object's UP vector; usually 0.0 1.0 0.0
  -nimbusRadius: the initial radius of the object's nimbus
  -geomFile: the .tri file giving this object's geometry. The filename
             should be relative to the directory from which you start the
             program, OR be an absolute filename.
  -objName: a string giving the name this object will have in the VE

The focusRadius for the Flag object is fixed at a large negative number to
keep it from seeing anything.


This should start the simulation. You can start processes at any time and
have them join the simulation. You can kill the player processes and
they'll leave the simulation (use File->Quit). If you kill any other
processes, you'll mess up the Connection Manager. I never added graceful
exits to those object but it should be easy; it's just a matter of calling
ConMan.unregister(id); where id is the object's id number.


