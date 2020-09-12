# icm
icm (Internet Connectivity Monitoring) monitors your internet connection uptime and logs each outage. If your internet has been unstable or unreliable icm can help identify how often and for how long your connection is down. It will optionally play .wav files and/or hit GET endpoints on connect and disconnect. Although a windows executable is provided, it is 100% java and should be portable across platforms via the jar distribution.<br/>

Connectivity is monitored by testing DNS resolution every 7 seconds, and on failure, it will test every 1 second until it has succeeded 5 times.
Values, sound clips and urls are configurable in the properties file. By default, it will create a "log" folder in the current directory on startup. This can be altered via the UI "save path" setting.<br/>

The GET endpoint settings can be used as home automation hooks, either notifying you of the outage or activating a backup router. Make sure these URLs are available on your local network.<br/>

DNS resolution is via the DNSJava library and (by default) uses Google, Cloudflare, Quad9 and OpenDNS servers in rotation. 
<br/>
<p/>

<h2>Credits</h2>
This is a hard fork of "Internet Connectivity Montitor" 1.41 with many fundamental changes (see https://code.google.com/archive/p/internetconnectivitymonitor/).<br/>
It includes source from jhlabs, please see http://www.jhlabs.com/java/layout/.<br/>
