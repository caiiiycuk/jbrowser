/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.interfaces;

/**
 * Builtin pages such as Download manager
 * @see http://kb.mozillazine.org/Chrome_URLs
 * @see http://kb.mozillazine.org/Dev_:_Firefox_Chrome_URLs
 * @author caiiiycuk
 */
public interface ChromePages {

    //--from http://kb.mozillazine.org/Chrome_URLs
    public static String Addons = "chrome://mozapps/content/extensions/extensions.xul";
    public static String Downloads = "chrome://mozapps/content/downloads/downloads.xul";
    public static String SavedPasswords = "chrome://passwordmgr/content/passwordManager.xul";
    public static String ErrorConsole = "chrome://global/content/console.xul";
    public static String CharacterEncodingCustomizeList = "chrome://global/content/customizeCharset.xul";// 	AD 	View->Character Encoding->Customize List
    public static String ToolbarsCustomize = "chrome://global/content/customizeToolbar.xul";// 	D 	View->Toolbars->Customize...
    public static String FindDialog = "chrome://global/content/finddialog.xul";// 	RU 	Find dialog?
    public static String FilePrint = "chrome://global/content/printdialog.xul";// 	D 	File->Print
    public static String FilePageSetup = "chrome://global/content/printPageSetup.xul";// 	D 	File->Page Setup
    public static String FilePrintPreview = "chrome://global/content/printPreviewProgress.xul";// 	D 	File->Print Preview message shown while preparing to preview the document
    public static String Config = "chrome://global/content/config.xul";// 	AD 	about:config
    public static String Certificates = "chrome://pippki/content/pref-certs.xul";//	Tools->Options->Advanced->Certificates
}
