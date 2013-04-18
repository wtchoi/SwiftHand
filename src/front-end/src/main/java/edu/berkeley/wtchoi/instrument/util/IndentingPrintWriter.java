package edu.berkeley.wtchoi.instrument.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.PrintWriter;
import java.io.Writer;

/**
 * A helper class for printing indented text
 *
 * @version $Revision: 1.2 $
 */
public class IndentingPrintWriter extends PrintWriter{

    private int indentLevel;
    private String indent;

    public IndentingPrintWriter() {
        this(new PrintWriter(System.out), "  ");
    }

    public IndentingPrintWriter(Writer out) {
        this(out, "  ");
    }

    public IndentingPrintWriter(Writer out, String indent) {
        super(out);
        this.indent = indent;
    }

    public void println(Object value) {
        super.print(value.toString());
        super.println();
    }

    public void println(String text) {
        super.print(text);
        super.println();
    }

    public void print(String text) {
        super.print(text);
    }

    public void printIndent() {
        for (int i = 0; i < indentLevel; i++) {
            super.print(indent);
        }
    }

    public void printnln(String s){
        this.printIndent();
        this.println(s);
    }

    public void println() {
        super.println();
    }

    public void incrementIndent() {
        ++indentLevel;
    }

    public void decrementIndent() {
        --indentLevel;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }

    public void flush() {
        super.flush();
    }
}
