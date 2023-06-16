#version 450


//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//------------------------------------------------------------------------------SPA--------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

//enumeration for function codes to select desired final outputs from SPA

#define SPA_ZA 0          //calculate zenith and azimuth
#define SPA_ZA_INC 1       //calculate zenith, azimuth, and incidence
#define SPA_ZA_RTS 2       //calculate zenith, azimuth, and sun rise/transit/set values
#define SPA_ALL 3          //calculate all SPA output values


int spa_year;            // 4-digit year,      valid range: -2000 to 6000, error code: 1
int spa_month;           // 2-digit month,         valid range: 1 to  12,  error code: 2
int spa_day;             // 2-digit day,           valid range: 1 to  31,  error code: 3
int spa_hour;            // Observer local hour,   valid range: 0 to  24,  error code: 4
int spa_minute;          // Observer local minute, valid range: 0 to  59,  error code: 5
double spa_second;       // Observer local second, valid range: 0 to <60,  error code: 6
double spa_delta_ut1;    // Fractional second difference between UTC and UT which is used
                         // to adjust UTC for earth's irregular rotation rate and is derived
                         // from observation only and is reported in this bulletin:
                         // http://maia.usno.navy.mil/ser7/ser7.dat,
                         // where delta_ut1 = DUT1
                         // valid range: -1 to 1 second (exclusive), error code 17

double spa_delta_t;      // Difference between earth rotation time and terrestrial time
                     // It is derived from observation only and is reported in this
                     // bulletin: http://maia.usno.navy.mil/ser7/ser7.dat,
                     // where delta_t = 32.184 + (TAI-UTC) - DUT1
                     // valid range: -8000 to 8000 seconds, error code: 7

double spa_timezone;     // Observer time zone (negative west of Greenwich)
                     // valid range: -18   to   18 hours,   error code: 8

double spa_longitude;    // Observer longitude (negative west of Greenwich)
                     // valid range: -180  to  180 degrees, error code: 9

double spa_latitude;     // Observer latitude (negative south of equator)
                     // valid range: -90   to   90 degrees, error code: 10

double spa_elevation;    // Observer elevation [meters]
                     // valid range: -6500000 or higher meters,    error code: 11

double spa_pressure;     // Annual average local pressure [millibars]
                     // valid range:    0 to 5000 millibars,       error code: 12

double spa_temperature;  // Annual average local temperature [degrees Celsius]
                     // valid range: -273 to 6000 degrees Celsius, error code; 13

double spa_slope;        // Surface slope (measured from the horizontal plane)
                     // valid range: -360 to 360 degrees, error code: 14

double spa_azm_rotation; // Surface azimuth rotation (measured from south to projection of
                     //     surface normal on horizontal plane, negative east)cos
                     // valid range: -360 to 360 degrees, error \ode: 15

double spa_atmos_refract;// Atmospheric refraction at sunrise and sunset (0.5667 deg is typical)
                     // valid range: -5   to   5 degrees, error code: 16

int spa_function;        // Switch to choose functions for desired output (from enumeration)

double spa_jd;          //Julian day
double spa_jc;          //Julian century

double spa_jde;         //Julian ephemeris day
double spa_jce;         //Julian ephemeris century
double spa_jme;         //Julian ephemeris millennium

double spa_l;           //earth heliocentric longitude [degrees]
double spa_b;           //earth heliocentric latitude [degrees]
double spa_r;           //earth radius vector [Astronomical Units, AU]

double spa_theta;       //geocentric longitude [degrees]
double spa_beta;        //geocentric latitude [degrees]

double spa_x0;          //mean elongation (moon-sun) [degrees]
double spa_x1;          //mean anomaly (sun) [degrees]
double spa_x2;          //mean anomaly (moon) [degrees]
double spa_x3;          //argument latitude (moon) [degrees]
double spa_x4;          //ascending longitude (moon) [degrees]

double spa_del_psi;     //nutation longitude [degrees]
double spa_del_epsilon; //nutation obliquity [degrees]
double spa_epsilon0;    //ecliptic mean obliquity [arc seconds]
double spa_epsilon;     //ecliptic true obliquity  [degrees]

double spa_del_tau;     //aberration correction [degrees]
double spa_lamda;       //apparent sun longitude [degrees]
double spa_nu0;         //Greenwich mean sidereal time [degrees]
double spa_nu;          //Greenwich sidereal time [degrees]

double spa_alpha;       //geocentric sun right ascension [degrees]
double spa_delta;       //geocentric sun declination [degrees]

double spa_h;           //observer hour angle [degrees]
double spa_xi;          //sun equatorial horizontal parallax [degrees]
double spa_del_alpha;   //sun right ascension parallax [degrees]
double spa_delta_prime; //topocentric sun declination [degrees]
double spa_alpha_prime; //topocentric sun right ascension [degrees]
double spa_h_prime;     //topocentric local hour angle [degrees]

double spa_e0;          //topocentric elevation angle (uncorrected) [degrees]
double spa_del_e;       //atmospheric refraction correction [degrees]
double spa_e;           //topocentric elevation angle (corrected) [degrees]

double spa_eot;         //equation of time [minutes]
double spa_srha;        //sunrise hour angle [degrees]
double spa_ssha;        //sunset hour angle [degrees]
double spa_sta;         //sun transit altitude [degrees]

double spa_zenith;       //topocentric zenith angle [degrees]
double spa_azimuth_astro;//topocentric azimuth angle (westward from south) [for astronomers]
double spa_azimuth;      //topocentric azimuth angle (eastward from north) [for navigators and solar radiation]
double spa_incidence;    //surface incidence angle [degrees]

double spa_suntransit;   //local sun transit time (or solar noon) [fractional hour]
double spa_sunrise;      //local sunrise time (+/- 30 seconds) [fractional hour]
double spa_sunset;       //local sunset time (+/- 30 seconds) [fractional hour]



int rts_year;            // 4-digit year,      valid range: -2000 to 6000, error code: 1
int rts_month;           // 2-digit month,         valid range: 1 to  12,  error code: 2
int rts_day;             // 2-digit day,           valid range: 1 to  31,  error code: 3
int rts_hour;            // Observer local hour,   valid range: 0 to  24,  error code: 4
int rts_minute;          // Observer local minute, valid range: 0 to  59,  error code: 5
double rts_second;       // Observer local second, valid range: 0 to <60,  error code: 6
double rts_delta_ut1;    // Fractional second difference between UTC and UT which is used
                         // to adjust UTC for earth's irregular rotation rate and is derived
                         // from observation only and is reported in this bulletin:
                         // http://maia.usno.navy.mil/ser7/ser7.dat,
                         // where delta_ut1 = DUT1
                         // valid range: -1 to 1 second (exclusive), error code 17

double rts_delta_t;      // Difference between earth rotation time and terrestrial time
                     // It is derived from observation only and is reported in this
                     // bulletin: http://maia.usno.navy.mil/ser7/ser7.dat,
                     // where delta_t = 32.184 + (TAI-UTC) - DUT1
                     // valid range: -8000 to 8000 seconds, error code: 7

double rts_timezone;     // Observer time zone (negative west of Greenwich)
                     // valid range: -18   to   18 hours,   error code: 8

double rts_longitude;    // Observer longitude (negative west of Greenwich)
                     // valid range: -180  to  180 degrees, error code: 9

double rts_latitude;     // Observer latitude (negative south of equator)
                     // valid range: -90   to   90 degrees, error code: 10

double rts_elevation;    // Observer elevation [meters]
                     // valid range: -6500000 or higher meters,    error code: 11

double rts_pressure;     // Annual average local pressure [millibars]
                     // valid range:    0 to 5000 millibars,       error code: 12

double rts_temperature;  // Annual average local temperature [degrees Celsius]
                     // valid range: -273 to 6000 degrees Celsius, error code; 13

double rts_slope;        // Surface slope (measured from the horizontal plane)
                     // valid range: -360 to 360 degrees, error code: 14

double rts_azm_rotation; // Surface azimuth rotation (measured from south to projection of
                     //     surface normal on horizontal plane, negative east)
                     // valid range: -360 to 360 degrees, error code: 15

double rts_atmos_refract;// Atmospheric refraction at sunrise and sunset (0.5667 deg is typical)
                     // valid range: -5   to   5 degrees, error code: 16

int rts_function;        // Switch to choose functions for desired output (from enumeration)

double rts_jd;          //Julian day
double rts_jc;          //Julian century

double rts_jde;         //Julian ephemeris day
double rts_jce;         //Julian ephemeris century
double rts_jme;         //Julian ephemeris millennium

double rts_l;           //earth heliocentric longitude [degrees]
double rts_b;           //earth heliocentric latitude [degrees]
double rts_r;           //earth radius vector [Astronomical Units, AU]

double rts_theta;       //geocentric longitude [degrees]
double rts_beta;        //geocentric latitude [degrees]

double rts_x0;          //mean elongation (moon-sun) [degrees]
double rts_x1;          //mean anomaly (sun) [degrees]
double rts_x2;          //mean anomaly (moon) [degrees]
double rts_x3;          //argument latitude (moon) [degrees]
double rts_x4;          //ascending longitude (moon) [degrees]

double rts_del_psi;     //nutation longitude [degrees]
double rts_del_epsilon; //nutation obliquity [degrees]
double rts_epsilon0;    //ecliptic mean obliquity [arc seconds]
double rts_epsilon;     //ecliptic true obliquity  [degrees]

double rts_del_tau;     //aberration correction [degrees]
double rts_lamda;       //apparent sun longitude [degrees]
double rts_nu0;         //Greenwich mean sidereal time [degrees]
double rts_nu;          //Greenwich sidereal time [degrees]

double rts_alpha;       //geocentric sun right ascension [degrees]
double rts_delta;       //geocentric sun declination [degrees]

double rts_h;           //observer hour angle [degrees]
double rts_xi;          //sun equatorial horizontal parallax [degrees]
double rts_del_alpha;   //sun right ascension parallax [degrees]
double rts_delta_prime; //topocentric sun declination [degrees]
double rts_alpha_prime; //topocentric sun right ascension [degrees]
double rts_h_prime;     //topocentric local hour angle [degrees]

double rts_e0;          //topocentric elevation angle (uncorrected) [degrees]
double rts_del_e;       //atmospheric refraction correction [degrees]
double rts_e;           //topocentric elevation angle (corrected) [degrees]

double rts_eot;         //equation of time [minutes]
double rts_srha;        //sunrise hour angle [degrees]
double rts_ssha;        //sunset hour angle [degrees]
double rts_sta;         //sun transit altitude [degrees]

double rts_zenith;       //topocentric zenith angle [degrees]
double rts_azimuth_astro;//topocentric azimuth angle (westward from south) [for astronomers]
double rts_azimuth;      //topocentric azimuth angle (eastward from north) [for navigators and solar radiation]
double rts_incidence;    //surface incidence angle [degrees]

double rts_suntransit;   //local sun transit time (or solar noon) [fractional hour]
double rts_sunrise;      //local sunrise time (+/- 30 seconds) [fractional hour]
double rts_sunset;       //local sunset time (+/- 30 seconds) [fractional hour]




void CopySpaToRts() {
    rts_year = spa_year;
    rts_month = spa_month;
    rts_day = spa_day;
    rts_hour = spa_hour;
    rts_minute = spa_minute;
    rts_second = spa_second;
    rts_delta_ut1 = spa_delta_ut1;
    rts_delta_t = spa_delta_t;
    rts_timezone = spa_timezone;
    rts_longitude = spa_longitude;
    rts_latitude = spa_latitude;
    rts_elevation = spa_elevation;
    rts_pressure = spa_pressure;
    rts_temperature = spa_temperature;
    rts_slope = spa_slope;
    rts_azm_rotation = spa_azm_rotation;
    rts_atmos_refract = spa_atmos_refract;
    rts_function = spa_function;
    rts_jd = spa_jd;          
    rts_jc = spa_jc;          
    rts_jde = spa_jde;        
    rts_jce = spa_jce;        
    rts_jme = spa_jme;         
    rts_l = spa_l;           
    rts_b = spa_b;
    rts_r = spa_r;
    rts_theta = spa_theta;
    rts_beta = spa_beta;
    rts_x0 =spa_x0;
    rts_x1 = spa_x1;
    rts_x2 = spa_x2;
    rts_x3 = spa_x3;
    rts_x4 = spa_x4;
    rts_del_psi = spa_del_psi;
    rts_del_epsilon = spa_del_epsilon;
    rts_epsilon0 = spa_epsilon0;
    rts_epsilon = spa_epsilon;
    rts_del_tau = spa_del_tau;
    rts_lamda = spa_lamda;
    rts_nu0 = spa_nu0;
    rts_nu = spa_nu;
    rts_alpha = spa_alpha;
    rts_delta = spa_delta;
    rts_h = spa_h;
    rts_xi = spa_xi;
    rts_del_alpha = spa_del_alpha;
    rts_delta_prime = spa_delta_prime;
    rts_alpha_prime = spa_alpha_prime;
    rts_h_prime = spa_h_prime;
    rts_e0 = spa_e0;
    rts_del_e = spa_del_e;
    rts_e = spa_e;
    rts_eot = spa_eot;
    rts_srha = spa_srha;
    rts_ssha = spa_ssha;
    rts_sta = spa_sta;
    rts_zenith = spa_zenith;
    rts_azimuth_astro = spa_azimuth_astro;
    rts_azimuth = spa_azimuth;
    rts_incidence = spa_incidence;
    rts_suntransit = spa_suntransit;
    rts_sunrise = spa_sunrise;
    rts_sunset = spa_sunset;

}


#define PI         3.1415926535897932384626433832795028841971
#define SUN_RADIUS 0.26667

#define L_COUNT 6
#define B_COUNT 2
#define R_COUNT 5
#define Y_COUNT 63

#define L_MAX_SUBCOUNT 64
#define B_MAX_SUBCOUNT 5
#define R_MAX_SUBCOUNT 40

#define TERM_A 0
#define TERM_B 1
#define TERM_C 2
#define TERM_COUNT_SPA 3

#define TERM_X0 0
#define TERM_X1 1
#define TERM_X2 2
#define TERM_X3 3
#define TERM_X4 4
#define TERM_X_COUNT 5

#define TERM_PSI_A 0
#define TERM_PSI_B 1
#define TERM_EPS_C 2
#define TERM_EPS_D 3
#define TERM_PE_COUNT 4

#define JD_MINUS 0
#define JD_ZERO 1
#define JD_PLUS 2
#define JD_COUNT 3

#define SUN_TRANSIT 0
#define SUN_RISE 1
#define SUN_SET 2
#define SUN_COUNT 3

#define TERM_Y_COUNT TERM_X_COUNT

const int l_subcount[L_COUNT] = { 64,34,20,7,3,1 };
const int b_subcount[B_COUNT] = { 5,2 };
const int r_subcount[R_COUNT] = { 40,10,6,2,1 };

///////////////////////////////////////////////////
///  Earth Periodic Terms
///////////////////////////////////////////////////
const double L_TERMS[L_COUNT][L_MAX_SUBCOUNT][TERM_COUNT_SPA] =
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
        {6,4.67,4690.48},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
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
        {2,3.75,0.98},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    },
    {
        {289.0,5.844,6283.076},
        {35,0,0},
        {17,5.49,12566.15},
        {3,5.2,155.42},
        {1,4.72,3.52},
        {1,5.3,18849.23},
        {1,5.97,242.73},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    },
    {
        {114.0,3.142,0},
        {8,4.13,6283.08},
        {1,3.84,12566.15},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    },
    {
        {1,3.14,0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    }
};

const double B_TERMS[B_COUNT][B_MAX_SUBCOUNT][TERM_COUNT_SPA] =
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
        {6,1.73,5223.69},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    }
};

const double R_TERMS[R_COUNT][R_MAX_SUBCOUNT][TERM_COUNT_SPA] =
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
        {9,0.27,5486.78},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    },
    {
        {4359.0,5.7846,6283.0758},
        {124.0,5.579,12566.152},
        {12,3.14,0},
        {9,3.63,77713.77},
        {6,1.87,5573.14},
        {3,5.47,18849.23},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    },
    {
        {145.0,4.273,6283.076},
        {7,3.92,12566.15},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    },
    {
        {4,2.56,6283.08},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    }
};

////////////////////////////////////////////////////////////////
///  Periodic Terms for the nutation in longitude and obliquity
////////////////////////////////////////////////////////////////

const int Y_TERMS[Y_COUNT][TERM_Y_COUNT] =
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

const double PE_TERMS[Y_COUNT][TERM_PE_COUNT] = {
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



//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//------------------------------------------------------------------------------MPA--------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------



#define SAMPA_NO_IRR 0  //calculate all values except estimated solar irradiances
#define SAMPA_ALL 1     //calculate all values



double mpa_l_prime;		//moon mean longitude [degrees]
double mpa_d;			//moon mean elongation [degrees]
double mpa_m;			//sun mean anomaly [degrees]
double mpa_m_prime;		//moon mean anomaly [degrees]
double mpa_f;           //moon argument of latitude [degrees]
double mpa_l;			//term l
double mpa_r;			//term r
double mpa_b;			//term b
double mpa_lamda_prime; //moon longitude [degrees]
double mpa_beta;		//moon latitude [degrees]
double mpa_cap_delta;   //distance from earth to moon [kilometers]
double mpa_pi;          //moon equatorial horizontal parallax [degrees] 
double mpa_lamda;       //apparent moon longitude [degrees]

double mpa_alpha;       //geocentric moon right ascension [degrees]
double mpa_delta;       //geocentric moon declination [degrees]

double mpa_h;           //observer hour angle [degrees]
double mpa_del_alpha;   //moon right ascension parallax [degrees]
double mpa_delta_prime; //topocentric moon declination [degrees]
double mpa_alpha_prime; //topocentric moon right ascension [degrees]
double mpa_h_prime;     //topocentric local hour angle [degrees]

double mpa_e0;          //topocentric elevation angle (uncorrected) [degrees]
double mpa_del_e;       //atmospheric refraction correction [degrees]
double mpa_e;           //topocentric elevation angle (corrected) [degrees]

//---------------------Final MPA OUTPUT VALUES------------------------

double mpa_zenith;        //topocentric zenith angle [degrees]
double mpa_azimuth_astro; //topocentric azimuth angle (westward from south) [for astronomers]
double mpa_azimuth;       //topocentric azimuth angle (eastward from north) [for navigators and solar radiation]



int sampa_function; //Switch to choose functions for desired output (from enumeration)

//---------INPUT VALUES required for estimated solar irradiances--------

double sampa_bird_ozone; //total column ozone thickness [cm] -- range from 0.05 - 0.4
double sampa_bird_pwv;   //total column water vapor [cm] -- range from 0.01 - 6.5 
double sampa_bird_aod;   //broadband aerosol optical depth -- range from 0.02 - 0.5
double sampa_bird_ba;	   //forward scattering factor -- 0.85 recommended for rural aerosols
double sampa_bird_albedo;//ground reflectance -- earth typical is 0.2, snow 0.9, vegitation 0.25 

//---------------------Final SAMPA OUTPUT VALUES------------------------

double sampa_ems; //local observed, topocentric, angular distance between sun and moon centers [degrees]
double sampa_rs;	//radius of sun disk [degrees]
double sampa_rm;  //radius of moon disk [degrees]

double sampa_a_sul;     //area of sun's unshaded lune (SUL) during eclipse [degrees squared]
double sampa_a_sul_pct; //percent area of SUL during eclipse [percent]

double sampa_dni;       //estimated direct normal solar irradiance using SERI/NREL Bird Clear Sky Model [W/m^2]
double sampa_dni_sul;   //estimated direct normal solar irradiance from the sun's unshaded lune [W/m^2]

double sampa_ghi;       //estimated global horizontal solar irradiance using SERI/NREL Bird Clear Sky Model [W/m^2]
double sampa_ghi_sul;   //estimated global horizontal solar irradiance from the sun's unshaded lune [W/m^2]

double sampa_dhi;       //estimated diffuse horizontal solar irradiance using SERI/NREL Bird Clear Sky Model [W/m^2]
double sampa_dhi_sul;   //estimated diffuse horizontal solar irradiance from the sun's unshaded lune [W/m^2]


#define PI    3.1415926535897932384626433832795028841971

#define COUNT 60

#define TERM_D 0
#define TERM_M 1
#define TERM_MPR 2
#define TERM_F 3
#define TERM_LB 4
#define TERM_R 5
#define TERM_COUNT_MPA 6

///////////////////////////////////////////////////////
///  Moon's Periodic Terms for Longitude and Distance
///////////////////////////////////////////////////////
const double ML_TERMS[COUNT][TERM_COUNT_MPA] =
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
const double MB_TERMS[COUNT][TERM_COUNT_MPA] =
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

///////////////////////////////////////////////

double rad2deg(double radians)
{
    return (180.0 / PI) * radians;
}

double deg2rad(double degrees)
{
    return (PI / 180.0) * degrees;
}

int integer(double value)
{
    return int(value);
}

double limit_degrees(double degrees)
{
    double limited;

    degrees /= 360.0;
    limited = 360.0 * (degrees - floor(degrees));
    if (limited < 0) limited += 360.0;

    return limited;
}

double limit_degrees180pm(double degrees)
{
    double limited;

    degrees /= 360.0;
    limited = 360.0 * (degrees - floor(degrees));
    if (limited < -180.0) limited += 360.0;
    else if (limited > 180.0) limited -= 360.0;

    return limited;
}

double limit_degrees180(double degrees)
{
    double limited;

    degrees /= 180.0;
    limited = 180.0 * (degrees - floor(degrees));
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
    double limited = minutes;

    if (limited < -20.0) limited += 1440.0;
    else if (limited > 20.0) limited -= 1440.0;

    return limited;
}

double dayfrac_to_local_hr(double dayfrac, double timezone)
{
    return 24.0 * limit_zero2one(dayfrac + timezone / 24.0);
}

double third_order_polynomial(double a, double b, double c, double d, double x)
{
    return ((a * x + b) * x + c) * x + d;
}

///////////////////////////////////////////////////////////////////////////////////////////////
int validate_inputs()
{
    if ((spa_year < -2000) || (spa_year > 6000)) return 1;
    if ((spa_month < 1) || (spa_month > 12)) return 2;
    if ((spa_day < 1) || (spa_day > 31)) return 3;
    if ((spa_hour < 0) || (spa_hour > 24)) return 4;
    if ((spa_minute < 0) || (spa_minute > 59)) return 5;
    if ((spa_second < 0) || (spa_second >= 60)) return 6;
    if ((spa_pressure < 0) || (spa_pressure > 5000)) return 12;
    if ((spa_temperature <= -273) || (spa_temperature > 6000)) return 13;
    if ((spa_delta_ut1 <= -1) || (spa_delta_ut1 >= 1)) return 17;
    if ((spa_hour == 24) && (spa_minute > 0)) return 5;
    if ((spa_hour == 24) && (spa_second > 0)) return 6;

    if (abs(spa_delta_t) > 8000) return 7;
    if (abs(spa_timezone) > 18) return 8;
    if (abs(spa_longitude) > 180) return 9;
    if (abs(spa_latitude) > 90) return 10;
    if (abs(spa_atmos_refract) > 5) return 16;
    if (spa_elevation < -6500000) return 11;

    if ((spa_function == SPA_ZA_INC) || (spa_function == SPA_ALL))
    {
        if (abs(spa_slope) > 360) return 14;
        if (abs(spa_azm_rotation) > 360) return 15;
    }

    return 0;
}
///////////////////////////////////////////////////////////////////////////////////////////////
double julian_day(int year, int month, int day, int hour, int minute, double second, double dut1, double tz)
{
    double day_decimal, julian_day, a;

    day_decimal = day + (hour - tz + (minute + (second + dut1) / 60.0) / 60.0) / 24.0;

    if (month < 3) {
        month += 12;
        year--;
    }

    julian_day = integer(365.25 * (year + 4716.0)) + integer(30.6001 * (month + 1)) + day_decimal - 1524.5;

    if (julian_day > 2299160.0) {
        a = integer(year / 100);
        julian_day += (2 - a + integer(a / 4));
    }

    return julian_day;
}

double julian_century(double jd)
{
    return (jd - 2451545.0) / 36525.0;
}

double julian_ephemeris_day(double jd, double delta_t)
{
    return jd + delta_t / 86400.0;
}

double julian_ephemeris_century(double jde)
{
    return (jde - 2451545.0) / 36525.0;
}

double julian_ephemeris_millennium(double jce)
{
    return (jce / 10.0);
}

double earth_periodic_term_summation_L(const double terms[L_MAX_SUBCOUNT][TERM_COUNT_SPA], int count, double jme)
{
    int i;
    double sum = 0;

    for (i = 0; i < count; i++)
        sum += terms[i][TERM_A] * cos(float(terms[i][TERM_B] + terms[i][TERM_C] * jme));

    return sum;
}

double earth_periodic_term_summation_R(const double terms[R_MAX_SUBCOUNT][TERM_COUNT_SPA], int count, double jme)
{
    int i;
    double sum = 0;

    for (i = 0; i < count; i++)
        sum += terms[i][TERM_A] * cos(float(terms[i][TERM_B] + terms[i][TERM_C] * jme));

    return sum;
}

double earth_periodic_term_summation_B(const double terms[B_MAX_SUBCOUNT][TERM_COUNT_SPA], int count, double jme)
{
    int i;
    double sum = 0;

    for (i = 0; i < count; i++)
        sum += terms[i][TERM_A] * cos(float(terms[i][TERM_B] + terms[i][TERM_C] * jme));

    return sum;
}

double earth_values_L(double term_sum[L_COUNT], int count, double jme)
{
    int i;
    double sum = 0;

    for (i = 0; i < count; i++)
        sum += term_sum[i] * pow(float(jme), i);

    sum /= 1.0e8;

    return sum;
}

double earth_values_B(double term_sum[B_COUNT], int count, double jme)
{
    int i;
    double sum = 0;

    for (i = 0; i < count; i++)
        sum += term_sum[i] * pow(float(jme), i);

    sum /= 1.0e8;

    return sum;
}

double earth_values_R(double term_sum[R_COUNT], int count, double jme)
{
    int i;
    double sum = 0;

    for (i = 0; i < count; i++)
        sum += term_sum[i] * pow(float(jme), i);

    sum /= 1.0e8;

    return sum;
}

double earth_heliocentric_longitude(double jme)
{
    double sum[L_COUNT];
    int i;

    for (i = 0; i < L_COUNT; i++)
        sum[i] = earth_periodic_term_summation_L(L_TERMS[i], l_subcount[i], jme);

    return limit_degrees(rad2deg(earth_values_L(sum, L_COUNT, jme)));

}

double earth_heliocentric_latitude(double jme)
{
    double sum[B_COUNT];
    int i;

    for (i = 0; i < B_COUNT; i++)
        sum[i] = earth_periodic_term_summation_B(B_TERMS[i], b_subcount[i], jme);

    return rad2deg(earth_values_B(sum, B_COUNT, jme));

}

double earth_radius_vector(double jme)
{
    double sum[R_COUNT];
    int i;

    for (i = 0; i < R_COUNT; i++)
        sum[i] = earth_periodic_term_summation_R(R_TERMS[i], r_subcount[i], jme);

    return earth_values_R(sum, R_COUNT, jme);

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
    return third_order_polynomial(1.0 / 189474.0, -0.0019142, 445267.11148, 297.85036, jce);
}

double mean_anomaly_sun(double jce)
{
    return third_order_polynomial(-1.0 / 300000.0, -0.0001603, 35999.05034, 357.52772, jce);
}

double mean_anomaly_moon(double jce)
{
    return third_order_polynomial(1.0 / 56250.0, 0.0086972, 477198.867398, 134.96298, jce);
}

double argument_latitude_moon(double jce)
{
    return third_order_polynomial(1.0 / 327270.0, -0.0036825, 483202.017538, 93.27191, jce);
}

double ascending_longitude_moon(double jce)
{
    return third_order_polynomial(1.0 / 450000.0, 0.0020708, -1934.136261, 125.04452, jce);
}

double xy_term_summation(int i, double x[TERM_X_COUNT])
{
    int j;
    double sum = 0;

    for (j = 0; j < TERM_Y_COUNT; j++)
        sum += x[j] * Y_TERMS[i][j];

    return sum;
}

void nutation_longitude_and_obliquity(double jce, double x[TERM_X_COUNT])
{
    int i;
    double xy_term_sum, sum_psi = 0, sum_epsilon = 0;

    for (i = 0; i < Y_COUNT; i++) {
        xy_term_sum = deg2rad(xy_term_summation(i, x));
        sum_psi += (PE_TERMS[i][TERM_PSI_A] + jce * PE_TERMS[i][TERM_PSI_B]) * sin(float(xy_term_sum));
        sum_epsilon += (PE_TERMS[i][TERM_EPS_C] + jce * PE_TERMS[i][TERM_EPS_D]) * cos(float(xy_term_sum));
    }

    spa_del_psi = sum_psi / 36000000.0;
    spa_del_epsilon = sum_epsilon / 36000000.0;
}

void nutation_longitude_and_obliquity_rts(double jce, double x[TERM_X_COUNT])
{
    int i;
    double xy_term_sum, sum_psi = 0, sum_epsilon = 0;

    for (i = 0; i < Y_COUNT; i++) {
        xy_term_sum = deg2rad(xy_term_summation(i, x));
        sum_psi += (PE_TERMS[i][TERM_PSI_A] + jce * PE_TERMS[i][TERM_PSI_B]) * sin(float(xy_term_sum));
        sum_epsilon += (PE_TERMS[i][TERM_EPS_C] + jce * PE_TERMS[i][TERM_EPS_D]) * cos(float(xy_term_sum));
    }

    rts_del_psi = sum_psi / 36000000.0;
    rts_del_epsilon = sum_epsilon / 36000000.0;
}

double ecliptic_mean_obliquity(double jme)
{
    double u = jme / 10.0;

    return 84381.448 + u * (-4680.93 + u * (-1.55 + u * (1999.25 + u * (-51.38 + u * (-249.67 +
        u * (-39.05 + u * (7.12 + u * (27.87 + u * (5.79 + u * 2.45)))))))));
}

double ecliptic_true_obliquity(double delta_epsilon, double epsilon0)
{
    return delta_epsilon + epsilon0 / 3600.0;
}

double aberration_correction(double r)
{
    return -20.4898 / (3600.0 * r);
}

double apparent_sun_longitude(double theta, double delta_psi, double delta_tau)
{
    return theta + delta_psi + delta_tau;
}

double greenwich_mean_sidereal_time(double jd, double jc)
{
    return limit_degrees(280.46061837 + 360.98564736629 * (jd - 2451545.0) +
        jc * jc * (0.000387933 - jc / 38710000.0));
}

double greenwich_sidereal_time(double nu0, double delta_psi, double epsilon)
{
    return nu0 + delta_psi * cos(float(deg2rad(epsilon)));
}

double geocentric_right_ascension(double lamda, double epsilon, double beta)
{
    double lamda_rad = deg2rad(lamda);
    double epsilon_rad = deg2rad(epsilon);

    return limit_degrees(rad2deg(atan(float(sin(float(lamda_rad)) * cos(float(epsilon_rad)) -
        tan(float(deg2rad(beta))) * sin(float(epsilon_rad))), float(cos(float(lamda_rad))))));
}

double geocentric_declination(double beta, double epsilon, double lamda)
{
    double beta_rad = deg2rad(beta);
    double epsilon_rad = deg2rad(epsilon);

    return rad2deg(asin(float(sin(float(beta_rad)) * cos(float(epsilon_rad)) +
        cos(float(beta_rad)) * sin(float(epsilon_rad)) * sin(float(deg2rad(lamda))))));
}

double observer_hour_angle(double nu, double longitude, double alpha_deg)
{
    return limit_degrees(nu + longitude - alpha_deg);
}

double sun_equatorial_horizontal_parallax(double r)
{
    return 8.794 / (3600.0 * r);
}

void right_ascension_parallax_and_topocentric_dec_spa(double latitude, double elevation,
    double xi, double h, double delta)
{
    double delta_alpha_rad;
    double lat_rad = deg2rad(latitude);
    double xi_rad = deg2rad(xi);
    double h_rad = deg2rad(h);
    double delta_rad = deg2rad(delta);
    double u = atan(float(0.99664719 * tan(float(lat_rad))));
    double y = 0.99664719 * sin(float(u)) + elevation * sin(float(lat_rad)) / 6378140.0;
    double x = cos(float(u)) + elevation * cos(float(lat_rad)) / 6378140.0;

    delta_alpha_rad = atan(float(-x * sin(float(xi_rad)) * sin(float(h_rad))),
        float(cos(float(delta_rad)) - x * sin(float(xi_rad)) * cos(float(h_rad))));

    spa_delta_prime = rad2deg(atan(float((sin(float(delta_rad)) - y * sin(float(xi_rad))) * cos(float(delta_alpha_rad))),
        float(cos(float(delta_rad)) - x * sin(float(xi_rad)) * cos(float(h_rad)))));

    spa_del_alpha = rad2deg(delta_alpha_rad);
}


void right_ascension_parallax_and_topocentric_dec_mpa(double latitude, double elevation,
    double xi, double h, double delta)
{
    double delta_alpha_rad;
    double lat_rad = deg2rad(latitude);
    double xi_rad = deg2rad(xi);
    double h_rad = deg2rad(h);
    double delta_rad = deg2rad(delta);
    double u = atan(float(0.99664719 * tan(float(lat_rad))));
    double y = 0.99664719 * sin(float(u)) + elevation * sin(float(lat_rad)) / 6378140.0;
    double x = cos(float(u)) + elevation * cos(float(lat_rad)) / 6378140.0;

    delta_alpha_rad = atan(float(-x * sin(float(xi_rad)) * sin(float(h_rad))),
        float(cos(float(delta_rad)) - x * sin(float(xi_rad)) * cos(float(h_rad))));

    mpa_delta_prime = rad2deg(atan(float((sin(float(delta_rad)) - y * sin(float(xi_rad))) * cos(float(delta_alpha_rad))),
        float(cos(float(delta_rad)) - x * sin(float(xi_rad)) * cos(float(h_rad)))));

    mpa_del_alpha = rad2deg(delta_alpha_rad);
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
    double lat_rad = deg2rad(latitude);
    double delta_prime_rad = deg2rad(delta_prime);

    return rad2deg(asin(float(sin(float(lat_rad)) * sin(float(delta_prime_rad)) +
        cos(float(lat_rad)) * cos(float(delta_prime_rad)) * cos(float(deg2rad(h_prime))))));
}

double atmospheric_refraction_correction(double pressure, double temperature,
    double atmos_refract, double e0)
{
    double del_e = 0;

    if (e0 >= -1 * (SUN_RADIUS + atmos_refract))
        del_e = (pressure / 1010.0) * (283.0 / (273.0 + temperature)) *
        1.02 / (60.0 * tan(float(deg2rad(e0 + 10.3 / (e0 + 5.11)))));

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
    double lat_rad = deg2rad(latitude);

    return limit_degrees(rad2deg(atan(float(sin(float(h_prime_rad))),
        float(cos(float(h_prime_rad)) * sin(float(lat_rad)) - tan(float(deg2rad(delta_prime))) * cos(float(lat_rad))))));
}

double topocentric_azimuth_angle(double azimuth_astro)
{
    return limit_degrees(azimuth_astro + 180.0);
}

double surface_incidence_angle(double zenith, double azimuth_astro, double azm_rotation,
    double slope)
{
    double zenith_rad = deg2rad(zenith);
    double slope_rad = deg2rad(slope);

    return rad2deg(acos(float(cos(float(zenith_rad)) * cos(float(slope_rad)) +
        sin(float(slope_rad)) * sin(float(zenith_rad)) * cos(float(deg2rad(azimuth_astro - azm_rotation))))));
}

double sun_mean_longitude(double jme)
{
    return limit_degrees(280.4664567 + jme * (360007.6982779 + jme * (0.03032028 +
        jme * (1 / 49931.0 + jme * (-1 / 15300.0 + jme * (-1 / 2000000.0))))));
}

double eot(double m, double alpha, double del_psi, double epsilon)
{
    return limit_minutes(4.0 * (m - 0.0057183 - alpha + del_psi * cos(float(deg2rad(epsilon)))));
}

double approx_sun_transit_time(double alpha_zero, double longitude, double nu)
{
    return (alpha_zero - longitude - nu) / 360.0;
}

double sun_hour_angle_at_rise_set(double latitude, double delta_zero, double h0_prime)
{
    double h0 = -99999;
    double latitude_rad = deg2rad(latitude);
    double delta_zero_rad = deg2rad(delta_zero);
    double argument = (sin(float(deg2rad(h0_prime))) - sin(float(latitude_rad)) * sin(float(delta_zero_rad))) /
        (cos(float(latitude_rad)) * cos(float(delta_zero_rad)));

    if (abs(argument) <= 1) h0 = limit_degrees180(rad2deg(acos(float(argument))));

    return h0;
}


double m_rts[3];

void approx_sun_rise_and_set(double h0)
{
    double h0_dfrac = h0 / 360.0;

    m_rts[SUN_RISE] = limit_zero2one(m_rts[SUN_TRANSIT] - h0_dfrac);
    m_rts[SUN_SET] = limit_zero2one(m_rts[SUN_TRANSIT] + h0_dfrac);
    m_rts[SUN_TRANSIT] = limit_zero2one(m_rts[SUN_TRANSIT]);
}


double alpha[JD_COUNT], delta[JD_COUNT];

double rts_alpha_delta_prime_alpha(double n)
{
    double a = alpha[JD_ZERO] - alpha[JD_MINUS];
    double b = alpha[JD_PLUS] - alpha[JD_ZERO];

    if (abs(a) >= 2.0) a = limit_zero2one(a);
    if (abs(b) >= 2.0) b = limit_zero2one(b);

    return alpha[JD_ZERO] + n * (a + b + (b - a) * n) / 2.0;
}

double rts_alpha_delta_prime_delta(double n)
{
    double a = delta[JD_ZERO] - delta[JD_MINUS];
    double b = delta[JD_PLUS] - delta[JD_ZERO];

    if (abs(a) >= 2.0) a = limit_zero2one(a);
    if (abs(b) >= 2.0) b = limit_zero2one(b);

    return delta[JD_ZERO] + n * (a + b + (b - a) * n) / 2.0;
}

double rts_sun_altitude(double latitude, double delta_prime, double h_prime)
{
    double latitude_rad = deg2rad(latitude);
    double delta_prime_rad = deg2rad(delta_prime);

    return rad2deg(asin(float(sin(float(latitude_rad)) * sin(float(delta_prime_rad)) +
        cos(float(latitude_rad)) * cos(float(delta_prime_rad)) * cos(float(deg2rad(h_prime))))));
}


double delta_prime[SUN_COUNT], h_prime[SUN_COUNT], h_rts[SUN_COUNT];

double sun_rise_and_set(double latitude, double h0_prime, int sun)
{
    return m_rts[sun] + (h_rts[sun] - h0_prime) /
        (360.0 * cos(float(deg2rad(delta_prime[sun]))) * cos(float(deg2rad(latitude))) * sin(float(deg2rad(h_prime[sun]))));
}

////////////////////////////////////////////////////////////////////////////////////////////////
// Calculate required SPA parameters to get the right ascension (alpha) and declination (delta)
// Note: JD must be already calculated and in structure
////////////////////////////////////////////////////////////////////////////////////////////////
void calculate_geocentric_sun_right_ascension_and_declination()
{
    double x[TERM_X_COUNT];

    spa_jc = julian_century(spa_jd);

    spa_jde = julian_ephemeris_day(spa_jd, spa_delta_t);
    spa_jce = julian_ephemeris_century(spa_jde);
    spa_jme = julian_ephemeris_millennium(spa_jce);

    spa_l = earth_heliocentric_longitude(spa_jme);
    spa_b = earth_heliocentric_latitude(spa_jme);
    spa_r = earth_radius_vector(spa_jme);

    spa_theta = geocentric_longitude(spa_l);
    spa_beta = geocentric_latitude(spa_b);

    x[TERM_X0] = spa_x0 = mean_elongation_moon_sun(spa_jce);
    x[TERM_X1] = spa_x1 = mean_anomaly_sun(spa_jce);
    x[TERM_X2] = spa_x2 = mean_anomaly_moon(spa_jce);
    x[TERM_X3] = spa_x3 = argument_latitude_moon(spa_jce);
    x[TERM_X4] = spa_x4 = ascending_longitude_moon(spa_jce);

    nutation_longitude_and_obliquity(spa_jce, x);

    spa_epsilon0 = ecliptic_mean_obliquity(spa_jme);
    spa_epsilon = ecliptic_true_obliquity(spa_del_epsilon, spa_epsilon0);

    spa_del_tau = aberration_correction(spa_r);
    spa_lamda = apparent_sun_longitude(spa_theta, spa_del_psi, spa_del_tau);
    spa_nu0 = greenwich_mean_sidereal_time(spa_jd, spa_jc);
    spa_nu = greenwich_sidereal_time(spa_nu0, spa_del_psi, spa_epsilon);

    spa_alpha = geocentric_right_ascension(spa_lamda, spa_epsilon, spa_beta);
    spa_delta = geocentric_declination(spa_beta, spa_epsilon, spa_lamda);
}


void calculate_geocentric_sun_right_ascension_and_declination_rts()
{
    double x[TERM_X_COUNT];

    rts_jc = julian_century(rts_jd);

    rts_jde = julian_ephemeris_day(rts_jd, rts_delta_t);
    rts_jce = julian_ephemeris_century(rts_jde);
    rts_jme = julian_ephemeris_millennium(rts_jce);

    rts_l = earth_heliocentric_longitude(rts_jme);
    rts_b = earth_heliocentric_latitude(rts_jme);
    rts_r = earth_radius_vector(rts_jme);

    rts_theta = geocentric_longitude(rts_l);
    rts_beta = geocentric_latitude(rts_b);

    x[TERM_X0] = rts_x0 = mean_elongation_moon_sun(rts_jce);
    x[TERM_X1] = rts_x1 = mean_anomaly_sun(rts_jce);
    x[TERM_X2] = rts_x2 = mean_anomaly_moon(rts_jce);
    x[TERM_X3] = rts_x3 = argument_latitude_moon(rts_jce);
    x[TERM_X4] = rts_x4 = ascending_longitude_moon(rts_jce);

    nutation_longitude_and_obliquity_rts(rts_jce, x);

    rts_epsilon0 = ecliptic_mean_obliquity(rts_jme);
    rts_epsilon = ecliptic_true_obliquity(rts_del_epsilon, rts_epsilon0);

    rts_del_tau = aberration_correction(rts_r);
    rts_lamda = apparent_sun_longitude(rts_theta, rts_del_psi, rts_del_tau);
    rts_nu0 = greenwich_mean_sidereal_time(rts_jd, rts_jc);
    rts_nu = greenwich_sidereal_time(rts_nu0, rts_del_psi, rts_epsilon);

    rts_alpha = geocentric_right_ascension(rts_lamda, rts_epsilon, rts_beta);
    rts_delta = geocentric_declination(rts_beta, rts_epsilon, rts_lamda);
}

////////////////////////////////////////////////////////////////////////
// Calculate Equation of Time (EOT) and Sun Rise, Transit, & Set (RTS)
////////////////////////////////////////////////////////////////////////

void calculate_eot_and_sun_rise_transit_set()
{

    double nu, m, h0, n;
    double nu_rts[SUN_COUNT];
    double alpha_prime[SUN_COUNT];
    double h0_prime = -1 * (SUN_RADIUS + spa_atmos_refract);
    int i;

    CopySpaToRts();
    m = sun_mean_longitude(spa_jme);
    spa_eot = eot(m, spa_alpha, spa_del_psi, spa_epsilon);

    rts_hour = rts_minute = 1;
    rts_second = 0.0;
    rts_delta_ut1 = rts_timezone = 0.0;

    rts_jd = julian_day(rts_year, rts_month, rts_day, rts_hour,
        rts_minute, rts_second, rts_delta_ut1, rts_timezone);

    calculate_geocentric_sun_right_ascension_and_declination_rts();
    nu = rts_nu;

    rts_delta_t = 0;
    rts_jd--;
    for (i = 0; i < JD_COUNT; i++) {
        calculate_geocentric_sun_right_ascension_and_declination_rts();
        alpha[i] = rts_alpha;
        delta[i] = rts_delta;
        rts_jd++;
    }

    m_rts[SUN_TRANSIT] = approx_sun_transit_time(alpha[JD_ZERO], spa_longitude, nu);
    h0 = sun_hour_angle_at_rise_set(spa_latitude, delta[JD_ZERO], h0_prime);

    if (h0 >= 0) {

        approx_sun_rise_and_set(h0);

        for (i = 0; i < SUN_COUNT; i++) {

            nu_rts[i] = nu + 360.985647 * m_rts[i];

            n = m_rts[i] + spa_delta_t / 86400.0;
            alpha_prime[i] = rts_alpha_delta_prime_alpha(n);
            delta_prime[i] = rts_alpha_delta_prime_delta(n);

            h_prime[i] = limit_degrees180pm(nu_rts[i] + spa_longitude - alpha_prime[i]);

            h_rts[i] = rts_sun_altitude(spa_latitude, delta_prime[i], h_prime[i]);
        }

        spa_srha = h_prime[SUN_RISE];
        spa_ssha = h_prime[SUN_SET];
        spa_sta = h_rts[SUN_TRANSIT];

        spa_suntransit = dayfrac_to_local_hr(m_rts[SUN_TRANSIT] - h_prime[SUN_TRANSIT] / 360.0,
            spa_timezone);

        spa_sunrise = dayfrac_to_local_hr(sun_rise_and_set(
            spa_latitude, h0_prime, SUN_RISE), spa_timezone);

        spa_sunset = dayfrac_to_local_hr(sun_rise_and_set(
            spa_latitude, h0_prime, SUN_SET), spa_timezone);

    }
    else spa_srha = spa_ssha = spa_sta = spa_suntransit = spa_sunrise = spa_sunset = -99999;

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
        spa_jd = julian_day(spa_year, spa_month, spa_day, spa_hour,
            spa_minute, spa_second, spa_delta_ut1, spa_timezone);

        calculate_geocentric_sun_right_ascension_and_declination();

        spa_h = observer_hour_angle(spa_nu, spa_longitude, spa_alpha);
        spa_xi = sun_equatorial_horizontal_parallax(spa_r);

        right_ascension_parallax_and_topocentric_dec_spa(spa_latitude, spa_elevation, spa_xi,
            spa_h, spa_delta);

        spa_alpha_prime = topocentric_right_ascension(spa_alpha, spa_del_alpha);
        spa_h_prime = topocentric_local_hour_angle(spa_h, spa_del_alpha);

        spa_e0 = topocentric_elevation_angle(spa_latitude, spa_delta_prime, spa_h_prime);
        spa_del_e = atmospheric_refraction_correction(spa_pressure, spa_temperature,
            spa_atmos_refract, spa_e0);
        spa_e = topocentric_elevation_angle_corrected(spa_e0, spa_del_e);

        spa_zenith = topocentric_zenith_angle(spa_e);
        spa_azimuth_astro = topocentric_azimuth_angle_astro(spa_h_prime, spa_latitude,
            spa_delta_prime);
        spa_azimuth = topocentric_azimuth_angle(spa_azimuth_astro);

        if ((spa_function == SPA_ZA_INC) || (spa_function == SPA_ALL))
            spa_incidence = surface_incidence_angle(spa_zenith, spa_azimuth_astro,
                spa_azm_rotation, spa_slope);

        if ((spa_function == SPA_ZA_RTS) || (spa_function == SPA_ALL))
            calculate_eot_and_sun_rise_transit_set();
    }

    return result;
}




///////////////////////////////////////////////////////////////////////////////////////////////

double fourth_order_polynomial(double a, double b, double c, double d, double e, double x)
{
    return (((a * x + b) * x + c) * x + d) * x + e;
}

double moon_mean_longitude(double jce)
{
    return limit_degrees(fourth_order_polynomial(
        -1.0 / 65194000, 1.0 / 538841, -0.0015786, 481267.88123421, 218.3164477, jce));
}

double moon_mean_elongation(double jce)
{
    return limit_degrees(fourth_order_polynomial(
        -1.0 / 113065000, 1.0 / 545868, -0.0018819, 445267.1114034, 297.8501921, jce));
}

double sun_mean_anomaly(double jce)
{
    return limit_degrees(third_order_polynomial(
        1.0 / 24490000, -0.0001536, 35999.0502909, 357.5291092, jce));
}

double moon_mean_anomaly(double jce)
{
    return limit_degrees(fourth_order_polynomial(
        -1.0 / 14712000, 1.0 / 69699, 0.0087414, 477198.8675055, 134.9633964, jce));
}

double moon_latitude_argument(double jce)
{
    return limit_degrees(fourth_order_polynomial(
        1.0 / 863310000, -1.0 / 3526000, -0.0036539, 483202.0175233, 93.2720950, jce));
}

void moon_periodic_term_summation_l_r(double d, double m, double m_prime, double f, double jce,
    const double terms[COUNT][TERM_COUNT_MPA])
{
    int i;
    double e_mult, trig_arg;
    double e = 1.0 - jce * (0.002516 + jce * 0.0000074);

    mpa_l = 0;
    mpa_r = 0;
    for (i = 0; i < COUNT; i++)
    {
        e_mult = pow(float(e), float(abs(terms[i][TERM_M])));
        trig_arg = deg2rad(terms[i][TERM_D] * d + terms[i][TERM_M] * m +
            terms[i][TERM_F] * f + terms[i][TERM_MPR] * m_prime);
        mpa_l += e_mult * terms[i][TERM_LB] * sin(float(trig_arg));
        mpa_r += e_mult * terms[i][TERM_R] * cos(float(trig_arg));
    }
}

void moon_periodic_term_summation_b(double d, double m, double m_prime, double f, double jce,
    const double terms[COUNT][TERM_COUNT_MPA])
{
    int i;
    double e_mult, trig_arg;
    double e = 1.0 - jce * (0.002516 + jce * 0.0000074);

    mpa_b = 0;
    for (i = 0; i < COUNT; i++)
    {
        e_mult = pow(float(e), float(abs(terms[i][TERM_M])));
        trig_arg = deg2rad(terms[i][TERM_D] * d + terms[i][TERM_M] * m +
            terms[i][TERM_F] * f + terms[i][TERM_MPR] * m_prime);
        mpa_b += e_mult * terms[i][TERM_LB] * sin(float(trig_arg));
    }
}

void moon_longitude_and_latitude(double jce, double l_prime, double f, double m_prime, double l, double b)
{
    double a1 = 119.75 + 131.849 * jce;
    double a2 = 53.09 + 479264.290 * jce;
    double a3 = 313.45 + 481266.484 * jce;
    double delta_l = 3958 * sin(float(deg2rad(a1))) + 318 * sin(float(deg2rad(a2))) + 1962 * sin(float(deg2rad(l_prime - f)));
    double delta_b = -2235 * sin(float(deg2rad(l_prime))) + 175 * sin(float(deg2rad(a1 - f))) + 127 * sin(float(deg2rad(l_prime - m_prime)))
        + 382 * sin(float(deg2rad(a3))) + 175 * sin(float(deg2rad(a1 + f))) - 115 * sin(float(deg2rad(l_prime + m_prime)));

    mpa_lamda_prime = limit_degrees(l_prime + (l + delta_l) / 1000000);
    mpa_beta = limit_degrees((b + delta_b) / 1000000);
}

double moon_earth_distance(double r)
{
    return 385000.56 + r / 1000;
}

double moon_equatorial_horiz_parallax(double delta)
{
    return rad2deg(asin(float(6378.14 / delta)));
}

double apparent_moon_longitude(double lamda_prime, double del_psi)
{
    return lamda_prime + del_psi;
}

double angular_distance_sun_moon(double zen_sun, double azm_sun, double zen_moon, double azm_moon)
{
    double zs = deg2rad(zen_sun);
    double zm = deg2rad(zen_moon);

    return rad2deg(acos(float(cos(float(zs))) * cos(float(zm)) + sin(float(zs)) * sin(float(zm)) * cos(float(deg2rad(azm_sun - azm_moon)))));
}

double sun_disk_radius(double r)
{
    return 959.63 / (3600.0 * r);
}

double moon_disk_radius(double e, double pi, double cap_delta)
{
    return 358473400 * (1 + sin(float(deg2rad(e))) * sin(float(deg2rad(pi)))) / (3600.0 * cap_delta);
}

void sul_area(double ems, double rs, double rm)
{
    double ems2 = ems * ems;
    double rs2 = rs * rs;
    double rm2 = rm * rm;
    double snum, ai, m, s, h;

    if (ems < (rs + rm))
    {
        if (ems <= abs(rs - rm))
            ai = PI * rm2;
        else {
            snum = ems2 + rs2 - rm2;
            m = (ems2 - rs2 + rm2) / (2 * ems);
            s = snum / (2 * ems);
            h = sqrt(4 * ems2 * rs2 - snum * snum) / (2 * ems);
            ai = (rs2 * acos(float(s / rs)) - h * s + rm2 * acos(float(m / rm)) - h * m);
        }
    }
    else ai = 0;

    sampa_a_sul = PI * rs2 - ai;
    if (sampa_a_sul < 0) sampa_a_sul = 0;
    sampa_a_sul_pct = sampa_a_sul * 100.0 / (PI * rs2);
}

///////////////////////////////////////////////////////////////////////////////////////////
// Calculate all MPA parameters and put into structure
// Note: All inputs values (listed in SPA header file) must already be in structure
///////////////////////////////////////////////////////////////////////////////////////////
void mpa_calculate()
{
    mpa_l_prime = moon_mean_longitude(spa_jce);
    mpa_d = moon_mean_elongation(spa_jce);
    mpa_m = sun_mean_anomaly(spa_jce);
    mpa_m_prime = moon_mean_anomaly(spa_jce);
    mpa_f = moon_latitude_argument(spa_jce);

    moon_periodic_term_summation_l_r(mpa_d, mpa_m, mpa_m_prime, mpa_f, spa_jce, ML_TERMS);
    moon_periodic_term_summation_b(mpa_d, mpa_m, mpa_m_prime, mpa_f, spa_jce, MB_TERMS);

    moon_longitude_and_latitude(spa_jce, mpa_l_prime, mpa_f, mpa_m_prime, mpa_l, mpa_b);

    mpa_cap_delta = moon_earth_distance(mpa_r);
    mpa_pi = moon_equatorial_horiz_parallax(mpa_cap_delta);

    mpa_lamda = apparent_moon_longitude(mpa_lamda_prime, spa_del_psi);

    mpa_alpha = geocentric_right_ascension(mpa_lamda, spa_epsilon, mpa_beta);
    mpa_delta = geocentric_declination(mpa_beta, spa_epsilon, mpa_lamda);

    mpa_h = observer_hour_angle(spa_nu, spa_longitude, mpa_alpha);

    right_ascension_parallax_and_topocentric_dec_mpa(spa_latitude, spa_elevation, mpa_pi,
        mpa_h, mpa_delta);
    mpa_alpha_prime = topocentric_right_ascension(mpa_alpha, mpa_del_alpha);
    mpa_h_prime = topocentric_local_hour_angle(mpa_h, mpa_del_alpha);

    mpa_e0 = topocentric_elevation_angle(spa_latitude, mpa_delta_prime, mpa_h_prime);
    mpa_del_e = atmospheric_refraction_correction(spa_pressure, spa_temperature,
        spa_atmos_refract, mpa_e0);
    mpa_e = topocentric_elevation_angle_corrected(mpa_e0, mpa_del_e);

    mpa_zenith = topocentric_zenith_angle(mpa_e);
    mpa_azimuth_astro = topocentric_azimuth_angle_astro(mpa_h_prime, spa_latitude, mpa_delta_prime);
    mpa_azimuth = topocentric_azimuth_angle(mpa_azimuth_astro);
}

///////////////////////////////////////////////////////////////////////////////////////////
// Calculate all SAMPA parameters and put into structure
// Note: All inputs values (listed in SPA header file) must already be in structure
///////////////////////////////////////////////////////////////////////////////////////////
int sampa_calculate()
{
    int result;

    spa_function = SPA_ALL;
    result = spa_calculate();

    if (result == 0)
    {
        mpa_calculate();

        sampa_ems = angular_distance_sun_moon(spa_zenith, spa_azimuth,
            mpa_zenith, mpa_azimuth);
        sampa_rs = sun_disk_radius(spa_r);
        sampa_rm = moon_disk_radius(mpa_e, mpa_pi, mpa_cap_delta);

        sul_area(sampa_ems, sampa_rs, sampa_rm);

    }

    return result;
}


