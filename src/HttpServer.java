import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final Logger logger = LogManager.getLogger(HttpServer.class.getCanonicalName());
    private static final int NUM_THREADS = 200;
    private static final String INDEX_FILE = "index.html";
    public final static String algorithm = "SSL";
    public Cache cache;

    private final File rootDiretory;
    private final int port;

    public HttpServer(File rootDiretory, int port) throws IOException{
        if(!rootDiretory.isDirectory()){
            throw new IOException(rootDiretory + " does not exist as a directory");
        }
        this.rootDiretory = rootDiretory;
        this.port = port;
        cache = new Cache(rootDiretory);
    }
    public void start() throws IOException{

        //创建安全Socket
       try {
            SSLContext context = SSLContext.getInstance(algorithm);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//           Oracle的默认密钥库类型
            KeyStore ks = KeyStore.getInstance("JKS");
//          口令短语以char[]数组存储，可以很快地从内存中擦除，而不是等待垃圾回收
            char[] password = "123456".toCharArray();
            ks.load(new FileInputStream("test.keystore"), password);
            kmf.init(ks, password);
            context.init(kmf.getKeyManagers(), null, null);
//          擦除口令
            Arrays.fill(password, '0');
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);

//          增加匿名（未认证）密码组
            String[] supported = serverSocket.getSupportedCipherSuites();
            String[] anonCipherSuitesSupported = new String[supported.length];
            int numAnonCipherSuitesSupported = 0;
            for (int i = 0; i < supported.length; i++) {
                if (supported[i].indexOf("_anon_") > 0) {
                    anonCipherSuitesSupported[numAnonCipherSuitesSupported++] = supported[i];
                }
            }
            String[] oldEnabled = serverSocket.getEnabledCipherSuites();
            String[] newEnabled = new String[oldEnabled.length + numAnonCipherSuitesSupported];
            System.arraycopy(oldEnabled, 0, newEnabled, 0, oldEnabled.length);
            System.arraycopy(anonCipherSuitesSupported, 0, newEnabled, oldEnabled.length, numAnonCipherSuitesSupported);
            // 创建线程池
            ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Runnable r = new Request(rootDiretory, INDEX_FILE, clientSocket, cache);
                    pool.submit(r);
                } catch (IOException ex) {
                    logger.info(ex);
                }
            }

        }catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException
                    | CertificateException | KeyManagementException e) {
                logger.info(e);
        }
    }

    public static void main(String[] args) {

        File docroot;
        String fn = "D:\\IdeaProjects\\MyHttpServer";
        try{
            docroot = new File(fn);
        }catch (ArrayIndexOutOfBoundsException ex){
            System.out.println("Usage: java HTTPServer docroot port");
            logger.log(Level.ERROR, "Error to read rootdirectory.", ex);
            return;
        }

        int port = 8080;
        try{
            HttpServer webserver = new HttpServer(docroot, port);
            webserver.start();
        } catch (IOException ex) {
            logger.info(ex);
        }
    }
}
