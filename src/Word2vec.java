import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Dylan于东海 on 2016/10/26.
 * java 实现google开源工具word2vec, 中文注释。
 */
public class Word2vec {

    static final int MAX_VOCAB = 21000000; //字典最大值

    static int layer1_size = 10;        //向量维度
    static int debug_mode;              //是否为debug模式
    static int binary;                  //是否为二进制存储
    static int cbow;                    //是否cbow模型
    static int window = 5;              //词袋大小
    static int sample;                  //
    static int hs;                      //是否层次softmax
    static int negative;                //负采样个数
    static int num_threads = 4;         //线程数目
    static int iter;                    //迭代次数
    static int min_count = 1;           //最小词频阈值，低于此阈值的词删除
    static int classes;                 //是否聚类

    static float alpha;                  //学习速率

    static  String train_file;           //训练数据来源文件路径
    static  String save_vocab_file;      //保存字典路径
    static  String read_vocab_file;      //读取字典路径
    static  String output_file;          //训练结果保存文件路径

    //词节点结构
    static class Vocab_word {
        long cn;                //词频
        List<Integer> point;    //从根节点到此词的路径
        String word;            //词文本
        List<Integer> code;     //本词节点的哈夫曼编码
        short codelen;          //本词节点的哈夫曼编码长度
        int father;             //此结点在哈夫曼树中的父节点
        int Ncode;              //此节点编码

        public Vocab_word(String w, long c) {
            word = w;
            cn = c;
        }

        @Override
        public String toString(){
            return word+", cn:"+cn+",father:"+father;
        }
    }

    static Map<String, Long> vocab = new HashMap<String, Long>();           //统计字典，词频
    static List<Vocab_word> leavesNodes = new ArrayList<Vocab_word>();      //叶节点
    static List<Vocab_word> fatherNodes = new ArrayList<Vocab_word>();      //非叶节点
    static long wordNum = 0;                                                //文件中词的数量
    static Map<String, Integer> vocabIndex = new HashMap();

    static List<float[]> wordVec;
    static List<float[]> fatherVec;

    static class trainThread implements Runnable {
        private int id = 0;
        private int sentSize = 1000;
        private long fileLength = 0;
        public long currentNum = 0;

        public trainThread(int i, long length, int ss) {
            id = i;
            fileLength = length;
            sentSize = ss;
        }
        public void  run() {
            try {
                InputStream is = new FileInputStream(train_file);
                is.skip(fileLength/num_threads * id);
                Scanner sc = new Scanner(is);
                sc.useDelimiter("[\\s\\t\\n]");

                List<String> sentence = new ArrayList<String>();

                while (sc.hasNext() && currentNum <= wordNum/num_threads) {
                    String word = sc.next();
                    if (word.length() > 0) {
                        //System.out.println("Thread " + id +" ," + word);
                        if (sentence.size() < sentSize) {
                            sentence.add(word);
                        }else {
                            for (int i=0;i<sentence.size();i++) {
                                float[] X = new float[layer1_size];           //隐含层向量
                                int sumNum = 0;
                                for (int j = i - window; j < i+window; j++) { //累加
                                    if (j >= 0 && j < sentence.size() && j != i){
                                        X = Tools.sumVec(X,wordVec.get(vocabIndex.get(sentence.get(j))));
                                        sumNum++;
                                    }
                                }
                                Tools.div(X,sumNum);
                            }
                            sentence.clear();
                        }
                        currentNum++;
                    }

                }
                //System.out.println("Thread " + id + " finished");
            }catch (IOException e) {
                System.out.println("Can not find train file: " + train_file);
                System.exit(1);
            }


        }
    }

    static void trainModel(){
        System.out.println("...开始训练..." + new Date());
        ExecutorService es = Executors.newFixedThreadPool(num_threads);
        File tF = new File(train_file);
        long fileLength = tF.length();
        for (int i=0;i<num_threads;i++) {
            es.execute(new trainThread(i,fileLength,1000));
        }
        es.shutdown();
        while (true) {
            if (es.isTerminated()) {
                System.out.println("...训练结束..." + new Date());
                break;
            }
            try {
                Thread.sleep(2000);
            }catch (Exception e) {
                System.out.println("sleep interupted");
            }

        }

    }
    static void initNet() {
        wordVec = new ArrayList<float[]>();
        Random random = new Random();
        for (int i=0;i<leavesNodes.size();i++) {
            float[] temp = new float[layer1_size];
            for (int j = 0; j < temp.length; j++) {
                temp[j] = (random.nextFloat()-0.5f)/layer1_size;
            }
            wordVec.add(temp);
        }
        fatherVec = new ArrayList<float[]>();
        for (int i=0;i<fatherNodes.size();i++) {
            float[] temp = new float[layer1_size];
            fatherVec.add(temp);
        }
        createHuffTree();
    }
    //当字典太大时移除低频词，每执行一次，低频词阈值增加1
    static void reduceVocab(){
        for (String word : vocab.keySet()) {
            long cn = vocab.get(word);
            if (cn <= min_count){
                vocab.remove(word);
            }
        }
        min_count++;
    }

    //统计字典和词频
    static void createVocab(String train_file) {
        Scanner sc = new Scanner(train_file);
        try {
            sc = new Scanner(new FileInputStream(train_file));
            sc.useDelimiter("[\\s\\t\\n]");                    //正则表达式设定Scanner的分隔符
        } catch (IOException e) {
            System.out.println("can't find file " + train_file);
            System.exit(1);
        }
        while (sc.hasNext()) {
            String word = sc.next();
            if (word.length() > 0) {                            //多个分隔符相邻时会出现length为0
                //System.out.println(word);
                if (vocab.containsKey(word)){
                    long cn = vocab.get(word);
                    vocab.put(word,++cn);
                }else {
                    vocab.put(word,1l);
                }
                wordNum++;
                if (vocab.size() >= MAX_VOCAB) {
                    reduceVocab();
                }
            }
        }
        System.out.println("...字典构建完成..." + new Date());
    }

    //向哈夫曼树的非叶子结点列表添加非叶子结点
    static void addNOde(List<Vocab_word> fatherNodes, Vocab_word a, Vocab_word b){
        //System.out.println(a.word+","+a.cn+" + "+b.word+","+b.cn);
        Vocab_word newFather = new Vocab_word("",a.cn + b.cn);
        a.father = fatherNodes.size();
        a.Ncode = 0;
        b.father = fatherNodes.size();
        b.Ncode = 1;
        newFather.father = -1;
        fatherNodes.add(newFather);
        //System.out.println("father "+(fatherNodes.size()-1)+", "+newFather.cn);
    }

    //根据词频构建哈夫曼树，为每个叶子结点找到其哈夫曼编码及到根节点的路径
    static void createHuffTree() {
        for (String word : vocab.keySet()) {
            leavesNodes.add(new Vocab_word(word, vocab.get(word)));
        }
        Collections.sort(leavesNodes, new Comparator<Vocab_word>() {
            @Override
            public int compare(Vocab_word a, Vocab_word b) {
                return a.cn < b.cn ? 1 : -1;
            }
        });



        Vocab_word fN;
        Vocab_word lN;
        addNOde(fatherNodes,leavesNodes.get(leavesNodes.size()-1),leavesNodes.get(leavesNodes.size()-2));

        Vocab_word buff = null;  //存储上一个最小的值，为null时证明刚合成过，需要对buff重新赋值
        Vocab_word minValueWord; //当前最小值
        int j = 0;
        int i = leavesNodes.size()-3;
        int leastNodes = leavesNodes.size()-1;

        while (leastNodes > 1) { //不断向fatherNOdes中添加合成的结点，每合成添加一个，剩余的结点个数就减1,当只剩一个根节点时停止
            fN = fatherNodes.get(j);
            if (i < 0) {        //叶节点都已经计算过时
                addNOde(fatherNodes,fN,fatherNodes.get(++j));
                leastNodes--;
                j++;
            }else {
                lN = leavesNodes.get(i);
                if (fN.cn < lN.cn) {
                    minValueWord = fN;
                    j++;
                }else {
                    minValueWord = lN;
                    i--;
                }

                if (buff == null) {
                    buff = minValueWord;
                }else {
                    addNOde(fatherNodes,buff,minValueWord);
                    leastNodes--;
                    buff = null;
                }
            }
        }

        vocab.clear();
        //计算每个叶节点的路径和编码
        for (int k = 0;k<leavesNodes.size();k++) {
            //System.out.println(leavesNodes.get(i).toString());
            Vocab_word word = leavesNodes.get(k);
            List<Integer> path = new ArrayList<Integer>();
            List<Integer> code = new ArrayList<Integer>();
            while(word.father >= 0) { //根节点的父节点坐标为-1
                //System.out.println("father:"+word.father);
                path.add(0, word.father);
                code.add(0,word.Ncode);
                word = fatherNodes.get(word.father);
            }
            word = leavesNodes.get(k);
            word.point = path;
            word.code = code;
            word.codelen = (short)code.size();
            vocabIndex.put(word.word,k);
            //System.out.println(path.toString());
            //System.out.println(code.toString());
        }

    }
    static int  ArgPos(String str, int argc, String[] args) {
        for (int a = 0; a < argc; a++) {
            if (str != null && str.equals(args[a])) {
                if (a == argc - 1) {
                    System.out.println("Argument missing for " + str);
                    System.exit(1);
                }
                return a;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int argc = args.length;
        if (argc == 1) {
            System.out.println(Constants.USAGE);
            return;
        }

        int i;
        if ((i = ArgPos("-size", argc, args)) >= 0) layer1_size = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-train", argc, args)) >= 0) train_file = args[i + 1];
        if ((i = ArgPos("-save-vocab", argc, args)) >= 0) save_vocab_file = args[i + 1];
        if ((i = ArgPos("-read-vocab", argc, args)) >= 0) read_vocab_file = args[i + 1];
        if ((i = ArgPos("-debug", argc, args)) >= 0) debug_mode = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-binary", argc, args)) >= 0) binary = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-cbow", argc, args)) >= 0) cbow = Integer.parseInt(args[i + 1]);
        if (cbow > 0) alpha = 0.05f;
        if ((i = ArgPos("-alpha", argc, args)) >= 0) alpha = Float.parseFloat(args[i + 1]);
        if ((i = ArgPos("-output", argc, args)) >= 0) output_file = args[i + 1];
        if ((i = ArgPos("-window", argc, args)) >= 0) window = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-sample", argc, args)) >= 0) sample = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-hs", argc, args)) >= 0) hs = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-negative", argc, args)) >= 0) negative = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-threads", argc, args)) >= 0) num_threads = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-iter", argc, args)) >= 0) iter = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-min-count", argc, args)) >= 0) min_count = Integer.parseInt(args[i + 1]);
        if ((i = ArgPos("-classes", argc, args)) >= 0) classes = Integer.parseInt(args[i + 1]);

        //System.out.println(train_file);
        createVocab(train_file);
        initNet();
        trainModel();
    }
}
