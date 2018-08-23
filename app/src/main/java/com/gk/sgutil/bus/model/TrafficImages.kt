package com.gk.sgutil.bus.model

import com.google.gson.annotations.SerializedName

class TrafficImage {
    class TrafficImages {
        @SerializedName("odata.metadata")
        var odata_metadata: String? = null

        @SerializedName("value")
        var value: Array<TrafficImage>? = null
    }

    @SerializedName("CameraID")
    var cameraID: String? = null

    @SerializedName("Latitude")
    var latitude: Double = 0.0

    @SerializedName("Longitude")
    var longitude: Double = 0.0

    @SerializedName("ImageLink")
    var imageLink: String? = null

    // Not in the response. To store the address name
    var address: String? = null
}