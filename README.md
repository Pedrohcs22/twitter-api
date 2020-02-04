# twitter-api

A twitter clone written in clojure.

## Prerequisites
1. You will need [Clojure](https://clojure.org/guides/getting_started) installed.

2. You will need [Leiningen](https://leiningen.org/#install) 2.0.0 or above installed.
    - Create the lein.bat and put it in your path or in the project root.

3. You will need [mongoDb](https://docs.mongodb.com/manual/installation/) installed.
    - Once installed and running you will be able to use the API.
    - The postman collection to be executed on the next step (.4) will create some system entities.
    - After that, open the Mongo Shell and use the command 'use twitter_api' to connect with the dc created for the app. Once connected you will be able to use 'db.tweets.find()' or 'db.users.find()' to retrieve data.
    
4. You will need [Postman](https://www.getpostman.com/downloads/) installed.
    - You can find status request tests on the 'tests' tab, on each request.
    - The collection can be found at the resources folder, open the Collection on Postman and execute all of them sequentially in order to have the test entities created.
    - The backend must be running.

## API Documentation 
   https://app.swaggerhub.com/apis-docs/pedro79/twitter-api-clone/1
   
## Running

To start a web server for the application, run:

    lein ring server

