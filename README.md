# sendgrid-android

This Android module allows you to quickly and easily send emails through SendGrid in Android. It is basically a modified pull from an obsolete sendgrid android library(https://github.com/danysantiago/sendgrid-android), slightly modified to use 21st century networking libraries.

## Requirements


The minimum supported SDK version is 14

## Adding it to your project


**Step 1.** Add it in your root build.gradle at the end of repositories:

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

**Step 2.** Add the dependency

    dependencies {
	     implementation 'com.github.oluwatayo:sendgrid-android:1.0.0'
	}

**Step 3.** Add the required permission

Add the `INTERNET` permissions to your android manifest

     <uses-permission android:name="android.permission.INTERNET" />
     
**Step 4.** Additional step
###### Because the Library uses an updated version of [Okhttp](https://square.github.io/okhttp/) we need to add the following. This goes into your app's build.gradle.
```groovy
...
android {
	...
    compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
}
```
     
**Step 5.** Usage
```java
SendGrid sendGrid = new SendGrid("Add your api key"); //Replace with your sendgrid api key
        sendGrid.setSendgridResponseCallbacks(new SendGridResponseCallback() {
            @Override
            public void onMailSendSuccess(SendGrid.Response response) {
                Toast.makeText(MainActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LOG", "" + response.getCode());
                textView.setText(String.valueOf(response.getMessage()));
            }

            @Override
            public void onMailSendFailed(SendGrid.Response response) {
                Toast.makeText(MainActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LOG", "" + response.getCode());
                textView.setText(String.valueOf(response.getCode()));
            }
        });
        SendGrid.Email email = new SendGrid.Email().addBcc("olu@olu.com")
                .addTo("test@gmail.com", "test test")
                .addCc("ceec@gmail.com")
                .setSubject("Test test")
                .setReplyTo("no-reply@gmail.com", "no-reply")
                .setFrom("ade@tmail.com", "tayo")
                .setHtml("<strong>Hi</strong>")
                .setText("Text");

        try {
            sendGrid.send(email);
        } catch (SendGridException e) {
            Toast.makeText(this, "ex: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.d("LOG", e.getLocalizedMessage());
        }

```