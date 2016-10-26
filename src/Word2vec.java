import java.util.List;

/**
 * Created by Dylan于东海 on 2016/10/26.
 * java 实现google开源工具word2vec, 中文注释。
 */
public class Word2vec {

    static int layer1_size = 10;        //向量维度
    static int debug_mode;              //是否为debug模式
    static int binary;                  //是否为二进制存储
    static int cbow;                    //是否cbow模型
    static int window;                  //词袋大小
    static int sample;                  //
    static int hs;                      //是否层次softmax
    static int negative;                //
    static int num_threads;             //线程数目
    static int iter;                    //迭代次数
    static int min_count;               //
    static int classes;                 //

    static float alpha;                  //学习速率

    static  String train_file;           //训练数据来源文件路径
    static  String save_vocab_file;      //保存字典路径
    static  String read_vocab_file;      //读取字典路径
    static  String output_file;          //训练结果保存文件路径

    //词结点结构
    static class vocab_word {
        long cn;                //词频
        List<Integer> point;    //从根节点到此词的路径
        String word;            //词文本
        String code;            //本词结点的哈夫曼编码
        short codelen;          //本词结点的哈夫曼编码长度
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
    }
}
