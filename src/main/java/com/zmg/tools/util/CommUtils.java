package com.zmg.tools.util;

public class CommUtils {
    public static final int MAGIC_PACKET_SIZE = 102;
    public static final int MAGIC_PACKET_HEADER_SIZE = 6;

    public static byte[] createMagicPacket(String macAddress)throws Exception{
        byte[] bytes = new byte[MAGIC_PACKET_SIZE];
        String address = macAddress.replaceAll("-","").trim();
        address = address.toUpperCase();
        for(int i=0;i<MAGIC_PACKET_HEADER_SIZE;i++){
            Integer c = 255;
            bytes[i] = c.byteValue();
        }

        for(int i=MAGIC_PACKET_HEADER_SIZE;i<MAGIC_PACKET_SIZE;){
            for(int j=0;j<address.length();j+=2){
                Integer c = "0123456789ABCDEF".indexOf(address.charAt(j))*16 +"0123456789ABCDEF".indexOf(address.charAt(j+1));
                bytes[i] = c.byteValue();
                i++;
            }
        }
        return bytes;
    }

    public static void main(String[] args) throws Exception{
        createMagicPacket("1f-ff-ff-ff-ff-ff");
    }
}
