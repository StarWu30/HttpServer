import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;

// 接收请求类
public class Request implements Runnable{
    private static final Logger logger = LogManager.getLogger(Request.class.getCanonicalName());
    private File rootDirectory;
    private String indexFileName = "index.html";
    private Socket connection;
    private ResponseProcessor response;
    private Cache cache;

    public Request(File rootDirectory, String indexFileName, Socket connection, Cache cache){
        if(rootDirectory.isFile()){
            throw new IllegalArgumentException(
                    "rootDirectory must be a directory, not a file");
        }
        try{
            rootDirectory = rootDirectory.getCanonicalFile();
        }catch (IOException ex){
        }
        this.rootDirectory = rootDirectory;
        if(indexFileName != null)
            this.indexFileName = indexFileName;
        this.connection = connection;
        this.cache = cache;
    }

    @Override
    public void run() {
        String root = rootDirectory.getPath();
        try{
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            Writer writer = new OutputStreamWriter(out);
            InputStream in = connection.getInputStream();
            Reader reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream()),"US-ASCII");
            response = new ResponseProcessor(rootDirectory, in, out, writer, reader, cache);
            StringBuilder requestLine = new StringBuilder();
            // 读取请求头
            while (true){
                int c = in.read();
                if(c == '\r' || c == '\n')
                    break;
                requestLine.append((char)c);
            }

            String get = requestLine.toString();
            logger.info(connection.getRemoteSocketAddress() + " " + get);
            String[] tokens = get.split("\\s+");
//            \\s表示   空格,回车,换行等空白符,
//            +号表示一个或多个的意思
            String method = tokens[0];
            String version = "";
            String fileName = tokens[1];
            if(fileName.endsWith("/"))
                fileName += indexFileName;
//          获取内容种类
            String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
            File theFile = new File(rootDirectory, fileName.substring(1,fileName.length()));
//          分别处理请求方法
            if(method.equals("GET") ){
                if(tokens.length > 2){
                    version = tokens[2];
                }
                response.doGET(theFile, contentType, root, version);
            }else if( method.equals("HEAD")){   // HEAD 请求
                response.doHEAD("HTTP/1.0 200 OK", "txt/html", 0);
            }else if( method.equals("POST")) {  // POST 请求
                response.doPOST(contentType);
            }else if( method.equals("PUT")){    // PUT 请求
                response.doPUT();
            }else if( method.equals("DELETE")){ // DELETE 请求
                response.doDELETE(fileName);
            }else if( method.equals("OPTIONS")){   // OPTIONS 请求
                response.doOPTIONS();
            }else if( method.equals("TRACE")){      // TRACE 请求
                response.doTRACE(requestLine.toString());
            }else {

            }
        } catch (IOException ex) {
            logger.log(Level.WARN, "Error talking to " + connection.getRemoteSocketAddress(), ex);
        } finally {
            try{
                connection.close();
            } catch (IOException ex) {
                logger.log(Level.WARN, "Error close Socket:" + connection.getRemoteSocketAddress(), ex);
            }
        }
    }
}
