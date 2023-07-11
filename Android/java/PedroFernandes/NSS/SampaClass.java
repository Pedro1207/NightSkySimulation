package PedroFernandes.NSS;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

enum SampaOption{
    SAMPA_NO_IRR,  //calculate all values except estimated solar irradiances
    SAMPA_ALL      //calculate all values
};

enum SpaOptions{
    SPA_ZA,           //calculate zenith and azimuth
    SPA_ZA_INC,       //calculate zenith, azimuth, and incidence
    SPA_ZA_RTS,       //calculate zenith, azimuth, and sun rise/transit/set values
    SPA_ALL,          //calculate all SPA output values
};

class Spa_data {
    //----------------------INPUT VALUES------------------------

    int year;            // 4-digit year,      valid range: -2000 to 6000, error code: 1
    int month;           // 2-digit month,         valid range: 1 to  12,  error code: 2
    int day;             // 2-digit day,           valid range: 1 to  31,  error code: 3
    int hour;            // Observer local hour,   valid range: 0 to  24,  error code: 4
    int minute;          // Observer local minute, valid range: 0 to  59,  error code: 5
    double second;       // Observer local second, valid range: 0 to <60,  error code: 6

    double delta_ut1;    // Fractional second difference between UTC and UT which is used
    // to adjust UTC for earth's irregular rotation rate and is derived
    // from observation only and is reported in this bulletin:
    // http://maia.usno.navy.mil/ser7/ser7.dat,
    // where delta_ut1 = DUT1
    // valid range: -1 to 1 second (exclusive), error code 17

    double delta_t;      // Difference between earth rotation time and terrestrial time
    // It is derived from observation only and is reported in this
    // bulletin: http://maia.usno.navy.mil/ser7/ser7.dat,
    // where delta_t = 32.184 + (TAI-UTC) - DUT1
    // valid range: -8000 to 8000 seconds, error code: 7

    double timezone;     // Observer time zone (negative west of Greenwich)
    // valid range: -18   to   18 hours,   error code: 8

    double longitude;    // Observer longitude (negative west of Greenwich)
    // valid range: -180  to  180 degrees, error code: 9

    double latitude;     // Observer latitude (negative south of equator)
    // valid range: -90   to   90 degrees, error code: 10

    double elevation;    // Observer elevation [meters]
    // valid range: -6500000 or higher meters,    error code: 11

    double pressure;     // Annual average local pressure [millibars]
    // valid range:    0 to 5000 millibars,       error code: 12

    double temperature;  // Annual average local temperature [degrees Celsius]
    // valid range: -273 to 6000 degrees Celsius, error code; 13

    double slope;        // Surface slope (measured from the horizontal plane)
    // valid range: -360 to 360 degrees, error code: 14

    double azm_rotation; // Surface azimuth rotation (measured from south to projection of
    //     surface normal on horizontal plane, negative east)
    // valid range: -360 to 360 degrees, error code: 15

    double atmos_refract;// Atmospheric refraction at sunrise and sunset (0.5667 deg is typical)
    // valid range: -5   to   5 degrees, error code: 16

    SpaOptions function;        // Switch to choose functions for desired output (from enumeration)

    //-----------------Intermediate OUTPUT VALUES--------------------

    double jd;          //Julian day
    double jc;          //Julian century

    double jde;         //Julian ephemeris day
    double jce;         //Julian ephemeris century
    double jme;         //Julian ephemeris millennium

    double l;           //earth heliocentric longitude [degrees]
    double b;           //earth heliocentric latitude [degrees]
    double r;           //earth radius vector [Astronomical Units, AU]

    double theta;       //geocentric longitude [degrees]
    double beta;        //geocentric latitude [degrees]

    double x0;          //mean elongation (moon-sun) [degrees]
    double x1;          //mean anomaly (sun) [degrees]
    double x2;          //mean anomaly (moon) [degrees]
    double x3;          //argument latitude (moon) [degrees]
    double x4;          //ascending longitude (moon) [degrees]

    double del_psi;     //nutation longitude [degrees]
    double del_epsilon; //nutation obliquity [degrees]
    double epsilon0;    //ecliptic mean obliquity [arc seconds]
    double epsilon;     //ecliptic true obliquity  [degrees]

    double del_tau;     //aberration correction [degrees]
    double lamda;       //apparent sun longitude [degrees]
    double nu0;         //Greenwich mean sidereal time [degrees]
    double nu;          //Greenwich sidereal time [degrees]

    double alpha;       //geocentric sun right ascension [degrees]
    double delta;       //geocentric sun declination [degrees]

    double h;           //observer hour angle [degrees]
    double xi;          //sun equatorial horizontal parallax [degrees]
    double del_alpha;   //sun right ascension parallax [degrees]
    double delta_prime; //topocentric sun declination [degrees]
    double alpha_prime; //topocentric sun right ascension [degrees]
    double h_prime;     //topocentric local hour angle [degrees]

    double e0;          //topocentric elevation angle (uncorrected) [degrees]
    double del_e;       //atmospheric refraction correction [degrees]
    double e;           //topocentric elevation angle (corrected) [degrees]

    double eot;         //equation of time [minutes]
    double srha;        //sunrise hour angle [degrees]
    double ssha;        //sunset hour angle [degrees]
    double sta;         //sun transit altitude [degrees]

    //---------------------Final OUTPUT VALUES------------------------

    double zenith;       //topocentric zenith angle [degrees]
    double azimuth_astro;//topocentric azimuth angle (westward from south) [for astronomers]
    double azimuth;      //topocentric azimuth angle (eastward from north) [for navigators and solar radiation]
    double incidence;    //surface incidence angle [degrees]

    double suntransit;   //local sun transit time (or solar noon) [fractional hour]
    double sunrise;      //local sunrise time (+/- 30 seconds) [fractional hour]
    double sunset;       //local sunset time (+/- 30 seconds) [fractional hour]

    public Spa_data() {

    }

    public Spa_data(int year, int month, int day, int hour, int minute, double second, double delta_ut1, double delta_t, double timezone, double longitude, double latitude, double elevation, double pressure, double temperature, double slope, double azm_rotation, double atmos_refract, SpaOptions function, double jd, double jc, double jde, double jce, double jme, double l, double b, double r, double theta, double beta, double x0, double x1, double x2, double x3, double x4, double del_psi, double del_epsilon, double epsilon0, double epsilon, double del_tau, double lamda, double nu0, double nu, double alpha, double delta, double h, double xi, double del_alpha, double delta_prime, double alpha_prime, double h_prime, double e0, double del_e, double e, double eot, double srha, double ssha, double sta, double zenith, double azimuth_astro, double azimuth, double incidence, double suntransit, double sunrise, double sunset) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.delta_ut1 = delta_ut1;
        this.delta_t = delta_t;
        this.timezone = timezone;
        this.longitude = longitude;
        this.latitude = latitude;
        this.elevation = elevation;
        this.pressure = pressure;
        this.temperature = temperature;
        this.slope = slope;
        this.azm_rotation = azm_rotation;
        this.atmos_refract = atmos_refract;
        this.function = function;
        this.jd = jd;
        this.jc = jc;
        this.jde = jde;
        this.jce = jce;
        this.jme = jme;
        this.l = l;
        this.b = b;
        this.r = r;
        this.theta = theta;
        this.beta = beta;
        this.x0 = x0;
        this.x1 = x1;
        this.x2 = x2;
        this.x3 = x3;
        this.x4 = x4;
        this.del_psi = del_psi;
        this.del_epsilon = del_epsilon;
        this.epsilon0 = epsilon0;
        this.epsilon = epsilon;
        this.del_tau = del_tau;
        this.lamda = lamda;
        this.nu0 = nu0;
        this.nu = nu;
        this.alpha = alpha;
        this.delta = delta;
        this.h = h;
        this.xi = xi;
        this.del_alpha = del_alpha;
        this.delta_prime = delta_prime;
        this.alpha_prime = alpha_prime;
        this.h_prime = h_prime;
        this.e0 = e0;
        this.del_e = del_e;
        this.e = e;
        this.eot = eot;
        this.srha = srha;
        this.ssha = ssha;
        this.sta = sta;
        this.zenith = zenith;
        this.azimuth_astro = azimuth_astro;
        this.azimuth = azimuth;
        this.incidence = incidence;
        this.suntransit = suntransit;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public Spa_data clone() {
        Spa_data newData = new Spa_data(year, month, day, hour, minute, second, delta_ut1, delta_t, timezone, longitude, latitude, elevation, pressure, temperature, slope, azm_rotation, atmos_refract, function, jd, jc, jde, jce, jme, l, b, r, theta, beta, x0, x1, x2, x3, x4, del_psi, del_epsilon, epsilon0, epsilon, del_tau, lamda, nu0, nu, alpha, delta, h, xi, del_alpha, delta_prime, alpha_prime, h_prime, e0, del_e, e, eot, srha, ssha, sta, zenith, azimuth_astro, azimuth, incidence, suntransit, sunrise, sunset);
        return newData;
    }
}

//enumeration for function codes to select desired final outputs from SAMPA
enum SampaOptions{
        SAMPA_NO_IRR,  //calculate all values except estimated solar irradiances
        SAMPA_ALL      //calculate all values
        };

        class Mpa_data
        {
        //-----------------Intermediate MPA OUTPUT VALUES--------------------

        double l_prime;		//moon mean longitude [degrees]
        double d;			//moon mean elongation [degrees]
        double m;			//sun mean anomaly [degrees]
        double m_prime;		//moon mean anomaly [degrees]
        double f;           //moon argument of latitude [degrees]
        double l;			//term l
        double r;			//term r
        double b;			//term b
        double lamda_prime; //moon longitude [degrees]
        double beta;		//moon latitude [degrees]
        double cap_delta;   //distance from earth to moon [kilometers]
        double pi;          //moon equatorial horizontal parallax [degrees]
        double lamda;       //apparent moon longitude [degrees]

        double alpha;       //geocentric moon right ascension [degrees]
        double delta;       //geocentric moon declination [degrees]

        double h;           //observer hour angle [degrees]
        double del_alpha;   //moon right ascension parallax [degrees]
        double delta_prime; //topocentric moon declination [degrees]
        double alpha_prime; //topocentric moon right ascension [degrees]
        double h_prime;     //topocentric local hour angle [degrees]

        double e0;          //topocentric elevation angle (uncorrected) [degrees]
        double del_e;       //atmospheric refraction correction [degrees]
        double e;           //topocentric elevation angle (corrected) [degrees]

        //---------------------Final MPA OUTPUT VALUES------------------------

        double zenith;        //topocentric zenith angle [degrees]
        double azimuth_astro; //topocentric azimuth angle (westward from south) [for astronomers]
        double azimuth;       //topocentric azimuth angle (eastward from north) [for navigators and solar radiation]

        } //Moon Position Algorithm (MPA) structure

        class Sampa_data
        {

        SpaOptions function; //Switch to choose functions for desired output (from enumeration)

        //---------INPUT VALUES required for estimated solar irradiances--------

        double bird_ozone; //total column ozone thickness [cm] -- range from 0.05 - 0.4
        double bird_pwv;   //total column water vapor [cm] -- range from 0.01 - 6.5
        double bird_aod;   //broadband aerosol optical depth -- range from 0.02 - 0.5
        double bird_ba;	   //forward scattering factor -- 0.85 recommended for rural aerosols
        double bird_albedo;//ground reflectance -- earth typical is 0.2, snow 0.9, vegitation 0.25

        //---------------------Final SAMPA OUTPUT VALUES------------------------

        double ems; //local observed, topocentric, angular distance between sun and moon centers [degrees]
        double rs;	//radius of sun disk [degrees]
        double rm;  //radius of moon disk [degrees]

        double a_sul;     //area of sun's unshaded lune (SUL) during eclipse [degrees squared]
        double a_sul_pct; //percent area of SUL during eclipse [percent]

        double dni;       //estimated direct normal solar irradiance using SERI/NREL Bird Clear Sky Model [W/m^2]
        double dni_sul;   //estimated direct normal solar irradiance from the sun's unshaded lune [W/m^2]

        double ghi;       //estimated global horizontal solar irradiance using SERI/NREL Bird Clear Sky Model [W/m^2]
        double ghi_sul;   //estimated global horizontal solar irradiance from the sun's unshaded lune [W/m^2]

        double dhi;       //estimated diffuse horizontal solar irradiance using SERI/NREL Bird Clear Sky Model [W/m^2]
        double dhi_sul;   //estimated diffuse horizontal solar irradiance from the sun's unshaded lune [W/m^2]

        } //Solar and Moon Position Algorithm (SAMPA) structure




public class SampaClass {

    Spa_data spa;
    Mpa_data mpa;
    Sampa_data sampa;

    public SampaClass(){
        spa = new Spa_data();
        mpa = new Mpa_data();
        sampa = new Sampa_data();
    }

    public void setValues(int year, int month, int day, int hour, int minute, double second, double timezone, double delta_ut1, double delta_t, double longitude, double latitude, double elevation, double pressure, double temperature, double slope, double azm_rotation, double atmos_refract, SpaOptions function){
        spa.year = year;
        spa.month = month;
        spa.day = day;
        spa.hour = hour;
        spa.minute = minute;
        spa.second = second;
        spa.delta_ut1 = delta_ut1;
        spa.delta_t = delta_t;
        spa.timezone = timezone;
        spa.longitude = longitude;
        spa.latitude = latitude;
        spa.elevation = elevation;
        spa.pressure = pressure;
        spa.temperature = temperature;
        spa.slope = slope;
        spa.azm_rotation = azm_rotation;
        spa.atmos_refract = atmos_refract;
        spa.function = function;
    }


    //---------------------------------------------------spa.c-----------------------------------------------------

    double PI = 3.1415926535897932384626433832795028841971;
    double SUN_RADIUS = 0.26667;

    int L_COUNT = 6;
    int B_COUNT = 2;
    int R_COUNT = 5;
    int Y_COUNT = 63;

    int L_MAX_SUBCOUNT = 64;
    int B_MAX_SUBCOUNT = 5;
    int R_MAX_SUBCOUNT = 40;

    enum Terms1 {TERM_A, TERM_B, TERM_C, TERM_COUNT};
    enum Terms2 {TERM_X0, TERM_X1, TERM_X2, TERM_X3, TERM_X4, TERM_X_COUNT};
    enum Terms3 {TERM_PSI_A, TERM_PSI_B, TERM_EPS_C, TERM_EPS_D, TERM_PE_COUNT};
    enum Terms4 {JD_MINUS, JD_ZERO, JD_PLUS, JD_COUNT};
    enum Terms5 {SUN_TRANSIT, SUN_RISE, SUN_SET, SUN_COUNT};

    int TERM_Y_COUNT = Terms2.TERM_X_COUNT.ordinal(); //MAYBE WRONG!!!!!!!!!!!!!!!!!!!!!!

    int[] l_subcount = {64,34,20,7,3,1};
    int[] b_subcount = {5,2};
    int[] r_subcount = {40,10,6,2,1};

///////////////////////////////////////////////////
///  Earth Periodic Terms
///////////////////////////////////////////////////
double[][][] L_TERMS =
    {
        {
            {175347046.0,0,0},
            {3341656.0,4.6692568,6283.07585},
            {34894.0,4.6261,12566.1517},
            {3497.0,2.7441,5753.3849},
            {3418.0,2.8289,3.5231},
            {3136.0,3.6277,77713.7715},
            {2676.0,4.4181,7860.4194},
            {2343.0,6.1352,3930.2097},
            {1324.0,0.7425,11506.7698},
            {1273.0,2.0371,529.691},
            {1199.0,1.1096,1577.3435},
            {990,5.233,5884.927},
            {902,2.045,26.298},
            {857,3.508,398.149},
            {780,1.179,5223.694},
            {753,2.533,5507.553},
            {505,4.583,18849.228},
            {492,4.205,775.523},
            {357,2.92,0.067},
            {317,5.849,11790.629},
            {284,1.899,796.298},
            {271,0.315,10977.079},
            {243,0.345,5486.778},
            {206,4.806,2544.314},
            {205,1.869,5573.143},
            {202,2.458,6069.777},
            {156,0.833,213.299},
            {132,3.411,2942.463},
            {126,1.083,20.775},
            {115,0.645,0.98},
            {103,0.636,4694.003},
            {102,0.976,15720.839},
            {102,4.267,7.114},
            {99,6.21,2146.17},
            {98,0.68,155.42},
            {86,5.98,161000.69},
            {85,1.3,6275.96},
            {85,3.67,71430.7},
            {80,1.81,17260.15},
            {79,3.04,12036.46},
            {75,1.76,5088.63},
            {74,3.5,3154.69},
            {74,4.68,801.82},
            {70,0.83,9437.76},
            {62,3.98,8827.39},
            {61,1.82,7084.9},
            {57,2.78,6286.6},
            {56,4.39,14143.5},
            {56,3.47,6279.55},
            {52,0.19,12139.55},
            {52,1.33,1748.02},
            {51,0.28,5856.48},
            {49,0.49,1194.45},
            {41,5.37,8429.24},
            {41,2.4,19651.05},
            {39,6.17,10447.39},
            {37,6.04,10213.29},
            {37,2.57,1059.38},
            {36,1.71,2352.87},
            {36,1.78,6812.77},
            {33,0.59,17789.85},
            {30,0.44,83996.85},
            {30,2.74,1349.87},
            {25,3.16,4690.48}
        },
        {
            {628331966747.0,0,0},
            {206059.0,2.678235,6283.07585},
            {4303.0,2.6351,12566.1517},
            {425.0,1.59,3.523},
            {119.0,5.796,26.298},
            {109.0,2.966,1577.344},
            {93,2.59,18849.23},
            {72,1.14,529.69},
            {68,1.87,398.15},
            {67,4.41,5507.55},
            {59,2.89,5223.69},
            {56,2.17,155.42},
            {45,0.4,796.3},
            {36,0.47,775.52},
            {29,2.65,7.11},
            {21,5.34,0.98},
            {19,1.85,5486.78},
            {19,4.97,213.3},
            {17,2.99,6275.96},
            {16,0.03,2544.31},
            {16,1.43,2146.17},
            {15,1.21,10977.08},
            {12,2.83,1748.02},
            {12,3.26,5088.63},
            {12,5.27,1194.45},
            {12,2.08,4694},
            {11,0.77,553.57},
            {10,1.3,6286.6},
            {10,4.24,1349.87},
            {9,2.7,242.73},
            {9,5.64,951.72},
            {8,5.3,2352.87},
            {6,2.65,9437.76},
            {6,4.67,4690.48}
        },
        {
            {52919.0,0,0},
            {8720.0,1.0721,6283.0758},
            {309.0,0.867,12566.152},
            {27,0.05,3.52},
            {16,5.19,26.3},
            {16,3.68,155.42},
            {10,0.76,18849.23},
            {9,2.06,77713.77},
            {7,0.83,775.52},
            {5,4.66,1577.34},
            {4,1.03,7.11},
            {4,3.44,5573.14},
            {3,5.14,796.3},
            {3,6.05,5507.55},
            {3,1.19,242.73},
            {3,6.12,529.69},
            {3,0.31,398.15},
            {3,2.28,553.57},
            {2,4.38,5223.69},
            {2,3.75,0.98}
        },
        {
            {289.0,5.844,6283.076},
            {35,0,0},
            {17,5.49,12566.15},
            {3,5.2,155.42},
            {1,4.72,3.52},
            {1,5.3,18849.23},
            {1,5.97,242.73}
        },
        {
            {114.0,3.142,0},
            {8,4.13,6283.08},
            {1,3.84,12566.15}
        },
        {
            {1,3.14,0}
        }
    };

double[][][] B_TERMS =
    {
        {
            {280.0,3.199,84334.662},
            {102.0,5.422,5507.553},
            {80,3.88,5223.69},
            {44,3.7,2352.87},
            {32,4,1577.34}
        },
        {
            {9,3.9,5507.55},
            {6,1.73,5223.69}
        }
    };

double[][][] R_TERMS =
    {
        {
            {100013989.0,0,0},
            {1670700.0,3.0984635,6283.07585},
            {13956.0,3.05525,12566.1517},
            {3084.0,5.1985,77713.7715},
            {1628.0,1.1739,5753.3849},
            {1576.0,2.8469,7860.4194},
            {925.0,5.453,11506.77},
            {542.0,4.564,3930.21},
            {472.0,3.661,5884.927},
            {346.0,0.964,5507.553},
            {329.0,5.9,5223.694},
            {307.0,0.299,5573.143},
            {243.0,4.273,11790.629},
            {212.0,5.847,1577.344},
            {186.0,5.022,10977.079},
            {175.0,3.012,18849.228},
            {110.0,5.055,5486.778},
            {98,0.89,6069.78},
            {86,5.69,15720.84},
            {86,1.27,161000.69},
            {65,0.27,17260.15},
            {63,0.92,529.69},
            {57,2.01,83996.85},
            {56,5.24,71430.7},
            {49,3.25,2544.31},
            {47,2.58,775.52},
            {45,5.54,9437.76},
            {43,6.01,6275.96},
            {39,5.36,4694},
            {38,2.39,8827.39},
            {37,0.83,19651.05},
            {37,4.9,12139.55},
            {36,1.67,12036.46},
            {35,1.84,2942.46},
            {33,0.24,7084.9},
            {32,0.18,5088.63},
            {32,1.78,398.15},
            {28,1.21,6286.6},
            {28,1.9,6279.55},
            {26,4.59,10447.39}
        },
        {
            {103019.0,1.10749,6283.07585},
            {1721.0,1.0644,12566.1517},
            {702.0,3.142,0},
            {32,1.02,18849.23},
            {31,2.84,5507.55},
            {25,1.32,5223.69},
            {18,1.42,1577.34},
            {10,5.91,10977.08},
            {9,1.42,6275.96},
            {9,0.27,5486.78}
        },
        {
            {4359.0,5.7846,6283.0758},
            {124.0,5.579,12566.152},
            {12,3.14,0},
            {9,3.63,77713.77},
            {6,1.87,5573.14},
            {3,5.47,18849.23}
        },
        {
            {145.0,4.273,6283.076},
            {7,3.92,12566.15}
        },
        {
            {4,2.56,6283.08}
        }
    };

////////////////////////////////////////////////////////////////
///  Periodic Terms for the nutation in longitude and obliquity
////////////////////////////////////////////////////////////////

int[][] Y_TERMS =
    {
        {0,0,0,0,1},
        {-2,0,0,2,2},
        {0,0,0,2,2},
        {0,0,0,0,2},
        {0,1,0,0,0},
        {0,0,1,0,0},
        {-2,1,0,2,2},
        {0,0,0,2,1},
        {0,0,1,2,2},
        {-2,-1,0,2,2},
        {-2,0,1,0,0},
        {-2,0,0,2,1},
        {0,0,-1,2,2},
        {2,0,0,0,0},
        {0,0,1,0,1},
        {2,0,-1,2,2},
        {0,0,-1,0,1},
        {0,0,1,2,1},
        {-2,0,2,0,0},
        {0,0,-2,2,1},
        {2,0,0,2,2},
        {0,0,2,2,2},
        {0,0,2,0,0},
        {-2,0,1,2,2},
        {0,0,0,2,0},
        {-2,0,0,2,0},
        {0,0,-1,2,1},
        {0,2,0,0,0},
        {2,0,-1,0,1},
        {-2,2,0,2,2},
        {0,1,0,0,1},
        {-2,0,1,0,1},
        {0,-1,0,0,1},
        {0,0,2,-2,0},
        {2,0,-1,2,1},
        {2,0,1,2,2},
        {0,1,0,2,2},
        {-2,1,1,0,0},
        {0,-1,0,2,2},
        {2,0,0,2,1},
        {2,0,1,0,0},
        {-2,0,2,2,2},
        {-2,0,1,2,1},
        {2,0,-2,0,1},
        {2,0,0,0,1},
        {0,-1,1,0,0},
        {-2,-1,0,2,1},
        {-2,0,0,0,1},
        {0,0,2,2,1},
        {-2,0,2,0,1},
        {-2,1,0,2,1},
        {0,0,1,-2,0},
        {-1,0,1,0,0},
        {-2,1,0,0,0},
        {1,0,0,0,0},
        {0,0,1,2,0},
        {0,0,-2,2,2},
        {-1,-1,1,0,0},
        {0,1,1,0,0},
        {0,-1,1,2,2},
        {2,-1,-1,2,2},
        {0,0,3,2,2},
        {2,-1,0,2,2},
    };

double[][] PE_TERMS = {
        {-171996,-174.2,92025,8.9},
        {-13187,-1.6,5736,-3.1},
        {-2274,-0.2,977,-0.5},
        {2062,0.2,-895,0.5},
        {1426,-3.4,54,-0.1},
        {712,0.1,-7,0},
        {-517,1.2,224,-0.6},
        {-386,-0.4,200,0},
        {-301,0,129,-0.1},
        {217,-0.5,-95,0.3},
        {-158,0,0,0},
        {129,0.1,-70,0},
        {123,0,-53,0},
        {63,0,0,0},
        {63,0.1,-33,0},
        {-59,0,26,0},
        {-58,-0.1,32,0},
        {-51,0,27,0},
        {48,0,0,0},
        {46,0,-24,0},
        {-38,0,16,0},
        {-31,0,13,0},
        {29,0,0,0},
        {29,0,-12,0},
        {26,0,0,0},
        {-22,0,0,0},
        {21,0,-10,0},
        {17,-0.1,0,0},
        {16,0,-8,0},
        {-16,0.1,7,0},
        {-15,0,9,0},
        {-13,0,7,0},
        {-12,0,6,0},
        {11,0,0,0},
        {-10,0,5,0},
        {-8,0,3,0},
        {7,0,-3,0},
        {-7,0,0,0},
        {-7,0,3,0},
        {-7,0,3,0},
        {6,0,0,0},
        {6,0,-3,0},
        {6,0,-3,0},
        {-6,0,3,0},
        {-6,0,3,0},
        {5,0,0,0},
        {-5,0,3,0},
        {-5,0,3,0},
        {-5,0,3,0},
        {4,0,0,0},
        {4,0,0,0},
        {4,0,0,0},
        {-4,0,0,0},
        {-4,0,0,0},
        {-4,0,0,0},
        {3,0,0,0},
        {-3,0,0,0},
        {-3,0,0,0},
        {-3,0,0,0},
        {-3,0,0,0},
        {-3,0,0,0},
        {-3,0,0,0},
        {-3,0,0,0},
    };

///////////////////////////////////////////////

    double rad2deg(double radians)
    {
        return (180.0/PI)*radians;
    }

    double deg2rad(double degrees)
    {
        return (PI/180.0)*degrees;
    }

    int integer(double value)
    {
        return (int) value;
    }

    double limit_degrees(double degrees)
    {
        double limited;

        degrees /= 360.0;
        limited = 360.0*(degrees-floor(degrees));
        if (limited < 0) limited += 360.0;

        return limited;
    }

    double limit_degrees180pm(double degrees)
    {
        double limited;

        degrees /= 360.0;
        limited = 360.0*(degrees-floor(degrees));
        if      (limited < -180.0) limited += 360.0;
        else if (limited >  180.0) limited -= 360.0;

        return limited;
    }

    double limit_degrees180(double degrees)
    {
        double limited;

        degrees /= 180.0;
        limited = 180.0*(degrees-floor(degrees));
        if (limited < 0) limited += 180.0;

        return limited;
    }

    double limit_zero2one(double value)
    {
        double limited;

        limited = value - floor(value);
        if (limited < 0) limited += 1.0;

        return limited;
    }

    double limit_minutes(double minutes)
    {
        double limited=minutes;

        if      (limited < -20.0) limited += 1440.0;
        else if (limited >  20.0) limited -= 1440.0;

        return limited;
    }

    double dayfrac_to_local_hr(double dayfrac, double timezone)
    {
        return 24.0*limit_zero2one(dayfrac + timezone/24.0);
    }

    double third_order_polynomial(double a, double b, double c, double d, double x)
    {
        return ((a*x + b)*x + c)*x + d;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    int validate_inputs()
    {
        if ((spa.year        < -2000) || (spa.year        > 6000)) return 1;
        if ((spa.month       < 1    ) || (spa.month       > 12  )) return 2;
        if ((spa.day         < 1    ) || (spa.day         > 31  )) return 3;
        if ((spa.hour        < 0    ) || (spa.hour        > 24  )) return 4;
        if ((spa.minute      < 0    ) || (spa.minute      > 59  )) return 5;
        if ((spa.second      < 0    ) || (spa.second      >=60  )) return 6;
        if ((spa.pressure    < 0    ) || (spa.pressure    > 5000)) return 12;
        if ((spa.temperature <= -273) || (spa.temperature > 6000)) return 13;
        if ((spa.delta_ut1   <= -1  ) || (spa.delta_ut1   >= 1  )) return 17;
        if ((spa.hour        == 24  ) && (spa.minute      > 0   )) return 5;
        if ((spa.hour        == 24  ) && (spa.second      > 0   )) return 6;

        if (abs(spa.delta_t)       > 8000    ) return 7;
        if (abs(spa.timezone)      > 18      ) return 8;
        if (abs(spa.longitude)     > 180     ) return 9;
        if (abs(spa.latitude)      > 90      ) return 10;
        if (abs(spa.atmos_refract) > 5       ) return 16;
        if (     spa.elevation      < -6500000) return 11;

        if ((spa.function == SpaOptions.SPA_ZA_INC) || (spa.function == SpaOptions.SPA_ALL))
        {
            if (abs(spa.slope)         > 360) return 14;
            if (abs(spa.azm_rotation)  > 360) return 15;
        }

        return 0;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    double julian_day (int year, int month, int day, int hour, int minute, double second, double dut1, double tz)
    {
        double day_decimal, julian_day, a;

        day_decimal = day + (hour - tz + (minute + (second + dut1)/60.0)/60.0)/24.0;

        if (month < 3) {
            month += 12;
            year--;
        }

        julian_day = integer(365.25*(year+4716.0)) + integer(30.6001*(month+1)) + day_decimal - 1524.5;

        if (julian_day > 2299160.0) {
            a = integer(year/100);
            julian_day += (2 - a + integer(a/4));
        }

        return julian_day;
    }

    double julian_century(double jd)
    {
        return (jd-2451545.0)/36525.0;
    }

    double julian_ephemeris_day(double jd, double delta_t)
    {
        return jd+delta_t/86400.0;
    }

    double julian_ephemeris_century(double jde)
    {
        return (jde - 2451545.0)/36525.0;
    }

    double julian_ephemeris_millennium(double jce)
    {
        return (jce/10.0);
    }

    double earth_periodic_term_summation(double[][] terms, int count, double jme)
    {
        int i;
        double sum=0;

        for (i = 0; i < count; i++)
            sum += terms[i][Terms1.TERM_A.ordinal()]*cos(terms[i][Terms1.TERM_B.ordinal()]+terms[i][Terms1.TERM_C.ordinal()]*jme);

        return sum;
    }

    double earth_values(double term_sum[], int count, double jme)
    {
        int i;
        double sum=0;

        for (i = 0; i < count; i++)
            sum += term_sum[i]*pow(jme, i);

        sum /= 1.0e8;

        return sum;
    }

    double earth_heliocentric_longitude(double jme)
    {
        double sum[] = new double[L_COUNT];
        int i;

        for (i = 0; i < L_COUNT; i++)
            sum[i] = earth_periodic_term_summation(L_TERMS[i], l_subcount[i], jme);

        return limit_degrees(rad2deg(earth_values(sum, L_COUNT, jme)));

    }

    double earth_heliocentric_latitude(double jme)
    {
        double sum[] = new double[B_COUNT];
        int i;

        for (i = 0; i < B_COUNT; i++)
            sum[i] = earth_periodic_term_summation(B_TERMS[i], b_subcount[i], jme);

        return rad2deg(earth_values(sum, B_COUNT, jme));

    }

    double earth_radius_vector(double jme)
    {
        double sum[] = new double[R_COUNT];
        int i;

        for (i = 0; i < R_COUNT; i++)
            sum[i] = earth_periodic_term_summation(R_TERMS[i], r_subcount[i], jme);

        return earth_values(sum, R_COUNT, jme);

    }

    double geocentric_longitude(double l)
    {
        double theta = l + 180.0;

        if (theta >= 360.0) theta -= 360.0;

        return theta;
    }

    double geocentric_latitude(double b)
    {
        return -b;
    }

    double mean_elongation_moon_sun(double jce)
    {
        return third_order_polynomial(1.0/189474.0, -0.0019142, 445267.11148, 297.85036, jce);
    }

    double mean_anomaly_sun(double jce)
    {
        return third_order_polynomial(-1.0/300000.0, -0.0001603, 35999.05034, 357.52772, jce);
    }

    double mean_anomaly_moon(double jce)
    {
        return third_order_polynomial(1.0/56250.0, 0.0086972, 477198.867398, 134.96298, jce);
    }

    double argument_latitude_moon(double jce)
    {
        return third_order_polynomial(1.0/327270.0, -0.0036825, 483202.017538, 93.27191, jce);
    }

    double ascending_longitude_moon(double jce)
    {
        return third_order_polynomial(1.0/450000.0, 0.0020708, -1934.136261, 125.04452, jce);
    }

    double xy_term_summation(int i, double x[])
    {
        int j;
        double sum=0;

        for (j = 0; j < TERM_Y_COUNT; j++)
            sum += x[j]*Y_TERMS[i][j];

        return sum;
    }

    void nutation_longitude_and_obliquity(double jce, double x[])
    {
        int i;
        double xy_term_sum, sum_psi=0, sum_epsilon=0;

        for (i = 0; i < Y_COUNT; i++) {
            xy_term_sum  = deg2rad(xy_term_summation(i, x));
            sum_psi     += (PE_TERMS[i][Terms3.TERM_PSI_A.ordinal()] + jce*PE_TERMS[i][Terms3.TERM_PSI_B.ordinal()])*sin(xy_term_sum);
            sum_epsilon += (PE_TERMS[i][Terms3.TERM_EPS_C.ordinal()] + jce*PE_TERMS[i][Terms3.TERM_EPS_D.ordinal()])*cos(xy_term_sum);
        }

    spa.del_psi     = sum_psi     / 36000000.0;
    spa.del_epsilon = sum_epsilon / 36000000.0;
    }

    double ecliptic_mean_obliquity(double jme)
    {
        double u = jme/10.0;

        return 84381.448 + u*(-4680.93 + u*(-1.55 + u*(1999.25 + u*(-51.38 + u*(-249.67 +
                u*(  -39.05 + u*( 7.12 + u*(  27.87 + u*(  5.79 + u*2.45)))))))));
    }

    double ecliptic_true_obliquity(double delta_epsilon, double epsilon0)
    {
        return delta_epsilon + epsilon0/3600.0;
    }

    double aberration_correction(double r)
    {
        return -20.4898 / (3600.0*r);
    }

    double apparent_sun_longitude(double theta, double delta_psi, double delta_tau)
    {
        return theta + delta_psi + delta_tau;
    }

    double greenwich_mean_sidereal_time (double jd, double jc)
    {
        return limit_degrees(280.46061837 + 360.98564736629 * (jd - 2451545.0) +
                jc*jc*(0.000387933 - jc/38710000.0));
    }

    double greenwich_sidereal_time (double nu0, double delta_psi, double epsilon)
    {
        return nu0 + delta_psi*cos(deg2rad(epsilon));
    }

    double geocentric_right_ascension(double lamda, double epsilon, double beta)
    {
        double lamda_rad   = deg2rad(lamda);
        double epsilon_rad = deg2rad(epsilon);

        return limit_degrees(rad2deg(atan2(sin(lamda_rad)*cos(epsilon_rad) -
                tan(deg2rad(beta))*sin(epsilon_rad), cos(lamda_rad))));
    }

    double geocentric_declination(double beta, double epsilon, double lamda)
    {
        double beta_rad    = deg2rad(beta);
        double epsilon_rad = deg2rad(epsilon);

        return rad2deg(asin(sin(beta_rad)*cos(epsilon_rad) +
                cos(beta_rad)*sin(epsilon_rad)*sin(deg2rad(lamda))));
    }

    double observer_hour_angle(double nu, double longitude, double alpha_deg)
    {
        return limit_degrees(nu + longitude - alpha_deg);
    }

    double sun_equatorial_horizontal_parallax(double r)
    {
        return 8.794 / (3600.0 * r);
    }

    void right_ascension_parallax_and_topocentric_dec(double latitude, double elevation,
                                                      double xi, double h, double delta) {
        double delta_alpha_rad;
        double lat_rad = deg2rad(latitude);
        double xi_rad = deg2rad(xi);
        double h_rad = deg2rad(h);
        double delta_rad = deg2rad(delta);
        double u = atan(0.99664719 * tan(lat_rad));
        double y = 0.99664719 * sin(u) + elevation * sin(lat_rad) / 6378140.0;
        double x = cos(u) + elevation * cos(lat_rad) / 6378140.0;

        delta_alpha_rad = atan2(-x * sin(xi_rad) * sin(h_rad),
                cos(delta_rad) - x * sin(xi_rad) * cos(h_rad));

        spa.delta_prime = rad2deg(atan2((sin(delta_rad) - y * sin(xi_rad)) * cos(delta_alpha_rad),
                cos(delta_rad) - x * sin(xi_rad) * cos(h_rad)));

        spa.del_alpha = rad2deg(delta_alpha_rad);
    }

    double topocentric_right_ascension(double alpha_deg, double delta_alpha)
    {
        return alpha_deg + delta_alpha;
    }

    double topocentric_local_hour_angle(double h, double delta_alpha)
    {
        return h - delta_alpha;
    }

    double topocentric_elevation_angle(double latitude, double delta_prime, double h_prime)
    {
        double lat_rad         = deg2rad(latitude);
        double delta_prime_rad = deg2rad(delta_prime);

        return rad2deg(asin(sin(lat_rad)*sin(delta_prime_rad) +
                cos(lat_rad)*cos(delta_prime_rad) * cos(deg2rad(h_prime))));
    }

    double atmospheric_refraction_correction(double pressure, double temperature,
                                             double atmos_refract, double e0)
    {
        double del_e = 0;

        if (e0 >= -1*(SUN_RADIUS + atmos_refract))
            del_e = (pressure / 1010.0) * (283.0 / (273.0 + temperature)) *
                    1.02 / (60.0 * tan(deg2rad(e0 + 10.3/(e0 + 5.11))));

        return del_e;
    }

    double topocentric_elevation_angle_corrected(double e0, double delta_e)
    {
        return e0 + delta_e;
    }

    double topocentric_zenith_angle(double e)
    {
        return 90.0 - e;
    }

    double topocentric_azimuth_angle_astro(double h_prime, double latitude, double delta_prime)
    {
        double h_prime_rad = deg2rad(h_prime);
        double lat_rad     = deg2rad(latitude);

        return limit_degrees(rad2deg(atan2(sin(h_prime_rad),
                cos(h_prime_rad)*sin(lat_rad) - tan(deg2rad(delta_prime))*cos(lat_rad))));
    }

    double topocentric_azimuth_angle(double azimuth_astro)
    {
        return limit_degrees(azimuth_astro + 180.0);
    }

    double surface_incidence_angle(double zenith, double azimuth_astro, double azm_rotation,
                                   double slope)
    {
        double zenith_rad = deg2rad(zenith);
        double slope_rad  = deg2rad(slope);

        return rad2deg(acos(cos(zenith_rad)*cos(slope_rad)  +
                sin(slope_rad )*sin(zenith_rad) * cos(deg2rad(azimuth_astro - azm_rotation))));
    }

    double sun_mean_longitude(double jme)
    {
        return limit_degrees(280.4664567 + jme*(360007.6982779 + jme*(0.03032028 +
                jme*(1/49931.0   + jme*(-1/15300.0     + jme*(-1/2000000.0))))));
    }

    double eot(double m, double alpha, double del_psi, double epsilon)
    {
        return limit_minutes(4.0*(m - 0.0057183 - alpha + del_psi*cos(deg2rad(epsilon))));
    }

    double approx_sun_transit_time(double alpha_zero, double longitude, double nu)
    {
        return (alpha_zero - longitude - nu) / 360.0;
    }

    double sun_hour_angle_at_rise_set(double latitude, double delta_zero, double h0_prime)
    {
        double h0             = -99999;
        double latitude_rad   = deg2rad(latitude);
        double delta_zero_rad = deg2rad(delta_zero);
        double argument       = (sin(deg2rad(h0_prime)) - sin(latitude_rad)*sin(delta_zero_rad)) /
                (cos(latitude_rad)*cos(delta_zero_rad));

        if (abs(argument) <= 1) h0 = limit_degrees180(rad2deg(acos(argument)));

        return h0;
    }

    void approx_sun_rise_and_set(double[] m_rts, double h0)
    {
        double h0_dfrac = h0/360.0;

        m_rts[Terms5.SUN_RISE.ordinal()]    = limit_zero2one(m_rts[Terms5.SUN_TRANSIT.ordinal()] - h0_dfrac);
        m_rts[Terms5.SUN_SET.ordinal()]     = limit_zero2one(m_rts[Terms5.SUN_TRANSIT.ordinal()] + h0_dfrac);
        m_rts[Terms5.SUN_TRANSIT.ordinal()] = limit_zero2one(m_rts[Terms5.SUN_TRANSIT.ordinal()]);
    }

    double rts_alpha_delta_prime(double []ad, double n)
    {
        double a = ad[Terms4.JD_ZERO.ordinal()] - ad[Terms4.JD_MINUS.ordinal()];
        double b = ad[Terms4.JD_PLUS.ordinal()] - ad[Terms4.JD_ZERO.ordinal()];

        if (abs(a) >= 2.0) a = limit_zero2one(a);
        if (abs(b) >= 2.0) b = limit_zero2one(b);

        return ad[Terms4.JD_ZERO.ordinal()] + n * (a + b + (b-a)*n)/2.0;
    }

    double rts_sun_altitude(double latitude, double delta_prime, double h_prime)
    {
        double latitude_rad    = deg2rad(latitude);
        double delta_prime_rad = deg2rad(delta_prime);

        return rad2deg(asin(sin(latitude_rad)*sin(delta_prime_rad) +
                cos(latitude_rad)*cos(delta_prime_rad)*cos(deg2rad(h_prime))));
    }

    double sun_rise_and_set(double []m_rts,   double []h_rts,   double []delta_prime, double latitude,
                            double []h_prime, double h0_prime, int sun)
    {
        return m_rts[sun] + (h_rts[sun] - h0_prime) /
                (360.0*cos(deg2rad(delta_prime[sun]))*cos(deg2rad(latitude))*sin(deg2rad(h_prime[sun])));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
// Calculate required SPA parameters to get the right ascension (alpha) and declination (delta)
// Note: JD must be already calculated and in structure
////////////////////////////////////////////////////////////////////////////////////////////////
    void calculate_geocentric_sun_right_ascension_and_declination(Spa_data sun_rts)
    {
        double x[] = new double[Terms2.TERM_X_COUNT.ordinal()];

        sun_rts.jc = julian_century(sun_rts.jd);

        sun_rts.jde = julian_ephemeris_day(sun_rts.jd, sun_rts.delta_t);
        sun_rts.jce = julian_ephemeris_century(sun_rts.jde);
        sun_rts.jme = julian_ephemeris_millennium(sun_rts.jce);

        sun_rts.l = earth_heliocentric_longitude(sun_rts.jme);
        sun_rts.b = earth_heliocentric_latitude(sun_rts.jme);
        sun_rts.r = earth_radius_vector(sun_rts.jme);

        sun_rts.theta = geocentric_longitude(sun_rts.l);
        sun_rts.beta  = geocentric_latitude(sun_rts.b);

        x[Terms2.TERM_X0.ordinal()] = sun_rts.x0 = mean_elongation_moon_sun(sun_rts.jce);
        x[Terms2.TERM_X1.ordinal()] = sun_rts.x1 = mean_anomaly_sun(sun_rts.jce);
        x[Terms2.TERM_X2.ordinal()] = sun_rts.x2 = mean_anomaly_moon(sun_rts.jce);
        x[Terms2.TERM_X3.ordinal()] = sun_rts.x3 = argument_latitude_moon(sun_rts.jce);
        x[Terms2.TERM_X4.ordinal()] = sun_rts.x4 = ascending_longitude_moon(sun_rts.jce);

        nutation_longitude_and_obliquity(sun_rts.jce, x);

        sun_rts.epsilon0 = ecliptic_mean_obliquity(sun_rts.jme);
        sun_rts.epsilon  = ecliptic_true_obliquity(sun_rts.del_epsilon, sun_rts.epsilon0);

        sun_rts.del_tau   = aberration_correction(sun_rts.r);
        sun_rts.lamda     = apparent_sun_longitude(sun_rts.theta, sun_rts.del_psi, sun_rts.del_tau);
        sun_rts.nu0       = greenwich_mean_sidereal_time (sun_rts.jd, sun_rts.jc);
        sun_rts.nu        = greenwich_sidereal_time (sun_rts.nu0, sun_rts.del_psi, sun_rts.epsilon);

        sun_rts.alpha = geocentric_right_ascension(sun_rts.lamda, sun_rts.epsilon, sun_rts.beta);
        sun_rts.delta = geocentric_declination(sun_rts.beta, sun_rts.epsilon, sun_rts.lamda);
    }

////////////////////////////////////////////////////////////////////////
// Calculate Equation of Time (EOT) and Sun Rise, Transit, & Set (RTS)
////////////////////////////////////////////////////////////////////////

    void calculate_eot_and_sun_rise_transit_set()
    {
        Spa_data sun_rts;
        double nu, m, h0, n;
        double[] alpha = new double[Terms4.JD_COUNT.ordinal()];
        double[] delta = new double[Terms4.JD_COUNT.ordinal()];
        double[] m_rts = new double[Terms5.SUN_COUNT.ordinal()];
        double[] nu_rts = new double[Terms5.SUN_COUNT.ordinal()];
        double[] h_rts = new double[Terms5.SUN_COUNT.ordinal()];
        double[] alpha_prime = new double[Terms5.SUN_COUNT.ordinal()];
        double[] delta_prime = new double[Terms5.SUN_COUNT.ordinal()];
        double[] h_prime = new double[Terms5.SUN_COUNT.ordinal()];
        double h0_prime = -1*(SUN_RADIUS + spa.atmos_refract);
        int i;

        sun_rts  = spa.clone();
        m        = sun_mean_longitude(spa.jme);
        spa.eot = eot(m, spa.alpha, spa.del_psi, spa.epsilon);

        sun_rts.hour = sun_rts.minute = 0;
        sun_rts.second = 0;
        sun_rts.delta_ut1 = sun_rts.timezone = 0.0;

        sun_rts.jd = julian_day (sun_rts.year,   sun_rts.month,  sun_rts.day,       sun_rts.hour,
                sun_rts.minute, sun_rts.second, sun_rts.delta_ut1, sun_rts.timezone);

        calculate_geocentric_sun_right_ascension_and_declination(sun_rts);
        nu = sun_rts.nu;

        sun_rts.delta_t = 0;
        sun_rts.jd--;
        for (i = 0; i < Terms4.JD_COUNT.ordinal(); i++) {
            calculate_geocentric_sun_right_ascension_and_declination(sun_rts);
            alpha[i] = sun_rts.alpha;
            delta[i] = sun_rts.delta;
            sun_rts.jd++;
        }

        m_rts[Terms5.SUN_TRANSIT.ordinal()] = approx_sun_transit_time(alpha[Terms4.JD_ZERO.ordinal()], spa.longitude, nu);
        h0 = sun_hour_angle_at_rise_set(spa.latitude, delta[Terms4.JD_ZERO.ordinal()], h0_prime);

        if (h0 >= 0) {

            approx_sun_rise_and_set(m_rts, h0);

            for (i = 0; i < Terms5.SUN_COUNT.ordinal(); i++) {

                nu_rts[i]      = nu + 360.985647*m_rts[i];

                n              = m_rts[i] + spa.delta_t/86400.0;
                alpha_prime[i] = rts_alpha_delta_prime(alpha, n);
                delta_prime[i] = rts_alpha_delta_prime(delta, n);

                h_prime[i]     = limit_degrees180pm(nu_rts[i] + spa.longitude - alpha_prime[i]);

                h_rts[i]       = rts_sun_altitude(spa.latitude, delta_prime[i], h_prime[i]);
            }

            spa.srha = h_prime[Terms5.SUN_RISE.ordinal()];
            spa.ssha = h_prime[Terms5.SUN_RISE.ordinal()];
            spa.sta  = h_rts[Terms5.SUN_TRANSIT.ordinal()];

            spa.suntransit = dayfrac_to_local_hr(m_rts[Terms5.SUN_TRANSIT.ordinal()] - h_prime[Terms5.SUN_TRANSIT.ordinal()] / 360.0,
                    spa.timezone);

            spa.sunrise = dayfrac_to_local_hr(sun_rise_and_set(m_rts, h_rts, delta_prime,
                    spa.latitude, h_prime, h0_prime, Terms5.SUN_RISE.ordinal()), spa.timezone);

            spa.sunset  = dayfrac_to_local_hr(sun_rise_and_set(m_rts, h_rts, delta_prime,
                    spa.latitude, h_prime, h0_prime, Terms5.SUN_SET.ordinal()),  spa.timezone);

        } else spa.srha= spa.ssha= spa.sta= spa.suntransit= spa.sunrise= spa.sunset= -99999;

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
// Calculate all SPA parameters and put into structure
// Note: All inputs values (listed in header file) must already be in structure
///////////////////////////////////////////////////////////////////////////////////////////
    int spa_calculate()
    {
        int result;

        result = validate_inputs();

        if (result == 0)
        {
            spa.jd = julian_day (spa.year,   spa.month,  spa.day,       spa.hour,
                    spa.minute, spa.second, spa.delta_ut1, spa.timezone);

            calculate_geocentric_sun_right_ascension_and_declination(spa);

            spa.h  = observer_hour_angle(spa.nu, spa.longitude, spa.alpha);
            spa.xi = sun_equatorial_horizontal_parallax(spa.r);

            right_ascension_parallax_and_topocentric_dec(spa.latitude, spa.elevation, spa.xi,
                    spa.h, spa.delta);

            spa.alpha_prime = topocentric_right_ascension(spa.alpha, spa.del_alpha);
            spa.h_prime     = topocentric_local_hour_angle(spa.h, spa.del_alpha);

            spa.e0      = topocentric_elevation_angle(spa.latitude, spa.delta_prime, spa.h_prime);
            spa.del_e   = atmospheric_refraction_correction(spa.pressure, spa.temperature,
                    spa.atmos_refract, spa.e0);
            spa.e       = topocentric_elevation_angle_corrected(spa.e0, spa.del_e);

            spa.zenith        = topocentric_zenith_angle(spa.e);
            spa.azimuth_astro = topocentric_azimuth_angle_astro(spa.h_prime, spa.latitude,
                    spa.delta_prime);
            spa.azimuth       = topocentric_azimuth_angle(spa.azimuth_astro);

            if ((spa.function == SpaOptions.SPA_ZA_INC) || (spa.function == SpaOptions.SPA_ALL))
                spa.incidence  = surface_incidence_angle(spa.zenith, spa.azimuth_astro,
                        spa.azm_rotation, spa.slope);

            if ((spa.function == SpaOptions.SPA_ZA_RTS) || (spa.function == SpaOptions.SPA_ALL))
                calculate_eot_and_sun_rise_transit_set();
        }

        return result;

    }

    int convertToAzDec()
    {

            spa.h  = observer_hour_angle(spa.nu, spa.longitude, spa.alpha);
            spa.xi = sun_equatorial_horizontal_parallax(spa.r);

            right_ascension_parallax_and_topocentric_dec(spa.latitude, spa.elevation, spa.xi,
                    spa.h, spa.delta);

            spa.alpha_prime = topocentric_right_ascension(spa.alpha, spa.del_alpha);
            spa.h_prime     = topocentric_local_hour_angle(spa.h, spa.del_alpha);

            spa.e0      = topocentric_elevation_angle(spa.latitude, spa.delta_prime, spa.h_prime);
            spa.del_e   = atmospheric_refraction_correction(spa.pressure, spa.temperature,
                    spa.atmos_refract, spa.e0);
            spa.e       = topocentric_elevation_angle_corrected(spa.e0, spa.del_e);

            spa.zenith        = topocentric_zenith_angle(spa.e);
            spa.azimuth_astro = topocentric_azimuth_angle_astro(spa.h_prime, spa.latitude,
                    spa.delta_prime);
            spa.azimuth       = topocentric_azimuth_angle(spa.azimuth_astro);

        return 0;

    }


    //-------------------------------------------------------------------------------SAMPA---------------------------------------------------------------------------------

    int COUNT = 60;

    enum TermsS {TERM_D, TERM_M, TERM_MPR, TERM_F, TERM_LB, TERM_R, TERM_COUNT};

///////////////////////////////////////////////////////
///  Moon's Periodic Terms for Longitude and Distance
///////////////////////////////////////////////////////
double[][] ML_TERMS =
    {
        {0,0,1,0,6288774,-20905355},
        {2,0,-1,0,1274027,-3699111},
        {2,0,0,0,658314,-2955968},
        {0,0,2,0,213618,-569925},
        {0,1,0,0,-185116,48888},
        {0,0,0,2,-114332,-3149},
        {2,0,-2,0,58793,246158},
        {2,-1,-1,0,57066,-152138},
        {2,0,1,0,53322,-170733},
        {2,-1,0,0,45758,-204586},
        {0,1,-1,0,-40923,-129620},
        {1,0,0,0,-34720,108743},
        {0,1,1,0,-30383,104755},
        {2,0,0,-2,15327,10321},
        {0,0,1,2,-12528,0},
        {0,0,1,-2,10980,79661},
        {4,0,-1,0,10675,-34782},
        {0,0,3,0,10034,-23210},
        {4,0,-2,0,8548,-21636},
        {2,1,-1,0,-7888,24208},
        {2,1,0,0,-6766,30824},
        {1,0,-1,0,-5163,-8379},
        {1,1,0,0,4987,-16675},
        {2,-1,1,0,4036,-12831},
        {2,0,2,0,3994,-10445},
        {4,0,0,0,3861,-11650},
        {2,0,-3,0,3665,14403},
        {0,1,-2,0,-2689,-7003},
        {2,0,-1,2,-2602,0},
        {2,-1,-2,0,2390,10056},
        {1,0,1,0,-2348,6322},
        {2,-2,0,0,2236,-9884},
        {0,1,2,0,-2120,5751},
        {0,2,0,0,-2069,0},
        {2,-2,-1,0,2048,-4950},
        {2,0,1,-2,-1773,4130},
        {2,0,0,2,-1595,0},
        {4,-1,-1,0,1215,-3958},
        {0,0,2,2,-1110,0},
        {3,0,-1,0,-892,3258},
        {2,1,1,0,-810,2616},
        {4,-1,-2,0,759,-1897},
        {0,2,-1,0,-713,-2117},
        {2,2,-1,0,-700,2354},
        {2,1,-2,0,691,0},
        {2,-1,0,-2,596,0},
        {4,0,1,0,549,-1423},
        {0,0,4,0,537,-1117},
        {4,-1,0,0,520,-1571},
        {1,0,-2,0,-487,-1739},
        {2,1,0,-2,-399,0},
        {0,0,2,-2,-381,-4421},
        {1,1,1,0,351,0},
        {3,0,-2,0,-340,0},
        {4,0,-3,0,330,0},
        {2,-1,2,0,327,0},
        {0,2,1,0,-323,1165},
        {1,1,-1,0,299,0},
        {2,0,3,0,294,0},
        {2,0,-1,-2,0,8752}
    };
///////////////////////////////////////////////////////
///  Moon's Periodic Terms for Latitude
///////////////////////////////////////////////////////
double[][] MB_TERMS =
    {
        {0,0,0,1,5128122,0},
        {0,0,1,1,280602,0},
        {0,0,1,-1,277693,0},
        {2,0,0,-1,173237,0},
        {2,0,-1,1,55413,0},
        {2,0,-1,-1,46271,0},
        {2,0,0,1,32573,0},
        {0,0,2,1,17198,0},
        {2,0,1,-1,9266,0},
        {0,0,2,-1,8822,0},
        {2,-1,0,-1,8216,0},
        {2,0,-2,-1,4324,0},
        {2,0,1,1,4200,0},
        {2,1,0,-1,-3359,0},
        {2,-1,-1,1,2463,0},
        {2,-1,0,1,2211,0},
        {2,-1,-1,-1,2065,0},
        {0,1,-1,-1,-1870,0},
        {4,0,-1,-1,1828,0},
        {0,1,0,1,-1794,0},
        {0,0,0,3,-1749,0},
        {0,1,-1,1,-1565,0},
        {1,0,0,1,-1491,0},
        {0,1,1,1,-1475,0},
        {0,1,1,-1,-1410,0},
        {0,1,0,-1,-1344,0},
        {1,0,0,-1,-1335,0},
        {0,0,3,1,1107,0},
        {4,0,0,-1,1021,0},
        {4,0,-1,1,833,0},
        {0,0,1,-3,777,0},
        {4,0,-2,1,671,0},
        {2,0,0,-3,607,0},
        {2,0,2,-1,596,0},
        {2,-1,1,-1,491,0},
        {2,0,-2,1,-451,0},
        {0,0,3,-1,439,0},
        {2,0,2,1,422,0},
        {2,0,-3,-1,421,0},
        {2,1,-1,1,-366,0},
        {2,1,0,1,-351,0},
        {4,0,0,1,331,0},
        {2,-1,1,1,315,0},
        {2,-2,0,-1,302,0},
        {0,0,1,3,-283,0},
        {2,1,1,-1,-229,0},
        {1,1,0,-1,223,0},
        {1,1,0,1,223,0},
        {0,1,-2,-1,-220,0},
        {2,1,-1,-1,-220,0},
        {1,0,1,1,-185,0},
        {2,-1,-2,-1,181,0},
        {0,1,2,1,-177,0},
        {4,0,-2,-1,176,0},
        {4,-1,-1,-1,166,0},
        {1,0,1,-1,-164,0},
        {4,0,1,-1,132,0},
        {1,0,-1,-1,-119,0},
        {4,-1,0,-1,115,0},
        {2,-2,0,1,107,0}
    };
///////////////////////////////////////////////////////////////////////////////////////////////

    double fourth_order_polynomial(double a, double b, double c, double d, double e, double x)
    {
        return (((a*x + b)*x + c)*x + d)*x + e;
    }

    double moon_mean_longitude(double jce)
    {
        return limit_degrees(fourth_order_polynomial(
                -1.0/65194000, 1.0/538841, -0.0015786, 481267.88123421, 218.3164477, jce));
    }

    double moon_mean_elongation(double jce)
    {
        return limit_degrees(fourth_order_polynomial(
                -1.0/113065000, 1.0/545868, -0.0018819, 445267.1114034, 297.8501921, jce));
    }

    double sun_mean_anomaly(double jce)
    {
        return limit_degrees(third_order_polynomial(
                1.0/24490000, -0.0001536, 35999.0502909, 357.5291092, jce));
    }

    double moon_mean_anomaly(double jce)
    {
        return limit_degrees(fourth_order_polynomial(
                -1.0/14712000, 1.0/69699, 0.0087414, 477198.8675055, 134.9633964, jce));
    }

    double moon_latitude_argument(double jce)
    {
        return limit_degrees(fourth_order_polynomial(
                1.0/863310000, -1.0/3526000, -0.0036539, 483202.0175233, 93.2720950, jce));
    }

    void moon_periodic_term_summation_l_r(double d, double m, double m_prime, double f, double jce,
								  double terms[][])
    {
        int i;
        double e_mult, trig_arg;
        double e  = 1.0 - jce*(0.002516 + jce*0.0000074);

        mpa.l=0;
        mpa.r=0;
        for (i = 0; i < COUNT; i++)
        {
            e_mult   = pow(e, abs(terms[i][TermsS.TERM_M.ordinal()]));
            trig_arg = deg2rad(terms[i][TermsS.TERM_D.ordinal()]*d + terms[i][TermsS.TERM_M.ordinal()]  *m +
                    terms[i][TermsS.TERM_F.ordinal()]*f + terms[i][TermsS.TERM_MPR.ordinal()]*m_prime);
            mpa.l += e_mult * terms[i][TermsS.TERM_LB.ordinal()] *sin(trig_arg);
            mpa.r += e_mult * terms[i][TermsS.TERM_R.ordinal()]  *cos(trig_arg);
        }
    }

    void moon_periodic_term_summation_b(double d, double m, double m_prime, double f, double jce,
                                        double[][] terms)
    {
        int i;
        double e_mult, trig_arg;
        double e  = 1.0 - jce*(0.002516 + jce*0.0000074);

        mpa.b=0;
        for (i = 0; i < COUNT; i++)
        {
            e_mult   = pow(e, abs(terms[i][TermsS.TERM_M.ordinal()]));
            trig_arg = deg2rad(terms[i][TermsS.TERM_D.ordinal()]*d + terms[i][TermsS.TERM_M.ordinal()]  *m +
                    terms[i][TermsS.TERM_F.ordinal()]*f + terms[i][TermsS.TERM_MPR.ordinal()]*m_prime);
            mpa.b += e_mult * terms[i][TermsS.TERM_LB.ordinal()] *sin(trig_arg);
        }
    }

    void moon_longitude_and_latitude(double jce, double l_prime, double f, double m_prime, double l, double b)
    {
        double a1 = 119.75 +    131.849*jce;
        double a2 =  53.09 + 479264.290*jce;
        double a3 = 313.45 + 481266.484*jce;
        double delta_l =  3958*sin(deg2rad(a1))      + 318*sin(deg2rad(a2))   + 1962*sin(deg2rad(l_prime-f));
        double delta_b = -2235*sin(deg2rad(l_prime)) + 175*sin(deg2rad(a1-f)) +  127*sin(deg2rad(l_prime-m_prime))
                + 382*sin(deg2rad(a3))      + 175*sin(deg2rad(a1+f)) -  115*sin(deg2rad(l_prime+m_prime));

	mpa.lamda_prime = limit_degrees(l_prime + (l + delta_l)/1000000);
	mpa.beta        = limit_degrees(          (b + delta_b)/1000000);
    }

    double moon_earth_distance(double r)
    {
        return 385000.56 + r/1000;
    }

    double moon_equatorial_horiz_parallax(double delta)
    {
        return rad2deg(asin(6378.14/delta));
    }

    double apparent_moon_longitude(double lamda_prime, double del_psi)
    {
        return lamda_prime + del_psi;
    }

    double angular_distance_sun_moon(double zen_sun, double azm_sun, double zen_moon, double azm_moon)
    {
        double zs = deg2rad(zen_sun);
        double zm = deg2rad(zen_moon);

        return rad2deg(acos(cos(zs)*cos(zm) + sin(zs)*sin(zm)*cos(deg2rad(azm_sun - azm_moon))));
    }

    double sun_disk_radius(double r)
    {
        return 959.63/(3600.0 * r);
    }

    double moon_disk_radius(double e, double pi, double cap_delta)
    {
        return 358473400*(1 + sin(deg2rad(e))*sin(deg2rad(pi)))/(3600.0 * cap_delta);
    }

    void sul_area(double ems, double rs, double rm)
    {
        double ems2 = ems*ems;
        double rs2  = rs*rs;
        double rm2  = rm*rm;
        double snum, ai, m, s, h;

        if (ems < (rs + rm))
        {
            if (ems <= abs(rs - rm))
                ai = PI*rm2;
            else {
                snum =  ems2 + rs2 - rm2;
                m    = (ems2 - rs2 + rm2) / (2*ems);
                s    =              snum  / (2*ems);
                h    = sqrt(4*ems2*rs2 - snum*snum) / (2*ems);
                ai   = (rs2*acos(s/rs) - h * s + rm2*acos(m/rm) - h * m);
            }
        } else ai = 0;

	sampa.a_sul = PI*rs2 - ai;
        if (sampa.a_sul < 0) sampa.a_sul = 0;
	sampa.a_sul_pct = sampa.a_sul * 100.0 / (PI*rs2);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
// Calculate all MPA parameters and put into structure
// Note: All inputs values (listed in SPA header file) must already be in structure
///////////////////////////////////////////////////////////////////////////////////////////
    void mpa_calculate()
    {
        mpa.l_prime = moon_mean_longitude(spa.jce);
        mpa.d       = moon_mean_elongation(spa.jce);
        mpa.m       = sun_mean_anomaly(spa.jce);
        mpa.m_prime = moon_mean_anomaly(spa.jce);
        mpa.f       = moon_latitude_argument(spa.jce);

        moon_periodic_term_summation_l_r(mpa.d, mpa.m, mpa.m_prime, mpa.f, spa.jce, ML_TERMS);
        moon_periodic_term_summation_b(mpa.d, mpa.m, mpa.m_prime, mpa.f, spa.jce, MB_TERMS);

        moon_longitude_and_latitude(spa.jce, mpa.l_prime, mpa.f, mpa.m_prime, mpa.l, mpa.b);

        mpa.cap_delta = moon_earth_distance(mpa.r);
        mpa.pi = moon_equatorial_horiz_parallax(mpa.cap_delta);

        mpa.lamda    = apparent_moon_longitude(mpa.lamda_prime, spa.del_psi);

        mpa.alpha = geocentric_right_ascension(mpa.lamda, spa.epsilon, mpa.beta);
        mpa.delta = geocentric_declination(mpa.beta, spa.epsilon, mpa.lamda);

        mpa.h  = observer_hour_angle(spa.nu, spa.longitude, mpa.alpha);

        right_ascension_parallax_and_topocentric_dec(spa.latitude, spa.elevation, mpa.pi,
                mpa.h, mpa.delta);
        mpa.alpha_prime = topocentric_right_ascension(mpa.alpha, mpa.del_alpha);
        mpa.h_prime     = topocentric_local_hour_angle(mpa.h, mpa.del_alpha);

        mpa.e0      = topocentric_elevation_angle(spa.latitude, mpa.delta_prime, mpa.h_prime);
        mpa.del_e   = atmospheric_refraction_correction(spa.pressure, spa.temperature,
                spa.atmos_refract, mpa.e0);
        mpa.e       = topocentric_elevation_angle_corrected(mpa.e0, mpa.del_e);

        mpa.zenith        = topocentric_zenith_angle(mpa.e);
        mpa.azimuth_astro = topocentric_azimuth_angle_astro(mpa.h_prime, spa.latitude, mpa.delta_prime);
        mpa.azimuth       = topocentric_azimuth_angle(mpa.azimuth_astro);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
// Calculate all SAMPA parameters and put into structure
// Note: All inputs values (listed in SPA header file) must already be in structure
///////////////////////////////////////////////////////////////////////////////////////////
    public int sampa_calculate()
    {
        int result;

        spa.function = SpaOptions.SPA_ZA;
        result = spa_calculate();

        if (result == 0)
        {
            mpa_calculate();

            sampa.ems = angular_distance_sun_moon(spa.zenith, spa.azimuth,
                    mpa.zenith, mpa.azimuth);
            sampa.rs  = sun_disk_radius(spa.r);
            sampa.rm  = moon_disk_radius(mpa.e, mpa.pi, mpa.cap_delta);

            sul_area(sampa.ems, sampa.rs, sampa.rm);

        }

        return result;
    }


}
