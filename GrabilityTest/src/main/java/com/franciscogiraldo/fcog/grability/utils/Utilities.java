package com.franciscogiraldo.fcog.grability.utils;

import android.content.pm.ActivityInfo;
import android.os.Build;

import com.franciscogiraldo.fcog.grability.R;

public class Utilities {


    /**
     * Determina si la aplicación corre en versiones superiores o iguales
     * a Android LOLLIPOP
     *
     * @return booleano de confirmación
     */
    public static boolean materialDesign() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}