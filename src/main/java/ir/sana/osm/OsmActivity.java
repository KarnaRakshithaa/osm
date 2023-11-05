package ir.sana.osm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lisom.com.lisom.style.LayerStyle;
import com.lisom.docs.DocBrowseActivity;
import com.lisom.feed.NewFeedItemActivity;
import com.lisom.feed.RSSFeedActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.shape.ShapeConverter;
import org.osmdroid.tileprovider.tilesource.HEREWeGoTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsmActivity extends AppCompatActivity implements MapEventsReceiver {
    private MapView map;
    private IMapController mapController;

    private String authenticatedUserEmail;
    FirebaseFirestore db;
    FirebaseStorage storage;

    List<String> projects = new ArrayList<>(5);
    String selectedProject;
    //String projectDataLocationUrl;
    String defaultSelectedLayerDataLocationUrl;
    String defaultSelectedLayer;

    //Map has two keys, layer_name and layer_gslocation
    List<Map<String, String>> availableLayers = new ArrayList<>(16);

    //Map has two keys, layer_name and layer_gslocation
    List<Map<String, String>> selectedLayers = new ArrayList<>(16);

    // K=layerName, V=KML Overlay Name. Used for deciding which Overlay object to load/unload from Map
    Map<String,String> loadedLayersOverlay = new HashMap<>(); // K=layerName, V=KML Overlay Name



    MyLocationNewOverlay mLocationOverlay;

    FolderOverlay testFolderOverlayForBubbleDisplay;
    InfoWindow bubbleWindow = null;

    TextView rssFeedBadge = null;

    private static final String TAG = "OsmActivity";


    private static final int PERMISSION_REQUEST_CODE = 1;

//    Marker.ENABLE_TEXT_LABELS_WHEN_NO_IMAGE = true;


    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            if (isStoragePermissionGranted()){

            }
        }
//        FloatingActionButton fab = findViewById(R.id.layers_checkbox_selection);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Log.d(TAG, "Layers floating action bar clicked");
//            }
//        });







        //handle permissions first, before map is created. not depicted here


        //load/initialize the osmdroid configuration, this can be done

        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map


        launchSigninFlow();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        LayerStyle.loadStyles(this);

    }

    private void setupRSSFeedBadge(String str) {
        if(str == null || str.length()<1)
            rssFeedBadge.setVisibility(View.INVISIBLE);
        else
            rssFeedBadge.setText(str);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.i(TAG, "On create context menu called");
        menu.setHeaderTitle(R.string.map_layers_selection);
        addProjectLayersSubMenus(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.layers_context_menu, menu);
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.layers_context_menu, popup.getMenu());
        addProjectLayersSubMenus(popup.getMenu());
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lisom_menu, menu);

        final MenuItem rssFeedMenuItem = menu.findItem(R.id.lisomFeed);
        View actionView = rssFeedMenuItem.getActionView();
        rssFeedBadge = (TextView) actionView.findViewById(R.id.rss_badge);
        actionView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onFeedAction(rssFeedMenuItem);
            }
        });

        setupRSSFeedBadge("5"); // hardocoded for demo
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        addProjectSubMenus(menu);
        addProjectLayersSubMenus(menu);

        return true;
    }

    private void addProjectSubMenus(Menu menu) {
        Log.d(TAG, "Adding project submenu items");
//        Menu projectsMenu = (Menu) findViewById(R.id.lisomProjects);
//        Menu projectsSubmenu = (Menu) findViewById(R.id.lisomProjects);

        MenuItem projectsItem = menu.findItem(R.id.lisomProjects);
        SubMenu projectsSubmenu = projectsItem.getSubMenu();
//        if(projectsSubmenu == null) return;
        if(! projects.isEmpty()){

            projectsSubmenu.clear();
//            projectsSubmenu.add("Super Item1");
//            projectsSubmenu.add("Super Item2");
//            projectsSubmenu.add("Super Item3");
            MenuItem.OnMenuItemClickListener projectSelectionListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    onChangeProjectAction(menuItem);
                    return true;
                }
            };
            for(String xproject: projects){
                MenuItem projectMenuItem = projectsSubmenu.add(R.id.projectsgroup1, Menu.NONE,Menu.NONE,  xproject);
                projectMenuItem.setOnMenuItemClickListener(projectSelectionListener);
                if(selectedProject != null){
                    if(projectMenuItem.getTitle().equals(selectedProject))
                        projectMenuItem.setChecked(true);
                }



            }
            projectsSubmenu.setGroupCheckable(R.id.projectsgroup1,true,true);
        }else{
            projectsSubmenu.clear();
            projectsSubmenu.add("Super Item1");
            projectsSubmenu.add("Super Item2");
            projectsSubmenu.add("Super Item3");
        }

//        findViewById(R.id.projectsgroup1).invalidate();

    }

    private void addProjectLayersContextSubMenus(Menu menu) {
        Log.d(TAG, "Adding layer submenu items");


        MenuItem layersItem = menu.findItem(R.id.lisomLayers);
        SubMenu layersSubmenu = layersItem.getSubMenu();
//        if(projectsSubmenu == null) return;
        if(! availableLayers.isEmpty()){

            layersSubmenu.clear();

            MenuItem.OnMenuItemClickListener layerSelectionListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    onChangeLayersAction(menuItem);
                    return true;
                }
            };
            for(int i=0;i<availableLayers.size();i++){
                String xLayerName = availableLayers.get(i).get("layer_name");
                MenuItem layerMenuItem = layersSubmenu.add(R.id.layersgroup1, Menu.NONE,Menu.NONE,  xLayerName);
                layerMenuItem.setOnMenuItemClickListener(layerSelectionListener);
                if(isSelectedLayer(xLayerName)) layerMenuItem.setChecked(true);
            }

            layersSubmenu.setGroupCheckable(R.id.layersgroup1,true,false);
        }else{
            layersSubmenu.clear();

        }

//        findViewById(R.id.projectsgroup1).invalidate();

    }

    private void addProjectLayersSubMenus(Menu menu) {
        Log.d(TAG, "Adding layer submenu items");


        MenuItem layersItem = menu.findItem(R.id.lisomLayers);
        SubMenu layersSubmenu = layersItem.getSubMenu();
//        if(projectsSubmenu == null) return;
        if(! availableLayers.isEmpty()){

            layersSubmenu.clear();

            MenuItem.OnMenuItemClickListener layerSelectionListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    onChangeLayersAction(menuItem);
                    return true;
                }
            };
            for(int i=0;i<availableLayers.size();i++){
                String xLayerName = availableLayers.get(i).get("layer_name");
                MenuItem layerMenuItem = layersSubmenu.add(R.id.layersgroup1, Menu.NONE,Menu.NONE,  xLayerName);
                layerMenuItem.setOnMenuItemClickListener(layerSelectionListener);
                if(isSelectedLayer(xLayerName)) layerMenuItem.setChecked(true);
            }

            layersSubmenu.setGroupCheckable(R.id.layersgroup1,true,false);
        }else{
            layersSubmenu.clear();

        }

//        findViewById(R.id.projectsgroup1).invalidate();

    }

    private boolean isSelectedLayer(String layerName){
        int selectedLayersCount = selectedLayers.size();
        if(selectedLayersCount < 1) return false;
        for(int i=0;i<selectedLayersCount;i++){
            String xLayerName = selectedLayers.get(i).get("layer_name");
            if(xLayerName.equals(layerName)) return true;
        }
        return false;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {

            default:
                System.out.println("item option selected "+ item.getItemId()+", title "+item.getTitle());
                return super.onOptionsItemSelected(item);
        }
    }

    public void onFeedAction(MenuItem mi){
        System.out.println("todo - Goto feed activity");
        setupRSSFeedBadge("");
        Intent intent = new Intent(getApplicationContext(), RSSFeedActivity.class);
        startActivity(intent);
    }

    public void onGotoDocsAction(MenuItem mi){
        Intent intent = new Intent(getApplicationContext(), DocBrowseActivity.class);
        startActivity(intent);
    }

    public void onGotoCurrentLocationAction(MenuItem mi) {
        // handle click here

        Log.d(TAG, "Enable or Disable Current Location");

//        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Location lastKnownLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if (lastKnownLoc != null) {
//            int longitude = (int) (lastKnownLoc.getLongitude() * 1000000);
//            int latitude = (int) (lastKnownLoc.getLatitude() * 1000000);
//            GeoPoint location = new GeoPoint(latitude, longitude);
//        }




        if(mLocationOverlay.isMyLocationEnabled())
            mLocationOverlay.disableMyLocation();
        else {
            mLocationOverlay.enableMyLocation();
            GeoPoint currentLocation = mLocationOverlay.getMyLocation();
            map.getController().setCenter(currentLocation);
        }

        map.invalidate();

    }


    public void onChangeBaseMapToMapnikAction(MenuItem mi) {
        System.out.println("clicked menu item title " + mi.getTitle()+", itemid "+mi.getItemId()+" basemap menu id "+R.id.basemapgroup2+", mapnik R.id "+R.id.submenu1_mapnik+", satellite R.id "+ R.id.submenu2_satellite);
        mi.setChecked(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.invalidate();

    }

    public void onChangeBaseMapToSatelliteAction(MenuItem mi) {
        System.out.println("clicked menu item title " + mi.getTitle()+", itemid "+mi.getItemId()+" basemap menu id "+R.id.basemapgroup2+", mapnik R.id "+R.id.submenu1_mapnik+", satellite R.id "+ R.id.submenu2_satellite);
        mi.setChecked(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
//        map.setTileSource(TileSourceFactory.USGS_TOPO);
        final ITileSource tileSource = new HEREWeGoTileSource(getApplicationContext());
        map.setTileSource(tileSource);
        map.invalidate();
    }

    public void onChangeProjectAction(MenuItem mi){
        mi.setChecked(true);
        System.out.println("Project Selected = "+ mi.getTitle());
        resetProjectFromMap();
        String selectedProjectFromMenu = mi.getTitle().toString();
        selectedProject = selectedProjectFromMenu;
        findProjectLayersAndFireDefaultLayerLoading(selectedProject);
    }

    public void onChangeLayersAction(MenuItem mi){

//        mi.setChecked(true);
        System.out.println("Layer Changed = "+ mi.getTitle()+" selected ?"+mi.isChecked());
        mi.setChecked(!mi.isChecked()); // basically toggle

        String relevantLayerName = mi.getTitle().toString();
        boolean whetherToLoad = mi.isChecked();

        fireLayerSelectionAction(relevantLayerName, whetherToLoad);


    }

    public void onLogoutAction(MenuItem mi){

    }



    private void loadMapUI(){

        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        map = findViewById(R.id.mapView);
        registerForContextMenu (map);
        map.setTileSource(TileSourceFactory.MAPNIK);
//        map.setTileSource(TileSourceFactory.USGS_TOPO);
//        final ITileSource tileSource = new HEREWeGoTileSource(ctx);
//        map.setTileSource(tileSource);
//        TileSourceFactory.USGS_SAT
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mapController = map.getController();


        //Add Scale Bar
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(map);
        map.getOverlays().add(myScaleBarOverlay);

        // Add current location overlay
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx),map);
        mLocationOverlay.enableMyLocation();
        map.getController().setCenter(mLocationOverlay.getMyLocation());
        map.getOverlays().add(mLocationOverlay);

        loadAccessMetadataAndFireFirstProjectLoading();


    }

    private void launchSigninFlow(){
        // Choose authentication providers
//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.EmailBuilder().build(),
//                new AuthUI.IdpConfig.PhoneBuilder().build(),
//                new AuthUI.IdpConfig.GoogleBuilder().build(),
//                new AuthUI.IdpConfig.FacebookBuilder().build(),
//                new AuthUI.IdpConfig.TwitterBuilder().build());

        List<AuthUI.IdpConfig> providers = Arrays.asList(

                new AuthUI.IdpConfig.GoogleBuilder().build());

// Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    public void loadKml(String layerName, String gsLocation) {
//        new KmlLoader(layerName, gsLocation).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        boolean zoomToBoundingBoxFlag = false;
        if(bubbleWindow == null){
            bubbleWindow = new BalloonView(R.layout.marker_bubble, map);
        }
        new KmlLoader(layerName, gsLocation, zoomToBoundingBoxFlag, bubbleWindow).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public void loadKmlAndZoomToBoundingBox(String layerName, String gsLocation) {
        boolean zoomToBoundingBoxFlag = true;
        if(bubbleWindow == null){
            bubbleWindow = new BalloonView(R.layout.marker_bubble, map);
        }

        new KmlLoader(layerName, gsLocation, zoomToBoundingBoxFlag, bubbleWindow).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        Toast.makeText(this, "Tapped - todo: work on attribute display", Toast.LENGTH_SHORT).show();
        return false;
    }
    @Override public boolean longPressHelper(GeoPoint p) {
        //DO NOTHING FOR NOW:
        Toast.makeText(this, "Long Tapped - current zoom level - "+map.getZoomLevelDouble(), Toast.LENGTH_SHORT).show();
        Marker startMarker = new Marker(map);
        startMarker.setPosition(p);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        return false;
    }



    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Log.i(TAG, user.getEmail() +" is logged in");

            user.getIdToken(true)
                    .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult result) {
                    String accessible_project = (String) result.getClaims().get("project");
                    Log.i(TAG, user.getEmail() +" has project access "+ accessible_project);

                }
            });

            authenticatedUserEmail = user.getEmail();
            loadMapUI();
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.i(TAG, " Sign-in failed. Please login");
            authenticatedUserEmail = null;
        }
    }

    private void loadAccessMetadataAndFireFirstProjectLoading(){
        String userEmail = authenticatedUserEmail;
        // Create a reference to the cities collection
        db.collection("lisom_users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                List<String> projectList = (List<String>) document.get("projects");
                                projects.addAll(projectList);
                                String firstProject = projectList.get(0); // first object
                                selectedProject = firstProject;
                                findProjectLayersAndFireDefaultLayerLoading(firstProject);
                                invalidateOptionsMenu();
                                break;
                            }
                        } else {
                            Log.d(TAG, "Error getting user project access: ", task.getException());
                        }
                    }
                });
    }

    private void findProjectLayersAndFireDefaultLayerLoading(String projectName){
        String userEmail = authenticatedUserEmail;
        // Create a reference to the cities collection
        db.collection("projects")
                .whereEqualTo("name", projectName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String gsDataLocationUrl = document.get("gslocation", String.class);
                                String layerNameToLoad = "UNNKNOWN"; // to be filled shortly
                                List<Map<String, String>> projectLayersList = (List<Map<String, String>>) document.get("layers");

                                if(projectLayersList != null && !projectLayersList.isEmpty()){
                                    availableLayers = projectLayersList;
                                    selectedLayers.add(availableLayers.get(0));
                                    layerNameToLoad = availableLayers.get(0).get("layer_name");
                                    String firstLayerGsLocation = availableLayers.get(0).get("layer_gslocation");

                                    gsDataLocationUrl = firstLayerGsLocation; // todo - a hack for now to see bridge layer shows up. to be removed
                                } else {
                                    // project does not have layers, there is a single file
                                    // create a synthetic default layer

                                    Map<String, String> defaultLayer = new HashMap<>();
                                    String syntheticLayerName = "DEFAULT";
                                    defaultLayer.put("layer_name", syntheticLayerName);
                                    defaultLayer.put("layer_gslocation", gsDataLocationUrl);
                                    availableLayers.add(defaultLayer);
                                    selectedLayers.add(defaultLayer);
                                    layerNameToLoad = syntheticLayerName;
                                }

                                defaultSelectedLayer = layerNameToLoad;

                                invalidateOptionsMenu();


                                StorageReference gsReference = storage.getReferenceFromUrl(gsDataLocationUrl); // gs://manvijlabs.appspot.com/tallagappa/tlgp.kml

                                final String  gsDataLocationUrl2 = gsDataLocationUrl;
                                gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // Got the download URL for 'users/me/profile.png'
//                                        projectDataLocationUrl = uri.toString();
                                        defaultSelectedLayerDataLocationUrl = uri.toString();
                                        Log.d(TAG, "data location url => " + defaultSelectedLayerDataLocationUrl);
                                        loadKmlAndZoomToBoundingBox(defaultSelectedLayer, defaultSelectedLayerDataLocationUrl);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                        Log.d(TAG, "Could not get download url for "+gsDataLocationUrl2, exception);
                                        defaultSelectedLayerDataLocationUrl = null;
                                        selectedProject = null;
                                        Toast.makeText(getApplicationContext(),
                                                "Failed to load project - "+exception.getMessage(),
                                                Toast.LENGTH_LONG).show();

                                        invalidateOptionsMenu();
                                        resetProjectFromMap();
                                    }
                                });

                                break;


                            }
                        } else {
                            Log.d(TAG, "Error getting project data location: ", task.getException());
                            selectedProject = null;
                            defaultSelectedLayerDataLocationUrl = null;
                            resetProjectFromMap();
                        }
                    }
                });


    }

    private void resetProjectFromMap(){

        // project data is the last overlay, so remove just the last overlay
//        map.getOverlays().clear();
//        map.getOverlays().remove(numberOfOverlays - 1); // last overlays
        Collection<String> kmlOverlayNameList = loadedLayersOverlay.values();
        List<Overlay> overlaysList = map.getOverlays();
        int overlaySize = overlaysList.size();
        List<Overlay> toBeDiscarded = new ArrayList<>();
        for(int i=0; i<overlaySize;i++){
            Overlay xOverlay = overlaysList.get(i);
            if (xOverlay instanceof FolderOverlay){
                FolderOverlay kmlOverlay = (FolderOverlay) xOverlay;
                if(kmlOverlayNameList.contains(kmlOverlay.getName()))
                    toBeDiscarded.add(kmlOverlay);

            }
        }
        map.getOverlays().removeAll(toBeDiscarded);

        map.getController().setCenter(mLocationOverlay.getMyLocation());
        map.invalidate();

        loadedLayersOverlay.clear();
        availableLayers.clear();
        selectedLayers.clear();
    }

    private void fireLayerSelectionAction(String layerName, boolean whetherToLoad){
        if (whetherToLoad)
            doLayerLoading(layerName);
        else
            doLayerUnloading(layerName);

    }

    private void doLayerLoading(String layerName){

        // 1. find the hashmap object for the layer
        Map<String, String > layer_object = null;
        for (int i = 0; i < availableLayers.size(); i++) {
            Map<String, String > tmp_layer_object = availableLayers.get(i);
            if(layerName.equals(tmp_layer_object.get("layer_name")))
                layer_object = tmp_layer_object;
        }
        // 2. add it to the selected layer list
        if(layer_object != null)
            selectedLayers.add(layer_object);
        else throw new RuntimeException("Unexpected - unknown layer name "+layerName);

        // 3. fire kml loading

        String layerGSLocation = layer_object.get("layer_gslocation");
//        String layerGSLocation = "gs://manvijlabs.appspot.com/MinorIrrigation_Ramanagara/RIVER_VALLEYS.kml";

        StorageReference gsReference = storage.getReferenceFromUrl(layerGSLocation); // gs://manvijlabs.appspot.com/tallagappa/tlgp.kml

        final String  gsDataLocationUrl2 = layerGSLocation;
        final Map<String, String > layer_object_final_reference = layer_object;
        gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
//                                        projectDataLocationUrl = uri.toString();
                String layerDataResolvedURL = uri.toString();
                Log.d(TAG, " layer data location resolved url => " + layerDataResolvedURL);
                loadKml(layerName, layerDataResolvedURL);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "Could not get download url for "+gsDataLocationUrl2, exception);
                Toast.makeText(getApplicationContext(),
                        "Failed to load layer data - "+exception.getMessage(),
                        Toast.LENGTH_LONG).show();

                selectedLayers.remove(layer_object_final_reference);

            }
        });


    }

    private void doLayerUnloading(String layerName){
//        int numberOfOverlays = map.getOverlays().size();
        String kmlOverlayName = loadedLayersOverlay.get(layerName);
        List<Overlay> overlaysList = map.getOverlays();
        int overlaySize = overlaysList.size();
        int layerIndex = -1; // about to be found out
        for(int i=0; i<overlaySize;i++){
            Overlay xOverlay = overlaysList.get(i);
            if (xOverlay instanceof FolderOverlay){
                FolderOverlay kmlOverlay = (FolderOverlay) xOverlay;
                if(kmlOverlay.getName().equals(kmlOverlayName)){
                    layerIndex = i;
                }
            }
        }

        map.getOverlays().remove(layerIndex);
        loadedLayersOverlay.remove(layerName);
        map.invalidate();


        // also remove this layer from selectedLayers List
        Map<String, String > layer_object = null;
        for (int i = 0; i < selectedLayers.size(); i++) {
            Map<String, String > tmp_layer_object = selectedLayers.get(i);
            if(layerName.equals(tmp_layer_object.get("layer_name")))
                layer_object = tmp_layer_object;
        }
        if(layer_object != null)
            selectedLayers.remove(layer_object);
        else {
            Log.w(TAG, "Unexpected - unknown layer name during layer removal"+layerName);
        }

//        loadShapeFile(null);


    }

    private void loadShapeFile(String shapeFileWithPath) { //throws Exception{
//            List<Overlay> folder = ShapeConverter.convert(mMapView, new File(myshape));
//            map.getOverlayManager().addAll(folder);
//            map.invalidate();
        File internalStorageFolder = getExternalFilesDir(null);
        File shapeFilesFolder = new File(internalStorageFolder, "shp_exprmt");
        System.out.println("shapefilefolder exists ? "+shapeFilesFolder.exists());
        File actualShapeFile = new File(shapeFilesFolder, "TLGP_UBL_STATIONS.shp");
        // /data/data/ir.aio.osm/files/shp_exprmt/TLGP_UBL_CANAL.shp
            System.out.println("internalStorageFolder location - "+internalStorageFolder.getAbsolutePath());
            File [] filesInStorage = internalStorageFolder.listFiles();
//            for(File x: filesInStorage){
//                try {
//                    System.out.println("Internal storage file - " + x.getAbsolutePath());
//                    System.out.println("Internal storage file canonical path- " + x.getCanonicalPath());
//                    System.out.println("Internal storage file just path- " + x.getPath());
//                    if(x.isDirectory()){
//                        File [] childFiles = x.listFiles();
//                        for(File xc: childFiles){
//                            System.out.println("Child Internal storage file - " + xc.getAbsolutePath());
//                            System.out.println("Internal storage file canonical path- " + xc.getCanonicalPath());
//                            System.out.println("Internal storage file just path- " + xc.getPath());
//                        }
//
//                    }
//                }catch (Exception ex){
//                    ex.printStackTrace();
//                }
//
//            }


//            List<Overlay>  folder = ShapeConverter.convert(map, new File(shapeFileWithPath));
        try {
            List<Overlay>  folder = ShapeConverter.convert(map, actualShapeFile);
            map.getOverlayManager().addAll(folder);
            map.invalidate();
        }catch(Exception ex){
            Log.w(TAG, "failed to load shape file", ex);
        }

    }



    class KmlLoader extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(OsmActivity.this);
        KmlDocument kmlDocument;
        private String kmlGSLocation;
        private String layerNameToLoad;
        private Boolean zoomToFlag = false;

        private InfoWindow balloonPopup;


        public KmlLoader(String layerName, String kml_gslocation){
//            boolean zoomToBB = false; //default
            this(layerName, kml_gslocation, false);
        }

        public KmlLoader(String layerName, String kml_gslocation, boolean zoomToBB){
//            layerNameToLoad = layerName;
//            kmlGSLocation = kml_gslocation;
//            zoomToFlag = zoomToBB;
            this(layerName, kml_gslocation, zoomToBB, null);
        }

        public KmlLoader(String layerName, String kml_gslocation, boolean zoomToBB, InfoWindow bubbleWindow){
            layerNameToLoad = layerName;
            kmlGSLocation = kml_gslocation;
            zoomToFlag = zoomToBB;
            balloonPopup = bubbleWindow;
        }




        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading Project...");
            progressDialog.show();



        }

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... voids) {

//            https://drive.google.com/file/d/1M2-xbfcArSJG654fPLI9ynfVWaluObdR/view?usp=sharing
//            https://drive.google.com/file/d/1M2-xbfcArSJG654fPLI9ynfVWaluObdR/view?usp=sharing
//            kmlDocument.parseKMZFile(getResources().openRawResource(R.raw.maramjhiri_revised), null);
//            String maramjhiri_kml_url = "https://drive.google.com/uc?id=1I3gq5WizzP0IhN5gkHkWumvQBEfBcrU-&export=download";
            String tlgp_kml_url = "https://drive.google.com/uc?id=1fdmmx7lHlpHzYCZJGfEQGiPmQApUn-MA&export=download";
//            String tlgp_kml_url2 = "https://drive.google.com/uc?id=1uoR5bFFiOcbhI2coinYBn65gMjL9UmaO&export=download";
//            kmlDocument.parseKMLUrl(maramjhiri_kml_url);
//            kmlDocument.parseKMLUrl(tlgp_kml_url);
//            kmlDocument.parseKMLUrl(tlgp_kml_url2);
//            kmlDocument.parseKMLStream(getResources().openRawResource(R.raw.sample), null);

            if(kmlGSLocation != null){

                kmlDocument = new KmlDocument();
                kmlDocument.parseKMLUrl(kmlGSLocation);
                FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.mKmlRoot.buildOverlay(map, null, null,kmlDocument);
                System.out.println("KML Overlay Name - "+kmlOverlay.getName());

                testFolderOverlayForBubbleDisplay = kmlOverlay;

                Drawable pushPinDrawable = getResources().getDrawable(R.drawable.baseline_push_pin_24);
//                InfoWindow bubbleWindow = new BubbleView(R.layout.marker_bubble, map);


                setMarkersRecursively(kmlOverlay, pushPinDrawable, balloonPopup);



                map.getOverlays().add(kmlOverlay);
                loadedLayersOverlay.put(layerNameToLoad, kmlOverlay.getName());
                MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(OsmActivity.this);
                map.getOverlays().add(0, mapEventsOverlay);

                return null;
            } else{
                Log.d(TAG, "No location available to load map");
                return null;
            }

        }

        private void setMarkersRecursively(FolderOverlay folderOverlay, Drawable pushpin, InfoWindow bubbleWindowRef){
            List<Overlay> kmlFolderOverlays = folderOverlay.getItems();
            String folderName = folderOverlay.getName();
            for(Overlay kmlOverlayItem: kmlFolderOverlays){

                if(kmlOverlayItem instanceof Marker){
                    System.out.println("kml item as marker");
                    Marker item = (Marker)kmlOverlayItem;
                    if(bubbleWindowRef != null)
                        item.setInfoWindow(bubbleWindowRef);

//                    System.out.println("Getting marker title - "+ item.getTitle());
//                    System.out.println("Getting marker snippet - "+item.getSnippet());
//                    System.out.println("Getting marker subdesc - "+item.getSubDescription());
                    item.setIcon(pushpin);

                    LayerStyle layerStyle = LayerStyle.getStyleForLayer(layerNameToLoad);

                    // since markers are PointLayers, we do not deal with line/polygon layerstyle here
                    LayerStyle.PointFeatureStyle pointFeatureStyle = layerStyle.getPointStyle();

                    if(pointFeatureStyle.hasTextIcon()) {
                        item.setIcon(null);
                        item.setTextIcon(item.getTitle());
                    } else {
                        item.setIcon(pointFeatureStyle.getIconObject());
                    }



//                    if (folderName != null && folderName.contains("MINORBRIDGES")) {
//
//                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_minorbridge));
//                    } else if (folderName != null && folderName.contains("MAJORBRIDGES")) {
//                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_majorbridge));
//                    } else if (folderName != null && folderName.contains("RUB")) {
//                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_roadunderbridge));
//                    } else if (folderName != null && folderName.contains("ROB")) {
//                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_roadoverbridge));
//                    } else if (folderName != null && folderName.contains("ALIGN_KMPOST")) {
////                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_kmpost));
//                        item.setTextIcon(item.getTitle());
//                    } else if (folderName != null && folderName.contains("STATION")) {
//                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_station));
//                    } else if (folderName != null && folderName.contains("GRAD")) {
//                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_point));
//                    } else if (folderName != null && folderName.contains("BENCH")) {
//                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_benchmark));
//                    } else if (folderName != null && folderName.contains("POWER")) {
//                        item.setIcon(getResources().getDrawable(R.drawable.rlwys_powerlines));
//                    }
//                    else if (folderName != null && layerNameToLoad.equalsIgnoreCase("KMPost")){
//                        item.setIcon(null);
//                        item.setTextIcon(item.getTitle());
//                    }





//                    item.setTextIcon(item.getTitle());
//                    item.setIcon(pushpin);
//                    item.setSubDescription(item.getTitle());
//                    item.setIcon(Drawable.createFromResourceStream(getResources(),R.drawable.baseline_push_pin_24));

//                    item.setTitle("testing title");
//                    item.setIcon(null);
//                    item.showInfoWindow();
//                    item.setTextLabelBackgroundColor(0);
//                    item.setTextLabelFontSize(12);
//                    item.setTextLabelForegroundColor(1);
//                    item.setTitle("hello world");
//                    item.setIcon(null);
//                    item.setTextIcon("hello world");
//must set the icon to null last


                }else if(kmlOverlayItem instanceof FolderOverlay){
                    FolderOverlay subFolderOverlay = (FolderOverlay)kmlOverlayItem;
                    setMarkersRecursively(subFolderOverlay, pushpin, bubbleWindow);
                }
                else{
                    System.out.println("kml overlay class is "+kmlOverlayItem.getClass());
                }

            }
        }

        private void setIconByLayer(Marker item){
            if (layerNameToLoad.equalsIgnoreCase("KMPost")){
                item.setIcon(null);
                item.setTextIcon(item.getTitle());
            }
        }

        private void loadShapeFile(String shapeFileWithPath) { //throws Exception{
//            List<Overlay> folder = ShapeConverter.convert(mMapView, new File(myshape));
//            map.getOverlayManager().addAll(folder);
//            map.invalidate();
            File internalStorageFolder = getFilesDir();
            File shapeFilesFolder = new File(internalStorageFolder, "shp_exprmt");
//            /sdcard/Android/data/ir.aio.osm/files/shp_exprmt/TLGP_UBL_PRO_ALIGNMENT.shx
            File actualShapeFile = new File(shapeFilesFolder, "TLGP_UBL_PRO_ALIGNMENT.shp");
            // /data/data/ir.aio.osm/files/shp_exprmt/TLGP_UBL_CANAL.shp
//            System.out.println("internalStorageFolder location - "+internalStorageFolder.getAbsolutePath());
//            File [] filesInStorage = internalStorageFolder.listFiles();
//            for(File x: filesInStorage){
//                try {
//                    System.out.println("Internal storage file - " + x.getAbsolutePath());
//                    System.out.println("Internal storage file canonical path- " + x.getCanonicalPath());
//                    System.out.println("Internal storage file just path- " + x.getPath());
//                }catch (Exception ex){
//                    ex.printStackTrace();
//                }
//
//            }


//            List<Overlay>  folder = ShapeConverter.convert(map, new File(shapeFileWithPath));
            try {
                List<Overlay>  folder = ShapeConverter.convert(map, actualShapeFile);
                map.getOverlayManager().addAll(folder);
                map.invalidate();
            }catch(Exception ex){
                Log.w(TAG, "failed to load shape file", ex);
            }

        }





        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            map.invalidate();

            if(zoomToFlag){
                BoundingBox bb = kmlDocument.mKmlRoot.getBoundingBox();
                map.zoomToBoundingBox(bb, true);
            }


//            findViewById(R.id.)
//            mapView.getController().setCenter(bb.getCenter());
            super.onPostExecute(aVoid);
        }
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (map != null)
            map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (map != null)
            map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }


    }

    private void ensurePermissions(){
//                requestPermissions(CONTEXT,
//                new String[] { Manifest.permission.REQUESTED_PERMISSION },
//                REQUEST_CODE);
    }
}