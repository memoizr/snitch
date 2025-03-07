package snitch.kofix;

public class FunkyVisualJava<V, I> {

    private final V mVideo;
    private final I mImage;
    private final String mS;
    private final I mI;

    public V getVideo() {
        return mVideo;
    }

    public I getImage() {
        return mImage;
    }

    public String getS() {
        return mS;
    }

    public I getI() {
        return mI;
    }

    public V getV() {
        return mV;
    }

    private final V mV;

    public FunkyVisualJava(I image, V video, String s, I i, V v) {
        mVideo = video;
        mImage = image;
        mS = s;
        mI = i;
        mV = v;
    }

    @Override
    public String toString() {
        return "FunkyVisualJava{" +
                "mVideo=" + mVideo +
                ", mImage=" + mImage +
                ", mS='" + mS + '\'' +
                ", mI=" + mI +
                ", mV=" + mV +
                '}';
    }
}
