package com.reeman.delige.constants;

import android.content.res.Resources;

import com.reeman.delige.R;
import com.tencent.bugly.beta.Beta;

public class BuglyConstants {

    public static void updateBuglyStrings(Resources resources){
        Beta.strToastYourAreTheLatestVersion = resources.getString(R.string.strToastYourAreTheLatestVersion);
        Beta.strToastCheckUpgradeError = resources.getString(R.string.strToastCheckUpgradeError);
        Beta.strToastCheckingUpgrade = resources.getString(R.string.strToastCheckingUpgrade);
        Beta.strNotificationDownloading = resources.getString(R.string.strNotificationDownloading);
        Beta.strNotificationClickToView = resources.getString(R.string.strNotificationClickToView);
        Beta.strNotificationClickToInstall = resources.getString(R.string.strNotificationClickToInstall);
        Beta.strNotificationClickToContinue = resources.getString(R.string.strNotificationClickToContinue);
        Beta.strNotificationClickToRetry = resources.getString(R.string.strNotificationClickToRetry);
        Beta.strNotificationDownloadSucc = resources.getString(R.string.strNotificationDownloadSucc);
        Beta.strNotificationDownloadError = resources.getString(R.string.strNotificationDownloadError);
        Beta.strNotificationHaveNewVersion = resources.getString(R.string.strNotificationHaveNewVersion);
        Beta.strNetworkTipsMessage = resources.getString(R.string.strNetworkTipsMessage);
        Beta.strNetworkTipsTitle = resources.getString(R.string.strNetworkTipsTitle);
        Beta.strNetworkTipsConfirmBtn = resources.getString(R.string.strNetworkTipsConfirmBtn);
        Beta.strNetworkTipsCancelBtn = resources.getString(R.string.strNetworkTipsCancelBtn);
        Beta.strUpgradeDialogVersionLabel = resources.getString(R.string.strUpgradeDialogVersionLabel);
        Beta.strUpgradeDialogFileSizeLabel = resources.getString(R.string.strUpgradeDialogFileSizeLabel);
        Beta.strUpgradeDialogUpdateTimeLabel = resources.getString(R.string.strUpgradeDialogUpdateTimeLabel);
        Beta.strUpgradeDialogFeatureLabel = resources.getString(R.string.strUpgradeDialogFeatureLabel);
        Beta.strUpgradeDialogUpgradeBtn = resources.getString(R.string.strUpgradeDialogUpgradeBtn);
        Beta.strUpgradeDialogInstallBtn = resources.getString(R.string.strUpgradeDialogInstallBtn);
        Beta.strUpgradeDialogRetryBtn = resources.getString(R.string.strUpgradeDialogRetryBtn);
        Beta.strUpgradeDialogContinueBtn = resources.getString(R.string.strUpgradeDialogContinueBtn);
        Beta.strUpgradeDialogCancelBtn = resources.getString(R.string.strUpgradeDialogCancelBtn);
    }
}
