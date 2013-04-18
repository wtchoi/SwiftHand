package edu.berkeley.wtchoi.swift.testing.android;

import edu.berkeley.wtchoi.swift.driver.ViewComponentInfo;
import edu.berkeley.wtchoi.swift.driver.ViewInfo;
import edu.berkeley.wtchoi.swift.driver.*;
import edu.berkeley.wtchoi.swift.driver.ViewComponentInfo.PointFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:50 PM
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



public class ViewToEvents{

    public static class InputMethodActivated extends PointFactory.PFException{}

    private static TouchFactory instance;
    private static Collection<ICommand> defaultEvents;

    private ViewToEvents(){}

    private static void collectScroll(ViewComponentInfo vi, Set<ICommand> inputVector){
        if(vi.isVerticalScrollable() && vi.getH() > 0 && vi.getW() > 0){
            DragCommand down =(DragCommand.getUpScorll(vi.getAX() + 1, vi.getAY() + 1, vi.getAX() + 1, vi.getAY() + vi.getH() - 2));
            DragCommand up = (DragCommand.getDownScorll(vi.getAX() + 1, vi.getAY() + 1, vi.getAX() + 1, vi.getAY() + vi.getH() - 2));

            up.setWidgetType(vi.getViewType());
            down.setWidgetType(vi.getViewType());

            inputVector.add(up);
            inputVector.add(down);
            return;
        }

        if(vi.children != null){
            for(ViewComponentInfo child : vi.children)
                collectScroll(child, inputVector);
        }
    }

    public static Set<ICommand> getRepresentativePoints(ViewInfo viewInfo){
        try{
            Set<ICommand> palette = viewInfo.getViewRoot().getRepresentativePoints(ViewToEvents.getInstance());
            if(defaultEvents != null) palette.addAll(defaultEvents);
            collectScroll(viewInfo.getViewRoot(), palette);
            return palette;
        }
        catch(InputMethodActivated e){
            Set<ICommand> palette = new TreeSet<ICommand>();
            palette.add(PushCommand.getBack());
            return palette;
        }
        catch(Exception ignore){
            throw new RuntimeException("Unreachable");
        }
    }

    public static <T extends Set<ICommand>> T getRepresentativePoints(ViewInfo viewInfo, T collection){
        try{
            Set<ICommand> palette = viewInfo.getViewRoot().getRepresentativePoints(ViewToEvents.getInstance());
            collection.addAll(palette);
            if(defaultEvents != null) collection.addAll(defaultEvents);
            collectScroll(viewInfo.getViewRoot(), collection);
        }
        catch(InputMethodActivated e){
            collection.add(PushCommand.getBack());
        }
        catch(Exception halt){
            throw new RuntimeException("Unreachable", halt);
        }
        return collection;
    }

    public static void setDefaultEvents(Collection<ICommand> events){
        defaultEvents = new LinkedList<ICommand>();
        defaultEvents.addAll(events);
    }

    private static TouchFactory getInstance() {
        if(instance == null){
            instance = new TouchFactory();
        }
        return instance;
    }
}


