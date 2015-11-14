package com.devdaily.stocks

import java.io._
import scala.io.Source
import org.htmlcleaner.HtmlCleaner
import aja.NetworkUtils

/**
 * USAGE:
 * import java.io._
 * import scala.io.Source
 * import com.devdaily.stocks.StockUtils
 *
 * object Main extends App {
 *
 *   def getStockPrice(symbol: String) = {
 *       val html = IO.getHtmlFromFile(symbol)
 *       StockUtils.extractPriceFromHtml(html, symbol)
 *   }
 *
 * }
 *
 */
object StockUtils {

    /**
     * Get data from a URL with preset timeout values.
     */
    def getHtmlFromUrl(symbol: String): Option[String] = {
        val url = "http://finance.yahoo.com/q/ks?s=%s+Key+Statistics".format(symbol.trim.toUpperCase)
        try {
            Some(NetworkUtils.get(url, 10000, 10000))
        } catch {
            case ioe: java.io.IOException => None
            case ste: java.net.SocketTimeoutException => None
        }
    }

    def getHtmlFromUrlNoTimeout(symbol: String): String = {
        val url = "http://finance.yahoo.com/q/ks?s=%s+Key+Statistics".format(symbol.trim.toUpperCase)
        Source.fromURL(url).mkString
    }

    /*
     * <div class="yfi_rt_quote_summary_rt_top"><p>
     * <span class="time_rtq_ticker">
     * <span id="yfs_l84_hpq">19.70</span></span>
     */
    def extractPriceFromHtml(html: String, symbol: String): String = {
        val cleaner = new HtmlCleaner
        val rootNode = cleaner.clean(html)

        var keepLooking = true
        var price = "0.00"
        val elements = rootNode.getElementsByName("span", true)
        for (elem <- elements if keepLooking) {
            val classType = elem.getAttributeByName("id")
            if (classType != null && classType.equalsIgnoreCase("yfs_l84_" + symbol.toLowerCase)) {
                price = elem.getText.toString.trim
                keepLooking = false
            }
        }
        return price
    }

    // for testing
    def getHtmlFromFile(symbol: String) = {
        val filename = symbol + ".html"
        Source.fromFile(filename).getLines.mkString
    }

    // for testing
    def saveContentsToFile(contents: String, filename: String) {
        val pw = new PrintWriter(new File(filename))
        pw.write(contents)
        pw.close
    }

}

