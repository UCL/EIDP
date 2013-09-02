/*
 * Browser.java
 *
 * Created on April 24, 2006, 11:52 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.eidp.webctrl;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
/**
 *
 * @author rusch
 */
public class Browser {
    
    protected String userAgent;
    protected String company;          // Firmenname des Herstellers
    protected String version;          // Version
    protected double jsversion;          // JavaScript-Version
    protected int mainVersion;      // Hauptversion
    protected double minorVersion;     // Unterversion
    protected String os;               // Betriebssystem
    protected String language = "de";  // Sprachcode Standard
    protected Locale locale;           // Locale-Objekt mit den aktuellen
    // Spracheinstellungen
    private Hashtable supportedLanguages; // Unterst�tzte Sprachen
    
    // BrowserType-Variables
    boolean is_nav  =    false;
    boolean is_nav2 =    false;
    boolean is_nav3 =    false;
    boolean is_nav4 =    false;
    boolean is_nav4up =  false;
    boolean is_navonly = false;
    
    boolean is_nav6 =    false;
    boolean is_nav6up =  false;
    boolean is_gecko =   false;
    
    boolean is_ie     = false;
    boolean is_ie3    = false;
    boolean is_ie4    = false;
    boolean is_ie4up  = false;
    boolean is_ie5    = false;
    boolean is_ie5_5  = false;
    boolean is_ie5up  = false;
    boolean is_ie5_5up =false;
    boolean is_ie6    = false;
    boolean is_ie6up  = false;
    
    boolean is_aol   = false;
    boolean is_aol3  = false;
    boolean is_aol4  = false;
    boolean is_aol5  = false;
    boolean is_aol6  = false;
    
    boolean is_opera =    false;
    boolean is_opera2 =   false;
    boolean is_opera3 =   false;
    boolean is_opera4 =   false;
    boolean is_opera5 =   false;
    boolean is_opera5up = false;
    
    boolean is_webtv = false;
    
    boolean is_TVNavigator = false;
    boolean is_AOLTV = false;
    
    boolean is_hotjava = false;
    boolean is_hotjava3 = false;
    boolean is_hotjava3up = false;
    
    /** Creates a new instance of Browser */
    public Browser(HttpServletRequest request) {
        this.initialize(request);
    }
    
    public void initialize(HttpServletRequest request) {
        this.supportedLanguages = new Hashtable(2);
        this.supportedLanguages.put("en", "");
        this.supportedLanguages.put("de", "");
        this.setUserAgent(request.getHeader("User-Agent"));
        this.setVersion();
        this.setMainVersion();
        this.setMinorVersion();
        this.setBrowserType();
        this.setOs();
        this.setLanguage(request);
        this.setLocale();
        this.setJavaScriptVersion();
        this.setBrowsername();
    }
    
    public void setUserAgent(String httpUserAgent) {
        this.userAgent = httpUserAgent.toLowerCase();
    }
    
    public void setBrowserType() {
        // Note: Opera and WebTV spoof Navigator.  We do strict client detection.
        // If you want to allow spoofing, take out the tests for opera and webtv.
        boolean is_nav  = ((userAgent.indexOf("mozilla")!=-1) && (userAgent.indexOf("spoofer")==-1)
        && (userAgent.indexOf("compatible") == -1) && (userAgent.indexOf("opera")==-1)
        && (userAgent.indexOf("webtv")==-1) && (userAgent.indexOf("hotjava")==-1));
        boolean is_nav2 = (is_nav && (this.mainVersion == 2));
        boolean is_nav3 = (is_nav && (this.mainVersion == 3));
        boolean is_nav4 = (is_nav && (this.mainVersion == 4));
        boolean is_nav4up = (is_nav && (this.mainVersion >= 4));
        boolean is_navonly      = (is_nav && ((userAgent.indexOf(";nav") != -1) ||
                (userAgent.indexOf("; nav") != -1)) );
        boolean is_nav6 = (is_nav && (this.mainVersion == 5));
        boolean is_nav6up = (is_nav && (this.mainVersion >= 5));
        boolean is_gecko = (userAgent.indexOf("gecko") != -1);
        
        
        boolean is_ie     = ((userAgent.indexOf("msie") != -1) && (userAgent.indexOf("opera") == -1));
        boolean is_ie3    = (is_ie && (this.mainVersion < 4));
        boolean is_ie4    = (is_ie && (this.mainVersion == 4) && (userAgent.indexOf("msie 4")!=-1) );
        boolean is_ie4up  = (is_ie && (this.mainVersion >= 4));
        boolean is_ie5    = (is_ie && (this.mainVersion == 4) && (userAgent.indexOf("msie 5.0")!=-1) );
        boolean is_ie5_5  = (is_ie && (this.mainVersion == 4) && (userAgent.indexOf("msie 5.5") !=-1));
        boolean is_ie5up  = (is_ie && !is_ie3 && !is_ie4);
        boolean is_ie5_5up =(is_ie && !is_ie3 && !is_ie4 && !is_ie5);
        boolean is_ie6    = (is_ie && (this.mainVersion == 4) && (userAgent.indexOf("msie 6.")!=-1) );
        boolean is_ie6up  = (is_ie && !is_ie3 && !is_ie4 && !is_ie5 && !is_ie5_5);
        
        // KNOWN BUG: On AOL4, returns false if IE3 is embedded browser
        // or if this is the first browser window opened.  Thus the
        // variables is_aol, is_aol3, and is_aol4 aren"t 100% reliable.
        boolean is_aol   = (userAgent.indexOf("aol") != -1);
        boolean is_aol3  = (is_aol && is_ie3);
        boolean is_aol4  = (is_aol && is_ie4);
        boolean is_aol5  = (userAgent.indexOf("aol 5") != -1);
        boolean is_aol6  = (userAgent.indexOf("aol 6") != -1);
        
        boolean is_opera = (userAgent.indexOf("opera") != -1);
        boolean is_opera2 = (userAgent.indexOf("opera 2") != -1 || userAgent.indexOf("opera/2") != -1);
        boolean is_opera3 = (userAgent.indexOf("opera 3") != -1 || userAgent.indexOf("opera/3") != -1);
        boolean is_opera4 = (userAgent.indexOf("opera 4") != -1 || userAgent.indexOf("opera/4") != -1);
        boolean is_opera5 = (userAgent.indexOf("opera 5") != -1 || userAgent.indexOf("opera/5") != -1);
        boolean is_opera5up = (is_opera && !is_opera2 && !is_opera3 && !is_opera4);
        
        boolean is_webtv = (userAgent.indexOf("webtv") != -1);
        
        boolean is_TVNavigator = ((userAgent.indexOf("navio") != -1) || (userAgent.indexOf("navio_aoltv") != -1));
        boolean is_AOLTV = is_TVNavigator;
        
        boolean is_hotjava = (userAgent.indexOf("hotjava") != -1);
        boolean is_hotjava3 = (is_hotjava && (this.mainVersion == 3));
        boolean is_hotjava3up = (is_hotjava && (this.mainVersion >= 3));
    }
    
    public void setJavaScriptVersion() {
        // *** JAVASCRIPT VERSION CHECK ***
        double is_js;
        if (is_nav2 || is_ie3) is_js = 1.0;
        else if (is_nav3) is_js = 1.1;
        else if (is_opera5up) is_js = 1.3;
        else if (is_opera) is_js = 1.1;
        else if ((is_nav4 && (this.minorVersion <= 4.05)) || is_ie4) is_js = 1.2;
        else if ((is_nav4 && (this.minorVersion > 4.05)) || is_ie5) is_js = 1.3;
        else if (is_hotjava3up) is_js = 1.4;
        else if (is_nav6 || is_gecko) is_js = 1.5;
        // NOTE: In the future, update this code when newer versions of JS
        // are released. For now, we try to provide some upward compatibility
        // so that future versions of Nav and IE will show they are at
        // *least* JS 1.x capable. Always check for JS version compatibility
        // with > or >=.
        else if (is_nav6up) is_js = 1.5;
        // NOTE: ie5up on mac is 1.4
        else if (is_ie5up) is_js = 1.3;
        
        // HACK: no idea for other browsers; always check for JS version with > or >=
        else is_js = 0.0;
        
        this.jsversion = is_js;
    }
    
    /**
     * Liefert die JavaScriptVersion des verwendeten Browsers.
     */
    public double gsetJavaScriptVersion() {
        return this.jsversion;
    }
    
    private void setBrowsername() {
        if (is_ie) {
            this.company = "Microsoft Internet-Explorer";
        } else if (is_opera) {
            this.company = "Opera";
        } else if (this.userAgent.indexOf("firefox") > -1) {
            this.company = "Mozilla Firefox";
        } else if (is_nav) {
            this.company = "Netscape Navigator";
        } else if (is_aol) {
            this.company = "AOL";
        } else if (is_webtv) {
            this.company = "WebTV";
        } else if (is_hotjava) {
            this.company = "Hot-Java";
        } else if (is_TVNavigator) {
            this.company = "TV-Navigator";
        } else if (is_AOLTV) {
            this.company = "AOL-TV";
        } else {
            this.company = "unbekannt";
        }
    }
    
    /**
     * Liefert den Firmennamen des Herstellers des verwendeten Browsers.
     */
    public String getBrowsername() {
        return this.company;
    }
    
    private void setVersion() {
        int tmpPos;
        String tmpString;
        
        if (this.company == "Microsoft") {
            String str = this.userAgent.substring(this.userAgent.indexOf("msie") + 5);
            this.version = str.substring(0, str.indexOf(";"));
        } else {
            tmpString = (this.userAgent.substring(tmpPos = (this.userAgent.indexOf("/")) + 1,
                    tmpPos + this.userAgent.indexOf(" "))).trim();
            this.version = tmpString.substring(0, tmpString.indexOf(" "));
        }
    }
    
    /**
     * Liefert die Versionsnummer des verwendeten Browsers.
     */
    public String getVersion() {
        return this.version;
    }
    
    private void setMainVersion() {
        try{
            this.mainVersion = Integer.parseInt(this.version.substring(0, this.version.indexOf(".")));
        }catch(NumberFormatException e){
            System.out.println("NumberFormatException in Browser.setMainVersion()");
        }
    }
    
    /**
     * Liefert die Hauptversionsnummer des verwendeten Browsers.
     */
    public int getMainVersion() {
        return this.mainVersion;
    }
    
    private void setMinorVersion() {
        try{
            this.minorVersion = Double.parseDouble(this.version);
        }catch(NumberFormatException e){
            System.out.println("NumberFormatException in Browser.setMinorVersion()");
        }
    }
    
    /**
     * Liefert die Unterversionsnummer des verwendeten Browsers.
     */
    public double getMinorVersion() {
        return this.minorVersion;
    }
    
    private void  setOs() {
        // *** PLATFORM ***
        boolean is_win   = ( (this.userAgent.indexOf("win")!=-1) || (this.userAgent.indexOf("16bit")!=-1) );
        // NOTE: On Opera 3.0, the userAgent string includes "Windows 95/NT4" on all
        //        Win32, so you can"t distinguish between Win95 and WinNT.
        boolean is_win95 = ((this.userAgent.indexOf("win95")!=-1) || (this.userAgent.indexOf("windows 95")!=-1));
        
        // is this a 16 bit compiled version?
        boolean is_win16 = ((this.userAgent.indexOf("win16")!=-1) ||
                (this.userAgent.indexOf("16bit")!=-1) || (this.userAgent.indexOf("windows 3.1")!=-1) ||
                (this.userAgent.indexOf("windows 16-bit")!=-1) );
        
        boolean is_win31 = ((this.userAgent.indexOf("windows 3.1")!=-1) || (this.userAgent.indexOf("win16")!=-1) ||
                (this.userAgent.indexOf("windows 16-bit")!=-1));
        
        boolean is_winme = ((this.userAgent.indexOf("win 9x 4.90")!=-1));
        boolean is_win2k = ((this.userAgent.indexOf("windows nt 5.0")!=-1));
        
        boolean is_winxp = ((this.userAgent.indexOf("windows nt 5.1")!=-1));
        
        // NOTE: Reliable detection of Win98 may not be possible. It appears that:
        //       - On Nav 4.x and before you"ll get plain "Windows" in userAgent.
        //       - On Mercury client, the 32-bit version will return "Win98", but
        //         the 16-bit version running on Win98 will still return "Win95".
        boolean is_win98 = ((this.userAgent.indexOf("win98")!=-1) || (this.userAgent.indexOf("windows 98")!=-1));
        boolean is_winnt = ((this.userAgent.indexOf("winnt")!=-1) || (this.userAgent.indexOf("windows nt")!=-1));
        boolean is_win32 = (is_win95 || is_winnt || is_win98 ||
                ((this.getMainVersion() >= 4) && (this.userAgent.indexOf("Win32")!=-1)) ||
                (this.userAgent.indexOf("win32")!=-1) || (this.userAgent.indexOf("32bit")!=-1));
        
        boolean is_os2   = ((this.userAgent.indexOf("os/2")!=-1) ||
                (this.version.indexOf("OS/2")!=-1) ||
                (this.userAgent.indexOf("ibm-webexplorer")!=-1));
        
        boolean is_mac    = (this.userAgent.indexOf("mac")!=-1);
        // hack ie5 js version for mac
        if (is_mac && is_ie5up) this.jsversion = 1.4;
        boolean is_mac68k = (is_mac && ((this.userAgent.indexOf("68k")!=-1) ||
                (this.userAgent.indexOf("68000")!=-1)));
        boolean is_macppc = (is_mac && ((this.userAgent.indexOf("ppc")!=-1) ||
                (this.userAgent.indexOf("powerpc")!=-1)));
        
        boolean is_sun   = (this.userAgent.indexOf("sunos")!=-1);
        boolean is_sun4  = (this.userAgent.indexOf("sunos 4")!=-1);
        boolean is_sun5  = (this.userAgent.indexOf("sunos 5")!=-1);
        boolean is_suni86= (is_sun && (this.userAgent.indexOf("i86")!=-1));
        boolean is_irix  = (this.userAgent.indexOf("irix") !=-1);    // SGI
        boolean is_irix5 = (this.userAgent.indexOf("irix 5") !=-1);
        boolean is_irix6 = ((this.userAgent.indexOf("irix 6") !=-1) || (this.userAgent.indexOf("irix6") !=-1));
        boolean is_hpux  = (this.userAgent.indexOf("hp-ux")!=-1);
        boolean is_hpux9 = (is_hpux && (this.userAgent.indexOf("09.")!=-1));
        boolean is_hpux10= (is_hpux && (this.userAgent.indexOf("10.")!=-1));
        boolean is_aix   = (this.userAgent.indexOf("aix") !=-1);      // IBM
        boolean is_aix1  = (this.userAgent.indexOf("aix 1") !=-1);
        boolean is_aix2  = (this.userAgent.indexOf("aix 2") !=-1);
        boolean is_aix3  = (this.userAgent.indexOf("aix 3") !=-1);
        boolean is_aix4  = (this.userAgent.indexOf("aix 4") !=-1);
        boolean is_linux = (this.userAgent.indexOf("inux")!=-1);
        boolean is_sco   = (this.userAgent.indexOf("sco")!=-1) || (this.userAgent.indexOf("unix_sv")!=-1);
        boolean is_unixware = (this.userAgent.indexOf("unix_system_v")!=-1);
        boolean is_mpras    = (this.userAgent.indexOf("ncr")!=-1);
        boolean is_reliant  = (this.userAgent.indexOf("reliantunix")!=-1);
        boolean is_dec   = ((this.userAgent.indexOf("dec")!=-1) || (this.userAgent.indexOf("osf1")!=-1) ||
                (this.userAgent.indexOf("dec_alpha")!=-1) || (this.userAgent.indexOf("alphaserver")!=-1) ||
                (this.userAgent.indexOf("ultrix")!=-1) || (this.userAgent.indexOf("alphastation")!=-1));
        boolean is_sinix = (this.userAgent.indexOf("sinix")!=-1);
        boolean is_freebsd = (this.userAgent.indexOf("freebsd")!=-1);
        boolean is_bsd = (this.userAgent.indexOf("bsd")!=-1);
        boolean is_unix  = ((this.userAgent.indexOf("x11")!=-1) || is_sun || is_irix || is_hpux ||
                is_sco ||is_unixware || is_mpras || is_reliant ||
                is_dec || is_sinix || is_aix || is_linux || is_bsd || is_freebsd);
        
        boolean is_vms   = ((this.userAgent.indexOf("vax")!=-1) || (this.userAgent.indexOf("openvms")!=-1));
        
        if(is_win16){
            this.os = "Windows 3.x";
        }else if(is_win95){
            this.os = "Windows 95";
        }else if(is_winme){
            this.os = "Windows Mellenium Edition";
        }else if(is_win2k){
            this.os = "Windows 2000";
        }else if(is_winxp){
            this.os = "Windows XP";
        }else if(is_win98){
            this.os = "Windows 98";
        }else if(is_winnt){
            this.os = "Windows NT";
        }else if(is_os2){
            this.os = "OS/2";
        }else if(is_macppc){
            this.os = "Macintosh Power PC";
        }else if(is_mac){
            this.os = "Macintosh";
        }else if(is_sun || is_sun4 || is_sun5 || is_suni86){
            this.os = "Sun Solaris";
        }else if(is_linux){
            this.os = "Linux";
        }else if(is_unix){
            this.os = "Unix";
        }else if(is_bsd){
            this.os = "BSD";
        }else if(is_freebsd){
            this.os = "FreeBSD";
        }else if(is_hpux){
            this.os = "HP Ux";
        }
    }
    
    /**
     * Liefert den Namen des Betriebssystems.
     */
    public String getOs() {
        return this.os;
    }
    
    private void setLanguage(HttpServletRequest request) {
        String prefLanguage = request.getHeader("Accept-Language");
        
        if (prefLanguage != null) {
            String language = null;
            StringTokenizer st = new StringTokenizer(prefLanguage, ",");
            
            int elements = st.countTokens();
            
            for (int idx = 0; idx < elements; idx++) {
                if (this.supportedLanguages.containsKey((language = st.nextToken()))) {
                    this.language = this.parseLocale(language);
                }
            }
        }
    }
    
  /*
   * Hilfsfunktion fr setLanguage().
   */
    private String parseLocale(String language) {
        StringTokenizer st = new StringTokenizer(language, "-");
        
        if (st.countTokens() == 2) {
            return st.nextToken();
        } else {
            return language;
        }
    }
    
    /**
     * Liefert das L�nderk�rzel der vom Benutzer
     * bevorzugten Sprache.
     */
    public String getLanguage() {
        return this.language;
    }
    
    private void setLocale() {
        this.locale = new Locale(this.language, "");
    }
    
    /**
     * Liefert ein Locale-Objekt mit der Sprach-Prferenz des verwendeten Browsers
     */
    public Locale getLocale() {
        return this.locale;
    }
}

