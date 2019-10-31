import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class SocketProxy {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        ExecutorService threadPool = Executors.newFixedThreadPool(Config.CORE_THREAD_POOL_SIZE);
        while(true){
            Socket socket;
            try{
                socket = serverSocket.accept();
                threadPool.execute(new SocketThread(socket));
            }
            catch (Exception e){
                System.out.println("监听端口："+port+"抛出异常");
            }
        }

    }
}
