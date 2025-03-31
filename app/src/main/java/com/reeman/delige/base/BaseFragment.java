package com.reeman.delige.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.reeman.delige.event.RobotEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

public abstract class BaseFragment extends Fragment {

    protected ViewGroup root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.w(this + " onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Timber.w(this + " onCreateView");
        root = (ViewGroup) LayoutInflater.from(getContext()).inflate(getLayoutRes(), container, false);
        return root;
    }

    protected abstract @LayoutRes
    int getLayoutRes();


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Timber.w(this + " onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.w(this + " onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        Timber.w(this + " onResume");
    }


    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        Timber.w(this + " onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.w(this + " onStop");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.w(this + " onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.w(this + " onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.w(this + " onDetach");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void defaultEvent(RobotEvent.OnDefaultEvent event){

    }
}
