package com.adamsresearch.jarview;

import java.io.*;
import java.util.zip.*;

public class UnZip {
   public static void main (String argv[]) {
      try {
         final int BUFFER = 2048;
         BufferedOutputStream dest = null;
         FileInputStream fis = new 
	   FileInputStream(argv[0]);
         CheckedInputStream checksum = new 
           CheckedInputStream(fis, new Adler32());
         ZipInputStream zis = new 
           ZipInputStream(new 
             BufferedInputStream(checksum));
         ZipEntry entry;
         while((entry = zis.getNextEntry()) != null) {
            System.out.println("Extracting: " +entry);
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
System.out.println("entry:  '" + entry.getName() + "'");
            FileOutputStream fos = new 
              FileOutputStream(entry.getName());
            dest = new BufferedOutputStream(fos, 
              BUFFER);
            while ((count = zis.read(data, 0, 
              BUFFER)) != -1) {
               dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
         }
         zis.close();
         System.out.println("Checksum: " + checksum.getChecksum().getValue());
      } catch(Exception e) {
         e.printStackTrace();
      }
   }
}
