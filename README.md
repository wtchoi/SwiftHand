Introduction
=============
*SwiftHand* is an automated Android GUI testing tool. The tool uses machine learning to
learn a model of the target app during testing, uses the learned model to generate user inputs that visit
unexplored states of the app, and uses the execution of the app on the generated inputs to refine
the model.

The main purpose of the repository is to provide working demo of the latest version of the
*SwiftHand*. The repository provides ready-instrumented benchmakrs and back-end(testing engine) binary.
Also source code for both front-end(instrumentation) and back-end is available.  Currently, maven build
script for the back-end is available. A script for the front-end will be added soon.



How to Use Compiled Back-End 
============================

#### Step 1. Install Android SDK (ADK)
Download from http://developer.android.com/sdk/index.html. We recomment to use SDK 4.1.2 or higher version. After install ADK, please make sure that you have following five files.
 
- ADK_ROOT/platform-tools/adb
- ADK_ROOT/tools/lib/ddms.jar
- ADK_ROOT/tools/lib/ddmlib.jar
- ADK_ROOT/tools/lib/chimpchat.jar
- ADK_ROOT/tools/lib/guava-13.0.1.jar



#### Step 2. Create Emulator
Do following steps to create Android Vritual Device. The steps will create emulator image.

1. Luanch SDK Manager (ADK_ROOT/tools/android)
2. Select 'Tools' -> 'Manage AVDs'
3. Push 'New' button to create emulator.
To reproduce the experimental result in the paper, We recommand to use 'Galaxy S' as a device, 'API Level 16' as a target, 'ARM' as a CPU/API. This step creates Android Virtual Device (AVD) image. 

You can find the official guideline about emulator management:
'http://developer.android.com/tools/devices/managing-avds.html'. 


#### Step 3. Start Emulator
We recommand following command to setup necessary environment variable:
```
<ADK_ROOT>/tools/emulator -avd <AVD_NAME> -wipe-data -dns-server 127.0.0.1
```
-wipe-data options it to start emulator from the factory-reset state -dns-server 127.0.0.1 option is a trick to disabling internet access. <AVD_NAME> refer the name of AVD created by step 2.


#### Step 4. Download SiwftHand Tool and benchmark programs.
You can download it from /dist directory of this repository.


#### Step 5. Set environment variables. Following command will do the work.
```
export ADK_ROOT=<ADK_ROOT>
export ADK_LIB="$ADK_ROOT/tools/lib"
export CLASSPATH="$ADK_LIB/ddml.jar:$ADK_LIB/ddmlib.jar:$ADK_LIB/chimpchat.jar:$ADK_LIB/guava-13.0.1.jar:<SWIFTHAND_DIR>/SwiftHand.jar:$CLASSPATH"
```

#### Step 6. Execute Tool
The tool can be executed using following command..
```
java edu.berkeley.wtchoi.swift.CommandLine
``` 
For example, to test mininote using SwiftHand with random seed 0 for 1 hour:
```
java edu.berkeley.wtchoi.swift.CommandLine benchmark/mininote.modified.apk swift 3600 0 <OUTPUT_DIR> 
```


How to Compile and Run
======================
#### Step 1. Install Maven.
Maven (http://maven.apache.org) is a project management and comprehension tool.


#### Step 2. Set up environment variables.
Build script requires *ADK_ROOT* environemtn variable. Set the variable to the path of your
installed Android SDK.

#### Step 3. Build
Type following command to compile the project
```
cd src
mvn package
```
If build process is succesfull, you will find following files:
```
back-end/target/back-end-0.1-jar-with-dependencies.jar
```

#### Step 4. Execute Tool
With compiled jar, executing the testing tool is much simpler.
```
java -jar back-end-0.1-jar-with-dependencies.jar benchmark/mininote.modified.apk swift 3600 0 <OUTPUT_DIR>
```
We assume that a running emulator (or phone) is connected to ADB. If not, please create and boot
an emulator before start testing.


 


