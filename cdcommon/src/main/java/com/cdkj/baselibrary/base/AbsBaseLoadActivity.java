package com.cdkj.baselibrary.base;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.cdkj.baselibrary.R;
import com.cdkj.baselibrary.databinding.ActivityAbsBaseLoadBinding;


/**
 * 带空页面，错误页面显示的BaseActivity 通过AbsBaseActivityj界面操作封装成View而来
 */
public abstract class AbsBaseLoadActivity extends BaseActivity {
    protected ActivityAbsBaseLoadBinding mBaseBinding;

    /**
     * 布局文件xml的resId,无需添加标题栏、加载、错误及空页面
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseBinding = DataBindingUtil.setContentView(this, R.layout.activity_abs_base_load);

        mBaseBinding.titleView.setVisibility(canLoadTopTitleView() ? View.VISIBLE : View.GONE);
        mBaseBinding.viewV.setVisibility(canLoadTopTitleView() ? View.VISIBLE : View.GONE);

        mBaseBinding.contentView.addComtentView(addMainView());

        mBaseBinding.titleView.setLeftFraClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canFinish()) {
                    topTitleViewleftClick();
                }
                finish();
            }
        });
        mBaseBinding.titleView.setRightFraClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topTitleViewRightClick();
            }
        });
        setTitleWhileBg();
        afterCreate(savedInstanceState);
    }

    /**
     * 能否结束当前页面
     *
     * @return
     */
    protected boolean canFinish() {
        return true;
    }

    /**
     * 能否加载标题
     *
     * @return
     */
    protected boolean canLoadTopTitleView() {
        return true;
    }

    /**
     * 添加要显示的View
     */
    public abstract View addMainView();

    /**
     * activity的初始化工作
     */
    public abstract void afterCreate(Bundle savedInstanceState);

    public void topTitleViewleftClick() {

    }

    public void topTitleViewRightClick() {

    }

    /**
     * 是否显示title
     *
     * @param isShow
     */
    protected void setShowTitle(boolean isShow) {
        mBaseBinding.titleView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mBaseBinding.viewV.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置title背景信息
     */
    protected void setTitleWhileBg() {
        mBaseBinding.titleView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        mBaseBinding.titleView.setLeftTitleColor(R.color.text_black_cd);
        mBaseBinding.titleView.setRightTitleColor(R.color.text_black_cd);
        mBaseBinding.titleView.setMidTitleColor(R.color.text_black_cd);
        mBaseBinding.titleView.setLeftImg(R.drawable.back_black);
//        if (canLoadTopTitleView()) {
//            UIStatusBarHelper.setStatusBarLightMode(this); // 沉浸式状态栏
//        }
    }


}
