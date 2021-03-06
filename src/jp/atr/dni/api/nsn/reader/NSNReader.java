package jp.atr.dni.api.nsn.reader;

import java.io.IOException;
import java.io.RandomAccessFile;

import jp.atr.dni.api.APIList;
import jp.atr.dni.api.nsn.NSNFile;
import jp.atr.dni.api.nsn.channels.NSNChannel;
import jp.atr.dni.api.nsn.header.NSNFileHeader;
import jp.atr.dni.api.utils.ReaderUtils;

/**
 * 
 * @author 武宮　誠 「Makoto Takemiya」<br />
 * （株）国際電気通信基礎技術研究・脳情報研究所・神経情報学研究室<br/>
 * 「ATR - Computational Neuroscience Laboratories, Department of Neuroinformatics」
 *
 * @version 2011/11/19
 */
public class NSNReader {

   /**
    * 
    * @param path - path to the neuroshare file to read
    * @return
    */
   public NSNFile readNSNFileAllData(String path) {
      String magicCode = null;
      RandomAccessFile file = null;

      try {
         file = new RandomAccessFile(path, "r");
         file.seek(0);

         magicCode = "";
         for (int i = 0; i < 16; i++) {
            magicCode += (char) file.readByte();
         }
         if (!magicCode.equals("NSN ver00000010 ") && !magicCode.trim().equals("NSN ver00000010 ")) {
            System.out.println("The FILE we are trying to read is not compatible with "
                  + "the ver 1.0 format of the Neuroshare Native Datafile "
                  + "Specification.\n\nSorry, but there is nothing else we can do now.\n\n" + "Returning...");
            return null;
         }

         // Now read in the FILE info
         NSNFileHeader fileHeader = readFileHeader(file);
         fileHeader.setMagicCode(magicCode);

         // System.out.println("entityCount: " + fileHeader.getEntityCount());

         if (fileHeader.getEntityCount() < 1) {
            // LOGGER.error("The FILE has no entities to read in." + "Exiting...");
            return null;
         }

         APIList<NSNChannel> channels = new APIList<NSNChannel>(new NSNChannelProvider(
               fileHeader.getEntityCount(), file.getFilePointer(), path));

         return new NSNFile(fileHeader, channels);

      } catch (Throwable ex) {
         ex.printStackTrace();
         return null;
      } finally {
         try {
            file.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private NSNFileHeader readFileHeader(RandomAccessFile file) throws IOException {
      String fileType = "";
      long entityCount = 0;
      double timeStampResolution;
      double timeSpan;
      String appName = "";
      long year = 0;
      long month = 0;
      long dayOfWeek = 0;
      long dayOfMonth = 0;
      long hourOfDay = 0;
      long minOfDay = 0;
      long secOfDay = 0;
      long milliSecOfDay = 0;
      String comments = "";

      file.seek(16); // Seek to guarantee we are where we want to be

      // Read in fileType
      for (int i = 0; i < 32; i++) {
         fileType += (char) file.readByte();
      }

      // Read in entityCount
      entityCount = ReaderUtils.readUnsignedInt(file);
      // Read in timeStampeRes
      timeStampResolution = ReaderUtils.readDouble(file);
      // Read in timespan
      timeSpan = ReaderUtils.readDouble(file);

      // Read in the appName
      for (int i = 0; i < 64; i++) {
         appName += (char) file.readByte();
      }

      // Read in the year
      year = ReaderUtils.readUnsignedInt(file);
      // Read in the month
      month = ReaderUtils.readUnsignedInt(file);
      // Read in the dayOfWeek
      dayOfWeek = ReaderUtils.readUnsignedInt(file);
      dayOfMonth = ReaderUtils.readUnsignedInt(file);
      // Read in the hourOfDay;
      hourOfDay = ReaderUtils.readUnsignedInt(file);
      // Read in the minOfDay
      minOfDay = ReaderUtils.readUnsignedInt(file);
      // Read in the secOfDay;
      secOfDay = ReaderUtils.readUnsignedInt(file);
      // Read in the milliSecOfDay;
      milliSecOfDay = ReaderUtils.readUnsignedInt(file);

      // Read in the comments
      for (int i = 0; i < 256; i++) {
         comments += (char) file.readByte();
      }
      comments = comments.trim();

      return new NSNFileHeader(fileType, entityCount, timeStampResolution, timeSpan, appName, year, month,
            dayOfWeek, dayOfMonth, hourOfDay, minOfDay, secOfDay, milliSecOfDay, comments);
   }
}