package com.jovistar.commons.util;

import java.util.Vector;

public class Strings {

    public final char CH_OPENTAG = '<';
    public final char CH_CLOSETAG = '>';

    public final char space = ' ';
    public final char dot = '.';
    public final String STR_OPENTAG = "</";
    public final String STR_CLOSETAG = "\">";

    //1/7 is left for icon

    static Strings instance;

    public static Strings getInstance() {

        if (instance == null) {
            instance = new Strings();
        }
        return instance;
    }

    private String padSpaces(StringBuffer text, int num) {
        for(int i=0;i<num;i++)
            text.append(space);
        return text.toString();
    }

    private String padDots(StringBuffer text, int num) {
        for(int i=0;i<num;i++)
            text.append(dot);
        return text.toString();
    }

    private String fitText2Pixel(String text, int pixlen) {
        //int textwidthpix = Constants.getInstance().TEXT_FONT.stringWidth(text);
        if(text==null) return "";
        int textwidthpix = 10;//CCMIDlet.instance.appfont.stringWidth(text);
        int textwidth = text.length();
        int per = ((pixlen*100) / (textwidthpix==0?1:textwidthpix));
        int textwidthnew = per*textwidth /100;

        StringBuffer sb = new StringBuffer(pixlen/*shud be far less value*/);
        if (text != null && textwidth >= textwidthnew) {
            sb.append(text);
            if(textwidthnew>3){
                sb.setLength(textwidthnew-3);
                padDots(sb,3);
            }
        } else if (text != null && textwidth < textwidthnew) {
            padSpaces(sb.append(text), textwidthnew-textwidth);
        }

        return sb.toString();
    }

    public String getText(String text) {
//        int screewidthpixavailable
//                = stringPixalWidthList;
//        text = fitText2Pixel(text, screewidthpixavailable);

        return text;
    }

    public String concat(Object text1, Object text2, Object text3, Object text4, Object text5, Object text6) {
        StringBuffer sb = new StringBuffer();
        if(text1!=null)
            sb.append(text1);
        if(text2!=null)
            sb.append(text2);
        if(text3!=null)
            sb.append(text3);
        if(text4!=null)
            sb.append(text4);
        if(text5!=null)
            sb.append(text5);
        if(text6!=null)
            sb.append(text6);

        String s = sb.toString();
        sb = null;
        return s;
    }

    public String getText(String text, String text2) {
//        int screewidthpixavailable
//                = stringPixalWidthList/2;
//        text = fitText2Pixel(text, screewidthpixavailable);
//        text2 = fitText2Pixel(text2, screewidthpixavailable);

        return text.concat(text2);
    }

    public String getText(String text, String text2, String text3) {
//        int screewidthpixavailable
//                = stringPixalWidthList/3;

//        text = fitText2Pixel(text, screewidthpixavailable);
//        text2 = fitText2Pixel(text2, screewidthpixavailable);
//        text3 = fitText2Pixel(text3, screewidthpixavailable);

        return text.concat(text2).concat(text3);
    }

    public Vector split(String s, char c) {
        Vector parts = new Vector();
        if (s != null) {
            int lastfound = 0;
            int pos = 0;
            while ((lastfound = s.indexOf(c, pos)) != - 1) {
                parts.addElement(s.substring(pos, lastfound));
                pos = lastfound + 1;
            }
            if (pos < s.length()) {
                parts.addElement(s.substring(pos));
            }
        }

        return parts;
    }

/*
    //put comma separated strings into vector
    public static void getCSV2StringVector(String string, Vector list) {
        //Vector list = new Vector();
        //String newstr = string.replace(' ', '\u0000');//u0000 is null char
        String str;
        XStringTokenizer tok = new XStringTokenizer(newstr, ",");

        if (tok.hasMoreElements()) {
            str = (String) tok.nextElement();
            list.addElement(str.trim());
        }
        //return list;
    }

    //put string vector data as csv string
    public static String getStringVector2CSV(Vector list) {
        StringBuffer csv = new StringBuffer();

        for(int i=0,n=list.size();i<n;i++)
            csv.append((String)list.elementAt(i)).append(",");
        return csv.toString();
    }

    public String getText(String text, String text2, String text3) {
        int screenwidth = AboutUI.screenCharWidth;
        int wordlen = screenwidth/3;
        StringBuffer sb = new StringBuffer(screenwidth*2);
        if (text != null && text.length() >= wordlen) {
            sb.append(text).setLength(wordlen-3);
            padDots(sb, 3);
        } else if (text != null && text.length() < wordlen) {
            padSpaces(sb.append(text), 20);
            sb.setLength(wordlen);
        }
        if (text2 != null && text2.length() >= wordlen) {
            sb.append(text2).setLength(2*wordlen-3);
            padDots(sb,3);
        } else if (text2 != null && text2.length() < wordlen) {
            padSpaces(sb.append(text2), wordlen-text2.length());
        }
        if (text3 != null && text3.length() >= wordlen) {
            sb.append(text3).setLength(3*wordlen-3);
            padDots(sb,3);
        } else if (text3 != null && text3.length() < wordlen) {
            padSpaces(sb.append(text3), wordlen-text3.length());
        }

        return sb.toString();
    }

    public String getText(String text, String text2) {
        int screenwidth = AboutUI.screenCharWidth;
        int wordlen = screenwidth/3;
        StringBuffer sb = new StringBuffer(screenwidth*2);
        if (text != null && text.length() >= wordlen) {
            sb.append(text).setLength(wordlen-3);
            sb.append(dots);
        } else if (text != null && text.length() < wordlen) {
            sb.append(text).append(spaces).setLength(wordlen);
        }
        if (text2 != null && text2.length() >= wordlen) {
            sb.append(text2).setLength(2*wordlen-3);
            sb.append(dots);
        } else if (text2 != null && text2.length() < wordlen) {
            sb.append(text2).append(spaces).setLength(2*wordlen);
        }

        return sb.toString();
    }

    public String[] split(String s, int i) {
        int len = s.length();
        String[] sa = new String[len / i + (((len % i) > 0) ? 1 : 0)];
        for (int j = 0; j < (sa.length - 1) || ((sa[j] = s.substring(j * i)) == null); sa[j] = s.substring(j * i, j++ * i + i));
        return sa;
    }

    public boolean isNull(String s) {
        return (s == null || s.trim().length() == 0);
    }

    public String longToDateString(long date) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(date));

        StringBuffer buffer = new StringBuffer();
        buffer.append(c.get(Calendar.MONTH) + 1).append("/").append(c.get(Calendar.DAY_OF_MONTH)).append("/").append(c.get(Calendar.YEAR)).append(" ").append(c.get(Calendar.HOUR_OF_DAY)).append(":");

        int min = c.get(Calendar.MINUTE);
        if (min > 10) {
            buffer.append(min);
        } else {
            buffer.append("0").append(min);
        }

        return buffer.toString();
    }

    public String getSizeString(long number) {
        String suffix;
        double sz;

        if (number > GIB) {
            //gb
            sz = (number / GIB);
            suffix = "GB";
        } else if (number > MIB) {
            //mb
            sz = (number / MIB);
            suffix = "MB";
        } else if (number > KIB) {
            //kb
            sz = number / KIB;
            suffix = "KB";
        } else {
            sz = number;
            suffix = "b";
        }

        return (Math.ceil(sz * 100.00) / 100.0) + suffix;
    }
    public String URLEncode(String url) {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < url.length(); i++) {
            switch (url.charAt(i)) {
                case ' ':
                    buffer.append("%20");
                    break;
                case '+':
                    buffer.append("%2b");
                    break;
                case '\'':
                    buffer.append("%27");
                    break;
                case '<':
                    buffer.append("%3c");
                    break;
                case '>':
                    buffer.append("%3e");
                    break;
                case '#':
                    buffer.append("%23");
                    break;
                case '%':
                    buffer.append("%25");
                    break;
                case '{':
                    buffer.append("%7b");
                    break;
                case '}':
                    buffer.append("%7d");
                    break;
                case '\\':
                    buffer.append("%5c");
                    break;
                case '^':
                    buffer.append("%5e");
                    break;
                case '~':
                    buffer.append("%73");
                    break;
                case '[':
                    buffer.append("%5b");
                    break;
                case ']':
                    buffer.append("%5d");
                    break;
                case '-':
                    buffer.append("%2D");
                    break;
                case '/':
                    buffer.append("%2F");
                    break;
                case ':':
                    buffer.append("%3A");
                    break;
                case '=':
                    buffer.append("%3D");
                    break;
                case '?':
                    buffer.append("%3F");
                    break;
                case '\r':
                    buffer.append("%0D");
                    break;
                case '\n':
                    buffer.append("%0A");
                    break;
                default:
                    buffer.append(url.charAt(i));
                    break;
            }
        }
        return buffer.toString();
    }
*/
    public static boolean isNumeric(String str)  
    {  
      try  
      {  
        double d = Double.parseDouble(str);  
      }  
      catch(NumberFormatException nfe)  
      {  
        return false;  
      }  
      return true;  
    }
}
