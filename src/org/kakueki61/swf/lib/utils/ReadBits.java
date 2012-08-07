package org.kakueki61.swf.lib.utils;

import java.io.IOException;
import java.io.InputStream;

public class ReadBits {
    private static final String TAG = ReadBits.class.getSimpleName();
    public static int bitPointer = 0;
    public static int lastByte = 0;
    
    /**
     * Reads unsigned bit value
     * 
     * 1.�o�C�g���E����ǂݍ���ŁA�o�C�g���E���܂����Ȃ�       
     * 2.�o�C�g���E����ǂݍ���ŁA�o�C�g���E���܂���
     * 3.�o�C�g���E�̓r������ǂݍ���ŁA�o�C�g���E���܂����Ȃ�
     * 4.�o�C�g���E�̓r������ǂݍ���ŁA�o�C�g���E���܂���
     * 
     * @param is
     * @param nBits the number of bits to read
     * @return
     * @throws IOException
     */
    public static int readUB(InputStream is, int nBits) throws IOException {
        int v = 0;
        if(bitPointer == 0) {                                   //�o�C�g�z�u����ǂݍ��ޏꍇ
            if(nBits <= 8) {                                    //1byte�ȓ��̏ꍇ
                lastByte = is.read();
                bitPointer = nBits % 8;                         //TODO check !
                v = lastByte >> (8 - nBits);
            }else {                                             //1byte�𒴂���ꍇ
                int byteNum = nBits / 8;
                for(int i = 0; i < byteNum; i++) {
                    //v |= is.read() << (nBits - 8 * (i + 1));
                    v <<= 8;            //and slide 8bits
                    v |= is.read();     //add 1byte
                }
                int restNum = nBits % 8;
                bitPointer = restNum;
                if(restNum > 0) {                                //not called when restNum == 0
                    v <<=8;
                    lastByte = is.read();
                    v |= lastByte;
                    v >>= (8 - restNum);                        //�]�肪��������銴�������ǂ����񂾂����H -> ���̕���lastByte����ǂނ̂ł���
                }
            }
        }else {                                                 //�J�n���o�C�g���E�łȂ�
            if(bitPointer + nBits <= 8) {                       //�o�C�g���E���܂����Ȃ�
                //System.out.println("case 3");
                int mask = 0x000000ff >> bitPointer;
                v = lastByte & mask;
                v >>= (8 - (bitPointer + nBits));
                bitPointer = (bitPointer + nBits) % 8;
            }else {                                             //beyond the boundary of byte
                //System.out.println("case 4");
                int mask = 0x00000000ff >> bitPointer;          //Java��>>�͎Z�p�V�t�g�ŁA�V�t�g�ŋ󂢂���ʃr�b�g�͕����g���Ŗ��߂���̂�0����ʂɒu���Ă���
                v = lastByte & mask;
                
                //calculation of the rest
                int excess = (bitPointer + nBits) - 8;          // nBits - (8 - bitPointer)
                int byteNum = excess / 8;
                int restNum = excess % 8;
                //System.out.println("byteNum: " + byteNum);
                //System.out.println("restNum: " + restNum);
                if(byteNum == 0) {
                    lastByte = is.read();
                    v <<= 8;
                    v |= lastByte;
                    v >>= (8 - restNum);
                }else {
                    for(int i = 0; i < byteNum; i++) {
                        lastByte = is.read();
                        v <<= 8;
                        v |= lastByte;
                    }
                    if(restNum > 0) {
                        lastByte = is.read();
                        v <<= 8;
                        v |= lastByte;
                        v >>= (8- restNum);
                    }
                }
                bitPointer = restNum;
            }
        }
        //System.out.println("lastByte: " + lastByte);
        //System.out.println("bitPointer: " + bitPointer);
        return v;
    }
    
    /**
     * �����t���r�b�g�l��ǂݎ��
     * Reads signed bit value
     * 
     * @param is        inputstream
     * @param nBits     the number of the bit to read
     * @return          the value of the bit to be read
     * @throws IOException
     */
    public static int readSB(InputStream is, int nBits) throws IOException {
        int v = readUB(is, nBits);
        //judge whether positive or negative
        if(v >> (nBits -1) == 1) {
            //�g�������ɓ����l�����
            //nBits��5bit�ŕ\���ł���l�Ȃ̂ŁA�ő�31
            int extend = 0xffffffff;    //1111 1111 1111 1111 1111 1111 1111 1111 -> 32bits
            extend <<= nBits;
            //����
            v |= extend;
        }
        return v;
    }
    
    /**
     * Reads singned fixed-point bit value.</br>
     * 
     * @param is        InputStream
     * @param nBits     the number of bits used to store the value
     * @return float
     * @throws IOException 
     */
    public static double readFB(InputStream is, int nBits) throws IOException {
//        int integer;
//        int fractional;
//        
//        /*
//         * devide 4 cases
//         * (i)  n <= 16         calculates nBits order fractional number ������(nBits)�ʂ܂ł̏���
//         * (ii) 16 < n < 32     16bits represent the number of after decimal point, the rest is the one before decimal point
//         * (iii)n = 32          16.16 fixed point number
//         * (iv) n > 32          return 0
//         */
//        if(nBits <= 32) {
//            if(nBits == 32) {
//                System.out.println("nBits: " + nBits + "(case 3)");
//                integer = readSB(is, 16);
//                fractional = readUB(is, 16);
//                
//            }else if(nBits < 32 && nBits > 16) {
//                System.out.println("nBits: " + nBits + "(case 2)");
//                
//            }else {
//                System.out.println("nBits: " + nBits + "(case 1)");
//                
//            }
//        }
//        System.out.println("nBits: " + nBits + "(case 4)");
//        return 0;
        int v = readSB(is, nBits);
        
        if(v == 0) {
            return 0;
        }
        
        boolean isNagative = false;
        if(v >> 63 == 1) {
            isNagative = true;
            v = v * (-1);
        }
        
        long vDouble = v;           //64bit
        vDouble <<= 20;
        int count = 0;
        while((vDouble & 0x10000000000000L) == 0) {
            vDouble <<= 1;
            count++;
        }
        //vDouble�͉������ɂ����܂���
        
        long exp = (16 - count) + 1023;  //add bias
        
        long signPart;
        if(isNagative) {
            signPart = 0x8000000000000000L;
        }else {
            signPart = 0x0000000000000000L;
        }
        long expPart = exp << 52;
        long significand = vDouble & 0x000fffffffffffffL;
        
        return Double.longBitsToDouble(signPart | expPart | significand);
    }
    
    public static void completeReadBits() {
        ReadBits.bitPointer = 0;
    }

}
