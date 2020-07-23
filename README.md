# icm
icm watches your internet connectivity and logs each outage. It will optionally play .wav files on connect and disconnect.
This is a hard fork of "Internet Connectivity Montitor" 1.41 with many fundamental changes (see https://code.google.com/archive/p/internetconnectivitymonitor/).
It includes source from jhlabs, please see http://www.jhlabs.com/java/layout/

This monitor will test DNS resolution every 7 seconds, and on failure, it will test every 1 second until it has succeeded 5 times. 
Values and urls are configurable in the properties file.

Note that if your router or internal infrastructure caches DNS, this will not successfully verify your internet connection. 
I suggest setting your local machine to use a remote DNS server rather than your router in this case, such as Google DNS (https://developers.google.com/speed/public-dns).

In process:
Switch DNS to use DNSJava so that we can specify the DNS server to use
Implement GET call to URLs on success and failure
Fix audio file references to use configured values
