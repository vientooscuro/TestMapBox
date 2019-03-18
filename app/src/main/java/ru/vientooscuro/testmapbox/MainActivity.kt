package ru.vientooscuro.testmapbox


import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Math.cos
import java.lang.Math.pow
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mapBoxMap: MapboxMap? = null

    private var lastPointX: Float = 0F
    private var lastPointY: Float = 0F

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.map_box_api_key))
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { mapboxMap ->
            this.mapBoxMap = mapboxMap
            val cameraPosition = CameraPosition.Builder().target((LatLng(55.719516616365269, 37.466816220893861))).zoom(14.0).bearing(0.0).tilt(0.0).build()
            mapboxMap.cameraPosition = cameraPosition
            mapboxMap.setStyle(Style.LIGHT) { style ->
                mapboxMap.addOnMapClickListener {
                    val layers = style.layers
                    layers.forEach { layer ->
                        Log.e("loglog", layer.id)
                    }
                    val coordinate = it
                    val uuid = UUID.randomUUID()
                    val collection = FeatureCollection.fromFeatures(listOf(Feature.fromGeometry(Point.fromLngLat(coordinate.longitude, coordinate.latitude))))
                    val source = GeoJsonSource("source-$uuid", collection)
                    style.addSource(source)

                    val size = 1000.0
                    val minRadius = size / metersPerPixel(coordinate.latitude, 2.0)
                    val maxRadius = size / metersPerPixel(coordinate.latitude, 15.0)

                    val layer = CircleLayer("layer-$uuid", "source-$uuid")
                    layer.withProperties(
                        circleRadius(
                            interpolate(
                                exponential(1.75),
                                zoom(),
                                stop(2, minRadius),
                                stop(15, maxRadius)
                            )
                        ),
                        circleColor(Color.argb(150, 255, 0, 0))
                    )
                    style.addLayer(layer)
                    true
                }
            }

        }

    }
}

fun metersPerPixel(latitude: Double, zoom: Double): Double {
    val constrainedScale = pow(2.0, zoom)
    val EARTH_RADIUS_M = 6378137.0
    val m2pi = 2 * Math.PI
    val degToRad = Math.PI / 180
    val deg = latitude * degToRad
    val worldSize = constrainedScale * 512
    val cosM2pi = cos(deg) * m2pi
    val res = cosM2pi * EARTH_RADIUS_M / worldSize
    return res
}

val Float.sqr: Float
    get() = this * this

val Float.sqrt: Float
    get() = Math.sqrt(this.toDouble()).toFloat()

fun PointF.distance(point: PointF): Float {
    return ((this.x - point.x).sqr + (this.y - point.y).sqr).sqrt
}
