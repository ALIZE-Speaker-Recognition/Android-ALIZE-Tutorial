<img src="http://alize.univ-avignon.fr/images/alize-logo.png" alt="The ALIZÉ logo" height="198" >

# ALIZÉ for Android — Tutorial

*This package is part of the ALIZÉ project: <http://alize.univ-avignon.fr>*



Welcome to ALIZÉ
----------------

ALIZÉ is a software platform for automatic speaker recognition. It can be used for carrying out research in this field, as well as for incorporating speaker recognition into applications.

<http://alize.univ-avignon.fr>


ALIZÉ for Android
-----------------

ALIZÉ for Android offers access to high-level APIs which allow to run a speaker detection system (for speaker verification/identification) on the Android platform. Through this API, a user can feed the system audio data and use it to train speaker models, and run speaker verification and identification tasks.


The tutorial
------------

This application serves as a basic tutorial for the API, showing how to use the various methods (and what for).
It is distributed as an application so that it is easy to play with the code and run it with your modifications, but the application itself presents no interest, as running it does nothing more than executing a scripted sequence of speaker recognition tasks and logging the results.

The part of interest is the code (and comments) in the onCreate() method in MainActivity.java. It unrolls a typical session of speaker recognition with ALIZÉ, from system initialization to speaker verification or identification, through the creation or adaptation of speaker models. For convenience, the code is all in one place instead of being scattered in several methods as in a real application.
