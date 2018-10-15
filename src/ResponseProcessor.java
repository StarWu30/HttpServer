import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.HashMap;

public class ResponseProcessor {
    private static final Logger logger = LogManager.getLogger(Cache.class.getCanonicalName());
    private InputStream in;
    private OutputStream out;
    private Writer writer;
    private Reader reader;
    private File rootDirectory;
    private Cache cache;

    // 404的html网页信息
    private String errorbody = new StringBuilder("<HTML>\r\n")
            .append("<HEAD><TITLE>File Not Found<TITLE>\r\n")
            .append("</HEAD>\r\n")
            .append("<BODY>")
            .append("<H1>HTTP Error 404: File Not Found</H1>\r\n")
            .append("</BODY></HTML>\r\n").toString();

    // 构造函数
    // 接收所需参数输入
    public ResponseProcessor(File rootDirectory, InputStream in, OutputStream out,
                             Writer writer, Reader reader, Cache cache){
        this.rootDirectory = rootDirectory;
        this.in = in;
        this.out = out;
        this.writer = writer;
        this.reader = reader;
        this.cache = cache;
    }

    // 处理HEAD请求方法
    public void doHEAD (String responseCode, String contentType, int length) {
        try{
            writer.write(responseCode + "\r\n");
            Date now = new Date();
            writer.write("Date: " + now + "\r\n");
            writer.write("Server: MyHttpServer\r\n");
            writer.write("Content-length: " + length + "\r\n");
            writer.write("Content-type: " + contentType + "\r\n\r\n");
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.WARN, "Error to write.", e);
        }
    }

    // 处理GET请求方法
    public void doGET(File theFile, String contentType, String root, String version){
        HashMap<String, byte[]> hashMap = cache.getHashMap();
        try{
//          判断文件是否可读（存在）
            if(cache.isCanread()){
//                byte[] theData = Files.readAllBytes(theFile.toPath());
//              通过文件名找到对应文件的数据
                byte[] theData = hashMap.get(theFile.getPath());
                if( version.startsWith("HTTP/")){
                    doHEAD("HTTP/1.0 200 OK", contentType, theData.length);
                }
                out.write(theData);
                out.flush();
            }else{
                if(version.startsWith("HTTP/")){
                    doHEAD("HTTP/1.0 501 Not Implemented", "text/html; charset=utf-8", errorbody.length());
                }
                writer.write(errorbody);
                writer.flush();
            }
        }catch (IOException ex){
            logger.log(Level.WARN, "Error to write.", ex);
        }
    }

    // 处理POST请求方法
    public void doPOST(String contentType) {
        StringBuilder str = new StringBuilder();
        int c;
        String username = null;
        try {
            while (true) {
                c = in.read();
                str.append((char)c);
//               如果密码框输入password才能正确响应
//               否则将无限循环
                System.out.println(str);
                if(str.toString().endsWith("password")) {
                    break;
                }
            }
//          分离出用户名
            String[] s = str.toString().split("Name=");
            String[] s1 = s[1].split("&");
            username = s1[0];
        } catch (IOException e) {
            logger.log(Level.WARN, "Error to read.", e);
        }
        String body = new StringBuilder("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta ContentType: text/html; charset=\"utf-8\">\n" +
                "\n" +
                "<title>MyHttpServer</title>\n" +
                "\t<style>.alsp{ font-family:\"微软雅黑\"; font-size:50px}</style>\n" +
                "\n" +
                "</head>\n" +
                "\t<script type=\"text/javascript\">\n" +
                "\t\tfunction disptime(){\n" +
                "\t\t\tvar today = new Date();\n" +
                "\t\t\tvar hh = today.getHours();\n" +
                "\t\t\tvar mm = today.getMinutes();\n" +
                "\t\t\tvar ss = today.getSeconds();\n" +
                "\t\t\tdocument.getElementById(\"myclock\").innerHTML=\"<h1>北京时间—\"+hh+\":\"+mm+\":\"+ss+\"</h1>\"\n" +
                "\t\t\t}\n" +
                "\t\t\tvar mytime = setInterval(\"disptime()\",1000);\n" +
                "\t</script>\t\n" +
                "\n" +
                "\n" +
                "<body>\n" +
                "\t<div class=\"alsp\">\n" +
                "\t<style> \n" +
                "      body{text-align:center} \n" +
                "    </style> \n" +
                "\t<br>\n" +
                "\t<div >欢迎您回来, ").append(username)
                .append("</div><br>\n" +
                        "\t</div>\n" +
                        "\t<img src=\"img/view.jpg\" alt=\"壁纸\" heigh=\"250\" width=\"160\" /><br>\n" +
                        "\t<a href=\"img/view.jpg\">放大保存图片</a><br><br>\n" +
                        "\t<a href=\"music/Littleluck.mp3\">来一首音乐</a><br>\n" +
                        "\t\n" +
                        "\t<div id=\"myclock\"></div><br><br><br>\n" +
                        "\t<body style=\"background-image:url(img/background.jpg)\">\n" +
                        "\t<script language=JavaScript>\n" +
                        "\n" +
                        "var caution = false\n" +
                        "function setCookie(name, value, expires, path, domain, secure){\n" +
                        "var curCookie = name + \"=\" + escape(value) +\n" +
                        "((expires) ? \"; expires=\" + expires.toGMTString() : \"\") +\n" +
                        "((path) ? \"; path=\" + path : \"\") +\n" +
                        "((domain) ? \"; domain=\" + domain : \"\") +\n" +
                        "((secure) ? \"; secure\" : \"\")\n" +
                        "if (!caution || (name + \"=\" + escape(value)).length<= 4000)\n" +
                        "document.cookie = curCookie\n" +
                        "else\n" +
                        "if (confirm(\"Cookie exceeds 4KB and will be cut!\"))\n" +
                        "document.cookie = curCookie\n" +
                        "}\n" +
                        "function getCookie(name) {\n" +
                        "var prefix = name + \"=\"\n" +
                        "var cookieStartIndex = document.cookie.indexOf(prefix)\n" +
                        "if (cookieStartIndex == -1)\n" +
                        "return null\n" +
                        "var cookieEndIndex = document.cookie.indexOf(\";\", cookieStartIndex+ prefix.length)\n" +
                        "if (cookieEndIndex == -1)\n" +
                        "cookieEndIndex = document.cookie.length\n" +
                        "return unescape(document.cookie.substring(cookieStartIndex +prefix.length, cookieEndIndex))\n" +
                        "}\n" +
                        "function deleteCookie(name, path, domain) {\n" +
                        "if (getCookie(name)) {\n" +
                        "document.cookie = name + \"=\" +\n" +
                        "((path) ? \"; path=\" + path : \"\") +\n" +
                        "((domain) ? \"; domain=\" + domain : \"\") +\n" +
                        "\"; expires=Thu, 01-Jan-70 00:00:01 GMT\"\n" +
                        "}\n" +
                        "}\n" +
                        "function fixDate(date) {\n" +
                        "var base = new Date(0)\n" +
                        "var skew = base.getTime()\n" +
                        "if (skew > 0)\n" +
                        "date.setTime(date.getTime() - skew)\n" +
                        "}\n" +
                        "var now = new Date()\n" +
                        "fixDate(now)\n" +
                        "now.setTime(now.getTime() + 365 * 24 * 60 * 60 * 1000)\n" +
                        "var visits = getCookie(\"counter\")\n" +
                        "if (!visits)\n" +
                        "visits = 1\n" +
                        "else\n" +
                        "visits = parseInt(visits) + 1\n" +
                        "setCookie(\"counter\", visits, now)\n" +
                        "document.write(\"您是第\" + visits + \"位访问本网页的！\")\n" +
                        "\n" +
                        "</script>\n" +
                        "</body>\n" +
                        "<body onload=\"disptime()\">  \n" +
                        "        <div id=\"myclock\"></div>  \n" +
                        "    </body>  \n" +
                        "\t\n" +
                        "</form>\n" +
                        "</html>").toString();
        byte[] theData = body.getBytes();
        try{
            doHEAD("HTTP/1.0 200 OK", contentType, theData.length);
            out.write(theData);
            out.flush();
        } catch (IOException e) {
            logger.log(Level.WARN, "Error to write.", e);
        }
    }

    // 处理PUT请求方法
    public void doPUT(){
        try{
            StringBuilder s = new StringBuilder();

            // 读取请求头
            while (true) {
                int c = in.read();
                s.append((char) c);
                // 以两个\r\n为结束符
                if(s.toString().endsWith("\r\n\r\n")){
                    break;
                }
            }

            // 根据请求头截取内容长度
            String cl = "Content-Length: ";
            if( !s.toString().contains("Content-Length: ")){
                cl = "content-length: ";
            }
            String temp = s.substring(s.indexOf(cl) + 16);
            String len = temp.substring(0,temp.indexOf("\r\n"));
            int length = 0;

            // 根据请求头获取内容种类
            // 默认使用.dat格式保存
            String type = ".dat";
            String ct = "Content-Type: ";
            if( !s.toString().contains("Content-Type: ")){
                ct = "content-type: ";
            }
            temp = s.substring(s.indexOf(ct) + 14);
            temp = temp.substring(0,temp.indexOf("\r\n"));
            String[] tokens = temp.split("/");
            if(tokens.length > 1){
                type = "." + tokens[1];
            }else {
                if(tokens != null)
                    type = "." + tokens[0];
            }

//          内容长度格式转换
//          String -> int
            try{
                length = Integer.parseInt(len);
            }catch (NumberFormatException e){
                logger.log(Level.WARN, "Error to format number.", e);
            }
            byte[] bytes = new byte[length];
            int i = 0;
//          根据内容长度读取文件
            while( i < length ){
                int c = in.read(bytes, i, length - i);
                i += c;
            }
            // 写入文件
            File file = new File("test" + type);
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(bytes);
            fout.flush();
//          返回一个200 OK的请求头
            doHEAD("HTTP/1.0 200 OK", "text/html", 0);
        } catch (IOException e) {
            logger.log(Level.WARN, "Error to read.", e);
        }

    }

    // 处理DELETE请求方法
    public void doDELETE(String filename){
        try{
            String root = rootDirectory.getPath();
            File file = new File(root, filename);
            if(file.exists()){
                file.delete();
                doHEAD("HTTP/1.1 200 OK","text/html",0);
            }else{
                doHEAD("HTTP/1.1 404 File Not Found","text/html", errorbody.length());
                writer.write(errorbody);
            }
        } catch (IOException e) {
            logger.log(Level.WARN, "Error to write.", e);
        }

    }

    // 处理OPTIONS请求方法
    public void doOPTIONS() {
        try{
            // 返回能处理GET、HEAD、POST、PUT、DELETE、OPTIONS命令的首部信息
            writer.write("HTTP/1.1 200 OK\r\n");
            Date now = new Date();
            writer.write("Date: " + now + "\r\n");
            writer.write("Server: MyHttpServer\r\n");
            writer.write("Allow: GET,HEAD,POST,PUT,DELETE,OPTIONS,TRACE\r\n");
            writer.write("Content-Sytle-Type: text/html\r\n");
            writer.write("Content-Length: 0\r\n");
            writer.write("Connection: close\r\n");
            writer.write("Content-Type: text/html; charset=utf-8\r\n\r\n");
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.WARN, "Error to write.", e);
        }

    }

    // 处理TRACE请求方法
    public void doTRACE(String requestLine){
        try{
            StringBuilder s = new StringBuilder();
            // 读取剩下部分请求头
            while (true) {
                int c = in.read();
                s.append((char) c);
                // 以两个\r\n为结束符
                if(s.toString().endsWith("\r\n\r\n")){
                    break;
                }
            }
            doHEAD("HTTP/1.1 200 OK", "message/http",0);
            writer.write(requestLine + "\r\n");
            writer.write(s.toString());
        } catch (IOException e) {
            logger.log(Level.WARN, "Error to write.", e);
        }
    }
}
