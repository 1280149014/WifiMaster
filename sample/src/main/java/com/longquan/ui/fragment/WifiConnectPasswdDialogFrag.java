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
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.longquan.R;
import com.longquan.ui.view.EditInputView;
import com.longquan.utils.SizeUtils;

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
        WindowManager.LayoutParams attr = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        attr.y = SizeUtils.dip2px(mContext, mContext.getResources().getDimension(R.dimen.dp_50));
        dialogWindow.setAttributes(attr);
        getDialog().setCanceledOnTouchOutside(false);
        return view;
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

}
