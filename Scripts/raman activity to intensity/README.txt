This script takes the ASCII file with the Raman activity extracted using either db_rs.sh or 
full_db_rs.sh or ga_rs.sh and converts them in ASCII files with the Raman intensity, given a 
particular excitation energy.

The conversion is done according to: Chaitanya et al, J. At. Mol. Sci. 3 (2012), 2012
Similar conversions schemes can be found in: Porezag et al. PRB 54 (1996), 7830


To enable the use of floating calculations in bash (via bc), a set of environmental 
variables are need to be declared. Instructions follow:

First, make sure bc loads the math library by default. In .bashrc (or .profile in OSX) add:

        alias bc='bc -l'

Add this line to the .bashrc (or .profile in OSX) to load the default settings in bash:

        export BC_ENV_ARGS="$HOME/.bcrc"

Create in "~" a file .bcrc and add the following. This will make available to bc all 
additional parameters needed.



scale=40  
k_c = 299792458 /* Speed of Light */
k_g = 6.67384 * 10^-11 /* Universal Gravitation */
k_atm = 100325 /* Atmospheric pressure */
k_h = 6.62606957 * 10^-34 /* Planck's constant */
k_hbar = 1.054571726 * 10^-34 /* H Bar */
k_mu = 1.256637061 * 10^-6 /* Vacuum permeability */
k_ep = 8.854187817 * 10^-12 /* Vacuum permittivity */
k_epsilon = 8.854187817 * 10^-12 /* Vacuum permittivity */
k_e = 1.602176565 * 10^-19 /* Elementary charge */
k_coulomb = 8.987551787 * 10^9 /* Coulomb's constant */
k_me = 9.10938294 * 10^-31 /* Rest mass of an electron */
k_mp = 1.672621777 * 10^-27 /* Rest mass of a proton */
k_n = 6.02214129 * 10^23 /* Avogadro's number */
k_b = 1.3806488 * 10^-23 /* Boltzmann's constant */
k_r = 8.3144621 /* Ideal gas constant */
k_si = 5.670373 * 10^-8 /* Stefan-Boltzmann constant */
k_sigma = 5.670373 * 10^-8 /* Stefan-Boltzmann constant */

pi = 3.1415926535897932384626433832795028841968 /* pi */ 

define round(n,m)
{
    oldscale = scale
    scale = m
    rounded = n / 1
    scale = oldscale
    return rounded
} 
