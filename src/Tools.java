/**
 * Created by Dylan于东海 on 2016/10/28.
 */
public class Tools {
    public static  void div(float[] v, int n) {
        for (int i = 0; i < v.length; i++) {
            v[i] /= n;
        }
    }
    public  static float[] sumVec (float[] a, float[] b) {
        int l = a.length < b.length ? a.length : b.length;
        float[] result = new float[l];
        for (int i=0;i<l;i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    public static float multiVec(float[] a, float[] b) {
        int l = a.length < b.length ? a.length : b.length;
        float result = 0;
        for (int i=0;i<l;i++) {
            result += a[i] * b[i];
        }
        return result;
    }
}
