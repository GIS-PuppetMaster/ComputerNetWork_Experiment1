import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketThread extends Thread {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] inputBuffer = new byte[Config.INPUT_BUFFER_SIZE];

    public SocketThread(Socket socket) {
        this.socket = socket;

    }

    @Override
    public void run() {
        try {
            if (!Config.user_list.contains(this.socket.getInetAddress().toString().substring(1))){
                System.out.println(this.socket.getInetAddress().toString().substring(1));
                socket.close();
                return;
            }
            inputStream = this.socket.getInputStream();
            outputStream = this.socket.getOutputStream();
            this.socket.setSoTimeout(50000);
            inputStream.read(inputBuffer);
            String inputString = new String(inputBuffer);
            String host;
            int port;
            if (inputString.length() < 8 ) {
                socket.close();
                return;
            }
            //http
            System.out.println(inputString);
            //获取http请求头
            //获取url
            String patternString = "[A-Za-z]+ ([A-Za-z0-9_.~!*'();:@&=+$,\\[\\]/?#\\-]+) [A-Za-z0-9_.~!*'();:@&=+$,\\[\\]/?#\\-]+";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(inputString);
            String url;
            int url_index;
            if (matcher.find()) {
                url = matcher.group(1);
            } else {
                throw new Exception("未发现http请求头url，报文内容：\n" + inputString);
            }
            //获取目标服务器host和port
            patternString = "Host: ([A-Za-z0-9_.~!*'();:@&=+$,\\[\\]/?#\\-]+)";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(inputString);
            if (matcher.find()) {
                host = matcher.group(1);
            } else {
                throw new Exception("未发现http请求头host，报文内容：\n" + inputString);
            }
            port = 80;
            if (host.contains(":")) {
                port = Integer.parseInt(host.substring(host.indexOf(':') + 1));
            }
            //连接到目标服务器
            //如果是白名单模式，检查Host是否在白名单内
            if("WHITE_LIST".equals(Config.MODE)){
                if(!Config.white_list.halfContain(host)){
                    System.out.println("Host:"+host+" 不在白名单内，拒绝访问");
                    socket.close();
                    return;
                }
            }
            if("BLACK_LIST".equals(Config.MODE)){
                if(Config.black_list.halfContain(host)){
                    System.out.println("Host:"+host+" 在黑名单内，拒绝访问");
                    socket.close();
                    return;
                }
            }
            if("HIJACK".equals(Config.MODE)){
                host="qhgd2002.china6688.com";
            }
            System.out.println("URL:" + url + "\n" + "Host:" + host + "\nPort:" + port);
            Socket socketOut = new Socket(host, port);
            socketOut.setSoTimeout(50000);
            OutputStream proxyOutput = socketOut.getOutputStream();
            //http直接转发请求头
            if("HIJACK".equals(Config.MODE)){
                String temp = new String(inputBuffer);
                temp = temp.replace(url,"http://qhgd2002.china6688.com/");
                proxyOutput.write(temp.getBytes());
                proxyOutput.flush();
            }
            else {
                proxyOutput.write(inputBuffer);
                proxyOutput.flush();
            }
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
