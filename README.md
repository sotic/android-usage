# android-usage
## EditText相关
### 禁止对EditText进行粘贴操作：
创建自定义View并继承EditText，其中构造函数需要注意：
```java
public EditTextDisablePaste(Context context) {
    this(context, null);
}

public EditTextDisablePaste(Context context, AttributeSet attrs) {
    //第三个默认参数务必设为android.R.attr.editTextStyle，否则无法响应焦点！
    this(context, attrs, android.R.attr.editTextStyle);
}

public EditTextDisablePaste(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
}
```
重写onTextContextMenuItem()方法
```java
@Override
public boolean onTextContextMenuItem(int id) {
    switch (id){
        case android.R.id.paste:
            ToastUtil.showInCenter("不允许粘贴");
            return false;
			case android.R.id.copy:
                Toast.makeText(getContext(), "Copy!", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.cut:
                Toast.makeText(getContext(), "Cut!", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.selectAll:
                Toast.makeText(getContext(), "Select all!", Toast.LENGTH_SHORT).show();
                break;
    }
    return super.onTextContextMenuItem(id);
}
```
特殊情况处理：个别机型(如oppo等)不会调用onTextContextMenuItem方法，使得粘贴操作无法检测。此时做如下处理：
- 判断是否为需要特殊处理的操作系统
- 给EditText添加一个TextWatcher
- 在onTextChanged方法中，判断用户输入的文字与剪切板最新的数据是否一致，如是则可能为粘贴操作
- 在afterTextChanged方法中，删除判断为粘贴的文字
```java
public class EditTextDisablePaste extends EditText {

    private boolean shouldCurrentOSCheckPasteLeak = true;
    private boolean hasPasteLeaked;//是否存在可能的粘贴检测遗漏，即在个别机型不会调用onTextContextMenuItem方法，使得粘贴操作无法检测
    private boolean isHandlingPasteLeak;
    private int pasteLeakStart, pasteLeakEnd;
    private static final int CHECK_PASTE_CHAR_NUM_THRESHOLD = 10;//设定一个阈值，当用户输入的文字达到阈值时，判断与剪贴板中最新数据是否一致

    public EditTextDisablePaste(Context context) {
        this(context, null);
    }

    public EditTextDisablePaste(Context context, AttributeSet attrs) {
        //第三个默认参数务必设为android.R.attr.editTextStyle，否则无法响应焦点！
        this(context, attrs, android.R.attr.editTextStyle);

    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public EditTextDisablePaste(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        checkCurrentOS();
        initTextChangedListener();
    }

    private void checkCurrentOS() {
        try {
            OSUtil osUtil = OSUtil.getInstance();
            shouldCurrentOSCheckPasteLeak = osUtil.isColorOS() && osUtil.getUIVersion() != null && osUtil.getUIVersion().contains("V2.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTextChangedListener() {
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(shouldCurrentOSCheckPasteLeak && count >= CHECK_PASTE_CHAR_NUM_THRESHOLD && !isHandlingPasteLeak) {
                    ClipData clipData = ((ClipboardManager)getContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip();
                    //如果用户最后输入的文本与剪切板最新的数据一致，则判断可能为粘贴操作
                    if(clipData != null && clipData.getItemAt(0) != null
                            && !TextUtils.isEmpty(clipData.getItemAt(0).getText())
                            && clipData.getItemAt(0).getText().equals(s.subSequence(start, start+count))) {
                        hasPasteLeaked = true;
                        pasteLeakStart = start;
                        pasteLeakEnd = start + count;
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(hasPasteLeaked) {
                    hasPasteLeaked = false;
                    isHandlingPasteLeak = true;
                    s.delete(pasteLeakStart, pasteLeakEnd);//本操作会调用onTextChanged方法，注意避免死循环(使用isHandlingPasteLeak字段)
                    isHandlingPasteLeak = false;

                    Toast.makeText(getContext(), "您可能在使用粘贴功能", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
```
其中判断手机UI系统的主要代码如下，这里以oppo手机为例：
```java
package com.me.app.edittextdemo.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class OSUtil {
    private static final String KEY_COLOROS_VERSION_NAME= "ro.build.version.opporom";

    private BuildProperties prop ;

    private static OSUtil instance = null;

    private OSUtil() throws IOException {
        prop = BuildProperties.newInstance();
    }
    
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
     * oppo
     * @return
     */
    public boolean isColorOS() {
        return isPropertiesExist(KEY_COLOROS_VERSION_NAME);
    }


    public String getUIVersion(){
        if (isColorOS()){
            return "ColorOS "+getProperty(KEY_COLOROS_VERSION_NAME);
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
```
