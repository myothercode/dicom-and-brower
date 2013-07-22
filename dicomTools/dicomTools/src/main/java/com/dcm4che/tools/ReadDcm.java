package com.dcm4che.tools;

import org.apache.commons.cli.*;
import org.dcm4che2.data.*;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.TagUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-10
 * Time: 下午2:53
 * To change this template use File | Settings | File Templates.
 */
public class ReadDcm implements DicomInputHandler {

    private static final int DEF_MAX_WIDTH = 78;
    private static final int MIN_MAX_WIDTH = 32;
    private static final int MAX_MAX_WIDTH = 512;
    private static final int DEF_MAX_VAL_LEN = 64;
    private static final int MIN_MAX_VAL_LEN = 16;
    private static final int MAX_MAX_VAL_LEN = 512;
    private static final String USAGE =
            "ReadDcm [-cVh] [-l <max>] [-w <max>] <dcmfile>";
    private static final String DESCRIPTION =
            "Dump DICOM file and data set\nOptions:";
    private static final String EXAMPLE = null;
    public static Map map=new HashMap();


    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        Option width = new Option("w", "width", true,
                "maximal number of characters per line, by default: 80");
        width.setArgName("max");
        opts.addOption(width);
        Option vallen = new Option("l", "vallen", true,
                "limit value prompt to <maxlen> characters, by default: 64");
        vallen.setArgName("max");
        opts.addOption(vallen);
        opts.addOption("c", "compact", false, "dump without attribute names");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e) {
            exit("ReadDcm: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = ReadDcm.class.getPackage();
            System.out.println("ReadDcm v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().isEmpty()) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }


    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'ReadDcm -h' for more information.");
        System.exit(1);
    }


    /*读取文件开始，参数为文件路径*/
    public  static void read(String filePath) {
        ReadDcm ReadDcm = new ReadDcm();
        File ifile = new File(filePath);
        try {
            ReadDcm.dump(ifile);
        } catch (IOException e) {
            System.err.println("ReadDcm: Failed to dump " + ifile + ": "
                    + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

    }
    public static void read(File file) {
        ReadDcm ReadDcm = new ReadDcm();
        File ifile = file;
        try {
            ReadDcm.dump(ifile);
        } catch (IOException e) {
            System.err.println("ReadDcm: Failed to dump " + ifile + ": "
                    + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static int parseInt(String s, String opt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {
            // parameter is not a valid integer; fall through to exit
        }
        exit("illegal argument for option -" + opt);
        throw new RuntimeException();
    }

    private StringBuffer line = new StringBuffer();
    private char[] cbuf = new char[64];
    private boolean withNames = true;
    private int maxWidth = DEF_MAX_WIDTH;
    private int maxValLen = DEF_MAX_VAL_LEN;

    public final void setMaxValLen(int maxValLen) {
        this.maxValLen = maxValLen;
    }


    public final void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }


    public final void setWithNames(boolean withNames) {
        this.withNames = withNames;
    }

    public void dump(File ifile) throws IOException {
        DicomInputStream dis = new DicomInputStream(ifile);
        try {
            dis.setHandler(this);
            dis.readDicomObject(new BasicDicomObject(), -1);
        } finally {
            dis.close();
        }
    }

    public boolean readValue(DicomInputStream in) throws IOException {
        switch (in.tag()) {
            case Tag.Item:
                if (in.sq().vr() != VR.SQ && in.valueLength() != -1) {
                    outFragment(in);
                } else {
                    outItem(in);
                }
                break;
            case Tag.ItemDelimitationItem:
            case Tag.SequenceDelimitationItem:
                if (in.level() > 0)
                    outItem(in);
                break;
            default:
                outElement(in);
        }
        return true;
    }


    private void outElement(DicomInputStream in) throws IOException {
        String tag=outTag(in);
        outVR(in);
        outLen(in);
        if (hasItems(in)) {
            outLine(in);
            readItems(in);
        } else {
            String value=outValue(in);
            map.put(tag,value);
            outLine(in);
        }


    }

    private String outValue(DicomInputStream in) throws IOException {
        int tag = in.tag();
        VR vr = in.vr();
        byte[] val = in.readBytes(in.valueLength());
        DicomObject dcmobj = in.getDicomObject();
        boolean bigEndian = in.getTransferSyntax().bigEndian();
        line.append(" [");
        line.delete(0,line.length()-1);
        vr.promptValue(val, bigEndian, dcmobj.getSpecificCharacterSet(),
                cbuf, maxValLen, line);
        line.append("]");
        if (tag == Tag.SpecificCharacterSet
                || tag == Tag.TransferSyntaxUID
                || TagUtils.isPrivateCreatorDataElement(tag)) {
            dcmobj.putBytes(tag, vr, val, bigEndian);
        }
        if (tag == 0x00020000) {
            in.setEndOfFileMetaInfoPosition(
                    in.getStreamPosition() + vr.toInt(val, bigEndian));
        }

        return line.toString().replace("[","").replace("]","");

    }

    private boolean hasItems(DicomInputStream in) {
        return in.valueLength() == -1 || in.vr() == VR.SQ;
    }

    private void readItems(DicomInputStream in) throws IOException {
        in.readValue(in);
        in.getDicomObject().remove(in.tag());
    }

    private void outItem(DicomInputStream in) throws IOException {
        outTag(in);
        outLen(in);
        outLine(in);
        in.readValue(in);
    }

    private void outFragment(DicomInputStream in) throws IOException {
        outTag(in);
        outLen(in);
        in.readValue(in);
        DicomElement sq = in.sq();
        byte[] data = sq.removeFragment(0);
        boolean bigEndian = in.getTransferSyntax().bigEndian();
        line.append(" [");
        sq.vr().promptValue(data, bigEndian, null, cbuf, maxValLen, line);
        line.append("]");
        outLine(in);
    }

    private String outTag(DicomInputStream in) {
        line.setLength(0);
        line.append(in.tagPosition()).append(':');
        for (int i = in.level(); i > 0; --i)
            line.append('>');
        line.delete(0,line.length()-1);
        TagUtils.toStringBuffer(in.tag(), line);
        return line.toString().replace("(","").replace(")","").replace(":","");
    }

    private void outVR(DicomInputStream in) {
        line.append(" ").append(in.vr());
    }

    private void outLen(DicomInputStream in) {
        line.append(" #").append(in.valueLength());
    }

    private void outLine(DicomInputStream in) {
        if (withNames)
            line.append(" ").append(in.getDicomObject().nameOf(in.tag()));
        if (line.length() > maxWidth)
            line.setLength(maxWidth);
        System.out.println(line);
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }
}
