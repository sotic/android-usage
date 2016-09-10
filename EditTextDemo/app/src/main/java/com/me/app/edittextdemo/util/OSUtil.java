package com.me.app.edittextdemo.util;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * 判断系统是不是miui，flyme，emui,
 */
public class OSUtil {
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_FLYME_VERSION_NAME = "ro.build.display.id";
    private static final String KEY_COLOROS_VERSION_NAME= "ro.build.version.opporom";
    private static final String KEY_FUNTOUCHOS_VERSION_NAME = "ro.vivo.os.version";
    private static final String KEY_EUI_VERSION_NAME = "ro.letv.eui";


    private static OSUtil instance = null;

    private OSUtil() throws IOException {
        prop = BuildProperties.newInstance();
    }
    private BuildProperties prop ;
    public static OSUtil getInstance() throws IOException {
        if (instance == null) {
            synchronized (OSUtil.class) {
                if (instance == null){
                    instance = new OSUtil();
                }
            }
        }
        return instance;
    }

    private boolean isPropertiesExist(String... keys) {
            for (String key : keys) {
                String str = prop.getProperty(key);
                if (str == null)
                    return false;
            }
            return true;
    }

    private String getProperty(String key) {
            return prop.getProperty(key);
    }

    /**
     * 华为
     * @return
     */
    public boolean isEMUI() {
        return isPropertiesExist(KEY_EMUI_VERSION_CODE);
    }

    /**
     * 小米
     * @return
     */
    public boolean isMIUI() {
        return isPropertiesExist(KEY_MIUI_VERSION_CODE, KEY_MIUI_VERSION_NAME);
    }

    /**
     * 魅族
     * @return
     */
    public boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * oppo
     * @return
     */
    public boolean isColorOS() {
        return isPropertiesExist(KEY_COLOROS_VERSION_NAME);
    }


    /**
     * vivo
     * @return
     */
    public boolean isFuntouchOS(){
        return isPropertiesExist(KEY_FUNTOUCHOS_VERSION_NAME);
    }


    /**
     * 乐视
     * @return
     */
    public boolean isEUI(){
        return isPropertiesExist(KEY_EUI_VERSION_NAME);
    }

    public String getUIVersion(){
        if (isMIUI()){
            return "MIUI "+getProperty(KEY_MIUI_VERSION_NAME);
        }
        else if (isEMUI()){
            return "EMUI "+getProperty(KEY_EMUI_VERSION_CODE);
        }
        else if (isFlyme()){
            return "Flyme "+getProperty(KEY_FLYME_VERSION_NAME);
        }
        else if (isColorOS()){
            return "ColorOS "+getProperty(KEY_COLOROS_VERSION_NAME);
        }
        else if (isFuntouchOS()){
            return "FuntouchOS "+getProperty(KEY_FUNTOUCHOS_VERSION_NAME);
        }
        else if (isEUI()){
            return "EUI "+getProperty(KEY_EUI_VERSION_NAME);
        }
        return null;
    }

    private static class BuildProperties {

        private final Properties properties;

        private BuildProperties() throws IOException {
            properties = new Properties();
            FileInputStream file = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            properties.load(file);
            file.close();
        }

        public String getProperty(final String name) {
            return properties.getProperty(name);
        }

        public static BuildProperties newInstance() throws IOException {
            return new BuildProperties();
        }
    }
}
