import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketThread extends Thread {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] inputBuffer = new byte[4096];

    public SocketThread(Socket socket) {
        this.socket = socket;

    }

    @Override
    public void run() {
        try {
            inputStream = this.socket.getInputStream();
            outputStream = this.socket.getOutputStream();
            this.socket.setSoTimeout(50000);
            inputStream.read(inputBuffer);
            String inputString = new String(inputBuffer);
            if (inputString.length() < 8 || "CONNECT".equals(inputString.substring(0, 7))) {
                return;
            }
            System.out.println(inputString);
            //获取http请求头
            //获取url
            String patternString = "[A-Za-z]+ ([A-Za-z0-9_.~!*'();:@&=+$,\\[\\]/?#\\-]+) [A-Za-z0-9_.~!*'();:@&=+$,\\[\\]/?#\\-]+";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(inputString);
            String url;
            if (matcher.find()) {
                url = matcher.group(1);
            } else {
                throw new Exception("未发现http请求头url，报文内容：\n" + inputString);
            }
            //获取目标服务器host和port
            patternString = "Host: ([A-Za-z0-9_.~!*'();:@&=+$,\\[\\]/?#\\-]+)";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(inputString);
            String host;
            if (matcher.find()) {
                host = matcher.group(1);
            } else {
                throw new Exception("未发现http请求头host，报文内容：\n" + inputString);
            }
            int port = 80;
            if (host.contains(":")){
                port = Integer.parseInt(host.substring(host.indexOf(':')+1));
            }
            System.out.println("Host:" + host + "\nPort:" + port);
            //连接到目标服务器
            Socket socketOut = new Socket(host, port);
            socketOut.setSoTimeout(50000);
            OutputStream proxyOutput = socketOut.getOutputStream();
            //http直接转发请求头
            proxyOutput.write(inputBuffer);
            proxyOutput.flush();
            InputStream proxyInput = socketOut.getInputStream();
            ProxyHandleThread handle1 = new ProxyHandleThread(inputStream, proxyOutput);
            //转发目标服务器响应至客户端
            ProxyHandleThread handle2 = new ProxyHandleThread(proxyInput,outputStream);
            handle1.start();
            handle2.start();
            handle1.join();
            handle2.join();
            socketOut.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
