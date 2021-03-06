package com.longquan.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.longquan.R;


/**
 * author : charile yuan
 * date   : 21-3-4
 * desc   :
 */
public class EditInputView extends LinearLayout implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

    /**
     * 默认输入类型
     * 0  -- text
     * 1  -- textPassword
     */
    private int mInputType;
    public static final int INPUT_TYPE_TEXT = 0;
    public static final int INPUT_TYPE_TEXT_PASSWORD = 1;
    private String mTextCancel;
    private String mTextConfirm;

    /**
     * 是否显示CheckBox
     */
    private boolean mIsShowCheckBox;
    private Context mContext;
    private EditText mEditTextContent;
    private ImageView mIvClear;
    private CheckBox mCbShowPassword;
    private Button mBtCancel;
    private Button mBtConfirm;
    private OnInputClickListener mOnInputClickListener;
    private OnInputTextChangeListener mOnInputTextChangeListener;
    private OnCheckedChangedListener mOnCheckedChangedListener;
    private int mConfirmEnableMinLength = 1;
    private InputMethodManager imm;
    private boolean isSupportChinese = true;

    public EditInputView(Context context) {
        this(context, null);
    }

    public EditInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.EditInputView,
                0, 0);

        mInputType = typedArray.getInt(R.styleable.EditInputView_inputType, 0);
        mTextCancel = typedArray.getString(R.styleable.EditInputView_text_cancel);
        mTextConfirm = typedArray.getString(R.styleable.EditInputView_text_confirm);
        mIsShowCheckBox = typedArray.getBoolean(R.styleable.EditInputView_isShowCheckBox, true);
        typedArray.recycle();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.input_edit, null);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(view, params);

        mEditTextContent = (EditText) view.findViewById(R.id.edit_content);
        setCustomActionModeCallbackForEditText();

        if (imm == null) {
            imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        mEditTextContent.requestFocus();
        setInputType(mInputType);
        setSelectionToEnd();
        imm.showSoftInput(mEditTextContent, 0);

        mEditTextContent.addTextChangedListener(this);

        mIvClear = (ImageView) view.findViewById(R.id.edit_clear);
        mIvClear.setOnClickListener(this);

        mCbShowPassword = (CheckBox) view.findViewById(R.id.cb_show_passwd);
        mCbShowPassword.setVisibility(mIsShowCheckBox ? VISIBLE : INVISIBLE);
        mCbShowPassword.setOnCheckedChangeListener(this);

        mBtCancel = (Button) view.findViewById(R.id.bt_cancel);
        if (!TextUtils.isEmpty(mTextCancel)) {
            mBtCancel.setText(mTextCancel);
        }
        mBtCancel.setOnClickListener(this);
        mBtConfirm = (Button) view.findViewById(R.id.bt_ok);
        if (!TextUtils.isEmpty(mTextConfirm)) {
            mBtConfirm.setText(mTextConfirm);
        }
        mBtConfirm.setOnClickListener(this);
        handleConfirmState();
    }


    @SuppressLint("NewApi")
    private void setCustomActionModeCallbackForEditText() {
        mEditTextContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        mEditTextContent.setCustomInsertionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_clear:
                mEditTextContent.setText("");
                break;
            case R.id.bt_cancel:
                if (mOnInputClickListener != null) {
                    mOnInputClickListener.onCancel(mEditTextContent);
                }
                break;
            case R.id.bt_ok:
                if (mOnInputClickListener != null) {
                    mOnInputClickListener.onConfirm(mEditTextContent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mEditTextContent.setCursorVisible(false);
        if (buttonView.getId() == R.id.cb_show_passwd) {
            if (mOnCheckedChangedListener != null) {
                mOnCheckedChangedListener.onCheckedChanged(buttonView, isChecked);
            } else {
                if (isChecked) {
                    mEditTextContent.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mEditTextContent.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        }
        setSelectionToEnd();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mOnInputTextChangeListener != null) {
            mOnInputTextChangeListener.beforeTextChanged(s, start, count, after);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mOnInputTextChangeListener != null) {
            mOnInputTextChangeListener.onTextChanged(s, start, before, count);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!isSupportChinese){
            if (s.length() > 0) {
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c >= 0x4e00 && c <= 0X9fff) {
                        s.delete(i,i+1);
                    }
                }
            }
        }
        // 限制输入不能以空格开头
        if (s.toString().startsWith(" ")) {
            mEditTextContent.setText(s.toString().trim());
        }
        handleConfirmState();
        if (mOnInputTextChangeListener != null && s != null) {
            mOnInputTextChangeListener.afterTextChanged(s);
        }



    }

    private void handleConfirmState() {
        String edInput = getEditText();
        // 禁用Confirm Button，一般是执行了Clear或者输入为空时调用
        if (edInput.getBytes().length == 0 || edInput.getBytes().length < mConfirmEnableMinLength) {
            mBtConfirm.setEnabled(false);
            mBtConfirm.setTextColor(mContext.getResources().getColor(R.color.edit_bt_ok_disabled_text_color));
        }
        // 启用Confirm Button
        if (!mBtConfirm.isEnabled() && edInput.getBytes().length >= mConfirmEnableMinLength) {
            mBtConfirm.setEnabled(true);
            mBtConfirm.setTextColor(mContext.getResources().getColor(R.color.edit_bt_ok_text_color));
        }
    }

    public EditText getEditTextView() {
        return mEditTextContent;
    }

    public void setIsShowCheckBox(boolean isShowCheckBox) {
        this.mIsShowCheckBox = isShowCheckBox;
    }

    public void setSupportChinese(boolean supportChinese) {
        isSupportChinese = supportChinese;
    }

    public void setOnCheckedChangedListener(OnCheckedChangedListener onCheckedChangedListener) {
        this.mOnCheckedChangedListener = onCheckedChangedListener;
    }

    public void setOnInputClickListener(OnInputClickListener onInputClickListener) {
        this.mOnInputClickListener = onInputClickListener;
    }

    public void setOnInputTextChangeListener(OnInputTextChangeListener onInputTextChangeListener) {
        this.mOnInputTextChangeListener = onInputTextChangeListener;
    }

    public void setEditTextContent(String text) {
        mEditTextContent.setText(text);
    }

    public void setEditTextContent(int resId) {
        mEditTextContent.setText(resId);
    }

    public String getEditText() {
        return mEditTextContent.getText().toString();
    }

    public void setEditTextHint(String hint) {
        mEditTextContent.setHint(hint);
    }

    public void setEditTextHint(int resId) {
        mEditTextContent.setHint(resId);
    }

    public void setShowCheckBox(boolean isShowCheckBox) {
        mCbShowPassword.setVisibility(isShowCheckBox ? VISIBLE : INVISIBLE);
    }

    public void setShowPasswordChecked(boolean isChecked) {
        mCbShowPassword.setChecked(isChecked);
    }

    public boolean getShowPasswordChecked() {
        return mCbShowPassword.isChecked();
    }

    public void setConfirmText(String text) {
        mBtConfirm.setText(text);
    }

    public void setConfirmText(int resId) {
        mBtConfirm.setText(resId);
    }

    public void setConfirmEnableMinLength(int minLength) {
        mConfirmEnableMinLength = minLength;
    }

    public void setInputType(int inputType) {
        if (inputType == INPUT_TYPE_TEXT) {
            mEditTextContent.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else if (inputType == INPUT_TYPE_TEXT_PASSWORD) {
            mEditTextContent.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }


    /**
     * 光标移动到尾部
     */
    private void setSelectionToEnd() {
        mEditTextContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                mEditTextContent.setSelection(mEditTextContent.getText().length());
                mEditTextContent.setCursorVisible(true);
            }
        }, 200);
    }

    public void setEditFilters(InputFilter[] inputFilters) {
        mEditTextContent.setFilters(inputFilters);
    }

    public interface OnCheckedChangedListener {
        void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
    }

    public interface OnInputClickListener {
        void onCancel(EditText editText);

        void onConfirm(EditText editText);
    }

    public interface OnInputTextChangeListener {
        void beforeTextChanged(CharSequence s, int start, int count, int after);

        void onTextChanged(CharSequence s, int start, int before, int count);

        void afterTextChanged(Editable s);
    }

}
