# SG Util
* Use LTA data service for bus arrivals, traffic images

# Implementation
* Kotlin + Room + ViewModel + LiveData + Retrofit2 + RX2 + Dagger2 + RecyclerView + Fresco 

## Classes
* MainActivity: Holder to manage the fragments
* dagger: Dependency injection modules
* bus: Package for bus related classes
    * model: Bus data model and services
        * BusStop, BusRoute, BusArrival, TrafficImage: Data models
        * BusDatabase, BusStopDao, BusRouteDao: Use Room to store data in the database
        * BusDataWebService: Use Retrofit2 to get the data
        * BusStopDaoWithCache: Cache bus stop data in memory
        * BusDataManager: Manges the data synchronization
    * modelview:
        * BusSearchingBaseViewModel: Base class to provide progress LiveData
        * BusStopsNearbyViewModel: Provide bus stops LiveData and handle the bus stop searching.  
            Uses bus stops and current location as two data models, so location collector is inside this class not in the view. 
        * BusArrivalsViewModel: Provide bus arrival LiveData and handle the information retrieval. Get data from web service. 
        * BusRoutesViewModel: Provides bus routes LiveData. Local search in the database only.
        * TrafficImagesViewModel: Provides traffic images LiveData. Get data from web service. 
    * view: Fragments for information display
        * BusStopsNearbyFragment: Fragment to display the bus stops nearby
        * BusStopsNearbyAdapter: Adapter for bus stops RecyclerView
        * BusArrivalsFragment: Fragment to display bus arrival information
        * BusArrivalsAdapter: Adapter for bus arrivals RecyclerView
        * BusRoutesFragment: Fragment to display bus routes information
        * BusRoutesAdapter: Adapter for bus routes RecyclerView
        * TrafficImagesFragment: Fragment to display traffic images
        * TrafficImagesAdapter: Adapter for traffic images RecyclerView
* location:
    * LocationCollector: Uses Google local service to continuously getting the current location
    * AddressFinder: Use Geocoder to convert geometric coordinates to address with LRU cache. 
