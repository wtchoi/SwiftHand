package edu.berkeley.wtchoi.swift.driver.drone;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import edu.berkeley.wtchoi.swift.driver.ViewComponentInfo;

import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 6:34 PM
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
public abstract class ViewTransformer {

    public static ViewComponentInfo fromView(View v){
        LinkedList<ViewComponentInfo> ch;
        if(v instanceof ViewGroup){
            ViewGroup vg = (ViewGroup)v;
            ch = new LinkedList<ViewComponentInfo>();
            for(int i =0;i<vg.getChildCount(); i++){
                ch.add(fromView(vg.getChildAt(i)) );
            }
        }
        else{
            ch = null;
        }

        ViewComponentInfo vi = new ViewComponentInfo(v.getLeft(), v.getTop(), v.getWidth(),v.getHeight(), v.getMeasuredWidth(), v.getMeasuredHeight(), ch);
        vi.setScroll(v.getScrollX(),v.getScrollY());

        vi.setId(v.getId());
        if(v instanceof  EditText){
            EditText et = (EditText) v;
            vi.setIsEditText(et.getEditableText() != null);
            vi.setTextContent(et.getText().toString());
            vi.setInputMethod(et.getInputType());
            vi.setInputMethodTarget(et.isInputMethodTarget());
        }
        vi.setIsContainer(v instanceof AbsListView);
        vi.setViewType(v.getClass().getName());

        //detect View with onClickListener
        if(Build.VERSION.SDK_INT >= 15){
            try{
                Class paratypes[] = {};
                Object args[] = {};
                Method method = v.getClass().getMethod("hasOnClickListeners", paratypes);
                Object retobj = method.invoke(v, args);
                vi.setHasOnClickListener((Boolean) retobj);
            }
            catch(Exception e){
                e.printStackTrace();
                vi.setHasOnClickListener(false);
            }
        }
        else{
            vi.setHasOnClickListener(true);
        }

        //detect View with isCheck
        //vi.setCameraDistance(v.getCameraDistance());
        vi.setDrawingTime(v.getDrawingTime());
        vi.setIsShown(v.isShown());
        vi.setFocus(v.hasFocus());
        vi.setFocusable(v.isFocusable());

        int[] location = new int[2];
        v.getLocationOnScreen(location);
        vi.setAbsolute(location[0], location[1]);


        //Determine which widget is able to click.
        //refer for detailed information, refer java.android.view.View.onTouchEvent()
        vi.setVisible(v.isEnabled() && v.isClickable());

        return vi;
    }
}
