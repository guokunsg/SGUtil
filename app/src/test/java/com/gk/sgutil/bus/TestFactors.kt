package com.gk.sgutil.bus

import com.gk.sgutil.bus.model.BASE_URL
import com.gk.sgutil.bus.model.BUS_ARRIVAL_ENDPOINT
import com.gk.sgutil.bus.model.BUS_ROUTES_ENDPOINT
import com.gk.sgutil.bus.model.BUS_STOPS_ENDPOINT
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import java.io.InputStream

const val HEADER_ACCOUNT_KEY = "AccountKey"
const val HEADER_ACCOUNT_KEY_VALUE = "fH270ARxS1SpB9rwJGYt2w=="

// Full url for the web services
const val URL_BUS_STOPS = BASE_URL + BUS_STOPS_ENDPOINT
const val URL_BUS_ARRIVAL = BASE_URL + BUS_ARRIVAL_ENDPOINT
const val URL_BUS_ROUTES = BASE_URL + BUS_ROUTES_ENDPOINT
const val URL_BUS_SERVICES = "http://datamall2.mytransport.sg/ltaodataservice/BusServices"

class TestFactors {
}

/**
 * Open the file in the resource folder as InputStream.
 * Resource folder is src/test/resources
 */
fun openResourceFiles(fileName: String): InputStream {
    return TestFactors::class.java.classLoader.getResourceAsStream(fileName)
}

// Helper function to download sample JSON data from the url
fun downloadSample(url: String): String? {
    val client = OkHttpClient.Builder().build();
    val request = Request.Builder()
            .url(url)
            .get()
            .header(HEADER_ACCOUNT_KEY, HEADER_ACCOUNT_KEY_VALUE)
            .build();
    val response = client.newCall(request).execute();
    return response.body()?.string()
}

// Helper function to make the Retrofit call using okhttp and returns the raw body response
fun getRawResponse(call: Call<*>): String {
    val request = call.clone().request()
    val client = OkHttpClient()
    val response = client.newCall(request).execute()
    return response.body()!!.string()
}
