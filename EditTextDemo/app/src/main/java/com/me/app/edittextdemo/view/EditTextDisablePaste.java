package com.me.app.edittextdemo.view;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import com.me.app.edittextdemo.util.OSUtil;

import java.io.IOException;

public class EditTextDisablePaste extends EditText {

    private boolean shouldCurrentOSCheckPasteLeak = true;
    private boolean hasPasteLeaked;//是否存在可能的粘贴检测遗漏，即在个别机型不会调用onTextContextMenuItem方法，使得粘贴操作无法检测
    private boolean isHandlingPasteLeak;
    private int pasteLeakStart, pasteLeakEnd;
    private static final int CHECK_PASTE_CHAR_NUM_THRESHOLD = 10;

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
                    //如果用户最后输入的文本与剪切板最新的数据一致，判断可能为粘贴操作
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

    @Override
    public boolean onTextContextMenuItem(int id) {
        // React:
        switch (id){
            case android.R.id.paste:
                Toast.makeText(getContext(), "不允许使用粘贴功能", Toast.LENGTH_SHORT).show();
                return false;
            case android.R.id.copy:
                Toast.makeText(getContext(), "Copy!", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.cut:
                Toast.makeText(getContext(), "Copy!", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onTextContextMenuItem(id);
    }

}
