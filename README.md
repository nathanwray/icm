# icm
icm (Internet Connectivity Monitoring) monitors your internet connectivity and logs each outage. It will optionally play .wav files and/or hit GET endpoints on connect and disconnect.<br/>

Connectivity is monitored by testing DNS resolution every 7 seconds, and on failure, it will test every 1 second until it has succeeded 5 times.
Values, sound clips and urls are configurable in the properties file.<br/>

Note that if your router or internal infrastructure caches DNS, resolution will not successfully verify your internet connection. 
I suggest setting your local machine to use a remote DNS server rather than your router in this case, such as Google DNS (https://developers.google.com/speed/public-dns).
<br/>
<p/>
In process:<br/>
<ul>
<li>
  Switch DNS to use DNSJava so that we can specify the DNS server to use
  </li>
</ul>

<h2>Credits</h2>
This is a hard fork of "Internet Connectivity Montitor" 1.41 with many fundamental changes (see https://code.google.com/archive/p/internetconnectivitymonitor/).<br/>
It includes source from jhlabs, please see http://www.jhlabs.com/java/layout/.<br/>
