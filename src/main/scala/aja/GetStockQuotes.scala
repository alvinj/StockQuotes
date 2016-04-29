package aja

import java.io._
import scala.io.Source
import com.devdaily.stocks.StockUtils
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import scala.collection.mutable.ArrayBuffer

/**
 * The goals here are (a) to get a config filename from STDIN, (b) read the config
 * file, which is assumed to have JSON data formatted like TestData.scala, 
 * (c) retrieve the stock quotes, and (d) print those results to STDOUT in a TBD format.
 */
object GetStockQuotes extends App {
    
    // get the config filename from the command-line args
    if (args.length != 1) printUsageAndExit
    val configFilename = args(0)

    // setup
    implicit val formats = DefaultFormats // for json handling
    case class Stock(symbol: String, name: String, notes: String)
    
    // read the config file (/Users/al/Projects/Scala/StockQuotes/stocks.conf)
    val jsonConfigData = getFileContentsAsString(configFilename)
    val listOfStocks: Array[Stock]= getStocks(jsonConfigData)
    
    // get the data/results from yahoo
    val results = for (stock <- listOfStocks) yield {
        val html = StockUtils.getHtmlFromUrl(stock.symbol)
        html match {
            case Some(contents) => {
                val price = StockUtils.extractPriceFromHtml(contents, stock.symbol)
                (stock, price)
            }
            case None => (stock, "Error")
        }
    }

    // i think my RadioPi display is 40 chars wide
    // print the results to stdout
    for ((stock, price) <- results) {
        // "1000.50".length = 7, "75.00".length=5
        val symbolPaddingLength = 7 - stock.symbol.length  // will be displayed left-justified within 7 chars
        val pricePaddingLength = 9 - price.length          // will be displayed right-justified within 9 chars
        val notesPaddingLength = 19 - stock.notes.length   // total field width is 24 chars (4 spaces before, '.' after)
        val symbolPadded = stock.symbol + " " * symbolPaddingLength
        val pricePrePadded = " " * pricePaddingLength + price
        val notesPostPadded = stock.notes + " " * notesPaddingLength + "."
        // add spaces here so the Radio Pi display might scroll slower
        println(" " * 40)
        println(s"$symbolPadded" + pricePrePadded + "    " + notesPostPadded)
    }

    def getStocks(stocksJsonString: String): Array[Stock] = {
        val stocks = ArrayBuffer[Stock]()
        val json = JsonParser.parse(stocksJsonString)

        // this also works okay with "sbt run"
        val elements = (json \\ "stock").children
        for (acct <- elements) {
            val stock = acct.extract[Stock]
            stocks += stock
        }
        stocks.toArray
    }
    
    private def longestString(xs: Seq[String]) = xs.reduceLeft((x,y) => if (x.length > y.length) x else y)

    private def getFileContentsAsString(canonicalFilename: String): String = {
        return Source.fromFile(canonicalFilename).mkString
    }

    private def printUsageAndExit {
        System.err.println("Error: Code expects one command-line argument, and that argument ")
        System.err.println("should be the canonical name of the configuration file.")
        System.exit(-1)
    }
}










