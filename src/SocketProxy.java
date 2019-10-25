import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class SocketProxy {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        ExecutorService threadPool = Executors.newFixedThreadPool(Config.CORE_THREAD_POOL_SIZE);
        while(true){
            Socket socket;
            try{
                socket = serverSocket.accept();
                threadPool.execute(new SocketThread(socket));
                //new SocketThread(socket).run();
            }
            catch (Exception e){

            }
        }

    }
}
