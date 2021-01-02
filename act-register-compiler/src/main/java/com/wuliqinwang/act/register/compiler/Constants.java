package com.wuliqinwang.act.register.compiler;

/**
 * @author wuli秦王
 */
interface Constants {
    String ACTIVITY ="android.app.Activity";
    String FRAGMENT = "android.app.Fragment";
    String FRAGMENT_V4 = "android.support.v4.app.Fragment";
    String SERVICE = "android.app.Service";
    String PARCELABLE = "android.os.Parcelable";

    String LANG = "java.lang";
    String BYTE = LANG + ".Byte";
    String SHORT = LANG + ".Short";
    String INTEGER = LANG + ".Integer";
    String LONG = LANG + ".Long";
    String FLOAT = LANG + ".Float";
    String DOUBEL = LANG + ".Double";
    String BOOLEAN = LANG + ".Boolean";
    String CHAR = LANG + ".Character";
    String STRING = LANG + ".String";
    String SERIALIZABLE = "java.io.Serializable";

    String ACT_REGISTER_PACKAGE_NAME = "com.wuliqinwang.act.register";
    String ANNOTATION_TYPE_AC_REGISTER = ACT_REGISTER_PACKAGE_NAME + ".annotation.ActRegister";
}
