package cn.leixiaoyue.lhlearningdemo.graffiti;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by Lei Xiaoyue on 2015-12-15.
 */
public class GraffitiPath {
    public Path path;
    public Paint paint;
    public GraffitiPath(Path path, Paint paint){
        this.path = new Path(path);
        this.paint = new Paint(paint);
    }
}
