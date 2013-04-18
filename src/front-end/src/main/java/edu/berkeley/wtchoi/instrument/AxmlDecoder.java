package edu.berkeley.wtchoi.instrument;

import edu.berkeley.wtchoi.instrument.DexProcessor.Manifest;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/12/13
 * Time: 11:40 AM
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
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
class AxmlDecoder extends AxmlVisitor {

    private static String androidNS = "http://schemas.android.com/apk/res/android";

    public AxmlDecoder(AxmlVisitor visitor){
        super(visitor);
    }

    public AxmlDecoder(){
        super();
    }

    public static class NodeDecoder extends NodeVisitor {
        private AxmlDecoder root;
        private NodeDecoder parent;
        private String namespace;
        private String name;
        private Object aux;

        private static boolean permissionHandled = false;
        private static boolean screeDensityHandled = false;


        private boolean hasActionMainAttr = false;
        private boolean hasCategoryLauncherAttr = false;


        public NodeDecoder(String namespace, String name, NodeDecoder parent, AxmlDecoder root, NodeVisitor nv){
            super(nv);
            this.namespace = namespace;
            this.name = name;
            this.parent = parent;
            this.root = root;
        }

        @Override
        public NodeVisitor visitChild(String namespace, String name){
            NodeVisitor nv = super.visitChild(namespace, name);
            return new NodeDecoder(namespace, name, this, root, nv);
        }

        @Override
        public void visitContentAttr(String namespace, String name, int resourceId, int type, Object obj){
            if(this.name == null){
                int x = 1;
            }
            //handle screen density
            if(this.name.compareTo("supports-screens") == 0
                    && namespace != null
                    && namespace.compareTo(androidNS) == 0
                    && name.compareTo("anyDensity") == 0){
                super.visitContentAttr(namespace, name, resourceId, type, true);
                screeDensityHandled = true;
            }
            else{
                super.visitContentAttr(namespace, name, resourceId, type, obj);
                if(this.name.compareTo("uses-permission") == 0
                        && namespace.compareTo(androidNS) == 0
                        && name.compareTo("name") == 0
                        && ((String)obj).compareTo("android.permission.INTERNET") == 0) {
                    permissionHandled = true;
                }
            }

            //check application
            if(this.name.compareTo("application") == 0
                    && namespace.compareTo(androidNS) == 0
                    && name.compareTo("name") == 0){
                root.manifest.appClassDefined = true;
                root.manifest.appClassName = (String)obj;
            }

            //check main activity
            if(this.parent != null && this.parent.name.compareTo("intent-filter") == 0){
                if(this.name.compareTo("action") == 0
                        && namespace.compareTo(androidNS) == 0
                        && name.compareTo("name") == 0
                        && ((String) obj).compareTo("android.intent.action.MAIN") == 0){
                    parent.parent.hasActionMainAttr = true;
                }
                if(this.name.compareTo("category") == 0
                        && namespace.compareTo(androidNS) == 0
                        && name.compareTo("name") == 0
                        && ((String) obj).compareTo("android.intent.category.LAUNCHER") == 0){
                    parent.parent.hasCategoryLauncherAttr = true;
                }
            }

            if(this.name.compareTo("activity") == 0
                    && namespace.compareTo(androidNS) == 0
                    && name.compareTo("name") == 0){
                aux = obj;
            }

            if(this.name.compareTo("manifest") == 0
                    && name.compareTo("package") == 0){
                root.manifest.pkgName = (String)obj;
            }
        }


        @Override
        public void visitContentEnd(){
            /*
            if(this.name.compareTo("supports-screens") == 0 && !screeDensityHandled){
                super.visitContentAttr("android", "anyDensity", 0x0101026c, AxmlVisitor.TYPE_INT_BOOLEAN, true);
                screeDensityHandled = true;
            }
            if(this.name.compareTo("uses-permission") == 0 && !permissionHandled){
                super.visitContentAttr("android", "name", 0x01010003, AxmlVisitor.TYPE_STRING, "android.permission.INTERNET");
                permissionHandled = true;
            }
            */
            //add mock application
            if(this.name.compareTo("application") == 0 && !root.manifest.appClassDefined){
                super.visitContentAttr(androidNS,"name", 0x01010003, AxmlVisitor.TYPE_STRING, ".MockApplication");
                root.manifest.appClassName = "MockApplication";
            }
            super.visitContentEnd();
        }



        @Override
        public void visitEnd(){
            if(name.compareTo("manifest") == 0 ){
                if(!screeDensityHandled){
                    screeDensityHandled = true;
                    NodeVisitor nv = visitChild(null,"supports-screens");
                    if(nv != null){
                        nv.visitBegin();
                        nv.visitContentAttr(androidNS,"anyDensity", 0x0101026c, AxmlVisitor.TYPE_INT_BOOLEAN, true);
                        nv.visitContentEnd();
                        nv.visitEnd();
                    }
                }
                if(!permissionHandled){
                    permissionHandled = true;
                    NodeVisitor nv = visitChild(null, "uses-permission");
                    if(nv != null){
                        nv.visitBegin();
                        nv.visitContentAttr(androidNS,"name", 0x01010003, AxmlVisitor.TYPE_STRING, "android.permission.INTERNET");
                        nv.visitContentEnd();
                        nv.visitEnd();
                    }
                }
            }
            super.visitEnd();

            //check
            if(this.name.compareTo("activity") == 0 && hasActionMainAttr && hasCategoryLauncherAttr){
                root.manifest.mainActivityClassName = (String)this.aux;
            }
        }
    }

    public Manifest manifest = new Manifest();

    @Override
    public NodeVisitor visitFirst(String namespace, String name){
        NodeVisitor nv = super.visitFirst(namespace, name);
        return new NodeDecoder(namespace, name, null, this, nv);
    }
}
