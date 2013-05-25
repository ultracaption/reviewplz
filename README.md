# reviewplz

A library for developers to push users to rate or write a review about their android application.

This library is currently being used in [MARTINI](http://martiniplz.com/).

## Getting Started

1. Register this project as a library project
2. Copy res/values/reviewplz_settings.xml to your project and customize it.

## How to use
#### Call reportLaunch method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ..
        ReviewPlz.reportLaunch(this);
        ..
    }

#### Call reportSignificantAction method

    void significantAction() {
      ..
      ReviewPlz.reportSignificantAction(this);
      ..
    }

#### Call showPushDialog method

    void whereYouWantToShowDialog() {
      ReviewPlz.showPushDialog(this);
    }
    
#### Customize push dialog

    void whereYouWantToShowDialog() {
      ReviewPlz.showPushDialog(this, new ReviewPushDialog(), "review");
    }
    ..
    public class ReviewPushDialog extends ReviewPlzDialog {
      ..
      public void onReviewButtonClick(View view) {
        okClicked();
      }
      public void onLaterButtonClick(View view) {
        laterClicked();
      }
      public void onRejectButtonClick(View view) {
        rejectClicked();
      }
      ..
    }
    
    
