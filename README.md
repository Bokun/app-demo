# app-demo

This is a demo app for the Bokun app store. 
It's purpose is to show you how to implement the OAuth process.

## 1. Setting up a tunnel to localhost
You need to set up tunnel using a tool like `ngrok` (https://ngrok.com/) to localhost so that the app store can access your app via a public URL.
Assuming you will be running this app on `localhost` port `8181`, you create a tunnel using:

`ngrok http 8181`

This will create a public URL for your app, which will tunnel to port 8181 on your localhost. 
Let's assume the URL looks like this:

`https://{id}.ngrok.io`

## 2. Configure and run the demo app
Open `conf/application.conf` and configure the following parameters: 

1. Bokun URL. This should point to the URL of the Bokun environment you are running.
If you are using Bokun test environment, then set this to `"https://%s.bokuntest.com"`

    * `bokun.url = "https://%s.bokun.io"`  

2. App URL. This should point to the URL of where your app is running

    * `app.url = "https://{id}.ngrok.io"`

3. App credentials. They can be found on the bottom of your app dashboard in the Bokun Partners site.

    * `app.apiKey = bb5d27dda5a24c4eaf8263ac5a5054f8`
    * `app.secretKey = 834404ae8e22453e967adcc6d6f95d93`

4. App permissions. The access scope that your app will request when a vendor installs.

    * `app.permissions = "VENDOR_USERS_READ,BOOKINGS_READ,CHECKOUTS_READ,CHECKOUTS_WRITE"`

Once you have configured these settings and saved `conf/application.conf`, start the app by running the following command in the app-demo folder:

`sbt run` 

## 3. Configuring the app URL in the Partner Dashboard
The final step is to tell Bókun the URL where your app is running. 
In the Bokun Partner Dashboard, in your app settings, you should set the following URLs:

**App URL** 

`https://{id}.ngrok.io/`

**Whitelisted redirect URL**

`https://{id}.ngrok.io`

Now you're good to go - have fun!