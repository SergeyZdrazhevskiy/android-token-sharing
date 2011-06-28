# Sharing sounds to SoundCloud via AccountManager

This is a small sample app to demonstrate how you could integrate SoundCloud in your
own Android application. The official SoundCloud app for Android app needs to
be installed, if it is missing the user will be prompted to install it.

## How sharing works

The integration uses the [Android account manager][] to obtain a token and then
makes requests using the standard SoundCloud [Java API wrapper][].

Here a very simplified usage pattern:

    AccountManager accountManager = AccountManager.get(this);
    Account[] acc = accountManager.getAccountsByType("com.soundcloud.android.account");
    if (acc.length > 0) {
      // possibly ask user for permission to obtain token
      String access = accountManager.blockingGetAuthToken(acc[0], "access_token", true);
      Token token = new Token(access, null, Token.SCOPE_NON_EXPIRING);
      ApiWrapper wrapper = new ApiWrapper(null, null, null, token, Env.LIVE);

      HttpResponse resp = wrapper.get(Request.to("/me"));
      // do something with resp
    } else {
      // logic to add an account
    }

See the sample app for the detailed usage.

The big advantage of this approach is that you have full control over how you
exactly use the API, however the drawback of this approach is that your app requires
a few more permissions (`android.permission.{GET_ACCOUNTS, MANAGE_ACCOUNTS, USE_CREDENTIALS}`).

If you just want to upload a file to SoundCloud consider the [Android intent
sharing][] method instead.

## Token invalidation

Once you have obtained a token from the account manager you should probably
persist it in your app and only request a new token if necessary.


[Android account manager]: http://developer.android.com/reference/android/accounts/AccountManager.html
[Java API wrapper]: https://github.com/soundcloud/java-api-wrapper
[Android intent sharing]: https://github.com/soundcloud/android-intent-sharing
