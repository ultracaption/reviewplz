package com.polysiya.reviewplz;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ReviewPlz {

  private static final String TAG = "ReviewPlz";

  private static final String PREF_NAME = "com.polysiya.reviewplz.pref";

  private static final String LAUNCHED_BEFORE = "LAUNCHED_BEFORE";
  private static final String START_TIME = "START_TIME";
  private static final String RATE_CLICKED = "RATE_CLICKED";
  private static final String LATER_CLICKED = "LATER_CLICKED";
  private static final String REJECT_CLICKED = "REJECT_CLICKED";

  private static final String LAUNCH_COUNT = "LAUNCH_COUNT";
  private static final String SIGNIFICANT_ACTION_COUNT = "SIGNIFICANT_ACTION_COUNT";

  private static final int ONE_DAY = 24 * 60 * 60 * 1000;


  public static void reportLaunch(Context context) {
    SharedPreferences pref = getPref(context);
    markLaunched(pref);

    if (isNotAvailable(pref))
      return;

    if (meetTheRequiredTimeCondition(context, pref)) {
      // Check other conditions
      increaseLaunchCount(pref);
    } else {
      // Need more time to show dialog
      boolean needMoreTimeToCount = Boolean.valueOf(context.getString(R.string.count_after_required_days_passed));
      if (needMoreTimeToCount) {
        // Do nothing
        return;
      } else {
        // Just increase count
        increaseLaunchCount(pref);
      }
    }
  }

  public static void reportSignificantAction(Context context) {
    SharedPreferences pref = getPref(context);
    if (isNotAvailable(pref))
      return;

    if (meetTheRequiredTimeCondition(context, pref)) {
      // Check other conditions
      increaseSignificantActionCount(pref);
    } else {
      // Need more time to show dialog
      boolean needMoreTimeToCount = Boolean.valueOf(context.getString(R.string.count_after_required_days_passed));
      if (needMoreTimeToCount) {
        // Do nothing
        return;
      } else {
        // Just increase count
        increaseSignificantActionCount(pref);
      }
    }
  }

  public static void clearHistory(Context context) {
    getPref(context).edit().clear().commit();
  }


  public static void showPushDialog(FragmentActivity activity) {
    showPushDialog(activity, new ReviewPlzDialog(), "reviewplz");
  }
  public static void showPushDialog(FragmentActivity activity, ReviewPlzDialog dialog, String tag) {
    SharedPreferences pref = getPref(activity);
    if (Boolean.valueOf(activity.getString(R.string.debug))) {
      showDialog(activity, dialog, tag);
      return;
    }

    if (isNotAvailable(pref))
      return;

    if (meetTheRequiredTimeCondition(activity, pref)
        && meetTheLaunchCountCondition(activity, pref)
        && meetTheActionCondition(activity, pref)) {
      // Show dialog
      showDialog(activity, dialog, tag);
    }
  }
  private static void showDialog(FragmentActivity activity, ReviewPlzDialog dialog, String tag) {
    if (dialog == null)
      dialog = new ReviewPlzDialog();
    if (tag == null)
      tag = "reviewplz";

    dialog.show(activity.getSupportFragmentManager(), "reviewplz");
  }

  public static void doRate(Context context) {
    try {
      Uri uri = Uri.parse(context.getString(R.string.app_market_url));
      context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    } catch (Exception e) {
      Log.e(TAG, "Invalid url", e);
    }

    SharedPreferences pref = getPref(context);
    pref.edit().putBoolean(RATE_CLICKED, true).commit();
  }
  public static void doLater(Context context) {
    SharedPreferences pref = getPref(context);
    pref.edit().putBoolean(LATER_CLICKED, true).putLong(START_TIME, System.currentTimeMillis()).commit();
  }
  public static void doReject(Context context) {
    SharedPreferences pref = getPref(context);
    pref.edit().putBoolean(REJECT_CLICKED, true).commit();
  }

  private static boolean isNotAvailable(SharedPreferences pref) {
    return pref.getBoolean(RATE_CLICKED, false) || pref.getBoolean(REJECT_CLICKED, false);
  }

  private static boolean meetTheRequiredTimeCondition(Context context, SharedPreferences pref) {
    long currentTime = System.currentTimeMillis();

    long startTime = pref.getLong(START_TIME, currentTime);
    boolean laterClicked = pref.getBoolean(LATER_CLICKED, false);

    int requiredDays;
    if (laterClicked) {
      requiredDays = Integer.parseInt(context.getString(R.string.required_days_after_later_clicked));
    } else {
      requiredDays = Integer.parseInt(context.getString(R.string.required_days_after_launch));
    }

    long requiredTime = startTime + requiredDays * ONE_DAY;
    return currentTime >= requiredTime;
  }
  private static boolean meetTheLaunchCountCondition(Context context, SharedPreferences pref) {
    int count = pref.getInt(LAUNCH_COUNT, 0);
    int requiredCount = Integer.parseInt(context.getString(R.string.required_launch_count));

    return count >= requiredCount;
  }
  private static boolean meetTheActionCondition(Context context, SharedPreferences pref) {
    int count = pref.getInt(SIGNIFICANT_ACTION_COUNT, 0);
    int requiredCount = Integer.parseInt(context.getString(R.string.required_significant_action_count));

    return count >= requiredCount;
  }

  private static void markLaunched(SharedPreferences pref) {
    boolean launchedBefore = pref.getBoolean(LAUNCHED_BEFORE, false);
    if (!launchedBefore) {
      pref.edit().putBoolean(LAUNCHED_BEFORE, true).putLong(START_TIME, System.currentTimeMillis()).commit();
    }
  }
  private static void increaseLaunchCount(SharedPreferences pref) {
    int count = pref.getInt(LAUNCH_COUNT, 0);
    pref.edit().putInt(LAUNCH_COUNT, count + 1).commit();
  }
  private static void increaseSignificantActionCount(SharedPreferences pref) {
    int count = pref.getInt(SIGNIFICANT_ACTION_COUNT, 0);
    pref.edit().putInt(SIGNIFICANT_ACTION_COUNT, count + 1).commit();
  }

  private static SharedPreferences getPref(Context context) {
    SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    debug(pref);
    return pref;
  }

  private static void debug(SharedPreferences pref) {
    StringBuilder builder = new StringBuilder();
    builder.append("=======================================" + "\n");
    builder.append("launched before: " + pref.getBoolean(LAUNCHED_BEFORE, false) + "\n");
    builder.append("start time: " + pref.getLong(START_TIME, 0) + "\n");
    builder.append("rate clicked: " + pref.getBoolean(RATE_CLICKED, false) + "\n");
    builder.append("later clicked: " + pref.getBoolean(LATER_CLICKED, false) + "\n");
    builder.append("reject clicked: " + pref.getBoolean(REJECT_CLICKED, false) + "\n");
    builder.append("launch count: " + pref.getInt(LAUNCH_COUNT, 0) + "\n");
    builder.append("significant action count: " + pref.getInt(SIGNIFICANT_ACTION_COUNT, 0) + "\n");
    builder.append("=======================================");

    Log.d(TAG, builder.toString());
  }

  public static class ReviewPlzDialog extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Dialog dialog = super.onCreateDialog(savedInstanceState);
      dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
      dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
      return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      String appName = getString(R.string.review_app_name);
      String title = String.format(getString(R.string.review_title), appName);
      String description = String.format(getString(R.string.review_description), appName);
      String ok = String.format(getString(R.string.review), appName);

      View root = inflater.inflate(R.layout.dialog_reviewplz, null);
      ((TextView) root.findViewById(R.id.title)).setText(title);
      ((TextView) root.findViewById(R.id.description)).setText(description);
      ((Button) root.findViewById(R.id.ok_button)).setText(ok);


      root.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          okClicked();
        }
      });
      root.findViewById(R.id.later_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          laterClicked();
        }
      });
      root.findViewById(R.id.reject_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          rejectClicked();
        }
      });
      return root;
    }

    protected void okClicked() {
      doRate(getActivity());
      dismiss();
    }
    protected void laterClicked() {
      doLater(getActivity());
      dismiss();
    }
    protected void rejectClicked() {
      doReject(getActivity());
      dismiss();
    }
  }
}
