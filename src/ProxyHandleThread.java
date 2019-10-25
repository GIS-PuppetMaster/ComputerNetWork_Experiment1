import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProxyHandleThread extends Thread {
    /**
     * 把从inputStream收到的消息转发到outputStream
     */
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] buffer = new byte[4096];
    public ProxyHandleThread(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        try {
            int len;
            while((len=this.inputStream.read(this.buffer))!=-1){
                if(len>0){
                    this.outputStream.write(this.buffer,0,len);
                    this.outputStream.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
