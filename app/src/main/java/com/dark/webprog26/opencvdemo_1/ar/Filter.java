package com.dark.webprog26.opencvdemo_1.ar;

import org.opencv.core.Mat;

/**
 * Created by webpr on 21.02.2017.
 */

public interface Filter {
    public abstract void apply(final Mat src, final Mat dst);
}
