# app-demo

This is a demo app for the Bokun app store. 
It is just intended to show you how to implement the OAuth process.

## Configuring URLs for the App
In the Bokun Partner Dashboard, when you create the app, you should set the following URLs:

**Installation URL** 
http://localhost:8181/install

**Whitelisted redirect URL**
http://localhost:8181/install/confirmed

## Configuring the client_id and client_secret
The client_id and client_secret are hardcoded in controllers.HomeController.
You need to change these values. Go to your Bókun Partners Dashboard, click "Apps" and then open your app.
Scroll down to "App Credentials".

* `CLIENT_ID`  Set this to the "API key"
* `CLIENT_SECRET`  Set this to the "Secret key"

Now you're good to go!