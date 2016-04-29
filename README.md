Stock Quotes Project
====================

The purpose of this project is to create a “stock quotes” module for
my Radio Pi project.


Running/Testing in Eclipse
--------------------------

I don't have a formal test structure set up here, in part because the way this works
is that it calls out to Yahoo to get the current stock price, and I don't have any
interest in mocking that up.

For me to run this in Eclipse, I go to Run Configurations and add this value to the
"Arguments" section:

    /Users/al/Projects/Scala/StockQuotes/stocks.conf

Then when I run the app in Eclipse, it uses that file as a command-line argument.
That file needs to contain config data that looks like this:

````
{
  "stocks": [
      { "stock": { "symbol": "AAPL",  "name": "Apple",    "notes": "buy at $93" }},
      { "stock": { "symbol": "CMG",   "name": "Chipotle", "notes": "buy soon ($420?)"}},
      { "stock": { "symbol": "VLKAY", "name": "VW",       "notes": "bought ~$32"}},
      { "stock": { "symbol": "WMT",   "name": "Walmart",  "notes": "buy at $6-65"}}
  ]
}
````

In you know Scala and JSON, and read the following section, hopefully everything else 
about this project will make sense.


Information
-----------

The way I use this project currently is to create a jar file with the `sbt package` command.
This creates a jar file named _stockquotes_2.10-1.0.jar_, under the _target_ directory.

On the Production RPI2 server I run the code with a shell script named run_stocks.sh that
has these contents:

	#!/bin/sh
	
	# production path (where the script and jar files are)
	cd /var/www/radio/scripts/stocks
	
	java -cp \
	  "htmlcleaner-2.2.jar:lift-json_2.10-2.5.1.jar:paranamer-2.1.jar:scala-library.jar:stockquotes_2.10-1.0.jar" \
	  aja.GetStockQuotes$delayedInit$body \
	  stocks.conf

That script is made executable, and run with a crontab entry like this:

	1,31 6-15 * * 1-5 /var/www/radio/scripts/stocks/run_stocks.sh > /var/www/radio/data/stocks.out 2>&1


Future: Making the requests run in parallel
-------------------------------------------

If it ever becomes a problem where I want/need to run the requests in parallel,
code like this should do the trick:

	import akka.actor.ActorSystem
	import scala.concurrent.{ Await, Future }
	import scala.concurrent.duration._
	import scala.concurrent.ExecutionContext.Implicits.global
	
	for(s <- stocks) {
	    val future = Future {
	        s.price = retrieveStockPrice(s.symbol)
	    }
	    Await.result(future, 5 seconds)
	}

