package com.cdkj.borrowingmenber.module.bankcert;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bumptech.glide.Glide;
import com.cdkj.baselibrary.activitys.WebViewActivity;
import com.cdkj.baselibrary.base.AbsBaseLoadActivity;
import com.cdkj.baselibrary.nets.RetrofitUtils;
import com.cdkj.baselibrary.utils.LogUtil;
import com.cdkj.baselibrary.utils.StringUtils;
import com.cdkj.borrowingmenber.R;
import com.cdkj.borrowingmenber.databinding.ActivityRhRegiBinding;
import com.cdkj.borrowingmenber.model.RhCardTypeModel;
import com.cdkj.borrowingmenber.module.api.MyApiServer;
import com.cdkj.borrowingmenber.weiget.bankcert.BaseRhCertCallBack;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * 人行注册页面
 * Created by cdkj on 2018/1/4.
 */

public class RhRegisterActivity extends AbsBaseLoadActivity {

    private List<RhCardTypeModel> rhCardTypeModels;

    private String mCardTpyeCode = "0";//选择的证件类型Code  默认为身份证

    public static void open(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, RhRegisterActivity.class);
        context.startActivity(intent);
    }

    private ActivityRhRegiBinding mBinding;

    private OptionsPickerView mCardTypePicker;//学历选择


    @Override
    public View addMainView() {
        mBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_rh_regi, null, false);
        return mBinding.getRoot();
    }

    @Override
    public void afterCreate(Bundle savedInstanceState) {

        mBaseBinding.titleView.setMidTitle("填写身份信息");

        initCardTypePicker();

        initListener();

//        getRetiCodeImg();

    }

    private void initListener() {

        //证件类型
        mBinding.linCardType.setOnClickListener(v -> {
            if (mCardTypePicker != null) mCardTypePicker.show();
        });
        //服务协议
        mBinding.tvRead1.setOnClickListener(v -> {
            WebViewActivity.openURL(this, "服务协议", "https://ipcrs.pbccrc.org.cn/html/servearticle.html");
        });
        //注册下一步
        mBinding.btnRegi1.setOnClickListener(v -> {
            regiNext();
        });
        //获取验证码
        mBinding.tvChangeCode.setOnClickListener(v -> getRetiCodeImg());
        mBinding.imgCode.setOnClickListener(v -> getRetiCodeImg());
    }

    /**
     * 注册
     */
    private void regiNext() {

//        if (TextUtils.isEmpty(mBinding.editRegiName.getText().toString())) {
//            showToast("请输入姓名");
//            return;
//        }
//        if (TextUtils.isEmpty(mBinding.editCardNum.getText().toString())) {
//            showToast("请输入证件号码");
//            return;
//        }
//        if (TextUtils.isEmpty(mBinding.editCode.getText().toString())) {
//            showToast("请输入验证码");
//            return;
//        }
//
//        if (!mBinding.checkboxRead.isChecked()) {
//            showToast("请阅读并同意服务协议");
//            return;
//        }
        rhRegiRequest();
    }

    /**
     * 初始化证件类型选择
     */
    private void initCardTypePicker() {
        mCardTypePicker = new OptionsPickerView.Builder(this, (options1, options2, options3, v) -> {

            RhCardTypeModel rhCardTypeModel = rhCardTypeModels.get(options1);

            if (rhCardTypeModel == null) return;
            mCardTpyeCode = rhCardTypeModel.getTypeCode();
            mBinding.tvCardType.setText(rhCardTypeModel.getTypeString());

        }).setContentTextSize(16).setLineSpacingMultiplier(4).build();

        mSubscription.add(Observable.just("")
                .observeOn(Schedulers.newThread())
                .map(s -> {
                    String[] types = getResources().getStringArray(R.array.id_card_type);
                    rhCardTypeModels = parseCardType(types);
                    return rhCardTypeModels;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rhCardTypeModels -> {
                    mCardTypePicker.setPicker(rhCardTypeModels);
                }, Throwable::printStackTrace));

    }

    /**
     * 解析证件类型
     *
     * @param types
     * @return
     */
    @NonNull
    private List<RhCardTypeModel> parseCardType(String[] types) {
        List<RhCardTypeModel> rhCardTypeModels = new ArrayList<>();

        for (String type : types) {
            if (TextUtils.isEmpty(type)) continue;

            RhCardTypeModel rhCardTypeModel = new RhCardTypeModel();

            rhCardTypeModel.setTypeCode(StringUtils.subStringEnd(type, type.length() - 1)); //截取code

            rhCardTypeModel.setTypeString(StringUtils.subString(type, 0, type.length() - 1));//截取类型名称

            LogUtil.E("证件类型" + rhCardTypeModel.getTypeString() + " __" + rhCardTypeModel.getTypeCode());

            rhCardTypeModels.add(rhCardTypeModel);
        }
        return rhCardTypeModels;
    }

    /**
     * 获取注册验证码图片
     */
    private void getRetiCodeImg() {
        Call<ResponseBody> call = RetrofitUtils.createApi(MyApiServer.class).rhRegiCode(new Date().getTime() + "");

        addCall(call);
        showLoadingDialog();
        call.enqueue(new BaseRhCertCallBack<ResponseBody>(this, BaseRhCertCallBack.RESPONSETYPE) {
            @Override
            protected void onSuccess(ResponseBody responseBody) {

                try {
                    Glide.with(RhRegisterActivity.this).load(responseBody.bytes()).error(com.cdkj.baselibrary.R.drawable.default_pic).into(mBinding.imgCode);
                } catch (Exception e) {
                    LogUtil.E("加载" + e);
                }

                LogUtil.E("请求验证码成功");
            }

            @Override
            protected void onFinish() {
                disMissLoading();
            }
        });
    }

    /**
     * 注册请求
     */
    private void rhRegiRequest() {

        Map<String, String> map = new HashMap<>();
        map.put("1", "on");
        map.put("method", "checkIdentity");
        map.put("org.apache.struts.taglib.html.TOKEN", "7dc93b932ad82457edaefb95da16732a");
        map.put("name", mBinding.editRegiName.getText().toString());
        map.put("certType", mCardTpyeCode);
        map.put("certNo", mBinding.editCardNum.getText().toString());
        map.put("_@IMGRC@_", mBinding.editCode.getText().toString());

        Call call = RetrofitUtils.createApi(MyApiServer.class).rhRegiRequest(map);

        addCall(call);

        showLoadingDialog();

        call.enqueue(new BaseRhCertCallBack(this, BaseRhCertCallBack.DOCTYPE) {

            @Override
            protected void onSuccess(Document doc) {
                Elements element = doc.getElementsByClass("erro_div1"); //获取登录错误提醒 如果有 说明登录没成功
                if (element != null && !TextUtils.isEmpty(element.text())) {
                    mBinding.errorInfo.setText(element.text());
                    showToast(element.text());
                    return;
                }
                Elements element2 = doc.getElementsByClass("span-grey2"); //获取登录错误提醒 如果有 说明登录没成功
                if (element2 != null && StringUtils.contains(element2.text(), "我已阅读并同意")) {
                    mBinding.errorInfo.setText("注册失败，请重试");
                    showToast("注册失败，请重试");
                    return;
                }

            }

            @Override
            protected void onFinish() {
                disMissLoading();
            }

        });

    }

}