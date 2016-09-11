# android-usage-tips
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
    // Do your thing:
    //boolean consumed = super.onTextContextMenuItem(id); // Change1
    // React:
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
