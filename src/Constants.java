/**
 * Created by Dylan于东海 on 2016/10/26.
 */
public class Constants {
    public  static final String USAGE = "WORD VECTOR estimation toolkit in java v 0.1c\n\n"+
            "Options:\n"+
            "Parameters for training:\n"+
            "\t-train <file>\n"+
            "\t\tUse text data from <file> to train the model\n"+
            "\t-output <file>\n"+
            "\t\tUse <file> to save the resulting word vectors / word clusters\n"+
            "\t-size <int>\n"+
            "\t\tSet size of word vectors; default is 100\n"+
            "\t-window <int>\n"+
            "\t\tSet max skip length between words; default is 5\n"+
            "\t-sample <float>\n"+
            "\t\tSet threshold for occurrence of words. Those that appear with higher frequency in the training data\n"+
            "\t\twill be randomly down-sampled; default is 1e-3, useful range is (0, 1e-5)\n"+
            "\t-hs <int>\n"+
            "\t\tUse Hierarchical Softmax; default is 0 (not used)\n"+
            "\t-negative <int>\n"+
            "\t\tNumber of negative examples; default is 5, common values are 3 - 10 (0 = not used)\n"+
            "\t-threads <int>\n"+
            "\t\tUse <int> threads (default 12)\n"+
            "\t-iter <int>\n"+
            "\t\tRun more training iterations (default 5)\n"+
            "\t-min-count <int>\n"+
            "\t\tThis will discard words that appear less than <int> times; default is 5\n"+
            "\t-alpha <float>\n"+
            "\t\tSet the starting learning rate; default is 0.025 for skip-gram and 0.05 for CBOW\n"+
            "\t-classes <int>\n"+
            "\t\tOutput word classes rather than word vectors; default number of classes is 0 (vectors are written)\n"+
            "\t-debug <int>\n"+
            "\t\tSet the debug mode (default = 2 = more info during training)\n"+
            "\t-binary <int>\n"+
            "\t\tSave the resulting vectors in binary moded; default is 0 (off)\n"+
            "\t-save-vocab <file>\n"+
            "\t\tThe vocabulary will be saved to <file>\n"+
            "\t-read-vocab <file>\n"+
            "\t\tThe vocabulary will be read from <file>, not constructed from the training data\n"+
            "\t-cbow <int>\n"+
            "\t\tUse the continuous bag of words model; default is 1 (use 0 for skip-gram model)\n"+
            "\nExamples:\n"+
            "./word2vec -train data.txt -output vec.txt -size 200 -window 5 -sample 1e-4 -negative 5 -hs 0 -binary 0 -cbow 1 -iter 3\n\n";
    public static void  main(String[] args) {
        System.out.println(USAGE);

    }
}
