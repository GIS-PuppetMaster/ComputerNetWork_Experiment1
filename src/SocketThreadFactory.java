import java.net.Socket;
import java.util.concurrent.ThreadFactory;

public class SocketThreadFactory implements ThreadFactory {
    private Socket socket;
    public SocketThreadFactory(Socket socket) {
        this.socket = socket;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new SocketThread(this.socket);
    }
}
