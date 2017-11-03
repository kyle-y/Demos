package com.example.android.testdemo.map;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.android.testdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yxb on 2017/11/3.
 */

public class MyTestActivity extends AppCompatActivity implements AMapLocationListener,
        Inputtips.InputtipsListener, GeocodeSearch.OnGeocodeSearchListener,
        AMap.OnCameraChangeListener, AMap.OnMapLoadedListener, PoiSearch.OnPoiSearchListener {

    private EditText mInput;
    private MapView mMapView;

    private AMap aMap;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;
    private UiSettings uiSetting;

    private GeocodeSearch geocoderSearch;//坐标和地址互查
    private Inputtips inputTips;//输入搜索
    private PoiSearch poiSearch;//poi搜索

    private InputtipsQuery inputquery;
    private PoiSearch.Query poiQuery;
    private RegeocodeQuery regeoQuery;
    private GeocodeQuery geoQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);
        initView();

        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapLoadedListener(this);
        aMap.setInfoWindowAdapter(infoWindowAdapter);
        initLocation();

        uiSetting = aMap.getUiSettings();
        uiSetting.setTiltGesturesEnabled(false);

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    private void initView() {

        mInput = (EditText) findViewById(R.id.input);
        mMapView = (MapView) findViewById(R.id.mapView);

        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                inputQuery(s.toString());
            }
        });
    }


    private void initLocation() {
        locationClient = new AMapLocationClient(this);
        locationClient.setLocationOption(getDefaultOption());
        startLoaction();
    }

    private void startLoaction() {
        if (locationClient != null) {
            locationClient.startLocation();
        }
    }

    private void stopLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
        }
    }

    private AMapLocationClientOption getDefaultOption() {
        locationClientOption = new AMapLocationClientOption();
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationClientOption.setGpsFirst(false);
        locationClientOption.setNeedAddress(true);
        locationClientOption.setOnceLocation(true);
        locationClientOption.setOnceLocationLatest(true);
        locationClientOption.setWifiScan(true);
        locationClientOption.setLocationCacheEnable(true);
        return locationClientOption;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (locationClient != null) {
            locationClient.stopLocation();
        }
        locationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        double lat = aMapLocation.getLatitude();
        double lng = aMapLocation.getLongitude();
        String address = aMapLocation.getAddress();
        mInput.setText(address);
        addMarker(lat, lng);
        stopLocation();
    }

    private void addMarker(double lat, double lng) {
        LatLng latlng = new LatLng(lat, lng);
        Marker marker = aMap.addMarker(new MarkerOptions().position(latlng).title("郑州").snippet("详细信息")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round)));
        Animation animation = new RotateAnimation(marker.getRotateAngle(), marker.getRotateAngle() + 180, 0, 0, 0);
        long duration = 1000L;
        animation.setDuration(duration);
        animation.setInterpolator(new LinearInterpolator());

        marker.setAnimation(animation);
        marker.startAnimation();

        Point screenPosition = aMap.getProjection().toScreenLocation(latlng);
        marker.setPositionByPixels(screenPosition.x,screenPosition.y);
        marker.showInfoWindow();
    }

    private AMap.InfoWindowAdapter infoWindowAdapter = new AMap.ImageInfoWindowAdapter() {
        @Override
        public long getInfoWindowUpdateTime() {
            return 0;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    };


    private void getAddressFromLatlng(LatLng latlng){
        if (regeoQuery == null) {
            regeoQuery = new RegeocodeQuery(new LatLonPoint(latlng.latitude, latlng.longitude), 500, GeocodeSearch.AMAP);
        } else {
            regeoQuery.setPoint(new LatLonPoint(latlng.latitude, latlng.longitude));
        }
        geocoderSearch.getFromLocationAsyn(regeoQuery);
    }

    private void getLatlngFromAddress(String address){
        if (geoQuery == null) {
            geoQuery = new GeocodeQuery(address, "0371");
        } else {
            geoQuery.setLocationName(address);
        }
        geocoderSearch.getFromLocationNameAsyn(geoQuery);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        //输入坐标获得地址
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        //输入地址获得坐标
    }

    private void inputQuery(String content){
        inputquery = new InputtipsQuery(content , "郑州");
        inputquery.setCityLimit(true);
        inputTips = new Inputtips(MyTestActivity.this, inputquery);
        inputTips.setInputtipsListener(this);
        inputTips.requestInputtipsAsyn();
    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        //当输入搜索完毕后
    }

    private void poiSearch(String content, LatLng latlng){
        poiQuery = new PoiSearch.Query(content, "", "郑州");
        poiQuery.setPageSize(10);
        poiQuery.setPageNum(5);
        poiSearch = new PoiSearch(this, poiQuery);
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latlng.latitude,
                latlng.longitude), 1000));
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        //当poi搜索成功后
        ArrayList<PoiItem> poiItems = poiResult.getPois();
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        //当poi的item搜索成功后
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        //当屏幕移动时
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        //当屏幕移动完毕
        getAddressFromLatlng(cameraPosition.target);
    }

    @Override
    public void onMapLoaded() {
        //当地图加载完毕
    }

}
