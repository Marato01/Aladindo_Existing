package com.reeman.delige.contract;

import android.app.Activity;

import com.reeman.delige.presenter.IPresenter;
import com.reeman.delige.view.IView;

public interface SplashContract {
    interface Presenter extends IPresenter{

        void startup(Activity activity);
    }

    interface View extends IView{

    }
}
