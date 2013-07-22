package com.dcm4che.tools;

import org.apache.commons.cli.*;
import org.dcm4che2.data.*;
import org.dcm4che2.image.ColorModelFactory;
import org.dcm4che2.image.LookupTable;
import org.dcm4che2.image.ShortLookupTable;
import org.dcm4che2.image.VOIUtils;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageWriterSpi;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.io.TranscoderInputHandler;
import org.dcm4che2.media.FileMetaInformation;
import org.dcm4che2.util.CloseUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-22
 * Time: 上午10:16
 * To change this template use File | Settings | File Templates.
 */
public class ModifyDcm {

    private static final String USAGE = "ModifyDcm [Options] SOURCE DEST\n"
            + "or ModifyDcm [Options] SOURCE... DIRECTORY";

    private static final String DESCRIPTION = "Convert DICOM file SOURCE to DEST, "
            + "or multiple SOURCE(s) to DIRECTORY.\nOptions:";

    private static final String EXAMPLE = "\nExample: ModifyDcm in.dcm out.dcm\n"
            + " => Decode DICOM object from DICOM file in.dcm and encode it with"
            + " Implicit VR Little Endian Transfer Syntax to DICOM file out.dcm.";

    private static final int KB = 1024;

    private boolean nofmi;

    private String tsuid;

    TransferSyntax destinationSyntax;

    private int transcoderBufferSize = KB;

    /** Contains values to overwrite in the output stream. */
    private DicomObject overwriteObject;

    private static CommandLine parse(String[] args) {
        Options opts = new Options();

        opts.addOption(null, "no-fmi", false,
                "Encode result without File Meta Information. At default, "
                        + " File Meta Information is included.");
        opts.addOption("e", "explicit", false,
                "Encode result with Explicit VR Little Endian Transfer Syntax. "
                        + "At default, Implicit VR Little Endian is used.");
        opts.addOption("b", "big-endian", false,
                "Encode result with Explicit VR Big Endian Transfer Syntax. "
                        + "At default, Implicit VR Little Endian is used.");
        opts.addOption("z", "deflated", false,
                "Encode result with Deflated Explicit VR Little Endian Syntax. "
                        + "At default, Implicit VR Little Endian is used.");

        OptionBuilder.withArgName("[seq/]attr=value");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator('=');
        OptionBuilder
                .withDescription("specify value to set in the output stream.  Currently only works when transcoding images.");
        opts.addOption(OptionBuilder.create("s"));

        opts.addOption("t", "syntax", true,
                "Encode result with the specified transfer syntax - recodes"
                        + " the image typically.");

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder
                .withDescription("transcoder buffer size in KB, 1KB by default");
        OptionBuilder.withLongOpt("buffer");
        opts.addOption(OptionBuilder.create(null));

        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e) {
            exit("ModifyDcm: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = ModifyDcm.class.getPackage();
            System.out.println("ModifyDcm v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() < 2) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'ModifyDcm -h' for more information.");
        System.exit(1);
    }

    private static String transferSyntax(CommandLine cl) {
        if (cl.hasOption("e")) {
            return UID.ExplicitVRLittleEndian;
        }
        if (cl.hasOption("b")) {
            return UID.ExplicitVRBigEndian;
        }
        if (cl.hasOption("z")) {
            return UID.DeflatedExplicitVRLittleEndian;
        }
        if (cl.hasOption("t")) {
            return cl.getOptionValue("t");
        }
        return UID.ImplicitVRLittleEndian;
    }

    private static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {
            // parameter is not a valid integer; fall through to exit
        }
        exit(errPrompt);
        throw new RuntimeException();
    }

    @SuppressWarnings("unchecked")
    public static void doModify(String sourcePath,String destPath,Map attrsMap) {
        /*args = new String[]{"E:\\dicom\\dicomfile\\1.2.276.0.7230010.3.0.3.5.1.6189812.569431277\\1.2.276.0.7230010.3.0.3.5.1.6189815.224816701.dcm",
                "E:\\dicom\\dicomfile\\1.2.276.0.7230010.3.0.3.5.1.6189812.569431277\\xx.dcm"
        };*/
        String[] args=new String[]{sourcePath,destPath};
        CommandLine cl = parse(args);
        ModifyDcm ModifyDcm = new ModifyDcm();
        ModifyDcm.setNoFileMetaInformation(cl.hasOption("no-fmi"));
        ModifyDcm.setTransferSyntax(transferSyntax(cl));
        if (cl.hasOption("buffer")) {
            ModifyDcm.setTranscoderBufferSize(parseInt(cl
                    .getOptionValue("buffer"),
                    "illegal argument of option --buffer", 1, 10000)
                    * KB);
        }
        if (cl.hasOption("s")) {
            ModifyDcm.overwriteObject = new BasicDicomObject();
            String[] matchingKeys = cl.getOptionValues("s");
            for (int i = 1; i < matchingKeys.length; i++, i++) {
                int[] tag = Tag.toTagPath(matchingKeys[i - 1]);
                String svalue = matchingKeys[i];
                //ModifyDcm.overwriteObject.putString(tag, null, svalue);
            }

        }

        List<String> argList = cl.getArgList();
        int argc = argList.size();

        File dest = new File(argList.get(argc - 1));
        long t1 = System.currentTimeMillis();
        int count=0;
        if (dest.isDirectory()) {
            //count = ModifyDcm.mconvert(argList, 0, dest);
            exit("Please use a file for dest!");
        } else {
            File src = new File(argList.get(0));
            if (argc > 2 || src.isDirectory()) {
                exit("ModifyDcm: when converting several files, "
                        + "last argument must be a directory\n");
            }
            count = ModifyDcm.mconvert(src, dest,attrsMap);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("\nconverted " + count + " file(s) in " + (t2 - t1)
                / 1000f + " s.");
    }

    public final void setNoFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    public final void setTransferSyntax(String tsuid) {
        this.tsuid = tsuid;
        this.destinationSyntax = TransferSyntax.valueOf(tsuid);
    }

    public final void setTranscoderBufferSize(int transcoderBufferSize) {
        this.transcoderBufferSize = transcoderBufferSize;
    }

    private int mconvert(List<String> args, int optind, File destDir) {
        int count = 0;
        for (int i = optind, n = args.size() - 1; i < n; ++i) {
            File src = new File(args.get(i));
            count += mconvert(src, new File(destDir, src.getName()),new HashMap());
        }
        return count;
    }

    public int mconvert(File src, File dest,Map attrs) {
        if (!src.exists()) {
            System.err.println("WARNING: No such file or directory: " + src
                    + " - skipped.");
            return 0;
        }
        if (src.isFile()) {
            try {
                convert(src, dest,attrs);
            } catch (Exception e) {
                System.err.println("WARNING: Failed to convert " + src + ":");
                e.printStackTrace(System.err);
                System.out.print('F');
                return 0;
            }
            System.out.print('.');
            return 1;
        }
        File[] files = src.listFiles();
        if (files.length > 0 && !dest.exists()) {
            dest.mkdirs();
        }
        int count = 0;
        for (int i = 0; i < files.length; ++i) {
            count += mconvert(files[i], new File(dest, files[i].getName()),attrs);
        }
        return count;
    }

    public void convert(File src, File dest, Map attrs) throws IOException {
        DicomInputStream dis = new DicomInputStream(src);
        DicomOutputStream dos = null;
        try {
            //DicomObject fmiAttrs = dis.readFileMetaInformation();
            DicomObject fmiAttrs = dis.readDicomObject();
            if(!attrs.isEmpty()){
               for(Object o : attrs.keySet()){
                 int key=(Integer)o;
                   switch (key){
                       case Tag.PatientID:
                           fmiAttrs.putString(Tag.PatientID, VR.LO, attrs.get(key).toString() + "_" + fmiAttrs.getString(Tag.PatientID));
                           break;
                       case Tag.SpecificCharacterSet:
                           fmiAttrs.putString(Tag.SpecificCharacterSet,VR.CS,attrs.get(key).toString());
                           break;
                       default:
                           break;
                   }
               }
            }
            //fmiAttrs.putString(Tag.PatientName,VR.PN,"dfdf") ;
            //DicomObject xx=dis.readDicomObject();
            //xx.putString(Tag.PatientName,VR.PN,"ff");
            //String n= xx.getString(Tag.PatientName);

            String sourceSyntaxUid = UID.ImplicitVRLittleEndian;
            if (fmiAttrs != null)
                sourceSyntaxUid = fmiAttrs.getString(Tag.TransferSyntaxUID,
                        UID.ImplicitVRLittleEndian);
            TransferSyntax sourceSyntax = TransferSyntax
                    .valueOf(sourceSyntaxUid);
            boolean bothRaw = (!sourceSyntax.encapsulated())
                    && (!destinationSyntax.encapsulated());
            boolean recodeImages = !(bothRaw || sourceSyntax
                    .equals(destinationSyntax));
            if (recodeImages) {
                recodeImages(src, dest);
                return;
            }
            dos = new DicomOutputStream(dest);
            if (!nofmi) {
               /* FileMetaInformation fmi = fmiAttrs == null ? createFMI(src)
                        : createFMI(fmiAttrs);
                dos.writeFileMetaInformation(fmi.getDicomObject());*/
                dos.writeDicomFile(fmiAttrs);
            }
            dos.setTransferSyntax(tsuid);
            TranscoderInputHandler h = new TranscoderInputHandler(dos,
                    transcoderBufferSize);
            dis.setHandler(h);
            dis.readDicomObject();
        } finally {
            CloseUtils.safeClose(dos);
            CloseUtils.safeClose(dis);
        }
    }

    /**
     * Checks to see if the stream needs to be stripped down in bit depth.
     * Modifies meta to have a new dicom object if it is changing bit depth, and
     * updates various headers.
     */
    protected LookupTable prepareBitStrip(DicomStreamMetaData meta,
                                          ImageReader reader) throws IOException {
        if (!destinationSyntax.uid().equals(UID.JPEGExtended24))
            return null;
        DicomObject ds = meta.getDicomObject();
        int stored = ds.getInt(Tag.BitsStored);
        if (stored < 13)
            return null;
        int frames = ds.getInt(Tag.NumberOfFrames, 1);
        WritableRaster r = (WritableRaster) reader.readRaster(0, null);
        int[] mm = VOIUtils.calcMinMax(ds, r);
        if (frames > 1) {
            r = (WritableRaster) reader.readRaster(frames - 1, null);
            int[] mm2 = VOIUtils.calcMinMax(ds, r);
            mm[0] = Math.min(mm[0], mm2[0]);
            mm[1] = Math.min(mm[1], mm2[1]);
        }
        if (frames > 2) {
            r = (WritableRaster) reader.readRaster(frames / 2 - 1, null);
            int[] mm2 = VOIUtils.calcMinMax(ds, r);
            mm[0] = Math.min(mm[0], mm2[0]);
            mm[1] = Math.min(mm[1], mm2[1]);
        }
        ds.putInt(Tag.SmallestImagePixelValue, VR.IS, mm[0]);
        ds.putInt(Tag.LargestImagePixelValue, VR.IS, mm[1]);
        int maxVal = mm[1];
        if (mm[0] < 0) {
            maxVal = Math.max(maxVal, 1 - mm[0]);
            maxVal *= 2;
        }
        int bits = 0;
        while (maxVal > 0) {
            bits++;
            maxVal >>= 1;
        }
        boolean signed = ds.getInt(Tag.PixelRepresentation) == 1;
        if (bits < 13 && mm[0] >= 0) {
            ds.putInt(Tag.BitsStored, VR.IS, bits);
            ds.putInt(Tag.HighBit, VR.IS, bits - 1);
            ds.putInt(Tag.PixelRepresentation, VR.IS, 0);
            return null;
        }
        ds.putInt(Tag.BitsStored, VR.IS, 12);
        ds.putInt(Tag.HighBit, VR.IS, 11);
        // Number of entries required
        int entries = mm[1] - mm[0] + 1;
        short[] slut = new short[entries];
        int range = entries - 1;
        for (int i = 0; i < entries; i++) {
            slut[i] = (short) ((4095 * i) / range);
        }
        if (signed) {
            ds.putInt(Tag.PixelRepresentation, VR.IS, 0);
        }
        LookupTable lut = new ShortLookupTable(stored, signed, -mm[0], 12, slut);
        return lut;
    }

    /**
     * Recodes the images from the source transfer syntax, as read from the src
     * file, to the specified destination syntax.
     */
    public void recodeImages(File src, File dest) throws IOException {
        ImageReader reader = new DicomImageReaderSpi().createReaderInstance();
        ImageWriter writer = new DicomImageWriterSpi().createWriterInstance();
        FileImageInputStream input = new FileImageInputStream(src);
        reader.setInput(input);
        if (dest.exists())
            dest.delete();
        FileImageOutputStream output = new FileImageOutputStream(dest);
        writer.setOutput(output);
        DicomStreamMetaData streamMeta = (DicomStreamMetaData) reader
                .getStreamMetadata();
        DicomObject ds = streamMeta.getDicomObject();
        DicomStreamMetaData writeMeta = (DicomStreamMetaData) writer
                .getDefaultStreamMetadata(null);
        DicomObject newDs = new BasicDicomObject();
        ds.copyTo(newDs);
        writeMeta.setDicomObject(newDs);
        int frames = ds.getInt(Tag.NumberOfFrames, 1);
        LookupTable lut = prepareBitStrip(writeMeta, reader);
        newDs.putString(Tag.TransferSyntaxUID, VR.UI, destinationSyntax.uid());
        newDs.putString(Tag.ImplementationClassUID, VR.UI,
                Implementation.classUID());
        newDs.putString(Tag.ImplementationVersionName, VR.SH,
                Implementation.versionName());
        if (overwriteObject != null) {
            overwriteObject.copyTo(newDs);
        }
        writer.prepareWriteSequence(writeMeta);
        for (int i = 0; i < frames; i++) {
            WritableRaster r = (WritableRaster) reader.readRaster(i, null);
            ColorModel cm = ColorModelFactory.createColorModel(ds);
            BufferedImage bi = new BufferedImage(cm, r, false, null);
            if (lut != null) {
                lut.lookup(bi.getRaster(), bi.getRaster());
            }
            IIOImage iioimage = new IIOImage(bi, null, null);
            writer.writeToSequence(iioimage, null);
        }
        writer.endWriteSequence();
        output.close();
        input.close();
    }

    private FileMetaInformation createFMI(File src) throws IOException {
        DicomInputStream dis2 = new DicomInputStream(src);
        try {
            dis2.setHandler(new StopTagInputHandler(Tag.SOPInstanceUID + 1));
            FileMetaInformation fmi = new FileMetaInformation(dis2
                    .readDicomObject());
            fmi.init();
            fmi.setTransferSyntaxUID(tsuid);
            return fmi;
        } finally {
            dis2.close();
        }
    }

    private FileMetaInformation createFMI(DicomObject srcfmi) {
        FileMetaInformation fmi = new FileMetaInformation(srcfmi);
        return new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi
                .getMediaStorageSOPInstanceUID(), tsuid);
    }

}
