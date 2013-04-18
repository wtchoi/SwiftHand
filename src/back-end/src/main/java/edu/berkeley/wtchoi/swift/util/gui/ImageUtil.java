package edu.berkeley.wtchoi.swift.util.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 7:26 PM
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
public abstract class ImageUtil {
    public static BufferedImage copyImage(BufferedImage target){
        ColorModel cm = target.getColorModel();
        boolean alpha = cm.isAlphaPremultiplied();
        WritableRaster raster = target.copyData(null);
        return new BufferedImage(cm, raster, alpha, null);
    }

    public static BufferedImage getScaleImage(BufferedImage image, int x, int y, boolean preserveRatio){
        if(image.getWidth() == x && image.getHeight() == y)
            return ImageUtil.copyImage(image) ;

        BufferedImage scaledImage = new BufferedImage(x,y, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        if(!preserveRatio){
            graphics2D.drawImage(image,0,0,x,y,null);
            return scaledImage;
        }

        int iw = image.getWidth();
        int ih = image.getHeight();
        double scaleX = (double) x / (double)iw;
        double scaleY = (double) y / (double)ih;
        double scale = Math.min(scaleX,scaleY);
        iw = (int)(scale * (double)iw);
        ih = (int)(scale * (double)ih);
        graphics2D.drawImage(image,0,0,iw,ih,null);
        return scaledImage;
    }
}
