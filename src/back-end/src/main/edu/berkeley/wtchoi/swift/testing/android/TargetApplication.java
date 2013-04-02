package edu.berkeley.wtchoi.swift.testing.android;

import edu.berkeley.wtchoi.swift.driver.DriverPacket;
import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;
import edu.berkeley.wtchoi.swift.driver.ViewInfo;
import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.logger.Logger;
import edu.berkeley.wtchoi.swift.driver.Device;
import edu.berkeley.wtchoi.swift.driver.Driver;
import edu.berkeley.wtchoi.swift.driver.DriverOption;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.TargetProgram;

import java.util.LinkedList;
import java.util.Set;


public class TargetApplication implements TargetProgram<AppRequest, AppResult, AppState> {
    private static Device device;
    private Driver driver;
    private AppState currentState;
    private AppState initState;

    private boolean mStatePrint = false;

    private ApkInfo apkInfo;

    private TargetObserver observer = new TargetObserver() {
        @Override
        public void onInitialCoverage(ProgramPointSet coverage){}
        public void onResetBegin() {}
        public void onResetEnd() {}
        public void onCommandBegin() {}
        public void onCommandEnd() {}
    };

    public static interface TargetObserver{
        public void onInitialCoverage(ProgramPointSet coverage);
        public void onResetBegin();
        public void onResetEnd();
        public void onCommandBegin();
        public void onCommandEnd();
    }

    public void setObserver(TargetObserver obs){
        observer = obs;
    }

    public void setApkInfo(ApkInfo ai){
        apkInfo = ai;
    }

    private TargetApplicationRecorder recorder = null;
    private TargetApplicationRecord record = null;

    public static interface TargetApplicationRecorder{
        public void append(AppRequest request, AppResult result);
        public void commit();
        public void close();
    }

    public static interface TargetApplicationRecord{
        public AppResult getNext();
        public boolean hasNext();
        public boolean checkConsistency(AppRequest request);
    }

    public void setRecorder(TargetApplicationRecorder r){
        recorder = r;
    }

    public void setRecord(TargetApplicationRecord r){
        record = r;
    }

    public void useStatePrint(){
        mStatePrint = true;
    }

    public static class Option{
        public String pkg;
        public String mainActivity;
        public String binaryPath;
        public boolean flagTraceLogging = false;
        public String deviceID;
        public int localPort;
        public int deviceObservationTickInterval;
        public int transitionTimeout;
    }

    public TargetApplication(Option appOption){
        DriverOption driverOption = new DriverOption();
        driverOption.getAdbPathFromEnvironment();
        driverOption.setTraceLogging(appOption.flagTraceLogging);
        driverOption.setApplicationBinary(appOption.binaryPath);
        driverOption.setDeviceObservationTickInterval(appOption.deviceObservationTickInterval);
        driverOption.setTransitionTimeout(appOption.transitionTimeout);

        if(appOption.pkg != null && appOption.mainActivity != null){
            driverOption.setApplicationPackage(appOption.pkg);
            driverOption.setMainActivity(appOption.mainActivity);
        }

        if(device == null){
            driver = Driver.connectToDevice(driverOption, appOption.deviceID, appOption.localPort);
            device = driver.getDevice();
        }
        else{
            driver = Driver.getDriverWithDevice(driverOption, device, appOption.localPort);
        }
        System.out.println("Target Application Initialized");

        if(driver == null) throw new RuntimeException("Cannot initiate driver");
    }

    public void setChannelTimeout(int msec){
        driver.setChannelTimeout(msec);
    }

    @Override
    public void init(){
        driver.startApp();

        LinkedList<DriverPacket.Type> options = new LinkedList<DriverPacket.Type>();
        options.add(DriverPacket.Type.RequestView);
        options.add(DriverPacket.Type.RequestCoverage);
        LinkedList<Object> results = driver.requestMultiple(options);

        ViewInfo vi = (ViewInfo)results.get(0);
        ProgramPointSet coverage = filter((ProgramPointSet) results.get(1));

        initState = AppState.createLiveState(vi);
        currentState = initState;
        statePrint();

        observer.onInitialCoverage(coverage);
    }

    private void statePrint(){
        if(mStatePrint){
            System.out.println("====== View =====");
            System.out.println(currentState.getViewInfo().toString());
            System.out.println("====== Palette ======");
            System.out.println(currentState.getPalette());
        }
    }

    @Override
    public void close(){
        driver.closeApplication();
        try{
            Thread.sleep(1000);
        }
        catch(Exception e){}
    }


    private AppResult queryRecord(AppRequest request){
        if(record == null) return null;
        if(!record.hasNext()) return null;
        if(!record.checkConsistency(request)) return null;
        return record.getNext();
    }

    @Override
    public AppResult execute(AppRequest request){
        AppResult fromRecord = queryRecord(request);
        if(fromRecord != null)
            return fromRecord;

        if(request.requestRestart()){
            observer.onResetBegin();

            if(request.requestReinstall)
                driver.removeApplication();

            driver.restartApp();
            currentState = initState;
            Logger.log("restart");
            observer.onResetEnd();
            statePrint();
        }

        CList<ICommand> sequence = request.getInputSequence();

        AppResult result = new AppResult(currentState);
        for(ICommand cmd:sequence){
            observer.onCommandBegin();
            if(checkFeasible(cmd)){
                try{
                    if(driver.go(cmd)){
                        updateResultLive(cmd, request, result);
                        statePrint();
                    }
                    else{
                        updateResultStop(cmd, request, result);
                        break;
                    }
                }
                catch(Exception e){
                    updateResultStop(cmd, request, result);
                    //currentState = AppState.createSuccessorStopState(currentState, cmd);
                    //result.addStop(cmd, currentState.copy());
                    break;
                }
            }
            else{
                result.setConflictFlag(true);
                break;
            }
            observer.onCommandEnd();
        }

        //record (request, result) pair
        if(recorder != null){
            recorder.append(request, result);
            recorder.commit();
        }

        return result;
    }

    private boolean checkFeasible(ICommand cmd){
        if(cmd.isManual()) return true;

        Set<ICommand> palette = ViewToEvents.getRepresentativePoints(currentState.getViewInfo());
        if(palette.contains(cmd))
            return true;
        return false;
    }

    private void updateResultStop(ICommand cmd, AppRequest request, AppResult result){
        ProgramPointSet coverage = requestCoverage();

        currentState = AppState.createSuccessorStopState(currentState, cmd);
        result.addStop(cmd, currentState.copy());

        result.updateCoverage(coverage);
    }

    private void updateResultLive(ICommand cmd, AppRequest request, AppResult result){
        LinkedList<Object> results = requestMultiple(request.requestTrace());

        ViewInfo vi = (ViewInfo)results.get(0);
        ProgramPointSet coverage = filter((ProgramPointSet) results.get(1));
        CompressedLog log = null;

        if(request.requestTrace())
            log = (CompressedLog) results.get(2);

        AppState s;
        if(vi != null)
            s = AppState.createSuccessorLiveState(currentState, cmd, vi);
        else
            s = AppState.createSuccessorStopState(currentState, cmd);

        result.add(cmd, s.copy(), log);
        currentState = s;
        result.updateCoverage(coverage);
    }

    private LinkedList<Object> requestMultiple(boolean requestTrace){
        LinkedList<DriverPacket.Type> options = new LinkedList<DriverPacket.Type>();
        options.add(DriverPacket.Type.RequestView);
        options.add(DriverPacket.Type.RequestCoverage);

        if(requestTrace)
            options.add(DriverPacket.Type.RequestCompressedLog);

        options.add(DriverPacket.Type.PrepareCommand);

        return driver.requestMultiple(options);
    }

    private ProgramPointSet requestCoverage(){
        LinkedList<DriverPacket.Type> options = new LinkedList<DriverPacket.Type>();
        options.add(DriverPacket.Type.RequestCoverage);

        LinkedList<Object> result = driver.requestMultiple(options);
        return (ProgramPointSet) result.get(0);
    }


    private ProgramPointSet filter(ProgramPointSet pps){
        final ProgramPointSet npps = new ProgramPointSet();

        pps.foreach(new ProgramPointSet.ProgramPointVisitor(){
            @Override
            public void visit(int bid, short mid){
                if(!apkInfo.checkExclude(mid))
                    npps.add(bid,mid);
            }
        });

        return npps;
    }


    @Override
    public AppState currentState(){
        return currentState.copy();
    }
}


