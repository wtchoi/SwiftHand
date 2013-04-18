package edu.berkeley.wtchoi.swift.testing.android;

import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;
import edu.berkeley.wtchoi.swift.testing.DummyObserver;
import edu.berkeley.wtchoi.swift.util.TickListener;
import edu.berkeley.wtchoi.swift.util.statistics.SimpleTracker;
import edu.berkeley.wtchoi.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/27/12
 * Time: 3:27 PM
 *
 * SwiftHand Project follows BSD License
 *
 * [The "BSD license"]
 * Copyright (c) 2013 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class AppTestingObserver extends DummyObserver<AppRequest, AppResult, AppState> implements TargetApplication.TargetObserver{
    private ProgramPointSet coverage = new ProgramPointSet();
    private ApkInfo apkinfo;

    private SimpleTracker queryTracker = new SimpleTracker();
    private SimpleTracker commandTracker = new SimpleTracker();
    private SimpleTracker resetTracker = new SimpleTracker();
    private SimpleTracker decisionTracker = new SimpleTracker();
    private SimpleTracker reportTracker = new SimpleTracker();

    private TickListener intermediateDump = new TickListener(1) {
        @Override
        public void doAction() {
            try{
                dumpIntermediate();
            }
            catch(Exception e){
                throw new RuntimeException("Cannot dump intermediate", e);
            }
        }};


    private TickListener summaryDump = new TickListener(1) {
        public void doAction() {
            printSummary(new PrintWriter(new OutputStreamWriter(System.out)));
        }};

    protected String path;
    protected String testname;

    private AppTestingGuide guide;

    public void setGuide(AppTestingGuide guide){
        this.guide = guide;
    }

    public AppTestingObserver(String name){
        testname = name;
    }

    public void intermediateDumpCycle(int cycle){
        intermediateDump.setCycle(cycle);
    }

    public void summaryDumpCycle(int cycle){
        summaryDump.setCycle(cycle);
   }

    public void setDumpPath(String path){
        this.path = path;
        File f = new File(path);
        f.mkdirs();
    }

    public void setApkInfo(ApkInfo ai){
        apkinfo = ai;
    }

    @Override
    public void onBegin(){
        System.out.println("TEST START : " + testname);
    }

    @Override
    public void onDecisionBegin(){
        decisionTracker.start();
    }
    @Override
    public void onDecisionEnd(){
        decisionTracker.stop();
    }

    @Override
    public void onReportBegin(){
        reportTracker.start();
    }

    @Override
    public void onReportEnd(){
        reportTracker.stop();
    }


    @Override
    public void onTestItemBegin(){
        queryTracker.start();
    }

    @Override
    public void onTestItemEnd(AppRequest request, AppResult result){
        queryTracker.stop();
        coverage.addAll(result.getCoverage());
        queryTracker.attach(commandTracker.count());
        queryTracker.attach(coverage.size());

        intermediateDump.tick();
        summaryDump.tick();
    }

    @Override
    public void onMainTestEnd(){
        Logger.log("Test Finished");
    }

    @Override
    public void finish(){}

    //TargetApplicationObserver
    @Override
    public void onInitialCoverage(ProgramPointSet c){
        coverage.addAll(c);
    }

    @Override
    public void onCommandBegin(){
        commandTracker.start();
    }

    @Override
    public void onCommandEnd(){
        commandTracker.stop();
        commandTracker.attach(coverage.size());
    }

    @Override
    public void onResetBegin(){
        resetTracker.start();

    }

    @Override
    public void onResetEnd(){
        resetTracker.stop();
    }


    private void printSummary(PrintWriter writer){
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        DecimalFormat ff = new DecimalFormat("#.##");
        DecimalFormat fp = new DecimalFormat("#.##%");
        writer.println("===============================");
        writer.println(sdf.format(calender.getTime()));
        writer.println("Target            = " + testname);
        String className = guide.getClass().toString();
        writer.println("Guide             = " + className.substring(className.lastIndexOf(".") + 1));

        if(guide.getOptionString() != null){
            boolean flag = true;
            for(String str: guide.getOptionString()){
                if(flag){
                    writer.println("Guide Option      = " + str);
                    flag = false;
                }
                else{
                    writer.println("                    " + str);
                }
            }
        }

        long time = queryTracker.total();
        if(time < 1000)
            writer.println("Elapsed Time(ms)  = " + time);
        else if(time < 60000)
            writer.println("Elapsed Time(s)   = " + ff.format(((double) time) / 1000.0));
        else
            writer.println("Elapsed Time(m)   = " + ff.format(((double) time) / 60000.0));

        writer.println("#Covered Branch   = " + coverage.size());
        writer.println("#Branch           = " + apkinfo.getBranchCount());
        writer.println("#Query            = " + queryTracker.count());
        writer.println("#Events           = " + commandTracker.count());
        writer.println("#Reset            = " + resetTracker.count());
        writer.println("Query Average(ms) = " + ff.format(queryTracker.average()));
        writer.println("Event Average(ms) = " + ff.format(commandTracker.average()));
        writer.println("Reset Average(ms) = " + ff.format(resetTracker.average()));

        double executionSum = commandTracker.total() + resetTracker.total();
        double commandRate = 100.0 * commandTracker.total() / executionSum;
        double resetRate =  100.0 * resetTracker.total() / executionSum;
        double decisionRate = ((double)(decisionTracker.total() + reportTracker.total())) / ((double) queryTracker.total());
        writer.println("Execution Rate    = " + fp.format(executionSum / ((double) queryTracker.total())));
        writer.println("Events : Reset    = " + ff.format(commandRate) + " : " + ff.format(resetRate));
        writer.println("Decision Rate     = " + fp.format(decisionRate));
        writer.println("===============================");
        writer.flush();
    }

    public void dumpFinalResult(){
        try{
            printSummary(new PrintWriter(new File(path + "/" + testname + ".summary")));
            commandTracker.dump(path + "/" + testname + ".progress");
            coverage.dump(path + "/" + testname + ".coverage");
            dumpOptional();
            dumpUncovered();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Cannot make a log file");
        }
    }

    public void dumpUncovered(){
        try{
            final PrintWriter uncoveredWriter = new PrintWriter(new File(path + "/" + testname + ".uncovered"));
            final PrintWriter coveredWriter = new PrintWriter(new File(path + "/" + testname + ".covered"));

            final TreeSet<Short> handledMethods = new TreeSet<Short>();

            apkinfo.decisionPointSet.foreach(new ProgramPointSet.ProgramPointVisitor() {

                boolean namePrintedUncovered;
                boolean namePrintedCovered;

                @Override
                public void visit(int bid, short mid) {
                    if(apkinfo.checkExclude(mid)) return;
                    if(handledMethods.contains(mid)) return;
                    handledMethods.add(mid);

                    final String name = apkinfo.methodMap.get(mid);
                    final short targetMid = mid;
                    namePrintedUncovered = false;
                    namePrintedCovered = false;

                    coverage.foreach(new ProgramPointSet.ProgramPointVisitor() {
                        @Override
                        public void visit(int bid, short mid) {
                            if(mid != targetMid) return;
                            if(!namePrintedCovered){
                                coveredWriter.write(name + "(" + mid + ") : ");
                                namePrintedCovered = true;
                            }
                            coveredWriter.write(bid + " ");
                        }
                    });

                    if(namePrintedCovered)
                        coveredWriter.write("\n");

                    apkinfo.decisionPointSet.foreach(new ProgramPointSet.ProgramPointVisitor(){
                        @Override
                        public void visit(int bid, short mid) {
                            if(mid != targetMid) return;
                            if(!coverage.contains(bid, mid)){
                                if(!namePrintedUncovered){
                                    uncoveredWriter.write(name + "(" + mid + ") : ");
                                    namePrintedUncovered = true;
                                }
                                uncoveredWriter.write(bid + " ");
                            }
                        }
                    });

                    if(namePrintedUncovered)
                        uncoveredWriter.write("\n");
                }
            });
            uncoveredWriter.flush();
            coveredWriter.flush();
        }
        catch(Exception e){
            throw new RuntimeException("Cannot make a log file", e);
        }
    }

    protected void dumpOptional() throws IOException {}
    protected void dumpIntermediate() throws IOException {}
    protected void dumpException(Throwable e) throws IOException{}


    @Override
    public void onEnd(){
        dumpFinalResult();
        System.out.println("TEST END : " + testname);
    }

    @Override
    public void onException(Throwable e){
        try{
            System.out.println("Error!");
            e.printStackTrace();
            dumpFinalResult();
            dumpException(e);
        }
        catch(Exception ee){
            System.out.println("Cannot make a log file");
        }
    }
}



