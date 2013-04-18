package edu.berkeley.wtchoi.instrument.DexProcessor;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 8/11/12
 * Time: 4:01 PM
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
 * */
public enum Opcode {

    //Instructino Name(Opcode, Instruction Size (1 unit = 2 byte))
    NOP(0x00,1),
    MOVE(0x01,1),
    MOVE_FROM16(0x02,2),
    MOVE_16(0x03,3),
    MOVE_WIDE(0x04,1),
    MOVE_WIDE_FROM16(0x05,2),
    MOVE_WIDE_16(0x06,3),
    MOVE_OBJECT(0x07,1),
    MOVE_OBJECT_FROM16(0x08,2),
    MOVE_OBJECT_16(0x09,3),
    MOVE_RESULT(0x0a,1),
    MOVE_RESULT_WIDE(0x0b,1),
    MOVE_RESULT_OBJECT(0x0c,1),
    MOVE_EXCEPTION(0x0d,1),
    RETURN_VOID(0x0e,1),
    RETURN(0x0f,1),
    RETURN_WIDE(0x10,1),
    RETURN_OBJECT(0x11,1),
    CONST_4(0x12,1),
    CONST_16(0x13,2),
    CONST(0x14,3),
    CONST_HIGH16(0x15,2),
    CONST_WIDE_16(0x16,2),
    CONST_WIDE_32(0x17,3),
    CONST_WIDE(0x18,5),
    CONST_WIDE_HIGH16(0x19,2),
    CONST_STRING(0x1a,2),
    CONST_STRING_JUMBO(0x1b,3),
    CONST_CLASS(0x1c,2),
    MONITOR_ENTER(0x1d,1),
    MONITOR_EXIT(0x1e,1),
    CHECK_CAST(0x1f,2),
    INSTANCE_OF(0x20,2),
    ARRAY_LENGTH(0x21,1),
    NEW_INSTANCE(0x22,2),
    NEW_ARRAY(0x23,2),
    FILLED_NEW_ARRAY(0x24,3),
    FILLED_NEW_ARRAY_RANGE(0x25,3),
    FILL_ARRAY_DATA(0x26,3),
    THROW(0x27,1),
    GOTO(0x28,1),
    GOTO_16(0x29,2),
    GOTO_32(0x2a,3),
    PACKED_SWITCH(0x2b,3),
    SPARSE_SWITCH(0x2c,3),
    CMPL_FLOAT(0x2d,2),
    CMPG_FLOAT(0x2e,2),
    CMPL_DOUBLE(0x2f,2),
    CMPG_DOUBLE(0x30,2),
    CMP_LONG(0x31,2),
    IF_EQ(0x32,2),
    IF_NE(0x33,2),
    IF_LT(0x34,2),
    IF_GE(0x35,2),
    IF_GT(0x36,2),
    IF_LE(0x37,2),
    IF_EQZ(0x38,2),
    IF_NEZ(0x39,2),
    IF_LTZ(0x3a,2),
    IF_GEZ(0x3b,2),
    IF_GTZ(0x3c,2),
    IF_LEZ(0x3d,2),
    UNUSED_3E(0x3e,1),
    UNUSED_3F(0x3f,1),
    UNUSED_40(0x40,1),
    UNUSED_41(0x41,1),
    UNUSED_42(0x42,1),
    UNUSED_43(0x43,1),
    AGET(0x44,2),
    AGET_WIDE(0x45,2),
    AGET_OBJECT(0x46,2),
    AGET_BOOLEAN(0x47,2),
    AGET_BYTE(0x48,2),
    AGET_CHAR(0x49,2),
    AGET_SHORT(0x4a,2),
    APUT(0x4b,2),
    APUT_WIDE(0x4c,2),
    APUT_OBJECT(0x4d,2),
    APUT_BOOLEAN(0x4e,2),
    APUT_BYTE(0x4f,2),
    APUT_CHAR(0x50,2),
    APUT_SHORT(0x51,2),
    IGET(0x52,2),
    IGET_WIDE(0x53,2),
    IGET_OBJECT(0x54,2),
    IGET_BOOLEAN(0x55,2),
    IGET_BYTE(0x56,2),
    IGET_CHAR(0x57,2),
    IGET_SHORT(0x58,2),
    IPUT(0x59,2),
    IPUT_WIDE(0x5a,2),
    IPUT_OBJECT(0x5b,2),
    IPUT_BOOLEAN(0x5c,2),
    IPUT_BYTE(0x5d,2),
    IPUT_CHAR(0x5e,2),
    IPUT_SHORT(0x5f,2),
    SGET(0x60,2),
    SGET_WIDE(0x61,2),
    SGET_OBJECT(0x62,2),
    SGET_BOOLEAN(0x63,2),
    SGET_BYTE(0x64,2),
    SGET_CHAR(0x65,2),
    SGET_SHORT(0x66,2),
    SPUT(0x67,2),
    SPUT_WIDE(0x68,2),
    SPUT_OBJECT(0x69,2),
    SPUT_BOOLEAN(0x6a,2),
    SPUT_BYTE(0x6b,2),
    SPUT_CHAR(0x6c,2),
    SPUT_SHORT(0x6d,2),
    INVOKE_VIRTUAL(0x6e,3),
    INVOKE_SUPER(0x6f,3),
    INVOKE_DIRECT(0x70,3),
    INVOKE_STATIC(0x71,3),
    INVOKE_INTERFACE(0x72,3),
    UNUSED_73(0x73,1),
    INVOKE_VIRTUAL_RANGE(0x74,3),
    INVOKE_SUPER_RANGE(0x75,3),
    INVOKE_DIRECT_RANGE(0x76,3),
    INVOKE_STATIC_RANGE(0x77,3),
    INVOKE_INTERFACE_RANGE(0x78,3),
    UNUSED_79(0x79,1),
    UNUSED_7A(0x7a,1),
    NEG_INT(0x7b,1),
    NOT_INT(0x7c,1),
    NEG_LONG(0x7d,1),
    NOT_LONG(0x7e,1),
    NEG_FLOAT(0x7f,1),
    NEG_DOUBLE(0x80,1),
    INT_TO_LONG(0x81,1),
    INT_TO_FLOAT(0x82,1),
    INT_TO_DOUBLE(0x83,1),
    LONG_TO_INT(0x84,1),
    LONG_TO_FLOAT(0x85,1),
    LONG_TO_DOUBLE(0x86,1),
    FLOAT_TO_INT(0x87,1),
    FLOAT_TO_LONG(0x88,1),
    FLOAT_TO_DOUBLE(0x89,1),
    DOUBLE_TO_INT(0x8a,1),
    DOUBLE_TO_LONG(0x8b,1),
    DOUBLE_TO_FLOAT(0x8c,1),
    INT_TO_BYTE(0x8d,1),
    INT_TO_CHAR(0x8e,1),
    INT_TO_SHORT(0x8f,1),
    ADD_INT(0x90,2),
    SUB_INT(0x91,2),
    MUL_INT(0x92,2),
    DIV_INT(0x93,2),
    REM_INT(0x94,2),
    AND_INT(0x95,2),
    OR_INT(0x96,2),
    XOR_INT(0x97,2),
    SHL_INT(0x98,2),
    SHR_INT(0x99,2),
    USHR_INT(0x9a,2),
    ADD_LONG(0x9b,2),
    SUB_LONG(0x9c,2),
    MUL_LONG(0x9d,2),
    DIV_LONG(0x9e,2),
    REM_LONG(0x9f,2),
    AND_LONG(0xa0,2),
    OR_LONG(0xa1,2),
    XOR_LONG(0xa2,2),
    SHL_LONG(0xa3,2),
    SHR_LONG(0xa4,2),
    USHR_LONG(0xa5,2),
    ADD_FLOAT(0xa6,2),
    SUB_FLOAT(0xa7,2),
    MUL_FLOAT(0xa8,2),
    DIV_FLOAT(0xa9,2),
    REM_FLOAT(0xaa,2),
    ADD_DOUBLE(0xab,2),
    SUB_DOUBLE(0xac,2),
    MUL_DOUBLE(0xad,2),
    DIV_DOUBLE(0xae,2),
    REM_DOUBLE(0xaf,2),
    ADD_INT_2ADDR(0xb0,1),
    SUB_INT_2ADDR(0xb1,1),
    MUL_INT_2ADDR(0xb2,1),
    DIV_INT_2ADDR(0xb3,1),
    REM_INT_2ADDR(0xb4,1),
    AND_INT_2ADDR(0xb5,1),
    OR_INT_2ADDR(0xb6,1),
    XOR_INT_2ADDR(0xb7,1),
    SHL_INT_2ADDR(0xb8,1),
    SHR_INT_2ADDR(0xb9,1),
    USHR_INT_2ADDR(0xba,1),
    ADD_LONG_2ADDR(0xbb,1),
    SUB_LONG_2ADDR(0xbc,1),
    MUL_LONG_2ADDR(0xbd,1),
    DIV_LONG_2ADDR(0xbe,1),
    REM_LONG_2ADDR(0xbf,1),
    AND_LONG_2ADDR(0xc0,1),
    OR_LONG_2ADDR(0xc1,1),
    XOR_LONG_2ADDR(0xc2,1),
    SHL_LONG_2ADDR(0xc3,1),
    SHR_LONG_2ADDR(0xc4,1),
    USHR_LONG_2ADDR(0xc5,1),
    ADD_FLOAT_2ADDR(0xc6,1),
    SUB_FLOAT_2ADDR(0xc7,1),
    MUL_FLOAT_2ADDR(0xc8,1),
    DIV_FLOAT_2ADDR(0xc9,1),
    REM_FLOAT_2ADDR(0xca,1),
    ADD_DOUBLE_2ADDR(0xcb,1),
    SUB_DOUBLE_2ADDR(0xcc,1),
    MUL_DOUBLE_2ADDR(0xcd,1),
    DIV_DOUBLE_2ADDR(0xce,1),
    REM_DOUBLE_2ADDR(0xcf,1),
    ADD_INT_LIT16(0xd0,2),
    RSUB_INT_LIT16(0xd1,2),
    MUL_INT_LIT16(0xd2,2),
    DIV_INT_LIT16(0xd3,2),
    REM_INT_LIT16(0xd4,2),
    AND_INT_LIT16(0xd5,2),
    OR_INT_LIT16(0xd6,2),
    XOR_INT_LIT16(0xd7,2),
    ADD_INT_LIT8(0xd8,2),
    RSUB_INT_LIT8(0xd9,2),
    MUL_INT_LIT8(0xda,2),
    DIV_INT_LIT8(0xdb,2),
    REM_INT_LIT8(0xdc,2),
    AND_INT_LIT8(0xdd,2),
    OR_INT_LIT8(0xde,2),
    XOR_INT_LIT8(0xdf,2),
    SHL_INT_LIT8(0xe0,2),
    SHR_INT_LIT8(0xe1,2),
    USHR_INT_LIT8(0xe2,2),
    //Above instructions are for optimization purpose. I've never seen them yet.
    //IGET_VOLATILE(0xe3),
    //IPUT_VOLATILE(0xe4),
    //SGET_VOLATILE(0xe5),
    //SPUT_VOLATILE(0xe6),
    //IGET_OBJECT_VOLATILE(0xe7),
    //IGET_WIDE_VOLATILE(0xe8),
    //IPUT_WIDE_VOLATILE(0xe9),
    //SGET_WIDE_VOLATILE(0xea),
    //SPUT_WIDE_VOLATILE(0xeb),
    //UNUSED_EC(0xec),
    //UNUSED_ED(0xed),
    //EXECUTE_INLINE(0xee),
    //EXECUTE_INLINE_RANGE(0xef),
    //INVOKE_DIRECT_EMPTY(0xf0),
    //IGET_QUICK(0xf2),
    //IGET_WIDE_QUICK(0xf3),
    //IGET_OBJECT_QUICK(0xf4),
    //IPUT_QUICK(0xf5),
    //IPUT_WIDE_QUICK(0xf6),
    //IPUT_OBJECT_QUICK(0xf7),
    //INVOKE_VIRTUAL_QUICK(0xf8),
    //INVOKE_VIRTUAL_QUICK_RANGE(0xf9),
    //INVOKE_SUPER_QUICK(0xfa),
    //INVOKE_SUPER_QUICK_RANGE(0xfb),
    //IPUT_OBJECT_VOLATILE(0xfc),
    //SGET_OBJECT_VOLATILE(0xfd),
    //SPUT_OBJECT_VOLATILE(0xfe),
    UNUSED_FF(0xff,1);

    final int op;
    final int size;

    private Opcode(int op, int size) {
        this.op = op;
        this.size = size;
    }

    @Override
    public String toString() {
        return name() + '(' + op + ')';
    }


    public int size(){
        //Node : returning byte size
        return size * 2;
    }

    public int encode(){
        return op;
    }

    private final static Opcode[] OPCODES = Opcode.values();

    public static Opcode decode(int opcode) {
        return OPCODES[opcode];
    }


    private boolean between(Opcode lb, Opcode ub){
        return op >= lb.op && op <= ub.op;
    }

    //Classification function
    public boolean isMOVE_REG(){ return between(MOVE,MOVE_OBJECT_16); }
    public boolean isMOVE_REG_OBJECT() {return between(MOVE_OBJECT, MOVE_OBJECT_16); }
    public boolean isMOVE_REG_WIDE(){ return between(MOVE_WIDE, MOVE_WIDE_16); }

    public boolean isMOVE_RESULT(){ return between(MOVE_RESULT,MOVE_RESULT_OBJECT); }
    public boolean isMOVE_RESULT_WIDE(){ return op == MOVE_RESULT_WIDE.op; }

    public boolean isMOVE_EXCEPTION(){ return op == MOVE_EXCEPTION.op; }

    public boolean isRETURN(){ return between(RETURN_VOID,RETURN_OBJECT); }
    public boolean isRETURN_WIDE(){ return op == RETURN_WIDE.op; }
    public boolean isRETURN_OBJECT(){ return op == RETURN_OBJECT.op; }

    public boolean isCONST_VAL(){ return between(CONST_4,CONST_HIGH16); }
    public boolean isCONST_VAL_WIDE16(){ return between(CONST_WIDE_16, CONST_WIDE_32); }
    public boolean isCONST_VAL_WIDE32(){ return between(CONST_WIDE, CONST_WIDE_HIGH16); }
    public boolean isCONST_STR(){ return between(CONST_STRING,CONST_STRING_JUMBO); }
    public boolean isCONST_CLASS(){ return op == CONST_CLASS.op; }

    public boolean isMONITOR(){ return op == MONITOR_ENTER.op || op == MONITOR_EXIT.op; }

    //check-case
    //instance-of
    //array-length
    //new-instance
    //new-array
    //filled-new-array
    public boolean isTHROW(){ return op == THROW.op; }
    public boolean isGOTO(){ return between(GOTO,GOTO_32); }
    //packed switch
    //sparse-switch
    public boolean isCMP() { return between(CMPL_FLOAT,CMP_LONG);}
    public boolean isCMP_WIDE(){ return between(CMPL_DOUBLE, CMP_LONG); }
    public boolean isTEST(){ return between(IF_EQ, IF_LE); }
    public boolean isTESTZ(){ return between(IF_EQZ, IF_LEZ); }

    public boolean isAGET(){ return between(AGET,AGET_SHORT); }
    public boolean isAPUT(){ return between(APUT,APUT_SHORT); }

    public boolean isIGET(){ return between(IGET,IGET_SHORT); }
    public boolean isIPUT(){ return between(IPUT,IPUT_SHORT); }
    public boolean isSGET(){ return between(SGET,SGET_SHORT); }
    public boolean isSPUT(){ return between(SPUT,SPUT_SHORT); }
    public boolean isFieldAccess(){ return between(IGET, SPUT_SHORT); }

    public boolean isAGET_WIDE(){ return op == AGET_WIDE.op; }
    public boolean isAPUT_WIDE(){ return op == APUT_WIDE.op; }
    public boolean isIGET_WIDE(){ return op == IGET_WIDE.op; }
    public boolean isIPUT_WIDE(){ return op == IPUT_WIDE.op; }
    public boolean isSGET_WIDE(){ return op == SGET_WIDE.op; }
    public boolean isSPUT_WIDE(){ return op == SPUT_WIDE.op; }

    public boolean isINVOKE(){ return between(INVOKE_VIRTUAL, INVOKE_INTERFACE) || between(INVOKE_VIRTUAL_RANGE,INVOKE_INTERFACE_RANGE);}
    public boolean isINVOKE_DIRECT(){ return this.op == INVOKE_DIRECT.encode() || this.op == INVOKE_DIRECT_RANGE.encode();}
    public boolean isINVOKE_STATIC(){ return this.op == INVOKE_STATIC.encode() || this.op == INVOKE_STATIC_RANGE.encode();}

    public boolean isUNOP(){ return between(NEG_INT, INT_TO_SHORT); }
    public boolean isUNOP_GET_WIDE(){
        if (op == NEG_LONG.op || op == NOT_LONG.op || op == NEG_DOUBLE.op) return true;
        if (op == LONG_TO_INT.op || op == LONG_TO_FLOAT.op || op == LONG_TO_DOUBLE.op) return true;
        if (op == DOUBLE_TO_INT.op || op == DOUBLE_TO_LONG.op || op == DOUBLE_TO_FLOAT.op) return true;
        return false;
    }
    public boolean isUNOP_PUT_WIDE(){
        if (op == NEG_LONG.op || op == NEG_DOUBLE.op) return true;
        if (op == INT_TO_LONG.op || op == INT_TO_DOUBLE.op || op == LONG_TO_DOUBLE.op) return true;
        if (op == FLOAT_TO_LONG.op || op == FLOAT_TO_DOUBLE.op || op == DOUBLE_TO_LONG.op) return true;
        return false;
    }
    public boolean isUNOP_PUT_SHORT(){ return op == INT_TO_SHORT.op; }
    public boolean isUNOP_PUT_CHAR(){ return op == INT_TO_CHAR.op; }
    public boolean isUNOP_PUT_BYTE(){ return op == INT_TO_BYTE.op; }
    public boolean isUNOP_PUT_BOOLEAN(){ return (op == NOT_INT.op || op == NOT_LONG.op); }


    public boolean isBINOP(){ return between(ADD_INT, REM_DOUBLE); }
    public boolean isBINOP_INT(){ return between(ADD_INT, USHR_INT); }
    public boolean isBINOP_LONG(){ return between(ADD_LONG, USHR_LONG); }
    public boolean isBINOP_LONG_SH(){ return between(SHL_LONG, USHR_LONG); }
    public boolean isBINOP_FLOAT(){ return between(ADD_FLOAT, REM_FLOAT); }
    public boolean isBINOP_DOUBLE(){ return between(ADD_DOUBLE, REM_DOUBLE); }

    public boolean isBINOP_2ADDR(){ return between(ADD_INT_2ADDR, REM_DOUBLE_2ADDR);}
    public boolean isBINOP_2ADDR_INT(){ return between(ADD_INT_2ADDR, USHR_INT_2ADDR);}
    public boolean isBINOP_2ADDR_LONG(){ return between(ADD_LONG_2ADDR, USHR_LONG_2ADDR);}
    public boolean isBINOP_2ADDR_LONG_SH(){ return between(SHL_LONG_2ADDR, USHR_LONG_2ADDR);}
    public boolean isBINOP_2ADDR_FLOAT(){ return between(ADD_FLOAT_2ADDR, REM_FLOAT_2ADDR);}
    public boolean isBINOP_2ADDR_DOUBLE(){ return between(ADD_DOUBLE_2ADDR, REM_DOUBLE_2ADDR);}

    public boolean isBINOP_LIT16(){ return between(ADD_INT_LIT16, XOR_INT_LIT16);}
    public boolean isBINOP_LIT8(){ return between(ADD_INT_LIT8, USHR_INT_LIT8);}
}
