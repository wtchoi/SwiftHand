package edu.berkeley.wtchoi.swift.util.gui;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 6:52 PM
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
import javax.swing.JPanel;
import java.awt.*;

public class ScalingFilePanel extends JPanel{
    protected FileImage fimage;

    public ScalingFilePanel(FileImage fi){
        super();
        fimage = fi;
        this.setBackground(Color.WHITE);
    }

    public void reload(){
        fimage.reload();
    }

    @Override
    public void paintComponent(Graphics g){
        int tw = getWidth();
        int th = getHeight();
        fimage.undoScale();
        if(fimage.getWidth() >= tw || fimage.getHeight() >= th){
            fimage.scale(tw,th,true);
        }
        this.setBackground(Color.WHITE);
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int x = (tw - fimage.getWidth())/2;
        int y = (th - fimage.getHeight())/2;
        g2.drawImage(fimage.getImage(), x,y, fimage.getWidth(), fimage.getHeight(), null);
        //TODO: Proper Centering
        //TODO: How to color backgroun properly?
    }
}
