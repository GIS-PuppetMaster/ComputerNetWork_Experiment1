import java.util.HashSet;
import java.util.Iterator;

public class Config {
    final static int INPUT_BUFFER_SIZE = 4096;
    final static int CORE_THREAD_POOL_SIZE = 10;
    static HashSet<String> user_list= new HashSet<>() {{
        add("127.0.0.1");
    }};
    static MyHashSet white_list = new MyHashSet(){{add("jwts.hit.edu.cn");}};
    static MyHashSet black_list = new MyHashSet(){{add("jwes.hit.edu.cn");}};
    final static String[] mode_list = new String[]{"","WHITE_LIST","BLACK_LIST","HIJACK"};
    static String MODE=mode_list[1];



}
class MyHashSet extends HashSet<String>{
    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    public boolean halfContain(String target){
        Iterator iterator = this.iterator();
        while(iterator.hasNext()){
            Object temp = iterator.next();
            String white = (String) temp;
            if(white.contains(target)){
                return true;
            }
        }
        return false;
    }
}

