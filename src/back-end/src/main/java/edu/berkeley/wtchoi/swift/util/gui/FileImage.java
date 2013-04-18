package edu.berkeley.wtchoi.swift.util.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 6:30 PM
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
public class FileImage{
    protected String path;
    protected BufferedImage image;
    protected BufferedImage imageF;
    protected BufferedImage imageS;
    protected ImageIcon icon;

    protected int scaleX;
    protected int scaleY;
    protected boolean preserveRatio = true;
    protected boolean untouched = true;
    protected boolean scaleRequired = false;


    public FileImage(String path){
        this.path = path;
    }

    final public void reload(){
        reloadImage();
        icon = null;
    }

    private void reloadImage(){
        try{
            java.io.File file = new java.io.File(path);
            image = ImageIO.read(file);
            imageF = filtering();
            delayedScale();
        }
        catch(Exception e){}
    }

    private void delayedScale(){
        scaleRequired = true;
    }

    final public BufferedImage getImage(){
        if(scaleRequired){
            if(!untouched){
                imageS = ImageUtil.getScaleImage(imageF,scaleX,scaleY,preserveRatio);
            }else{
                imageS = imageF;
            }
            scaleRequired = false;
        }
        return imageS;
    }

    final public ImageIcon getIcon(){
        if(icon == null) icon = new ImageIcon(getImage());
        return icon;
    }

    final public void scale(int tx, int ty, boolean preserveRatio){
        scaleImage(tx,ty,preserveRatio);
        icon = null;
    }

    private void scaleImage(int tx, int ty, boolean  preserveRatio){
        scaleX = tx;
        scaleY = ty;
        this.preserveRatio = preserveRatio;
        untouched = false;

        delayedScale();
    }

    public void undoScale(){
        untouched = true;
        imageS = imageF;
        icon = null;
        scaleRequired = false;
    }

    final public int getWidth(){ return getImage().getWidth();}
    final public int getHeight(){ return getImage().getHeight();}

    protected BufferedImage filtering(){
        return image;
    }
}
