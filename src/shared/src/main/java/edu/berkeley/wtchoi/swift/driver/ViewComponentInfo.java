package edu.berkeley.wtchoi.swift.driver;

import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.ExtendedComparable;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/27/12
 * Time: 5:50 AM
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
public class ViewComponentInfo implements Comparable, Externalizable {
    /**
     *
     */
    private static final long serialVersionUID = -5186309675577891457L;

    public LinkedList<ViewComponentInfo> children;

    //basic properties
    private int x; //left
    private int y; //top
    private int width;
    private int height;
    private int measuredWidth;
    private int measuredHeight;
    private int scrollX = 0; //View.mScrollX;
    private int scrollY = 0; //View.mScrollY;
    private int absoluteX = 0; //Absolute position on Screen;
    private int absoluteY = 0; //Absolute position on Screen;
    private float cameraDistance = 0; //z-axis value

    //additional properties
    private boolean visible = false;
    private long drawingTime = 0; //last time view is drawn
    private boolean isShown = false; //whether view is shown or not
    private boolean hasFocus = false;
    private boolean focusable = false;
    private boolean hasOnClickListener = false;

    private String viewType;
    private String textContent;
    private boolean isEditText = false;
    private boolean isInputMethodTarget = false;
    private boolean isContainer = false;
    private int inputMethod;

    transient private boolean isCollectionMember = false;
    transient private boolean isSpinner = false;




    private int id;

    @Override
    public void writeExternal(ObjectOutput o) throws IOException{
        o.writeLong(serialVersionUID);
        writeTo(o);
    }

    private void writeTo(ObjectOutput o) throws IOException{
        o.writeInt(x) ;
        o.writeInt(y);
        o.writeInt(width);
        o.writeInt(height);
        o.writeInt(measuredWidth);
        o.writeInt(measuredHeight);
        o.writeInt(scrollX);
        o.writeInt(scrollY);
        o.writeInt(absoluteX);
        o.writeInt(absoluteY);
        o.writeFloat(cameraDistance);
        o.writeBoolean(visible);
        o.writeLong(drawingTime);
        o.writeBoolean(isShown);
        o.writeBoolean(hasFocus);
        o.writeBoolean(focusable);
        o.writeBoolean(hasOnClickListener);
        o.writeObject(viewType);
        o.writeObject(textContent);
        o.writeBoolean(isEditText);
        o.writeBoolean(isInputMethodTarget);
        o.writeBoolean(isContainer);
        o.writeInt(inputMethod);
        o.writeInt(id);

        if(children != null){
            o.writeInt(children.size());
            for(ViewComponentInfo child:children){
                child.writeTo(o);
            }
        }
        else{
            o.writeInt(0);
        }

    }

    @Override
    public void readExternal(ObjectInput o) throws IOException, ClassNotFoundException{
        if(o.readLong() != serialVersionUID)
            throw new RuntimeException("Version miss match!");

        readFrom(o, this);
    }

    private static void readFrom(ObjectInput o, ViewComponentInfo v) throws IOException, ClassNotFoundException{
        v.x = o.readInt() ;
        v.y = o.readInt();
        v.width = o.readInt();
        v.height = o.readInt();
        v.measuredWidth = o.readInt();
        v.measuredHeight = o.readInt();
        v.scrollX = o.readInt();
        v.scrollY = o.readInt();
        v.absoluteX = o.readInt();
        v.absoluteY = o.readInt();
        v.cameraDistance = o.readFloat();
        v.visible = o.readBoolean();
        v.drawingTime = o.readLong();
        v.isShown = o.readBoolean();
        v.hasFocus = o.readBoolean();
        v.focusable = o.readBoolean();
        v.hasOnClickListener = o.readBoolean();
        v.viewType = (String) o.readObject();
        v.textContent = (String)o.readObject();
        v.isEditText = o.readBoolean();
        v.isInputMethodTarget = o.readBoolean();
        v.isContainer = o.readBoolean();
        v.inputMethod = o.readInt();
        v.id = o.readInt();

        int size = o.readInt();
        if(size != 0){
            v.children = new LinkedList<ViewComponentInfo>();
            for(int i = 0;i<size;i++){
                ViewComponentInfo c = new ViewComponentInfo();
                readFrom(o, c);
                v.children.add(c);
            }
        }
    }


    public ViewComponentInfo(){}

    @SuppressWarnings("unchecked")
    public ViewComponentInfo(int ix, int iy, int iw, int ih, int mw, int mh, LinkedList<ViewComponentInfo> ic) {
        x = ix;
        y = iy;
        width = iw;
        height = ih;
        measuredWidth = mw;
        measuredHeight = mh;


        if (ic != null) children = (LinkedList<ViewComponentInfo>) ic.clone();
        else children = null;
    }

    public void setVisible(boolean flag){
        visible = flag;
    }

    public void setScroll(int x, int y){
        scrollX = x;
        scrollY = y;
    }

    public void setAbsolute(int x, int y){
        absoluteX = x;
        absoluteY = y;
    }

    public void setViewType(String vt){
        viewType = vt;
    }

    public void setHasOnClickListener(boolean t){
        hasOnClickListener = t;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return width;
    }

    public int getH() {
        return height;
    }

    public int getSX(){
        return scrollX;
    }

    public int getSY(){
        return scrollY;
    }

    public int getAX(){
        return absoluteX;
    }

    public int getAY(){
        return absoluteY;
    }


    public String toString() {
        java.io.StringWriter sw = new java.io.StringWriter();
        BufferedWriter buffer = new BufferedWriter(sw);
        try {
            ViewComponentInfo.toString(buffer, this, 0);
            buffer.flush();
        } catch (Exception e) {
            System.out.println("Error occur!");
        }

        return sw.toString();
    }

    private static void toString(Writer buffer, ViewComponentInfo mv, int depth) throws java.io.IOException {
        for (int i = 0; i < depth; i++) {
            buffer.write("  ");
        }

        buffer.write(mv.viewType + " ");
        buffer.write(mv.visible?"V":"");
        buffer.write(mv.isShown?"S":"");
        buffer.write(mv.hasFocus?"F":"");
        buffer.write(mv.isInputMethodTarget?"T":"");
        buffer.write(mv.hasOnClickListener()?"L":"");
        buffer.write(mv.isContainer?"C":"");
        buffer.write(" ");
        buffer.write(String.valueOf(mv.drawingTime));
        buffer.write("<");
        buffer.write(Integer.toString(mv.x));
        buffer.write(",");
        buffer.write(Integer.toString(mv.y));
        buffer.write(",");
        buffer.write(Integer.toString(mv.width));
        buffer.write(",");
        buffer.write(Integer.toString(mv.height));
        buffer.write(",");
        buffer.write(Integer.toString(mv.measuredWidth));
        buffer.write(",");
        buffer.write(Integer.toString(mv.measuredHeight));
        buffer.write(",");
        buffer.write(Integer.toString(mv.scrollX));
        buffer.write(",");
        buffer.write(Integer.toString(mv.scrollY));
        buffer.write(",");
        buffer.write(Integer.toString(mv.absoluteX));
        buffer.write(",");
        buffer.write(Integer.toString(mv.absoluteY));
        buffer.write(">");

        if(mv.isEditText){
            buffer.write(String.valueOf(mv.getInputMethod()));
            buffer.write(" ");
            buffer.write(mv.getTextContent());
        }
        buffer.write("\n");

        if (mv.children == null) return;

        for (ViewComponentInfo child : mv.children) {
            toString(buffer, child, depth + 1);
        }
    }


    //Function to collect representative click positions for each equivalence class
    public <T extends ExtendedComparable<T>> CSet<T> getRepresentativePoints(PointFactory<T> factory) throws PointFactory.PFException {
        //Infer click points from view hierarchy
        TreeSet<Integer> grids_x = createDescendingIntegerSet();
        TreeSet<Integer> grids_y = createDescendingIntegerSet();
        this.collectAbsoluteGrid(grids_x, grids_y);

        extendGrids(grids_x);
        extendGrids(grids_y);

        Collection<T> values = generatePoints(grids_x, grids_y, factory, true);//.values();
        return new CSet<T>(values);
    }


    private void collectAbsoluteGrid(Collection<Integer> grids_x, Collection<Integer> grids_y){
        //if(this.width == 0 || this.height == 0) return;

        grids_x.add(this.absoluteX);
        grids_y.add(this.absoluteY);

        if(this.width > 0){
            grids_x.add(this.absoluteX + this.width - 1);
            grids_x.add(this.absoluteX + this.width + 1);
        }

        if(this.height > 0){
            grids_y.add(this.absoluteY + this.height - 1);
            grids_y.add(this.absoluteY + this.height + 1);
        }

        if(this.children != null)
            for(ViewComponentInfo child : this.children){
                child.collectAbsoluteGrid(grids_x, grids_y);
            }
    }

    public boolean checkHit(Integer aX, Integer aY){
        if (!(this.absoluteX <= aX)) return false;
        if (!(this.absoluteX + this.width > aX)) return false;
        if (!(this.absoluteY <= aY)) return false;
        if (!(this.absoluteY + this.height > aY)) return false;
        return true;
    }

    public ViewComponentInfo projectAbsoluteCoordinateRecursively(Integer ix, Integer iy){
        //Miss
        if(!checkHit(ix, iy)) return null;

        if(this.viewType.endsWith("Spinner")){
            this.isSpinner = true;
            return this;
        }

        //I'm hit and has child.
        if(this.children != null){
            //Assumption : children never intersect

            ViewComponentInfo projected_child;

            if(this.isContainer || this.viewType.equals("android.widget.TabWidget")){
                Vector<ViewComponentInfo> views = new Vector<ViewComponentInfo>();
                for (ViewComponentInfo child : children) {
                    if(child.checkHit(ix,iy)){
                        child.isCollectionMember = true;
                        views.add(child);
                    }
                }
                if(views.size() == 1){
                    return views.firstElement();
                }

            }
            if(this.viewType.endsWith(("FrameLayout"))){
                LinkedList<ViewComponentInfo> chlist = new LinkedList<ViewComponentInfo>(children);
                ViewComponentInfo child = getFrameContent(children);
                projected_child = child.projectAbsoluteCoordinateRecursively(ix, iy);
                if (projected_child != null)
                    return projected_child;
            }
            else{
                Vector<ViewComponentInfo> views = new Vector<ViewComponentInfo>();
                for (ViewComponentInfo child : children) {
                    projected_child = child.projectAbsoluteCoordinateRecursively(ix, iy);
                    if (projected_child != null) views.add(projected_child);
                }
                if(views.size() == 1){
                    return views.firstElement();
                }
            }
        }

        //The point hit no child.
        //Thus return my self.
        if(this.viewType.endsWith("Layout") || this.viewType.endsWith("TextView") || this.viewType.endsWith("ScrollView") || this.viewType.endsWith("ViewStub") || this.viewType.endsWith("DialogTitle") || this.viewType.endsWith("ImageView")){
            if(this.hasOnClickListener()) return this;
            else return null;
        }
        return this;
    }

    private ViewComponentInfo getFrameContent(LinkedList<ViewComponentInfo> views){
        for(ViewComponentInfo view: views){
            if(view.isShown) return view;
        }
        return null;
    }



    private static void extendGrids(TreeSet<Integer> grids) {
        TreeSet<Integer> inter_grids = createDescendingIntegerSet();

        Integer prev = 0;
        for (Integer cur : grids) {
            if (prev == 0 || prev + 1 == cur) {
                prev = cur;
                continue;
            }
            inter_grids.add((prev + cur) / 2);
            prev = cur;
        }
        grids.addAll(inter_grids);

    }

    private <T> Collection<T> generatePoints(TreeSet<Integer> grids_x, TreeSet<Integer> grids_y, PointFactory<T> factory, boolean collectExtremity) throws PointFactory.PFException{

        TreeMap<ViewComponentInfo, Collection<T>> map = new TreeMap<ViewComponentInfo, Collection<T>>();
        ViewComponentInfo hit;
        for (Integer x : grids_x) {
            for (Integer y : grids_y) {
                //System.out.println("("+x+","+y+")");
                hit = this.projectAbsoluteCoordinateRecursively(x, y);
                if (hit != null){
                    //Collection<T> iter = factory.get(x,y,hit);
                    map.put(hit, factory.get(x, y, hit));
                }
            }
        }

        LinkedList<T> lst = new LinkedList<T>();
        for(Map.Entry<ViewComponentInfo, Collection<T>> entry: map.entrySet()){
            for(T t:entry.getValue()){
                if(collectExtremity){
                    if(entry.getKey().children == null || entry.getKey().isCollectionMember || entry.getKey().isSpinner || entry.getKey().hasOnClickListener()){
                        lst.add(t);
                    }
                }
                else
                    lst.add(t);
            }
        }
        return lst;
    }


    private static Comparator<Integer> descendingOrderComparator = new Comparator<Integer>(){
        @Override
        public int compare(Integer a, Integer b){
            return a.compareTo(b);
        }
    };

    private static TreeSet<Integer> createDescendingIntegerSet(){
        return new TreeSet<Integer>(descendingOrderComparator);
    }

    @SuppressWarnings("unchecked")
    LinkedList<ViewComponentInfo> getChildren() {
        return (LinkedList<ViewComponentInfo>) children.clone();
    }

    @Override
    public int compareTo(Object o) {
        //return this.toString().compareTo(o.toString());
        return Integer.valueOf(System.identityHashCode(this)).compareTo(System.identityHashCode(o));
    }

    public static interface PointFactory<T> {
        //Point object generated from (x,y) position can b]e multiple
        public static class PFException extends Exception{}

        public Collection<T> get(int x, int y, ViewComponentInfo v) throws PFException;
    }

    public void setIsEditText(boolean f){
        isEditText = f;
    }

    public boolean isEditText(){
        return isEditText;
    }

    public void setTextContent(String s){
        textContent = s;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setIsShown(boolean f){
        isShown = f;
    }

    public void setDrawingTime(long drawingTime){
        this.drawingTime = drawingTime;
    }

    public void setCameraDistance(float distance){
        this.cameraDistance = distance;
    }

    public void setFocus(boolean f){
        hasFocus = f;
    }

    public void setFocusable(boolean f){
        focusable = f;
    }

    public void setInputMethod(int im){
        inputMethod = im;
    }

    public void setIsContainer(boolean f){
        isContainer = f;
    }

    public boolean hasFocus(){
        return this.hasFocus;
    }

    public boolean focusable(){
        return focusable;
    }

    public boolean hasOnClickListener(){
        return this.hasOnClickListener;
    }

    public String getViewType(){
        return viewType;
    }

    public int getId(){
        return this.id;
    }

    public String getTextContent(){
        return textContent;
    }

    public int getInputMethod(){
        return inputMethod;
    }

    public void setInputMethodTarget(boolean f){
        isInputMethodTarget = f;
    }

    public boolean isInputMethodTarget(){
        return isInputMethodTarget;
    }

    public boolean isVerticalScrollable(){
        return viewType.equals("android.widget.ListView") || viewType.equals("android.widget.ScrollView");
    }
}
