package edu.berkeley.wtchoi.swift.testing.android.lstar.ctree;

import edu.berkeley.wtchoi.swift.util.gui.FileImage;
import edu.berkeley.wtchoi.swift.util.gui.ScrollableFileLabel;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/14/12
 * Time: 9:53 PM
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

public class CTreeViewer implements Runnable{
    CTree tree;
    ScrollableFileLabel picture;
    JFrame frame;

    public CTreeViewer(CTree ctree){
        tree = ctree;

        //FileImage fimage = new ScaledFileImage("/tmp/out.gif",0.8);
        FileImage fimage = new FileImage("/tmp/out.gif");
        picture = new ScrollableFileLabel(fimage);

        JScrollPane scroller = new JScrollPane(picture);
        scroller.setAutoscrolls(true);
        scroller.setPreferredSize(new Dimension(800,550));

        frame = new JFrame("TreeView");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane();
        frame.getContentPane().add(scroller);
        frame.setSize(800,600);

        //frame = new JFrame("TreeView");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.add(picture);
    }

    public void run(){
        frame.setVisible(true);
    }

    public void reload(){
        tree.drawTree("/tmp/out.gif");
        picture.reload();
        frame.repaint();
    }
}

