package com.longquan.ui.fragment;
import android.content.Context;
import android.os.Bundle;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.longquan.R;
import com.longquan.ui.view.EditInputView;
import com.longquan.utils.SizeUtils;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * author : charile yuan
 * date   : 21-3-4
 * desc   :
 */
public class WifiConnectPasswdDialogFrag extends DialogFragment {

    private Context mContext;
    private EditInputView mDialogView;
    private final int PASSWORD_MIN_LENGTH = 8;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_wifi_bt_edit_input, null);
        initPasswordInputView(view);
        Window dialogWindow = getDialog().getWindow();
//        WindowManager.LayoutParams attr = dialogWindow.getAttributes();
//        dialogWindow.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//        attr.y = SizeUtils.dip2px(mContext, mContext.getResources().getDimension(R.dimen.dp_50));
//        dialogWindow.setAttributes(attr);
        getDialog().setCanceledOnTouchOutside(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EditText editText = mDialogView.findViewById(R.id.edit_content);
        editText.postDelayed(() -> {
            editText.requestFocus();
            InputMethodManager manager =
                    ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
            if (manager != null) manager.showSoftInput(editText, 0);
        }, 200);
    }

    private void initPasswordInputView(View parent) {
        mDialogView = (EditInputView) parent.findViewById(R.id.edit_input);
        mDialogView.setShowCheckBox(true);
        mDialogView.setConfirmText(R.string.wifi_connect);
        mDialogView.setConfirmEnableMinLength(PASSWORD_MIN_LENGTH);
        mDialogView.setInputType(EditInputView.INPUT_TYPE_TEXT_PASSWORD);
        mDialogView.setIsShowCheckBox(true);
        mDialogView.setOnCheckedChangedListener(new EditInputView.OnCheckedChangedListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //如果选中，显示密码
                    mDialogView.getEditTextView().setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    //否则隐藏密码
                    mDialogView.getEditTextView().setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        mDialogView.setOnInputClickListener(new EditInputView.OnInputClickListener() {
            @Override
            public void onCancel(EditText editText) {
//                EBus.p(new CancelEvent(true));
                dismiss();
            }

            @Override
            public void onConfirm(EditText editText) {
                String passWord = mDialogView.getEditText();
//                EBus.p(new EditPwdTextEvent(passWord));
                dismiss();
            }
        });
        mDialogView.setSupportChinese(false);

    }



    /**
     * 显示键盘
     *
     * @param et 输入焦点
     */
    public void showInput(final EditText et) {
//        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        View v = getActivity().getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

}
