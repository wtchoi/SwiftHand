How to Run 
==========

#### Step 1. Install Android SDK (ADK)
You can download from http://developer.android.com/sdk/index.html We recomment to use SDK 4.1.2 or higher version. After install ADK, please make sure that you have following five files.
 
- ADK_ROOT/platform-tools/adb
- ADK_ROOT/tools/lib/ddms.jar
- ADK_ROOT/tools/lib/ddmlib.jar
- ADK_ROOT/tools/lib/chimpchat.jar
- ADK_ROOT/tools/lib/guava-13.0.1.jar



#### Step 2. Create Emulator
1. Luanch SDK Manager (ADK_ROOT/tools/android)
2. Select 'Tools' -> 'Manage AVDs'
3. Push 'New' button to create emulator.
To reproduce the experimental result in the paper, We recommand to use 'Galaxy S' as a device, 'API Level 16' as a target, 'ARM' as a CPU/API. This step creates Android Virtual Device (AVD) image. 

You can find the official guideline about emulator management:
'http://developer.android.com/tools/devices/managing-avds.html'. 


#### Step 3. Start Emulator
We recommand following command:
```
<ADK_ROOT>/tools/emulator -avd <AVD_NAME> -wipe-data -dns-server 127.0.0.1
```
-wipe-data options it to start emulator from the factory-reset state -dns-server 127.0.0.1 option is a trick to disabling internet access. <AVD_NAME> refer the name of AVD created by step 2.

Step 4. Download SiwftHand Tool and benchmark program.
You can download it from /dist directory of this repository.

Step 5. Set environment variables. Following command will do the work.
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
java edu.berkeley.wtchoi.swift.CommandLine mininote.apk swift 3600 0 <OUTPUT_DIR> 
```



